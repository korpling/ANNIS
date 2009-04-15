package de.deutschdiachrondigital.dddquery.sql;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import de.deutschdiachrondigital.dddquery.sql.model.AliasSet;

public class TestAliasSetProvider {

	private AliasSetProvider provider;

	@Before
	public void setup() {
		provider = new AliasSetProvider();
	}
	
	@Test
	public void getAliasSet() {
		AliasSet aliasSet1 = provider.getAliasSet();
		assertThat(aliasSet1, is(not(nullValue())));
		assertThat(aliasSet1.getId(), is(1));
	}
	
	@Test
	public void getAliasSetNewId() {
		AliasSet aliasSet1 = provider.getAliasSet();
		AliasSet aliasSet2 = provider.getAliasSet();
		assertThat(aliasSet2.getId(), is(2));
		assertThat(aliasSet1, is(not(sameInstance(aliasSet2))));
	}
	
	@Test
	public void getAliasSetBound() {
		AliasSet aliasSet1 = provider.getAliasSet("foo");
		AliasSet aliasSet2 = provider.getAliasSet("foo");
		assertThat(aliasSet2.getId(), is(1));
		assertThat(aliasSet1, is(sameInstance(aliasSet2)));
	}
	
	@Test
	public void isBoundUnknown() {
		assertThat(provider.isBound("foo"), is(false));
	}
	
	@Test
	public void isBoundKnown() {
		provider.getAliasSet("foo");
		assertThat(provider.isBound("foo"), is(true));
	}
	
	@Test
	public void startUnionPriorAliasSetsUnchanged() {
		AliasSet global = provider.getAliasSet("global");
		provider.startUnion();
		assertThat(provider.getAliasSet("global"), is(sameInstance(global)));
	}
	
	@Test
	public void newAlternativePriorAliasSetsUnchanged() {
		AliasSet global = provider.getAliasSet("global");
		provider.startUnion();
		provider.newAlternative();
		assertThat(provider.getAliasSet("global"), is(sameInstance(global)));
	}
	
	@Test
	public void newAlternativeNewAliasSetsDifferent() {
		provider.startUnion();
		AliasSet alternative1 = provider.getAliasSet("local");
		provider.newAlternative();
		AliasSet alternative2 = provider.getAliasSet("local");
		provider.newAlternative();
		AliasSet alternative3 = provider.getAliasSet("local");
		assertThat(alternative1, is(not(sameInstance(alternative2))));
		assertThat(alternative2, is(not(sameInstance(alternative3))));
	}
	
	@Test
	public void endUnionNewAliasSetsDifferent() {
		provider.startUnion();
		AliasSet alternative1 = provider.getAliasSet("local");
		provider.endUnion();
		AliasSet alternative2 = provider.getAliasSet("local");
		assertThat(alternative1, is(not(sameInstance(alternative2))));
	}
	
//	@Test(expected=RuntimeException.class)
	public void startUnionMustBeCalledBeforeNewAlternativeBad() {
		provider.newAlternative();
	}
	
	@Test
	public void startUnionMustBeCalledBeforeNewAlternativeGood() {
		provider.startUnion();
		provider.newAlternative();
	}
	
	@Test(expected=RuntimeException.class)
	public void startUnionMustBeCalledBeforeEndUnionBad() {
		provider.endUnion();
	}
	
	@Test
	public void startUnionMustBeCalledBeforeEndUnionGood() {
		provider.startUnion();
		provider.endUnion();
	}
	
	@Test(expected=RuntimeException.class)
	public void startEndUnionCallsMustBeProperlyNestedBad() {
		provider.startUnion();
		provider.endUnion();
		provider.endUnion();
	}

	@Test
	public void startEndUnionCallsMustBeProperlyNestedGood() {
		provider.startUnion();
		provider.startUnion();
		provider.endUnion();
		provider.endUnion();
	}

}
