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

import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import static annis.sqlgen.AbstractSqlGenerator.TABSTOP;
import static annis.sqlgen.AbstractFromClauseGenerator.tableAliasDefinition;
import static annis.sqlgen.TableAccessStrategy.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;


public class TableJoinsInFromClauseSqlGenerator 
	extends AbstractFromClauseGenerator  {
	
	@Override
	public String fromClause(QueryData queryData, List<QueryNode> alternative, String indent) {
		List<String> tables = new ArrayList<String>();
		for (QueryNode node : alternative)
			tables.add(fromClauseForNode(node, false));
		return StringUtils.join(tables, ",\n" + indent + TABSTOP);
	}
	
  public String fromClauseForNode(QueryNode node, boolean leftJoin) 
  {
    TableAccessStrategy tas = tables(node);
    return fromClauseForNode(tas.getTableAliases(), tas.getColumnAliases(), 
      node, leftJoin);
  }
  
	public static String fromClauseForNode(Map<String, String> tableAliases,
    Map<String, Map<String,String>> columnAliases,
    QueryNode node, boolean leftJoin) 
  {
		StringBuilder sb = new StringBuilder();
		
		// every node uses the node table
		sb.append(tableAliasDefinition(tableAliases, node, NODE_TABLE, 1));
		
		// rank table
		if (usesRankTable(node) && !isMaterialized(tableAliases, RANK_TABLE, NODE_TABLE)) 
    {
			sb.append(" ");
			sb.append(joinDefinition(tableAliases, columnAliases, node, RANK_TABLE, "node_ref", NODE_TABLE, "id", false));
		}
		
		// component table
		if (usesComponentTable(node) && ! isMaterialized(tableAliases, COMPONENT_TABLE, RANK_TABLE)) {
			sb.append(" ");
			sb.append(joinDefinition(tableAliases, columnAliases, node, COMPONENT_TABLE, "id", RANK_TABLE, "component_ref", false));
		}
		
		// node annotations
		if (usesNodeAnnotationTable(node)) {
			int start = isMaterialized(tableAliases, NODE_ANNOTATION_TABLE, NODE_TABLE) ? 2 : 1;
			int size = node != null ? node.getNodeAnnotations().size() : 1;
			for (int i = start; i <= size; ++i) {
				sb.append(" ");
				sb.append(joinDefinition(tableAliases, columnAliases, node, NODE_ANNOTATION_TABLE, "node_ref", NODE_TABLE, "id", i, leftJoin));
			}
		}
		
		// add node annotation table if it is required by the SELECT clause, but not materialized with node table
		if (!usesNodeAnnotationTable(node) && node.getNodeAnnotations().size() > 0 && 
				!isMaterialized(tableAliases, NODE_ANNOTATION_TABLE, NODE_TABLE)) {
			sb.append(" ");
			sb.append(joinDefinition(tableAliases, columnAliases, node, NODE_ANNOTATION_TABLE, "node_ref", NODE_TABLE, "id", leftJoin));
		}
		
		// edge annotations
		if (usesEdgeAnnotationTable(node)) 
    {
			int start = isMaterialized(tableAliases, EDGE_ANNOTATION_TABLE, RANK_TABLE) ? 2 : 1;
			int size = node != null ? node.getEdgeAnnotations().size() : 1;
			for (int i = start; i <= size; ++i) {
				sb.append(" ");
				sb.append(joinDefinition(tableAliases, columnAliases, node, 
          EDGE_ANNOTATION_TABLE, "rank_ref", RANK_TABLE, "pre", i, leftJoin));
			}
		}
				
		return sb.toString(); 
	}

	protected String joinDefinition(QueryNode node, String table, String column, String joinedTable, String joinedColumn) 
  {
    TableAccessStrategy tas = tables(node);
		return joinDefinition(tas.getTableAliases(), tas.getColumnAliases() ,node, table, column, joinedTable, joinedColumn, false);
	}

	protected static String joinDefinition(Map<String, String> tableAliases,
    Map<String, Map<String,String>> columnAliases,
    QueryNode node, String table, String column, String joinedTable, String joinedColumn, boolean leftJoin) 
  {
		return joinDefinition(tableAliases, columnAliases, node, table, column, joinedTable, joinedColumn, 1, leftJoin);
	}

	protected static String joinDefinition(Map<String, String> tableAliases,
    Map<String, Map<String,String>> columnAliases,
    QueryNode node, String table, String column, String joinedTable, String joinedColumn, int count) 
  {
		return joinDefinition(tableAliases, columnAliases, node, table, column, joinedTable, joinedColumn, count, false);
	}

	protected static String joinDefinition(Map<String, String> tableAliases,
    Map<String, Map<String,String>> columnAliases,
    QueryNode node, String table, String column, String joinedTable, String joinedColumn, int count, boolean leftJoin) 
  {
		StringBuffer sb = new StringBuffer();
		if (leftJoin) {
			sb.append("LEFT OUTER ");
		}
		sb.append("JOIN ");
		sb.append(tableAliasDefinition(tableAliases, node, table, count));
		sb.append(" ON (");
		sb.append(aliasedColumn(tableAliases, columnAliases, node, table, column, count));
		sb.append(" = ");
		sb.append(aliasedColumn(tableAliases, columnAliases, node, joinedTable, joinedColumn));
		sb.append(")");
		return sb.toString();
	}

}
