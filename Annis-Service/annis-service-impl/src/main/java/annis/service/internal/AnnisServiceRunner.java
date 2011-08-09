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

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import annis.AnnisBaseRunner;

public class AnnisServiceRunner extends AnnisBaseRunner
{

  private static Logger log = Logger.getLogger(AnnisServiceRunner.class);
  private static Thread mainDaemonThread;
  private static AnnisServiceRunner annisService;

  public static void main(String[] args)
  {
    PropertyConfigurator.configure(System.getProperty("annis.home") + "/conf/logging.properties");

    annisService = new AnnisServiceRunner();

    // run as a deamon?
    if(args.length == 1 && ("-d".equals(args[0])))
    {
      annisService.startAndDetach();
    }
    // no, run in debug mode
    else
    {
      // create a logging appender for stdout
      Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%d{HH:mm:ss,SSS} [%t] %C{1} %p: %m\n")));
      annisService.start();
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
    catch(Throwable e)
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
    // starts RMI service at bean creation 
    new ClassPathXmlApplicationContext("annis/service/internal/AnnisServiceRunner-context.xml");
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
   * shutdown the AnnisService
   * - ensure that current work load finishes, FIXME: shutdownrequested!
   * - delete pid file
   */
  public void shutdown()
  {
    log.info("Shutting down...");

    // block, until current thread completes
    try
    {
      mainDaemonThread.join();
    }
    catch(InterruptedException e)
    {
      log.error("Interrupted while waiting on main daemon thread to complete.");
    }

    // delete pid file
    boolean success = new File(System.getProperty("annisservice.pid_file")).delete();
    if(!success)
    {
      log.error("Couldn't delete pid file.");
    }
  }
}
