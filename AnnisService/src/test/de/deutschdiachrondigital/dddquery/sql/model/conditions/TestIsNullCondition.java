package de.deutschdiachrondigital.dddquery.sql.model.conditions;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.deutschdiachrondigital.dddquery.sql.model.Condition;

public class TestIsNullCondition {

	@Test
	public void toSql() {
		Condition condition = new IsNullCondition("some.field");
		assertThat(condition.toString(), equalTo("some.field IS NULL"));
	}
	
}
