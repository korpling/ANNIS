package de.deutschdiachrondigital.dddquery.sql.model;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TestLiteral {

	@Test
	public void isJoinField() {
		assertThat(new Literal(null), instanceOf(JoinField.class));
	}
	
	@Test
	public void testToString() {
		assertThat(new Literal("foo").toString(), is("foo"));
	}
	
}
