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
import de.deutschdiachrondigital.dddquery.node.AEdgeTypeSpec;
import de.deutschdiachrondigital.dddquery.node.AElementNodeTest;
import de.deutschdiachrondigital.dddquery.node.AEqComparison;
import de.deutschdiachrondigital.dddquery.node.AExactEdgeAnnotation;
import de.deutschdiachrondigital.dddquery.node.AExistanceEdgeAnnotation;
import de.deutschdiachrondigital.dddquery.node.AFollowingAxis;
import de.deutschdiachrondigital.dddquery.node.AFollowingSiblingAxis;
import de.deutschdiachrondigital.dddquery.node.AFunctionExpr;
import de.deutschdiachrondigital.dddquery.node.AGeComparison;
import de.deutschdiachrondigital.dddquery.node.AGtComparison;
import de.deutschdiachrondigital.dddquery.node.AImmediatelyFollowingAxis;
import de.deutschdiachrondigital.dddquery.node.ALeComparison;
import de.deutschdiachrondigital.dddquery.node.ALeftAlignAxis;
import de.deutschdiachrondigital.dddquery.node.ALeftChildAxis;
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
import de.deutschdiachrondigital.dddquery.node.ARangeSpec;
import de.deutschdiachrondigital.dddquery.node.ARegexpEdgeAnnotation;
import de.deutschdiachrondigital.dddquery.node.ARegexpLiteralExpr;
import de.deutschdiachrondigital.dddquery.node.ARightAlignAxis;
import de.deutschdiachrondigital.dddquery.node.ARightChildAxis;
import de.deutschdiachrondigital.dddquery.node.ASiblingAxis;
import de.deutschdiachrondigital.dddquery.node.AStep;
import de.deutschdiachrondigital.dddquery.node.AStringLiteralExpr;
import de.deutschdiachrondigital.dddquery.node.ASuffixAxis;
import de.deutschdiachrondigital.dddquery.node.Node;
import de.deutschdiachrondigital.dddquery.node.PExpr;
import de.deutschdiachrondigital.dddquery.node.PStep;
import de.deutschdiachrondigital.dddquery.node.TNumber;
import de.deutschdiachrondigital.dddquery.node.Token;
import de.deutschdiachrondigital.dddquery.sql.model.AliasSet;
import de.deutschdiachrondigital.dddquery.sql.model.Column;
import de.deutschdiachrondigital.dddquery.sql.model.JoinField;
import de.deutschdiachrondigital.dddquery.sql.model.Literal;
import de.deutschdiachrondigital.dddquery.sql.model.Path;
import de.deutschdiachrondigital.dddquery.sql.model.RegexpLiteral;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.Alternative;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.ArbitraryCondition;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.Conjunction;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.IsNullCondition;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.Join;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.OrCondition;

public class PathTranslator4 extends AbstractPathTranslator {

	private enum TextValue { Element, Attribute };
	private TextValue textValue;
	
	private String comparison;
	
	private String rankTable;
	private String rankAnnoTable;
	private String structTable;
	private String annoTable;
	private String annoAttributeTable;
	
	@Override
	public void inAPathExpr(APathExpr node) {
		if (context == null) {
			target = aliasSetProvider.getAliasSet();
			path.addAliasSet(target);
		}
	}
	
	
	@Override
	public void inAAbsolutePathType(AAbsolutePathType node) {
		path.addCondition(new IsNullCondition(target.getColumn(rankTable, "parent")));
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
//		path.addCondition(Join.eq(context.getColumn("annotations", structTable), target.getColumn("annotations", structTable)));
	}

	@Override
	public void inAChildAxis(AChildAxis node) {
		path.addCondition(Join.eq(context.getColumn(rankTable, "pre"), target.getColumn(rankTable, "parent")));
		path.addCondition(Join.eq(context.getColumn(rankTable, "zshg"), target.getColumn(rankTable, "zshg")));
//		if (node.getEdgeTypeSpec() != null)
//			node.getEdgeTypeSpec().apply(this);
	}
	
