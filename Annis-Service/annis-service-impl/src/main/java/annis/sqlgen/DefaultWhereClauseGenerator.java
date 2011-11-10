/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.sqlgen;

import static annis.sqlgen.SqlConstraints.between;
import static annis.sqlgen.SqlConstraints.in;
import static annis.sqlgen.SqlConstraints.join;
import static annis.sqlgen.SqlConstraints.numberJoin;
import static annis.sqlgen.SqlConstraints.sqlString;
import static annis.sqlgen.TableAccessStrategy.COMPONENT_TABLE;
import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.FACTS_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import annis.model.AnnisNode;
import annis.model.AnnisNode.TextMatching;
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
import annis.sqlgen.model.RangedJoin;
import annis.sqlgen.model.RankTableJoin;
import annis.sqlgen.model.RightAlignment;
import annis.sqlgen.model.RightDominance;
import annis.sqlgen.model.RightOverlap;
import annis.sqlgen.model.SameSpan;
import annis.sqlgen.model.Sibling;

public class DefaultWhereClauseGenerator extends TableAccessStrategyFactory
		implements WhereClauseSqlGenerator {

	@Override
	public Set<String> whereConditions(QueryData queryData,
			List<AnnisNode> alternative, String indent) {
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
				addAnnotationConditions(conditions, node, i, annotation, NODE_ANNOTATION_TABLE,
						"node_annotation_");
			}

			// edge annotations
			int j = 0;
			for (Annotation annotation : node.getEdgeAnnotations()) {
				++j;
				addAnnotationConditions(conditions, node, j, annotation, EDGE_ANNOTATION_TABLE,
						"edge_annotation_");
			}
		}

		return new HashSet<String>(conditions);
	}

	protected void addPointingRelationConditions(List<String> conditions,
			AnnisNode node, AnnisNode target,
			PointingRelation join, QueryData queryData) {
		addSingleEdgeCondition(node, target, conditions, join,
				"p");
	}

	protected void addDominanceConditions(List<String> conditions,
			AnnisNode node, AnnisNode target, Dominance join,
			QueryData queryData) {
		addSingleEdgeCondition(node, target, conditions, join, "d");
	}

	protected void addRightDominanceConditions(List<String> conditions,
			AnnisNode node, AnnisNode target,
			RightDominance join, QueryData queryData) {
		addLeftOrRightDominance(conditions, node, target, queryData,
				join, "max", "right_token");
	}

	protected void addLeftDominanceConditions(List<String> conditions,
			AnnisNode node, AnnisNode target, LeftDominance join,
			QueryData queryData) {
		addLeftOrRightDominance(conditions, node, target, queryData,
				join, "min", "left_token");
	}

	void addLeftOrRightDominance(List<String> conditions, AnnisNode node,
			AnnisNode target, QueryData queryData, RankTableJoin join,
			String aggregationFunction, String tokenBoarder) {
		List<Long> corpusList = queryData.getCorpusList();
		conditions.add(join("=",
				tables(target).aliasedColumn(COMPONENT_TABLE, "type"),
				sqlString("d")));
		conditions.add(join("=", tables(node).aliasedColumn(RANK_TABLE, "pre"),
				tables(target).aliasedColumn(RANK_TABLE, "parent")));

		if (join.getName() != null) {
			conditions.add(join("=",
					tables(target).aliasedColumn(COMPONENT_TABLE, "name"),
					sqlString(join.getName())));
		} else {
			conditions.add(tables(target)
					.aliasedColumn(COMPONENT_TABLE, "name") + " IS NULL");
		}

		conditions.add(in(
				tables(target).aliasedColumn(NODE_TABLE, tokenBoarder),
				"SELECT "
						+ aggregationFunction
						+ "(lrsub."
						+ tokenBoarder
						+ ") FROM "
						+ FACTS_TABLE
						+ " as lrsub "
						+ "WHERE parent="
						+ tables(node).aliasedColumn(RANK_TABLE, "pre")
						+ " AND corpus_ref="
						+ tables(target)
								.aliasedColumn(NODE_TABLE, "corpus_ref")
						+ " AND toplevel_corpus IN("
						+ (corpusList == null || corpusList.isEmpty() ? "NULL"
								: StringUtils.join(corpusList, ",")) + ")"));
	}

	void joinOnNode(List<String> conditions, AnnisNode node, AnnisNode target,
			String operator, String leftColumn, String rightColumn) {
		conditions.add(join(operator,
				tables(node).aliasedColumn(NODE_TABLE, leftColumn),
				tables(target).aliasedColumn(NODE_TABLE, rightColumn)));
	}

	void betweenJoinOnNode(List<String> conditions, AnnisNode node,
			AnnisNode target, String leftColumn, String rightColumn, int min,
			int max) {
		conditions
				.add(between(
						tables(node).aliasedColumn(NODE_TABLE, leftColumn),
						tables(target).aliasedColumn(NODE_TABLE, rightColumn),
						min, max));
	}

	void numberJoinOnNode(List<String> conditions, AnnisNode node,
			AnnisNode target, String operator, String leftColumn,
			String rightColumn, int offset) {
		conditions.add(numberJoin(operator,
				tables(node).aliasedColumn(NODE_TABLE, leftColumn),
				tables(target).aliasedColumn(NODE_TABLE, rightColumn), offset));
	}

	protected void addAnnotationConditions(List<String> conditions,
			AnnisNode node, int index, Annotation annotation, String table,
			String tableAlias) {
		if (annotation.getNamespace() != null) {
			conditions.add(join("=",
					tables(node).aliasedColumn(table,
							tableAlias + "namespace", index),
					sqlString(annotation.getNamespace())));
		}
		conditions.add(join("=",
				tables(node).aliasedColumn(table,
						tableAlias + "name", index),
				sqlString(annotation.getName())));
		if (annotation.getValue() != null) {
			TextMatching textMatching = annotation.getTextMatching();
			conditions.add(join(textMatching.sqlOperator(), tables(node)
					.aliasedColumn(table,
							tableAlias + "value", index),
					sqlString(annotation.getValue(), textMatching)));
		}
	}

	protected void addPrecedenceConditions(List<String> conditions,
			AnnisNode node, AnnisNode target, Precedence join,
			QueryData queryData) {
		joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");

		RangedJoin precedence = (RangedJoin) join;
		int min = precedence.getMinDistance();
		int max = precedence.getMaxDistance();

		// indirect
		if (min == 0 && max == 0) {
			// FIXME: optimize indirect precedence
			joinOnNode(conditions, node, target, "<", "right_token",
					"left_token");

		}
		// exact distance
		else if (min == max) {
			numberJoinOnNode(conditions, node, target, "=", "right_token",
					"left_token", -min);

		}
		// ranged distance
		else {
			betweenJoinOnNode(conditions, node, target, "right_token",
					"left_token", -min, -max);
			// conditions.add(numberJoin("<=",
			// tables(node).aliasedColumn(NODE_TABLE, "right_token"),
			// tables(target).aliasedColumn(NODE_TABLE, "left_token"), -min));
			// conditions.add(numberJoin(">=",
			// tables(node).aliasedColumn(NODE_TABLE, "right_token"),
			// tables(target).aliasedColumn(NODE_TABLE, "left_token"), -max));
		}
	}

	protected void addRightOverlapConditions(List<String> conditions,
			AnnisNode target, AnnisNode node, RightOverlap join,
			QueryData queryData) {
		joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
		joinOnNode(conditions, node, target, ">=", "right", "right");
		joinOnNode(conditions, target, node, ">=", "right", "left");
		joinOnNode(conditions, node, target, ">=", "left", "left");
	}

	protected void addLeftOverlapConditions(List<String> conditions,
			AnnisNode target, AnnisNode node, LeftOverlap join,
			QueryData queryData) {
		joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
		joinOnNode(conditions, node, target, "<=", "left", "left");
		joinOnNode(conditions, target, node, "<=", "left", "right");
		joinOnNode(conditions, node, target, "<=", "right", "right");
	}

	protected void addOverlapConditions(List<String> conditions,
			AnnisNode node, AnnisNode target, Overlap join,
			QueryData queryData) {
		joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
		joinOnNode(conditions, node, target, "<=", "left", "right");
		joinOnNode(conditions, target, node, "<=", "left", "right");
	}

	protected void addInclusionConditions(List<String> conditions,
			AnnisNode node, AnnisNode target, Inclusion join,
			QueryData queryData) {
		// FIMXE: optimizeInclusion
		joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
		joinOnNode(conditions, node, target, "<=", "left", "left");
		joinOnNode(conditions, node, target, ">=", "right", "right");
	}

	protected void addRightAlignmentConditions(List<String> conditions,
			AnnisNode node, AnnisNode target,
			RightAlignment join, QueryData queryData) {
		joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
		joinOnNode(conditions, node, target, "=", "right", "right");
	}

	protected void addLeftAlignmentConditions(List<String> conditions,
			AnnisNode node, AnnisNode target, LeftAlignment join,
			QueryData queryData) {
		joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
		joinOnNode(conditions, node, target, "=", "left", "left");
	}

	protected void addIdenticalConditions(List<String> conditions,
			AnnisNode node, AnnisNode target, Identical join,
			QueryData queryData) {
		joinOnNode(conditions, node, target, "=", "id", "id");
	}

	protected void addSameSpanConditions(List<String> conditions,
			AnnisNode node, AnnisNode target, SameSpan join,
			QueryData queryData) {
		joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
		joinOnNode(conditions, node, target, "=", "left", "left");
		joinOnNode(conditions, node, target, "=", "right", "right");
	}

	protected void addCommonAncestorConditions(List<String> conditions,
			AnnisNode node, AnnisNode target,
			CommonAncestor join, QueryData queryData) {
		List<Long> corpusList = queryData.getCorpusList();
		conditions.add(join("=",
				tables(node).aliasedColumn(COMPONENT_TABLE, "type"),
				sqlString("d")));
		if (join.getName() != null) {
			conditions.add(join("=",
					tables(node).aliasedColumn(COMPONENT_TABLE, "name"),
					sqlString(join.getName())));
		} else {
			conditions.add(tables(node).aliasedColumn(COMPONENT_TABLE, "name")
					+ " IS NULL");
		}

		joinOnNode(conditions, node, target, "<>", "id", "id");

		// fugly
		TableAccessStrategy tas = tables(null);
		String pre1 = tables(node).aliasedColumn(RANK_TABLE, "pre");
		String pre2 = tables(target).aliasedColumn(RANK_TABLE, "pre");
		String pre = tas.column("ancestor", tas.columnName(RANK_TABLE, "pre"));
		String post = tas
				.column("ancestor", tas.columnName(RANK_TABLE, "post"));

		StringBuffer sb = new StringBuffer();
		sb.append("EXISTS (SELECT 1 FROM " + tas.tableName(RANK_TABLE)
				+ " AS ancestor WHERE\n");
		sb.append("\t" + pre + " < " + pre1 + " AND " + pre1 + " < " + post
				+ " AND\n");
		sb.append("\t"
				+ pre
				+ " < "
				+ pre2
				+ " AND "
				+ pre2
				+ " < "
				+ post
				+ " AND toplevel_corpus IN("
				+ (corpusList == null || corpusList.isEmpty() ? "NULL"
						: StringUtils.join(corpusList, ",")) + "))");
		conditions.add(sb.toString());
	}

	protected void addSiblingConditions(List<String> conditions,
			AnnisNode node, AnnisNode target, Sibling join,
			QueryData queryData) {
		conditions.add(join("=",
				tables(node).aliasedColumn(COMPONENT_TABLE, "type"),
				sqlString("d")));
		Sibling sibling = (Sibling) join;
		if (sibling.getName() != null) {
			conditions.add(join("=",
					tables(node).aliasedColumn(COMPONENT_TABLE, "name"),
					sqlString(sibling.getName())));
		} else {
			conditions.add(tables(node).aliasedColumn(COMPONENT_TABLE, "name")
					+ " IS NULL");
		}
		conditions.add(join("=",
				tables(node).aliasedColumn(RANK_TABLE, "parent"),
				tables(target).aliasedColumn(RANK_TABLE, "parent")));
		joinOnNode(conditions, node, target, "<>", "id", "id");
	}

	protected void addSingleEdgeCondition(AnnisNode node, AnnisNode target,
			List<String> conditions, Join join, final String edgeType) {
		conditions.add(join("=",
				tables(target).aliasedColumn(COMPONENT_TABLE, "type"),
				sqlString(edgeType)));

		RankTableJoin rankTableJoin = (RankTableJoin) join;
		if (rankTableJoin.getName() != null) {
			conditions.add(join("=",
					tables(target).aliasedColumn(COMPONENT_TABLE, "name"),
					sqlString(rankTableJoin.getName())));
		} else {
			conditions.add(tables(target)
					.aliasedColumn(COMPONENT_TABLE, "name") + " IS NULL");
		}

		int min = rankTableJoin.getMinDistance();
		int max = rankTableJoin.getMaxDistance();

		// direct
		if (min == 1 && max == 1) {
			conditions.add(join("=",
					tables(node).aliasedColumn(RANK_TABLE, "pre"),
					tables(target).aliasedColumn(RANK_TABLE, "parent")));

			// indirect
		} else {
			conditions.add(join("<",
					tables(node).aliasedColumn(RANK_TABLE, "pre"),
					tables(target).aliasedColumn(RANK_TABLE, "pre")));
			conditions.add(join("<",
					tables(target).aliasedColumn(RANK_TABLE, "pre"),
					tables(node).aliasedColumn(RANK_TABLE, "post")));

			// exact
			if (min > 0 && min == max) {
				conditions
						.add(numberJoin(
								"=",
								tables(node).aliasedColumn(RANK_TABLE, "level"),
								tables(target).aliasedColumn(RANK_TABLE,
										"level"), -min));

				// range
			} else if (min > 0 && min < max) {
				conditions.add(between(
						tables(node).aliasedColumn(RANK_TABLE, "level"),
						tables(target).aliasedColumn(RANK_TABLE, "level"),
						-min, -max));
			}
		}
	}

	protected void addTokenArityConditions(List<String> conditions,
			QueryData queryData, AnnisNode node) {
		AnnisNode.Range tokenArity = node.getTokenArity();
		if (tokenArity.getMin() == tokenArity.getMax()) {
			conditions.add(numberJoin("=",
					tables(node).aliasedColumn(NODE_TABLE, "left_token"),
					tables(node).aliasedColumn(NODE_TABLE, "right_token"),
					-(tokenArity.getMin()) + 1));
		} else {
			conditions.add(between(
					tables(node).aliasedColumn(NODE_TABLE, "left_token"),
					tables(node).aliasedColumn(NODE_TABLE, "right_token"),
					-(tokenArity.getMin()) + 1, -(tokenArity.getMax()) + 1));
		}
	}

	protected void addNodeArityConditions(List<String> conditions,
			QueryData queryData, AnnisNode node) {
		// fugly
		List<Long> corpusList = queryData.getCorpusList();
		TableAccessStrategy tas = tables(null);
		String pre1 = tables(node).aliasedColumn(RANK_TABLE, "pre");
		String parent = tas.column("children",
				tas.columnName(RANK_TABLE, "parent"));
		String pre = tas.column("children", tas.columnName(RANK_TABLE, "pre"));
		StringBuffer sb = new StringBuffer();
		sb.append("(SELECT count(DISTINCT " + pre + ")\n");
		sb.append("\tFROM " + tas.tableName(RANK_TABLE) + " AS children\n");
		sb.append("\tWHERE "
				+ parent
				+ " = "
				+ pre1
				+ " AND toplevel_corpus IN("
				+ (corpusList.isEmpty() ? "NULL" : StringUtils.join(corpusList,
						",")) + ")" + ")");
		AnnisNode.Range arity = node.getArity();
		if (arity.getMin() == arity.getMax()) {
			conditions.add(join("=", sb.toString(),
					String.valueOf(arity.getMin())));
		} else {
			conditions.add(between(sb.toString(), arity.getMin(),
					arity.getMax()));
		}
	}

	protected void addNodeNameCondition(List<String> conditions,
			QueryData queryData, AnnisNode node) {
		conditions.add(join("=",
				tables(node).aliasedColumn(NODE_TABLE, "name"),
				sqlString(node.getName())));
	}

	protected void addNodeNamespaceConditions(List<String> conditions,
			QueryData queryData, AnnisNode node) {
		conditions.add(join("=",
				tables(node).aliasedColumn(NODE_TABLE, "namespace"),
				sqlString(node.getNamespace())));
	}

	protected void addIsRootConditions(List<String> conditions,
			QueryData queryData, AnnisNode node) {
		conditions.add(tables(node).aliasedColumn(RANK_TABLE, "root")
				+ " IS TRUE");
	}

	protected void addIsTokenConditions(List<String> conditions,
			QueryData queryData, AnnisNode node) {
		conditions.add(tables(node).aliasedColumn(NODE_TABLE, "is_token")
				+ " IS TRUE");
	}

	protected void addSpanConditions(List<String> conditions,
			QueryData queryData, AnnisNode node) {
		TextMatching textMatching = node.getSpanTextMatching();
		conditions.add(join(textMatching.sqlOperator(), tables(node)
				.aliasedColumn(NODE_TABLE, "span"),
				sqlString(node.getSpannedText(), textMatching)));
	}

}
