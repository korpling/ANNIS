package ex.annis.ql.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import annisservice.exceptions.AnnisQLSemanticsException;

import de.deutschdiachrondigital.dddquery.helper.AnnisQlTranslator;
import ex.annis.ql.helper.AstBuilder;
import ex.annis.ql.lexer.LexerException;
import ex.annis.ql.node.ALinguisticConstraintExpr;
import ex.annis.ql.node.ARangeSpec;
import ex.annis.ql.node.PDominanceSpec;
import ex.annis.ql.node.PExpr;
import ex.annis.ql.node.PLingOp;
import ex.annis.ql.node.PPrecedenceSpec;
import ex.annis.ql.node.Start;
import ex.annis.ql.parser.ParserException;

public class TestAnnisQlTranslator {
	
	private AstBuilder b;
	private AnnisQlTranslator translator;
	
	private ALinguisticConstraintExpr expr1;
	private ALinguisticConstraintExpr expr2;
	private ALinguisticConstraintExpr expr3;

	@Before
	public void setup() {
		
		// mapper under test
		translator = new AnnisQlTranslator(null);
		
		// Builder for abstract syntax trees
		b = new AstBuilder();

		// a few expressions with a simple mapping for boolean operators ($vi/$vj)
		expr1 = b.newLinguisticConstraintExpr(b.newDominanceLingOp(b.newDirectDominanceSpec()), "1", "2");
		expr2 = b.newLinguisticConstraintExpr(b.newDominanceLingOp(b.newDirectDominanceSpec()), "3", "4");
		expr3 = b.newLinguisticConstraintExpr(b.newDominanceLingOp(b.newDirectDominanceSpec()), "5", "6");
	}
	
	@Test
	public void edgeAnnotation() throws ParserException, LexerException, IOException, AnnisQLSemanticsException {
		testMapping("#1 >[tiger:func=\"OA\"] #2", "$n1/child(tiger:func=\"OA\")::$n2");
	}

	private void testMapping(String annis, String expected) throws ParserException, LexerException, IOException, AnnisQLSemanticsException {
//		Parser parser = new Parser(new Lexer(new PushbackReader(new StringReader(annis))));
//		Start start = parser.parse();
//
//		AnnisQlTranslator mapper = new AnnisQlTranslator(new SemanticAnalysisImpl().analyze(start));
//		start.apply(mapper);
//		start.apply(new TreeDumper(new PrintWriter(System.out)));

		String actual = new AnnisQlTranslator().translate(annis);
		
		assertEquals("wrong mapping", expected, actual);
	}

	@Test
	public void textSearch() throws ParserException, LexerException, IOException, AnnisQLSemanticsException {
		testMapping("\"hello\"", "STRUCT#(n1)[. = \"hello\"]$n1");
	}
	
	@Test
	public void textSearchOnlyToken() throws ParserException, LexerException, IOException, AnnisQLSemanticsException {
		testMapping("tok", "STRUCT#(n1)$n1");
	}
	
	@Test
	public void textRegexpSearch() throws ParserException, LexerException, IOException, AnnisQLSemanticsException {
		testMapping("/world/", "STRUCT#(n1)[. = r\"world\"]$n1");
	}

	// FIXME: typ="wert" oder typ=/wert/
	@Test
	public void annotationSearchNoText() throws ParserException, LexerException, IOException, AnnisQLSemanticsException {
		testMapping("typ=\"wert\"", "STRUCT#(n1)[@typ = \"wert\"]$n1");
	}
	
	// FIXME: aus der grammatik rausnehmen?
//	@Test
	public void annotationSearchText() throws ParserException, LexerException, IOException, AnnisQLSemanticsException {
		testMapping("typ:wert:\"text\"", "//typ[@v=\"wert\"]$v1/element-span::span()#/contained::\"text\"");
	}
	
	private void testUnaryLingOp(PLingOp lingOp, String dddQuery) {
		testLingOp(b.newLinguisticConstraintExpr(lingOp, "3"), dddQuery);
	}

	private void testBinaryLingOp(PLingOp lingOp, String dddQuery) {
		testLingOp(b.newLinguisticConstraintExpr(lingOp, "1", "2"), dddQuery);
	}