	@Override
	public void inAEdgeTypeSpec(AEdgeTypeSpec node) {
		path.addCondition(Join.eq(context.getColumn(rankTable, "edge_type"), "'" + token(node.getEdgeType()) + "'"));
		if (node.getName() != null)
			path.addCondition(Join.eq(context.getColumn(rankTable, "name"), "'" + token(node.getName()) + "'"));
	}
	
	@Override
	public void inALeftChildAxis(ALeftChildAxis node) {
		path.addCondition(Join.eq(context.getColumn(rankTable, "pre"), target.getColumn(rankTable, "pre - 1")));
		// FIXME: edge type?
	}
	
	@Override
	public void inARightChildAxis(ARightChildAxis node) {
		path.addCondition(Join.eq(context.getColumn(rankTable, "post"), target.getColumn(rankTable, "post + 1")));
		// FIXME: edge type?
	}
	
//	@Override
//	public void inAEdgeAnnotation(AEdgeAnnotation node) {
//		path.addCondition(Join.eq(target.getColumn(rankAnnoTable, "edge"), "'" + token(node.getAttribute()) + "'"));
//		path.addCondition(Join.eq(target.getColumn(rankAnnoTable, "value"), "'" + token(node.getValue()) + "'"));
//	}
	
	@Override
	public void caseAExistanceEdgeAnnotation(AExistanceEdgeAnnotation node) {
		AliasSet aliasSet = target;
		if (aliasSet.usesTable(rankAnnoTable)) {
			aliasSet = aliasSetProvider.getAliasSet();
			path.addAliasSet(aliasSet);
			path.addCondition(Join.eq(target.getColumn(rankAnnoTable, "rank_ref"), aliasSet.getColumn(rankAnnoTable, "rank_ref")));
		}
		String pattern = token(node.getType());
		if (node.getNamespace() != null)
			pattern = token(node.getNamespace()) + ":" + pattern;
		path.addCondition(Join.eq(aliasSet.getColumn(rankAnnoTable, "edge"), "'" + pattern + "'"));
	};
	
	@Override
	public void caseAExactEdgeAnnotation(AExactEdgeAnnotation node) {
		AliasSet aliasSet = target;
		if (aliasSet.usesTable(rankAnnoTable)) {
			aliasSet = aliasSetProvider.getAliasSet();
			path.addAliasSet(aliasSet);
			path.addCondition(Join.eq(target.getColumn(rankAnnoTable, "rank_ref"), aliasSet.getColumn(rankAnnoTable, "rank_ref")));
		}
		String pattern = token(node.getType());
		if (node.getNamespace() != null)
			pattern = token(node.getNamespace()) + ":" + pattern;
		path.addCondition(Join.eq(aliasSet.getColumn(rankAnnoTable, "edge"), "'" + pattern + "'"));
		path.addCondition(Join.eq(aliasSet.getColumn(rankAnnoTable, "value"), "'" + token(node.getValue()) + "'"));
	}
	
	@Override
	public void caseARegexpEdgeAnnotation(ARegexpEdgeAnnotation node) {
		AliasSet aliasSet = target;
		if (aliasSet.usesTable(rankAnnoTable)) {
			aliasSet = aliasSetProvider.getAliasSet();
			path.addAliasSet(aliasSet);
			path.addCondition(Join.eq(target.getColumn(rankAnnoTable, "rank_ref"), aliasSet.getColumn(rankAnnoTable, "rank_ref")));
		}
		String pattern = token(node.getType());
		if (node.getNamespace() != null)
			pattern = token(node.getNamespace()) + ":" + pattern;
		path.addCondition(Join.eq(aliasSet.getColumn(rankAnnoTable, "edge"), "'" + pattern + "'"));
		path.addCondition(new Join("~", aliasSet.getColumn(rankAnnoTable, "value"), "'" + token(node.getValue()) + "'"));
	}
	
	@Override
	public void inAParentAxis(AParentAxis node) {
		AliasSet helper = aliasSetProvider.getAliasSet();
		path.addAliasSet(helper);
		// rank.pre = rank.pre (nicht struct.id = struct.id)
		path.addCondition(Join.eq(context.getColumn(rankTable, "pre"), helper.getColumn(rankTable, "pre")));
		path.addCondition(Join.eq(helper.getColumn(rankTable, "parent"), target.getColumn(rankTable, "pre")));
	}
	
