package annisservice;

import static de.deutschdiachrondigital.dddquery.helper.IsCollection.isCollection;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import annisservice.AnnisServiceImpl.CacheKey;
import annisservice.exceptions.AnnisServiceException;
import annisservice.ifaces.AnnisResultSet;
import annisservice.objects.AnnisResultSetImpl;
import de.deutschdiachrondigital.dddquery.helper.AnnisQlTranslator;
import de.deutschdiachrondigital.dddquery.sql.AnnotationRetriever;
import de.deutschdiachrondigital.dddquery.sql.GraphMatcher;
import de.deutschdiachrondigital.dddquery.sql.Match;
import de.deutschdiachrondigital.dddquery.sql.MockResultSet;

public class TestAnnisServiceImpl {

	private AnnisServiceImpl annisService;
	
	private final List<Long> mockCorpusList = Arrays.asList(2L, 3L, 5L, 7L);
	private final String mockAnnisQuery = "annis query";
	private final String mockDddQuery = "ddd query";
	private final List<Match> mockMatches = Arrays.asList(new Match(), new Match());
	private final ResultSet mockResultSet = new MockResultSet();
	private final AnnisResultSet mockAnnisResultSet = new AnnisResultSetImpl();
	
	@Before
	public void setup() throws RemoteException {
		annisService = new AnnisServiceImpl();
		annisService.setCorpusList(mockCorpusList);
		
		annisService.setDddQueryMapper(new AnnisQlTranslator() {
			@Override
			public String translate(String input) {
				assertThat(input, sameInstance(mockAnnisQuery));
				return mockDddQuery;
			}
		});
		
		annisService.setGraphMatcher(new GraphMatcher() {
			@Override
			public List<Match> matchGraph(String dddQuery) {
				assertThat(dddQuery, sameInstance(mockDddQuery));
				return mockMatches;
			}
		});
		
		annisService.setAnnotationRetriever(new AnnotationRetriever() {
			@Override
			public ResultSet retrieveAnnotations(List<Match> matches, int left, int right) {
				assertThat(matches, is(mockMatches));
				assertThat(left, is(123));
				assertThat(right, is(321));
				return mockResultSet;
			}
		});

		annisService.setAnnisResultSetBuilder(new AnnisResultSetBuilder() {
			@Override
			public AnnisResultSet buildResultSet(ResultSet resultSet) {
				assertThat(resultSet, sameInstance(mockResultSet));
				return mockAnnisResultSet;
			}
		});
	}
	
	@Test(expected=AnnisServiceException.class)
	public void getCountCorpusListEmpty() {
		annisService.getCount(new ArrayList<Long>(), "query");
	}
	
	@Test(expected=AnnisServiceException.class)
	public void getCountCorpusListUnknownCorpus() {
		annisService.getCount(Arrays.asList(new Long[] { 2L, 3L, 5L, 8L } ), "query");
	}
	
	@Test
	public void getCountControlFlow() {
		int actual = annisService.getCount(mockCorpusList, mockAnnisQuery);
		assertThat(actual, is(mockMatches.size()));
	}
	
	@Test
	public void getCountSavesMatches() {
		Map<CacheKey, List<Match>> cache = annisService.getResultCache();
		assertThat(cache.isEmpty(), is(true));
		
		annisService.getCount(mockCorpusList, mockAnnisQuery);

		assertThat(cache, hasEntry(new CacheKey(mockCorpusList, mockAnnisQuery), mockMatches));
	}
	
	@Test
	public void getCountUsesCache() {
		Map<CacheKey, List<Match>> cache = annisService.getResultCache();
		cache.put(new CacheKey(mockCorpusList, mockAnnisQuery), mockMatches);
		
		annisService.setDddQueryMapper(new AnnisQlTranslator() {
			@Override
			public String translate(String input) {
				throw new AssertionError("query processed, but there should be a cached result");
			}
		});
		
		int actual = annisService.getCount(mockCorpusList, mockAnnisQuery);
		assertThat(actual, is(mockMatches.size()));
	}
	
	@Test(expected=AnnisServiceException.class)
	public void getResultSetMissingCachedResult() {
		annisService.getResultSet(mockCorpusList, mockAnnisQuery, 0, 0, 0, 0);
	}
	
	@Test
	public void getResultSetControlFlow() {
		annisService.getCount(mockCorpusList, mockAnnisQuery);
		AnnisResultSet resultSet = annisService.getResultSet(mockCorpusList, mockAnnisQuery, 2, 1, 123, 321);
		assertThat(resultSet, sameInstance(mockAnnisResultSet));
	}
	
	@Test
	public void getResultSetSlicesLimitOffset() {
		// setup 5 matches
		final Match match1 = new Match();
		final Match match2 = new Match();
		final Match match3 = new Match();
		final Match match4 = new Match();
		final Match match5 = new Match();
		List<Match> matches = Arrays.asList( new Match[] { match1, match2, match3, match4, match5 } );
		
		// setup cache
		Map<CacheKey, List<Match>> cache = annisService.getResultCache();
		cache.put(new CacheKey(mockCorpusList, mockAnnisQuery), matches);
		
		// check for correct slice of the match list
		annisService.setAnnotationRetriever(new AnnotationRetriever() {
			@Override
			public ResultSet retrieveAnnotations(List<Match> matches, int left, int right) {
				assertThat(matches, isCollection(match2, match3));
				return mockResultSet;
			}
		});
		
		// retrieve annotations with LIMIT = 2, OFFSET = 1
		annisService.getResultSet(mockCorpusList, mockAnnisQuery, 2, 1, 0, 0);
	}
	
	@Test
	public void getResultSetMaximumOffset() {
		// setup 5 matches
		final Match match1 = new Match();
		final Match match2 = new Match();
		final Match match3 = new Match();
		final Match match4 = new Match();
		final Match match5 = new Match();
		List<Match> matches = Arrays.asList( new Match[] { match1, match2, match3, match4, match5 } );
		
		// setup cache
		Map<CacheKey, List<Match>> cache = annisService.getResultCache();
		cache.put(new CacheKey(mockCorpusList, mockAnnisQuery), matches);
		
		// check for correct slice of the match list
		annisService.setAnnotationRetriever(new AnnotationRetriever() {
			@Override
			public ResultSet retrieveAnnotations(List<Match> matches, int left, int right) {
				assertThat(matches, isCollection(match3, match4, match5));
				return mockResultSet;
			}
		});
		
		// retrieve annotations with LIMIT = 2, OFFSET = 1
		annisService.getResultSet(mockCorpusList, mockAnnisQuery, 25, 3, 0, 0);
	}
}
