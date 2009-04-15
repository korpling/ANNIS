package de.deutschdiachrondigital.dddquery.sql;

import java.util.List;

import de.deutschdiachrondigital.dddquery.node.AAbsolutePathType;
import de.deutschdiachrondigital.dddquery.node.AAncestorAxis;
import de.deutschdiachrondigital.dddquery.node.AAndExpr;
import de.deutschdiachrondigital.dddquery.node.AAttributeAxis;
import de.deutschdiachrondigital.dddquery.node.AAttributeNodeTest;
import de.deutschdiachrondigital.dddquery.node.AChildAxis;
import de.deutschdiachrondigital.dddquery.node.AComparisonExpr;
import de.deutschdiachrondigital.dddquery.node.AContainedAxis;
import de.deutschdiachrondigital.dddquery.node.AContainingAxis;
import de.deutschdiachrondigital.dddquery.node.ADescendantAxis;
import de.deutschdiachrondigital.dddquery.node.AElementNodeTest;
import de.deutschdiachrondigital.dddquery.node.AEqComparison;
import de.deutschdiachrondigital.dddquery.node.AFollowingAxis;
import de.deutschdiachrondigital.dddquery.node.AFollowingSiblingAxis;
import de.deutschdiachrondigital.dddquery.node.AGeComparison;
import de.deutschdiachrondigital.dddquery.node.AGtComparison;
import de.deutschdiachrondigital.dddquery.node.AImmediatelyFollowingAxis;
import de.deutschdiachrondigital.dddquery.node.ALeComparison;
import de.deutschdiachrondigital.dddquery.node.ALeftAlignAxis;
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
import de.deutschdiachrondigital.dddquery.node.ARegexpLiteralExpr;
import de.deutschdiachrondigital.dddquery.node.ARightAlignAxis;
import de.deutschdiachrondigital.dddquery.node.ASiblingAxis;
import de.deutschdiachrondigital.dddquery.node.AStep;
import de.deutschdiachrondigital.dddquery.node.AStringLiteralExpr;
import de.deutschdiachrondigital.dddquery.node.ASuffixAxis;
import de.deutschdiachrondigital.dddquery.node.Node;
import de.deutschdiachrondigital.dddquery.node.PExpr;
import de.deutschdiachrondigital.dddquery.node.PStep;
import de.deutschdiachrondigital.dddquery.node.Token;
import de.deutschdiachrondigital.dddquery.sql.model.AliasSet;
import de.deutschdiachrondigital.dddquery.sql.model.Column;
import de.deutschdiachrondigital.dddquery.sql.model.JoinField;
import de.deutschdiachrondigital.dddquery.sql.model.Literal;
import de.deutschdiachrondigital.dddquery.sql.model.Path;
import de.deutschdiachrondigital.dddquery.sql.model.RegexpLiteral;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.Alternative;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.Conjunction;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.IsNullCondition;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.Join;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.OrCondition;

public class PathTranslatorB extends AbstractPathTranslator {

	private enum TextValue { Element, Attribute };
	private TextValue textValue;
	
	private String comparison;
	
	@Override
	public void inAPathExpr(APathExpr node) {
		if (context == null) {
			target = aliasSetProvider.getAliasSet();
			path.addAliasSet(target);
		}
	}
	
	
	@Override
	public void inAAbsolutePathType(AAbsolutePathType node) {
		path.addCondition(new IsNullCondition(target.getColumn("rank", "parent")));
	}
	
	@Override
	public void caseAStep(AStep node) {
		inAStep(node);
		if (node.getAxis() != null)
			node.getAxis().apply(this);
		if (node.getNodeTest() != null)
			node.getNodeTest().apply(this);
		if (node.getMarkerSpec() != null)
			node.getMarkerSpec().apply(this);
		AliasSet oldTarget = target;
		for (PExpr expr : node.getPredicates()) {
			expr.apply(this);
			target = oldTarget;
		}
	}
	
	@Override
	public void inAStep(AStep node) {
		if (node.getAxis() instanceof AAttributeAxis)
			return;
		
		context = target;
		
		if (node.getVariable() == null) {
			target = aliasSetProvider.getAliasSet();
			path.addAliasSet(target);
		} else {
			String name = token(node.getVariable());
			boolean known = aliasSetProvider.isBound(name);
			target = aliasSetProvider.getAliasSet(name);
			if ( ! known )
				path.addAliasSet(target);
		}
	}
	
	@Override
	public void inAMarkerSpec(AMarkerSpec node) {
		path.markAliasSet(target, token(node.getMarker()));
	}
	