	// FIXME echt kleiner, größer
	@Override
	public void inADescendantAxis(ADescendantAxis node) {
		path.addCondition(Join.le(context.getColumn(rankTable, "pre"), target.getColumn(rankTable, "pre")));
		path.addCondition(Join.le(target.getColumn(rankTable, "pre"), context.getColumn(rankTable, "post")));
		path.addCondition(Join.eq(context.getColumn(rankTable, "zshg"), target.getColumn(rankTable, "zshg")));
//		if (node.getEdgeTypeSpec() != null)
//			node.getEdgeTypeSpec().apply(this);
		if (node.getRangeSpec() != null) {
			ARangeSpec rangeSpec = (ARangeSpec) node.getRangeSpec();
			if (rangeSpec.getMax() == null) {
				path.addCondition(Join.eq(context.getColumn(rankTable, "level"), target.getColumn(rankTable, "level - " + token(rangeSpec.getMin()))));
			} else {
				path.addCondition(Join.ge(context.getColumn(rankTable, "level"), target.getColumn(rankTable, "level - " + token(rangeSpec.getMin()))));
				path.addCondition(Join.le(context.getColumn(rankTable, "level"), target.getColumn(rankTable, "level - " + token(rangeSpec.getMax()))));
			}
		}
	}
	
	// FIXME echt größer
	@Override
	public void inAAncestorAxis(AAncestorAxis node) {
		AliasSet helper = aliasSetProvider.getAliasSet();
		path.addAliasSet(helper);
		// rank.pre = rank.pre (nicht struct.id = struct.id)
		path.addCondition(Join.eq(context.getColumn(rankTable, "pre"), helper.getColumn(rankTable, "pre")));
		path.addCondition(Join.ge(helper.getColumn(rankTable, "pre"), target.getColumn(rankTable, "pre")));
		path.addCondition(Join.ge(target.getColumn(rankTable, "pre"), helper.getColumn(rankTable, "post")));
	}
	
	@Override
	public void inASiblingAxis(ASiblingAxis node) {
		path.addCondition(Join.eq(context.getColumn(rankTable, "parent"), target.getColumn(rankTable, "parent")));
	}
	
	@Override
	public void inAFollowingAxis(AFollowingAxis node) {
		path.addCondition(Join.eq(context.getColumn(structTable, "text_ref"), target.getColumn(structTable, "text_ref")));

		ARangeSpec rangeSpec = (ARangeSpec) node.getRangeSpec();
		if (rangeSpec == null)
			rangeSpec = new ARangeSpec(new TNumber("1"), new TNumber("50"));

		if (rangeSpec.getMax() == null) {
			path.addCondition(Join.eq(context.getColumn(structTable, "right_token"), target.getColumn(structTable, "left_token - " + token(rangeSpec.getMin()))));
		} else {
			path.addCondition(Join.le(context.getColumn(structTable, "right_token"), target.getColumn(structTable, "left_token - " + token(rangeSpec.getMin()))));
			path.addCondition(Join.ge(context.getColumn(structTable, "right_token"), target.getColumn(structTable, "left_token - " + token(rangeSpec.getMax()))));
		}
	}
	
	@Override
	public void inAPrecedingAxis(APrecedingAxis node) {
		path.addCondition(Join.eq(context.getColumn(structTable, "text_ref"), target.getColumn(structTable, "text_ref")));
		path.addCondition(Join.gt(context.getColumn(structTable, "right_token"), target.getColumn(structTable, "left_token")));
	}
	
	@Override
	public void inAFollowingSiblingAxis(AFollowingSiblingAxis node) {
		path.addCondition(Join.eq(context.getColumn(rankTable, "parent"), target.getColumn(rankTable, "parent")));
		path.addCondition(Join.eq(context.getColumn(structTable, "text_ref"), target.getColumn(structTable, "text_ref")));
		path.addCondition(Join.lt(context.getColumn(structTable, "right_token"), target.getColumn(structTable, "left_token")));
	}
	
