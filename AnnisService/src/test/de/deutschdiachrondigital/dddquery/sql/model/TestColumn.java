package de.deutschdiachrondigital.dddquery.sql.model;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TestColumn {

	@Test
	public void testToString() {
		AliasSet aliasSet = new AliasSet(1);
		Column column = new Column(aliasSet, "table", "column");
		assertThat(column.toString(), is("table1.column"));
		
		aliasSet.setId(2);
		assertThat(column.toString(), is("table2.column"));
	}
	
	@Test
	public void isJoinField() {
		assertThat(new Column(new AliasSet(1), null, null), instanceOf(JoinField.class));
	}
	
}
