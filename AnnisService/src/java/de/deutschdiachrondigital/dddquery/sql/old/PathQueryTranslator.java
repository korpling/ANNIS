package de.deutschdiachrondigital.dddquery.sql.old;

import java.util.HashMap;
import java.util.Map;

import de.deutschdiachrondigital.dddquery.analysis.DepthFirstAdapter;
import de.deutschdiachrondigital.dddquery.node.AAbsolutePathType;
import de.deutschdiachrondigital.dddquery.node.AAncestorAxis;
import de.deutschdiachrondigital.dddquery.node.AAndExpr;
import de.deutschdiachrondigital.dddquery.node.AAttributeAxis;
import de.deutschdiachrondigital.dddquery.node.AChildAxis;
import de.deutschdiachrondigital.dddquery.node.AComparisonExpr;
import de.deutschdiachrondigital.dddquery.node.AContainedAxis;
import de.deutschdiachrondigital.dddquery.node.AContainingAxis;
import de.deutschdiachrondigital.dddquery.node.ADescendantAxis;
import de.deutschdiachrondigital.dddquery.node.AEqComparison;
import de.deutschdiachrondigital.dddquery.node.AFollowingAxis;
import de.deutschdiachrondigital.dddquery.node.AGeComparison;
import de.deutschdiachrondigital.dddquery.node.AGtComparison;
import de.deutschdiachrondigital.dddquery.node.ALeComparison;
import de.deutschdiachrondigital.dddquery.node.ALtComparison;
import de.deutschdiachrondigital.dddquery.node.AMarkerSpec;
import de.deutschdiachrondigital.dddquery.node.ANeComparison;
import de.deutschdiachrondigital.dddquery.node.ANumberLiteralExpr;
import de.deutschdiachrondigital.dddquery.node.AParentAxis;
import de.deutschdiachrondigital.dddquery.node.APathExpr;
import de.deutschdiachrondigital.dddquery.node.ARelativePathType;
import de.deutschdiachrondigital.dddquery.node.ASiblingAxis;
import de.deutschdiachrondigital.dddquery.node.AStep;
import de.deutschdiachrondigital.dddquery.node.AStringLiteralExpr;
import de.deutschdiachrondigital.dddquery.node.Node;
import de.deutschdiachrondigital.dddquery.node.PExpr;
import de.deutschdiachrondigital.dddquery.node.TId;
import de.deutschdiachrondigital.dddquery.node.TPattern;
import de.deutschdiachrondigital.dddquery.sql.model.AliasSet;
import de.deutschdiachrondigital.dddquery.sql.model.JoinField;
import de.deutschdiachrondigital.dddquery.sql.model.Literal;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.IsNullCondition;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.Join;
import de.deutschdiachrondigital.dddquery.sql.old.PathQuery.TextValue;

/**
 * - wenn kein target vorhanden ist, wird eins erstellt; dieses wird context im ersten schritt (inAPathExpr)
 * - der textwert eines knoten ist der element-span, es sei denn der pfad endet auf der achse attribute (inAStep)
 * - bei absoluten Pfaden ist der erste context ein Wurzelknoten (inAAbsolutePathType)
 * - XXX: achsen, knotentests
 * - durch teilschritte des pfades ausgezeichnetete knotenmengen können markiert sein (inAMarkerSpec)
 * - variablen
 */
@Deprecated
public class PathQueryTranslator extends DepthFirstAdapter {

	private PathQuery pathQuery;
	private String comparison;
	private Map<String, AliasSet> variables;
	
	public PathQueryTranslator(PathQuery pathQuery) {
		this.pathQuery = pathQuery;
		variables = new HashMap<String, AliasSet>();
	}
	
	@Override
	public void caseAPathExpr(APathExpr node) {
		AliasSet oldTarget = pathQuery.getTargetAliasSet();
		
		super.caseAPathExpr(node);
		
		if (oldTarget != null)
			pathQuery.setTargetAliasSet(oldTarget);
	}
	
	@Override
	public void inARelativePathType(ARelativePathType node) {
		AliasSet parent = pathQuery.getParentAliasSet();
		if (parent != null) {
			pathQuery.setTargetAliasSet(parent);
			pathQuery.addAliasSet(parent);
		} else if (pathQuery.getTargetAliasSet() == null)
			pathQuery.newAliasSet();
	}
	
