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
package annis.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@Profile("!desktop")
public class ServiceStarter implements ApplicationListener<ApplicationReadyEvent>, DisposableBean {

    private final static Logger log = LoggerFactory.getLogger(ServiceStarter.class);

    @Autowired
    private UIConfig config;

    @Autowired
    private ResourceLoader resourceLoader;
    private final AtomicBoolean abortThread = new AtomicBoolean();

    private Process backgroundProcess;

    private Thread tReaderOut;

    private Thread tReaderErr;

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        if (config.getWebserviceUrl() == null || config.getWebserviceUrl().isEmpty()) {
            // start the bundled web service
            try {
                // Extract the bundled resource to a temporary file
                Optional<String> execPath = Optional.empty();
                if ("amd64".equals(SystemUtils.OS_ARCH)) {
                    if (SystemUtils.IS_OS_LINUX) {
                        execPath = Optional.of("linux-x86-64/graphannis-webservice");
                    } else if (SystemUtils.IS_OS_MAC_OSX) {
                        execPath = Optional.of("darwin/graphannis-webservice.osx");
                    } else if (SystemUtils.IS_OS_WINDOWS) {
                        execPath = Optional.of("win32-x86-64/graphannis-webservice.exe");
                    }
                } else {
                    log.error("GraphANNIS can only be run on 64 bit operating systems!");
                }

                if (execPath.isPresent()) {
                    File tmpExec = File.createTempFile("graphannis-webservice-", "");
                    Resource resource = resourceLoader.getResource("classpath:" + execPath.get());
                    if (resource.exists()) {
                        log.info("Extracting the bundled graphANNIS service to {}",
                                tmpExec.getAbsolutePath());
                        FileUtils.copyInputStreamToFile(resource.getInputStream(), tmpExec);
                        tmpExec.setExecutable(true);

                        // If the configuration does not exist, create an empty file
                        File serviceConfigFile = getServiceConfig();

                        // Start the process and read output/error stream in background threads
                        log.info(
                                "Starting the bundled graphANNIS service with configuration file {}",
                                serviceConfigFile.getAbsolutePath());
                        backgroundProcess = new ProcessBuilder(tmpExec.getAbsolutePath(),
                                "--config", serviceConfigFile.getAbsolutePath()).start();
                        final BufferedReader outputStream = new BufferedReader(
                                new InputStreamReader(backgroundProcess.getInputStream(),
                                        StandardCharsets.UTF_8));

                        final BufferedReader errorStream = new BufferedReader(new InputStreamReader(
                                backgroundProcess.getErrorStream(), StandardCharsets.UTF_8));

                        tReaderOut = new Thread(() -> {
                            while (!this.abortThread.get() && backgroundProcess.isAlive()) {
                                String line;
                                try {
                                    line = outputStream.readLine();
                                    if (line != null) {
                                        log.info(line);
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
                        tReaderOut.start();
                        tReaderErr = new Thread(() -> {
                            while (!this.abortThread.get() && backgroundProcess.isAlive()) {
                                String line;
                                try {
                                    line = errorStream.readLine();
                                    if (line != null) {
                                        log.error(line);
                                    }
                                } catch (IOException ex) {
                                    if (!this.abortThread.get()) {
                                        log.error("Could not read service error output", ex);
                                    }
                                    break;
                                }
                                Thread.yield();
                            }
                        });
                        tReaderErr.start();

                        // Use the provided service configuration to get the correct port
                        Toml parsedServiceConfig = new Toml().read(serviceConfigFile);
                        config.setWebserviceUrl(getServiceURL(parsedServiceConfig));
                    }
                }
            } catch (final IOException ex) {
                log.error(
                        "Could not start integrated graphANNIS service, configure \"annis.webservice-url\" to set an existing service.",
                        ex);
            }
        }
    }

    private long getServicePort(Toml config) {
        long port = 5711l;
        Toml bindTable = config.getTable("bind");
        if (bindTable != null) {
            port = bindTable.getLong("port", 5711l);
        }
        return port;
    }

    protected String getServiceURL(Toml config) {
        return "http://localhost:" + getServicePort(config) + "/v0";
    }

    protected File getServiceConfig() throws IOException {
        File result = new File(config.getWebserviceConfig());
        if (!result.exists()) {
            result.createNewFile();
        }
        // Set to a default data folder and SQLite file
        Toml configToml = new Toml().read(result);
        Map<String, Object> config = configToml.toMap();
        Toml databaseTable = configToml.getTable("database");
        Map<String, Object> databaseConfig;
        if (databaseTable == null) {
            // Create a new map instead of re-using the existing one
            databaseConfig = new LinkedHashMap<>();
            config.put("database", databaseConfig);
        } else {
            databaseConfig = databaseTable.toMap();
        }
        // Add the graphannis data and sqlite location of not existing yet
        Object previousDatabase = databaseConfig.putIfAbsent("graphannis", Paths
                .get(System.getProperty("user.home"), ".annis", "v4").toAbsolutePath().toString());
        Object previousSqlite = databaseConfig.putIfAbsent("sqlite",
                Paths.get(System.getProperty("user.home"), ".annis", "v4", "service_data.sqlite3")
                        .toAbsolutePath().toString());

        if (previousDatabase == null || previousSqlite == null) {
            // Write updated configuration to file
            TomlWriter writer = new TomlWriter();
            writer.write(config, result);
        }

        return result;
    }

    @Override
    public void destroy() throws Exception {
        this.abortThread.set(true);

        if (this.tReaderOut != null) {
            this.tReaderOut.interrupt();
        }

        if (this.tReaderErr != null) {
            this.tReaderErr.interrupt();
        }

        if (this.backgroundProcess != null) {
            this.backgroundProcess.destroy();
            if (!this.backgroundProcess.waitFor(5, TimeUnit.SECONDS)) {
                // Destroy the process by force after 5 seconds
                log.warn(
                        "GraphANNIS process did not stop after 5 seconds, stopping it forcefully");
                this.backgroundProcess.destroyForcibly();
            } else {
                log.info("Stopped graphANNIS process");
            }
        }
    }

    public Optional<UsernamePasswordAuthenticationToken> getDesktopUserToken() {
        return Optional.empty();
    }
}
