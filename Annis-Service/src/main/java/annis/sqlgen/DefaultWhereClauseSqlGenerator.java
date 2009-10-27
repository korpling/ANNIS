package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.COMPONENT_TABLE;
import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnisNode.TextMatching;
import annis.sqlgen.model.CommonAncestor;
import annis.sqlgen.model.Dominance;
import annis.sqlgen.model.Inclusion;
import annis.sqlgen.model.Join;
import annis.sqlgen.model.LeftAlignment;
import annis.sqlgen.model.LeftDominance;
import annis.sqlgen.model.LeftOverlap;
import annis.sqlgen.model.Overlap;
import annis.sqlgen.model.PointingRelation;
import annis.sqlgen.model.Precedence;
import annis.sqlgen.model.RangedJoin;
import annis.sqlgen.model.RankTableJoin;
import annis.sqlgen.model.RightAlignment;
import annis.sqlgen.model.RightDominance;
import annis.sqlgen.model.RightOverlap;
import annis.sqlgen.model.SameSpan;
import annis.sqlgen.model.Sibling;

public class DefaultWhereClauseSqlGenerator 
	extends BaseNodeSqlGenerator
	implements WhereClauseSqlGenerator {

	public List<String> whereConditions(AnnisNode node, List<Long> corpusList,
			List<Annotation> metaData) {
		List<String> conditions = new ArrayList<String>();
		
		if (node.getSpannedText() != null) {
			TextMatching textMatching = node.getSpanTextMatching();
			conditions.add(join(textMatching.sqlOperator(), tables(node).aliasedColumn(NODE_TABLE, "span"), sqlString(node.getSpannedText(), textMatching)));
		}

		if (node.isToken())
			conditions.add(tables(node).aliasedColumn(NODE_TABLE, "token_index") + " IS NOT NULL");

		if (node.isRoot())
			conditions.add(tables(node).aliasedColumn(RANK_TABLE, "root") + " IS TRUE");

		if (node.getNamespace() != null)
			conditions.add(join("=", tables(node).aliasedColumn(NODE_TABLE, "namespace"), sqlString(node.getNamespace())));

		if (node.getName() != null)
			conditions.add(join("=", tables(node).aliasedColumn(NODE_TABLE, "name"), sqlString(node.getName())));

		if (node.getArity() != null) {
			// fugly
			TableAccessStrategy tas = tables(null);
			String pre1 = tables(node).aliasedColumn(RANK_TABLE, "pre");
			String parent = tas.column("children", tas.columnName(RANK_TABLE, "parent"));
			String pre = tas.column("children", tas.columnName(RANK_TABLE, "pre"));
			StringBuffer sb = new StringBuffer();
			sb.append("(SELECT count(DISTINCT " + pre + ")\n");
			sb.append("\tFROM " + tas.tableName(RANK_TABLE) + " AS children\n");
			sb.append("\tWHERE " + parent + " = " + pre1 + ")");
			AnnisNode.Range arity = node.getArity();
			if (arity.getMin() == arity.getMax()) {
				conditions.add(join("=", sb.toString(), String.valueOf(arity.getMin())));
			} else {
				conditions.add(between(sb.toString(), arity.getMin(), arity.getMax()));
			}
		}
		
		if (node.getTokenArity() != null) {
			AnnisNode.Range tokenArity = node.getTokenArity();
			if (tokenArity.getMin() == tokenArity.getMax()) {
				conditions.add(numberJoin("=", tables(node).aliasedColumn(NODE_TABLE, "left_token"), tables(node).aliasedColumn(NODE_TABLE, "right_token"),	-(tokenArity.getMin()) + 1));
			} else {
				conditions.add(between(tables(node).aliasedColumn(NODE_TABLE, "left_token"), tables(node).aliasedColumn(NODE_TABLE, "right_token"), -(tokenArity.getMin()) + 1, -(tokenArity.getMax()) + 1));
			}
		}
		
		addAnnotationConditions(node, conditions, node.getNodeAnnotations(), NODE_ANNOTATION_TABLE);

		addJoinConditions(node, conditions);

		addAnnotationConditions(node, conditions, node.getEdgeAnnotations(), EDGE_ANNOTATION_TABLE);

		return conditions;
	}
	
	private void addJoinConditions(AnnisNode node, List<String> conditions) {
		for (Join join : node.getJoins()) {
			AnnisNode target = join.getTarget();
		
			if (join instanceof SameSpan) {
				conditions.add(join("=", tables(node).aliasedColumn(NODE_TABLE, "text_ref"), tables(target).aliasedColumn(NODE_TABLE, "text_ref")));
				conditions.add(join("=", tables(node).aliasedColumn(NODE_TABLE, "left"), tables(target).aliasedColumn(NODE_TABLE, "left")));
				conditions.add(join("=", tables(node).aliasedColumn(NODE_TABLE, "right"), tables(target).aliasedColumn(NODE_TABLE, "right")));
			
			} else if (join instanceof LeftAlignment) {
				conditions.add(join("=", tables(node).aliasedColumn(NODE_TABLE, "text_ref"), tables(target).aliasedColumn(NODE_TABLE, "text_ref")));
				conditions.add(join("=", tables(node).aliasedColumn(NODE_TABLE, "left"), tables(target).aliasedColumn(NODE_TABLE, "left")));
			
			} else if (join instanceof RightAlignment) {
				conditions.add(join("=", tables(node).aliasedColumn(NODE_TABLE, "text_ref"), tables(target).aliasedColumn(NODE_TABLE, "text_ref")));
				conditions.add(join("=", tables(node).aliasedColumn(NODE_TABLE, "right"), tables(target).aliasedColumn(NODE_TABLE, "right")));
			
			} else if (join instanceof Inclusion) {
				conditions.add(join("=", tables(node).aliasedColumn(NODE_TABLE, "text_ref"), tables(target).aliasedColumn(NODE_TABLE, "text_ref")));
				conditions.add(join("<=", tables(node).aliasedColumn(NODE_TABLE, "left"), tables(target).aliasedColumn(NODE_TABLE, "left")));
				conditions.add(join(">=", tables(node).aliasedColumn(NODE_TABLE, "right"), tables(target).aliasedColumn(NODE_TABLE, "right")));
			
			} else if (join instanceof Overlap) {
				conditions.add(join("=", tables(node).aliasedColumn(NODE_TABLE, "text_ref"), tables(target).aliasedColumn(NODE_TABLE, "text_ref")));
				conditions.add(join("<=", tables(node).aliasedColumn(NODE_TABLE, "left"), tables(target).aliasedColumn(NODE_TABLE, "right")));
				conditions.add(join("<=", tables(target).aliasedColumn(NODE_TABLE, "left"), tables(node).aliasedColumn(NODE_TABLE, "right")));
			
			} else if (join instanceof LeftOverlap) {
				conditions.add(join("=", tables(node).aliasedColumn(NODE_TABLE, "text_ref"), tables(target).aliasedColumn(NODE_TABLE, "text_ref")));
				conditions.add(join("<=", tables(node).aliasedColumn(NODE_TABLE, "left"), tables(target).aliasedColumn(NODE_TABLE, "left")));
				conditions.add(join("<=", tables(target).aliasedColumn(NODE_TABLE, "left"), tables(node).aliasedColumn(NODE_TABLE, "right")));
				conditions.add(join("<=", tables(node).aliasedColumn(NODE_TABLE, "right"), tables(target).aliasedColumn(NODE_TABLE, "right")));
			
			} else if (join instanceof RightOverlap) {
				conditions.add(join("=", tables(node).aliasedColumn(NODE_TABLE, "text_ref"), tables(target).aliasedColumn(NODE_TABLE, "text_ref")));
				conditions.add(join(">=", tables(node).aliasedColumn(NODE_TABLE, "right"), tables(target).aliasedColumn(NODE_TABLE, "right")));
				conditions.add(join(">=", tables(target).aliasedColumn(NODE_TABLE, "right"), tables(node).aliasedColumn(NODE_TABLE, "left")));
				conditions.add(join(">=", tables(node).aliasedColumn(NODE_TABLE, "left"), tables(target).aliasedColumn(NODE_TABLE, "left")));
			
			} else if (join instanceof Precedence) {
				conditions.add(join("=", tables(node).aliasedColumn(NODE_TABLE, "text_ref"), tables(target).aliasedColumn(NODE_TABLE, "text_ref")));
				
				RangedJoin precedence = (RangedJoin) join;
				int min = precedence.getMinDistance();
				int max = precedence.getMaxDistance();
			
				// indirect
				if (min == 0 && max == 0) {
					conditions.add(join("<", tables(node).aliasedColumn(NODE_TABLE, "right_token"), tables(target).aliasedColumn(NODE_TABLE, "left_token")));
			
					// exact distance
				} else if (min == max) {
					conditions.add(numberJoin("=", tables(node).aliasedColumn(NODE_TABLE, "right_token"), tables(target).aliasedColumn(NODE_TABLE, "left_token"), -min));
			
					// ranged distance
				} else {
					conditions.add(between(tables(node).aliasedColumn(NODE_TABLE, "right_token"), tables(target).aliasedColumn(NODE_TABLE, "left_token"), -min, -max));
//					conditions.add(numberJoin("<=", tables(node).aliasedColumn(NODE_TABLE, "right_token"), tables(target).aliasedColumn(NODE_TABLE, "left_token"), -min));
//					conditions.add(numberJoin(">=", tables(node).aliasedColumn(NODE_TABLE, "right_token"), tables(target).aliasedColumn(NODE_TABLE, "left_token"), -max));
				}
			
			} else if (join instanceof Sibling) {
				conditions.add(join("=", tables(node).aliasedColumn(COMPONENT_TABLE, "type"), sqlString("d")));
				Sibling sibling = (Sibling) join;
				if (sibling.getName() != null)
					conditions.add(join("=", tables(node).aliasedColumn(COMPONENT_TABLE, "name"), sqlString(sibling.getName())));
				else
					conditions.add(tables(node).aliasedColumn(COMPONENT_TABLE, "name") + " IS NULL");
				conditions.add(join("=", tables(node).aliasedColumn(RANK_TABLE, "parent"), tables(target).aliasedColumn(RANK_TABLE, "parent")));
				
			} else if (join instanceof CommonAncestor) {
				conditions.add(join("=", tables(node).aliasedColumn(COMPONENT_TABLE, "type"), sqlString("d")));
				CommonAncestor commonAncestor = (CommonAncestor) join;
				if (commonAncestor.getName() != null)
					conditions.add(join("=", tables(node).aliasedColumn(COMPONENT_TABLE, "name"), sqlString(commonAncestor.getName())));
				else
					conditions.add(tables(node).aliasedColumn(COMPONENT_TABLE, "name") + " IS NULL");

				// fugly
				TableAccessStrategy tas = tables(null);
				String pre1 = tables(node).aliasedColumn(RANK_TABLE, "pre");
				String pre2 = tables(target).aliasedColumn(RANK_TABLE, "pre");
				String pre = tas.column("ancestor", tas.columnName(RANK_TABLE, "pre"));
				String post = tas.column("ancestor", tas.columnName(RANK_TABLE, "post"));
				
				StringBuffer sb = new StringBuffer();
				sb.append("EXISTS (SELECT 1 FROM " + tas.tableName(RANK_TABLE) + " AS ancestor WHERE\n");
				sb.append("\t" + pre + " < " + pre1 + " AND " + pre1 + " < " + post + " AND\n");
				sb.append("\t" + pre + " < " + pre2 + " AND " + pre2 + " < " + post + ")");
				conditions.add(sb.toString());
				
			} else if (join instanceof LeftDominance) {
				conditions.add(join("=", tables(node).aliasedColumn(COMPONENT_TABLE, "type"), sqlString("d")));
				RankTableJoin rankTableJoin = (RankTableJoin) join;
				if (rankTableJoin.getName() != null)
					conditions.add(join("=", tables(node).aliasedColumn(COMPONENT_TABLE, "name"), sqlString(rankTableJoin.getName())));
				else
					conditions.add(tables(node).aliasedColumn(COMPONENT_TABLE, "name") + " IS NULL");
				conditions.add(numberJoin("=", tables(node).aliasedColumn(RANK_TABLE, "pre"), tables(target).aliasedColumn(RANK_TABLE, "pre"), -1));
			
			} else if (join instanceof RightDominance) {
				conditions.add(join("=", tables(node).aliasedColumn(COMPONENT_TABLE, "type"), sqlString("d")));				
				RankTableJoin rankTableJoin = (RankTableJoin) join;
				if (rankTableJoin.getName() != null)
					conditions.add(join("=", tables(node).aliasedColumn(COMPONENT_TABLE, "name"), sqlString(rankTableJoin.getName())));
				else
					conditions.add(tables(node).aliasedColumn(COMPONENT_TABLE, "name") + " IS NULL");
				conditions.add(numberJoin("=", tables(node).aliasedColumn(RANK_TABLE, "post"), tables(target).aliasedColumn(RANK_TABLE, "post"), 1));
			
			} else if (join instanceof Dominance) {
				addEdgeConditions(node, target, conditions, join, "d");

			} else if (join instanceof PointingRelation) {
				addEdgeConditions(node, target, conditions, join, "p");

			}
		}
	}

	private void addEdgeConditions(AnnisNode node, AnnisNode target, List<String> conditions, Join join, final String edgeType) {
//		conditions.add(join("=", tables(node).aliasedColumn(RANK_TABLE, "component_ref"), tables(target).aliasedColumn(RANK_TABLE, "component_ref")));				
		conditions.add(join("=", tables(node).aliasedColumn(COMPONENT_TABLE, "type"), sqlString(edgeType)));				
		
		RankTableJoin rankTableJoin = (RankTableJoin) join;
		if (rankTableJoin.getName() != null)
			conditions.add(join("=", tables(node).aliasedColumn(COMPONENT_TABLE, "name"), sqlString(rankTableJoin.getName())));
		else
			conditions.add(tables(node).aliasedColumn(COMPONENT_TABLE, "name") + " IS NULL");

		int min = rankTableJoin.getMinDistance();
		int max = rankTableJoin.getMaxDistance();

		// direct
		if (min == 1 && max == 1) {
			conditions.add(join("=", tables(node).aliasedColumn(RANK_TABLE, "pre"), tables(target).aliasedColumn(RANK_TABLE, "parent")));

		// indirect
		} else {
			conditions.add(join("<", tables(node).aliasedColumn(RANK_TABLE, "pre"), tables(target).aliasedColumn(RANK_TABLE, "pre")));
			conditions.add(join("<", tables(target).aliasedColumn(RANK_TABLE, "pre"), tables(node).aliasedColumn(RANK_TABLE, "post")));

			// exact
			if (min > 0 && min == max) {
				conditions.add(numberJoin("=", tables(node).aliasedColumn(RANK_TABLE, "level"), tables(target).aliasedColumn(RANK_TABLE, "level"), -min));

			// range
			} else if (min > 0 && min < max) {
				conditions.add(between(tables(node).aliasedColumn(RANK_TABLE, "level"), tables(target).aliasedColumn(RANK_TABLE, "level"), -min, -max));
//				conditions.add(numberJoin("<=", tables(node).aliasedColumn(RANK_TABLE, "level"), tables(target).aliasedColumn(RANK_TABLE, "level"), -(min + 1)));
//				conditions.add(numberJoin(">=", tables(node).aliasedColumn(RANK_TABLE, "level"), tables(target).aliasedColumn(RANK_TABLE, "level"), -(max + 1)));
			}
		}
	}

	private void addAnnotationConditions(AnnisNode node, List<String> conditions, Set<Annotation> annotations, String table) {
		int i = 0;
		for (Annotation annotation : annotations) {
			++i;
			if (annotation.getNamespace() != null)
				conditions.add(join("=", tables(node).aliasedColumn(table, "namespace", i), sqlString(annotation.getNamespace())));
			conditions.add(join("=", tables(node).aliasedColumn(table, "name", i), sqlString(annotation.getName())));
			if (annotation.getValue() != null) {
				TextMatching textMatching = annotation.getTextMatching();
				conditions.add(join(textMatching.sqlOperator(), tables(node).aliasedColumn(table, "value", i), sqlString(annotation.getValue(), textMatching)));
			}
		}
	}


}
