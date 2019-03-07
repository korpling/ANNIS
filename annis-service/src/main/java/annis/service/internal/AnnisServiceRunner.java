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
import java.net.InetSocketAddress;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.DispatcherType;

import org.aeonbits.owner.ConfigFactory;
import org.apache.shiro.web.env.EnvironmentLoader;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.corpus_tools.graphannis.errors.GraphANNISException;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annis.AnnisBaseRunner;
import annis.AnnisRunnerException;
import annis.ServiceConfig;
import annis.administration.AdministrationDao;
import annis.administration.CorpusAdministration;
import annis.administration.DeleteCorpusDao;
import annis.dao.QueryDao;
import annis.dao.ShortenerDao;
import annis.exceptions.AnnisException;
import annis.security.MultipleIniWebEnvironment;
import annis.service.objects.AnnisCorpus;

public class AnnisServiceRunner extends ResourceConfig {

    private static final Logger log = LoggerFactory.getLogger(AnnisServiceRunner.class);

    private final ServiceConfig cfg = ConfigFactory.create(ServiceConfig.class);

    private static AnnisServiceRunner annisServiceRunner;

    private boolean isShutdownRequested = false;
    private int errorCode = 0;

    private static Thread mainThread;

    private Server server;

    private boolean useAuthentification = true;
    private Integer overridePort = null;

    private final QueryDao queryDao;
    private final AdministrationDao adminDao;
    private final DeleteCorpusDao deleteCorpusDao;
    private final ShortenerDao shortenerDao;
    private final CorpusAdministration corpusAdministration;
    private final ImportWorker importWorker;
    

    public AnnisServiceRunner() throws GraphANNISException {
        this(null, null);
    }

    public AnnisServiceRunner(Integer port, CorpusAdministration corpusAdmin) throws GraphANNISException {
        this.overridePort = port;
        boolean nosecurity = Boolean.parseBoolean(System.getProperty("annis.nosecurity", "false"));
        this.useAuthentification = !nosecurity;

        if(corpusAdmin == null) {
            this.queryDao = QueryDao.create();
            this.deleteCorpusDao = DeleteCorpusDao.create(this.queryDao);
            this.adminDao = AdministrationDao.create(queryDao, deleteCorpusDao);
            this.shortenerDao = ShortenerDao.create();
            this.corpusAdministration = CorpusAdministration.create(this.adminDao, this.shortenerDao);
        } else {
            this.queryDao = corpusAdmin.getAdministrationDao().getQueryDao();
            this.deleteCorpusDao = corpusAdmin.getAdministrationDao().getDeleteCorpusDao();
            this.adminDao = corpusAdmin.getAdministrationDao();
            this.shortenerDao = corpusAdmin.getShortenerDao();
            this.corpusAdministration = corpusAdmin;
        }
        this.importWorker = new ImportWorker(this.corpusAdministration);
        this.importWorker.start();
        
        property("queryDao", this.queryDao);
        property("adminDao", this.adminDao);
        property("shortenerDao", this.shortenerDao);
        property("corpusAdministration", this.corpusAdministration);
        property("importWorker", this.importWorker);
        
        

        packages("annis.service.internal", "annis.provider", "annis.rest.provider");

    }
    
    public QueryDao getQueryDao() {
        return queryDao;
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

        int port = overridePort == null ? cfg.webservicePort() : overridePort;
        try {
            // only allow connections from localhost
            // if the administrator wants to allow external acccess he *has* to
            // use a HTTP proxy which also should use SSL encryption
            // URI addr = URI.create("http://localhost:" + port);
            // server = JettyHttpContainerFactory.createServer(addr, this);

            InetSocketAddress addr = new InetSocketAddress("localhost", port);
            server = new Server(addr);

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
            context.setContextPath("/");

            server.setHandler(context);

            ServletContainer jerseyContainer = new ServletContainer(this);

            ServletHolder holder = new ServletHolder(jerseyContainer);
            context.addServlet(holder, "/*");
            context.setInitParameter(EnvironmentLoader.ENVIRONMENT_CLASS_PARAM,
                    MultipleIniWebEnvironment.class.getName());

            if (useAuthentification) {
                log.info("Using authentification");
                context.setInitParameter(EnvironmentLoader.CONFIG_LOCATIONS_PARAM,
                        "file:" + System.getProperty("annis.home") + "/conf/shiro.ini," + "file:"
                                + System.getProperty("annis.home") + "/conf/develop_shiro.ini");
            } else {
                log.warn("*NOT* using authentification, your ANNIS service *IS NOT SECURED*");
                context.setInitParameter(EnvironmentLoader.CONFIG_LOCATIONS_PARAM,
                        "file:" + System.getProperty("annis.home") + "/conf/shiro_no_security.ini");
            }

            //EnumSet<DispatcherType> gzipDispatcher = EnumSet.of(DispatcherType.REQUEST);
            //context.addFilter(GzipFilter.class, "/*", gzipDispatcher);

            // configure Apache Shiro with the web application
            context.addEventListener(new EnvironmentLoaderListener());
            EnumSet<DispatcherType> shiroDispatchers = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD,
                    DispatcherType.INCLUDE, DispatcherType.ERROR);
            context.addFilter(ShiroFilter.class, "/*", shiroDispatchers);

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
