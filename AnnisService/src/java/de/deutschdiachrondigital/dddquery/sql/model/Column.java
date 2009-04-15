package de.deutschdiachrondigital.dddquery.sql.model;



public class Column implements JoinField {

	private AliasSet aliasSet;
	private String table;
	private String column;
	
	public Column(AliasSet aliasSet, String table, String column) {
		this.aliasSet = aliasSet;
		this.table = table;
		this.column = column;
		aliasSet.useTable(table); // FIXME Test
	}
	
	@Override
	public String toString() {
		return table + aliasSet.getId() + "." + column;
	}
	
	@Override
	public boolean equals(Object obj) {
		return toString().equals(obj.toString());
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

}
