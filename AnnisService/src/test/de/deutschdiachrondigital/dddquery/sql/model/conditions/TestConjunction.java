package de.deutschdiachrondigital.dddquery.sql.model.conditions;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class TestConjunction {

	Conjunction conjunction;

	@Before
	public void setUp() {
		conjunction = new Conjunction();
	}
	
	@Test(expected=RuntimeException.class)
	public void sqlStringNoConditions() {
		conjunction.sqlString();
	}
	
//	@Test FIXME
	public void sqlStringOneCondition() {
		conjunction.addCondition(Join.eq("1", "2"));
		assertThat(conjunction.sqlString(), is("( 1 = 2 )"));
	}
	
//	@Test FIXME
	public void sqlStringManyConditions() {
		conjunction.addCondition(Join.eq("1", "2"));
		conjunction.addCondition(Join.eq("2", "3"));
		conjunction.addCondition(Join.eq("3", "4"));
		assertThat(conjunction.sqlString(), is("( 1 = 2 AND 2 = 3 AND 3 = 4 )"));
	}
	
}
