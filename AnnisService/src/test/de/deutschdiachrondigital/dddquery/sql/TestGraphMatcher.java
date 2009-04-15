package de.deutschdiachrondigital.dddquery.sql;

import static de.deutschdiachrondigital.dddquery.helper.TestHelpers.uniqueInt;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.MockitoAnnotations.Mock;

import de.deutschdiachrondigital.dddquery.analysis.DepthFirstAdapter;
import de.deutschdiachrondigital.dddquery.helper.QueryExecution;
import de.deutschdiachrondigital.dddquery.helper.ResultSetConverter;
import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.parser.DddQueryParser;
import de.deutschdiachrondigital.dddquery.sql.GraphMatcher.MatchFilter;
import de.deutschdiachrondigital.dddquery.sql.model.Graph;

public class TestGraphMatcher {

	private final int NUM_MATCHES = 10; 
	private final String DDD_QUERY = "sample DDDquery";
	private final Start SYNTAX_TREE = new Start();
	private final Graph GRAPH = new Graph();
	private final String SQL_QUERY = "sample SQL query";
	private final ResultSet RESULT_SET = new MockResultSet();
	private final List<Match> MATCHES = new ArrayList<Match>();
	
	@Mock private DddQueryParser parser;
	@Mock private DepthFirstAdapter preProcessor;
	@Mock private GraphTranslator graphTranslator;
	@Mock private SqlGenerator sqlGenerator;
	@Mock private QueryExecution queryExecution;
	@Mock private GraphMatcher.MatchFilter matchFilter;
	@Mock private ResultSetConverter<List<Match>> resultSetConverter;
	@Mock private GraphMatcher graphMatcher;

	@Before
	public void setupMatches() {
		for (int i = 0; i < NUM_MATCHES; ++i)
			MATCHES.add(new Match(Arrays.asList(new Node(uniqueInt(), uniqueInt(), uniqueInt(), uniqueInt()))));
	}
	
	@Before 
	public void setupDependencies() {
		initMocks(this);
		
		stub(parser.parseDddQuery(DDD_QUERY)).toReturn(SYNTAX_TREE);
		stub(graphTranslator.translate(SYNTAX_TREE)).toReturn(GRAPH);
		stub(sqlGenerator.translate(GRAPH)).toReturn(SQL_QUERY);
		stub(queryExecution.executeQuery(SQL_QUERY)).toReturn(RESULT_SET);
		stub(resultSetConverter.convertResultSet(RESULT_SET)).toReturn(MATCHES);
		for (Match match : MATCHES)
			stub(matchFilter.filterMatch(match)).toReturn(false);
		
		graphMatcher = new GraphMatcher();
		graphMatcher.setParser(parser);
		graphMatcher.setPreProcessors(Arrays.asList(preProcessor));
		graphMatcher.setGraphTranslator(graphTranslator);
		graphMatcher.setSqlGenerator(sqlGenerator);
		graphMatcher.setQueryExecution(queryExecution);
		graphMatcher.setResultSetConverter(resultSetConverter);
		graphMatcher.setMatchFilters(Arrays.asList(matchFilter));
	}
	
	public interface Bla {
		public void doStuff();
	}
	
	@Test
	public void matchGraphWiring() {
		List<Match> actual = graphMatcher.matchGraph(DDD_QUERY);

		assertThat(actual, is(MATCHES));

		final int[] ints = { 1, 2, 3 };	
		
		for (final int i : ints) {
			Bla bla = new Bla() {
				public void doStuff() {
					System.out.println(i);
				}
			};
			bla.doStuff();
		}

		InOrder inOrder = inOrder(parser, preProcessor, graphTranslator, sqlGenerator, queryExecution, resultSetConverter, matchFilter);
		inOrder.verify(parser).parseDddQuery(DDD_QUERY);
		inOrder.verify(preProcessor).caseStart(SYNTAX_TREE);
		inOrder.verify(graphTranslator).translate(SYNTAX_TREE);
		inOrder.verify(sqlGenerator).translate(GRAPH);
		inOrder.verify(queryExecution).executeQuery(SQL_QUERY);
		inOrder.verify(resultSetConverter).convertResultSet(RESULT_SET);
		inOrder.verify(matchFilter).init();
		for (Match match : MATCHES)
			inOrder.verify(matchFilter).filterMatch(match);
		verifyNoMoreInteractions(parser, preProcessor, graphTranslator, sqlGenerator, queryExecution, resultSetConverter, matchFilter);
	}
	
	@Test
	public void retrieveAnnotationsFiltersMatches() {
		// stub match filter to filter every second match
		for (int i = 0; i < NUM_MATCHES; ++i)
			stub(matchFilter.filterMatch(MATCHES.get(i))).toReturn(i % 2 == 0);
		
		// create list with every other match (all that should not have been filtered)
		List<Match> matches = new ArrayList<Match>();
		for (int i = 0; i < NUM_MATCHES; ++i)
			if (i % 2 != 0)
				matches.add(MATCHES.get(i));

		// test
		assertThat(graphMatcher.matchGraph(DDD_QUERY), is(matches));
	}
	
