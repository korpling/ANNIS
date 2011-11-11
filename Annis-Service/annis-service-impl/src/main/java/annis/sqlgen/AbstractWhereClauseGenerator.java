package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.ql.parser.QueryData;
import annis.sqlgen.model.CommonAncestor;
import annis.sqlgen.model.Dominance;
import annis.sqlgen.model.Identical;
import annis.sqlgen.model.Inclusion;
import annis.sqlgen.model.Join;
import annis.sqlgen.model.LeftAlignment;
import annis.sqlgen.model.LeftDominance;
import annis.sqlgen.model.LeftOverlap;
import annis.sqlgen.model.Overlap;
import annis.sqlgen.model.PointingRelation;
import annis.sqlgen.model.Precedence;
import annis.sqlgen.model.RightAlignment;
import annis.sqlgen.model.RightDominance;
import annis.sqlgen.model.RightOverlap;
import annis.sqlgen.model.SameSpan;
import annis.sqlgen.model.Sibling;

public abstract class AbstractWhereClauseGenerator 
	extends TableAccessStrategyFactory 
	implements WhereClauseSqlGenerator<QueryData>
{

	public Set<String> whereConditions(QueryData queryData, List<AnnisNode> alternative, String indent) {
		List<String> conditions = new ArrayList<String>();
	
		for (AnnisNode node : alternative) {
	
			// node constraints
			if (node.getSpannedText() != null) {
				addSpanConditions(conditions, queryData, node);
			}
			if (node.isToken()) {
				addIsTokenConditions(conditions, queryData, node);
			}
			if (node.isRoot()) {
				addIsRootConditions(conditions, queryData, node);
			}
			if (node.getNamespace() != null) {
				addNodeNamespaceConditions(conditions, queryData, node);
			}
			if (node.getName() != null) {
				addNodeNameCondition(conditions, queryData, node);
			}
			if (node.getArity() != null) {
				addNodeArityConditions(conditions, queryData, node);
			}
			if (node.getTokenArity() != null) {
				addTokenArityConditions(conditions, queryData, node);
			}
	
			// node joins
			for (Join join : node.getJoins()) {
				AnnisNode target = join.getTarget();
				if (join instanceof SameSpan) {
					addSameSpanConditions(conditions, node, target,
							(SameSpan) join, queryData);
				} else if (join instanceof Identical) {
					addIdenticalConditions(conditions, node, target,
							(Identical) join, queryData);
				} else if (join instanceof LeftAlignment) {
					addLeftAlignmentConditions(conditions, node, target,
							(LeftAlignment) join, queryData);
				} else if (join instanceof RightAlignment) {
					addRightAlignmentConditions(conditions, node, target,
							(RightAlignment) join, queryData);
				} else if (join instanceof Inclusion) {
					addInclusionConditions(conditions, node, target,
							(Inclusion) join, queryData);
				} else if (join instanceof Overlap) {
					addOverlapConditions(conditions, node, target,
							(Overlap) join, queryData);
				} else if (join instanceof LeftOverlap) {
					addLeftOverlapConditions(conditions, target, node,
							(LeftOverlap) join, queryData);
				} else if (join instanceof RightOverlap) {
					addRightOverlapConditions(conditions, target, node,
							(RightOverlap) join, queryData);
				} else if (join instanceof Precedence) {
					addPrecedenceConditions(conditions, node, target,
							(Precedence) join, queryData);
				} else if (join instanceof Sibling) {
					addSiblingConditions(conditions, node, target,
							(Sibling) join, queryData);
				} else if (join instanceof CommonAncestor) {
					addCommonAncestorConditions(conditions, node, target,
							(CommonAncestor) join, queryData);
				} else if (join instanceof LeftDominance) {
					addLeftDominanceConditions(conditions, node, target,
							(LeftDominance) join, queryData);
				} else if (join instanceof RightDominance) {
					addRightDominanceConditions(conditions, node, target,
							(RightDominance) join, queryData);
				} else if (join instanceof Dominance) {
					addDominanceConditions(conditions, node, target,
							(Dominance) join, queryData);
				} else if (join instanceof PointingRelation) {
					addPointingRelationConditions(conditions, node, target,
							(PointingRelation) join, queryData);
				}
			}
	
			// node annotations
			int i = 0;
			for (Annotation annotation : node.getNodeAnnotations()) {
				++i;
				addAnnotationConditions(conditions, node, i, annotation, NODE_ANNOTATION_TABLE);
			}
	
			// edge annotations
			int j = 0;
			for (Annotation annotation : node.getEdgeAnnotations()) {
				++j;
				addAnnotationConditions(conditions, node, j, annotation, EDGE_ANNOTATION_TABLE);
			}
		}
	
		return new HashSet<String>(conditions);
	}

	protected abstract void addSpanConditions(List<String> conditions, QueryData queryData,
			AnnisNode node);

	protected abstract void addIsTokenConditions(List<String> conditions, QueryData queryData,
			AnnisNode node);

	protected abstract void addIsRootConditions(List<String> conditions, QueryData queryData,
			AnnisNode node);

	protected abstract void addNodeNamespaceConditions(List<String> conditions,
			QueryData queryData, AnnisNode node);

	protected abstract void addNodeNameCondition(List<String> conditions, QueryData queryData,
			AnnisNode node);

	protected abstract void addNodeArityConditions(List<String> conditions, QueryData queryData,
			AnnisNode node);

	protected abstract void addTokenArityConditions(List<String> conditions,
			QueryData queryData, AnnisNode node);

	protected abstract void addSingleEdgeCondition(AnnisNode node, AnnisNode target,
			List<String> conditions, Join join, final String edgeType);

	protected abstract void addSiblingConditions(List<String> conditions, AnnisNode node,
			AnnisNode target, Sibling join, QueryData queryData);

	protected abstract void addCommonAncestorConditions(List<String> conditions,
			AnnisNode node, AnnisNode target, CommonAncestor join, QueryData queryData);

	protected abstract void addSameSpanConditions(List<String> conditions, AnnisNode node,
			AnnisNode target, SameSpan join, QueryData queryData);

	protected abstract void addIdenticalConditions(List<String> conditions, AnnisNode node,
			AnnisNode target, Identical join, QueryData queryData);

	protected abstract void addLeftAlignmentConditions(List<String> conditions,
			AnnisNode node, AnnisNode target, LeftAlignment join, QueryData queryData);

	protected abstract void addRightAlignmentConditions(List<String> conditions,
			AnnisNode node, AnnisNode target, RightAlignment join, QueryData queryData);

	protected abstract void addInclusionConditions(List<String> conditions, AnnisNode node,
			AnnisNode target, Inclusion join, QueryData queryData);

	protected abstract void addOverlapConditions(List<String> conditions, AnnisNode node,
			AnnisNode target, Overlap join, QueryData queryData);

	protected abstract void addLeftOverlapConditions(List<String> conditions,
			AnnisNode target, AnnisNode node, LeftOverlap join, QueryData queryData);

	protected abstract void addRightOverlapConditions(List<String> conditions,
			AnnisNode target, AnnisNode node, RightOverlap join, QueryData queryData);

	protected abstract void addPrecedenceConditions(List<String> conditions,
			AnnisNode node, AnnisNode target, Precedence join, QueryData queryData);

	protected abstract void addAnnotationConditions(List<String> conditions,
			AnnisNode node, int index, Annotation annotation, String table);

	protected abstract void addLeftDominanceConditions(List<String> conditions,
			AnnisNode node, AnnisNode target, LeftDominance join, QueryData queryData);

	protected abstract void addRightDominanceConditions(List<String> conditions,
			AnnisNode node, AnnisNode target, RightDominance join, QueryData queryData);

	protected abstract void addDominanceConditions(List<String> conditions, AnnisNode node,
			AnnisNode target, Dominance join, QueryData queryData);

	protected abstract void addPointingRelationConditions(List<String> conditions,
			AnnisNode node, AnnisNode target, PointingRelation join, QueryData queryData);

}