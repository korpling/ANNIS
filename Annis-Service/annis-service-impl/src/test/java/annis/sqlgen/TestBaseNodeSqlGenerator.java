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
package annis.sqlgen;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class TestBaseNodeSqlGenerator {

	private BaseNodeSqlGenerator generator;
	
	@Before
	public void setup() {
		generator = new BaseNodeSqlGenerator();
	}
	
	// join two columns with a positive offset
	@Test
	public void joinPlus() {
		String expected = "lhs <op> rhs + 0";
		String actual = generator.numberJoin("<op>", "lhs", "rhs", 0);
		assertEquals(expected, actual);
	}
	
	// join two columns with a negative offset
	@Test
	public void joinMinus() {
		String expected = "lhs <op> rhs - 1";
		String actual = generator.numberJoin("<op>", "lhs", "rhs", -1);
		assertEquals(expected, actual);
	}
	
	// SQL string literals are enclosed in single quotes
	@Test
	public void sqlString() {
		String string = "string";
		assertThat(generator.sqlString(string), is("'" + string + "'"));
	}
	
	// join two columns
	@Test
	public void join() {
		String expected = "lhs <op> rhs";
		String actual = generator.join("<op>", "lhs", "rhs");
		assertEquals(expected, actual);
	}
	
}
