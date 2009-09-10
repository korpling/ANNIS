package de.deutschdiachrondigital.dddquery.helper;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;

import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.parser.DddQueryParser;

@Ignore
public class TestAst2String {

	@Ignore
	public void test() {
		DddQueryParser parser = new BeanFactory().getDddQueryParser();
		String[] inputs = {
				"/a",
				"//b/@c",
				"a",
				"a = b",
				"a[@b = 'c']",
				"a[b = 'c'][d = 1]",
				"a#",
				"a#(m)",
				"a$v",
				"a#(m)[pred][pred2]$v",
				"a | b",
				"a & b"
		};
		for (String input : inputs) {
			System.out.println(input);
			assertThat(string(parser.parse(input)), is(input));
		}
	}
	
	private String string(Start start) {
		Ast2String ast2String = new Ast2String();
		start.apply(ast2String);
		return ast2String.getResult();
	}
	
}
