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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Utility class that prints out memory usage of the Java VM to a logfile.
 * 
 * @author Viktor Rosenfeld <rosenfel@informatik.hu-berlin.de>
 */
public class MemoryUsage {

	private static final Logger log = LoggerFactory.getLogger(MemoryUsage.class);	
	
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
