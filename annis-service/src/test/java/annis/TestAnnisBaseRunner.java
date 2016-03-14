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

import java.io.IOException;
import java.io.PrintStream;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;


public class TestAnnisBaseRunner {

	/*
	 * Mock implementation of AnnisBaseRunner to allow testing.
	 * 
	 * - a bean for this class is defined in TestAnnisBaseRunner-context.xml
	 * - the runInteractive() method sets interactive = true
	 * - command doStuff() implemented that saves its arguments
	 */
	public static class MockAnnisRunner extends AnnisBaseRunner { 
		
		boolean interactive = false;
		String args;
		
		@Override
		protected void runInteractive() {
			interactive = true;
		}
		
		public void doKnownCommand(String args) {
			this.args = args;
		}
		
	};
	
	// object under test
	private MockAnnisRunner instance;
	
	// create a fresh instance for each test
	@Before
	public void setupRunnerInstance() {
		// create an instance
		instance = (MockAnnisRunner) MockAnnisRunner.getInstance("mockAnnisRunner", false, "annis/TestAnnisBaseRunner-context.xml");
	}
	
	// a fresh instance has out set to System.out
	@Test
	public void freshInstanceOutIsSystemOut() {
		PrintStream out = instance.getOut();
		assertThat(out, is(not(nullValue())));
		assertThat(out, is(sameInstance(System.out)));
	}
	
	// if argument list is empty, the program runs interactive
	@Test
	public void runInteractive() throws IOException {
		// sanity check
		assertThat(instance.interactive, is(false));
		
		// empty argument list => interactive run
		String[] args = { };
		instance.run(args);
		
		// test interactive run
		assertThat(instance.interactive, is(true));
	}
	
	// if argument list is not empty, the first arg is the command, the second the arguments
	@Test
	public void runKnownCommand() throws IOException {
		// sanity check
		assertThat(instance.args, is(nullValue()));
		
		// command "stuff" with arguments
		String[] args = { "knownCommand arg1 arg2"};
		instance.run(args);
		
		// test execution of "stuff" command
		assertThat(instance.args, is("arg1 arg2"));
	}

	// if an unknown command is requested, print an error
	@Test(expected=UsageException.class)
	public void runCommandUnknownCommand() {
		instance.runCommand("unknownCommand", "");
	}
}
