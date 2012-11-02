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

import java.io.IOException;
import annis.AnnisBaseRunner;
import annis.exceptions.AnnisException;
import annis.utils.Utils;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.spring.container.SpringComponentProviderFactory;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.GzipFilter;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.support.ResourcePropertySource;

public class AnnisServiceRunner extends AnnisBaseRunner
{

  private static final Logger log = LoggerFactory.getLogger(AnnisServiceRunner.class);
  private static AnnisServiceRunner annisServiceRunner;
  private boolean isShutdownRequested = false;
  private static Thread mainThread;
  private Server server;
  
  private boolean useAuthentification = true;

  public AnnisServiceRunner()
  {
    this.useAuthentification = 
      Boolean.parseBoolean(System.getProperty("annis.secure", "true"));
  }
  
  public static void main(String[] args) throws Exception
  {
    
    boolean daemonMode = false;
    if(args.length == 1 && ("-d".equals(args[0])))
    {
      daemonMode = true;
    }
    
    AnnisBaseRunner.setupLogging(!daemonMode);

    
    mainThread = Thread.currentThread();

    annisServiceRunner = new AnnisServiceRunner();

    // run as a deamon?
    if (daemonMode)
    {
      log.info("Running in Daemon mode");

      File pidFile = new File(System.getProperty("annisservice.pid_file"));
      pidFile.deleteOnExit();

      annisServiceRunner.start(false);

      if(!annisServiceRunner.isShutdownRequested)
      {
        closeSystemStreams();
      }

    }
    // no, run in debug mode
    else
    {
      log.info("Running in Debug mode");

      annisServiceRunner.start(false);
    }

    if(!annisServiceRunner.isShutdownRequested)
    {
      addShutdownHook();
    }
    
    try
    {
      while (!annisServiceRunner.isShutdownRequested)
      {
        Thread.sleep(1000);
      }
    }
    catch (InterruptedException ex)
    {
      log.error("interrupted in endless loop", ex);
    }

  }

  /**
   * shutdown the AnnisService - ensure that current work load finishes
   */
  public void shutdown()
  {
    log.info("Shutting down...");
    isShutdownRequested = true;

    try
    {
      mainThread.join();
    }
    catch (InterruptedException e)
    {
      log.error(
        "Interrupted which waiting on main daemon thread to complete.");
    }
  }

  private static void closeSystemStreams()
  {
    System.err.close();
    System.out.close();
  }

  static private void addShutdownHook()
  {
    Runtime.getRuntime().addShutdownHook(new Thread()
    {

      @Override
      public void run()
      {
        annisServiceRunner.shutdown();
      }
    });
  }

  private void createWebServer()
  {

    // create beans
    GenericXmlApplicationContext ctx = new GenericXmlApplicationContext();
    MutablePropertySources sources = ctx.getEnvironment().getPropertySources();
    try
    {
      sources.addFirst(new ResourcePropertySource("file:" + Utils.getAnnisFile(
        "conf/annis-service.properties").getAbsolutePath()));
    }
    catch (IOException ex)
    {
      log.error("Could not load conf/annis-service.properties", ex);
    }
    ctx.load("file:" + Utils.getAnnisFile("conf/spring/Service.xml").getAbsolutePath());
    ctx.refresh();


    ResourceConfig rc = new PackagesResourceConfig("annis.service.internal", "annis.provider", "annis.rest.provider"    );
    
    final IoCComponentProviderFactory factory = new SpringComponentProviderFactory(rc,
      ctx);

    int port = ctx.getBean(QueryService.class).getPort();
    try
    {
      // only allow connections from localhost
      // if the administrator wants to allow external acccess he *has* to
      // use a HTTP proxy which also should use SSL encryption
      InetSocketAddress addr = new InetSocketAddress("localhost", port);
      server = new Server(addr);
            
      ServletContextHandler context = 
        new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
      context.setContextPath("/");
      server.setHandler(context);
      server.setThreadPool(new ExecutorThreadPool());
      
      ServletContainer jerseyContainer = new ServletContainer(rc)
      {
        @Override
        protected void initiate(ResourceConfig rc, WebApplication wa)
        {
          wa.initiate(rc, factory);
        }        
      };
      
      ServletHolder holder = new ServletHolder(jerseyContainer);
      context.addServlet(holder, "/*");

      
      if(useAuthentification)
      {
        context.setInitParameter("shiroConfigLocations",
        "file:" + System.getProperty("annis.home") + "/conf/shiro.ini");
      }
      else
      {
        context.setInitParameter("shiroConfigLocations",
        "file:" + System.getProperty("annis.home") + "/conf/shiro_no_security.ini");
      }
      
      EnumSet<DispatcherType> gzipDispatcher = EnumSet.of(DispatcherType.REQUEST);
      context.addFilter(GzipFilter.class, "/*", gzipDispatcher);
      // enable compression
      //context.setInitParameter("com.sun.jersey.spi.container.ContainerRequestFilters", 
      //  "com.sun.jersey.api.container.filter.GZIPContentEncodingFilter");
      //context.setInitParameter("com.sun.jersey.spi.container.ContainerResponseFilters", 
      ///  "com.sun.jersey.api.container.filter.GZIPContentEncodingFilter");
      
      // configure Apache Shiro with the web application
      context.addEventListener(new EnvironmentLoaderListener());
      EnumSet<DispatcherType> shiroDispatchers = EnumSet.of(
        DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE,
        DispatcherType.ERROR);
      context.addFilter(ShiroFilter.class, "/*", shiroDispatchers);
      
    }
    catch (IllegalArgumentException ex)
    {
      log.error("IllegalArgumentException at ANNIS service startup", ex);
      isShutdownRequested = true;;
    }
    catch (NullPointerException ex)
    {
      log.error("NullPointerException at ANNIS service startup", ex);
      isShutdownRequested = true;
    }

  }

  /**
   * Creates and starts the server
   * @param rethrowExceptions Set to true if you want to get exceptions re-thrown to parent 
   */
  public void start(boolean rethrowExceptions) throws Exception
  {
    log.info("Starting up REST...");

    try
    {
      createWebServer();
      if (server == null)
      {
        isShutdownRequested = true;
      }
      else
      {
        server.start();
      }
    }
    catch (Exception ex)
    {
      log.error("could not start ANNIS REST service", ex);
      isShutdownRequested = true;
      
      if(rethrowExceptions)
      {
        if(!(ex instanceof AnnisException) && ex.getCause() instanceof AnnisException)
        {
          throw((AnnisException) ex.getCause());
        }
        else
        {
          throw(ex);
        }
      }
    }
  }

  /**
   * True if authorization is enabled.
   * @return 
   */
  public boolean isUseAuthentification()
  {
    return useAuthentification;
  }

  /**
   * Set wether you want to protect the service using authentification.
   * 
   * Default value is true.
   * @param useAuthentification True if service should be authentificated, false if not.
   */
  public void setUseAuthentification(boolean useAuthentification)
  {
    this.useAuthentification = useAuthentification;
  }
  
  
}
