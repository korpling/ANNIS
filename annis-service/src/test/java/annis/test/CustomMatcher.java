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
package annis.test;

import java.util.Collection;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class CustomMatcher {

	public static Matcher<Collection<?>> hasInstance(final Class<?> clazz) {
		return new TypeSafeMatcher<Collection<?>>() {
			
			@Override
			public boolean matchesSafely(Collection<?> collection) {
				for (Object item : collection) {
					if (clazz.isInstance(item))
						return true;
				}
				return false;
			}

			public void describeTo(Description description) {
				description.appendText("a list containing an instance of " + clazz);
			}
			
		};
	}
	
}