	@Test
	public void defaultResultSetConverter() throws SQLException {
		// a result set with 12 columns => 3 nodes per match
		final ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
		stub(resultSetMetaData.getColumnCount()).toReturn(12);

		// list with 36 unique ints => 36 / 12 = 3 matches 
		final List<Integer> ints = new ArrayList<Integer>();
		for (int i = 0; i < 36; ++i)
			ints.add(uniqueInt());
		
		// mock result set that returns the ints in order and next() returns true until there no more ints left
		final ResultSet resultSet = new MockResultSet() {
			final Iterator<Integer> intsIt = ints.iterator();
			
			@Override
			public ResultSetMetaData getMetaData() throws SQLException {
				return resultSetMetaData;
			}
			
			@Override
			public int getInt(int columnIndex) throws SQLException {
				return intsIt.next();
			}
			
			@Override
			public boolean next() throws SQLException {
				return intsIt.hasNext();
			}
		};
		
		// expected: 3 matches with 3 nodes each (each node uses 4 ints of the resultset)
		// calls to ResultSet.getInt() are in order
		final Iterator<Integer> intsIt = ints.iterator();
		List<Match> expected = new ArrayList<Match>();
		for (int i = 0; i < 3; ++i)
			expected.add(new Match(Arrays.asList(new Node[] { 
					new Node(intsIt.next(), intsIt.next(), intsIt.next(), intsIt.next()),
					new Node(intsIt.next(), intsIt.next(), intsIt.next(), intsIt.next()),
					new Node(intsIt.next(), intsIt.next(), intsIt.next(), intsIt.next()) } )));
		
		// test
		ResultSetConverter<List<Match>> resultSetConverter = new GraphMatcher.DefaultResultSetConverter();
		
		assertThat(resultSetConverter.convertResultSet(resultSet), is(expected));
	}

	@Test
	public void multipleTextFilterFalse() {
		// a match with 3 nodes and all have the same textRef
		int textRef = uniqueInt();
		Match match = new Match();
		for (int i = 0; i < 3; ++i)
			match.add(new Node(uniqueInt(), textRef, uniqueInt(), uniqueInt()));
		
		// DON'T filter this match
		MatchFilter filter = new GraphMatcher.MultipleTextsFilter();
		assertThat(filter.filterMatch(match), is(false));
	}
	
	@Test
	public void multipleTextFilterTrue() {
		// a match with 3 nodes and all have a different textRef
		Match match = new Match();
		for (int i = 0; i < 3; ++i)
			match.add(new Node(uniqueInt(), uniqueInt(), uniqueInt(), uniqueInt()));
		
		// do filter this match
		MatchFilter filter = new GraphMatcher.MultipleTextsFilter();
		assertThat(filter.filterMatch(match), is(true));
	}
	
	@Test
	public void sameMatchFilterFalse() {
		// 3 matches, all different
		Match match1 = new Match(Arrays.asList(new Node(uniqueInt(), uniqueInt(), uniqueInt(), uniqueInt())));
		Match match2 = new Match(Arrays.asList(new Node(uniqueInt(), uniqueInt(), uniqueInt(), uniqueInt())));
		Match match3 = new Match(Arrays.asList(new Node(uniqueInt(), uniqueInt(), uniqueInt(), uniqueInt())));
		
		MatchFilter filter = new GraphMatcher.DuplicateMatchFilter();
		filter.init();
		
		// DON'T filter any match
		assertThat(filter.filterMatch(match1), is(false));
		assertThat(filter.filterMatch(match2), is(false));
		assertThat(filter.filterMatch(match3), is(false));
	}
	
	@Test
	public void sameMatchFilterTrue() {
		// 3 matches, first 2 have different struct_id, but same text_ref, token_left, token_right, last is completely different
		final int TEXT_REF = uniqueInt();
		final int TOKEN_RIGHT = uniqueInt();
		final int TOKEN_LEFT = uniqueInt();
		Match match1 = new Match(Arrays.asList(new Node(uniqueInt(), TEXT_REF, TOKEN_LEFT, TOKEN_RIGHT)));
		Match match2 = new Match(Arrays.asList(new Node(uniqueInt(), TEXT_REF, TOKEN_LEFT, TOKEN_RIGHT)));
		Match match3 = new Match(Arrays.asList(new Node(uniqueInt(), uniqueInt(), uniqueInt(), uniqueInt())));

		MatchFilter filter = new GraphMatcher.DuplicateMatchFilter();
		filter.init();

		// filter the second match
		assertThat(filter.filterMatch(match1), is(false));
		assertThat(filter.filterMatch(match2), is(true));
		assertThat(filter.filterMatch(match3), is(false));
	}

}
