package de.deutschdiachrondigital.dddquery.helper;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoAnnotations.Mock;

import de.deutschdiachrondigital.dddquery.lexer.Lexer;
import de.deutschdiachrondigital.dddquery.lexer.LexerException;
import de.deutschdiachrondigital.dddquery.node.EOF;
import de.deutschdiachrondigital.dddquery.node.Token;


public class Foo {

	public interface Next {
		public boolean next();
	}
	
	@Test 
	public void next() {
		Next next = mock(Next.class);
		stub(next.next()).toReturn(true);
		stub(next.next()).toReturn(false);
		
		System.out.println(next.next());
		System.out.println(next.next());
	}
	
	
	// NULL-Werte in numeric-Feldern werden als Literal "0" zurückgeliefert
//	@Test
	public void getLongNull() throws SQLException {
		QueryExecution execution = new QueryExecution();
		ResultSet resultSet = execution.executeQuery("select * from long_test");
		while (resultSet.next())
			System.out.println(resultSet.getLong(1));
	}
	
	class Bar {
		void do1() { }
		void do2() { }
		void do3() { }
	}
	
	class Baz {
		void doit(Bar bar) {
			bar.do1();
			bar.do3();
			bar.do2();
		}
	}
	
	@Mock private Bar bar;
	
	@Test
	public void mocking() {
		MockitoAnnotations.initMocks(this);
		
		new Baz().doit(bar);
		
		InOrder inOrder = inOrder(bar);
		inOrder.verify(bar).do1();
		inOrder.verify(bar).do3();
		inOrder.verify(bar).do2();
		verifyNoMoreInteractions(bar);
	}
	
	@Test
	public void unicodeSql() throws SQLException, ClassNotFoundException {
		String[] inputs = {
				"delete from foo",
				"insert into foo values ( 'a', 'ä' )",
				"insert into foo values ( 'o', 'ö' )",
				"insert into foo values ( 'u', 'ü' )",
				"insert into foo values ( 's', 'ß' )",
		};
		QueryExecution execution = new QueryExecution();
		Connection connection = execution.getConnection();
		for (String input : inputs)
			connection.createStatement().execute(input);
		ResultSet result = execution.executeQuery("select * from foo where umlaut = 'ä'");
		result.next();
		System.out.println(result.getString(2));
	}
	
//	@Test
	public void arrayEquals() {
		int[] a = {1, 2, 3};
		int[] b = {1, 2, 3};
//		assertThat(a, is(b));
//		assertThat(a.toString(), is(b.toString()));
		assertThat(Arrays.equals(a, b), is(true));
	}
	
//	@Test
	public void lex() throws LexerException, IOException {
//		String input = "STRUCT#[@lemma = r\".+[^aeiouäöü]chen\"]$n1 & STRUCT#[@pos = \"NN\"]$n2 & $n1/matching-element::$n2";
		String input = "STRUCT#$n1/@lemma::r\".+[^aeiouäöü]chen\" & STRUCT#$n2/@pos::\"NN\" & $n1/matching-element::$n2";
//		String input = "STRUCT#[. = \",\"]";
		Lexer lexer = new Lexer(new PushbackReader(new StringReader(input)));
		Token t = null;
		while ( ! ((t = lexer.next()) instanceof EOF) ) {
			System.out.println(t.getPos() + "\t" + t.getText() + "\t" + t.getClass());
		}
	}

}
