package ex.annis.ql.analysis;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.PrintWriter;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;

import ex.annis.ql.helper.Ast2String;
import ex.annis.ql.helper.TreeDumper;
import ex.annis.ql.lexer.Lexer;
import ex.annis.ql.node.AAndExpr;
import ex.annis.ql.node.AOrExpr;
import ex.annis.ql.node.Node;
import ex.annis.ql.node.PExpr;
import ex.annis.ql.node.Start;
import ex.annis.ql.parser.Parser;

public class TestDnfNormalizer {

	@Test
	public void normalizeAndRememberOriginalPosition() {
		String input = "/das/ & ( /Haus/ & #1 . #2 | node & #1 . #3 )";
		Start start = parse(input);
		DnfNormalizer dnfNormalizer = new DnfNormalizer();
		start.apply(dnfNormalizer);
		AOrExpr orExpr = (AOrExpr) start.getPExpr();
		for (PExpr alternative : orExpr.getExpr()) {
			AAndExpr and = (AAndExpr) alternative;
			for (PExpr expr : and.getExpr()) {
				String str = new Ast2String().toString(expr);
				if ("/das/".equals(str))
					assertThat(dnfNormalizer.getPosition(expr), is(1));
				if ("/Haus/".equals(str))
					assertThat(dnfNormalizer.getPosition(expr), is(2));
				if ("node".equals(str))
					assertThat(dnfNormalizer.getPosition(expr), is(3));
			}
		}
	}

	private Start parse(String input) {
		try {
			Parser parser = new Parser(new Lexer(new PushbackReader(new StringReader(input))));
			Start start = parser.parse();
			return start;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void dumpTree(Node node) {
		StringWriter writer = new StringWriter();
		TreeDumper dumper = new TreeDumper(new PrintWriter(writer)); 
		node.apply(dumper);
		System.out.println(writer.toString());
	}
	
}
