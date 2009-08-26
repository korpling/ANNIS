package annis;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

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
	
	@SuppressWarnings("unchecked")
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
		assertThat(tableFormatter.formatAsTable(new ArrayList<Object>()), is("(empty)"));
	}
	
	@Test
	public void noFields() {
		assertThat(tableFormatter.formatAsTable(Arrays.asList(new Object())), is("(no columns to print)"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void emptyCollection() {
		assertThat(tableFormatter.formatAsTable(Arrays.asList(new ArrayList<Object>())), is("(no columns to print)"));
	}
	
	@Test
	public void unknownField() {
		assertThat(tableFormatter.formatAsTable(Arrays.asList(new Object()), "unknownField"), is("(no columns found)"));
	}
	
}
