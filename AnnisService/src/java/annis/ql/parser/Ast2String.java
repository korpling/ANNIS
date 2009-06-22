package annis.ql.parser;

import java.util.LinkedList;

import annis.ql.analysis.DepthFirstAdapter;
import annis.ql.node.AAndExpr;
import annis.ql.node.AAnnotationSearchExpr;
import annis.ql.node.AArityLingOp;
import annis.ql.node.ADominanceLingOp;
import annis.ql.node.AEdgeAnnotation;
import annis.ql.node.AEdgeDominanceSpec;
import annis.ql.node.AEdgeSpec;
import annis.ql.node.AExactOverlapLingOp;
import annis.ql.node.AGroupedExpr;
import annis.ql.node.AInclusionLingOp;
import annis.ql.node.AIndirectDominanceSpec;
import annis.ql.node.AIndirectPrecedenceSpec;
import annis.ql.node.ALeftAlignLingOp;
import annis.ql.node.ALeftLeafDominanceSpec;
import annis.ql.node.ALeftOverlapLingOp;
import annis.ql.node.ALinguisticConstraintExpr;
import annis.ql.node.AOrExpr;
import annis.ql.node.APrecedenceLingOp;
import annis.ql.node.ARangeSpec;
import annis.ql.node.ARegexpTextSpec;
import annis.ql.node.ARightAlignLingOp;
import annis.ql.node.ARightLeafDominanceSpec;
import annis.ql.node.ARootLingOp;
import annis.ql.node.ASiblingAndPrecedenceLingOp;
import annis.ql.node.ASiblingLingOp;
import annis.ql.node.ATokenArityLingOp;
import annis.ql.node.AWildTextSpec;
import annis.ql.node.Node;
import annis.ql.node.PTextSpec;
import annis.ql.node.TDigits;
import annis.ql.node.TId;
import annis.ql.node.TRegexp;
import annis.ql.node.TText;


public class Ast2String extends DepthFirstAdapter {

	StringBuffer sb;
	
	public Ast2String() {
		sb = new StringBuffer();
	}
	
	@Override
	public void caseAAndExpr(AAndExpr node) {
		expressionList(node.getExpr(), " & ");
	}

	private void expressionList(LinkedList<? extends Node> expressions, String separator) {
		for (Node expr : expressions) {
			expr.apply(this);
			sb.append(separator);
		}
		sb.setLength(sb.length() - separator.length());
	}
	
	@Override
	public void caseAAnnotationSearchExpr(AAnnotationSearchExpr node) {
		annotation(node.getAnnoNamespace(), node.getAnnoType(), node.getAnnoValue());
	}

	private void annotation(TId namespace, TId type, PTextSpec value) {
		if (namespace != null) {
			sb.append(token(namespace));
			sb.append(":");
		}
		sb.append(token(type));
		if (value != null) {
			sb.append("=");
			value.apply(this);
		}
	}
	
	@Override
	public void caseAArityLingOp(AArityLingOp node) {
		sb.append(":arity=");
		node.getRangeSpec().apply(this);
	}
	
	@Override
	public void caseADominanceLingOp(ADominanceLingOp node) {
		sb.append(">");
		node.getDominanceSpec().apply(this);
	}
	
	@Override
	public void caseAEdgeAnnotation(AEdgeAnnotation node) {
		annotation(node.getNamespace(), node.getType(), node.getValue());
	}
	
	@Override
	public void caseAEdgeDominanceSpec(AEdgeDominanceSpec node) {
		sb.append("[");
		expressionList(((AEdgeSpec) node.getEdgeSpec()).getEdgeAnnotation(), " ");
		sb.append("]");
	}
	
	@Override
	public void caseAExactOverlapLingOp(AExactOverlapLingOp node) {
		sb.append("_=_");
	}
	
	@Override
	public void caseAGroupedExpr(AGroupedExpr node) {
		sb.append("( ");
		node.getExpr().apply(this);
		sb.append(" )");
	}
	
	@Override
	public void caseAInclusionLingOp(AInclusionLingOp node) {
		sb.append("_i_");
	}
	
	@Override
	public void caseAIndirectDominanceSpec(AIndirectDominanceSpec node) {
		sb.append("*");
	}
	
	@Override
	public void caseAIndirectPrecedenceSpec(AIndirectPrecedenceSpec node) {
		sb.append("*");
	}
	
	@Override
	public void caseALeftAlignLingOp(ALeftAlignLingOp node) {
		sb.append("_l_");
	}
	
	@Override
	public void caseALeftLeafDominanceSpec(ALeftLeafDominanceSpec node) {
		sb.append("@l");
	}
	
	@Override
	public void caseALeftOverlapLingOp(ALeftOverlapLingOp node) {
		sb.append("_ol_");
	}
	
	@Override
	public void caseALinguisticConstraintExpr(ALinguisticConstraintExpr node) {
		if (node.getLingOp() == null)
			return;
		sb.append("#");
		sb.append(token(node.getLhs()));
		sb.append(" ");
		node.getLingOp().apply(this);
		if (node.getRhs() != null) {
			sb.append(" ");
			sb.append("#");
			sb.append(token(node.getRhs()));
		}
	}
	
	@Override
	public void caseAOrExpr(AOrExpr node) {
		expressionList(node.getExpr(), " | ");
	}
	
	@Override
	public void caseAPrecedenceLingOp(APrecedenceLingOp node) {
		sb.append(".");
	}
	
	@Override
	public void caseARangeSpec(ARangeSpec node) {
		sb.append(token(node.getMin()));
		if (node.getMax() != null)
			sb.append(token(node.getMax()));
	}
	
	@Override
	public void caseARegexpTextSpec(ARegexpTextSpec node) {
		sb.append("/");
		sb.append(token(node.getRegexp()));
		sb.append("/");
	}
	
	@Override
	public void caseARightAlignLingOp(ARightAlignLingOp node) {
		sb.append("_r_");
	}
	
	@Override
	public void caseARightLeafDominanceSpec(ARightLeafDominanceSpec node) {
		sb.append("@r");
	}
	
	@Override
	public void caseARootLingOp(ARootLingOp node) {
		sb.append(":root");
	}
	
	@Override
	public void caseASiblingAndPrecedenceLingOp(ASiblingAndPrecedenceLingOp node) {
		sb.append("$.*");
	}
	
	@Override
	public void caseASiblingLingOp(ASiblingLingOp node) {
		sb.append("$");
	}
	
	@Override
	public void caseATokenArityLingOp(ATokenArityLingOp node) {
		sb.append(":tokenarity=");
	}
	
	@Override
	public void caseAWildTextSpec(AWildTextSpec node) {
		sb.append("\"");
		sb.append(token(node.getText()));
		sb.append("\"");
	}
	
	private String token(TText text) {
		return text.getText();
	}
	
	private String token(TId token) {
		return token.getText();
	}
	
	private String token(TDigits digits) {
		return digits != null ? digits.getText() : null;
	}
	
	private String token(TRegexp regexp) {
		return regexp.getText();
	}

	public String getResult() {
		String string = sb.toString();
//		if (string.startsWith("( ") && string.endsWith(" )"))
//			string = string.substring(2, string.length() - 2);
		return string;
	}
	
	public String toString(Node node) {
//		Ast2String instance = new Ast2String();
//		node.apply(instance);
//		return instance.getResult();
		return "Ast2String not implemented";
	}
	
}