	private void testLingOp(ALinguisticConstraintExpr node, String dddQuery) {
		translator.caseALinguisticConstraintExpr(node);
		assertEquals("wrong mapping", dddQuery, translator.getDddQuery());
	}
	
	@Test
	public void caseAExactOverlapLingOp() {
		testBinaryLingOp(b.newExactOverlapLingOp(), "$n1/matching-element::$n2");
	}

	@Test
	public void caseALeftAlignLingOp() {
		testBinaryLingOp(b.newLeftAlignLingOp(), "$n1/left-align::$n2");
	}
	
	@Test
	public void caseARightAlignLingOp() {
		testBinaryLingOp(b.newRightAlignLingOp(), "$n1/right-align::$n2");
	}
	
	@Test
	public void caseAInclusionLingOp() {
		testBinaryLingOp(b.newInclusionLingOp(), "$n1/containing::$n2");
	}
	
	@Test
	public void caseALeftOverlapLingOp() {
		testBinaryLingOp(b.newLeftOverlapLingOp(), "$n1/overlapping-preceding::$n2");
	}
	
	@Test
	public void caseAPrecedenceLingOpDirect() {
		testBinaryLingOp(b.newPrecedenceLingOp(
				b.newDirectPrecedenceSpec()), "$n1 -> $n2");
	}
	
	@Test
	public void caseAPrecedenceLingOpIndirect() {
		testBinaryLingOp(b.newPrecedenceLingOp(
				b.newIndirectPrecedenceSpec()), "$n1 --> $n2");
	}
	
	@Test 
	public void caseAPrecedenceLingOpRangeExact() {
		ARangeSpec spec = b.newRangeSpec("2");
		String id = String.valueOf(spec.hashCode());
		
		testBinaryLingOp(b.newPrecedenceLingOp(b.newRangePrecedenceSpec(spec)), 
				"( ( $v1/immediately-following::$v2_1_" + id + 
				" & $v2_1_" + id + "/immediately-following::$v2_2_" + id + 
				" & $v2_2_" + id + "/immediately-following::$v2 ) )");
	}

	@Test 
	public void caseAPrecedenceLingOpRangeInterval() {
		ARangeSpec spec = b.newRangeSpec("1", "3");
		String id = String.valueOf(spec.hashCode());
		
		testBinaryLingOp(b.newPrecedenceLingOp(b.newRangePrecedenceSpec(spec)), 
				"( ( $v1/immediately-following::$v1_1_" + id + 
				" & $v1_1_" + id + "/immediately-following::$v2 ) | " +
				"( $v1/immediately-following::$v2_1_" + id + 
				" & $v2_1_" + id + "/immediately-following::$v2_2_" + id + 
				" & $v2_2_" + id + "/immediately-following::$v2 ) | " +
				"( $v1/immediately-following::$v3_1_" + id + 
				" & $v3_1_" + id + "/immediately-following::$v3_2_" + id + 
				" & $v3_2_" + id + "/immediately-following::$v3_3_" + id + 
				" & $v3_3_" + id + "/immediately-following::$v2 ) )");
	}
	
	@Test(expected=RuntimeException.class)
	public void caseAPrecedenceLingOpUnknownSpec() {
		testBinaryLingOp(b.newPrecedenceLingOp((PPrecedenceSpec) null), "");
	}
	
	@Test
	public void caseADominanceLingOpDirect() {
		testBinaryLingOp(b.newDominanceLingOp(
				b.newDirectDominanceSpec()), "$n1/$n2");
	}
	
	@Test
	public void caseADominanceLingOpIndirect() {
		testBinaryLingOp(b.newDominanceLingOp(
				b.newIndirectDominanceSpec()), "$n1//$n2");
	}
	
	@Test
	public void caseADominanceLingOpRangeExact() {
		ARangeSpec spec = b.newRangeSpec("2");
		String id = String.valueOf(spec.hashCode());

		testBinaryLingOp(b.newDominanceLingOp(b.newRangeDominanceSpec(spec)), 
				"( $v1/$v2_1_" + id + "/$v2_2_" + id + "/$v2 )");
	}
	
