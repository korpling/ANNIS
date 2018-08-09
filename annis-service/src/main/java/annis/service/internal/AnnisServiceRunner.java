/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.service.internal;

import java.io.File;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.aeonbits.owner.ConfigFactory;
import org.apache.shiro.web.env.EnvironmentLoader;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annis.AnnisBaseRunner;
import annis.AnnisRunnerException;
import annis.ServiceConfig;
import annis.dao.QueryDao;
import annis.exceptions.AnnisException;
import annis.security.MultipleIniWebEnvironment;
import annis.service.objects.AnnisCorpus;

public class AnnisServiceRunner extends AnnisBaseRunner {

    private static final Logger log = LoggerFactory.getLogger(AnnisServiceRunner.class);

    private final ServiceConfig cfg = ConfigFactory.create(ServiceConfig.class);

    private static AnnisServiceRunner annisServiceRunner;

    private boolean isShutdownRequested = false;
    private int errorCode = 0;

    private static Thread mainThread;

    private HttpServer server;

    private boolean useAuthentification = true;
    private Integer overridePort = null;

    private QueryDao queryDao;

    public AnnisServiceRunner() {
        this(null);
    }

    public AnnisServiceRunner(Integer port) {
        this.overridePort = port;
        boolean nosecurity = Boolean.parseBoolean(System.getProperty("annis.nosecurity", "false"));
        this.useAuthentification = !nosecurity;
    }

    public static void main(String[] args) throws Exception {

        boolean daemonMode = false;
        if (args.length == 1 && ("-d".equals(args[0]))) {
            daemonMode = true;
        }

        AnnisBaseRunner.setupLogging(!daemonMode);

        mainThread = Thread.currentThread();

        annisServiceRunner = new AnnisServiceRunner();

        // run as a deamon?
        if (daemonMode) {
            log.info("Running in Daemon mode");

            File pidFile = new File(System.getProperty("annisservice.pid_file"));
            pidFile.deleteOnExit();

            annisServiceRunner.start(false);

            if (!annisServiceRunner.isShutdownRequested) {
                closeSystemStreams();
            }

        }
        // no, run in debug mode
        else {
            log.info("Running in Debug mode");

            annisServiceRunner.start(false);
        }

        if (!annisServiceRunner.isShutdownRequested) {
            addShutdownHook();
        }

        try {
            while (!annisServiceRunner.isShutdownRequested) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException ex) {
            log.error("interrupted in endless loop", ex);
        }

        // explicitly exit so we can decide if there was an error or not and everything
        // is closed.
        if (annisServiceRunner.errorCode != 0) {
            System.exit(annisServiceRunner.errorCode);
        }
    }

    /**
     * shutdown the AnnisService - ensure that current work load finishes
     */
    public void shutdown() {
        log.info("Shutting down...");
        isShutdownRequested = true;

        try {
            mainThread.join();
        } catch (InterruptedException e) {
            log.error("Interrupted which waiting on main daemon thread to complete.");
        }
    }

    private static void closeSystemStreams() {
        System.err.close();
        System.out.close();
    }

    static private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                annisServiceRunner.shutdown();
            }
        });
    }

    private void createWebServer() {

        ResourceConfig rc = new ResourceConfig().packages("annis.service.internal", "annis.provider",
                "annis.rest.provider");
        

        int port = overridePort == null ? cfg.webservicePort() : overridePort;
        try {
            // only allow connections from localhost
            // if the administrator wants to allow external acccess he *has* to
            // use a HTTP proxy which also should use SSL encryption
            URI addr = URI.create("http://localhost:" + port);

            // TODO: check useAuthentification flag

            rc.property(EnvironmentLoader.ENVIRONMENT_CLASS_PARAM,
                    MultipleIniWebEnvironment.class.getName());

            if (useAuthentification) {
                log.info("Using authentification");
                rc.property(EnvironmentLoader.CONFIG_LOCATIONS_PARAM,
                        "file:" + System.getProperty("annis.home") + "/conf/shiro.ini," + "file:"
                                + System.getProperty("annis.home") + "/conf/develop_shiro.ini");
            } else {
                log.warn("*NOT* using authentification, your ANNIS service *IS NOT SECURED*");
                rc.property(EnvironmentLoader.CONFIG_LOCATIONS_PARAM,
                        "file:" + System.getProperty("annis.home") + "/conf/shiro_no_security.ini");
            }
            
            
            server = GrizzlyHttpServerFactory.createHttpServer(addr, rc);

        } catch (IllegalArgumentException ex) {
            log.error("IllegalArgumentException at ANNIS service startup", ex);
            isShutdownRequested = true;
            errorCode = 101;
        } catch (NullPointerException ex) {
            log.error("NullPointerException at ANNIS service startup", ex);
            isShutdownRequested = true;
            errorCode = 101;
        } catch (AnnisRunnerException ex) {
            errorCode = ex.getExitCode();
        }

    }

    /**
     * Creates and starts the server
     *
     * @param rethrowExceptions
     *                              Set to true if you want to get exceptions
     *                              re-thrown to parent
     */
    public void start(boolean rethrowExceptions) throws Exception {
        log.info("Starting up REST...");

        try {
            createWebServer();
            if (server == null) {
                isShutdownRequested = true;
                errorCode = 100;
            } else {
                server.start();
            }
        } catch (Exception ex) {
            log.error("could not start ANNIS REST service", ex);
            isShutdownRequested = true;
            errorCode = 100;

            if (rethrowExceptions) {
                if (!(ex instanceof AnnisException) && ex.getCause() instanceof AnnisException) {
                    throw ((AnnisException) ex.getCause());
                } else {
                    throw (ex);
                }
            }
        }
    }

    /**
     * True if authorization is enabled.
     *
     * @return
     */
    public boolean isUseAuthentification() {
        return useAuthentification;
    }

    /**
     * Set wether you want to protect the service using authentification.
     *
     * Default value is true.
     *
     * @param useAuthentification
     *                                True if service should be authentificated,
     *                                false if not.
     */
    public void setUseAuthentification(boolean useAuthentification) {
        this.useAuthentification = useAuthentification;
    }

    /**
     * Set the timeout in milliseconds
     * 
     * @param milliseconds
     *                         Timeout if greater than zero, disabled timeout if
     *                         less then zero.
     */
    public void setTimeout(int milliseconds) {
        if (queryDao != null) {
            queryDao.setTimeout(milliseconds);
        }
    }

    public int getTimeout() {
        return cfg.timeout();
    }

    public List<AnnisCorpus> getCorpora() {
        if (queryDao != null) {
            return queryDao.listCorpora();
        }
        return new LinkedList<>();
    }
}
