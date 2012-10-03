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
import annis.utils.Utils;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.spi.spring.container.SpringComponentProviderFactory;
import java.io.File;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.support.ResourcePropertySource;

public class AnnisServiceRunner extends AnnisBaseRunner
{

  private static final Logger log = LoggerFactory.getLogger(AnnisServiceRunner.class);
  private static AnnisServiceRunner annisServiceRunner;
  private static boolean isShutdownRequested = false;
  private static Thread mainThread;
  private HttpServer server;

  public static void main(String[] args) throws IOException
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

      annisServiceRunner.start();

      if(!isShutdownRequested)
      {
        closeSystemStreams();
      }

    }
    // no, run in debug mode
    else
    {
      log.info("Running in Debug mode");

      annisServiceRunner.start();
    }

    if(!isShutdownRequested)
    {
      addShutdownHook();
    }
    
    try
    {
      while (!isShutdownRequested)
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

  public void createWebServer()
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


    ResourceConfig rc = new PackagesResourceConfig("annis.service.internal", "annis.provider", "annis.rest.provider");
    IoCComponentProviderFactory factory = new SpringComponentProviderFactory(rc,
      ctx);

    int port = ctx.getBean(AnnisWebService.class).getPort();
    URI baseURI = UriBuilder.fromUri("http://localhost").port(port).build();
    try
    {
      server = GrizzlyServerFactory.createHttpServer(baseURI, rc, factory);
    }
    catch (IOException ex)
    {
      log.error("IOException at ANNIS service startup", ex);
      isShutdownRequested = true;
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

  private void start()
  {
    log.info("Starting up...");

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
    }


  }
}
