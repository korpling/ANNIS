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
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.FilterReply;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import jline.console.ConsoleReader;
import jline.console.completer.StringsCompleter;
import jline.console.history.FileHistory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.context.support.GenericXmlApplicationContext;

public abstract class AnnisBaseRunner
{

  private static final Logger log = LoggerFactory.getLogger(
    AnnisBaseRunner.class);
  // the root of the Annis installation

  private static String annisHomePath;
  // console output for easier testing, normally set to System.out

  protected PrintStream out = System.out;

  private FileHistory history;
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
    return (AnnisBaseRunner) getBean(beanName, logToConsole, contextLocations);
  }

  public static Object getBean(String beanName,
    boolean logToConsole, String... contextLocations)
  {
    checkForAnnisHome();
    setupLogging(logToConsole);

    GenericXmlApplicationContext ctx = new GenericXmlApplicationContext();
    ctx.setValidating(false);
   
    AnnisXmlContextHelper.prepareContext(ctx);

    ctx.load(contextLocations);
    ctx.refresh();
    return ctx.getBean(beanName);
  }

  public void run(String[] args) throws IOException
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

  protected void runInteractive() throws IOException
  {
    System.out.println(helloMessage + " " + VersionInfo.getReleaseName());
    System.out.println();
    System.out.println("Use \"help\" for a list of all commands.");
    System.out.println();

    ConsoleReader console = new ConsoleReader();
    File annisDir = new File(System.getProperty("user.home") + "/.annis/");
    String annisDirPath = annisDir.getAbsolutePath();
    if (!annisDir.exists())
    {
      log.info("Creating directory: " + annisDirPath);
      if (!annisDir.mkdirs())
      {
        log.warn("Could not create directory: " + annisDirPath);
      }
    }
    else if (!annisDir.isDirectory())
    {
      log.warn(
        "Could not create directory because a file with the same name already exists: " + annisDirPath);
    }

    history = new FileHistory(new File(
      System.getProperty("user.home") + "/.annis/shellhistory.txt"));
    console.setHistory(history);
    console.setHistoryEnabled(true);
    console.setBellEnabled(true);
    console.setExpandEvents(false);

    List<String> commands = detectAvailableCommands();
    Collections.sort(commands);
    console.addCompleter(new StringsCompleter(commands));

    Splitter argSplitter = Splitter.on(' ').limit(2);
    String line;
    StringBuilder input = new StringBuilder();
    prompt = "no corpus>";
    console.setPrompt(prompt + " ");
    while ((line = console.readLine()) != null)
    {
      if(line.endsWith("\\"))
      {
        // multi-line input
        input.append(line.substring(0, line.length()-1)).append("\n");
        // notifiy user by changing the prompt
        console.setPrompt("> ");
      }
      else
      {
        // input finished, run command
        input.append(line);
        
        ArrayList<String> splitted = Lists.newArrayList(argSplitter.split(input.toString()));
        String command = splitted.get(0);
        String args = "";

        if ("help".equalsIgnoreCase(command))
        {
          System.out.println("Available commands:");
          System.out.println(StringUtils.join(commands, "\n"));
        }
        else
        {
          if (splitted.size() > 1)
          {
            args = splitted.get(1);
          }
        }
        try
        {
          if(!command.isEmpty())
          {
            runCommand(command, args);
          }
        }
        catch (UsageException e)
        {
          error(e);
        }
        // reset the current prompt
        console.setPrompt(prompt + " ");
        // empty input
        input = new StringBuilder();
      }
    } // end while
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
    LinkedList<String> result = new LinkedList<>();

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

      if (history != null)
      {
        history.flush();
      }
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
    catch (IOException e)
    {
      log.error("IOException was thrown", e);
    }
  }

  private static void checkForAnnisHome()
  {
    // check if annis.home is set and correct
    annisHomePath = System.getProperty("annis.home");
    if (annisHomePath == null)
    {
      throw new AnnisRunnerException(
        "Please set the annis.home property to the ANNIS distribution directory.", 2);
    }
    File file = new File(annisHomePath);
    if (!file.exists() || !file.isDirectory())
    {
      throw new AnnisRunnerException("The directory '" + annisHomePath
        + "' does not exist or is not a directory.", 3);
    }
  }

  // configure logging
  public static void setupLogging(boolean console)
  {
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.
      getILoggerFactory();

    JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(loggerContext);
    loggerContext.reset();
    try
    {
      jc.doConfigure(System.getProperty("annis.home")
        + "/conf/logback.xml");
    }
    catch (JoranException ex)
    {
      System.out.println(ex.getMessage());
    }

    ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
    consoleAppender.setContext(loggerContext);
    consoleAppender.setName("CONSOLE");

    consoleAppender.setLayout(new ConsoleLayout());

    ThresholdFilter consoleFilter = new ConsoleFilter();

    consoleFilter.setLevel(console ? "DEBUG" : "WARN");
    consoleFilter.start();

    consoleAppender.addFilter(consoleFilter);
    consoleAppender.setTarget("System.err");
    consoleAppender.start();

    ch.qos.logback.classic.Logger logbackLogger = loggerContext.getLogger(
      Logger.ROOT_LOGGER_NAME);

    logbackLogger.addAppender(consoleAppender);

    SLF4JBridgeHandler.removeHandlersForRootLogger();;
    SLF4JBridgeHandler.install();
  }

  public static class ConsoleLayout extends LayoutBase<ILoggingEvent>
  {

    @Override
    public String doLayout(ILoggingEvent e)
    {
      IThrowableProxy tp = e.getThrowableProxy();

      StringBuilder sb = new StringBuilder();
      sb.append("[").append(e.getLevel()).append("]\t");
      if(e.getFormattedMessage() != null)
      {
        sb.append(e.getFormattedMessage());
        sb.append(" ");
      }
      if(tp != null)
      {
        sb.append(tp.getClassName())
          .append(": ").append(tp.getMessage());
        sb.append(" ");
      }
      sb.append("- ");

      long t = e.getTimeStamp() - e.getLoggerContextVO().getBirthTime();

      long s = t / 1000;
      long m = s / 60;
      long h = m / 60;

      sb.append(h).append("h").append(m % 60).append("m").append(s % 60).
        append("s");
      sb.append(CoreConstants.LINE_SEPARATOR);
      return sb.toString();
    }

  }

  /**
   * Filters the jdbc message at the beginning of all annis console commands.
   */
  public static class ConsoleFilter extends ThresholdFilter
  {

    @Override
    public FilterReply decide(ILoggingEvent event)
    {

      if (event.getLoggerName() != null && event.getLoggerName().equals(
        org.apache.commons.dbcp2.BasicDataSource.class.getCanonicalName()))
      {
        return FilterReply.DENY;
      }

      return super.decide(event);
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
