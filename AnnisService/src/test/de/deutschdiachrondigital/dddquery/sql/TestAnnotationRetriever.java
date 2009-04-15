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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.MockitoAnnotations.Mock;

import de.deutschdiachrondigital.dddquery.helper.QueryExecution;
import de.deutschdiachrondigital.dddquery.sql.AnnotationRetriever.SqlGenerator;

public class TestAnnotationRetriever {

	// some constants to test wiring
	private final int NUM_MATCHES = 10;
	private final List<Match> MATCHES = new ArrayList<Match>();
	private final int LEFT = uniqueInt();
	private final int RIGHT = uniqueInt();
	private final String SQL_QUERY = "SELECT ...";
	private final ResultSet RESULT_SET = new MockResultSet();
	
	@Before
	public void setupMatches() {
		for (int i = 0; i < NUM_MATCHES; ++i)
			MATCHES.add(new Match(Arrays.asList(new Node(uniqueInt(), uniqueInt(), uniqueInt(), uniqueInt()))));
	}
	
	// object under test
	private AnnotationRetriever annotationRetriever;
	
	// dependencies
	@Mock private SqlGenerator sqlGenerator;
	@Mock private QueryExecution queryExecution;
	
	@Before
	public void setupDependencies() {
		// create mocks
		initMocks(this);
		
		sqlGenerator = mock(SqlGenerator.class);
		queryExecution = mock(QueryExecution.class);
		
		// stub query execution to always expect our dummy sql query and return the dummy result set
		stub(queryExecution.executeQuery(SQL_QUERY)).toReturn(RESULT_SET);
		
		// wiring
		annotationRetriever = new AnnotationRetriever();
		annotationRetriever.setSqlGenerator(sqlGenerator);
		annotationRetriever.setQueryExecution(queryExecution);
	}
	
	private void assertCallsAndResults(List<Match> matches, ResultSet actual) {
		InOrder inOrder = inOrder(sqlGenerator, queryExecution);
		
		// match list and left, right passed to sql generator
		inOrder.verify(sqlGenerator).generateSql(matches, LEFT, RIGHT);
		// sql query passed to query execution
		inOrder.verify(queryExecution).executeQuery(SQL_QUERY);
		// no more interactions
		verifyNoMoreInteractions(sqlGenerator, queryExecution);
		
		// correct result set returned
		assertThat(actual, is(RESULT_SET));
	}
	
	@Test
	public void retrieveAnnotationsNoLimitOffset() {
		// stub the sql generator to expect the entire match list
		stub(sqlGenerator.generateSql(MATCHES, LEFT, RIGHT)).toReturn(SQL_QUERY);

		// test
		ResultSet actual = annotationRetriever.retrieveAnnotations(MATCHES, LEFT, RIGHT);
		assertCallsAndResults(MATCHES, actual);
	}
	
	@Test
	public void retrieveAnnotationsWithLimitOffSet() {
		// create slice of the match list
		int limit = 5;
		int offset = 3;
		assertThat(limit + offset < NUM_MATCHES, is(true));
		List<Match> matches = MATCHES.subList(offset, offset + limit);
		
		// stub the sql generator to expect this slice
		stub(sqlGenerator.generateSql(matches, LEFT, RIGHT)).toReturn(SQL_QUERY);
		
		// test
		ResultSet actual = annotationRetriever.retrieveAnnotations(MATCHES, LEFT, RIGHT, limit, offset);
		assertCallsAndResults(matches, actual);
	}

	@Test (expected=DddException.class)
	public void retrieveAnnotationsOffsetTooLarge() {
		// offset outside range (offset > NUM_MATCHES)
		int limit = uniqueInt();
		int offset = NUM_MATCHES + uniqueInt();
		assertThat(offset > NUM_MATCHES, is(true));
		
		// this should throw an exception
		annotationRetriever.retrieveAnnotations(MATCHES, LEFT, RIGHT, limit, offset);
	}
	
	@Test
	public void retrieveAnnotationsOffsetPlusLimitTooLarge() {
		// offset is in range, but offset + limit isn't (offset + limit > NUM_MATCHES)
		int offset = NUM_MATCHES / 2;
		int limit = NUM_MATCHES - 1;
		assertThat(offset > NUM_MATCHES, is(false));
		assertThat(offset + limit > NUM_MATCHES, is(true));
		
		// expect a sublist from offset to the end of the original list
		List<Match> matches = MATCHES.subList(offset, MATCHES.size());
		stub(sqlGenerator.generateSql(matches, LEFT, RIGHT)).toReturn(SQL_QUERY);
		
		// test
		ResultSet actual = annotationRetriever.retrieveAnnotations(MATCHES, LEFT, RIGHT, limit, offset);
		assertCallsAndResults(matches, actual);
	}
	
}
