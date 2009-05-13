package annis.sqlgen;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import annis.dao.CorpusSelectionStrategy;
import annis.dao.CorpusSelectionStrategy1;
import annis.model.AnnisNode;
import annis.sqlgen.AbstractNodeSqlAdapter;
import annis.sqlgen.CoveredTokensSelectClauseSqlAdapter;
import annis.sqlgen.ClauseSqlAdapter;
import annis.sqlgen.NodeSqlAdapter;
import annis.sqlgen.NodeSqlAdapterFactory;
import annis.sqlgen.model.Dominance;
import annis.sqlgen.model.PointingRelation;


public class TestClauseSqlAdapter {

	// class under test
	private ClauseSqlAdapter clauseSqlAdapter;

	// two example nodes in a list
	private List<AnnisNode> nodes;
	private AnnisNode node23;
	private AnnisNode node42;
	final private int MAX_WIDTH = 2;
	
	// mocked SqlTableNodeAdapter for each node + stubbed factory to generate these nodes
	private NodeSqlAdapter adapter23;
	private NodeSqlAdapter adapter42;
	@Mock private NodeSqlAdapterFactory nodeSqlAdapterFactory;
	
	private Map<AnnisNode, NodeSqlAdapter> adapters;
	
	// CorpusSelectionStrategy dependency
	@Mock private CorpusSelectionStrategy1 corpusSelectionStragegy;
	
	@Before
	public void setup() {
		initMocks(this);

		// default arguments for appendXXXClause calls
		nodes = new ArrayList<AnnisNode>();
		adapters = new HashMap<AnnisNode, NodeSqlAdapter>();

		// node 23
		node23 = createNode(23);
		adapter23 = createNodeSqlAdapter(node23);

		// node 42
		node42 = createNode(42);
		adapter42 = createNodeSqlAdapter(node42);
		
		// create object under test and wire dependencies
		clauseSqlAdapter = new ClauseSqlAdapter();
		clauseSqlAdapter.setNodeSqlAdapterFactory(nodeSqlAdapterFactory);
	}

	// create a node and add it to default nodes list
	private AnnisNode createNode(int aliasCount) {
		AnnisNode node = new AnnisNode(aliasCount);
		nodes.add(node);
		return node;
	}
	
	// create an adapter for a node and add it to the default adapters map
	private NodeSqlAdapter createNodeSqlAdapter(AnnisNode node) {
		NodeSqlAdapter adapter = mock(NodeSqlAdapter.class);
		adapters.put(node, adapter);
		when(nodeSqlAdapterFactory.createNodeSqlAdapter(node, corpusSelectionStragegy)).thenReturn(adapter);
		return adapter;
	}

	///// sanity checks
	
	// nodes must not be empty
	@Test(expected=IllegalArgumentException.class)
	public void toSqlNodesSizeZero() {
		clauseSqlAdapter.toSql(new ArrayList<AnnisNode>(), 1, corpusSelectionStragegy, null);
	}
	
	// maxWidth must be at least the number of nodes
	@Test(expected=IllegalArgumentException.class)
	public void toSqlMaxWidthLessThanNodesSize() {
		clauseSqlAdapter.toSql(nodes, nodes.size() - 1, corpusSelectionStragegy, null);
	}
	
	///// flow control
	
	// SQL query consists of SELECT, FROM and (possibly empty) WHERE clause 
	@Test
	@SuppressWarnings("unchecked")
	public void toSqlFlowControl() {
		// stub the generation of the SELECT, FROM and WHERE clauses 
		// stub the generation of SqlTableNodeAdapter for each node
		// alias global nodes to verify that it was passed to called functions
		final String selectClause = "SELECT ... ";
		final String fromClause = "FROM ... ";
		final String whereClause = "WHERE ... ";
		final Map<AnnisNode, NodeSqlAdapter> mockedAdapters = mock(Map.class);
		final List<AnnisNode> aliasedNodes = nodes;
		ClauseSqlAdapter stubbedClauseSqlGenerator = new ClauseSqlAdapter() {
			
			@Override
			Map<AnnisNode, NodeSqlAdapter> sqlAdaptersForNodes(
					List<AnnisNode> nodes, CorpusSelectionStrategy corpusSelector) {
				assertThat(nodes, is(sameInstance(aliasedNodes)));
				return mockedAdapters;
			}
			
//			@Override
//			public String toSql(List<AnnisNode> nodes, int maxWidth,
//					CorpusSelector corpusSelector) {
//				assertThat(corpusSelector, is(sameInstance(CORPUS_SELECTOR)));
//				return super.toSql(aliasedNodes, maxWidth, corpusSelector);
//			}
			
			@Override
			void appendSelectClause(StringBuffer sb, List<AnnisNode> nodes,
					CorpusSelectionStrategy corpusSelectionStrategy, int maxWidth, SelectClauseSqlAdapter selectClauseSqlAdapter) {
				assertThat(nodes, is(sameInstance(aliasedNodes)));
				sb.append(selectClause);
			}
			
			@Override
			void appendFromClause(StringBuffer sb, List<AnnisNode> nodes, Map<AnnisNode, NodeSqlAdapter> adapters) {
				assertThat(nodes, is(sameInstance(aliasedNodes)));
				assertThat(adapters, is(sameInstance(mockedAdapters)));
				sb.append(fromClause);
			}
			
			@Override
			void appendWhereClause(StringBuffer sb, List<AnnisNode> nodes, Map<AnnisNode, NodeSqlAdapter> adapters) {
				assertThat(nodes, is(sameInstance(aliasedNodes)));
				assertThat(adapters, is(sameInstance(mockedAdapters)));
				sb.append(whereClause);
			}
		};

		String sql = stubbedClauseSqlGenerator.toSql(aliasedNodes, MAX_WIDTH, corpusSelectionStragegy, null);
		assertThat(sql, is(selectClause + fromClause + whereClause));
	}
	