	@Override
	public void inAAttributeAxis(AAttributeAxis node) {
//		path.addCondition(Join.eq(context.getColumn("annotations", "struct"), target.getColumn("annotations", "struct")));
	}

	@Override
	public void inAChildAxis(AChildAxis node) {
		path.addCondition(Join.eq(context.getColumn("rank", "pre"), target.getColumn("rank", "parent")));
	}
	
	@Override
	public void inAParentAxis(AParentAxis node) {
		AliasSet helper = aliasSetProvider.getAliasSet();
		path.addAliasSet(helper);
		// rank.pre = rank.pre (nicht struct.id = struct.id)
		path.addCondition(Join.eq(context.getColumn("rank", "pre"), helper.getColumn("rank", "pre")));
		path.addCondition(Join.eq(helper.getColumn("rank", "parent"), target.getColumn("rank", "pre")));
	}
	
	// FIXME echt kleiner, größer
	@Override
	public void inADescendantAxis(ADescendantAxis node) {
		path.addCondition(Join.le(context.getColumn("rank", "pre"), target.getColumn("rank", "pre")));
		path.addCondition(Join.le(target.getColumn("rank", "pre"), context.getColumn("rank", "post")));
	}
	
	// FIXME echt größer
	@Override
	public void inAAncestorAxis(AAncestorAxis node) {
		AliasSet helper = aliasSetProvider.getAliasSet();
		path.addAliasSet(helper);
		// rank.pre = rank.pre (nicht struct.id = struct.id)
		path.addCondition(Join.eq(context.getColumn("rank", "pre"), helper.getColumn("rank", "pre")));
		path.addCondition(Join.ge(helper.getColumn("rank", "pre"), target.getColumn("rank", "pre")));
		path.addCondition(Join.ge(target.getColumn("rank", "pre"), helper.getColumn("rank", "post")));
	}
	
	@Override
	public void inASiblingAxis(ASiblingAxis node) {
		path.addCondition(Join.eq(context.getColumn("rank", "parent"), target.getColumn("rank", "parent")));
	}
	
	@Override
	public void inAFollowingAxis(AFollowingAxis node) {
		path.addCondition(Join.eq(context.getColumn("annotations", "text_ref"), target.getColumn("annotations", "text_ref")));
		path.addCondition(Join.lt(context.getColumn("annotations", "token_right"), target.getColumn("annotations", "token_left")));
	}
	
	@Override
	public void inAPrecedingAxis(APrecedingAxis node) {
		path.addCondition(Join.eq(context.getColumn("annotations", "text_ref"), target.getColumn("annotations", "text_ref")));
		path.addCondition(Join.gt(context.getColumn("annotations", "token_right"), target.getColumn("annotations", "token_left")));
	}
	
	@Override
	public void inAFollowingSiblingAxis(AFollowingSiblingAxis node) {
		path.addCondition(Join.eq(context.getColumn("rank", "parent"), target.getColumn("rank", "parent")));
		path.addCondition(Join.eq(context.getColumn("annotations", "text_ref"), target.getColumn("annotations", "text_ref")));
		path.addCondition(Join.lt(context.getColumn("annotations", "token_right"), target.getColumn("annotations", "token_left")));
	}
	
	@Override
	public void inAPrecedingSiblingAxis(APrecedingSiblingAxis node) {
		path.addCondition(Join.eq(context.getColumn("rank", "parent"), target.getColumn("rank", "parent")));
		path.addCondition(Join.eq(context.getColumn("annotations", "text_ref"), target.getColumn("annotations", "text_ref")));
		path.addCondition(Join.gt(context.getColumn("annotations", "token_left"), target.getColumn("annotations", "token_right")));
	}
	
	@Override
	public void inAContainedAxis(AContainedAxis node) {
		path.addCondition(Join.ge(context.getColumn("annotations", "right"), target.getColumn("annotations", "right")));
		path.addCondition(Join.le(context.getColumn("annotations", "left"), target.getColumn("annotations", "left")));
	}
	
	@Override
	public void inAContainingAxis(AContainingAxis node) {
		path.addCondition(Join.le(context.getColumn("annotations", "right"), target.getColumn("annotations", "right")));
		path.addCondition(Join.ge(context.getColumn("annotations", "left"), target.getColumn("annotations", "left")));
	}
	
	@Override
	public void inAPrefixAxis(APrefixAxis node) {
		path.addCondition(Join.eq(context.getColumn("annotations", "text_ref"), target.getColumn("annotations", "text_ref")));
		path.addCondition(Join.eq(context.getColumn("annotations", "right"), target.getColumn("annotations", "right")));
		path.addCondition(Join.le(context.getColumn("annotations", "left"), target.getColumn("annotations", "left")));
	}
	
