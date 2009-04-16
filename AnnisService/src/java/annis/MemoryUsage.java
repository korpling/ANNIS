package annis;

import org.apache.log4j.Logger;

/**
 * Utility class that prints out memory usage of the Java VM to a logfile.
 * 
 * @author Viktor Rosenfeld <rosenfel@informatik.hu-berlin.de>
 */
public class MemoryUsage {

	private static Logger log = Logger.getLogger(MemoryUsage.class);	
	
	/**
	 * Print total, free and maximum memory of the VM to a logfile.
	 */
	public static void logMemoryUsage() {
		Runtime runtime = Runtime.getRuntime();
		
		String total = String.valueOf(runtime.totalMemory() / 1024) + "k";
		String free = String.valueOf(runtime.freeMemory() / 1024) + "k";
		String max = String.valueOf(runtime.maxMemory() / 1024) + "k";
		
		log.debug("total: " + total + "; free: " + free + "; max: " + max);
	}
	
}
