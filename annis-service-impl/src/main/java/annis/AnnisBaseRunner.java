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
package annis;

import annis.exceptions.AnnisQLSyntaxException;
import annis.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

import java.util.logging.Level;
import jline.ConsoleReader;
import jline.SimpleCompletor;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.support.ResourcePropertySource;

public abstract class AnnisBaseRunner
{

  private static Logger log = Logger.getLogger(AnnisBaseRunner.class);
  // the root of the Annis installation
  private static String annisHomePath;
  // console output for easier testing, normally set to System.out
  protected PrintStream out = System.out;
  ;
  // for the interactive shell
  private String helloMessage;
  private String prompt;

  public static AnnisBaseRunner getInstance(String beanName,
    String... contextLocations)
  {
    return getInstance(beanName, true, contextLocations);
  }

  public static AnnisBaseRunner getInstance(String beanName,
    boolean logToConsole, String... contextLocations)
  {
    checkForAnnisHome();
    setupLogging(logToConsole);

    GenericXmlApplicationContext ctx = new GenericXmlApplicationContext();
    MutablePropertySources sources = ctx.getEnvironment().getPropertySources();
    try
    {
      sources.addFirst(new ResourcePropertySource("file:" + Utils.getAnnisFile(
        "conf/database.properties").getAbsolutePath()));
    }
    catch (IOException ex)
    {
      log.error("Could not load conf/database.properties", ex);
    }
    try
    {
      sources.addFirst(new ResourcePropertySource("file:" + Utils.getAnnisFile(
        "conf/annis-service.properties").getAbsolutePath()));
    }
    catch (IOException ex)
    {
      log.error("Could not load conf/annis-service.properties", ex);
    }
    
    ctx.load(contextLocations);
    ctx.refresh();
    return (AnnisBaseRunner) ctx.getBean(beanName);
  }

  public void run(String[] args)
  {
    try
    {
      // run interactive if no argument is given
      if (args.length == 0)
      {
        runInteractive();
        return;

        // else, every argument is a command
      }
      else
      {
        for (String cmd : args)
        {
          // split into command name and arguments
          String[] split = cmd.split("\\s+", 2);

          System.out.println("running command '" + cmd + "'");
          runCommand(split[0], split.length >= 2 ? split[1] : "");
        }
      }
    }
    catch (AnnisRunnerException e)
    {
      log.error("Uncaught exception", e);
      error("Uncaught exception: " + e.getMessage());
    }
    catch (Throwable e)
    {
      log.warn("Uncaught exception", e);
      error(e);
    }
  }

  protected void runInteractive() throws IOException
  {
    System.out.println(helloMessage);
    System.out.println();
    System.out.println("Use \"help\" for a list of all commands.");
    System.out.println();

    ConsoleReader console = new ConsoleReader();
    console.setUseHistory(true);
    console.setBellEnabled(true);

    String[] commands = detectAvailableCommands().toArray(new String[0]);
    console.addCompletor(new SimpleCompletor(commands));

    String line;
    prompt = "no corpus>";
    while ((line = console.readLine(prompt + " ")) != null)
    {
      try
      {

        String command = line.split(" ")[0];

        if ("help".equalsIgnoreCase(command))
        {
          System.out.println("Available commands:");
          System.out.println(StringUtils.join(commands, "\n"));
        }
        else
        {
          String args = StringUtils.join(Arrays.asList(line.split(" ")).subList(
            1, line.split(" ").length), " ");
          runCommand(command, args);
        }
      }
      catch (IndexOutOfBoundsException e)
      {
        continue;
      }
      catch (UsageException e)
      {
        error(e);
      }
    }
  }

  protected void error(Throwable e)
  {
    error(e.getMessage());
  }

  protected void error(String error)
  {
    System.out.println();
    System.out.println("ERROR: " + error);
  }

  protected List<String> detectAvailableCommands()
  {
    LinkedList<String> result = new LinkedList<String>();

    Method[] methods = getClass().getMethods();

    for (Method m : methods)
    {
      if (m.getName().startsWith("do"))
      {
        String commandName = m.getName().substring("do".length());
        if (commandName.length() > 1)
        {
          commandName = commandName.substring(0, 1).toLowerCase() + commandName.
            substring(1);
          result.add(commandName);
        }
      }
    }
    return result;
  }

  protected void runCommand(String command, String args)
  {
    String methodName = "do" + command.substring(0, 1).toUpperCase() + command.
      substring(1);
    log.debug("looking for: " + methodName);

    try
    {
      long start = new Date().getTime();
      Method commandMethod = getClass().getMethod(methodName, String.class);
      commandMethod.invoke(this, args);
      System.out.println("Time: " + (new Date().getTime() - start) + " ms");
    }
    catch (InvocationTargetException e)
    {
      // FIXME: Exception-Handling is all over the place (refactor into a handleException method)
      Throwable cause = e.getCause();
      try
      {
        throw cause;
      }
      catch (AnnisQLSyntaxException ee)
      {
        error(ee.getMessage());
      }
      catch (Throwable ee)
      {
        log.error("Uncaught exception: ", ee);
        error("Uncaught Exception: " + ee.getMessage());
      }
    }
    catch (IllegalAccessException e)
    {
      log.error("BUG: IllegalAccessException should never be thrown", e);
      throw new AnnisRunnerException("BUG: can't access method: " + methodName,
        e);
    }
    catch (SecurityException e)
    {
      log.error("BUG: SecurityException should never be thrown", e);
      error(e);
    }
    catch (NoSuchMethodException e)
    {
      throw new UsageException("don't know how to do: " + command);
    }
  }

  private static void checkForAnnisHome()
  {
    // check if annis.home is set and correct
    annisHomePath = System.getProperty("annis.home");
    if (annisHomePath == null)
    {
      System.out.println(
        "Please set the annis.home property to the Annis distribution directory.");
      System.exit(1);
    }
    File file = new File(annisHomePath);
    if (!file.exists() || !file.isDirectory())
    {
      System.out.println("The directory '" + annisHomePath
        + "' does not exist or is not a directory.");
      System.exit(2);
    }
  }

  // configure logging
  private static void setupLogging(boolean console)
  {
    PropertyConfigurator.configure(annisHomePath + "/conf/logging.properties");

    if (console)
    {
      Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout(
        "%d{HH:mm:ss.SSS} %C{1} %p: %m\n")));
    }
  }

  ///// Getter / Setter
  public static String getAnnisHome()
  {
    return annisHomePath;
  }

  public String getHelloMessage()
  {
    return helloMessage;
  }

  public void setHelloMessage(String helloMessage)
  {
    this.helloMessage = helloMessage;
  }

  public String getPrompt()
  {
    return prompt;
  }

  public void setPrompt(String prompt)
  {
    this.prompt = prompt;
  }

  public PrintStream getOut()
  {
    return out;
  }

  public void setOut(PrintStream out)
  {
    this.out = out;
  }
}
