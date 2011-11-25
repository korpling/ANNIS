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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;

// annis.home sollte nicht im Test, sondern außerhalb gesetzt werden
@Deprecated
@Ignore
public class AnnisHomeTest {

	@BeforeClass
	public static void setupAnnisHomeProperty() {
//		System.out.println(System.getProperty("annis.home"));
//		System.setProperty("annis.home", ".");
//		PropertyConfigurator.configure("./conf/logging.properties");
//		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%d{HH:MM:ss.SSS} %C{1} %p: %m\n")));
	}

	@AfterClass
	public static void tearDownAnnisHomeProperty() {
//		System.clearProperty("annis.home");
	}
	
}