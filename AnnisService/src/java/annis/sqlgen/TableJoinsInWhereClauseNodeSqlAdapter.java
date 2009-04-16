package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.EDGE_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class TableJoinsInWhereClauseNodeSqlAdapter extends AbstractNodeSqlAdapter {

	public String fromClause() {
		List<String> tables = new ArrayList<String>();
		
		// every node uses the struct table
		tables.add(tableAliasDefinition(NODE_TABLE, 1));
		
		// rank table
		if (getTableAccessStrategy().usesRankTable() && ! getTableAccessStrategy().isMaterialized(EDGE_TABLE, NODE_TABLE)) {
			tables.add(tableAliasDefinition(EDGE_TABLE, 1));
		}
		
		// node annotations
		if (getTableAccessStrategy().usesNodeAnnotationTable()) {
			int start = getTableAccessStrategy().isMaterialized(NODE_ANNOTATION_TABLE, NODE_TABLE) ? 2 : 1;
			for (int i = start; i <= node.getNodeAnnotations().size(); ++i) {
				tables.add(tableAliasDefinition(NODE_ANNOTATION_TABLE, i));
			}
		}
		
		// edge annotations
		if (getTableAccessStrategy().usesEdgeAnnotationTable()) {
			int start = getTableAccessStrategy().isMaterialized(EDGE_ANNOTATION_TABLE, EDGE_TABLE) ? 2 : 1;
			for (int i = start; i <= node.getEdgeAnnotations().size(); ++i) {
				tables.add(tableAliasDefinition(EDGE_ANNOTATION_TABLE, i));
			}
		}
		
		return StringUtils.join(tables, ", "); 
	}
	
	@Override
	public List<String> whereClause() {
		List<String> conditions = super.whereClause();
		
		// join rank table
		if (getTableAccessStrategy().usesRankTable() && ! getTableAccessStrategy().isMaterialized(EDGE_TABLE, NODE_TABLE)) {
			conditions.add(join("=", getTableAccessStrategy().aliasedColumn(EDGE_TABLE, "node_ref"), getTableAccessStrategy().aliasedColumn(NODE_TABLE, "id")));
		}
		
		// join node annotations
		if (getTableAccessStrategy().usesNodeAnnotationTable()) {
			int start = getTableAccessStrategy().isMaterialized(NODE_ANNOTATION_TABLE, NODE_TABLE) ? 2 : 1;
			for (int i = start; i <= node.getNodeAnnotations().size(); ++i) {
				conditions.add(join("=", getTableAccessStrategy().aliasedColumn(NODE_ANNOTATION_TABLE, "node_ref", i), getTableAccessStrategy().aliasedColumn(NODE_TABLE, "id")));
			}
		}
		
		// join edge annotations
		if (getTableAccessStrategy().usesEdgeAnnotationTable()) {
			int start = getTableAccessStrategy().isMaterialized(EDGE_ANNOTATION_TABLE, EDGE_TABLE) ? 2 : 1;
			for (int i = start; i <= node.getEdgeAnnotations().size(); ++i) {
				conditions.add(join("=", getTableAccessStrategy().aliasedColumn(EDGE_ANNOTATION_TABLE, "rank_ref", i), getTableAccessStrategy().aliasedColumn(EDGE_TABLE, "pre")));
			}
		}
		
		return conditions;
	}
	
}