	///// SELECT clause
	
	@SuppressWarnings("unchecked")
	@Test
	public void appendSelectClause() {
		SelectClauseSqlAdapter adapter = mock(CoveredTokensSelectClauseSqlAdapter.class);
		final String SELECT_CLAUSE_FOR_ALL_NODES = "SELECT CLAUSE FOR ALL NODES";
		when(adapter.selectClause(anyList(), any(CorpusSelectionStrategy.class)))
			.thenReturn(SELECT_CLAUSE_FOR_ALL_NODES);
		
		String expected = "SELECT " + SELECT_CLAUSE_FOR_ALL_NODES + "\n";
		
		StringBuffer sb = new StringBuffer();
		clauseSqlAdapter.appendSelectClause(sb, nodes, corpusSelectionStragegy, MAX_WIDTH, adapter);
		assertEquals(expected, sb.toString());
	}
	
	// SELECT clause for a node (maxWidth = nodes.size())
	@Test
	public void XappendSelectClauseNodesSizeEqualsMaxWidth() {
		// stub SELECT clause of adapter for node 23
		final String SELECT_CLAUSE_FOR_NODE_23 = "SELECT CLAUSE FOR NODE 23";
		when(adapter23.selectClause()).thenReturn(SELECT_CLAUSE_FOR_NODE_23);

		// stub SELECT clause of adapter for node 42
		final String SELECT_CLAUSE_FOR_NODE_42 = "SELECT CLAUSE FOR NODE 42";
		when(adapter42.selectClause()).thenReturn(SELECT_CLAUSE_FOR_NODE_42);

		String expected = "" +
				"SELECT DISTINCT\n" +
				"\t" + SELECT_CLAUSE_FOR_NODE_23 + ",\n" +
				"\t" + SELECT_CLAUSE_FOR_NODE_42 + "\n";

		StringBuffer sb = new StringBuffer();
		clauseSqlAdapter.appendSelectClause(sb, nodes, corpusSelectionStragegy, MAX_WIDTH, null);
		assertEquals(expected, sb.toString());
	}
	
	// maxWidth > nodes.size(): append NULLs to SELECT clause
	@Test
	public void XappendSelectClauseNodesLessThanMaxWidth() {
		// stub SELECT clauses
		final String SELECT_CLAUSE = "SELECT CLAUSE";
		when(adapter23.selectClause()).thenReturn(SELECT_CLAUSE);
		when(adapter42.selectClause()).thenReturn(SELECT_CLAUSE);
		
		// stub adapter for missing nodes
		final String NULLS = "NULLS";
		AbstractNodeSqlAdapter nullAdapter = mock(AbstractNodeSqlAdapter.class);
		when(nullAdapter.selectClauseNullValues()).thenReturn(NULLS);
		when(nodeSqlAdapterFactory.createNodeSqlAdapter()).thenReturn(nullAdapter);
		
		String expected = "" +
			"SELECT DISTINCT\n" +
			"\t" + SELECT_CLAUSE + ",\n" +
			"\t" + SELECT_CLAUSE + ",\n" +
			"\t" + NULLS + ",\n" +
			"\t" + NULLS + "\n";

		StringBuffer sb = new StringBuffer();
		clauseSqlAdapter.appendSelectClause(sb, nodes, corpusSelectionStragegy, MAX_WIDTH + 2, null);
		assertEquals(expected, sb.toString());
	}
	
	///// FROM clause
	
