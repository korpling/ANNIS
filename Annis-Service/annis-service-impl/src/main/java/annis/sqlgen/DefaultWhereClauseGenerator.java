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
import static annis.sqlgen.TableAccessStrategy.FACTS_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import annis.model.QueryNode;
import annis.model.QueryNode.TextMatching;
import annis.model.QueryAnnotation;
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

public class DefaultWhereClauseGenerator 
	extends AbstractWhereClauseGenerator
{

	@Override
	protected void addPointingRelationConditions(List<String> conditions,
			QueryNode node, QueryNode target,
			PointingRelation join, QueryData queryData) {
		addSingleEdgeCondition(node, target, conditions, join,
				"p");
	}

	@Override
	protected void addDominanceConditions(List<String> conditions,
			QueryNode node, QueryNode target, Dominance join,
			QueryData queryData) {
		addSingleEdgeCondition(node, target, conditions, join, "d");
	}

	@Override
	protected void addRightDominanceConditions(List<String> conditions,
			QueryNode node, QueryNode target,
			RightDominance join, QueryData queryData) {
		addLeftOrRightDominance(conditions, node, target, queryData,
				join, "max", "right_token");
	}

	@Override
	protected void addLeftDominanceConditions(List<String> conditions,
			QueryNode node, QueryNode target, LeftDominance join,
			QueryData queryData) {
		addLeftOrRightDominance(conditions, node, target, queryData,
				join, "min", "left_token");
	}

	void addLeftOrRightDominance(List<String> conditions, QueryNode node,
			QueryNode target, QueryData queryData, RankTableJoin join,
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

	void joinOnNode(List<String> conditions, QueryNode node, QueryNode target,
			String operator, String leftColumn, String rightColumn) {
		conditions.add(join(operator,
				tables(node).aliasedColumn(NODE_TABLE, leftColumn),
				tables(target).aliasedColumn(NODE_TABLE, rightColumn)));
	}

	void betweenJoinOnNode(List<String> conditions, QueryNode node,
			QueryNode target, String leftColumn, String rightColumn, int min,
			int max) {
		conditions
				.add(between(
						tables(node).aliasedColumn(NODE_TABLE, leftColumn),
						tables(target).aliasedColumn(NODE_TABLE, rightColumn),
						min, max));
	}

	void numberJoinOnNode(List<String> conditions, QueryNode node,
			QueryNode target, String operator, String leftColumn,
			String rightColumn, int offset) {
		conditions.add(numberJoin(operator,
				tables(node).aliasedColumn(NODE_TABLE, leftColumn),
				tables(target).aliasedColumn(NODE_TABLE, rightColumn), offset));
	}

	@Override
	protected void addAnnotationConditions(List<String> conditions,
			QueryNode node, int index, QueryAnnotation annotation, String table) {
		if (annotation.getNamespace() != null) {
			conditions.add(join("=",
					tables(node).aliasedColumn(table,
							"namespace", index),
					sqlString(annotation.getNamespace())));
		}
		conditions.add(join("=",
				tables(node).aliasedColumn(table,
						"name", index),
				sqlString(annotation.getName())));
		if (annotation.getValue() != null) {
			TextMatching textMatching = annotation.getTextMatching();
			conditions.add(join(textMatching.sqlOperator(), tables(node)
					.aliasedColumn(table,
							"value", index),
					sqlString(annotation.getValue(), textMatching)));
		}
	}

	@Override
	protected void addPrecedenceConditions(List<String> conditions,
			QueryNode node, QueryNode target, Precedence join,
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

	@Override
	protected void addRightOverlapConditions(List<String> conditions,
			QueryNode target, QueryNode node, RightOverlap join,
			QueryData queryData) {
		joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
		joinOnNode(conditions, node, target, ">=", "right", "right");
		joinOnNode(conditions, target, node, ">=", "right", "left");
		joinOnNode(conditions, node, target, ">=", "left", "left");
	}

	@Override
	protected void addLeftOverlapConditions(List<String> conditions,
			QueryNode target, QueryNode node, LeftOverlap join,
			QueryData queryData) {
		joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
		joinOnNode(conditions, node, target, "<=", "left", "left");
		joinOnNode(conditions, target, node, "<=", "left", "right");
		joinOnNode(conditions, node, target, "<=", "right", "right");
	}

	@Override
	protected void addOverlapConditions(List<String> conditions,
			QueryNode node, QueryNode target, Overlap join,
			QueryData queryData) {
		joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
		joinOnNode(conditions, node, target, "<=", "left", "right");
		joinOnNode(conditions, target, node, "<=", "left", "right");
	}

	@Override
	protected void addInclusionConditions(List<String> conditions,
			QueryNode node, QueryNode target, Inclusion join,
			QueryData queryData) {
		// FIMXE: optimizeInclusion
		joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
		joinOnNode(conditions, node, target, "<=", "left", "left");
		joinOnNode(conditions, node, target, ">=", "right", "right");
	}

	@Override
	protected void addRightAlignmentConditions(List<String> conditions,
			QueryNode node, QueryNode target,
			RightAlignment join, QueryData queryData) {
		joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
		joinOnNode(conditions, node, target, "=", "right", "right");
	}

	@Override
	protected void addLeftAlignmentConditions(List<String> conditions,
			QueryNode node, QueryNode target, LeftAlignment join,
			QueryData queryData) {
		joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
		joinOnNode(conditions, node, target, "=", "left", "left");
	}

	@Override
	protected void addIdenticalConditions(List<String> conditions,
			QueryNode node, QueryNode target, Identical join,
			QueryData queryData) {
		joinOnNode(conditions, node, target, "=", "id", "id");
	}

	@Override
	protected void addSameSpanConditions(List<String> conditions,
			QueryNode node, QueryNode target, SameSpan join,
			QueryData queryData) {
		joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
		joinOnNode(conditions, node, target, "=", "left", "left");
		joinOnNode(conditions, node, target, "=", "right", "right");
	}

	@Override
	protected void addCommonAncestorConditions(List<String> conditions,
			QueryNode node, QueryNode target,
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

	@Override
	protected void addSiblingConditions(List<String> conditions,
			QueryNode node, QueryNode target, Sibling join,
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

	@Override
	protected void addSingleEdgeCondition(QueryNode node, QueryNode target,
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

	@Override
	protected void addTokenArityConditions(List<String> conditions,
			QueryData queryData, QueryNode node) {
		QueryNode.Range tokenArity = node.getTokenArity();
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

	@Override
	protected void addNodeArityConditions(List<String> conditions,
			QueryData queryData, QueryNode node) {
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
		QueryNode.Range arity = node.getArity();
		if (arity.getMin() == arity.getMax()) {
			conditions.add(join("=", sb.toString(),
					String.valueOf(arity.getMin())));
		} else {
			conditions.add(between(sb.toString(), arity.getMin(),
					arity.getMax()));
		}
	}

	@Override
	protected void addNodeNameCondition(List<String> conditions,
			QueryData queryData, QueryNode node) {
		conditions.add(join("=",
				tables(node).aliasedColumn(NODE_TABLE, "name"),
				sqlString(node.getName())));
	}

	@Override
	protected void addNodeNamespaceConditions(List<String> conditions,
			QueryData queryData, QueryNode node) {
		conditions.add(join("=",
				tables(node).aliasedColumn(NODE_TABLE, "namespace"),
				sqlString(node.getNamespace())));
	}

	@Override
	protected void addIsRootConditions(List<String> conditions,
			QueryData queryData, QueryNode node) {
		conditions.add(tables(node).aliasedColumn(RANK_TABLE, "root")
				+ " IS TRUE");
	}

	@Override
	protected void addIsTokenConditions(List<String> conditions,
			QueryData queryData, QueryNode node) {
		conditions.add(tables(node).aliasedColumn(NODE_TABLE, "is_token")
				+ " IS TRUE");
	}

	@Override
	protected void addSpanConditions(List<String> conditions,
			QueryData queryData, QueryNode node) {
		TextMatching textMatching = node.getSpanTextMatching();
		conditions.add(join(textMatching.sqlOperator(), tables(node)
				.aliasedColumn(NODE_TABLE, "span"),
				sqlString(node.getSpannedText(), textMatching)));
	}

}
