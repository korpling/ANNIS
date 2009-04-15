package de.deutschdiachrondigital.dddquery.helper;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import annisservice.exceptions.AnnisQLSemanticsException;
import annisservice.exceptions.AnnisQLSyntaxException;
import annisservice.exceptions.AnnisServiceException;
import ex.annis.ql.analysis.AnalysisAdapter;
import ex.annis.ql.analysis.DnfNormalizer;
import ex.annis.ql.analysis.SearchExpressionCounter;
import ex.annis.ql.analysis.SemanticAnalysisImpl;
import ex.annis.ql.lexer.Lexer;
import ex.annis.ql.lexer.LexerException;
import ex.annis.ql.node.AAndExpr;
import ex.annis.ql.node.AAnnotationSearchExpr;
import ex.annis.ql.node.AAnyNodeSearchExpr;
import ex.annis.ql.node.AArityLingOp;
import ex.annis.ql.node.ADirectDominanceSpec;
import ex.annis.ql.node.ADirectPointingRelationSpec;
import ex.annis.ql.node.ADirectPrecedenceSpec;
import ex.annis.ql.node.ADominanceLingOp;
import ex.annis.ql.node.AEdgeAnnotation;
import ex.annis.ql.node.AEdgeDominanceSpec;
import ex.annis.ql.node.AEdgeSpec;
import ex.annis.ql.node.AExactOverlapLingOp;
import ex.annis.ql.node.AImplicitAndExpr;
import ex.annis.ql.node.AInclusionLingOp;
import ex.annis.ql.node.AIndirectDominanceSpec;
import ex.annis.ql.node.AIndirectPointingRelationSpec;
import ex.annis.ql.node.AIndirectPrecedenceSpec;
import ex.annis.ql.node.ALeftAlignLingOp;
import ex.annis.ql.node.ALeftLeafDominanceSpec;
import ex.annis.ql.node.ALeftOverlapLingOp;
import ex.annis.ql.node.ALinguisticConstraintExpr;
import ex.annis.ql.node.AOrExpr;
import ex.annis.ql.node.APointingRelationLingOp;
import ex.annis.ql.node.APrecedenceLingOp;
import ex.annis.ql.node.ARangeDominanceSpec;
import ex.annis.ql.node.ARangePrecedenceSpec;
import ex.annis.ql.node.ARangeSpec;
import ex.annis.ql.node.ARegexpTextSpec;
import ex.annis.ql.node.ARightAlignLingOp;
import ex.annis.ql.node.ARightLeafDominanceSpec;
import ex.annis.ql.node.ARootLingOp;
import ex.annis.ql.node.ASameAnnotationGroupLingOp;
import ex.annis.ql.node.ASiblingAndPrecedenceLingOp;
import ex.annis.ql.node.ASiblingLingOp;
import ex.annis.ql.node.ATextSearchExpr;
import ex.annis.ql.node.ATokenArityLingOp;
import ex.annis.ql.node.AWildTextSpec;
import ex.annis.ql.node.PEdgeAnnotation;
import ex.annis.ql.node.PExpr;
import ex.annis.ql.node.PLingOp;
import ex.annis.ql.node.PTextSpec;
import ex.annis.ql.node.Start;
import ex.annis.ql.node.Token;
import ex.annis.ql.parser.NodeSearchNormalizer;
import ex.annis.ql.parser.Parser;
import ex.annis.ql.parser.ParserException;
import ex.annis.ql.parser.TokenSearchNormalizer;

public class AnnisQlTranslator extends AnalysisAdapter {

	private Logger log = Logger.getLogger(this.getClass());
	
	StringBuffer dddQuery;
	SemanticAnalysisImpl analysis;
	
	String error;
	
	@Override
	public void caseARootLingOp(ARootLingOp node) {
		error = "can't map root lingop";
	}
	
	@Override
	public void caseATokenArityLingOp(ATokenArityLingOp node) {
		error = "can't map tokenarity lingop";
	}
	
	@Override
	public void caseASameAnnotationGroupLingOp(ASameAnnotationGroupLingOp node) {
		error = "can't map @ lingop";
	}
	
	public AnnisQlTranslator(SemanticAnalysisImpl analysis) {
		this.analysis = analysis;
		this.dddQuery = new StringBuffer();
	}

