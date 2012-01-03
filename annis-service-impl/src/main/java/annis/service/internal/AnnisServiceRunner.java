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
import java.util.logging.Level;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger; 
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import annis.AnnisBaseRunner;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.spi.spring.container.SpringComponentProviderFactory;
import java.io.File;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;

public class AnnisServiceRunner extends AnnisBaseRunner
{

  private static Logger log = Logger.getLogger(AnnisServiceRunner.class);
  private static AnnisServiceRunner annisServiceRunner;
  private static boolean isShutdownRequested = false;
  private static Thread mainThread;
  private HttpServer server;

  public static void main(String[] args) throws IOException
  {
    mainThread = Thread.currentThread();
    
    PropertyConfigurator.configure(System.getProperty("annis.home")
      + "/conf/logging.properties");

    annisServiceRunner = new AnnisServiceRunner();

    // run as a deamon?
    if (args.length == 1 && ("-d".equals(args[0])))
    {
      log.info("Running in Daemon mode");

      File pidFile = new File(System.getProperty("annisservice.pid_file"));
      pidFile.deleteOnExit();

      annisServiceRunner.start();
      closeSystemStreams();    
    }
    // no, run in debug mode
    else
    {
      log.info("Running in Debug mode");
      // create a logging appender for stdout
      Logger.getRootLogger().addAppender(
        new ConsoleAppender(new PatternLayout(
        "%d{HH:mm:ss,SSS} [%t] %C{1} %p: %m\n")));
      annisServiceRunner.start();
    }
    
    addShutdownHook();

    try
    {
      while (!isShutdownRequested)
      {
        Thread.sleep(1000);
      }
    }
    catch (InterruptedException ex)
    {
      java.util.logging.Logger.getLogger(AnnisServiceRunner.class.getName()).
        log(Level.SEVERE, "interrupted in endless loop", ex);
    }

  }

  /**
   * shutdown the AnnisService - ensure that current work load finishes
   */
  public static void shutdown()
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
        AnnisServiceRunner.shutdown();
      }
    });
  }

  protected void createWebServer()
  {

    // create beans
    ClassPathXmlApplicationContext cxt = new ClassPathXmlApplicationContext(
      "annis/service/internal/AnnisServiceRunner-context.xml");
    ResourceConfig rc = new PackagesResourceConfig("annis.service.internal");
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
}
