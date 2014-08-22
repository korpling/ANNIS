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

import static annis.sqlgen.AbstractSqlGenerator.TABSTOP;
import static annis.sqlgen.SqlConstraints.join;
import static annis.sqlgen.TableAccessStrategy.COMPONENT_TABLE;
import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import annis.model.QueryNode;
import annis.ql.parser.QueryData;

public class TableJoinsInWhereClauseGenerator
	extends AbstractFromClauseGenerator
	implements WhereClauseSqlGenerator<QueryData>
{

	@Override
	public String fromClause(QueryData queryData, List<QueryNode> alternative,
			String indent) {
		List<String> tables = new ArrayList<>();
		for (QueryNode node : alternative)
			tables.add(fromClauseForNode(queryData.getCorpusList(), node, false));
		return StringUtils.join(tables, ",\n" + indent + TABSTOP);
	}

	@Override
	public Set<String> whereConditions(QueryData queryData,
			List<QueryNode> alternative, String indent) {
		Set<String> conditions = new HashSet<>();
		
		for (QueryNode node : alternative) {
			conditions.addAll(whereConditionsForNode(node));
		}
		
		return conditions;
	}
	
  public String fromClauseForNode(QueryNode node) {
		return fromClauseForNode(null, node, false);
	}
  
	public String fromClauseForNode(List<Long> corpusList, QueryNode node) {
		return fromClauseForNode(corpusList, node, false);
	}

	public String fromClauseForNode(List<Long> corpusList, 
    QueryNode node, boolean leftJoin) {
		List<String> tables = new ArrayList<>();

		// every node uses the node table
		tables.add(tableAliasDefinition(node, NODE_TABLE, 1, corpusList));

		// rank table
		if (tables(node).usesRankTable() && ! tables(node).isMaterialized(RANK_TABLE, NODE_TABLE)) {
			tables.add(tableAliasDefinition(node, RANK_TABLE, 1, corpusList));
		}

		// component table
		if (tables(node).usesRankTable() && ! tables(node).isMaterialized(COMPONENT_TABLE, RANK_TABLE)) {
			tables.add(tableAliasDefinition(node, COMPONENT_TABLE, 1, corpusList));
		}

		// node annotations
		if (tables(node).usesNodeAnnotationTable()) {
			int start = tables(node).isMaterialized(NODE_ANNOTATION_TABLE, NODE_TABLE) ? 2 : 1;
			for (int i = start; i <= node.getNodeAnnotations().size(); ++i) {
				tables.add(tableAliasDefinition(node, NODE_ANNOTATION_TABLE, i, corpusList));
			}
		}

		// edge annotations
		if (tables(node).usesEdgeAnnotationTable()) {
			int start = tables(node).isMaterialized(EDGE_ANNOTATION_TABLE, RANK_TABLE) ? 2 : 1;
			for (int i = start; i <= node.getEdgeAnnotations().size(); ++i) {
				tables.add(tableAliasDefinition(node, EDGE_ANNOTATION_TABLE, i, corpusList));
			}
		}

		return StringUtils.join(tables, ", ");
	}

  public Set<String> whereConditionsForNode(QueryNode node)
  {

		Set<String> conditions = new HashSet<>();

		// join rank table
		if (tables(node).usesRankTable() && ! tables(node).isMaterialized(RANK_TABLE, NODE_TABLE)) {
			conditions.add(join("=", tables(node).aliasedColumn(RANK_TABLE, "node_ref"), tables(node).aliasedColumn(NODE_TABLE, "id")));
		}

		// join component table
		if (tables(node).usesRankTable() && ! tables(node).isMaterialized(COMPONENT_TABLE, RANK_TABLE)) {
			conditions.add(join("=", tables(node).aliasedColumn(RANK_TABLE, "component_ref"), tables(node).aliasedColumn(COMPONENT_TABLE, "id")));
		}

		// join node annotations
		if (tables(node).usesNodeAnnotationTable()) {
			int start = tables(node).isMaterialized(NODE_ANNOTATION_TABLE, NODE_TABLE) ? 2 : 1;
			for (int i = start; i <= node.getNodeAnnotations().size(); ++i) {
				conditions.add(join("=", tables(node).aliasedColumn(NODE_ANNOTATION_TABLE, "node_ref", i), tables(node).aliasedColumn(NODE_TABLE, "id")));
			}
		}

		// join edge annotations
		if (tables(node).usesEdgeAnnotationTable()) {
			int start = tables(node).isMaterialized(EDGE_ANNOTATION_TABLE, RANK_TABLE) ? 2 : 1;
			for (int i = start; i <= node.getEdgeAnnotations().size(); ++i) {
				conditions.add(join("=", tables(node).aliasedColumn(EDGE_ANNOTATION_TABLE, "rank_ref", i), tables(node).aliasedColumn(RANK_TABLE, "pre")));
			}
		}

		return conditions;
	}

}