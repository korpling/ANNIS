
package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.EDGE_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static test.IsCollectionEmpty.empty;
import static test.IsCollectionSize.size;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import annis.dao.BaseCorpusSelectionStrategy;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnisNode.TextMatching;
import annis.sqlgen.model.Dominance;
import annis.sqlgen.model.Inclusion;
import annis.sqlgen.model.LeftAlignment;
import annis.sqlgen.model.LeftDominance;
import annis.sqlgen.model.LeftOverlap;
import annis.sqlgen.model.Overlap;
import annis.sqlgen.model.PointingRelation;
import annis.sqlgen.model.Precedence;
import annis.sqlgen.model.RightAlignment;
import annis.sqlgen.model.RightDominance;
import annis.sqlgen.model.RightOverlap;
import annis.sqlgen.model.SameSpan;
import annis.sqlgen.model.Sibling;

// FIXME: move tests to TestAbstractNodeSqlAdapter
public class TestTableJoinsInFromClauseNodeSqlAdapter {

	// an example node
	private AnnisNode node23;
	private AnnisNode node42;

	// object under test: the adapter to that node
	private AbstractNodeSqlAdapter adapter23;

	// more constants for easier testing
	private final static String NAME = "name";
	
	@Mock private BaseCorpusSelectionStrategy corpusSelectionStrategy;
	private TableAccessStrategyFactory tableAccessStrategyFactory;
	
	@Mock Set<Annotation> annotations;
	
	@Before
	public void setup() {
		initMocks(this);
		node23 = new AnnisNode(23);
		node42 = new AnnisNode(42);
	
		tableAccessStrategyFactory = new TableAccessStrategyFactory();
		
		adapter23 = new TableJoinsInFromClauseNodeSqlAdapter();
		adapter23.setNode(node23);
		adapter23.setCorpusSelectionStrategy(corpusSelectionStrategy);
		adapter23.setTableAccessStrategyFactory(tableAccessStrategyFactory);

		// add column aliases to make sure they are used
		tableAccessStrategyFactory.addTableAlias(NODE_TABLE, "_node");
		tableAccessStrategyFactory.addTableAlias(EDGE_TABLE, "_rank");
		tableAccessStrategyFactory.addTableAlias(NODE_ANNOTATION_TABLE, "_annotation");
		tableAccessStrategyFactory.addTableAlias(EDGE_ANNOTATION_TABLE, "_rank_annotation");
		
		when(annotations.size()).thenReturn(3);
		when(corpusSelectionStrategy.viewName(anyString())).thenAnswer(new Answer<String>() {

			// return string unchanged
			public String answer(InvocationOnMock invocation) throws Throwable {
				return (String) invocation.getArguments()[0];
			}
			
		});
	}
	
	// return id, text_ref, left_token, right_token for every node
	@Test
	public void selectClause() {
		String expected = "_node23.id, _node23.text_ref, _node23.left_token, _node23.right_token";
		String actual = adapter23.selectClause();
		assertEquals(expected, actual);
	}
	
	// return right number of NULL fields
	@Test
	public void selectClauseNullValues() {
		String expected = "NULL, NULL, NULL, NULL";
		assertEquals(expected, adapter23.selectClauseNullValues());
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
			"_node AS _node23" + 
			" " +
			"JOIN _rank AS _rank23 ON (_rank23.node_ref = _node23.id)";		
		assertEquals(expected, adapter23.fromClause());
	}
	
	// don't join rank table if the join is materialized
	@Test
	public void fromClauseUsesRankAliasedTonode() {
		node23.setRoot(true);
		tableAccessStrategyFactory.addTableAlias(EDGE_TABLE, "_node");

		String expected = "_node AS _node23";
		assertEquals(expected, adapter23.fromClause());
	}
	
	// join the annotation table for each node annotation
	@Test
	public void fromClauseNodeAnnotations() {
		node23.setNodeAnnotations(annotations);
		
		String expected =
			"_node AS _node23 " + 
			"JOIN _annotation AS _annotation23_1 ON (_annotation23_1.node_ref = _node23.id) " +
			"JOIN _annotation AS _annotation23_2 ON (_annotation23_2.node_ref = _node23.id) " +
			"JOIN _annotation AS _annotation23_3 ON (_annotation23_3.node_ref = _node23.id)";
		assertEquals(expected, adapter23.fromClause());
	}
	
