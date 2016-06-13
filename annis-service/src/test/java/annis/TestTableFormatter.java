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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class TestTableFormatter {

	// dummy table data
	private static final String PRINTME1 = "PRINTME1";
	private static final String DONTPRINTME1 = "DONTPRINTME1";
	private static final String PRINTME2 = "PRINTME2";
	private static final String METO2 = "METO2";
	private static final String DONTPRINTME2 = "DONTPRINTME2";
	private TableFormatter tableFormatter;
	
	@SuppressWarnings("unused")
	private class ObjectWithTableColumns {
		private String printMe;
		private String meTo;
		private String dontPrintMe;
		
		public ObjectWithTableColumns(String printMe, String meTo, String dontPrintMe) {
			this.printMe = printMe;
			this.meTo = meTo;
			this.dontPrintMe = dontPrintMe;
		}
	}
	
	@Before
	public void setup() {
		tableFormatter = new TableFormatter();
	}
	
	@Test
	public void formatAsTable() {
		// a list with 2 entries
		ObjectWithTableColumns o1 = new ObjectWithTableColumns(PRINTME1, null, DONTPRINTME1);
		ObjectWithTableColumns o2 = new ObjectWithTableColumns(PRINTME2, METO2, DONTPRINTME2);
		
		// what fields should be printed
		String[] fields = { "printMe", "meTo" };
		
		String expected = "" +
				" printMe |  meTo\n" +	// header: fields annotated with TableColumn
				"---------+------\n" +
				"PRINTME1 |      \n" +	// values
				"PRINTME2 | METO2\n";
		
		
		System.out.println(tableFormatter.formatAsTable(Arrays.asList(o1, o2), fields));
		assertEquals(expected, tableFormatter.formatAsTable(Arrays.asList(o1, o2), fields));
	}
	
	@Test
	public void collection() {
		// collections with 1 and 2 items
		Collection<String> c1 = Arrays.asList("1");
		Collection<String> c2 = Arrays.asList("2", "3");
		
		String expected = "" +
			"#0 | #1\n" +	// as many columns as the indivudual collections have items
			"---+---\n" +
			" 1 |   \n" +	// values
			" 2 |  3\n";

		System.out.println(tableFormatter.formatAsTable(Arrays.asList(c1, c2)));
		assertEquals(expected, tableFormatter.formatAsTable(Arrays.asList(c1, c2)));
	}
	
	@Test
	public void empty() {
		assertThat(tableFormatter.formatAsTable(new ArrayList<>()), is("(empty)"));
	}
	
	@Test
	public void noFields() {
		assertThat(tableFormatter.formatAsTable(Arrays.asList(new Object())), is("(no columns to print)"));
	}
	
	@Test
	public void emptyCollection() {
		assertThat(tableFormatter.formatAsTable(Arrays.asList(new ArrayList<>())), is("(no columns to print)"));
	}
	
	@Test
	public void unknownField() {
		assertThat(tableFormatter.formatAsTable(Arrays.asList(new Object()), "unknownField"), is("(no columns found)"));
	}
	
}
