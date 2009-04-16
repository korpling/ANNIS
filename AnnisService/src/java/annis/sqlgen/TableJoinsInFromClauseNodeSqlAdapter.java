package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.EDGE_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;

public class TableJoinsInFromClauseNodeSqlAdapter extends AbstractNodeSqlAdapter {

	public String fromClause() {
		StringBuffer sb = new StringBuffer();
		
		// every node uses the struct table
		sb.append(tableAliasDefinition(NODE_TABLE, 1));
		
		// rank table
		if (getTableAccessStrategy().usesRankTable() && ! getTableAccessStrategy().isMaterialized(EDGE_TABLE, NODE_TABLE)) {
			sb.append(" ");
			sb.append(joinDefinition(EDGE_TABLE, "node_ref", NODE_TABLE, "id"));
		}
		
		// node annotations
		if (getTableAccessStrategy().usesNodeAnnotationTable()) {
			int start = getTableAccessStrategy().isMaterialized(NODE_ANNOTATION_TABLE, NODE_TABLE) ? 2 : 1;
			for (int i = start; i <= node.getNodeAnnotations().size(); ++i) {
				sb.append(" ");
				sb.append(joinDefinition(NODE_ANNOTATION_TABLE, "node_ref", NODE_TABLE, "id", i));
			}
		}
		
		// edge annotations
		if (getTableAccessStrategy().usesEdgeAnnotationTable()) {
			int start = getTableAccessStrategy().isMaterialized(EDGE_ANNOTATION_TABLE, EDGE_TABLE) ? 2 : 1;
			for (int i = start; i <= node.getEdgeAnnotations().size(); ++i) {
				sb.append(" ");
				sb.append(joinDefinition(EDGE_ANNOTATION_TABLE, "rank_ref", EDGE_TABLE, "pre", i));
			}
		}
		
		return sb.toString(); 
	}

	///// Joins
	
	String joinDefinition(String table, String column, String joinedTable, String joinedColumn) {
		return joinDefinition(table, column, joinedTable, joinedColumn, 1);
	}

	String joinDefinition(String table, String column, String joinedTable, String joinedColumn, int count) {
		StringBuffer sb = new StringBuffer();
		sb.append("JOIN ");
		sb.append(tableAliasDefinition(table, count));
		sb.append(" ON (");
		sb.append(getTableAccessStrategy().aliasedColumn(table, column, count));
		sb.append(" = ");
		sb.append(getTableAccessStrategy().aliasedColumn(joinedTable, joinedColumn));
		sb.append(")");
		return sb.toString();
	}

}
