package ex.annis.ql.analysis;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.PrintWriter;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

import ex.annis.ql.helper.TreeDumper;
import ex.annis.ql.lexer.Lexer;
import ex.annis.ql.node.Node;
import ex.annis.ql.node.Start;
import ex.annis.ql.parser.NodeSearchNormalizer;
import ex.annis.ql.parser.Parser;
import ex.annis.ql.parser.TokenSearchNormalizer;


public class TestSemanticAnalysis {
	
	private boolean print;
	
	@Before
	public void noPrint() {
		print = false;
	}
	
	@Test
	public void regexpSearch() {
		good("/das/");
	}

	@Test
	public void textSearch() {
		good("\"Dorf\"");
	}
	
	@Test
	public void annotation() {
		good("das=/Haus/");
	}
	
	@Test
	public void token() {
		good("tok");
	}
	
	@Test
	public void node() {
		good("node");
	}
	
	@Test
	public void errorSingleLingOp() {
		bad("#1 . #2");
	}
	
	@Test
	public void and() {
		good("/das/ & /Haus/ & #1 . #2");
	}
	
	@Test
	public void errorMissingLingOp() {
		bad("/das/ & /Haus/");
	}
	
	@Test
	public void errorUnconnected() {
		bad("/das/ & cat=/NP/ & node & #1 . #2");
	}
	
	@Test
	public void bug1() {
		good("node & pos=\"VVFIN\" & cat=\"S\" & node & #3 >[func=\"OA\"] #1 & #3 >[func=\"SB\"] #4 & #3 > #2 & #1 .* #2 & #2 .* #4");
	}
	
	@Test
	public void errorUnknownSearchRef() {
		bad("/das/ & /Haus/ & #1 . #3");
	}
	
	@Test
	public void distributedAnd() {
		good("/das/ & ( cat=/NP/ & #1 . #2 | /Haus/ & #1 . #3 )");
	}

	@Test
	public void or() {
		good("( node & cat=/NP/ & #1 . #2 | /das/ & /Haus/ & #3 . #4 )");
	}
	
	@Test
	public void badAnd() {
		bad("node & ( cat=/NP/ & #1 . #2 | /Haus/ & #1 . #2 )");
	}
	
	@Test
	public void badOr() {
		bad("( node & cat=/NP/ & #1 . #2 | /das/ & /Haus/ & #1 . #2 )");
	}

	private void bad(String input) {
		SemanticAnalysisImpl analysis = analyze(input);
		assertThat(analysis.isValid(), is(false));
	}
	
	private void good(String input) {
		SemanticAnalysisImpl analysis = analyze(input);
		assertThat(analysis.isValid(), is(true));
	}
	
	private SemanticAnalysisImpl analyze(String input) {
		Start start = parse(input);
		if (print)
			dumpTree(start);
		start.apply(new TokenSearchNormalizer());
		start.apply(new NodeSearchNormalizer());
		SemanticAnalysisImpl analysis = new SemanticAnalysisImpl();
		analysis.setDnfNormalizer(new DnfNormalizer());
		analysis.setExpressionCounter(new SearchExpressionCounter());
		start.apply(analysis);
		return analysis;
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

	public static void dumpTree(Node node) {
		StringWriter writer = new StringWriter();
		TreeDumper dumper = new TreeDumper(new PrintWriter(writer)); 
		node.apply(dumper);
		System.out.println(writer.toString());
	}
	
	
}
