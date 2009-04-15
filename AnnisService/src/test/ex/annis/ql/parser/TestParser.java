package ex.annis.ql.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ex.annis.ql.helper.AstBuilder;
import ex.annis.ql.helper.AstComparator;
import ex.annis.ql.helper.TreeDumper;
import ex.annis.ql.lexer.Lexer;
import ex.annis.ql.lexer.LexerException;
import ex.annis.ql.node.AAnnotationSearchExpr;
import ex.annis.ql.node.EOF;
import ex.annis.ql.node.PExpr;
import ex.annis.ql.node.Start;
import ex.annis.ql.node.TId;
import ex.annis.ql.node.Token;

public class TestParser {
	
	// used to build ASTs
	private AstBuilder b;

	@Before
	public void setupBuilder() {
		b = new AstBuilder();
	}
	
	@Test
	public void example() throws ParserException, LexerException, IOException {
		String input = "*=verb";
		Lexer lexer = new Lexer(new PushbackReader(new StringReader(input)));
		System.out.println(lexer.next().getClass());
//		Start start = new Parser(lexer).parse();
//		start.apply(new TreeDumper(new PrintWriter(System.out)));
	}
	
	/*
	 * Takes an input string, parses it and compares the generated AST to a template.
	 */
	private void testParser(String input, Start expectedTree) {
		try {
			Parser parser = new Parser(new Lexer(new PushbackReader(new StringReader(input))));
			Start actual = parser.parse();
			actual.apply(new AstComparator(expectedTree));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Tests whether the parser recognizes a number of (syntactically) valid queries.
	 */
	@Test
	public void inputStrings() {
		String[] inputs = {
				
				// search for annotations
				"typ=\"wert\"",
				"typ=/wert/",
				
				// existance search for annotations
				"typ",
				
				// search for text
				"\"text\"",
				"/regexp/",
				
				// unary operators
				"#1:root",
				"#1:arity=2",
				"#1:arity=2,3",
				"#1:tokenarity=2",
				"#1:tokenarity=2,3",
				
				// binary operators
				"#1 _=_ #2",
				"#1 _l_ #2",
				"#1 _r_ #2",
				"#1 _i_ #2",
				"#1 _ol_ #2",
				"#1 . #2",
				"#1 .* #2",
				"#1 .2 #3",
				"#1 .2,3 #3",
				"#1 > #2",
				"#1 >* #2",
				"#1 >2 #2",
				"#1 >2 , 3 #3",
				"#1 >@l #3",
				"#1 > @r #3",
				"#1 >edge #2",
				"#1 $ #2",
				"#1 $.* #2",
				"#1 @ #2",
				
				// boolean operators
				"! tok",
				"not tok",
				"nIcHt tok",
				"tok1 & tok2",
				"tok1 & tok2 && tok3 and tok4 uNd tok5",
				"tok1 | tok2",
				"tok1 | tok2 || tok3 or tok4 oDeR tok5",
				"tok1 ^ tok2",
				"tok1 ^ tok2 xor tok3",
				"either tok1 oder tok2",
				"entweder tok1 | tok2",
				"either tok1 or tok2 or tok3",
				
				// grouping
				"(tok)",
				"{[(tok]})",
				"tok1 & tok2 | tok3 & tok4",
				"(tok1 & tok2) | (tok3 & tok4)",
				
				// implicit and
				"(tok) (tok) (tok)",
				
				// complex expressions
				"! (! tok)",
				"(tok1 & tok2 & /foo/ & /bar/) ( (#1 >edge #2) (#1 _ol_ #3) (#2 .2,3 #4) | { #3 _i_ #4 } )",
				"\"in\" & \"der\" & \"dem\" & ( ( #1 .* #2 & #1 .* #3) | ( #1 .* #3 & #3 .*#2 ) )",
				"(\"in\" & \"der\" & #1 .* #2) | ( \"in\" & \"dem\" & #3 .* #4)",
		};
		for (String input : inputs) {
			try {
				Start start = (new Parser(new Lexer(new PushbackReader(new StringReader(input))))).parse();
				start.apply(new TreeDumper(new PrintWriter(System.out)));
			} catch (Exception e) {
				fail("input: " + input + "; error: " + e.getMessage());
			}
		}
	}
	
	@Test
	public void terminalAccessorMethodName() {
		AstComparator c = new AstComparator(new TId("foo"));
		
		assertEquals("Name der Getter-Methode f�r ein Terminal falsch berechnet", 
				"getAnnoGroup", c.terminalAccessorMethodName("anno_group"));
	}

	/**
	 * Test for a search expression with no annotation group and without text: typ:wert .
	 */
	@Test
	public void searchAnnotation() {
		Start expected = b.newStart(b.newAnnotationSearchExpr("typ", b.newWildTextSpec("wert")));
		testParser("typ=\"wert\"", expected);
	}
	
	@Test
	public void searchAnnotationRegexp() {
		Start expected = b.newStart(b.newAnnotationSearchExpr("typ", b.newRegexpTextSpec("regexp")));
		testParser("typ=/regexp/", expected);
	}
	
	@Test
	public void searchAnnotationExistance() {
		Start expected = b.newStart(b.newAnnotationSearchExpr("typ"));
		
		testParser("typ", expected);
	}
	
	@Test
	public void documentConstraint() {
		Start expected = b.newStart(b.newDocumentConstraintExpr(b.newWildTextSpec("name")));
		
		testParser("doc=\"name\"", expected);
		testParser("dok=\"name\"", expected);
	}
	
	/**
	 * Test for a simple text search: "text" .
	 */
	@Test
	public void searchPatternSimpleText() {
		Start expected = b.newStart(
				b.newTextSearchExpr(b.newWildTextSpec("text")));
				
		testParser("\"text\"", expected);
	}
	
	/**
	 * Test for a simple regexp search: /regexp/ .
	 */
	@Test
	public void searchPatternSimpleRegexp() {
		Start expected = b.newStart(
				b.newTextSearchExpr(b.newRegexpTextSpec("regexp")));

		testParser("/regexp/", expected);
	}

	/**
	 * XXX: Ist satz::"text" eine g�ltige Suche?
	 */
	@Ignore
	public void searchPatternTextInGroup() {
		fail("not implemented");
	}
	
	/**
	 * Test for: #1:root
	 */
	@Test
	public void opRoot() {
		Start expected = b.newStart(
				b.newLinguisticConstraintExpr(b.newRootLingOp(), "1"));
		
		testParser("#1:root", expected);
	}
	
	/**
	 * Test for: #2:arity=3
	 */
	@Test
	public void opArity() {
		Start expected = b.newStart(
				b.newLinguisticConstraintExpr(
						b.newArityLingOp(b.newRangeSpec("3")),
						"2"));
		
		testParser("#2:arity=3", expected);
	}
	
	/**
	 * Test for: #3:arity=4,5
	 */
	@Test
	public void opArityRange() {
		Start expected = b.newStart(
				b.newLinguisticConstraintExpr(
						b.newArityLingOp(b.newRangeSpec("4", "5")),
						"3"));
		
		testParser("#3:arity=4,5", expected);
	}
	
	/**
	 * Test for: #2:tokenarity=3
	 */
	@Test
	public void opTokenArity() {
		Start expected = b.newStart(
				b.newLinguisticConstraintExpr(
						b.newTokenArityLingOp(b.newRangeSpec("3")),
						"2"));
		
		testParser("#2:tokenarity=3", expected);
	}
	
	/**
	 * Test for: #3:tokenarity=4,5
	 */
	@Test
	public void opTokenArityRange() {
		Start expected = b.newStart(
				b.newLinguisticConstraintExpr(
						b.newTokenArityLingOp(b.newRangeSpec("4", "5")),
						"3"));
		
		testParser("#3:tokenarity=4,5", expected);
	}
	
	/**
	 * Test for: #1 _=_ #2
	 */
	@Test
	public void opExactOverlap() {
		Start expected = b.newStart(
				b.newLinguisticConstraintExpr(b.newExactOverlapLingOp(), "1", "2"));
		
		testParser("#1 _=_ #2", expected);
	}
	
	/**
	 * Test for: #1 _l_ #2
	 */
	@Test
	public void opLeftAlign() {
		Start expected = b.newStart(
				b.newLinguisticConstraintExpr(b.newLeftAlignLingOp(), "1", "2"));
		
		testParser("#1 _l_ #2", expected);
	}
	
	/**
	 * Test for: #1 _r_ #2
	 */
	@Test
	public void opRightAlign() {
		Start expected = b.newStart(
				b.newLinguisticConstraintExpr(b.newRightAlignLingOp(), "1", "2"));
		
		testParser("#1 _r_ #2", expected);
	}
	
	/**
	 * Test for: #1 _i_ #2
	 */
	@Test
	public void opInclusion() {
		Start expected = b.newStart(
				b.newLinguisticConstraintExpr(b.newInclusionLingOp(), "1", "2"));
		
		testParser("#1 _i_ #2", expected);
	}
	
	/**
	 * Test for: #1 _ol_ #2
	 */
	@Test
	public void opLeftOverlap() {
		Start expected = b.newStart(
				b.newLinguisticConstraintExpr(b.newLeftOverlapLingOp(), "1", "2"));
		
		testParser("#1 _ol_ #2", expected);
	}
		
	/**
	 * Test for: #1 . #2
	 */
	@Test
	public void opPrecedenceDirect() {
		Start expected = b.newStart(
				b.newLinguisticConstraintExpr(b.newPrecedenceLingOp(b.newDirectPrecedenceSpec()), 
						"1", "2"));
		
		testParser("#1 . #2", expected);
	}
		
	/**
	 * Test for: #1 .* #2
	 */
	@Test
	public void opPrecedenceIndirect() {
		Start expected = b.newStart(
				b.newLinguisticConstraintExpr(b.newPrecedenceLingOp(b.newIndirectPrecedenceSpec()), 
						"1", "2"));
		
		testParser("#1 .* #2", expected);
	}
		
	/**
	 * Test for: #1 .2 #3
	 */
	@Test
	public void opPrecedenceExact() {
		Start expected = b.newStart(
				b.newLinguisticConstraintExpr(
						b.newPrecedenceLingOp(
								b.newRangePrecedenceSpec(
										b.newRangeSpec("2"))), 
						"1", "3"));
		
		testParser("#1 .2 #3", expected);
	}
		
	/**
	 * Test for: #1 .2,3 #4
	 */
	@Test
	public void opPrecedenceRange() {
		Start expected = b.newStart(
				b.newLinguisticConstraintExpr(
						b.newPrecedenceLingOp(
								b.newRangePrecedenceSpec(
										b.newRangeSpec("2", "3"))), 
						"1", "4"));
		
		testParser("#1 .2,3 #4", expected);
	}
	
	/**
	 * Test for: #1 > #2
	 */
	@Test
	public void opDominanceDirect() {
		Start expected = b.newStart(
				b.newLinguisticConstraintExpr(b.newDominanceLingOp(b.newDirectDominanceSpec()), 
						"1", "2"));
		
		testParser("#1 > #2", expected);
	}
		
	/**
	 * Test for: #1 >* #2
	 */
	@Test
	public void opDominanceIndirect() {
		Start expected = b.newStart(
				b.newLinguisticConstraintExpr(b.newDominanceLingOp(b.newIndirectDominanceSpec()), 
						"1", "2"));
		
		testParser("#1 >* #2", expected);
	}
		
	/**
	 * Test for: #1 >2 #3
	 */
	@Test
	public void opDominanceExact() {
		Start expected = b.newStart(
				b.newLinguisticConstraintExpr(
						b.newDominanceLingOp(
								b.newRangeDominanceSpec(
										b.newRangeSpec("2"))), 
						"1", "3"));
		
		testParser("#1 >2 #3", expected);
	}
		
	/**
	 * Test for: #1 >2,3 #4
	 */
	@Test
	public void opDominanceRange() {
		Start expected = b.newStart(
				b.newLinguisticConstraintExpr(
						b.newDominanceLingOp(
								b.newRangeDominanceSpec(
										b.newRangeSpec("2", "3"))), 
						"1", "4"));
		
		testParser("#1 >2,3 #4", expected);
	}
	
	/**
	 * Test for: #1 >@l #2
	 */
	@Test
	public void opDominanceLeftLeaf() {
		Start expected = b.newStart(
				b.newLinguisticConstraintExpr(
						b.newDominanceLingOp(b.newLeftLeafDominanceSpec()),
						"1", "2"));
		
		testParser("#1 >@l #2", expected);
	}
	
	/**
	 * Test for: #1 >@r #2
	 */
	@Test
	public void opDominanceRightLeaf() {
		Start expected = b.newStart(
				b.newLinguisticConstraintExpr(
						b.newDominanceLingOp(b.newRightLeafDominanceSpec()),
						"1", "2"));
		
		testParser("#1 >@r #2", expected);
	}
	
	/**
	 * Test for: #1 $ #2
	 */
	@Test
	public void opSibling() {
		Start expected = b.newStart(
				b.newLinguisticConstraintExpr(b.newSiblingLingOp(), "1", "2"));
		testParser("#1 $ #2", expected);
	}
	
	/**
	 * Test for: #1 $.* #2
	 */
	@Test
	public void opSiblingAndPrecedence() {
		Start expected = b.newStart(
				b.newLinguisticConstraintExpr(b.newSiblingAndPrecedenceLingOp(), "1", "2"));
		testParser("#1 $.* #2", expected);
	}
	
	/**
	 * Test for: #1 @ #2
	 */
	@Test
	public void opSameAnnotationGroup() {
		Start expected = b.newStart(
				b.newLinguisticConstraintExpr(b.newSameAnnotationGroupLingOp(), "1", "2"));
		testParser("#1 @ #2", expected);
	}
	
//	/**
//	 * Test for: tok1 | tok2 || tok3 or tok4 oDeR tok5
//	 */
//	@Test
//	public void or() {
//		Start expected = b.newStart(
//				b.newOrExpr(
//						b.newOrExpr(
//								b.newOrExpr(
//										b.newOrExpr(token("tok1"), token("tok2")),
//										token("tok3")),
//								token("tok4")),
//						token("tok5")));
//		
//		testParser("tok1 | tok2 || tok3 or tok4 oDeR tok5", expected);
//	}
	
	/**
	 * Test for a grouped expression: ( { [ tok ) ] }
	 * 
	 * Einfache Gruppierungen werden nicht im abstrakten Syntaxbaum dargestellt.
	 */
	@Test
	public void simpleGroupedExpression() {
		Start expected = b.newStart(
				token("tok"));
				
		testParser("( { [ tok ) ] }", expected);
	}
	
	/**
	 * Test for a grouped expression overriding operator association: (tok1 & tok2) | (tok3 & tok4)
	 */
//	@Test
//	public void groupedExpression() {
//		Start expected = b.newStart(
//				b.newOrExpr(
//						b.newAndExpr(token("tok1"), token("tok2")),
//						b.newAndExpr(token("tok3"), token("tok4"))));
//
//		testParser("(tok1 & tok2) | (tok3 & tok4)", expected);
//	}
	
	/**
	 * Test for implicit and: (tok1) (tok2) (tok3)
	 */
	@Test
	public void implicitAnd() {
		
		List<PExpr> factors = new ArrayList<PExpr>();
		factors.add(token("tok1"));
		factors.add(token("tok2"));
		factors.add(token("tok3"));
		
		Start expected = b.newStart(
				b.newImplicitAndExpr(factors));
		
		testParser("(tok1) (tok2) (tok3)", expected);
		
	}
	
	/*
	 * Convenience method to create a representation for tok
	 */
	private AAnnotationSearchExpr token(String name) {
		return b.newAnnotationSearchExpr(name);
	}
	
	@Test
	public void namespaces() throws ParserException, LexerException, IOException {
		String input = "mmax:anaphor_antecedent";
		Lexer lexer = new Lexer(new PushbackReader(new StringReader(input), 3000));

		Parser parser = new Parser(new Lexer(new PushbackReader(new StringReader(input), 3000)));
		Start actual = parser.parse();
		actual.apply(new TreeDumper(new PrintWriter(System.out)));
	}
}