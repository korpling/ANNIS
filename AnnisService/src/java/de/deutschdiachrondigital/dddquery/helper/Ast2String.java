package de.deutschdiachrondigital.dddquery.helper;

import java.util.LinkedList;

import de.deutschdiachrondigital.dddquery.analysis.DepthFirstAdapter;
import de.deutschdiachrondigital.dddquery.node.AAbsolutePathType;
import de.deutschdiachrondigital.dddquery.node.AAlignedAxis;
import de.deutschdiachrondigital.dddquery.node.AAlignmentAxis;
import de.deutschdiachrondigital.dddquery.node.AAncestorAxis;
import de.deutschdiachrondigital.dddquery.node.AAndExpr;
import de.deutschdiachrondigital.dddquery.node.AAttributeAxis;
import de.deutschdiachrondigital.dddquery.node.AAttributeNodeTest;
import de.deutschdiachrondigital.dddquery.node.AChildAxis;
import de.deutschdiachrondigital.dddquery.node.AComparisonExpr;
import de.deutschdiachrondigital.dddquery.node.AContainedAxis;
import de.deutschdiachrondigital.dddquery.node.AContainedElementAxis;
import de.deutschdiachrondigital.dddquery.node.AContainingAxis;
import de.deutschdiachrondigital.dddquery.node.AContainingElementAxis;
import de.deutschdiachrondigital.dddquery.node.ADescendantAxis;
import de.deutschdiachrondigital.dddquery.node.AElementNodeTest;
import de.deutschdiachrondigital.dddquery.node.AElementSpanAxis;
import de.deutschdiachrondigital.dddquery.node.AEqComparison;
import de.deutschdiachrondigital.dddquery.node.AFollowingAxis;
import de.deutschdiachrondigital.dddquery.node.AFollowingSiblingAxis;
import de.deutschdiachrondigital.dddquery.node.AGeComparison;
import de.deutschdiachrondigital.dddquery.node.AGtComparison;
import de.deutschdiachrondigital.dddquery.node.AImmediatelyFollowingAxis;
import de.deutschdiachrondigital.dddquery.node.AImmediatelyFollowingSiblingAxis;
import de.deutschdiachrondigital.dddquery.node.AImmediatelyPrecedingAxis;
import de.deutschdiachrondigital.dddquery.node.AImmediatelyPrecedingSiblingAxis;
import de.deutschdiachrondigital.dddquery.node.ALayerAxis;
import de.deutschdiachrondigital.dddquery.node.ALeComparison;
import de.deutschdiachrondigital.dddquery.node.ALtComparison;
import de.deutschdiachrondigital.dddquery.node.AMarkerSpec;
import de.deutschdiachrondigital.dddquery.node.AMatchingElementAxis;
import de.deutschdiachrondigital.dddquery.node.ANeComparison;
import de.deutschdiachrondigital.dddquery.node.ANumberLiteralExpr;
import de.deutschdiachrondigital.dddquery.node.AOrExpr;
import de.deutschdiachrondigital.dddquery.node.AOverlappingAxis;
import de.deutschdiachrondigital.dddquery.node.AOverlappingFollowingAxis;
import de.deutschdiachrondigital.dddquery.node.AOverlappingPrecedingAxis;
import de.deutschdiachrondigital.dddquery.node.AParentAxis;
import de.deutschdiachrondigital.dddquery.node.APathExpr;
import de.deutschdiachrondigital.dddquery.node.APrecedingAxis;
import de.deutschdiachrondigital.dddquery.node.APrecedingSiblingAxis;
import de.deutschdiachrondigital.dddquery.node.APrefixAxis;
import de.deutschdiachrondigital.dddquery.node.ARelativePathType;
import de.deutschdiachrondigital.dddquery.node.ASiblingAxis;
import de.deutschdiachrondigital.dddquery.node.AStep;
import de.deutschdiachrondigital.dddquery.node.AStringLiteralExpr;
import de.deutschdiachrondigital.dddquery.node.ASuffixAxis;
import de.deutschdiachrondigital.dddquery.node.AWholeTextAxis;
import de.deutschdiachrondigital.dddquery.node.Node;
import de.deutschdiachrondigital.dddquery.node.PExpr;

