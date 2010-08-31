package annis;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import annis.exceptions.ParseException;
import java.util.LinkedList;
import java.util.List;
import jline.ArgumentCompletor;
import jline.ConsoleReader;
import jline.SimpleCompletor;


public abstract class AnnisBaseRunner {
	
	private Logger log = Logger.getLogger(this.getClass());

	// the root of the Annis installation
	private static String annisHomePath;

	// console output for easier testing, normally set to System.out
	protected PrintStream out = System.out;;
	
	// for the interactive shell
	private String helloMessage;
	private String prompt;
	
	public static AnnisBaseRunner getInstance(String beanName, String... contextLocations) {
		return getInstance(beanName, true, contextLocations);
	}
	
	public static AnnisBaseRunner getInstance(String beanName, boolean logToConsole, String... contextLocations) {
		checkForAnnisHome();
		setupLogging(logToConsole);
		
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(contextLocations);
		return (AnnisBaseRunner) ctx.getBean(beanName);
	}
	
	public void run(String[] args) {
		try {
			// run interactive if no argument is given
			if (args.length == 0) {
				runInteractive();
				return;
			
			// else, 1st argument is command, the rest are arguments to the command
			} else {
				String command = args[0];
				String argument = StringUtils.join(Arrays.asList(args).subList(1, args.length), " ");
				runCommand(command, argument);
			}
		} catch (AnnisRunnerException e) {
			log.error("Uncaught exception", e);
			error("Uncaught exception: " + e.getMessage());
		} catch (Throwable e) {
			log.warn("Uncaught exception", e);
			error(e);
		}
	}
	
	protected void runInteractive() throws IOException {
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
    while((line = console.readLine(prompt + " ")) != null)
    {
			try {
				
				String command = line.split(" ")[0];

        if("help".equalsIgnoreCase(command))
        {
          System.out.println("Available commands:");
          System.out.println(StringUtils.join(commands, "\n"));
        }
        else
        {
          String args = StringUtils.join(Arrays.asList(line.split(" ")).subList(1, line.split(" ").length), " ");
          runCommand(command, args);
        }
			} catch (IndexOutOfBoundsException e) {
				continue;
			} catch (UsageException e) {
				error(e);
			}
		}
	}

	protected void error(Throwable e) {
		error(e.getMessage());
	}

	protected void error(String error) {
		System.out.println();
		System.out.println("ERROR: " + error);
	}

  protected List<String> detectAvailableCommands()
  {
    LinkedList<String> result = new LinkedList<String>();
    Method[] methods = getClass().getMethods();
    
    for(Method m : methods)
    {
      if(m.getName().startsWith("do"))
      {
        String commandName = m.getName().substring("do".length());
        if(commandName.length() > 1)
        {
          commandName = commandName.substring(0,1).toLowerCase() + commandName.substring(1);
          result.add(commandName);   
        }
      }
    }
    
    return result;
  }

	protected void runCommand(String command, String args) {
		String methodName = "do" + command.substring(0, 1).toUpperCase() + command.substring(1);
		log.debug("looking for: " + methodName);
		
		try {
			long start = new Date().getTime();
			Method commandMethod = getClass().getMethod(methodName, String.class);
			commandMethod.invoke(this, args);
			System.out.println("Time: " + (new Date().getTime() - start) + " ms");
		} catch (InvocationTargetException e) {
			// FIXME: Exception-Handling is all over the place (refactor into a handleException method)
			Throwable cause = e.getCause();
			try {
				throw cause;
			} catch (ParseException ee) {
				error(ee.getMessage());
			} catch (Throwable ee) {
				log.error("Uncaught exception: ", ee);
				error("Uncaught Exception: " + ee.getMessage());
			}
		} catch (IllegalAccessException e) {
			log.error("BUG: IllegalAccessException should never be thrown", e);
			throw new AnnisRunnerException("BUG: can't access method: " + methodName, e);
		} catch (SecurityException e) {
			log.error("BUG: SecurityException should never be thrown", e);
			error(e);
		} catch (NoSuchMethodException e) {
			throw new UsageException("don't know how to do: " + command);
		}
	}

	private static void checkForAnnisHome() {
		// check if annis.home is set and correct
		annisHomePath = System.getProperty("annis.home");
		if (annisHomePath == null) {
			System.out.println("Please set the annis.home property to the Annis distribution directory.");
			System.exit(1);
		}
		File file = new File(annisHomePath);
		if (! file.exists() || ! file.isDirectory()) {
			System.out.println("The directory '" + annisHomePath + "' does not exist or is not a directory.");
			System.exit(2);
		}
	}

	// configure logging
	private static void setupLogging(boolean console) {
		PropertyConfigurator.configure(annisHomePath + "/conf/logging.properties");

		if (console)
			Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%d{HH:mm:ss.SSS} %C{1} %p: %m\n")));
	}
	
	///// Getter / Setter
	
	public static String getAnnisHome() {
		return annisHomePath;
	}

	public String getHelloMessage() {
		return helloMessage;
	}

	public void setHelloMessage(String helloMessage) {
		this.helloMessage = helloMessage;
	}

	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public PrintStream getOut() {
		return out;
	}

	public void setOut(PrintStream out) {
		this.out = out;
	}

}