	@Override
	public void inAPrecedingSiblingAxis(APrecedingSiblingAxis node) {
		path.addCondition(Join.eq(context.getColumn(rankTable, "parent"), target.getColumn(rankTable, "parent")));
		path.addCondition(Join.eq(context.getColumn(structTable, "text_ref"), target.getColumn(structTable, "text_ref")));
		path.addCondition(Join.gt(context.getColumn(structTable, "left_token"), target.getColumn(structTable, "right_token")));
	}
	
	@Override
	public void inAContainedAxis(AContainedAxis node) {
		path.addCondition(Join.eq(context.getColumn(structTable, "text_ref"), target.getColumn(structTable, "text_ref")));
		path.addCondition(Join.ge(context.getColumn(structTable, "right"), target.getColumn(structTable, "right")));
		path.addCondition(Join.le(context.getColumn(structTable, "left"), target.getColumn(structTable, "left")));
	}
	
	@Override
	public void inAContainingAxis(AContainingAxis node) {
		path.addCondition(Join.eq(context.getColumn(structTable, "text_ref"), target.getColumn(structTable, "text_ref")));
		path.addCondition(Join.ge(context.getColumn(structTable, "right"), target.getColumn(structTable, "right")));
		path.addCondition(Join.le(context.getColumn(structTable, "left"), target.getColumn(structTable, "left")));
	}
	
	@Override
	public void inAPrefixAxis(APrefixAxis node) {
		path.addCondition(Join.eq(context.getColumn(structTable, "text_ref"), target.getColumn(structTable, "text_ref")));
		path.addCondition(Join.eq(context.getColumn(structTable, "right"), target.getColumn(structTable, "right")));
		path.addCondition(Join.le(context.getColumn(structTable, "left"), target.getColumn(structTable, "left")));
	}
	
	@Override
	public void inASuffixAxis(ASuffixAxis node) {
		path.addCondition(Join.eq(context.getColumn(structTable, "text_ref"), target.getColumn(structTable, "text_ref")));
		path.addCondition(Join.ge(context.getColumn(structTable, "right"), target.getColumn(structTable, "right")));
		path.addCondition(Join.eq(context.getColumn(structTable, "left"), target.getColumn(structTable, "left")));
	}
	
	@Override
	public void inAFunctionExpr(AFunctionExpr node) {
		String name = token(node.getName());
		if (name.equals("isToken"))
			path.addCondition(new ArbitraryCondition(target.getColumn(structTable, "token_index") + " IS NOT NULL"));
	}
	
	@Override
	public void inAOverlappingAxis(AOverlappingAxis node) {
		Alternative alternative = new Alternative();
		Conjunction conjunction1 = new Conjunction();
		conjunction1.addCondition(Join.ge(context.getColumn(structTable, "left"), target.getColumn(structTable, "left")));
		conjunction1.addCondition(Join.le(context.getColumn(structTable, "left"), target.getColumn(structTable, "left")));
		Conjunction conjunction2 = new Conjunction();
		conjunction2.addCondition(Join.ge(context.getColumn(structTable, "right"), target.getColumn(structTable, "right")));
		conjunction2.addCondition(Join.le(context.getColumn(structTable, "right"), target.getColumn(structTable, "left")));
		alternative.addCondition(conjunction1);
		alternative.addCondition(conjunction2);
		
		path.addCondition(Join.eq(context.getColumn(structTable, "text_ref"), target.getColumn(structTable, "text_ref")));
		path.addCondition(alternative);
	}
	
	@Override
	public void inAOverlappingFollowingAxis(AOverlappingFollowingAxis node) {
		path.addCondition(Join.eq(context.getColumn(structTable, "text_ref"), target.getColumn(structTable, "text_ref")));
		// für ANNIS
		path.addCondition(Join.le(context.getColumn(structTable, "left"), target.getColumn(structTable, "left")));
		path.addCondition(Join.gt(context.getColumn(structTable, "right"), target.getColumn(structTable, "left")));
		path.addCondition(Join.le(context.getColumn(structTable, "right"), target.getColumn(structTable, "right")));
	}
	
