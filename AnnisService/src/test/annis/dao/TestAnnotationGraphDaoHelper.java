package annis.dao;

import static de.deutschdiachrondigital.dddquery.helper.TestHelpers.uniqueInt;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static test.IsCollection.isCollection;
import static test.IsCollectionEmpty.empty;
import static test.IsCollectionSize.size;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import annis.AnnisHomeTest;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import annis.ql.parser.QueryAnalysis;
import annis.ql.parser.QueryData;
import annis.sqlgen.SqlGenerator;
import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.parser.DddQueryParser;

public class TestAnnotationGraphDaoHelper extends AnnisHomeTest {

	// object under test
	private AnnotationGraphDaoHelper annotationGraphDaoHelper;
	
	// dependencies
	@Mock private AnnisNodeRowMapper annisNodeRowMapper;
	@Mock private EdgeRowMapper edgeRowMapper;
	@Mock private AnnotationRowMapper nodeAnnotationRowMapper;
	@Mock private AnnotationRowMapper edgeAnnotationRowMapper;
	@Mock private AnnotationGraphDaoHelper.MatchGroupRowMapper matchGroupRowMapper;
	
	// some dummy matches
	@Mock private Match match1;
	@Mock private Match match2;
	@Mock private Match match3;
	@Mock private Match match4;
	@Mock private Match match5;
	@Mock private List<Match> matches;
	
	@Mock private ResultSet resultSet;

	// some dummy annotations
	private static final Annotation ANNOTATION1 = new Annotation("namespace", "name1");
	private static final Annotation ANNOTATION2 = new Annotation("namespace", "name2");
	
