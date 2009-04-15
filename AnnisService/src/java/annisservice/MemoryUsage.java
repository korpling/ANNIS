package annisservice;

import org.apache.log4j.Logger;

public class MemoryUsage {

	private static Logger log = Logger.getLogger(MemoryUsage.class);	
	
	public static void print() {
		Runtime runtime = Runtime.getRuntime();
		
		String total = String.valueOf(runtime.totalMemory() / 1024) + "k";
		String free = String.valueOf(runtime.freeMemory() / 1024) + "k";
		String max = String.valueOf(runtime.maxMemory() / 1024) + "k";
		
		log.debug("total: " + total + "; free: " + free + "; max: " + max);
	}
	
}