public class Ast2String extends DepthFirstAdapter {

	StringBuffer sb;
	boolean first;
	boolean inPred = false;
	
	public Ast2String() {
		sb = new StringBuffer();
	}

	@Override
	public void inAPathExpr(APathExpr node) {
		first = true;
		if (node.getPathType() instanceof ARelativePathType && node.getStep().isEmpty())
			sb.append(".");
	}
	
	@Override
	public void inAAbsolutePathType(AAbsolutePathType node) {
		sb.append("/");
	}
	
	@Override
	public void caseAStep(AStep node) {
		if (node.getAxis() != null)
			node.getAxis().apply(this);
		node.getNodeTest().apply(this);
		if (node.getMarkerSpec() != null)
			node.getMarkerSpec().apply(this);
		
		if ( ! node.getPredicates().isEmpty() ) {
			inPred = true;
			for (PExpr predicate : node.getPredicates()) {
				sb.append("[");
				predicate.apply(this);
				sb.append("]");
			}
			inPred = false;
		}
		
		if (node.getVariable() != null) {
			sb.append("$" + node.getVariable().getText());
		}
	}
	
	@Override
	public void inAMarkerSpec(AMarkerSpec node) {
		sb.append("#"); 
		if (node.getMarker() != null)
			sb.append("(" + node.getMarker().getText() + ")");
	}
	
	@Override
	public void inAChildAxis(AChildAxis node) {
		if ( ! first )
			sb.append("/");
		first = false;
	}
	
	@Override
	public void inAAncestorAxis(AAncestorAxis node) {
		sb.append("\\\\");
	}
	
	@Override
	public void inAAttributeAxis(AAttributeAxis node) {
		if (inPred)
			sb.append("@");
		else
			sb.append("/@");
	}
	
	@Override
	public void inAContainedAxis(AContainedAxis node) {
		sb.append(" ~> ");
	}
	
	@Override
	public void inAAlignedAxis(AAlignedAxis node) {
		sb.append("aligned::");
	}
	
	@Override
	public void inAAlignmentAxis(AAlignmentAxis node) {
		sb.append("alignement::");
	}
	
	@Override
	public void inAContainedElementAxis(AContainedElementAxis node) {
		sb.append("contained-element");
	}
	
	@Override
	public void inAContainingAxis(AContainingAxis node) {
		sb.append(" ~< ");
	}
	
	@Override
	public void inAContainingElementAxis(AContainingElementAxis node) {
		sb.append("containing-element::");
	}
	
	@Override
	public void inADescendantAxis(ADescendantAxis node) {
		if ( ! first )
			sb.append("//");
		else
			sb.append("/");
		first = false;
	}
	
	@Override
	public void inAElementSpanAxis(AElementSpanAxis node) {
		sb.append("element-span::");
	}
	
	@Override
	public void inAFollowingAxis(AFollowingAxis node) {
		sb.append(" --> ");
	}
	
	@Override
	public void inAFollowingSiblingAxis(AFollowingSiblingAxis node) {
		sb.append(" -->^ ");
	}
	
	@Override
	public void inAImmediatelyFollowingAxis(AImmediatelyFollowingAxis node) {
		sb.append(" -> ");
	}
	
	@Override
	public void inAImmediatelyFollowingSiblingAxis(AImmediatelyFollowingSiblingAxis node) {
		sb.append(" ->^ ");
	}
	
	@Override
	public void inAImmediatelyPrecedingAxis(AImmediatelyPrecedingAxis node) {
		sb.append(" <- ");
	}
	
	@Override
	public void inAImmediatelyPrecedingSiblingAxis(AImmediatelyPrecedingSiblingAxis node) {
		sb.append(" <-^ ");
	}
	