	@Override
	public void inASuffixAxis(ASuffixAxis node) {
		path.addCondition(Join.eq(context.getColumn("annotations", "text_ref"), target.getColumn("annotations", "text_ref")));
		path.addCondition(Join.ge(context.getColumn("annotations", "right"), target.getColumn("annotations", "right")));
		path.addCondition(Join.eq(context.getColumn("annotations", "left"), target.getColumn("annotations", "left")));
	}
	
	@Override
	public void inAOverlappingAxis(AOverlappingAxis node) {
		Alternative alternative = new Alternative();
		Conjunction conjunction1 = new Conjunction();
		conjunction1.addCondition(Join.ge(context.getColumn("annotations", "left"), target.getColumn("annotations", "left")));
		conjunction1.addCondition(Join.le(context.getColumn("annotations", "left"), target.getColumn("annotations", "left")));
		Conjunction conjunction2 = new Conjunction();
		conjunction2.addCondition(Join.ge(context.getColumn("annotations", "right"), target.getColumn("annotations", "right")));
		conjunction2.addCondition(Join.le(context.getColumn("annotations", "right"), target.getColumn("annotations", "left")));
		alternative.addCondition(conjunction1);
		alternative.addCondition(conjunction2);
		
		path.addCondition(Join.eq(context.getColumn("annotations", "text_ref"), target.getColumn("annotations", "text_ref")));
		path.addCondition(alternative);
	}
	
	@Override
	public void inAOverlappingFollowingAxis(AOverlappingFollowingAxis node) {
		path.addCondition(Join.eq(context.getColumn("annotations", "text_ref"), target.getColumn("annotations", "text_ref")));
		// FIXME: 2x left = left?
		path.addCondition(Join.ge(context.getColumn("annotations", "left"), target.getColumn("annotations", "left")));
		path.addCondition(Join.le(context.getColumn("annotations", "left"), target.getColumn("annotations", "left")));
	}
	
	@Override
	public void inAOverlappingPrecedingAxis(AOverlappingPrecedingAxis node) {
		path.addCondition(Join.eq(context.getColumn("annotations", "text_ref"), target.getColumn("annotations", "text_ref")));
		path.addCondition(Join.ge(context.getColumn("annotations", "right"), target.getColumn("annotations", "right")));
		path.addCondition(Join.le(context.getColumn("annotations", "right"), target.getColumn("annotations", "left")));
	}
	
	// FIXME: test
	@Override
	public void inAImmediatelyFollowingAxis(AImmediatelyFollowingAxis node) {
		path.addCondition(Join.eq(context.getColumn("annotations", "text_ref"), target.getColumn("annotations", "text_ref")));
		// FIXME: doch strings im join? warum noch mal der bezug auf Column?
		path.addCondition(Join.eq(context.getColumn("annotations", "token_right"), target.getColumn("annotations", "token_left").toString() + " - 1"));
	}
	
	@Override
	public void inAMatchingElementAxis(AMatchingElementAxis node) {
		path.addCondition(Join.eq(context.getColumn("annotations", "text_ref"), target.getColumn("annotations", "text_ref")));
		path.addCondition(Join.eq(context.getColumn("annotations", "left"), target.getColumn("annotations", "left")));
		path.addCondition(Join.eq(context.getColumn("annotations", "right"), target.getColumn("annotations", "right")));
	}
	
	@Override
	public void inALeftAlignAxis(ALeftAlignAxis node) {
		path.addCondition(Join.eq(context.getColumn("annotations", "text_ref"), target.getColumn("annotations", "text_ref")));
		path.addCondition(Join.eq(context.getColumn("annotations", "left"), target.getColumn("annotations", "left")));
	}

	@Override
	public void inARightAlignAxis(ARightAlignAxis node) {
		path.addCondition(Join.eq(context.getColumn("annotations", "text_ref"), target.getColumn("annotations", "text_ref")));
		path.addCondition(Join.eq(context.getColumn("annotations", "right"), target.getColumn("annotations", "right")));
	}
	
	@Override
	public void inAElementNodeTest(AElementNodeTest node) {
		textValue = TextValue.Element;
		if (node.getName() != null) {
//			path.addCondition(Join.eq(target.getColumn("annotations", "name"), "'" + token(node.getName()) + "'"));
		}
	}
	