	// join annotation table for each node annotation, use node table for first annotation if they are the same
	@Test
	public void fromClauseNodeAnnotationsAliasedTonode() {
		node23.setNodeAnnotations(annotations);
		tableAccessStrategyFactory.addTableAlias(NODE_ANNOTATION_TABLE, "_node");
		tableAccessStrategyFactory.addColumnAlias(NODE_ANNOTATION_TABLE, "node_ref", "id");
		
		String expected =
			"_node AS _node23_1 " +
			"JOIN _node AS _node23_2 ON (_node23_2.id = _node23_1.id) " +
			"JOIN _node AS _node23_3 ON (_node23_3.id = _node23_1.id)";
		assertEquals(expected, adapter23.fromClause());
	}
	
	// join the rank table once and the edge annotation table for each edge annotation
	@Test
	public void fromClauseOfEdgeAnnotations() {
		node23.setEdgeAnnotations(annotations);
		
		String expected =
			"_node AS _node23 " + 
			"JOIN _rank AS _rank23 ON (_rank23.node_ref = _node23.id) " +
			"JOIN _rank_annotation AS _rank_annotation23_1 ON (_rank_annotation23_1.rank_ref = _rank23.pre) " +
			"JOIN _rank_annotation AS _rank_annotation23_2 ON (_rank_annotation23_2.rank_ref = _rank23.pre) " +
			"JOIN _rank_annotation AS _rank_annotation23_3 ON (_rank_annotation23_3.rank_ref = _rank23.pre)";
		assertEquals(expected, adapter23.fromClause());
	}
	
	// join annotation table for each edge annotation, use rank table for first annotation if they are the same
	@Test
	public void fromClauseEdgeAnnotationsAliasedTonode() {
		node23.setEdgeAnnotations(annotations);
		tableAccessStrategyFactory.addTableAlias(EDGE_ANNOTATION_TABLE, "_rank");
		tableAccessStrategyFactory.addColumnAlias(EDGE_ANNOTATION_TABLE, "rank_ref", "pre");
		
		String expected =
			"_node AS _node23 " + 
			"JOIN _rank AS _rank23_1 ON (_rank23_1.node_ref = _node23.id) " +
			"JOIN _rank AS _rank23_2 ON (_rank23_2.pre = _rank23_1.pre) " +
			"JOIN _rank AS _rank23_3 ON (_rank23_3.pre = _rank23_1.pre)";
		assertEquals(expected, adapter23.fromClause());
	}
	
	// use one node table for each annotation and nothing else if all tables are aliased to node
	@SuppressWarnings("unchecked")
	@Test
	public void fromClauseTablesAliasedTonode() {
		Set<Annotation> annotations1 = mock(Set.class);
		when(annotations1.size()).thenReturn(2);
		node23.setNodeAnnotations(annotations1);

		Set<Annotation> annotations2 = mock(Set.class);
		when(annotations2.size()).thenReturn(3);
		node23.setEdgeAnnotations(annotations2);

		tableAccessStrategyFactory.addTableAlias(EDGE_TABLE, "_node");
		tableAccessStrategyFactory.addTableAlias(NODE_ANNOTATION_TABLE, "_node");
		tableAccessStrategyFactory.addColumnAlias(NODE_ANNOTATION_TABLE, "node_ref", "id");
		tableAccessStrategyFactory.addTableAlias(EDGE_ANNOTATION_TABLE, "_node");
		tableAccessStrategyFactory.addColumnAlias(EDGE_ANNOTATION_TABLE, "rank_ref", "pre");

		String expected =
			"_node AS _node23_1 " +
			"JOIN _node AS _node23_2 ON (_node23_2.id = _node23_1.id) " +
			"JOIN _node AS _node23_3 ON (_node23_3.pre = _node23_1.pre) " +
			"JOIN _node AS _node23_4 ON (_node23_4.pre = _node23_1.pre)";
		assertEquals(expected, adapter23.fromClause());
	}
	
	///// WHERE clause
	
	// WHERE condition for corpus selection
	@Test
	public void whereClauseCorpusSelection() {
		// stub CorpusSelectionStrategy
		final String COPRUS_SELECTION_CONDITION = "foo";
		when(corpusSelectionStrategy.whereClauseForNode(anyString())).thenReturn(COPRUS_SELECTION_CONDITION);
		
		// check condition creation
		checkWhereCondition(COPRUS_SELECTION_CONDITION);
		
		// verify correct table column was passed
		verify(corpusSelectionStrategy).whereClauseForNode("_node23.corpus_ref");
	}
	
