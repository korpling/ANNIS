package annis.sqlgen;

import java.util.HashMap;
import java.util.Map;

import annis.dao.CorpusSelectionStrategy;
import annis.model.AnnisNode;

public class TableAccessStrategyFactory {

	// table aliases
	private Map<String, String> tableAliases;
	
	// column aliases
	private Map<String, Map<String, String>> columnAliases;
	
	public TableAccessStrategyFactory() {
		tableAliases = new HashMap<String, String>();
		columnAliases = new HashMap<String, Map<String,String>>();
	}
	
	public TableAccessStrategy createTableAccessStrategy(AnnisNode node, CorpusSelectionStrategy corpusSelectionStrategy) {
		TableAccessStrategy tableAccessStrategy = new TableAccessStrategy(node, corpusSelectionStrategy, tableAliases, columnAliases);
		return tableAccessStrategy;
	}

	///// Delegates
	
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