	public String getDddQuery() {
		return error != null ? error : dddQuery.toString();
	}
	
	@Override
	public void caseStart(Start node) {
		error = null;
		node.getPExpr().apply(this);
	}
	
	// tested
	@Override
	public void caseATextSearchExpr(ATextSearchExpr node) {
		writeMapping("STRUCT", "#(n", pos(node), ")");
		
		if (node.getTextSpec() != null) { 
			writeMapping("[. = ");
			node.getTextSpec().apply(this);
			writeMapping("]");
		} else {
			writeMapping("[isToken()]");
		}
		
		writeMapping("$n", pos(node));
	}
	
	@Override
	public void caseAAnyNodeSearchExpr(AAnyNodeSearchExpr node) {
		writeMapping("STRUCT", "#(n", pos(node), ")", "$n", pos(node));
	}
	
	// tested
	@Override
	public void caseAWildTextSpec(AWildTextSpec node) {
		writeMapping("\"", token(node.getText()), "\"");
	}
	
	// tested
	@Override
	public void caseARegexpTextSpec(ARegexpTextSpec node) {
		writeMapping("r\"", token(node.getRegexp()), "\"");
	}
	
	// FIXME: Wert als Regexp
	@Override
	public void caseAAnnotationSearchExpr(AAnnotationSearchExpr node) {
		writeMapping("STRUCT#(n", pos(node), ")", "[@");
		
		if (node.getAnnoNamespace() != null)
			writeMapping(token(node.getAnnoNamespace()), ":");
		
		writeMapping(token(node.getAnnoType()));

		PTextSpec annoValue = node.getAnnoValue();
		if (annoValue != null) {
			writeMapping(" = ");
			annoValue.apply(this);
		}
		
		writeMapping("]$n", pos(node));
	}

	private String pos(PExpr expr) {
		return String.valueOf(analysis.getPosition(expr));
	}
	
	@Override
	public void caseALinguisticConstraintExpr(ALinguisticConstraintExpr node) {
		node.getLingOp().apply(this);
	}
	
	@Override
	public void caseAExactOverlapLingOp(AExactOverlapLingOp node) {
		writeMapping("$n", lhs(node), "/matching-element::$n", rhs(node));
	}
	
	@Override
	public void caseALeftAlignLingOp(ALeftAlignLingOp node) {
		writeMapping("$n", lhs(node), "/left-align::$n", rhs(node));
	}
	
	@Override
	public void caseARightAlignLingOp(ARightAlignLingOp node) {
		writeMapping("$n", lhs(node), "/right-align::$n", rhs(node));
	}

	@Override
	public void caseAInclusionLingOp(AInclusionLingOp node) {
		writeMapping("$n", lhs(node), "/containing::$n", rhs(node));
	}
	
	@Override
	public void caseALeftOverlapLingOp(ALeftOverlapLingOp node) {
		writeMapping("$n", lhs(node), "/overlapping-following::$n", rhs(node));
	}
	
	@Override
	public void caseAPrecedenceLingOp(APrecedenceLingOp node) {
		try {
			node.getPrecedenceSpec().apply(this);
		} catch (NullPointerException e) {
			throw new RuntimeException("BUG: unknown precedence spec", e);
		}
	}
	
	// tested
	@Override
	public void caseADirectPrecedenceSpec(ADirectPrecedenceSpec node) {
		PLingOp lingOp = (PLingOp) node.parent();
		writeMapping("$n", lhs(lingOp), " -> $n", rhs(lingOp));
	}
	
	// tested
	@Override
	public void caseAIndirectPrecedenceSpec(AIndirectPrecedenceSpec node) {
		PLingOp lingOp = (PLingOp) node.parent();
		writeMapping("$n", lhs(lingOp), " --> $n", rhs(lingOp));
	}
	
	@Override
	public void caseARangePrecedenceSpec(ARangePrecedenceSpec node) {
		APrecedenceLingOp lingOp = (APrecedenceLingOp) node.parent();
//		writeMapping(precedenceRangeMapping(lingOp, node));

		writeRangedAxis("following", (ARangeSpec) node.getRangeSpec(), lhs(lingOp), rhs(lingOp));
	}