	@Test
	public void caseADominanceLingOpRangeInterval() {
		ARangeSpec spec = b.newRangeSpec("1", "3");
		String id = String.valueOf(spec.hashCode());

		testBinaryLingOp(b.newDominanceLingOp(b.newRangeDominanceSpec(spec)), 
				"( $v1/$v1_1_" + id + "/$v2 " +
				"| $v1/$v2_1_" + id + "/$v2_2_" + id + "/$v2 " +
				"| $v1/$v3_1_" + id + "/$v3_2_" + id + "/$v3_3_" + id + "/$v2 )");
	}
	
	@Test
	public void caseADominanceLingOpLeftLeaf() {
		testBinaryLingOp(b.newDominanceLingOp(
				b.newLeftLeafDominanceSpec()), "$v1/$v2[count(preceding-sibling::*) = 0]");
	}
	
	@Test
	public void caseADominanceLingOpRightLeaf() {
		testBinaryLingOp(b.newDominanceLingOp(
				b.newRightLeafDominanceSpec()), "$v1/$v2[count(following-sibling::*) = 0]");
	}
	
	@Test(expected=RuntimeException.class)
	public void caseADominanceLingOpUnknownSpec() {
		testBinaryLingOp(b.newDominanceLingOp((PDominanceSpec) null), "");
	}
	
	@Test
	public void caseASiblingLingOp() {
		testBinaryLingOp(b.newSiblingLingOp(), "$n1/sibling::$n2");
	}
	
	@Test
	public void caseASiblingAndPrecedenceLingOp() {
		testBinaryLingOp(b.newSiblingAndPrecedenceLingOp(), 
				"( $v1/sibling::$v2 & $v1/following::$v2 )");
	}
	
	@Ignore
	public void caseASameAnnotationGroupLingOp() {
		fail("not implemented");
	}
	
	@Ignore
	public void caseARootLingOp() {
		fail("not implemented");
	}
	
	@Test
	public void caseAArityLingOpExact() {
		testUnaryLingOp(b.newArityLingOp(b.newRangeSpec("2")), "$v3[count(*) = 2]");
	}

	@Test
	public void caseAArityLingOpInterval() {
		testUnaryLingOp(b.newArityLingOp(b.newRangeSpec("2", "5")), 
				"( $v3[count(*) >= 2] & $v3[count(*) <= 5] )");
	}
	
	@Ignore
	public void caseATokenArityLingOp() {
		fail("not implemented");
	}
	
	private void testGenericPExpr(PExpr expr, String expected) {
		Start start = b.newStart(expr);
		translator.caseStart(start);
		assertEquals("wrong mapping", expected, translator.getDddQuery());
	}

//	@Test
//	public void caseANotExpr() {
//		testGenericPExpr(b.newNotExpr(expr1), "! ( $n1/$n2 )");
//	}
//	
//	@Test
//	public void caseAAndExpr() {
//		testGenericPExpr(b.newAndExpr(expr1, expr2), "( $n1/$n2 ) & ( $n3/$n4 )");
//	}
//
//	@Test
//	public void caseAOrExpr() {
//		testGenericPExpr(b.newOrExpr(expr1, expr2), "( $n1/$n2 ) | ( $n3/$n4 )");
//	}
//	
//	@Test
//	public void caseAXorExpr() {
//		testGenericPExpr(b.newXorExpr(expr1, expr2), "( $n1/$n2 ) ^ ( $n3/$n4 )");
//	}
	
	@Test
	public void caseAImplictAndExpr() {
		List<PExpr> exprs = new ArrayList<PExpr>();
		exprs.add(expr1);
		exprs.add(expr2);
		exprs.add(expr3);
		
		testGenericPExpr(b.newImplicitAndExpr(exprs), "( $n1/$n2 ) & ( $n3/$n4 ) & ( $n5/$n6 )");
	}
	
	@Test
	public void foo() {
		String input = "foo & bar & #1 ->aa[biz] #2";
		System.out.println(new AnnisQlTranslator().translate(input));
	}
}
