package de.deutschdiachrondigital.dddquery.sql.model;

import java.util.HashSet;
import java.util.Set;



public class AliasSet {
	
	int id;
	Set<String> usedTables;
	
	public AliasSet(int id) {
		this.id = id;
		usedTables = new HashSet<String>();
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( ! (obj instanceof AliasSet) )
			return false;
		AliasSet a = (AliasSet) obj;
		return id == a.id;
	}
	
	@Override
	public int hashCode() {
		return id;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Column getColumn(String table, String column) {
		useTable(table);
		return new Column(this, table, column);
	}

	public void useTable(String table) {
		usedTables.add(table);
	}
	
	@Override
	public String toString() {
		return "AliasSet(" + id + ")";
	}

	public Set<String> getUsedTables() {
		return usedTables;
	}

	public void setUsedTables(Set<String> usedTables) {
		this.usedTables = usedTables;
	}

	public String getTable(String table) {
		return table + id;
	}

	public boolean usesTable(String table) {
		return usedTables.contains(table);
	}
	
}