	@Before
	public void setup() throws SQLException {
		initMocks(this);

		annotationGraphDaoHelper = new AnnotationGraphDaoHelper();
		annotationGraphDaoHelper.setAnnisNodeRowMapper(annisNodeRowMapper);
		annotationGraphDaoHelper.setEdgeRowMapper(edgeRowMapper);
		annotationGraphDaoHelper.setNodeAnnotationRowMapper(nodeAnnotationRowMapper);
		annotationGraphDaoHelper.setEdgeAnnotationRowMapper(edgeAnnotationRowMapper);
		annotationGraphDaoHelper.setMatchGroupRowMapper(matchGroupRowMapper);
		
		when(matchGroupRowMapper.mapRow(any(ResultSet.class), anyInt())).thenReturn("1");
		when(annisNodeRowMapper.mapRow(any(ResultSet.class), anyInt())).thenReturn(newNode(1));
		when(edgeRowMapper.mapRow(any(ResultSet.class), anyInt())).thenReturn(newEdge(1, 2));
		assertThat(ANNOTATION1, is(not(ANNOTATION2)));
	
		matches = Arrays.asList(match1, match2, match3, match4, match5);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void createSqlQuery() {
		// stub AnnotateMatchesQueryHelper to return a dummy token relation for the FROM clause
		final List<Match> MATCHES = mock(List.class);
		final String TOKEN_RELATION = "TOKEN_RELATION";

		// FIXME: so ugly, just stub a match list
		AnnotationGraphDaoHelper stubbedAnnotateMatchesQueryHelper = new AnnotationGraphDaoHelper() {
			@Override
			protected String tokenRelation(List<Match> matches, int left, int right) {
				assertThat(matches, is(sameInstance(MATCHES)));
				return TOKEN_RELATION;
			}
		};

		String expected = "" +
			"SELECT DISTINCT\n" + 
			"\ttokens.key, facts.*\n" +
			"FROM\n" +
			TOKEN_RELATION + "\n" +
			"\tJOIN facts AS facts ON (tokens.text_ref = facts.text_ref AND (tokens.min <= facts.left_token AND tokens.max >= facts.right_token OR facts.left_token <= tokens.min AND tokens.min <= facts.right_token OR facts.left_token <= tokens.max AND tokens.max <= facts.right_token))\n" +
			"ORDER BY tokens.key, facts.pre";
		
		assertEquals(expected, stubbedAnnotateMatchesQueryHelper.createSqlQuery(MATCHES, 1, 1));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void createSqlQueryInline() {
		// constants
		final String INLINE_SQL = "INLINE SQL";
		final String DDDQUERY = "DDDQUERY";
		final long OFFSET = 1L;
		final long LIMIT = 2L;
		final int LEFT_CONTEXT = 3;
		final int RIGHT_CONTEXT = 3;
		final List<Long> CORPUSLIST = mock(List.class);
		final Start STATEMENT = new Start();
		
		// stub DddQueryParser and SqlGenerator for inline query
		DddQueryParser dddQueryParser = mock(DddQueryParser.class);
		when(dddQueryParser.parse(DDDQUERY)).thenReturn(STATEMENT);
		annotationGraphDaoHelper.setDddQueryParser(dddQueryParser);
		SqlGenerator sqlGenerator = mock(SqlGenerator.class);
		when(sqlGenerator.toSql(STATEMENT, CORPUSLIST)).thenReturn(INLINE_SQL);
		annotationGraphDaoHelper.setSqlGenerator(sqlGenerator);
		QueryData queryData = mock(QueryData.class);
		QueryAnalysis queryAnalysis = mock(QueryAnalysis.class);
		when(queryAnalysis.analyzeQuery(STATEMENT, CORPUSLIST)).thenReturn(queryData);
		annotationGraphDaoHelper.setQueryAnalysis(queryAnalysis);
		
		// stub a match with 3 nodes
		when(queryData.getMaxWidth()).thenReturn(3);
		
		String expected = "" + 
			"SELECT DISTINCT\n" + 
			"\t(matches.id1 || ',' || matches.id2 || ',' || matches.id3) AS key, facts.*\n" +
			"FROM\n" +
			"\t(" + INLINE_SQL + " ORDER BY id1, id2, id3 OFFSET " + OFFSET + " LIMIT " + LIMIT + ") AS matches,\n" +
			"\tfacts AS facts\n" +
			"WHERE\n" +
			"\t(facts.text_ref = matches.text_ref1 AND ((facts.left_token >= matches.left_token1 - " + LEFT_CONTEXT + " AND facts.right_token <= matches.right_token1 + " + RIGHT_CONTEXT + ") OR (facts.left_token <= matches.left_token1 - " + LEFT_CONTEXT + " AND matches.left_token1 - " + LEFT_CONTEXT + " <= facts.right_token) OR (facts.left_token <= matches.right_token1 + " + RIGHT_CONTEXT + " AND matches.right_token1 + " + RIGHT_CONTEXT + " <= facts.right_token))) OR\n" +
			"\t(facts.text_ref = matches.text_ref2 AND ((facts.left_token >= matches.left_token2 - " + LEFT_CONTEXT + " AND facts.right_token <= matches.right_token2 + " + RIGHT_CONTEXT + ") OR (facts.left_token <= matches.left_token2 - " + LEFT_CONTEXT + " AND matches.left_token2 - " + LEFT_CONTEXT + " <= facts.right_token) OR (facts.left_token <= matches.right_token2 + " + RIGHT_CONTEXT + " AND matches.right_token2 + " + RIGHT_CONTEXT + " <= facts.right_token))) OR\n" +
			"\t(facts.text_ref = matches.text_ref3 AND ((facts.left_token >= matches.left_token3 - " + LEFT_CONTEXT + " AND facts.right_token <= matches.right_token3 + " + RIGHT_CONTEXT + ") OR (facts.left_token <= matches.left_token3 - " + LEFT_CONTEXT + " AND matches.left_token3 - " + LEFT_CONTEXT + " <= facts.right_token) OR (facts.left_token <= matches.right_token3 + " + RIGHT_CONTEXT + " AND matches.right_token3 + " + RIGHT_CONTEXT + " <= facts.right_token)))\n" +
			"ORDER BY key, facts.pre";
		
		assertEquals(expected, annotationGraphDaoHelper.createSqlQuery(CORPUSLIST, DDDQUERY, OFFSET, LIMIT, LEFT_CONTEXT, RIGHT_CONTEXT));
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void tokenRelation() {
		// setup a list of matches
		final int NUM_MATCHES = 3;
		final int TEXT_REF = 10;
		List<Match> matches = new ArrayList<Match>();
		for (int i = 0; i < NUM_MATCHES; ++i)
			matches.add(new Match(Arrays.asList(
					new Span(uniqueInt(), TEXT_REF, uniqueInt(), uniqueInt()),
					new Span(uniqueInt(), TEXT_REF, uniqueInt(), uniqueInt()))));

		// setup expected String
		final int LEFT = uniqueInt();
		final int RIGHT = uniqueInt();
		StringBuffer expected = new StringBuffer();
		expected.append("	(\n");

		// FIXME: just write the string with replace
		for (Match match : matches) {
			expected.append("		SELECT '");
			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;
			for (Span node : match) {
				min = Math.min(min, node.getTokenLeft());
				max = Math.max(max, node.getTokenRight());
				expected.append(node.getStructId());
				expected.append(",");
			}
			expected.setLength(expected.length() - ",".length());
			expected.append("'::varchar AS key, ");
			expected.append(TEXT_REF);
			expected.append(" AS text_ref, ");
			expected.append(min - LEFT);
			expected.append(" AS min, ");
			expected.append(max + RIGHT);
			expected.append(" AS max UNION\n");
		}
		expected.setLength(expected.length() - " UNION\n".length());
		expected.append("\n	) AS tokens");
		
		// test
		assertEquals(expected.toString(), annotationGraphDaoHelper.tokenRelation(matches, LEFT, RIGHT));
	}
	
	// create an sql query for a text id
	@Test
	public void createSqlQueryText() {
		final long TEXT_ID = 1;
		
		String expected = "SELECT DISTINCT\n"
				+ "\t'-1' AS key, facts.*\n"
				+ "FROM\n"
				+ "\tfacts AS facts\n"
				+ "WHERE\n" + "\tfacts.text_ref = " + TEXT_ID + "\n"
				+ "ORDER BY facts.pre";
		
		assertEquals(expected, annotationGraphDaoHelper.createSqlQuery(TEXT_ID));
	}
	
	// return a slice of a list
	@Test
	public void slice() {
		// slice elements 2 and 3
		int offset = 1;
		int length = 2;
		List<Match> slice = annotationGraphDaoHelper.slice(matches, offset, length);
		assertThat(slice, isCollection(match2, match3));
	}
	
	// if the offset is larger than the list size, return empty list
	@Test
	public void sliceOffsetToLarge() {
		// slice (non-existing) elements 6 and 7
		int offset = 5;
		int length = 2;
		List<Match> slice = annotationGraphDaoHelper.slice(matches, offset, length);
		assertThat(slice, is(empty()));
	}
	
	// if offset + limit is larger than list size, return remaining elements
	@Test
	public void sliceAtEndOfList() {
		// slice 5 elements starting at 3
		int offset = 2;
		int length = 5;
		List<Match> slice = annotationGraphDaoHelper.slice(matches, offset, length);
		assertThat(slice, isCollection(match3, match4, match5));
	}
	
	// if length = 0, return entire list
	@Test
	public void sliceDontslice() {
		int offset = 2;
		int length = 0;
		List<Match> slice = annotationGraphDaoHelper.slice(matches, offset, length);
		assertThat(slice, isCollection(match1, match2, match3, match4, match5));
	}
	
	// first column is the match group key
	// return one annotation graph for each distinct key
	@Test
	public void count() throws SQLException {
		// 3 rows with different keys
		stubRowCount(3);
		when(matchGroupRowMapper.mapRow(any(ResultSet.class), anyInt())).thenReturn("1", "2", "3");
		
		// test and call
		assertThat(annotationGraphDaoHelper.extractData(resultSet), size(3));
	}

	// throw exception if key is null
	@Test(expected=IllegalArgumentException.class)
	public void badKeyNull() throws SQLException {
		// 1 row: key is null
		stubRowCount(1);
		when(matchGroupRowMapper.mapRow(any(ResultSet.class), anyInt())).thenReturn(null);
		annotationGraphDaoHelper.extractData(resultSet);
	}
	
	// throw exception if key can't be split into ids
	@Test(expected=IllegalArgumentException.class)
	public void badKeyNoLongs() throws SQLException {
		// 1 row: key is "1,abc"
		stubRowCount(1);
		when(matchGroupRowMapper.mapRow(any(ResultSet.class), anyInt())).thenReturn("1,abc");
		annotationGraphDaoHelper.extractData(resultSet);
	}
	
	// node id different -> add to annotation graph
	@Test
	public void addNodesAndEdgesToGraph() throws SQLException {
		// 2 rows: different node ids
		stubRowCount(2);
		when(annisNodeRowMapper.mapRow(any(ResultSet.class), anyInt())).thenReturn(newNode(1), newNode(2));
		when(edgeRowMapper.mapRow(any(ResultSet.class), anyInt())).thenReturn(newEdge(null, newNode(1), 1, 2), newEdge(null, newNode(2), 3, 4));
		
		// expected graph
		AnnisNode node1 = newNode(1);
		AnnisNode node2 = newNode(2);
		Edge edge1 = newEdge(null, newNode(1), 1, 2);
		Edge edge2 = newEdge(null, newNode(2), 3, 4);
		
		// test and call
		assertThat(annotationGraphDaoHelper.extractData(resultSet), containsGraph(0, withNodes(node1, node2), withEdges(edge1, edge2)));
	}

	// assume that result set is sorted by key, pre
	// node and edge mapping functions have to be cleared for new match group
	@Test
	public void clearNodesAndEdgesOnNewGraph() throws SQLException {
		// 3 rows: new graph, same node, same edge
		stubRowCount(3);
		when(matchGroupRowMapper.mapRow(any(ResultSet.class), anyInt())).thenReturn("1", "2", "3");
		when(annisNodeRowMapper.mapRow(any(ResultSet.class), anyInt())).thenReturn(newNode(1), newNode(1), newNode(1));
		when(edgeRowMapper.mapRow(any(ResultSet.class), anyInt())).thenReturn(newEdge(null, newNode(1), 1, 2));
		
		// expected graph
		AnnisNode node = newNode(1);
		Edge edge = newEdge(null, newNode(1), 1, 2);
		
		// test and call
		List<AnnotationGraph> graphs = annotationGraphDaoHelper.extractData(resultSet);
		assertThat(graphs, containsGraph(0, withNodes(node), withEdges(edge)));
		assertThat(graphs, containsGraph(1, withNodes(node), withEdges(edge)));
		assertThat(graphs, containsGraph(2, withNodes(node), withEdges(edge)));
	}
	
	// same key, node, different node annotation -> annotations added to node
	@Test
	public void mergeNodeAnnotations() throws SQLException {
		// 2 rows: same node, 2 annotations
		stubRowCount(2);
		when(nodeAnnotationRowMapper.mapRow(any(ResultSet.class), anyInt())).thenReturn(ANNOTATION1, ANNOTATION2);
		
		// expected
		AnnisNode node = newNode(1);
		node.addNodeAnnotation(ANNOTATION1);
		node.addNodeAnnotation(ANNOTATION2);
		
		// test and call
		assertThat(annotationGraphDaoHelper.extractData(resultSet), containsGraph(0, withNodes(node), null));
	}

	// same key, node, different edge annotation -> annotations added to edges
	@Test
	public void mergeEdgeAnnotations() throws SQLException {
		// 2 rows: same node, 2 edge annotations
		stubRowCount(2);
		
		when(edgeAnnotationRowMapper.mapRow(any(ResultSet.class), anyInt())).thenReturn(ANNOTATION1, ANNOTATION2);
		
		// expected
		Edge edge = newEdge(null, newNode(1), 1, 2);
		edge.addAnnotation(ANNOTATION1);
		edge.addAnnotation(ANNOTATION2);
		
		// test and call
		assertThat(annotationGraphDaoHelper.extractData(resultSet), containsGraph(0, null, withEdges(edge)));
	}
	
	private final static long NODE_REF1 = 1;
	private final static long NODE_REF2 = 2;
	private final static long PARENT = 3;

	// fix IDs of source and destination node (mapRow saves rank value)
	@Test
	public void fixNodes() {
		// an edge whose source node has a rank value as ID and destination node has an ID but no other data
		Edge edge = new Edge();
		edge.setSource(new AnnisNode(PARENT));
		edge.setDestination(new AnnisNode(NODE_REF2));
		
		// this is the node that should be edge.source
		AnnisNode expectedSource = new AnnisNode(NODE_REF1);
		
		// the destination node has to be pulled from nodeById
		AnnisNode expectedDestination = new AnnisNode(NODE_REF2);
		
		// setup mapping functions
		Map<Long, AnnisNode> nodeById = new HashMap<Long, AnnisNode>();
		nodeById.put(NODE_REF2, expectedDestination);
		
		Map<Long, Edge> edgeByPre = new HashMap<Long, Edge>();
		Edge parentEdge = new Edge();
		parentEdge.setDestination(expectedSource);
		edgeByPre.put(PARENT, parentEdge);
		
		// call and test
		annotationGraphDaoHelper.fixNodes(edge, edgeByPre, nodeById);
		assertThat(edge.getSource(), sameInstance(expectedSource));
		assertThat(edge.getDestination(), sameInstance(expectedDestination));
	}

	// it's possible that a node that is referenced as a parent is not in the result set
	// (for example pointing exceptions)
	@Test
	public void fixNodesIncomingEdgeIncomplete() {
		// an edge whose source node has a rank value as ID and destination node has an ID but no other data
		Edge edge = new Edge();
		edge.setDestination(new AnnisNode(NODE_REF2));
		
		// the destination node has to be pulled from nodeById
		AnnisNode expectedDestination = new AnnisNode(NODE_REF2);
		
		// setup mapping functions
		Map<Long, AnnisNode> nodeById = new HashMap<Long, AnnisNode>();
		nodeById.put(NODE_REF2, expectedDestination);

		
		Map<Long, Edge> edgeByPre = new HashMap<Long, Edge>();
		edge.setSource(new AnnisNode(PARENT));		// unknown parent, edgeByPre returns null
		
		// call and test
		annotationGraphDaoHelper.fixNodes(edge, edgeByPre, nodeById);
		assertThat(edge.getSource(), is(nullValue()));
		assertThat(edge.getDestination(), sameInstance(expectedDestination));
	}
	
	// construct edges according to pre- and post order
	@Test
	public void trees() throws SQLException {
		// row 1: root node	
		// row 2 + 3: children of 1
		// row 4: new root node
		final int PRE1 = 1; final int POST1 = 6; final int ID1 = 1;
		final int PRE2 = 2; final int POST2 = 3; final int ID2 = 2;
		final int PRE3 = 4; final int POST3 = 5; final int ID3 = 3;
		final int PRE4 = 7; final int POST4 = 8; final int ID4 = 4;
		
		stubRowCount(4);
		when(annisNodeRowMapper.mapRow(any(ResultSet.class), anyInt())).thenReturn(
				newNode(ID1), newNode(ID2), newNode(ID3), newNode(ID4));
		when(edgeRowMapper.mapRow(any(ResultSet.class), anyInt())).thenReturn(
				newEdge(null, newNode(ID1), PRE1, POST1),
				newEdge(newNode(PRE1), newNode(ID2), PRE2, POST2),
				newEdge(newNode(PRE1), newNode(ID3), PRE3, POST3),
				newEdge(null, newNode(ID4), PRE4, POST4)
		);
		
		// expected
		AnnisNode node1 = newNode(ID1);
		AnnisNode node2 = newNode(ID2);
		AnnisNode node3 = newNode(ID3);
		AnnisNode node4 = newNode(ID4);
		Edge edge1 = newEdge(null, node1, PRE1, POST1);
		Edge edge2 = newEdge(node1, node2, PRE2, POST2);
		Edge edge3 = newEdge(node1, node3, PRE3, POST3);
		Edge edge4 = newEdge(null, node4, PRE4, POST4);
		
		assertThat(annotationGraphDaoHelper.extractData(resultSet), containsGraph(0,
				withNodes(node1, node2, node3, node4),
				withEdges(edge1, edge2, edge3, edge4)));
	}
	
	// column key contains ids of matched nodes => mark these in the graph
	@Test
	public void markMatchedNodes() throws SQLException {
		// 1 row: key column is array of ids
		stubRowCount(1);
		final long ID1 = 1;
		final long ID2 = 2;
		final long ID3 = 3;
		final String KEY = ID1 + "," + ID2 + "," + ID3;
		when(matchGroupRowMapper.mapRow(any(ResultSet.class), anyInt())).thenReturn(KEY);
		
		// call and test
		AnnotationGraph graph = annotationGraphDaoHelper.extractData(resultSet).get(0);
		assertThat(graph.getMatchedNodeIds(), is((Set<Long>) new HashSet<Long>(Arrays.asList(ID1, ID2, ID3))));
	}
	
	///// private helper

	private void stubRowCount(final int rows) throws SQLException {
		when(resultSet.next()).thenAnswer(new Answer<Boolean>() {

			private int count = 0;

			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				++count;
				if (count <= rows)
					return true;
				if (count == rows + 1)
					return false;
				fail("next called one too many times (" + count + ")");
				return false;
			}

		});
	}

	private AnnisNode newNode(int id) {
		return new AnnisNode(id);
	}

	private Edge newEdge(AnnisNode source, AnnisNode destination, int pre, int post) {
		Edge edge = new Edge();
		
		edge.setSource(source);
		edge.setDestination(destination);
		edge.setPre(pre);
		edge.setPost(post);
		
		return edge;
	}
	
	private Edge newEdge(int pre, int post) {
		return newEdge(null, null, pre, post);
	}

	private List<AnnisNode> withNodes(AnnisNode... nodes) {
		if (nodes.length == 0)
			return Arrays.asList(newNode(1));
		return Arrays.asList(nodes);
	}

	private List<Edge> withEdges(Edge... edges) {
		if (edges.length == 0)
			return Arrays.asList(newEdge(1, 2));
		return Arrays.asList(edges);
	}

	private Matcher<List<AnnotationGraph>> containsGraph(final int index, final List<AnnisNode> _nodes, final List<Edge> _edges) {
		return new TypeSafeMatcher<List<AnnotationGraph>>() {
	
			@Override
			public boolean matchesSafely(List<AnnotationGraph> item) {
				if (index >= item.size())
					fail("no graph with index " + index);
				AnnotationGraph graph = item.get(index);
				List<AnnisNode> nodes = graph.getNodes();
				final boolean sameNodes = _nodes == null || _nodes.equals(nodes);
				List<Edge> edges = graph.getEdges();
				final boolean sameEdges = _edges == null || _edges.equals(edges);
				
				if ( ! sameNodes || ! sameEdges )
					return false;
				
				if (_edges == null || _nodes == null)
					return true;
				
				// source has outgoing, dest has incoming
				List<AnnisNode> destNodes = new ArrayList<AnnisNode>();
				for (Edge edge : edges) {
					AnnisNode src = edge.getSource();
					AnnisNode dst = edge.getDestination();
					destNodes.add(dst);
					if (src != null)
						assertThat("edge not in src.outgoing", src.getOutgoingEdges(), hasItem(edge));
					assertThat("edge not in dst.incoming", dst.getIncomingEdges(), hasItem(edge));
				}
				
				// all edge destinations in nodes
				for (AnnisNode node : destNodes)
					assertThat(nodes, hasItem(node));
				
				// nodes are linked to graph
				for (AnnisNode node : nodes)
					assertThat("node not linked to graph", node.getGraph(), is(graph));
				
				return true;
			}
	
			public void describeTo(Description description) {
				description.appendText("graph " + index);
				List<String> desc = new ArrayList<String>();
				if (_nodes != null)  {
					desc.add("nodes: " + _nodes);
				}
				if (_edges != null) {
					desc.add("edges: " + _edges);
				}
				if (desc.size() > 0) {
					description.appendText(" with ");
					description.appendText(StringUtils.join(desc, " and "));
				}
			}
		};
	}
	
}
