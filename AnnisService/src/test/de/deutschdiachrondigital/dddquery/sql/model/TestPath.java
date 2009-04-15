package de.deutschdiachrondigital.dddquery.sql.model;

import static de.deutschdiachrondigital.dddquery.helper.IsCollection.isCollection;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import de.deutschdiachrondigital.dddquery.sql.model.conditions.Join;

public class TestPath {
	
	private Path path1;
	private Path path2;

	@Before
	public void setup() {
		path1 = new Path();
		path2 = new Path();
	}

	@Test
	public void mergeAliasSets() {
		AliasSet aliasSet1 = new AliasSet(1);
		path1.addAliasSet(aliasSet1);
		
		AliasSet aliasSet2 = new AliasSet(2);
		path2.addAliasSet(aliasSet2);
		
		path1.merge(path2);
		
		assertThat(path1.getAliasSets(), isCollection(aliasSet1, aliasSet2));
	}
	
	@Test
	public void mergeConditions() {
		Condition condition1 = Join.eq("1", "2");
		path1.addCondition(condition1);
		
		Condition condition2 = Join.ne("1", "2");
		path2.addCondition(condition2);
		
		path1.merge(path2);
		
		assertThat(path1.getConditions(), isCollection(condition1, condition2));
	}
	
	@Test
	public void mergeMarkings() {
		AliasSet aliasSet1 = new AliasSet(1);
		path1.markAliasSet(aliasSet1, "foo");
		
		AliasSet aliasSet2 = new AliasSet(2);
		path2.markAliasSet(aliasSet2, "bar");
		
		path1.merge(path2);
		
		Map<AliasSet, String> markings = path1.getMarkings();
		assertThat(markings.size(), is(2));
		assertThat(markings, hasEntry(aliasSet1, "foo"));
		assertThat(markings, hasEntry(aliasSet2, "bar"));
	}
	
}
