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

import java.io.IOException;
import java.util.logging.Level;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import annis.AnnisBaseRunner;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.spi.spring.container.SpringComponentProviderFactory;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.springframework.context.ConfigurableApplicationContext;

public class AnnisServiceRunner extends AnnisBaseRunner
{
  
  private static Logger log = Logger.getLogger(AnnisServiceRunner.class);
  private static Thread mainDaemonThread;
  private static AnnisServiceRunner annisService;
  private HttpServer server;
  
  protected void createWebServer()
  {

    // create beans
    ClassPathXmlApplicationContext cxt = new ClassPathXmlApplicationContext(
      "annis/service/internal/AnnisServiceRunner-context.xml");
    ResourceConfig rc = new DefaultResourceConfig();
    IoCComponentProviderFactory factory = new SpringComponentProviderFactory(rc,
      cxt);
    
    int port = cxt.getBean(AnnisWebService.class).getPort();
    URI baseURI = UriBuilder.fromUri("http://localhost").port(port).build();
    try
    {
      server = GrizzlyServerFactory.createHttpServer(baseURI, rc, factory);
    }
    catch (IOException ex)
    {
      java.util.logging.Logger.getLogger(AnnisServiceRunner.class.getName()).
        log(Level.SEVERE, null, ex);
    }
    catch (IllegalArgumentException ex)
    {
      java.util.logging.Logger.getLogger(AnnisServiceRunner.class.getName()).
        log(Level.SEVERE, null, ex);
    }
    catch (NullPointerException ex)
    {
      java.util.logging.Logger.getLogger(AnnisServiceRunner.class.getName()).
        log(Level.SEVERE, null, ex);
    }
    
  }
  
  public static void main(String[] args)
  {
    PropertyConfigurator.configure(System.getProperty("annis.home")
      + "/conf/logging.properties");
    
    annisService = new AnnisServiceRunner();

    // run as a deamon?
    if (args.length == 1 && ("-d".equals(args[0])))
    {
      annisService.startAndDetach();
    }
    // no, run in debug mode
    else
    {
      // create a logging appender for stdout
      Logger.getRootLogger().addAppender(
        new ConsoleAppender(new PatternLayout(
        "%d{HH:mm:ss,SSS} [%t] %C{1} %p: %m\n")));
      annisService.start();
    }
    
    try
    {
      // endless loop
      while (true)
      {
        System.in.read();
      }
    }
    catch (IOException ex)
    {
      Logger.getLogger(AnnisServiceRunner.class.getName()).error(
        "could not read form System.in", ex);
    }
  }

  /**
   * start the AnnisService and detach from the shell
   */
  public void startAndDetach()
  {
    try
    {
      start();
      daemonize();
      
    }
    catch (Throwable e)
    {
      log.fatal("Startup failed.", e);
      System.err.println("Startup failed.");
      e.printStackTrace();
      System.exit(1);
    }
  }
  
  private void start()
  {
    log.info("Starting up...");
    createWebServer();
    if (server != null)
    {
      try
      {
        server.start();
      }
      catch (IOException ex)
      {
        java.util.logging.Logger.getLogger(AnnisServiceRunner.class.getName()).
          log(Level.SEVERE, null, ex);
      }
    }
    
  }
  
  private void daemonize()
  {
    // save the current thread
    mainDaemonThread = Thread.currentThread();

    // add a shutdown hook to achieve an orderly shutdown
    Runtime.getRuntime().addShutdownHook(new Thread()
    {
      
      public void run()
      {
        annisService.shutdown();
      }
    });

    // close stdout and stderr to detach process from shell
    closeSystemStreams();
    
    log.info("Running as a daemon.");
  }
  
  private void closeSystemStreams()
  {
    System.err.close();
    System.out.close();
  }

  /**
   * shutdown the AnnisService - ensure that current work load finishes,
   * FIXME: shutdownrequested! - delete pid file
   */
  public void shutdown()
  {
    log.info("Shutting down...");

    // block, until current thread completes
    try
    {
      mainDaemonThread.join();
    }
    catch (InterruptedException e)
    {
      log.error("Interrupted while waiting on main daemon thread to complete.");
    }
    
    if (server != null)
    {
      server.stop();
    }

    // delete pid file
    boolean success = new File(System.getProperty("annisservice.pid_file")).
      delete();
    if (!success)
    {
      log.error("Couldn't delete pid file.");
    }
  }
}
