package de.deutschdiachrondigital.dddquery.sql.old;

import static de.deutschdiachrondigital.dddquery.helper.IsCollectionContainingSubTypes.containsItem;
import static de.deutschdiachrondigital.dddquery.helper.IsCollectionEmpty.empty;
import static de.deutschdiachrondigital.dddquery.helper.IsCollectionSize.size;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import de.deutschdiachrondigital.dddquery.sql.model.AliasSet;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.Join;

public class TestPathQuery {

	PathQuery path;
	
	@Before
	public void setup() {
		path = new PathQuery();
	}
	
	// an empty query has no further conditions
	@Test
	public void emptyQueryNoConditions() {
		assertThat(path.getConditions(), empty());
	}
	
	/**
	 * Preconditions:
	 * - a valid context alias set
	 * - a valid target alias set (different from above)
	 * - alias set list contains context and target
	 * Results:
	 * - old target saved as context
	 * - a new alias set as target (with id 3)
	 * - alias set list contains old context, target and new target
	 */
	@Test
	public void newAliasSet() {
		
		// create old context and target alias sets
		AliasSet oldContext = new AliasSet(1);
		AliasSet oldTarget = new AliasSet(2);
		path.setContextAliasSet(oldContext);
		path.setTargetAliasSet(oldTarget);
		path.getAliasSets().addAll(Arrays.asList(new AliasSet[] { oldContext, oldTarget } ));
		
		AliasSet newTarget = path.newAliasSet();
		
		assertThat(path.getContextAliasSet(), sameInstance(oldTarget));
		assertThat(path.getTargetAliasSet(), sameInstance(newTarget));
		assertThat(newTarget.getId(), is(3));
		assertThat(path.getAliasSets(), is(Arrays.asList(new AliasSet[] { oldContext, oldTarget, newTarget })));
	}	

	@Test
	public void markTargetAliasSet() {
		AliasSet target = path.newAliasSet();
		path.markTargetAliasSet("foo");
		assertThat(path.getMarkings(), hasEntry(target, "foo"));
	}
	
	@Test
	public void markTargetAliasSetNullMarker() {
		AliasSet target = path.newAliasSet();
		path.markTargetAliasSet(null);
		assertThat(path.getMarkings(), hasEntry(target, ""));
	}
	
//	@Test
	public void mergeAliasSets() {
		AliasSet aliasSet1_1 = new AliasSet(1);
		AliasSet aliasSet1_2 = new AliasSet(2);
		path.setAliasSets(new ArrayList<AliasSet>(Arrays.asList(new AliasSet[] { aliasSet1_1, aliasSet1_2 } )));
		
		AliasSet aliasSet2_1 = aliasSet1_1;
		AliasSet aliasSet2_2 = new AliasSet(2);
		AliasSet aliasSet2_3 = new AliasSet(3);
		PathQuery toMerge = new PathQuery();
		toMerge.setAliasSets(Arrays.asList(new AliasSet[] { aliasSet2_1, aliasSet2_2, aliasSet2_3 } ));
		
		path.merge(toMerge);
		
		assertThat(path.getAliasSets(), is(Arrays.asList( new AliasSet[] {
			aliasSet1_1, aliasSet1_2, aliasSet2_2, aliasSet2_3
		})));
		assertThat(aliasSet2_2.getId(), is(3));
		assertThat(aliasSet2_3.getId(), is(4));
	}
	
//	@Test
	public void mergeConditions() {
		AliasSet aliasSet1 = new AliasSet(1);
		path.setAliasSets(new ArrayList<AliasSet>(Arrays.asList(new AliasSet[] { aliasSet1 } )));
		path.addCondition(Join.eq(aliasSet1.getColumn("table", "column"), "'foo'"));
		
		AliasSet aliasSet2 = new AliasSet(1);
		PathQuery toMerge = new PathQuery();
		toMerge.setAliasSets(Arrays.asList(new AliasSet[] { aliasSet2 } ));
		toMerge.addCondition(Join.eq(aliasSet2.getColumn("another_table", "column"), "'bar'"));
		
		path.merge(toMerge);
		
		assertThat(path.getConditions(), size(2));
		assertThat(path.getConditions(), containsItem(Join.eq("table1.column", "'foo'")));
		assertThat(path.getConditions(), containsItem(Join.eq("another_table2.column", "'bar'")));
	}
	
	@Test
	public void mergeMarkings() {
		path.newAliasSet();
		PathQuery toMerge = new PathQuery();
		AliasSet aliasSet = toMerge.newAliasSet();
		toMerge.markTargetAliasSet("foo");
		path.merge(toMerge);
		assertThat(path.getMarkings(), hasEntry(aliasSet, "foo"));
	}
	
	@Test
	public void mergeMarkingsDontOverwriteExistingMarks() {
		AliasSet target = path.newAliasSet();
		path.markTargetAliasSet("foo");
		
		PathQuery toMerge = new PathQuery(target);
		toMerge.markTargetAliasSet("bar");
		
		path.merge(toMerge);
		
		assertThat(path.getMarkings(), hasEntry(target, "foo"));
	}
}
