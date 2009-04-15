package de.deutschdiachrondigital.dddquery.sql.model.conditions;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TestJoin {

	@Test
	public void eq() {
		Join join = Join.eq("1", "2");
		assertThat(join.toString(), equalTo("1 = 2"));
	}
	
	@Test
	public void lt() {
		Join join = Join.lt("1", "2");
		assertThat(join.toString(), equalTo("1 < 2"));
	}
	
	@Test
	public void le() {
		Join join = Join.le("1", "2");
		assertThat(join.toString(), equalTo("1 <= 2"));
	}
	
	@Test
	public void gt() {
		Join join = Join.gt("1", "2");
		assertThat(join.toString(), equalTo("1 > 2"));
	}
	
	@Test
	public void ge() {
		Join join = Join.ge("1", "2");
		assertThat(join.toString(), equalTo("1 >= 2"));
	}
	
}