	// WHERE skips null condition for corpus selection
	@Test
	public void whereClauseCorpusSelectionNull() {
		assertThat(adapter23.whereClause(), is(empty()));
	}
	
	// WHERE condition for root node
	@Test
	public void whereClauseForNodeRoot() {
		node23.setRoot(true);
		checkWhereCondition("_rank23.parent IS NULL");
	}

	// WHERE condition for namespace
	@Test
	public void whereClauseForNodeNamespace() {
		node23.setNamespace("namespace");
		checkWhereCondition(join("=", "_node23.namespace", "'namespace'"));
	}
	
	// WHERE condition for name
	@Test
	public void whereClauseForNodeName() {
		node23.setName("name");
		checkWhereCondition(join("=", "_node23.name", "'name'"));
	}
	
	// WHERE condition for spanned text (string)
	@Test
	public void whereClauseForNodeSpanString() {
		node23.setSpannedText("string", TextMatching.EXACT);
		checkWhereCondition(join("=", "_node23.span", "'string'"));
	}
	
	// WHERE condition for spanned text (regexp) 
	@Test
	public void whereClauseForNodeSpanRegexp() {
		node23.setSpannedText("regexp", TextMatching.REGEXP);
		checkWhereCondition(join("~", "_node23.span", "'^regexp$'"));
	}
	
	// WHERE condition for node annotation
	@Test
	public void whereClauseForNodeAnnotation() {
		node23.addNodeAnnotation(new Annotation("namespace1", "name1"));
		node23.addNodeAnnotation(new Annotation("namespace2", "name2", "value2", TextMatching.EXACT));
		node23.addNodeAnnotation(new Annotation("namespace3", "name3", "value3", TextMatching.REGEXP));
		checkWhereCondition(
				join("=", "_annotation23_1.namespace", "'namespace1'"),
				join("=", "_annotation23_1.name", "'name1'"),
				join("=", "_annotation23_2.namespace", "'namespace2'"),
				join("=", "_annotation23_2.name", "'name2'"),
				join("=", "_annotation23_2.value", "'value2'"),
				join("=", "_annotation23_3.namespace", "'namespace3'"),
				join("=", "_annotation23_3.name", "'name3'"),
				join("~", "_annotation23_3.value", "'^value3$'")
		);
	}
	
	// WHERE condition for node annotation
	@Test
	public void whereClauseForNodeEdgeAnnotation() {
		node23.addEdgeAnnotation(new Annotation("namespace1", "name1"));
		node23.addEdgeAnnotation(new Annotation("namespace2", "name2", "value2", TextMatching.EXACT));
		node23.addEdgeAnnotation(new Annotation("namespace3", "name3", "value3", TextMatching.REGEXP));
		checkWhereCondition(
				join("=", "_rank_annotation23_1.namespace", "'namespace1'"),
				join("=", "_rank_annotation23_1.name", "'name1'"),
				join("=", "_rank_annotation23_2.namespace", "'namespace2'"),
				join("=", "_rank_annotation23_2.name", "'name2'"),
				join("=", "_rank_annotation23_2.value", "'value2'"),
				join("=", "_rank_annotation23_3.namespace", "'namespace3'"),
				join("=", "_rank_annotation23_3.name", "'name3'"),
				join("~", "_rank_annotation23_3.value", "'^value3$'")
		);
	}
	
	// WHERE condition for isToken
	@Test
	public void whereClauseForNodeIsToken() {
		node23.setToken(true);
		checkWhereCondition("_node23.token_index IS NOT NULL");
	}
	
	// WHERE condition for _=_
	@Test
	public void whereClauseForNodeSameSpan() {
		node23.addJoin(new SameSpan(node42));
		checkWhereCondition(
				join("=", "_node23.text_ref", "_node42.text_ref"),
				join("=", "_node23.left", "_node42.left"),
				join("=", "_node23.right", "_node42.right")
		);
	}
	
	// WHERE condition for _l_
	@Test
	public void whereClauseForNodeLeftAlignment() {
		node23.addJoin(new LeftAlignment(node42));
		checkWhereCondition(
				join("=", "_node23.text_ref", "_node42.text_ref"),
				join("=", "_node23.left", "_node42.left")
		);
	}
	
