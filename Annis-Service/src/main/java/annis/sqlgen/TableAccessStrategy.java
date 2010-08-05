package annis.sqlgen;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.Bag;
import org.apache.commons.collections.bag.HashBag;

import annis.model.AnnisNode;

public class TableAccessStrategy {

	// default table names
  public final static String REAL_NODE_TABLE = "real_node";
	public final static String NODE_TABLE = "node";
	public final static String RANK_TABLE = "rank";
	public final static String COMPONENT_TABLE = "component";
	public final static String NODE_ANNOTATION_TABLE = "node_annotation";
	public final static String EDGE_ANNOTATION_TABLE = "edge_annotation";

	// the wrapped node
	private AnnisNode node;
	
	// table aliases
	private Map<String, String> tableAliases;
	
	// aliased column names
	private Map<String, Map<String, String>> columnAliases;
	
	public TableAccessStrategy() {
		
	}
	
	public TableAccessStrategy(AnnisNode node) {
		this.node = node;
		this.tableAliases = new HashMap<String, String>();
		this.columnAliases = new HashMap<String, Map<String,String>>();
	}

	///// table and column aliases
	
	public String tableName(String table) {
		return tableAliases.containsKey(table) ? tableAliases.get(table) : table;
	}

	public String columnName(String table, String column) {
		if (columnAliases.containsKey(table)) {
			Map<String, String> columns = columnAliases.get(table);
			if (columns.containsKey(column)) {
				return columns.get(column);
			}
		}
		return column;
	}
	
	public String aliasedTable(String table, int count) {
		// sanity checks
		if (table.equals(NODE_ANNOTATION_TABLE) && count > node.getNodeAnnotations().size())
			throw new IllegalArgumentException("access to node annotation table out of range: " + count);
		if (table.equals(EDGE_ANNOTATION_TABLE) && count > node.getEdgeAnnotations().size())
			throw new IllegalArgumentException("access to edge annotation table out of range: " + count);
		if (table.equals(NODE_TABLE) && count > 1)
			throw new IllegalArgumentException("access to struct table out of range: " + count);
		if (table.equals(RANK_TABLE) && count > 1)
			throw new IllegalArgumentException("access to rank table out of range: " + count);
		
		// offset table count for edge annotations if node and edge annotations are the same table
		if (table.equals(EDGE_ANNOTATION_TABLE) && isMaterialized(EDGE_ANNOTATION_TABLE, NODE_ANNOTATION_TABLE))
			count = count + node.getNodeAnnotations().size() - 1;
		
		// compute table counts
		Bag tables = computeSourceTables();

		String aliasedName = tableName(table);
		String aliasCount = String.valueOf(node.getId());
		String countSuffix = tables.getCount(aliasedName) > 1 ? "_" + count : "";

		return aliasedName + aliasCount + countSuffix;
	}

	public String aliasedColumn(String table, String column) {
		return aliasedColumn(table, column, 1);
	}
	
	public String aliasedColumn(String table, String column, int count) {
		return column(aliasedTable(table, count), columnName(table, column));
	}
	
	protected String column(String table, String column) {
		return table + "." + column;
	}
	
	///// table usage
	
	protected Bag computeSourceTables() {
		Bag tables = new HashBag();
		
		tables.add(tableName(NODE_ANNOTATION_TABLE), node.getNodeAnnotations().size());
		tables.add(tableName(EDGE_ANNOTATION_TABLE), node.getEdgeAnnotations().size());
		
		if ( tables.getCount(tableName(RANK_TABLE)) == 0 && usesRankTable() )
			tables.add(tableName(RANK_TABLE));
		if ( tables.getCount(tableName(COMPONENT_TABLE)) == 0 && usesRankTable() )
			tables.add(tableName(COMPONENT_TABLE));
		
		if (tables.getCount(tableName(NODE_TABLE)) == 0)
			tables.add(tableName(NODE_TABLE));
		
		return tables;
	}
	
	public boolean usesNodeAnnotationTable() {
		return ! node.getNodeAnnotations().isEmpty();
	}
	
	public boolean usesRankTable() {
		return usesComponentTable() || node.isRoot() || node.getArity() != null;
	}
	
	public boolean usesComponentTable() {
		return node.isPartOfEdge() || usesEdgeAnnotationTable();
	}
	
	public boolean usesEdgeAnnotationTable() {
		return ! node.getEdgeAnnotations().isEmpty();
	}
	
	public boolean isMaterialized(String table, String otherTable) {
		return tableName(table).equals(tableName(otherTable));
	}
	
	///// delegates
	
	public void addTableAlias(String table, String alias) {
		tableAliases.put(table, alias);
	}
	
	public void addColumnAlias(String table, String column, String alias) {
		if ( ! columnAliases.containsKey(table) )
			columnAliases.put(table, new HashMap<String, String>());
		
		Map<String, String> aliases = columnAliases.get(table);
		aliases.put(column, alias);
	}
	
	///// Getter / Setter
	
	public AnnisNode getNode() {
		return node;
	}

	public void setNode(AnnisNode node) {
		this.node = node;
	}

	public Map<String, String> getTableAliases() {
		return tableAliases;
	}

	public void setTableAliases(Map<String, String> tableAliases) {
		this.tableAliases = tableAliases;
	}
	

	public Map<String, Map<String, String>> getColumnAliases() {
		return columnAliases;
	}

	public void setColumnAliases(Map<String, Map<String, String>> columnAliases) {
		this.columnAliases = columnAliases;
	}

}