	private void writeRangedAxis(String axis, ARangeSpec rangeSpec, String lhs,
			String rhs) {
		String range = String.valueOf(token(rangeSpec.getMin()));
		if (rangeSpec.getMax() != null)
			range += "," + String.valueOf(token(rangeSpec.getMax()));
		
		writeMapping("$n", lhs, "/" + axis + "(", range, ")::$n", rhs);
	}

	private String[] precedenceRangeMapping(APrecedenceLingOp node, ARangePrecedenceSpec spec) {
		List<String> m = new ArrayList<String>();
		
		ARangeSpec range = (ARangeSpec) spec.getRangeSpec();
		int min = Integer.parseInt(token(range.getMin()));
		int max = range.getMax() != null ? Integer.parseInt(token(range.getMax())) : 0;
		
		// unique id for this ling op
		String id = String.valueOf(spec.getRangeSpec().hashCode());

		// encapsulate entire expression
		m.add("( ");
		
		// always has one alternative
		buildPrecedenceRangeMappingSubExpression(m, min, id, lhs(node), rhs(node));
		
		// if it's an interval from n to m (inclusive), build next m - n alternatives
		if (max > min) {
			for (int i = min + 1; i <= max; ++i) {
				// next alternative
				m.add(" | ");
				buildPrecedenceRangeMappingSubExpression(m, i, id, lhs(node), rhs(node));
			}
		}
			
		// close entire expression
		m.add(" )");
		
		return m.toArray(new String[] { });
	}
	
	private void buildPrecedenceRangeMappingSubExpression(List<String> mapping, int step, String id, String lhs, String rhs) {
		// right side of subexpression
		for (String part : new String[] { "( $v", lhs, "/immediately-following::" } )
			mapping.add(part);
		
		// middle
		for (int i = 1; i <= step; ++i) {
			String i_str = String.valueOf(i);
			String step_str = String.valueOf(step);
			String[] middle = {
					"$v", step_str, "_", i_str, "_", id, " & $v", step_str, "_", i_str, "_", id, "/immediately-following::"
			};
			for (String part : middle) 
				mapping.add(part);
		}
		
		// left side
		for (String part : new String[] { "$v", rhs, " )" } )
			mapping.add(part);
	}

	@Override
	public void caseADominanceLingOp(ADominanceLingOp node) {
		try {
			node.getDominanceSpec().apply(this);
		} catch (NullPointerException e) {
			throw new RuntimeException("BUG: unknown dominance spec");
		}
	}
	
	@Override
	public void caseAPointingRelationLingOp(APointingRelationLingOp node) {
		try {
			node.getPointingRelationSpec().apply(this);
		} catch (NullPointerException e) {
			throw new RuntimeException("BUG: unknown pointing relation spec");
		}
	}
	
	@Override
	public void caseADirectPointingRelationSpec(ADirectPointingRelationSpec node) {
		PLingOp lingOp = (PLingOp) node.parent();
		String name = token(node.getName());
		if (node.getEdgeSpec() != null) {
			writeMapping("$n", lhs(lingOp), "/child[p,", name, "](");
			node.getEdgeSpec().apply(this);
			writeMapping(")::$n", rhs(lingOp));
		} else
			writeMapping("$n", lhs(lingOp), "/child[p,", name, "]::$n", rhs(lingOp));
	}
	
	@Override
	public void caseAIndirectPointingRelationSpec(AIndirectPointingRelationSpec node) {
		PLingOp lingOp = (PLingOp) node.parent();
		String name = token(node.getName());
		writeMapping("$n", lhs(lingOp), "/descendant[p,", name, "]::$n", rhs(lingOp));
	}

	@Override
	public void caseADirectDominanceSpec(ADirectDominanceSpec node) {
		PLingOp lingOp = (PLingOp) node.parent();
		writeMapping("$n", lhs(lingOp), "/child[d]::$n", rhs(lingOp));
	}
	
	@Override
	public void caseAIndirectDominanceSpec(AIndirectDominanceSpec node) {
		PLingOp lingOp = (PLingOp) node.parent();
		writeMapping("$n", lhs(lingOp), "/descendant[d]::$n", rhs(lingOp));
	}
	