	// WHERE condition for _r_
	@Test
	public void whereClauseForNodeRightAlignment() {
		node23.addJoin(new RightAlignment(node42));
		checkWhereCondition(
				join("=", "_node23.text_ref", "_node42.text_ref"),
				join("=", "_node23.right", "_node42.right")
		);
	}
	
	// WHERE condition for _i_
	@Test
	public void whereClauseForNodeInclusion() {
		node23.addJoin(new Inclusion(node42));
		checkWhereCondition(
				join("=", "_node23.text_ref", "_node42.text_ref"),
				join("<=", "_node23.left", "_node42.left"),
				join(">=", "_node23.right", "_node42.right")
		);
	}
	
	// WHERE condition for _ol_
	@Test
	public void whereClauseForNodeLeftOverlap() {
		node23.addJoin(new LeftOverlap(node42));
		checkWhereCondition(
				join("=", "_node23.text_ref", "_node42.text_ref"),
				join("<=", "_node23.left", "_node42.left"),
				join("<", "_node23.left", "_node42.right"),
				join("<", "_node23.right", "_node42.right")
		);
	}
	
	// WHERE condition for _or_
	@Test
	public void whereClauseForNodeRightOverlap() {
		node23.addJoin(new RightOverlap(node42));
		checkWhereCondition(
				join("=", "_node23.text_ref", "_node42.text_ref"),
				join("<", "_node23.left", "_node42.left"),
				join("<=", "_node23.left", "_node42.right"),
				join(">=", "_node23.right", "_node42.right")
		);
	}
	
	// WHERE condition for _o_
	@Test(expected=NotImplementedException.class)	// FIXME: geht das ohne ODER
	public void whereClauseForNodeOverlap() {
		node23.addJoin(new Overlap(node42));
		checkWhereCondition();
	}
	
	// WHERE condition for .
	@Test
	public void whereClauseForNodeDirectPrecedence() {
		node23.addJoin(new Precedence(node42, 1));
		checkWhereCondition(
				join("=", "_node23.text_ref", "_node42.text_ref"),
				join("=", "_node23.right_token", "_node42.left_token", -1)
		);
	}
	
	// WHERE condition for .*
	@Test
	public void whereClauseForNodeIndirectPrecedence() {
		node23.addJoin(new Precedence(node42));
		checkWhereCondition(
				join("=", "_node23.text_ref", "_node42.text_ref"),
				join("<", "_node23.right_token", "_node42.left_token")
		);
	}
	
	// WHERE condition for .n
	@Test
	public void whereClauseForNodeExactPrecedence() {
		node23.addJoin(new Precedence(node42, 10));
		checkWhereCondition(
				join("=", "_node23.text_ref", "_node42.text_ref"),
				join("=", "_node23.right_token", "_node42.left_token", -10));
	}
	
	// WHERE condition for .n,m
	@Test
	public void whereClauseForNodeRangedPrecedence() {
		node23.addJoin(new Precedence(node42, 10, 20));
		checkWhereCondition(
				join("=", "_node23.text_ref", "_node42.text_ref"),
				join("<=", "_node23.right_token", "_node42.left_token", -10),
				join(">=", "_node23.right_token", "_node42.left_token", -20)
		);
	}
	
	// WHERE condition for >
	@Test
	public void whereClauseForNodeDirectDominance() {
		node23.addJoin(new Dominance(node42, 1));
		checkWhereCondition(
				join("=", "_rank23.zshg", "_rank42.zshg"),
				join("=", "_rank42.edge_type", "'d'"),
				join("=", "_rank23.pre", "_rank42.parent")
		);
	}
	
	// WHERE condition for > name
	@Test
	public void whereClauseDirectDominanceNamed() {
		node23.addJoin(new Dominance(node42, NAME, 1));
		checkWhereCondition(
				join("=", "_rank23.zshg", "_rank42.zshg"),
				join("=", "_rank42.edge_type", "'d'"),
				join("=", "_rank42.name", "'" + NAME + "'"),
				join("=", "_rank23.pre", "_rank42.parent")
		);
	}
	
	// WHERE condition for >*
	@Test
	public void whereClauseForNodeIndirectDominance() {
		node23.addJoin(new Dominance(node42));
		checkWhereCondition(
				join("=", "_rank23.zshg", "_rank42.zshg"),
				join("=", "_rank42.edge_type", "'d'"),
				join("<", "_rank23.pre", "_rank42.pre"),
				join(">", "_rank23.post", "_rank42.post")
		);
	}
	
