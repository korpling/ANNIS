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
import static annis.sqlgen.TableAccessStrategy.COMPONENT_TABLE;
import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;
import static annis.sqlgen.TableAccessStrategy.TEXT_TABLE;

import java.util.ArrayList;
import java.util.List;


import org.apache.commons.lang.StringUtils;

import annis.model.AnnisNode;
import annis.ql.parser.QueryData;


public class TableJoinsInFromClauseSqlGenerator 
	extends AbstractFromClauseGenerator  {
	
	@Override
	public String fromClause(QueryData queryData, List<AnnisNode> alternative, String indent) {
		List<String> tables = new ArrayList<String>();
		for (AnnisNode node : alternative)
			tables.add(fromClauseForNode(node, false));
		return StringUtils.join(tables, ",\n" + indent + TABSTOP);
	}
	
	public String fromClauseForNode(AnnisNode node, boolean leftJoin) {
		StringBuffer sb = new StringBuffer();
		
		// every node uses the node table
		sb.append(tableAliasDefinition(node, NODE_TABLE, 1));
		
		// rank table
		if (tables(node).usesRankTable() && ! tables(node).isMaterialized(RANK_TABLE, NODE_TABLE)) {
			sb.append(" ");
			sb.append(joinDefinition(node, RANK_TABLE, "node_ref", NODE_TABLE, "id", false));
		}
		
		// component table
		if (tables(node).usesComponentTable() && ! tables(node).isMaterialized(COMPONENT_TABLE, RANK_TABLE)) {
			sb.append(" ");
			sb.append(joinDefinition(node, COMPONENT_TABLE, "id", RANK_TABLE, "component_ref", false));
		}
		
		// node annotations
		if (tables(node).usesNodeAnnotationTable()) {
			int start = tables(node).isMaterialized(NODE_ANNOTATION_TABLE, NODE_TABLE) ? 2 : 1;
			int size = node != null ? node.getNodeAnnotations().size() : 1;
			for (int i = start; i <= size; ++i) {
				sb.append(" ");
				sb.append(joinDefinition(node, NODE_ANNOTATION_TABLE, "node_ref", NODE_TABLE, "id", i, leftJoin));
			}
		}
		
		// add node annotation table if it is required by the SELECT clause, but not materialized with node table
		if (! tables(node).usesNodeAnnotationTable() && node.requiresTable(NODE_ANNOTATION_TABLE) && 
				! tables(node).isMaterialized(NODE_ANNOTATION_TABLE, NODE_TABLE)) {
			sb.append(" ");
			sb.append(joinDefinition(node, NODE_ANNOTATION_TABLE, "node_ref", NODE_TABLE, "id", leftJoin));
		}
		
		// edge annotations
		if (tables(node).usesEdgeAnnotationTable()) {
			int start = tables(node).isMaterialized(EDGE_ANNOTATION_TABLE, RANK_TABLE) ? 2 : 1;
			int size = node != null ? node.getEdgeAnnotations().size() : 1;
			for (int i = start; i <= size; ++i) {
				sb.append(" ");
				sb.append(joinDefinition(node, EDGE_ANNOTATION_TABLE, "rank_ref", RANK_TABLE, "pre", i, leftJoin));
			}
		}
		
		// text table
		if (node != null && node.requiresTable(TEXT_TABLE)) {
			sb.append(" ");
			sb.append(joinDefinition(node, TEXT_TABLE, "id", NODE_TABLE, "text_ref", false));
		}
		
		return sb.toString(); 
	}

	protected String joinDefinition(AnnisNode node, String table, String column, String joinedTable, String joinedColumn) {
		return joinDefinition(node, table, column, joinedTable, joinedColumn, false);
	}

	protected String joinDefinition(AnnisNode node, String table, String column, String joinedTable, String joinedColumn, boolean leftJoin) {
		return joinDefinition(node, table, column, joinedTable, joinedColumn, 1, leftJoin);
	}

	protected String joinDefinition(AnnisNode node, String table, String column, String joinedTable, String joinedColumn, int count) {
		return joinDefinition(node, table, column, joinedTable, joinedColumn, count, false);
	}

	protected String joinDefinition(AnnisNode node, String table, String column, String joinedTable, String joinedColumn, int count, boolean leftJoin) {
		StringBuffer sb = new StringBuffer();
		if (leftJoin) {
			sb.append("LEFT OUTER ");
		}
		sb.append("JOIN ");
		sb.append(tableAliasDefinition(node, table, count));
		sb.append(" ON (");
		sb.append(tables(node).aliasedColumn(table, column, count));
		sb.append(" = ");
		sb.append(tables(node).aliasedColumn(joinedTable, joinedColumn));
		sb.append(")");
		return sb.toString();
	}

}