	@Override
	public void caseAEdgeDominanceSpec(AEdgeDominanceSpec node) {
		PLingOp lingOp = (PLingOp) node.parent();
		writeMapping("$n", lhs(lingOp), "/child[d](");
		node.getEdgeSpec().apply(this);
		writeMapping(")::$n", rhs(lingOp));
	}
	
	@Override
	public void caseAEdgeSpec(AEdgeSpec node) {
		for (PEdgeAnnotation edgeAnnotation : node.getEdgeAnnotation())
			edgeAnnotation.apply(this);
		if (node.getEdgeAnnotation().size() > 0)
			dddQuery.setLength(dddQuery.length() - " ".length());
	}
	
	@Override
	public void caseAEdgeAnnotation(AEdgeAnnotation node) {
		if (node.getNamespace() != null)
			writeMapping(token(node.getNamespace()), ":");
		writeMapping(token(node.getType()));
		if (node.getValue() != null) {
			writeMapping("=");
			node.getValue().apply(this);
		}
		writeMapping(" ");
	}
	
	@Override
	public void caseALeftLeafDominanceSpec(ALeftLeafDominanceSpec node) {
		PLingOp lingOp = (PLingOp) node.parent();
		writeMapping("$n", lhs(lingOp), "/left-child::$n", rhs(lingOp));
	}
	
	@Override
	public void caseARightLeafDominanceSpec(ARightLeafDominanceSpec node) {
		PLingOp lingOp = (PLingOp) node.parent();
		writeMapping("$n", lhs(lingOp), "/right-child::$n", rhs(lingOp));
	}
	
	@Override
	public void caseARangeDominanceSpec(ARangeDominanceSpec node) {
		ADominanceLingOp lingOp = (ADominanceLingOp) node.parent();
//		writeMapping(dominanceRangeMapping(lingOp, node));
		
		writeRangedAxis("descendant[d]", (ARangeSpec) node.getRangeSpec(), lhs(lingOp), rhs(lingOp));
	}
	
	private String[] dominanceRangeMapping(ADominanceLingOp node, ARangeDominanceSpec spec) {
		List<String> m = new ArrayList<String>();
		
		ARangeSpec range = (ARangeSpec) spec.getRangeSpec();
		int min = Integer.parseInt(token(range.getMin()));
		int max = range.getMax() != null ? Integer.parseInt(token(range.getMax())) : 0;
		
		// unique id for this ling op
		String id = String.valueOf(spec.getRangeSpec().hashCode());
		
		// encapsulate entire expression
		m.add("( ");
		
		// always has one alternative
		buildDominanceRangeMappingSubExpression(m, min, id, lhs(node), rhs(node));
		
		// if it's an interval from n to m (inclusive), build next m - n alternatives
		if (max > min) {
			for (int i = min + 1; i <= max; ++i) {
				// next alternative
				m.add(" | ");
				buildDominanceRangeMappingSubExpression(m, i, id, lhs(node), rhs(node));
			}
		}
			
		// close entire expression
		m.add(" )");
		
		return m.toArray(new String[] { });
	}
	
	// XXX: refactor dominance and precedence
	private void buildDominanceRangeMappingSubExpression(List<String> mapping, int step, String id, String lhs, String rhs) {
		// right side of subexpression
		for (String part : new String[] { "$v", lhs } )
			mapping.add(part);
		
		// middle
		for (int i = 1; i <= step; ++i) {
			String i_str = String.valueOf(i);
			String step_str = String.valueOf(step);
			String[] middle = {
					"/$v", step_str, "_", i_str, "_", id
			};
			for (String part : middle) 
				mapping.add(part);
		}
		
		// left side
		for (String part : new String[] { "/$v", rhs } )
			mapping.add(part);
	}
	
	@Override
	public void caseASiblingLingOp(ASiblingLingOp node) {
		writeMapping("$n", lhs(node), "/sibling::$n", rhs(node));
	}
	
	@Override
	public void caseASiblingAndPrecedenceLingOp(ASiblingAndPrecedenceLingOp node) {
		writeMapping("( $v", lhs(node), "/sibling::$v", rhs(node), " & $v", lhs(node), "/following::$v", rhs(node), " )");
	}
	
