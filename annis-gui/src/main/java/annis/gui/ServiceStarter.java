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
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class ServiceStarter implements ApplicationListener<ApplicationReadyEvent>, DisposableBean {

    private final static Logger log = LoggerFactory.getLogger(ServiceStarter.class);

    @Autowired
    private UIConfig config;

    @Autowired
    private ResourceLoader resourceLoader;
    private final AtomicBoolean abortThread = new AtomicBoolean();

    private Process backgroundProcess;

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        if (config.getWebserviceURL() == null || config.getWebserviceURL().isEmpty()) {
            // start the bundled web service
            try {
                // Extract the bundled resource to a temporary file
                File tmpExec = File.createTempFile("graphannis-webservice", "");
                Resource resource = resourceLoader.getResource("classpath:graphannis-webservice");
                if (resource.exists()) {
                    log.info("Extracting the bundled graphANNIS service to {}",
                            tmpExec.getAbsolutePath());
                    FileUtils.copyInputStreamToFile(resource.getInputStream(), tmpExec);
                    tmpExec.setExecutable(true);

                    // If the configuration does not exist, create an empty file
                    File serviceConfigFile = new File(config.getWebserviceConfig());
                    if (!serviceConfigFile.exists()) {
                        serviceConfigFile.createNewFile();
                    }

                    // Start the process and read output/error stream in background threads
                    log.info("Starting the bundled graphANNIS service");
                    backgroundProcess = new ProcessBuilder(tmpExec.getAbsolutePath(), "--config",
                            serviceConfigFile.getAbsolutePath()).start();
                    final BufferedReader outputStream = new BufferedReader(new InputStreamReader(
                            backgroundProcess.getInputStream(), StandardCharsets.UTF_8));
                    final BufferedReader errorStream = new BufferedReader(new InputStreamReader(
                            backgroundProcess.getErrorStream(), StandardCharsets.UTF_8));

                    Thread tReaderOut = new Thread(() -> {
                        while (!this.abortThread.get()) {
                            String line;
                            try {
                                line = outputStream.readLine();
                                if (line != null) {
                                    log.info(line);
                                }
                            } catch (IOException ex) {
                                log.error("Could not read service output", ex);
                            }
                        }
                    });
                    tReaderOut.start();
                    Thread tReaderErr = new Thread(() -> {
                        while (!this.abortThread.get()) {
                            String line;
                            try {
                                line = errorStream.readLine();
                                if (line != null) {
                                    log.error(line);
                                }
                            } catch (IOException ex) {
                                log.error("Could not read service error output", ex);
                            }
                        }
                    });
                    tReaderErr.start();

                    config.setWebserviceURL("http://localhost:5711/v0");
                }
            } catch (final IOException ex) {
                log.error(
                        "Could not start integrated graphANNIS service, configure \"annis.webservice-url\" to set an existing service.",
                        ex);
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        this.abortThread.set(true);
        if (this.backgroundProcess != null) {
            this.backgroundProcess.destroy();
        }
    }
}
