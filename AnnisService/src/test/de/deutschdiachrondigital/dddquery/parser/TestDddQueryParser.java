package de.deutschdiachrondigital.dddquery.parser;

import static de.deutschdiachrondigital.dddquery.helper.Helper.dumpTree;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.deutschdiachrondigital.dddquery.analysis.DepthFirstAdapter;
import de.deutschdiachrondigital.dddquery.helper.BeanFactory;
import de.deutschdiachrondigital.dddquery.helper.TreeDumper;
import de.deutschdiachrondigital.dddquery.lexer.Lexer;
import de.deutschdiachrondigital.dddquery.lexer.LexerException;
import de.deutschdiachrondigital.dddquery.node.EOF;
import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.node.Token;

public class TestDddQueryParser {

	private DddQueryParser parser;
	private BeanFactory dddQuery;

	@Before
	public void setup() {
		dddQuery = new BeanFactory();
		parser = dddQuery.getDddQueryParser();
	}
	
	// post processors are applied in the order they are given in the list 
	@Test
	public void parseDddQueryAppliesPostProcessors() {
		final StringBuffer sb = new StringBuffer();
		List<DepthFirstAdapter> postProcessors = new ArrayList<DepthFirstAdapter>();
		postProcessors.add(new DepthFirstAdapter() {
			@Override
			public void caseStart(Start node) {
				sb.append("one ");
			}
		});
		postProcessors.add(new DepthFirstAdapter() {
			@Override
			public void caseStart(Start node) {
				sb.append("two");
			}
		});

		DddQueryParser parser = new DddQueryParser();
		parser.setPostProcessors(postProcessors);
		
		parser.parseDddQuery("a");
		assertThat(sb.toString(), is("one two"));
	}

	// see tree-tests.xml
	@Test
	public void runTestTrees() {
		Map<String, String> tests = dddQuery.getTreeTests();

		TreeDumper treeDumper = new TreeDumper();
		List<ParserTestCase> errors = new ArrayList<ParserTestCase>();
		for (Entry<String, String> test : tests.entrySet()) {
			ParserTestCase testCase = new ParserTestCase();
			testCase.input = test.getKey();
			testCase.expected = test.getValue().trim();
			try {
				testCase.actual = treeDumper.dumpTree(parser.parseDddQuery(testCase.input)).trim();
				if ( ! testCase.actual.equals(testCase.expected) )
					errors.add(testCase);
			} catch (ParseException e) {
				errors.add(testCase);
			}
		}
		
		if (errors.isEmpty())
			return;
		
		for (ParserTestCase error : errors) {
			System.out.println("error parsing: " + error.input);
			System.out.println("expected:\n" + error.expected);
			System.out.println("got:\n" + error.actual);
		}
		
		fail("some errors during parsing");
	}

	private class ParserTestCase {
		String input;
		String expected;
		String actual;
	}
	
	// example input that should be correctly parsed (better move to tree-tests.xml)
	@Test
	public void inputExamples() {
		String[] inputs = {
				
			// Achsen
			"/a/layer(name)::element()",
			
			"/a/alignment(role1, role2, greed1, greed2)::element()",	// TODO: (alt) ist das richtig?
			"/a/alignment(role1, role2, greed1)::element()",
			"/a/alignment(role1, role2)::element()",

			"/a/aligned(role1, role2, greed1, greed2)::span()",	// TODO: (alt) ist das richtig?
			"/a/aligned(role1, role2, greed1)::span()",
			"/a/aligned(role1, role2)::span()",
			
			// Behandlung von . und ..
			"./child::element()",
			".//a",
			". ^ a",
			"a/..",
			"a/../b",
			"../a",
			
		};
		
		boolean failed = false;
		for (String input : inputs) {
			try {
				parser.parseDddQuery(input);
			} catch (RuntimeException e) {
				System.out.println("input: " + input + "; error: " + e.getMessage() + "\n");
				failed = true;
			}
		}
		if (failed)
			fail("not all examples are accepted");
	}
	
	// example input that should result in an error
	@Test
	public void grammarErrors() {
		String[] inputs = {
				"/child::span(fehler)",
		};
		
		boolean failed = false;
		for (String input : inputs) {
			try {
				parser.parseDddQuery(input);
				System.out.println("bad input accepted: " + input);
				failed = true;
			} catch (RuntimeException e) {
				// okay
			}
		}
		if (failed)
			fail("some bad queries are accepted");
	}

	// helper method to check how the lexer tokenizes a string
	@Ignore
	public void lexer() throws LexerException, IOException {
		String input = "a/left-child::b";
		Lexer lexer = new Lexer(new PushbackReader(new StringReader(input)));
		while (true) {
			Token token = lexer.next();
			System.out.println(token);
			if (token instanceof EOF)
				return;
		}
	}
	
	@Test
	public void foo() throws ParserException, LexerException, IOException {
		String input = "a/child(tiger:func=\"OA\" urml:foo=r\"bar\")::b";
		Parser parser = new Parser(new Lexer(new PushbackReader(new StringReader(input))));
		Start start = parser.parse();
		dumpTree(start);
	}
	
}
