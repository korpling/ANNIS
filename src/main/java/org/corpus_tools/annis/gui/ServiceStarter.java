/*
 * Copyright 2020 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corpus_tools.annis.gui;

import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import com.google.common.base.Objects;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PreDestroy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@Profile("!desktop")
public class ServiceStarter implements ApplicationListener<ApplicationReadyEvent> {


  private static final String DATABASE_SECTION = "database";

  private static final String LOGGING_SECTION = "logging";

  private final static Logger log = LoggerFactory.getLogger(ServiceStarter.class);

  @Autowired
  protected UIConfig config;

  @Autowired
  private ResourceLoader resourceLoader;
  private final AtomicBoolean abortThread = new AtomicBoolean();

  private Process backgroundProcess;

  private Thread tReaderOut;

  private Thread tReaderErr;

  private Timer serviceWatcherTimer;


  @Override
  public void onApplicationEvent(final ApplicationReadyEvent event) {
    if (config.getWebserviceUrl() == null || config.getWebserviceUrl().isEmpty()) {
      startService();
      serviceWatcherTimer = new Timer(true);
      TimerTask serviceWatcherTask = new TimerTask() {

        @Override
        public void run() {
          // Check if the process is crashed and not manually shutdown
          if (config.isRestartBackendOnCrash() && !backgroundProcess.isAlive()
              && !abortThread.get()) {
            // Restart the whole process
            log.warn("It seems the bundled graphANNIS service has crashed, restarting it.");
            startService();
          }
        }
      };
      serviceWatcherTimer.schedule(serviceWatcherTask, 2500, 2500);
    }
  }


  /**
   * Start the bundled web service
   */
  private void startService() {
    try {
      // Extract the bundled resource to a temporary file
      Optional<String> execPath = executablePathForSystem();

      if (execPath.isPresent()) {
        File tmpExec = File.createTempFile("graphannis-webservice-", "");
        Resource resource = resourceLoader.getResource("classpath:" + execPath.get());
        if (resource.exists()) {
          log.info("Extracting the bundled graphANNIS service to {}", tmpExec.getAbsolutePath());
          FileUtils.copyInputStreamToFile(resource.getInputStream(), tmpExec);
          if (!tmpExec.setExecutable(true)) {
            log.warn("Could not mark the bundled graphANNIS service as executable");
          }

          // If the configuration does not exist, create an empty file
          File serviceConfigFile = getServiceConfig();

          // Start the process and read output/error stream in background threads
          log.info("Starting the bundled graphANNIS service with configuration file {}",
              serviceConfigFile.getAbsolutePath());
          ProcessBuilder backgroundProcessBuilder;

          if (SystemUtils.IS_OS_MAC_OSX) {
            // On MacOS has several processor architectors, but allows to emulate x86_64 processors
            // with the Rosetta tool. We use the `arch` helper program to trigger emulation if
            // necessary. This is necessary when Java support the native processor architecture and
            // thus is not emulated yet.
            backgroundProcessBuilder = new ProcessBuilder("arch", "-arch x86_64",
                tmpExec.getAbsolutePath(), "--config", serviceConfigFile.getAbsolutePath());

          } else {
            backgroundProcessBuilder = new ProcessBuilder(tmpExec.getAbsolutePath(), "--config",
                serviceConfigFile.getAbsolutePath());
          }
          backgroundProcess = backgroundProcessBuilder.start();

          // Create threads that read from the output and error streams and add the messages to
          // our log
          this.tReaderOut = createOutputWatcherThread(backgroundProcess.getInputStream(), false);
          this.tReaderErr = createOutputWatcherThread(backgroundProcess.getErrorStream(), true);

          // Use the provided service configuration to get the correct port
          TomlMapper mapper = new TomlMapper();
          Map<?, ?> parsedServiceConfig = mapper.readValue(serviceConfigFile, Map.class);
          config.setWebserviceUrl(getServiceURL(parsedServiceConfig));
        }
      }
    } catch (final IOException ex) {
      log.error(
          "Could not start integrated graphANNIS service, configure \"annis.webservice-url\" to set an existing service.",
          ex);
    }

  }

  /**
   * Get the system-specific relative path to the bundled resource.
   * 
   * @return The path to the executable if system is supported. Empty if architecture is not
   *         supported.
   */
  private Optional<String> executablePathForSystem() {
    Optional<String> execPath = Optional.empty();
    if ("amd64".equals(SystemUtils.OS_ARCH) || "x86_64".equals(SystemUtils.OS_ARCH)) {
      if (SystemUtils.IS_OS_LINUX) {
        execPath = Optional.of("linux-x86-64/graphannis-webservice");
      } else if (SystemUtils.IS_OS_MAC_OSX) {
        execPath = Optional.of("darwin/graphannis-webservice.osx");
      } else if (SystemUtils.IS_OS_WINDOWS) {
        execPath = Optional.of("win32-x86-64/graphannis-webservice.exe");
      }
    } else {
      log.error(
          "GraphANNIS can only be run on 64 bit operating systems (\"amd64\" or \"x86_64\") "
              + "and with a 64 bit version of Java, " + "but this is reported as architecture {}!",
          SystemUtils.OS_ARCH);
    }
    return execPath;
  }

  private Thread createOutputWatcherThread(InputStream stream, boolean isError) {
    final BufferedReader bufferedStream =
        new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));


    Thread result = new Thread(() -> {
      while (!this.abortThread.get()) {
        String line;
        try {
          line = bufferedStream.readLine();
          if (line != null) {
            if (isError) {
              log.error(line);
            } else {
              log.info(line);
            }
          }
        } catch (IOException ex) {
          if (!this.abortThread.get()) {
            log.error("Could not read service output", ex);
          }
          break;
        }
        Thread.yield();
      }
    });
    result.start();
    return result;
  }


  protected String getServiceURL(Map<?, ?> serviceConfig) {
    long port = 5711l;
    Object bindSection = serviceConfig.get("bind");
    if (bindSection instanceof Map) {
      @SuppressWarnings("rawtypes")
      Object portRaw = ((Map) bindSection).get("port");
      if (portRaw instanceof Long) {
        port = (Long) portRaw;
      }
    }
    return "http://localhost:" + port + "/v1";
  }

  @SuppressWarnings("unchecked")
  protected File getServiceConfig() throws IOException {
    File existingConfigFile = new File(config.getWebserviceConfig());

    final TomlMapper mapper = new TomlMapper();

    boolean writeTemporaryConfigFile = false;

    // Read existing file or create empty configuration
    final Map<Object, Object> serviceConfig;
    if (existingConfigFile.exists()) {
      serviceConfig = mapper.readValue(existingConfigFile, Map.class);
    } else {
      serviceConfig = new LinkedHashMap<>();
      writeTemporaryConfigFile = true;
    }

    // Set to a default data folder and SQLite file
    Object databaseConfigRaw = serviceConfig.get(DATABASE_SECTION);
    final Map<Object, Object> databaseConfig;
    if (databaseConfigRaw instanceof Map) {
      databaseConfig = (Map<Object, Object>) databaseConfigRaw;
    } else {
      // Create a new map instead of re-using the existing one
      databaseConfig = new LinkedHashMap<>();
      serviceConfig.put(DATABASE_SECTION, databaseConfig);
      writeTemporaryConfigFile = true;
    }
    // Add the graphannis data and sqlite location of not existing yet
    String defaultGraphannisLocation =
        Paths.get(System.getProperty("user.home"), ".annis", "v4").toAbsolutePath().toString();
    Object overwrittenGraphannisLocation =
        databaseConfig.putIfAbsent("graphannis", defaultGraphannisLocation);

    String defaultSqliteLocation =
        Paths.get(System.getProperty("user.home"), ".annis", "v4", "service_data.sqlite3")
            .toAbsolutePath().toString();
    Object overwrittenSqliteLocation = databaseConfig.putIfAbsent("sqlite", defaultSqliteLocation);

    if (overwrittenGraphannisLocation == null || overwrittenSqliteLocation == null) {
      // If any of the configuration values did not exist yet,
      // we have to write a new configuration file with the default location
      writeTemporaryConfigFile = true;
    }

    // Change service debug level if ANNIS itself is in debug mode
    Object loggingConfigRaw = serviceConfig.get(LOGGING_SECTION);
    final Map<Object, Object> loggingConfig;
    if (loggingConfigRaw instanceof Map) {
      loggingConfig = (Map<Object, Object>) loggingConfigRaw;
    } else {
      loggingConfig = new LinkedHashMap<>();
      serviceConfig.put(LOGGING_SECTION, loggingConfig);
    }
    Object debugConfigRaw = loggingConfig.get("debug");
    if (!Objects.equal(debugConfigRaw, log.isDebugEnabled())) {
      // Overwrite the original configuration to make sure the graphANNIS webservice has the same
      // level as the ANNIS front-end
      loggingConfig.put("debug", log.isDebugEnabled());
      writeTemporaryConfigFile = true;
    }

    if (writeTemporaryConfigFile) {
      // Write updated configuration to a new temporary file
      File tmpConfigFile = File.createTempFile("annis-service-config-", ".toml");
      tmpConfigFile.deleteOnExit();
      if (existingConfigFile.exists()) {
        log.info("Writing adjusted service configuration file {} -> {}.",
            existingConfigFile.getAbsolutePath(), tmpConfigFile.getAbsolutePath());
      } else {
        log.info("Writing default service configuration file to {}",
            tmpConfigFile.getAbsolutePath());
      }
      mapper.writeValue(tmpConfigFile, serviceConfig);
      return tmpConfigFile;
    } else {
      // Return the original configuration file, which did not need to be changed
      log.info("Using unchanged service configuration file {}",
          existingConfigFile.getAbsolutePath());
      return existingConfigFile;
    }
  }

  @PreDestroy
  private void destroy() throws InterruptedException {
    this.abortThread.set(true);

    if (this.serviceWatcherTimer != null) {
      this.serviceWatcherTimer.cancel();
      int purgedTasks = this.serviceWatcherTimer.purge();
      log.debug("Cancelled {} graphANNIS service watchdog tasks", purgedTasks);
    }

    if (this.tReaderOut != null) {
      this.tReaderOut.interrupt();
    }

    if (this.tReaderErr != null) {
      this.tReaderErr.interrupt();
    }

    if (this.backgroundProcess != null) {
      log.info("Attempting to stop graphANNIS process");
      this.backgroundProcess.destroy();
      if (!this.backgroundProcess.waitFor(15, TimeUnit.SECONDS)) {
        // Destroy the process by force after 15 seconds
        log.warn("GraphANNIS process did not stop after 15 seconds, stopping it forcefully");
        int result = this.backgroundProcess.destroyForcibly().waitFor();
        log.warn("GraphANNIS result code was {}", result);
      } else {
        log.info("Stopped graphANNIS process");
      }
    }
  }

  public Optional<Authentication> getDesktopUserToken() {
    return Optional.empty();
  }
}
