
package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.EDGE_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import annis.dao.BaseCorpusSelectionStrategy;
import annis.model.AnnisNode;
import annis.model.Annotation;

public class TestTableJoinsInWhereClauseNodeSqlAdapter {

	// an example node
	private AnnisNode node23;

	// object under test: the adapter to that node
	private TableJoinsInWhereClauseNodeSqlAdapter adapter23;

	@Mock private BaseCorpusSelectionStrategy corpusSelectionStrategy;
	private TableAccessStrategyFactory tableAccessStrategyFactory;
	
	// a few distinct annotations
	private static final Annotation annotation1 = new Annotation("namespace", "name1");
	private static final Annotation annotation2 = new Annotation("namespace", "name2");
	private static final Annotation annotation3 = new Annotation("namespace", "name3");
	private Set<Annotation> annotations;
	
	@Before
	public void setup() {
		initMocks(this);
		node23 = new AnnisNode(23);
	
		tableAccessStrategyFactory = new TableAccessStrategyFactory();
		
		adapter23 = new TableJoinsInWhereClauseNodeSqlAdapter();
		adapter23.setNode(node23);
		adapter23.setCorpusSelectionStrategy(corpusSelectionStrategy);
		adapter23.setTableAccessStrategyFactory(tableAccessStrategyFactory);

		// add column aliases to make sure they are used
		tableAccessStrategyFactory.addTableAlias(NODE_TABLE, "_node");
		tableAccessStrategyFactory.addTableAlias(EDGE_TABLE, "_rank");
		tableAccessStrategyFactory.addTableAlias(NODE_ANNOTATION_TABLE, "_annotation");
		tableAccessStrategyFactory.addTableAlias(EDGE_ANNOTATION_TABLE, "_rank_annotation");
		
		annotations = annotationSet(annotation1, annotation2, annotation3); 

		// view name = table name
		when(corpusSelectionStrategy.viewName(anyString())).thenAnswer(new Answer<String>() {

			// return string unchanged
			public String answer(InvocationOnMock invocation) throws Throwable {
				return (String) invocation.getArguments()[0];
			}
			
		});
	}
	
	// every node uses the node table
	@Test
	public void fromClauseDefault() {
		String expected = "_node AS _node23";
		assertEquals(expected, adapter23.fromClause());
	}

	// if the node is part of a dominance join, use the rank table
	@Test
	public void fromClauseUsesRank() {
		node23.setRoot(true);

		String expected = 
			"_node AS _node23, _rank AS _rank23";		
		assertEquals(expected, adapter23.fromClause());
		checkWhereCondition("_rank23.node_ref = _node23.id");
	}
	
	// don't join rank table if the join is materialized
	@Test
	public void fromClauseUsesRankAliasedToStruct() {
		node23.setRoot(true);
		tableAccessStrategyFactory.addTableAlias(EDGE_TABLE, "_node");

		String expected = "_node AS _node23";
		assertEquals(expected, adapter23.fromClause());
	}
	
	// join the annotation table for each node annotation
	@Test
	public void fromClauseNodeAnnotations() {
		node23.setNodeAnnotations(annotations);
		
		String expected = "_node AS _node23, _annotation AS _annotation23_1, _annotation AS _annotation23_2, _annotation AS _annotation23_3"; 
		assertEquals(expected, adapter23.fromClause());
		checkWhereCondition(
				"_annotation23_1.node_ref = _node23.id",
				"_annotation23_2.node_ref = _node23.id",
				"_annotation23_3.node_ref = _node23.id"
		);
	}
	
	// join annotation table for each node annotation, use node table for first annotation if they are the same
	@Test
	public void fromClauseNodeAnnotationsAliasedToStruct() {
		node23.setNodeAnnotations(annotations);
		tableAccessStrategyFactory.addTableAlias(NODE_ANNOTATION_TABLE, "_node");
		tableAccessStrategyFactory.addColumnAlias(NODE_ANNOTATION_TABLE, "node_ref", "id");
		
		String expected = "_node AS _node23_1, _node AS _node23_2, _node AS _node23_3";
		assertEquals(expected, adapter23.fromClause());
		checkWhereCondition(
				"_node23_2.id = _node23_1.id",
				"_node23_3.id = _node23_1.id"
		);
	}
	
	// join the rank table once and the edge annotation table for each edge annotation
	@Test
	public void fromClauseOfEdgeAnnotations() {
		node23.setEdgeAnnotations(annotations);
		
		String expected = "_node AS _node23, _rank AS _rank23, _rank_annotation AS _rank_annotation23_1, _rank_annotation AS _rank_annotation23_2, _rank_annotation AS _rank_annotation23_3"; 
		assertEquals(expected, adapter23.fromClause());
		checkWhereCondition(
				"_rank23.node_ref = _node23.id",
				"_rank_annotation23_2.rank_ref = _rank23.pre",
				"_rank_annotation23_2.rank_ref = _rank23.pre",
				"_rank_annotation23_2.rank_ref = _rank23.pre"
		);
	}
	
	// join annotation table for each edge annotation, use rank table for first annotation if they are the same
	@Test
	public void fromClauseEdgeAnnotationsAliasedToStruct() {
		node23.setEdgeAnnotations(annotations);
		tableAccessStrategyFactory.addTableAlias(EDGE_ANNOTATION_TABLE, "_rank");
		tableAccessStrategyFactory.addColumnAlias(EDGE_ANNOTATION_TABLE, "rank_ref", "pre");
		
		String expected = "_node AS _node23, _rank AS _rank23_1, _rank AS _rank23_2, _rank AS _rank23_3";
		assertEquals(expected, adapter23.fromClause());
		checkWhereCondition(
				"_rank23_1.node_ref = _node23.id",
				"_rank23_2.pre = _rank23_1.pre",
				"_rank23_3.pre = _rank23_1.pre"
		);
	}
	
	// use one node table for each annotation and nothing else if all tables are aliased to node
	@Test
	public void fromClauseTablesAliasedToStruct() {
		Set<Annotation> annotations1 = annotationSet(annotation1, annotation2);
		node23.setNodeAnnotations(annotations1);

		Set<Annotation> annotations2 = annotationSet(annotation1, annotation2, annotation3);
		node23.setEdgeAnnotations(annotations2);

		tableAccessStrategyFactory.addTableAlias(EDGE_TABLE, "_node");
		tableAccessStrategyFactory.addTableAlias(NODE_ANNOTATION_TABLE, "_node");
		tableAccessStrategyFactory.addColumnAlias(NODE_ANNOTATION_TABLE, "node_ref", "id");
		tableAccessStrategyFactory.addTableAlias(EDGE_ANNOTATION_TABLE, "_node");
		tableAccessStrategyFactory.addColumnAlias(EDGE_ANNOTATION_TABLE, "rank_ref", "pre");

		String expected = "_node AS _node23_1, _node AS _node23_2, _node AS _node23_3, _node AS _node23_4";
		assertEquals(expected, adapter23.fromClause());
		checkWhereCondition(
				"_node23_2.id = _node23_1.id",
				"_node23_3.pre = _node23_1.pre",
				"_node23_4.pre = _node23_1.pre"
		);
	}

	private TreeSet<Annotation> annotationSet(Annotation... annotations) {
		return new TreeSet<Annotation>(Arrays.asList(annotations));
	}
	
	///// WHERE clause

	///// Helper
	
	private void checkWhereCondition(String... expected) {
		List<String> actual = adapter23.whereClause();
		for (String item : expected)
			assertThat(actual, hasItem(item));
	}

}