	@Override
	public void inAAbsolutePathType(AAbsolutePathType node) {
		pathQuery.newAliasSet();
		pathQuery.addCondition(new IsNullCondition(pathQuery.targetAlias("rank", "parent")));
	}
	
	@Override
	public void inAStep(AStep node) {
		if (node.getVariable() != null) {
			String varref = token(node.getVariable());
			if (variables.containsKey(varref)) {
				pathQuery.newAliasSet(variables.get(varref));
			} else {
				variables.put(varref, pathQuery.newAliasSet());
			}
		} else
			pathQuery.newAliasSet();
	}
	
	@Override
	public void inAChildAxis(AChildAxis node) {
		pathQuery.addCondition(Join.eq(pathQuery.contextAlias("rank", "pre"), pathQuery.targetAlias("rank", "parent")));
	}
	
	@Override
	public void inAParentAxis(AParentAxis node) {
		pathQuery.addCondition(Join.eq(pathQuery.contextAlias("struct", "id"), pathQuery.targetAlias("struct", "id")));
		pathQuery.newAliasSet();
		pathQuery.addCondition(Join.eq(pathQuery.contextAlias("rank", "parent"), pathQuery.targetAlias("rank", "pre")));
	}
	
	@Override
	public void inADescendantAxis(ADescendantAxis node) {
		pathQuery.addCondition(Join.lt(pathQuery.contextAlias("rank", "pre"), pathQuery.targetAlias("rank", "pre")));
		pathQuery.addCondition(Join.gt(pathQuery.contextAlias("rank", "post"), pathQuery.targetAlias("rank", "post")));
	}
	
	@Override
	public void inAAncestorAxis(AAncestorAxis node) {
		AliasSet context = pathQuery.getContextAliasSet();
		AliasSet target = pathQuery.getTargetAliasSet();
		AliasSet helper = pathQuery.newAliasSet();
		pathQuery.addCondition(Join.eq(context.getColumn("struct", "id"), helper.getColumn("struct", "id")));
		pathQuery.addCondition(Join.gt(helper.getColumn("rank", "pre"), target.getColumn("rank", "pre")));
		pathQuery.addCondition(Join.lt(helper.getColumn("rank", "post"), target.getColumn("rank", "post")));
	}
	
	@Override
	public void inASiblingAxis(ASiblingAxis node) {
		pathQuery.addCondition(Join.eq(pathQuery.contextAlias("rank", "parent"), pathQuery.targetAlias("rank", "parent")));
	}
	
	@Override
	public void inAFollowingAxis(AFollowingAxis node) {
		pathQuery.addCondition(Join.eq(pathQuery.contextAlias("struct", "text_ref"), pathQuery.targetAlias("struct", "text_ref")));
		pathQuery.addCondition(Join.lt(pathQuery.contextAlias("struct", "right"), pathQuery.targetAlias("struct", "right")));
	}
	
	@Override
	public void inAContainingAxis(AContainingAxis node) {
		pathQuery.addCondition(Join.eq(pathQuery.contextAlias("struct", "text_ref"), pathQuery.targetAlias("struct", "text_ref")));
		pathQuery.addCondition(Join.ge(pathQuery.contextAlias("struct", "left"), pathQuery.targetAlias("struct", "left")));
		pathQuery.addCondition(Join.le(pathQuery.contextAlias("struct", "right"), pathQuery.targetAlias("struct", "right")));
	}
	
	@Override
	public void inAContainedAxis(AContainedAxis node) {
		pathQuery.addCondition(Join.eq(pathQuery.contextAlias("struct", "text_ref"), pathQuery.targetAlias("struct", "text_ref")));
		pathQuery.addCondition(Join.le(pathQuery.contextAlias("struct", "left"), pathQuery.targetAlias("struct", "left")));
		pathQuery.addCondition(Join.ge(pathQuery.contextAlias("struct", "right"), pathQuery.targetAlias("struct", "right")));
	}
	