	@Override
	public void inALayerAxis(ALayerAxis node) {
		sb.append("layer::");
	}
	
	@Override
	public void inAMatchingElementAxis(AMatchingElementAxis node) {
		sb.append("matching-element::");
	}
	
	@Override
	public void inAOverlappingAxis(AOverlappingAxis node) {
		sb.append("overlapping::");
	}
	
	@Override
	public void inAOverlappingFollowingAxis(AOverlappingFollowingAxis node) {
		sb.append("overlapping-following::");
	}
	
	@Override
	public void inAOverlappingPrecedingAxis(AOverlappingPrecedingAxis node) {
		sb.append("overlapping-preceding");
	}
	
	@Override
	public void inAParentAxis(AParentAxis node) {
		sb.append("\\");
	}
	
	@Override
	public void inAPrecedingAxis(APrecedingAxis node) {
		sb.append(" <-- ");
	}
	
	@Override
	public void inAPrecedingSiblingAxis(APrecedingSiblingAxis node) {
		sb.append(" <--^ ");
	}
	
	@Override
	public void inAPrefixAxis(APrefixAxis node) {
		sb.append("prefix::");
	}
	
	@Override
	public void inASiblingAxis(ASiblingAxis node) {
		sb.append(" ^ ");
	}
	
	@Override
	public void inASuffixAxis(ASuffixAxis node) {
		sb.append("suffix::");
	}
	
	@Override
	public void inAWholeTextAxis(AWholeTextAxis node) {
		sb.append("whole-text::");
	}
	
	@Override
	public void outAElementNodeTest(AElementNodeTest node) {
		if (node.getName() != null) {
			sb.append(node.getName().getText());
		}
	}
	
	@Override
	public void outAAttributeNodeTest(AAttributeNodeTest node) {
		if (node.getName() != null) {
			sb.append(node.getName().getText());
		}
	}
	
	@Override
	public void caseAAndExpr(AAndExpr node) {
		andor("&", node.getExpr());
	}

	@Override
	public void caseAOrExpr(AOrExpr node) {
		andor("|", node.getExpr());
	}
	
	private void andor(String op, LinkedList<PExpr> exprs) {
		op = " " + op + " ";
		sb.append("( ");
		for (PExpr expr : exprs) {
			expr.apply(this);
			sb.append(op);
		}
		sb.setLength(sb.length() - op.length());
		sb.append(" )");
	}
	
	@Override
	public void caseAComparisonExpr(AComparisonExpr node) {
		node.getLhs().apply(this);
		node.getComparison().apply(this);
		node.getRhs().apply(this);
	}
	
	@Override
	public void inAEqComparison(AEqComparison node) {
		sb.append(" = ");
	}
	
	@Override
	public void inANeComparison(ANeComparison node) {
		sb.append(" != ");
	}
	
	@Override
	public void inALtComparison(ALtComparison node) {
		sb.append(" < ");
	}
	
	@Override
	public void inALeComparison(ALeComparison node) {
		sb.append(" <= ");
	}
	
	@Override
	public void inAGtComparison(AGtComparison node) {
		sb.append(" > ");
	}
		
	@Override
	public void inAGeComparison(AGeComparison node) {
		sb.append(" >= ");
	}
	
	@Override
	public void inANumberLiteralExpr(ANumberLiteralExpr node) {
		sb.append(node.getNumber().getText());
	}
	
	@Override
	public void inAStringLiteralExpr(AStringLiteralExpr node) {
		sb.append("'" + node.getString().getText() + "'");
	}
	
	public String getResult() {
		String string = sb.toString();
		if (string.startsWith("( ") && string.endsWith(" )"))
			string = string.substring(2, string.length() - 2);
		return string;
	}
	
	public String toString(Node node) {
		Ast2String instance = new Ast2String();
		node.apply(instance);
		return instance.getResult();
	}
}