	@Override
	public void caseAArityLingOp(AArityLingOp node) {
		
		ARangeSpec range = (ARangeSpec) node.getRangeSpec();
		String min = token(range.getMin());
		String max = token(range.getMax());
		
		if (max == null)
			writeMapping("$v", lhs(node), "[count(*) = ", min, "]"); 
		else
			writeMapping("( $v", lhs(node), "[count(*) >= ", min, "] & $v", lhs(node), "[count(*) <= ", max, "] )"); 
	}
	
	@Override
	public void caseAOrExpr(AOrExpr node) {
		writeMapping("( ");
		for (PExpr expr : node.getExpr()) {
			expr.apply(this);
			writeMapping(" | ");
		}
		dddQuery.setLength(dddQuery.length() - " & ".length());
		writeMapping(" )");
	}
	
	@Override
	public void caseAAndExpr(AAndExpr node) {
		writeMapping("( ");
		for (PExpr expr : node.getExpr()) {
			expr.apply(this);
			writeMapping(" & ");
		}
		dddQuery.setLength(dddQuery.length() - " & ".length());
		writeMapping(" )");
	}
	
	@Override
	public void caseAImplicitAndExpr(AImplicitAndExpr node) {
		Iterator<PExpr> it = node.getExpr().iterator();

		// write first factor
		writeMapping("( ");
		it.next().apply(this);
		
		// write next alternatives
		while (it.hasNext()) {
			writeMapping(" ) & ( ");
			it.next().apply(this);
		}
		
		// write end
		writeMapping(" )");
	}
	
	private void writeBinaryBooleanMapping(String op, PExpr lhs, PExpr rhs) {
		String[] before = { "( " };
		String[] middle = { " ) ", op , " ( " };
		String[] after = { " )" };
		
		writeMapping(before);
		lhs.apply(this);
		writeMapping(middle);
		rhs.apply(this);
		writeMapping(after);
	}
	
	private void writeMapping(String... mapping) {
		for (String part : mapping)
			dddQuery.append(part);
	}
	
	private String token(Token token) {
		return token != null ? token.getText().trim() : null;
	}
	
	private String lhs(PLingOp node) {
		return token(((ALinguisticConstraintExpr) node.parent()).getLhs());
	}
	
	private String rhs(PLingOp node) {
		return token(((ALinguisticConstraintExpr) node.parent()).getRhs());
	}
	
	public AnnisQlTranslator() {
		// TODO Auto-generated constructor stub
	}
	
	// FIXME: test, auslagern
	public String translate(String input) throws AnnisQLSemanticsException {

		log.debug("translating: " + input);
		
		try {
			Parser parser = new Parser(new Lexer(new PushbackReader(new StringReader(input), 3000)));
			Start start = parser.parse();

			start.apply(new TokenSearchNormalizer());
			start.apply(new NodeSearchNormalizer());
			
			SemanticAnalysisImpl analysis = new SemanticAnalysisImpl();
			analysis.setDnfNormalizer(new DnfNormalizer());
			analysis.setExpressionCounter(new SearchExpressionCounter());
			start.apply(analysis);
			
			if (analysis.isValid()) {

				AnnisQlTranslator mapper = new AnnisQlTranslator(analysis);
				start.apply(mapper);

				return mapper.getDddQuery();
			} else {
				log.warn("Bad query: " + analysis.getError().message);
				throw new AnnisQLSemanticsException(analysis.getError().message);
			}
		} catch (ParserException e) {
			log.warn("Syntax error: " + e.getMessage());
			throw new AnnisQLSyntaxException("Syntax error: " + e.getMessage());
		} catch (LexerException e) {
			log.warn("Lexer error: " + e.getMessage());
			throw new AnnisQLSyntaxException("Couldn't parse the query, please contact the administrator: " + e.getMessage());
		} catch (IOException e) {
			log.warn("BUG", e);
			throw new AnnisServiceException("An unlikely error occured in the back-end, please contact the administrator: " + e.getMessage());
		}
	}

	private void dumpTree(Start start) {
		StringWriter writer = new StringWriter();
		start.apply(new ex.annis.ql.helper.TreeDumper(new PrintWriter(writer)));
		String result = writer.toString();
		System.out.println(result);
	}

}
