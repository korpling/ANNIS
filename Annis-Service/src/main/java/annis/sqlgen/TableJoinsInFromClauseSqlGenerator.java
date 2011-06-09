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

import static annis.sqlgen.TableAccessStrategy.COMPONENT_TABLE;
import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;
import annis.model.AnnisNode;

public class TableJoinsInFromClauseSqlGenerator 
	extends BaseNodeSqlGenerator 
	implements FromClauseSqlGenerator {

	public String fromClause(AnnisNode node) {
		StringBuffer sb = new StringBuffer();
		
		// every node uses the node table
		sb.append(tableAliasDefinition(node, NODE_TABLE, 1));
		
		// rank table
		if (tables(node).usesRankTable() && ! tables(node).isMaterialized(RANK_TABLE, NODE_TABLE)) {
			sb.append(" ");
			sb.append(joinDefinition(node, RANK_TABLE, "node_ref", NODE_TABLE, "id"));
		}
		
		// component table
		if (tables(node).usesComponentTable() && ! tables(node).isMaterialized(COMPONENT_TABLE, RANK_TABLE)) {
			sb.append(" ");
			sb.append(joinDefinition(node, COMPONENT_TABLE, "id", RANK_TABLE, "component_ref"));
		}
		
		// node annotations
		if (tables(node).usesNodeAnnotationTable()) {
			int start = tables(node).isMaterialized(NODE_ANNOTATION_TABLE, NODE_TABLE) ? 2 : 1;
			for (int i = start; i <= node.getNodeAnnotations().size(); ++i) {
				sb.append(" ");
				sb.append(joinDefinition(node, NODE_ANNOTATION_TABLE, "node_ref", NODE_TABLE, "id", i));
			}
		}
		
		// edge annotations
		if (tables(node).usesEdgeAnnotationTable()) {
			int start = tables(node).isMaterialized(EDGE_ANNOTATION_TABLE, RANK_TABLE) ? 2 : 1;
			for (int i = start; i <= node.getEdgeAnnotations().size(); ++i) {
				sb.append(" ");
				sb.append(joinDefinition(node, EDGE_ANNOTATION_TABLE, "rank_ref", RANK_TABLE, "pre", i));
			}
		}
		
		return sb.toString(); 
	}

	protected String joinDefinition(AnnisNode node, String table, String column, String joinedTable, String joinedColumn) {
		return joinDefinition(node, table, column, joinedTable, joinedColumn, 1);
	}

	protected String joinDefinition(AnnisNode node, String table, String column, String joinedTable, String joinedColumn, int count) {
		StringBuffer sb = new StringBuffer();
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