	@Override
	public void inAOverlappingPrecedingAxis(AOverlappingPrecedingAxis node) {
		path.addCondition(Join.eq(context.getColumn(structTable, "text_ref"), target.getColumn(structTable, "text_ref")));
		// FIXME: broken, see above
		path.addCondition(Join.ge(context.getColumn(structTable, "right"), target.getColumn(structTable, "right")));
		path.addCondition(Join.le(context.getColumn(structTable, "right"), target.getColumn(structTable, "left")));
	}
	
	// FIXME: test
	@Override
	public void inAImmediatelyFollowingAxis(AImmediatelyFollowingAxis node) {
		path.addCondition(Join.eq(context.getColumn(structTable, "text_ref"), target.getColumn(structTable, "text_ref")));
		// FIXME: doch strings im join? warum noch mal der bezug auf Column?
		path.addCondition(Join.eq(context.getColumn(structTable, "right_token"), target.getColumn(structTable, "left_token").toString() + " - 1"));
	}
	
	@Override
	public void inAMatchingElementAxis(AMatchingElementAxis node) {
		path.addCondition(Join.eq(context.getColumn(structTable, "text_ref"), target.getColumn(structTable, "text_ref")));
		path.addCondition(Join.eq(context.getColumn(structTable, "left"), target.getColumn(structTable, "left")));
		path.addCondition(Join.eq(context.getColumn(structTable, "right"), target.getColumn(structTable, "right")));
	}
	
	@Override
	public void inALeftAlignAxis(ALeftAlignAxis node) {
		path.addCondition(Join.eq(context.getColumn(structTable, "text_ref"), target.getColumn(structTable, "text_ref")));
		path.addCondition(Join.eq(context.getColumn(structTable, "left"), target.getColumn(structTable, "left")));
	}

	@Override
	public void inARightAlignAxis(ARightAlignAxis node) {
		path.addCondition(Join.eq(context.getColumn(structTable, "text_ref"), target.getColumn(structTable, "text_ref")));
		path.addCondition(Join.eq(context.getColumn(structTable, "right"), target.getColumn(structTable, "right")));
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
		if (node.getNamespace() != null) {
			path.addCondition(Join.eq(target.getColumn(annoTable, "ns"), "'" + token(node.getNamespace()) + "'"));
		}
		if (node.getName() != null) {
			path.addCondition(Join.eq(target.getColumn(annoAttributeTable, "attribute"), "'" + token(node.getName()) + "'"));
		}
		textValue = TextValue.Attribute;
		target.useTable(structTable);
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
		return new RegexpLiteral("'^" + token(regexpLiteral.getRegexp()) + "$'");
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
				return new Column(target, annoAttributeTable, "value");
			else
				return new Column(target, structTable, "span");
		}
		
		// FIXME: test über achse?
		AStep step = (AStep) steps.get(steps.size() - 1);
		
		if (step.getAxis() instanceof AAttributeAxis) {
			return new Column(target, annoAttributeTable, "value");
		} else
			return new Column(target, structTable, "span");
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
		condition.setLhs(target.getColumn(rankTable, "pre"));
		
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


	public String getRankTable() {
		return rankTable;
	}


	public void setRankTable(String rankTable) {
		this.rankTable = rankTable;
	}


	public String getRankAnnoTable() {
		return rankAnnoTable;
	}


	public void setRankAnnoTable(String rankAnnoTable) {
		this.rankAnnoTable = rankAnnoTable;
	}


	public String getStructTable() {
		return structTable;
	}


	public void setStructTable(String structTable) {
		this.structTable = structTable;
	}


	public String getAnnoTable() {
		return annoTable;
	}


	public void setAnnoTable(String annosTable) {
		this.annoTable = annosTable;
	}


	public String getAnnoAttributeTable() {
		return annoAttributeTable;
	}


	public void setAnnoAttributeTable(String annoAttributeTable) {
		this.annoAttributeTable = annoAttributeTable;
	}
	
}
