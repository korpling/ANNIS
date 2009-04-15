package de.deutschdiachrondigital.dddquery.helper;

import java.io.File;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import annisservice.AnnisBaseRunner;

public class AnnisServiceRunner extends AnnisBaseRunner {

	private static Logger log = Logger.getLogger(AnnisServiceRunner.class);
	private static Thread mainDaemonThread;
	private static AnnisServiceRunner annisService;

	public static void main(String[] args) {
		annisService = new AnnisServiceRunner();
		
		// run as a deamon?
		if (args.length == 1 && ( "-d".equals(args[0]) ))
			annisService.startAndDetach();
		
		// no, run in debug mode
		else {
			// create a logging appender for stdout
			Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%d{HH:mm:ss,SSS} [%t] %C{1} %p: %m\n")));
			annisService.start();
		}
	}

	/**
	 * start the AnnisService and detach from the shell
	 */
	public void startAndDetach() {
		try {
			start();
			daemonize();
			
		} catch (Throwable e) {
			log.fatal("Startup failed.", e);
			System.err.println("Startup failed.");
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void start() {
		log.info("Starting up...");
		
		// start the RMI service
		new BeanFactory().runService();
	}
	
	private void daemonize() {
		// save the current thread
		mainDaemonThread = Thread.currentThread();
		
		// add a shutdown hook to achieve an orderly shutdown
		Runtime.getRuntime().addShutdownHook( new Thread() { 
			public void run() { 
				annisService.shutdown(); 
			} 
		} );		

		// close stdout and stderr to detach process from shell
		System.err.close();
		System.out.close();
		
		log.info("Running as a daemon.");
	}
	
	/**
	 * shutdown the AnnisService
	 * - ensure that current work load finishes, FIXME: shutdownrequested!
	 * - delete pid file
	 */
	public void shutdown() {
		log.info("Shutting down...");

		// block, until current thread completes
		try	{
			mainDaemonThread.join();
		} catch(InterruptedException e)	{
			log.error("Interrupted while waiting on main daemon thread to complete.");
		}

		// delete pid file
		boolean success = new File(System.getProperty("annisservice.pid_file")).delete();
		if (!success)
			log.error("Couldn't delete pid file.");
	}

}
