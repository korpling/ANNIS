/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.sqlgen;

import annis.model.QueryAnnotation;
import annis.model.QueryNode;
import static annis.sqlgen.TableAccessStrategy.COMPONENT_TABLE;
import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.Bag;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import static org.hamcrest.Matchers.is;
import org.hamcrest.TypeSafeMatcher;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TestTableAccessStrategy {

	private Map<String, Integer> expected;
	
	private TableAccessStrategy tableAccessStrategy;
	
	@Mock private QueryNode node23;
	@Mock private Set<QueryAnnotation> annotations;
//	// TODO: refactor into ViewConstrainedTableAccessStrategy
//	@Mock private CorpusSelectionStrategy corpusSelectionStrategy;

	@Before
	public void setup() {
		initMocks(this);
		when(node23.getId()).thenReturn(23L);
		when(annotations.size()).thenReturn(3);
//		// TODO: refactor into ViewConstrainedTableAccessStrategy
//		when(corpusSelectionStrategy.viewName(anyString())).thenAnswer(new Answer<String>() {
//
//			// return string unchanged
//			public String answer(InvocationOnMock invocation) throws Throwable {
//				return (String) invocation.getArguments()[0];
//			}
//			
//		});
//		tableAccessStrategy = new TableAccessStrategy(node23, corpusSelectionStrategy);

		tableAccessStrategy = new TableAccessStrategy(node23);

		expected = new HashMap<>();
		expected.put(NODE_TABLE, 0);
		expected.put(RANK_TABLE, 0);
		expected.put(COMPONENT_TABLE, 0);
		expected.put(NODE_ANNOTATION_TABLE, 0);
		expected.put(EDGE_ANNOTATION_TABLE, 0);
	}
	
	///// used source tables
	
	// a fresh node always uses the struct table
	@Test
	public void computeSourceTablesFreshNode() {
		expectTableCount(NODE_TABLE, 1);
		assertUsedTables();
	}
	
	// root node uses rank table
	@Test
	public void computeSourceTablesRootNode() {
		when(node23.isRoot()).thenReturn(true);
		expectTableCount(NODE_TABLE, 1);
		expectTableCount(RANK_TABLE, 1);
		expectTableCount(COMPONENT_TABLE, 1);
		assertUsedTables();
	}
	
	// if rank and component table are materialized, root node only uses struct and rank table
	@Test
	public void computeSourceTablesRootNodeComponentAliasedToRank() {
		when(node23.isRoot()).thenReturn(true);
		tableAccessStrategy.addTableAlias(COMPONENT_TABLE, RANK_TABLE);
		expectTableCount(NODE_TABLE, 1);
		expectTableCount(RANK_TABLE, 1);
		assertUsedTables();
	}
	
	// if rank, component and struct table are materialized, root node only uses struct table
	@Test
	public void computeSourceTablesRootNodeRankAndComponentAliasedToStruct() {
		when(node23.isRoot()).thenReturn(true);
		tableAccessStrategy.addTableAlias(COMPONENT_TABLE, NODE_TABLE);
		tableAccessStrategy.addTableAlias(RANK_TABLE, NODE_TABLE);
		expectTableCount(NODE_TABLE, 1);
		assertUsedTables();
	}
	
	// root node uses rank and component table
	@Test
	public void computeSourceTablesPartOfEdge() {
		when(node23.isPartOfEdge()).thenReturn(true);
		expectTableCount(NODE_TABLE, 1);
		expectTableCount(RANK_TABLE, 1);
		expectTableCount(COMPONENT_TABLE, 1);
		assertUsedTables();
	}
	
	// if rank, component and struct table are materialized, root node only uses struct table
	@Test
	public void computeSourceTablesPartOfEdgeRankAliasedToStruct() {
		when(node23.isPartOfEdge()).thenReturn(true);
		tableAccessStrategy.addTableAlias(RANK_TABLE, NODE_TABLE);
		tableAccessStrategy.addTableAlias(COMPONENT_TABLE, NODE_TABLE);
		expectTableCount(NODE_TABLE, 1);
		assertUsedTables();
	}
	
	// join node annotations
	@Test
	public void computeSourceTablesNodeAnnotations() {
		when(node23.getNodeAnnotations()).thenReturn(annotations);
		expectTableCount(NODE_TABLE, 1);
		expectTableCount(NODE_ANNOTATION_TABLE, 3);
		assertUsedTables();
	}

	// if NODE_ANNOTATION_TABLE and STRUCT_TABLE are materialized, only use as many tables as there are annotations
	@Test
	public void computeSourceTablesNodeAnnotationsAliasedToStruct() {
		when(node23.getNodeAnnotations()).thenReturn(annotations);
		tableAccessStrategy.addTableAlias(NODE_ANNOTATION_TABLE, NODE_TABLE);
		expectTableCount(NODE_TABLE, 3);
		assertUsedTables();
	}
	
	// join edge annotations
	@Test
	public void computeSourceTablesEdgeAnnotations() {
		when(node23.getEdgeAnnotations()).thenReturn(annotations);
		expectTableCount(NODE_TABLE, 1);
		expectTableCount(RANK_TABLE, 1);
		expectTableCount(COMPONENT_TABLE, 1);
		expectTableCount(EDGE_ANNOTATION_TABLE, 3);
		assertUsedTables();
	}

	// if edge_annotation, component and rank are materialized, only use as many tables as there are annotations
	@Test
	public void computeSourceTablesEdgeAnnotationsAliasedToRank() {
		when(node23.getEdgeAnnotations()).thenReturn(annotations);
		tableAccessStrategy.addTableAlias(COMPONENT_TABLE, RANK_TABLE);
		tableAccessStrategy.addTableAlias(EDGE_ANNOTATION_TABLE, RANK_TABLE);
		expectTableCount(NODE_TABLE, 1);
		expectTableCount(RANK_TABLE, 3);
		assertUsedTables();
	}
	
	// if edge_annotation, rank, component and struct are materialized, only use as many tables as there are annotations
	@Test
	public void computeSourceTablesEdgeAnnotationsAliasedToStruct() {
		when(node23.getEdgeAnnotations()).thenReturn(annotations);
		tableAccessStrategy.addTableAlias(COMPONENT_TABLE, NODE_TABLE);
		tableAccessStrategy.addTableAlias(RANK_TABLE, NODE_TABLE);
		tableAccessStrategy.addTableAlias(EDGE_ANNOTATION_TABLE, NODE_TABLE);
		expectTableCount(NODE_TABLE, 3);
		assertUsedTables();
	}
	
	// all tables are materialized, use as many tables as there are annotations
	@Test
	public void computeSourceTablesNodeAndEdgeAnnotationsAliasedToStruct() {
		when(node23.isRoot()).thenReturn(true);
		when(node23.isPartOfEdge()).thenReturn(true);
		when(node23.getNodeAnnotations()).thenReturn(annotations);
		when(node23.getEdgeAnnotations()).thenReturn(annotations);
		tableAccessStrategy.addTableAlias(RANK_TABLE, NODE_TABLE);
		tableAccessStrategy.addTableAlias(COMPONENT_TABLE, NODE_TABLE);
		tableAccessStrategy.addTableAlias(EDGE_ANNOTATION_TABLE, NODE_TABLE);
		tableAccessStrategy.addTableAlias(NODE_ANNOTATION_TABLE, NODE_TABLE);
		expectTableCount(NODE_TABLE, 6);
		assertUsedTables();
	}
	
	///// table and column names

	// table with default name
	@Test
	public void tableName() {
		assertEquals("foo", tableAccessStrategy.tableName("foo"));
	}
	
	// table with mapped name (materialization)
	@Test
	public void tableNameAliased() {
		tableAccessStrategy.addTableAlias("foo", "FOO");
		assertEquals("FOO", tableAccessStrategy.tableName("foo"));
	}
	
	// column with default name
	@Test
	public void columnName() {
		assertEquals("bar", tableAccessStrategy.columnName("foo", "bar"));
	}
	
	// column with mapped name
	@Test
	public void columnNameAliased() {
		tableAccessStrategy.addColumnAlias("foo", "bar", "BAR");
		assertEquals("BAR", tableAccessStrategy.columnName("foo", "bar"));
	}
	
	///// table and column aliases
	
	// only use one struct table
	@Test
	public void aliasedTableStruct() {
		assertSingleTable(NODE_TABLE);
	}

	// use one table per node annotation
	@Test
	public void aliasedTableNodeAnnotations() {
		when(node23.getNodeAnnotations()).thenReturn(annotations);
		assertMultipleTables(NODE_ANNOTATION_TABLE, 3);
	}
	
	// use one table per edge annotation
	@Test
	public void aliasedTableEdgeAnnotations() {
		when(node23.getEdgeAnnotations()).thenReturn(annotations);
		assertMultipleTables(EDGE_ANNOTATION_TABLE, 3);
	}
	
	// node and edge annotations use different table
	@Test
	public void aliasedTableNodeAndEdgeAnnotationsDifferentTable() {
		when(node23.getNodeAnnotations()).thenReturn(annotations);
		when(node23.getEdgeAnnotations()).thenReturn(annotations);
		assertMultipleTables(NODE_ANNOTATION_TABLE, 3);
		assertMultipleTables(EDGE_ANNOTATION_TABLE, 3);
	}

	// node and edge annotations use same table, alias first node annotations then edge annotations
	@SuppressWarnings("unchecked")
	@Test
	public void aliasedTableNodeAndEdgeAnnotationsSameTable() {
		final int NODE_ANNOTATION_COUNT = 2;
		Set<QueryAnnotation> nodeAnnotations = mock(Set.class);
		when(nodeAnnotations.size()).thenReturn(NODE_ANNOTATION_COUNT);
		when(node23.getNodeAnnotations()).thenReturn(nodeAnnotations);
		
		final int EDGE_ANNOTATION_COUNT = 3;
		Set<QueryAnnotation> edgeAnnotations = mock(Set.class);
		when(edgeAnnotations.size()).thenReturn(EDGE_ANNOTATION_COUNT);
		when(node23.getEdgeAnnotations()).thenReturn(edgeAnnotations);

		tableAccessStrategy.addTableAlias(NODE_ANNOTATION_TABLE, NODE_TABLE);
		tableAccessStrategy.addTableAlias(EDGE_ANNOTATION_TABLE, NODE_TABLE);
		
		assertMultipleAliasedTables(NODE_ANNOTATION_TABLE, NODE_ANNOTATION_COUNT, NODE_TABLE, 0);
		assertMultipleAliasedTables(EDGE_ANNOTATION_TABLE, EDGE_ANNOTATION_COUNT, NODE_TABLE, NODE_ANNOTATION_COUNT - 1);
	}
	
	// only one struct table can be used
	@Test(expected=IllegalArgumentException.class)
	public void onlyOneStructTable() {
		tableAccessStrategy.aliasedTable(NODE_TABLE, 2);
	}

	// only one rank table can be used
	@Test(expected=IllegalArgumentException.class)
	public void onlyOneRankTable() {
		tableAccessStrategy.aliasedTable(RANK_TABLE, 2);
	}

	
	// throw exception if trying to access a table for an unknown edge annotation
	@Test(expected=IllegalArgumentException.class)
	public void edgeAnnotationsUnknownAnnotation() {
		when(node23.getEdgeAnnotations()).thenReturn(annotations);
		tableAccessStrategy.aliasedTable(EDGE_ANNOTATION_TABLE, 4);
	}
	
	// column alias for a table that is only used once
	@Test
	public void aliasedColumn() {
		tableAccessStrategy.addColumnAlias(NODE_TABLE, "bar", "BAR");
		assertEquals(NODE_TABLE + "23.BAR", tableAccessStrategy.aliasedColumn(NODE_TABLE, "bar"));
	}
	
	// column alias for a table that is used many times
	@Test
	public void aliasedColumnManyTables() {
		when(node23.getNodeAnnotations()).thenReturn(annotations);
		tableAccessStrategy.addTableAlias(NODE_ANNOTATION_TABLE, NODE_TABLE);
		tableAccessStrategy.addColumnAlias(NODE_TABLE, "foo", "FOO");
		tableAccessStrategy.addColumnAlias(NODE_ANNOTATION_TABLE, "bar", "BAR");
		assertEquals(NODE_TABLE + "23_1.FOO", tableAccessStrategy.aliasedColumn(NODE_TABLE, "foo"));
		assertEquals(NODE_TABLE + "23_1.BAR", tableAccessStrategy.aliasedColumn(NODE_ANNOTATION_TABLE, "bar", 1));
		assertEquals(NODE_TABLE + "23_2.BAR", tableAccessStrategy.aliasedColumn(NODE_ANNOTATION_TABLE, "bar", 2));
		assertEquals(NODE_TABLE + "23_3.BAR", tableAccessStrategy.aliasedColumn(NODE_ANNOTATION_TABLE, "bar", 3));
	}
	
	///// table usage
	
	// fresh node uses no table (except struct)
	@Test
	public void usedTablesFreshNode() {
		assertThat(tableAccessStrategy.usesEdgeAnnotationTable(), is(false));
		assertThat(tableAccessStrategy.usesNodeAnnotationTable(), is(false));
	}
	
	// if node has an edge, use rank table
	@Test
	public void usesRankTableEdge() {
		when(node23.isPartOfEdge()).thenReturn(true);
		assertThat(tableAccessStrategy.usesRankTable(), is(true));
	}
	
	// if node is root, use rank table
	@Test
	public void usesRankTableRoot() {
		when(node23.isRoot()).thenReturn(true);
		assertThat(tableAccessStrategy.usesRankTable(), is(true));
	}
	
	// if node has edge annotations, use rank table and edge annotation table
	@Test
	public void usedTableEdgeAnnotation() {
		when(node23.getEdgeAnnotations()).thenReturn(annotations);
		assertThat(tableAccessStrategy.usesRankTable(), is(true));
		assertThat(tableAccessStrategy.usesEdgeAnnotationTable(), is(true));
	}
	
	// if node has node annotations, use node annotation table
	@Test
	public void usedTableNodeAnnotation() {
		when(node23.getNodeAnnotations()).thenReturn(annotations);
		assertThat(tableAccessStrategy.usesNodeAnnotationTable(), is(true));
	}
	
	// join of two tables is materialized
	@Test
	public void isMaterializedTrue() {
		tableAccessStrategy.addTableAlias(RANK_TABLE, NODE_TABLE);
		assertThat(tableAccessStrategy.isMaterialized(RANK_TABLE, NODE_TABLE), is(true));
	}
	
	// join of two tables is not materialized
	@Test
	public void isMaterializedFalse() {
		assertThat(tableAccessStrategy.isMaterialized(RANK_TABLE, NODE_TABLE), is(false));
	}
	
//	// TODO: refactor into ViewConstrainedTableAccessStrategy
//	// use view name if corpus selection is done through views
//	@Test
//	public void tableNameUsesView() {
//		final String STRUCT_TABLE_VIEW = "STRUCT_TABLE_VIEW";
//		when(corpusSelectionStrategy.viewName(NODE_TABLE)).thenReturn(STRUCT_TABLE_VIEW);
//		assertThat(tableAccessStrategy.tableName(NODE_TABLE), is(STRUCT_TABLE_VIEW));
//	}
	
//	// TODO: refactor into ViewConstrainedTableAccessStrategy
//	// access to the aliased name, not the view
//	@Test
//	public void tableNameDontUseView() {
//		final String STRUCT_TABLE_VIEW = "STRUCT_TABLE_VIEW";
//		when(corpusSelectionStrategy.viewName(NODE_TABLE)).thenReturn(STRUCT_TABLE_VIEW);
//		assertThat(tableAccessStrategy.tableName(NODE_TABLE, false), is(NODE_TABLE));
//	}
	
	///// private helper

	private void expectTableCount(String table, int count) {
		expected.put(table, count);
	}
	
	private void assertUsedTables() {
		assertThat(tableAccessStrategy.computeSourceTables(), hasTables(expected));
	}
	
	private void assertSingleTable(String table) {
		assertThat(tableAccessStrategy.aliasedTable(table, 1), is(table + node23.getId()));
	}
	
	private void assertMultipleTables(String table, int count) {
		for (int i = 1; i <= count; ++i)
			assertThat(tableAccessStrategy.aliasedTable(table, i), is(table + node23.getId() + "_" + i));
	}
	
	private void assertMultipleAliasedTables(String table, int count, String alias, int offset) {
		for (int i = 2; i <= count; ++i)
			assertThat(tableAccessStrategy.aliasedTable(table, i), is(alias + node23.getId() + "_" + (i + offset)));
	}

	private Matcher<Bag> hasTables(final Map<String, Integer> expectedTables) {
		return new TypeSafeMatcher<Bag>() {

			@Override
			public boolean matchesSafely(Bag item) {
				for (String table : expectedTables.keySet()) {
					if (item.getCount(table) != expectedTables.get(table))
						return false;
				}
				return true;
			}

			public void describeTo(Description description) {
				description.appendValue(expectedTables);
			}
			
		};
	}
	
}
