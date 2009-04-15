package de.deutschdiachrondigital.dddquery.sql.preprocessors;

import static org.junit.Assert.fail;

import java.io.PrintWriter;

import org.junit.Before;
import org.junit.Test;

import de.deutschdiachrondigital.dddquery.helper.BeanFactory;
import de.deutschdiachrondigital.dddquery.helper.TreeDumper;
import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.parser.DddQueryParser;

// TODO: welche Defizite hat der von SableCC generierte Parse-Baum -> refactoren
public class TestSemanticAnalysis {

	private DddQueryParser parser;

	@Before
	public void setup() {
		parser = new BeanFactory().getDddQueryParser();
	}
	
	@Test
	public void onlyBooleanLogicAndPathsAsOuterExprs() {
		boolean failed = false;
		String[] good = {
				"/a",
				"(a | b) & c",
				"a[1 = 2 & 3 != 4]"
		};
		for (String input : good) {
			try {
				parser.parseDddQuery(input).apply(new SemanticAnalysis());
			} catch (RuntimeException e) {
				System.out.println("good input not accepted: " + input);
				failed = true;
			}
		}
		
		if (failed)
			fail("some good queries are not accepted");

		String[] bad = {
				"1 = 2",
				"1 != 2",
				"1 < 2",
				"1 > 2",
				"1 <= 2",
				"1 >= 2",
				"1 + 2",
				"1 - 2",
				"1 * 2",
				"1 div 2",
				"1 idiv 2",
				"1 mod 2",
				"1",
				"\"foo\"",
				"count(1)",
				"1 & 2",
				"1 | 2",
		};
		for (String input : bad) {
			try {
				Start start = parser.parseDddQuery(input);
				start.apply(new SemanticAnalysis());
				start.apply(new TreeDumper(new PrintWriter(System.out)));
				System.out.println("bad input accepted: " + input);
				failed = true;
			} catch (RuntimeException e) {
				// okay
			}
		}
		
		if (failed)
			fail("some non-sensical queries are accepted");
	}

}