	// FROM clause consists of string "FROM" followed by table definitions for a node, separated by ","
	@Test
	public void appendFromClause() {
		// stub FROM clause of adapter for node 23
		final String FROM_CLAUSE_FOR_NODE_23 = "FROM CLAUSE FOR NODE 23";
		when(adapter23.fromClause()).thenReturn(FROM_CLAUSE_FOR_NODE_23);

		// stub FROM clause of adapter for node 42
		final String FROM_CLAUSE_FOR_NODE_42 = "FROM CLAUSE FOR NODE 42";
		when(adapter42.fromClause()).thenReturn(FROM_CLAUSE_FOR_NODE_42);
		
		// expected FROM clause
		String expected = 
			"FROM\n" + 
			"\t" + FROM_CLAUSE_FOR_NODE_23 + ",\n" +
			"\t" + FROM_CLAUSE_FOR_NODE_42 + "\n";
		
		// test
		StringBuffer sb = new StringBuffer();
		clauseSqlAdapter.appendFromClause(sb, nodes, adapters);
		assertEquals(expected, sb.toString());
	}
	
	// create adapters for nodes that are NOT part of a dominance join
	@SuppressWarnings("unchecked")
	@Test
	public void sqlTableAdapters() {
		// test call
		Map<AnnisNode, NodeSqlAdapter> adapters = clauseSqlAdapter.sqlAdaptersForNodes(nodes, corpusSelectionStragegy);
		
		// verify the returned map
		assertThat(adapters, allOf(hasEntry(node23, adapter23), hasEntry(node42, adapter42)));
	}
	
	// assume a node does not need the rank table because of a join
	@SuppressWarnings("unchecked")
	@Test
	public void computeUsedRankTablesFalse() {
		Map<AnnisNode, Boolean> usedRankTables = clauseSqlAdapter.computeNodesInDominanceJoin(nodes);
		assertThat(usedRankTables, allOf(hasEntry(node23, false), hasEntry(node42, false)));
	}
	
	// nodes that take part in dominance joins need the rank table in from clause
	@SuppressWarnings("unchecked")
	@Test
	public void computeUsedRankTablesTrueDominance() {
		node23.addJoin(new Dominance(node42));
		Map<AnnisNode, Boolean> usedRankTables = clauseSqlAdapter.computeNodesInDominanceJoin(nodes);
		assertThat(usedRankTables, allOf(hasEntry(node23, true), hasEntry(node42, true)));
	}
		
	// nodes that take part in dominance joins need the rank table in from clause
	@SuppressWarnings("unchecked")
	@Test
	public void computeUsedRankTablesTruePointingRelation() {
		node23.addJoin(new PointingRelation(node42, "name"));
		Map<AnnisNode, Boolean> usedRankTables = clauseSqlAdapter.computeNodesInDominanceJoin(nodes);
		assertThat(usedRankTables, allOf(hasEntry(node23, true), hasEntry(node42, true)));
	}
		
	///// WHERE clause
	
	@Test
	public void appendWhereClauseEmptyNodes() {
		// stub empty WHERE clause for both adapters
		final List<String> EMPTY_WHERE_CLAUSE = new ArrayList<String>();
		when(adapter23.whereClause()).thenReturn(EMPTY_WHERE_CLAUSE);
		when(adapter42.whereClause()).thenReturn(EMPTY_WHERE_CLAUSE);
		
		// expected WHERE clause
		String expected = "" +
				"\t-- node 23\n" +
				"\t-- node 42\n";
		
		// test
		StringBuffer sb = new StringBuffer();
		clauseSqlAdapter.appendWhereClause(sb, nodes, adapters);
		assertEquals(expected, sb.toString());
	}
	
	// if at least one node has a WHERE condition, prepend WHERE clause and AND conditions
	@Test
	public void appendWhereClauseNotEmptyNodes() {
		AnnisNode node99 = createNode(99);
		NodeSqlAdapter adapter99 = createNodeSqlAdapter(node99);
		
		// stub WHERE conditions for nodes
		when(adapter23.whereClause()).thenReturn(Arrays.asList("node 23 condition 1", "node 23 condition 2"));
		when(adapter42.whereClause()).thenReturn(new ArrayList<String>());
		when(adapter99.whereClause()).thenReturn(Arrays.asList("node 99 condition 1", "node 99 condition 2"));

		// test call
		StringBuffer sb = new StringBuffer();
		clauseSqlAdapter.appendWhereClause(sb, nodes, adapters);
		
		String expected = "" +
				"WHERE\n" +						// prepend WHERE
				"\t-- node 23\n" +
				"\tnode 23 condition 1 AND\n" +	// append AND to condition
				"\tnode 23 condition 2 AND\n" +	// more conditions in WHERE clause, append AND
				"\t-- node 42\n" +				// node without conditions
				"\t-- node 99\n" +
				"\tnode 99 condition 1 AND\n" +
				"\tnode 99 condition 2\n";		// last condition, don't append AND
		String actual = sb.toString();
		assertEquals(expected, actual);
	}
	
}