	@Override
	public void inAAttributeAxis(AAttributeAxis node) {
		pathQuery.addCondition(Join.eq(pathQuery.contextAlias("rank", "pre"), pathQuery.targetAlias("rank", "pre")));
	}
	
//	// XXX: Grammatik ändern?
//	@Override
//	public void inAKindNodeTest(AKindNodeTest node) {
//		PNodeType type = node.getNodeType();
//		
//		if (type instanceof AElementNodeType && node.getName() != null)
//			pathQuery.addCondition(Join.eq(pathQuery.targetAlias("struct", "name"), "'" + token(node.getName()) + "'"));
//		
//		if (type instanceof AAttributeNodeType) {
//			pathQuery.getTargetAliasSet().useTable("anno");
//			if (node.getName() != null)
//				pathQuery.addCondition(Join.eq(pathQuery.targetAlias("anno_attribute", "name"), "'" + token(node.getName()) + "'"));
//		}
//
//	}
	
	@Override
	public void inAMarkerSpec(AMarkerSpec node) {
		pathQuery.markTargetAliasSet(token(node.getMarker()));
	}

	@Override
	public void outAStep(AStep node) {

		// determine the text value of the step
		pathQuery.setTextValue(TextValue.Element);
		node.apply(new DepthFirstAdapter() {
			
			int level = 0;
			
			@Override
			public void inAPathExpr(APathExpr node) {
				++level;
			}
			
			@Override
			public void outAPathExpr(APathExpr node) {
				--level;
			}
			
//			@Override
//			public void caseAAttributeNodeType(AAttributeNodeType node) {
//				if (level == 0)
//					pathQuery.setTextValue(TextValue.Attribute);
//			}
			
		});	
		
		AliasSet target = pathQuery.getTargetAliasSet();
		
		if (target.usesTable("anno_attribute"))
			target.useTable("anno");
		
		if (target.usesTable("anno"))
			target.useTable("struct");
	}
	
	@Override
	public void caseAComparisonExpr(AComparisonExpr node) {
		node.getComparison().apply(this);
		pathQuery.addCondition(new Join(comparison, textValue(node.getLhs()), textValue(node.getRhs())));
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
		for (PExpr expr : node.getExpr()) {
			pathQuery.setTargetAliasSet(null);
			expr.apply(this);
		}
	}
	
	public JoinField textValue(Node node) {
		if (node instanceof AStringLiteralExpr)
			return textValue((AStringLiteralExpr) node);
		if (node instanceof ANumberLiteralExpr)
			return textValue((ANumberLiteralExpr) node);
		if (node instanceof APathExpr)
			return textValue((APathExpr) node);

		throw new RuntimeException("can't determine text value of " + node.getClass());
	}

	private JoinField textValue(AStringLiteralExpr stringLiteral) {
		return new Literal("'" + token(stringLiteral.getString()) + "'");
	}
	
	private JoinField textValue(ANumberLiteralExpr numberLiteral) {
		return new Literal(numberLiteral.getNumber().getText());
	}
	
	private JoinField textValue(APathExpr path) {
		PathQuery tmpPath = new PathQuery(pathQuery.getTargetAliasSet());
		path.apply(new PathQueryTranslator(tmpPath));
		pathQuery.merge(tmpPath);
		switch (tmpPath.getTextValue()) {
		case Attribute: return tmpPath.targetAlias("anno_attribute", "value");
		case Element: ; 
		default: return tmpPath.targetAlias("struct", "span");
		}
	}

	private String token(TId token) {
		return token == null ? null : token.getText();
	}
	
	private String token(TPattern token) {
		return token == null ? "" : token.getText();
	}

	public String getComparison() {
		return comparison;
	}

	public void setComparison(String comparison) {
		this.comparison = comparison;
	}

	public AliasSet getAliasSetByName(String variable) {
		return variables.get(variable);
	}

	public Map<String, AliasSet> getVariables() {
		return variables;
	}

	public void setVariables(Map<String, AliasSet> variables) {
		this.variables = variables;
	}

	public PathQuery getPathQuery() {
		return pathQuery;
	}

	public void setPathQuery(PathQuery pathQuery) {
		this.pathQuery = pathQuery;
	}

}
