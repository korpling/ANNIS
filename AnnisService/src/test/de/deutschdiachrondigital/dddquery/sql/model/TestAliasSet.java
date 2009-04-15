package de.deutschdiachrondigital.dddquery.sql.model;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class TestAliasSet {

	@Test
	public void idIsSaved() {
		AliasSet aliasSet = new AliasSet(42);
		assertThat(aliasSet.getId(), is(42));
	}
	
	@Test
	public void getColumn() {
		AliasSet aliasSet = new AliasSet(1);
		Column column = aliasSet.getColumn("table", "column");
		assertThat(column.toString(), is("table1.column"));
	}
	
	@Test
	public void getColumnSavesTable() {
		AliasSet aliasSet = new AliasSet(1);
		aliasSet.getColumn("table", "column");
		
		Set<String>	expected = new HashSet<String>();
		expected.add("table");
		
		assertThat(aliasSet.getUsedTables(), is(expected));
	}
}
