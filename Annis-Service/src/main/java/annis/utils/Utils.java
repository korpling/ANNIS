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
package annis.utils;

import java.util.List;

public class Utils {

	public static String min(List<Long> runtimeList) {
		long min = Long.MAX_VALUE;
		for (long value : runtimeList)
			min = Math.min(min, value);
		return String.valueOf(min);
	}

	public static String max(List<Long> runtimeList) {
		long max = Long.MIN_VALUE;
		for (long value : runtimeList)
			max = Math.max(max, value);
		return String.valueOf(max);
	}
	
	public static String avg(List<Long> runtimeList) {
		if (runtimeList.isEmpty())
			return "";
		
		long sum = 0;
		for (long value : runtimeList)
			sum += value;
		return String.valueOf(sum / runtimeList.size());
	}


}
