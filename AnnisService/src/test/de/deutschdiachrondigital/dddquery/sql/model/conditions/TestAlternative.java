package de.deutschdiachrondigital.dddquery.sql.model.conditions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class TestAlternative {
	
	private Alternative alternative;

	@Before
	public void setUp() {
		alternative = new Alternative();
	}

	@Test(expected=RuntimeException.class)
	public void sqlStringNoConditions() {
		alternative.sqlString();
	}
	
//	@Test FIXME
	public void sqlStringOneCondition() {
		alternative.addCondition(Join.eq("1", "2"));
		assertThat(alternative.sqlString(), is("( 1 = 2 )"));
	}
	
//	@Test FIXME
	public void sqlStringManyConditions() {
		alternative.addCondition(Join.eq("1", "2"));
		alternative.addCondition(Join.eq("2", "3"));
		alternative.addCondition(Join.eq("3", "4"));
		assertThat(alternative.sqlString(), is("( 1 = 2 OR 2 = 3 OR 3 = 4 )"));
	}
	
	@Test
	public void equalsTrue() {
		// empty alternative
		assertThat(alternative, is(new Alternative()));
		
		// same conditions
		alternative.addCondition(Join.eq("1", "1"));
		
		Alternative alternative2 = new Alternative();
		alternative2.addCondition(Join.eq("1", "1"));
		
		assertThat(alternative, is(alternative2));
	}
	
	@Test
	public void equalsFalse() {
		// empty, but different operator
		CompoundCondition someCondition = new CompoundCondition() {

			@Override
			protected String operator() {
				return "FOO";
			}
			
		};
		assertThat(alternative, not(is(someCondition)));

		// different conditions
		alternative.addCondition(Join.eq("1", "1"));
		
		Alternative alternative2 = new Alternative();
		alternative2.addCondition(Join.eq("2", "2"));
		
		assertThat(alternative, not(is(alternative2)));
	}
	
}