	// WHERE condition for >n
	@Test
	public void whereClauseForNodeExactDominance() {
		node23.addJoin(new Dominance(node42, 10));
		checkWhereCondition(
				join("=", "_rank23.zshg", "_rank42.zshg"),
				join("=", "_rank42.edge_type", "'d'"),
				join("<", "_rank23.pre", "_rank42.pre"),
				join(">", "_rank23.post", "_rank42.post"),
				join("=", "_rank23.level", "_rank42.level", -10)
		);
	}
	
	// WHERE condition for >n,m
	@Test
	public void whereClauseForNodeRangedDominance() {
		node23.addJoin(new Dominance(node42, 10, 20));
		checkWhereCondition(
				join("=", "_rank23.zshg", "_rank42.zshg"),
				join("=", "_rank42.edge_type", "'d'"),
				join("<", "_rank23.pre", "_rank42.pre"),
				join(">", "_rank23.post", "_rank42.post"),
				join("<=", "_rank23.level", "_rank42.level", -10),
				join(">=", "_rank23.level", "_rank42.level", -20)
		);
	}
	
	// WHERE condition for >@l
	@Test
	public void whereClauseForNodeLeftDominance() {
		node23.addJoin(new LeftDominance(node42));
		checkWhereCondition(join("=", "_rank23.pre", "_rank42.pre", -1));
	}
	
	// WHERE condition for >@r
	@Test
	public void whereClauseForNodeRightDominance() {
		node23.addJoin(new RightDominance(node42));
		checkWhereCondition(join("=", "_rank23.post", "_rank42.post", 1));
	}
	
	// WHERE condition for ->
	@Test
	public void whereClauseDirectPointingRelation() {
		node23.addJoin(new PointingRelation(node42, NAME, 1));
		checkWhereCondition(
				join("=", "_rank23.zshg", "_rank42.zshg"),
				join("=", "_rank42.edge_type", "'p'"),
				join("=", "_rank42.name", "'" + NAME + "'"),
				join("=", "_rank23.pre", "_rank42.parent")

		);
	}
	
	// WHERE condition for ->*
	@Test
	public void whereClauseIndirectPointingRelation() {
		node23.addJoin(new PointingRelation(node42, NAME));
		checkWhereCondition(
				join("=", "_rank23.zshg", "_rank42.zshg"),
				join("=", "_rank42.edge_type", "'p'"),
				join("=", "_rank42.name", "'" + NAME + "'"),
				join("<", "_rank23.pre", "_rank42.pre"),
				join(">", "_rank23.post", "_rank42.post")
		);
	}
	
	// WHERE condition for $
	@Test
	public void whereClauseForNodeSibling() {
		node23.addJoin(new Sibling(node42));
		checkWhereCondition(join("=", "_rank23.parent", "_rank42.parent"));
	}
	
	///// Joins and Literals
	
	// join two columns with a positive offset
	@Test
	public void joinPlus() {
		String expected = "lhs <op> rhs + 0";
		String actual = adapter23.numberJoin("<op>", "lhs", "rhs", 0);
		assertEquals(expected, actual);
	}
	
	// join two columns with a negative offset
	@Test
	public void joinMinus() {
		String expected = "lhs <op> rhs - 1";
		String actual = adapter23.numberJoin("<op>", "lhs", "rhs", -1);
		assertEquals(expected, actual);
	}
	
	// SQL string literals are enclosed in single quotes
	@Test
	public void sqlString() {
		String string = "string";
		assertThat(adapter23.sqlString(string), is("'" + string + "'"));
	}
	
	// join two columns
	@Test
	public void join() {
		String expected = "lhs <op> rhs";
		String actual = adapter23.join("<op>", "lhs", "rhs");
		assertEquals(expected, actual);
	}
	
	///// Helper
	
	private void checkWhereCondition(String... expected) {
		List<String> actual = adapter23.whereClause();
		for (String item : expected)
			assertThat(actual, hasItem(item));
		assertThat(actual, is(size(expected.length)));
	}

	private String join(String op, String lhs, String rhs) {
		return lhs + " " + op + " " + rhs;
	}
	
	private String join(String op, String lhs, String rhs, int offset) {
		String plus = offset >= 0 ? " + " : " - ";
		return join(op, lhs, rhs) + plus + String.valueOf(Math.abs(offset));
	}
	
}
