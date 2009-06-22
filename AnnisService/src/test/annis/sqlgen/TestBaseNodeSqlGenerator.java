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