	@Override
	public void inAAttributeNodeTest(AAttributeNodeTest node) {
		if (node.getName() != null) {
			path.addCondition(Join.eq(target.getColumn("annotations", "attribute"), "'" + token(node.getName()) + "'"));
		}
		textValue = TextValue.Attribute;
//		TARGET.USETABLE("ANNOTATIONS");
//		TARGET.USETABLE("ANNOTATIONS");
	}
	
	public JoinField textValue(Node node) {
		if (node instanceof AStringLiteralExpr)
			return textValue((AStringLiteralExpr) node);
		if (node instanceof ARegexpLiteralExpr)
			return textValue((ARegexpLiteralExpr) node);
		if (node instanceof ANumberLiteralExpr)
			return textValue((ANumberLiteralExpr) node);
		if (node instanceof APathExpr)
			return textValue((APathExpr) node);

		throw new RuntimeException("can't determine text value of " + node.getClass());
	}

	private JoinField textValue(AStringLiteralExpr stringLiteral) {
		return new Literal("'" + token(stringLiteral.getString()) + "'");
	}
	
	private JoinField textValue(ARegexpLiteralExpr regexpLiteral) {
		return new RegexpLiteral("'" + token(regexpLiteral.getRegexp()) + "'");
	}

	private JoinField textValue(ANumberLiteralExpr numberLiteral) {
		return new Literal(numberLiteral.getNumber().getText());
	}
	
	private JoinField textValue(APathExpr pathExpr) {
		
		TextValue oldTextValue = textValue;
		
		pathExpr.apply(this);
		
		List<PStep> steps = pathExpr.getStep();
		
		// context node, FIXME das wird schiefgehen, weil jetzt alles annotations verwendet
		if (steps.isEmpty()) {
			if (oldTextValue == TextValue.Attribute)
				return new Column(target, "annotations", "value");
			else
				return new Column(target, "annotations", "span");
		}
		
		// FIXME: test über achse?
		AStep step = (AStep) steps.get(steps.size() - 1);
		
		if (step.getAxis() instanceof AAttributeAxis) {
			return new Column(target, "annotations", "value");
		} else
			return new Column(target, "annotations", "span");
	}

	@Override
	public void caseAComparisonExpr(AComparisonExpr node) {
		node.getComparison().apply(this);
		JoinField lhs = textValue(node.getLhs());
		JoinField rhs = textValue(node.getRhs());
		
		if ( (lhs instanceof RegexpLiteral || rhs instanceof RegexpLiteral) & comparison.equals("=") )
			comparison = "~";
		
		else {
			// FIXME test
			if (lhs instanceof Literal) {
				if (((Literal) lhs).toString().contains("*")) {
					lhs = new Literal(lhs.toString().replace("*", "%"));
					comparison = "ILIKE";
				}
			}
			
			if (rhs instanceof Literal) {
				if (((Literal) rhs).toString().contains("*")) {
					rhs = new Literal(rhs.toString().replace("*", "%"));
					comparison = "ILIKE";
				}
			}
		}
		
		path.addCondition(new Join(comparison, lhs, rhs));
	}
	
	@Override
	public void inAEqComparison(AEqComparison node) {
		comparison = "=";
	}
	
	@Override
	public void inANeComparison(ANeComparison node) {
		comparison = "!=";
	}
	
	@Override
	public void inALtComparison(ALtComparison node) {
		comparison = "<";
	}
	
	@Override
	public void inALeComparison(ALeComparison node) {
		comparison = "<=";
	}
	
	@Override
	public void inAGtComparison(AGtComparison node) {
		comparison = ">";
	}
	
	@Override
	public void inAGeComparison(AGeComparison node) {
		comparison = ">=";
	}
	
	@Override
	public void caseAAndExpr(AAndExpr node) {
		AliasSet oldTarget = target;
		for (PExpr expr : node.getExpr()) {
			expr.apply(this);
			target = oldTarget;
		}
	}
	
	@Override
	public void caseAOrExpr(AOrExpr node) {
		OrCondition condition = new OrCondition();
		condition.setLhs(target.getColumn("rank", "pre"));
		
		AliasSet oldTarget = target;
		Path oldPath = path;
		for (PExpr expr : node.getExpr()) {
			Path innerPath = new Path();
			path = innerPath;
			target = aliasSetProvider.getAliasSet();
			path.addAliasSet(target);
			expr.apply(this);
			target = oldTarget;
			condition.addAlternative(innerPath);
		}
		path = oldPath;
		
		path.addCondition(condition);
	}
	
	private String token(Token token) {
		return token != null ? token.getText() : "";
	}
	

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public String getComparison() {
		return comparison;
	}

	public void setComparison(String comparison) {
		this.comparison = comparison;
	}
	
}
