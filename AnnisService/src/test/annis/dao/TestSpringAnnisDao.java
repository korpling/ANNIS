package annis.dao;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static test.IsCollectionEmpty.empty;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import test.TestHelper;
import annis.AnnisHomeTest;
import annis.WekaDaoHelper;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.service.ifaces.AnnisAttribute;
import annis.service.ifaces.AnnisCorpus;
import annis.sqlgen.ListCorpusAnnotationsSqlHelper;
import annis.sqlgen.ListCorpusSqlHelper;
import annis.sqlgen.ListNodeAnnotationsSqlHelper;
import annis.sqlgen.SqlGenerator;
import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.parser.DddQueryParser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"SpringAnnisDao-context.xml"})
public class TestSpringAnnisDao extends AnnisHomeTest {

	// SpringAnnisDao instance that is managed by Spring
	@Autowired private AnnisDao springManagedAnnisDao;
	
	// simple SpringDao instance with mocked dependencies
	private SpringAnnisDao annisDao;
	@Mock private DddQueryParser dddQueryParser;
	@Mock private SqlGenerator sqlGenerator;
	@Mock private ParameterizedSingleColumnRowMapper<String> planRowMapper;
	@Mock private MatchRowMapper matchRowMapper;
	@Mock private JdbcTemplate jdbcTemplate;
//	@Mock private AnnisResultSetBuilder annisResultSetBuilder;
	@Mock private AnnotationGraphDaoHelper annotationGraphHelper;
	@Mock private WekaDaoHelper wekaHelper;
	@Mock private ListCorpusSqlHelper listCorpusHelper;
	@Mock private ListNodeAnnotationsSqlHelper listNodeAnnotationsSqlHelper;
	@Mock private ListCorpusAnnotationsSqlHelper listCorpusAnnotationsHelper;
	@Mock private CorpusSelectionStrategyFactory corpusSelectionStrategyFactory;
	@Mock private CorpusSelectionStrategy corpusSelectionStrategy;
	@Mock private PlatformTransactionManager transactionManager;

	// constants for flow control verification
	private static final String DDDQUERY = "DDDQUERY";
	private static final Start STATEMENT = new Start();
	private static final String SQL = "SQL";
	private static final List<Long> CORPUS_LIST = new ArrayList<Long>();
	@Mock private List<Match> MATCHES;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		initMocks(this);
		annisDao = new SpringAnnisDao();
		annisDao.setDddQueryParser(dddQueryParser);
		annisDao.setSqlGenerator(sqlGenerator);
		annisDao.setMatchRowMapper(matchRowMapper);
		annisDao.setPlanRowMapper(planRowMapper);
		annisDao.setJdbcTemplate(jdbcTemplate);
		annisDao.setAnnotationGraphDaoHelper(annotationGraphHelper);
		annisDao.setWekaSqlHelper(wekaHelper);
		annisDao.setListCorpusSqlHelper(listCorpusHelper);
		annisDao.setListNodeAnnotationsSqlHelper(listNodeAnnotationsSqlHelper);
		annisDao.setListCorpusAnnotationsSqlHelper(listCorpusAnnotationsHelper);
		annisDao.setCorpusSelectionStrategyFactory(corpusSelectionStrategyFactory);
		annisDao.setTransactionManager(transactionManager);

		when(dddQueryParser.parse(DDDQUERY)).thenReturn(STATEMENT);
		when(sqlGenerator.toSql(STATEMENT, corpusSelectionStrategy)).thenReturn(SQL);
		when(corpusSelectionStrategyFactory.createCorpusSelectionStrategy(CORPUS_LIST)).thenReturn(corpusSelectionStrategy);
		when(corpusSelectionStrategyFactory.createCorpusSelectionStrategy(anyList())).thenReturn(corpusSelectionStrategy);
		TransactionStatus status = mock(TransactionStatus.class);
		when(transactionManager.getTransaction(any(TransactionDefinition.class))).thenReturn(status);
	}
	
	// check dependencies
	@Test
	public void springManagedInstanceHasAllDependencies() {
		SpringAnnisDao springAnnisDao = (SpringAnnisDao) TestHelper.proxyTarget(springManagedAnnisDao);
		assertThat(springAnnisDao.getSimpleJdbcTemplate(), is(not(nullValue())));
		assertThat(springAnnisDao.getDddQueryParser(), is(not(nullValue())));
		assertThat(springAnnisDao.getSqlGenerator(), is(not(nullValue())));
		assertThat(springAnnisDao.getMatchFilters(), is(not(nullValue())));
		assertThat(springAnnisDao.getMatchRowMapper(), is(not(nullValue())));
		assertThat(springAnnisDao.getPlanRowMapper(), is(not(nullValue())));
		assertThat(springAnnisDao.getAnnotationGraphDaoHelper(), is(not(nullValue())));
		assertThat(springAnnisDao.getListCorpusSqlHelper(), is(not(nullValue())));
		assertThat(springAnnisDao.getListCorpusAnnotationsSqlHelper(), is(not(nullValue())));
		assertThat(springAnnisDao.getCorpusSelectionStrategyFactory(), is(not(nullValue())));
		assertThat(springAnnisDao.getTransactionManager(), is(not(nullValue())));
		assertThat(springAnnisDao.getWekaSqlHelper(), is(not(nullValue())));
		assertThat(springAnnisDao.getListNodeAnnotationsSqlHelper(), is(not(nullValue())));
	}

	@Test
	public void findWithoutView() {
		// stub corpus selection to not use views
		when(corpusSelectionStrategy.usesViews()).thenReturn(false);

		// find some matches
		annisDao.findMatches(CORPUS_LIST, DDDQUERY);
		
		InOrder inOrder = inOrder(sqlGenerator, corpusSelectionStrategy, jdbcTemplate);
		inOrder.verify(sqlGenerator).toSql(STATEMENT, corpusSelectionStrategy);
		inOrder.verify(corpusSelectionStrategy).usesViews();
		inOrder.verify(jdbcTemplate).query(SQL, matchRowMapper);
		verifyNoMoreInteractions(jdbcTemplate);
	}
	
	@Test
	public void findWithView() throws SQLException {
		
		// stub SQL for view creation and deletion
		final String CREATE_VIEW = "CREATE VIEW";
//		final String DROP_VIEW = "DROP VIEW";
		when(corpusSelectionStrategy.usesViews()).thenReturn(true);
		when(corpusSelectionStrategy.createViewSql()).thenReturn(CREATE_VIEW);
//		when(corpusSelectionStrategy.dropViewSql()).thenReturn(DROP_VIEW);

		// find some matches
		annisDao.findMatches(CORPUS_LIST, DDDQUERY);
		
		InOrder inOrder = inOrder(sqlGenerator, corpusSelectionStrategy, jdbcTemplate);
		inOrder.verify(sqlGenerator).toSql(STATEMENT, corpusSelectionStrategy);
		inOrder.verify(corpusSelectionStrategy).usesViews();
		inOrder.verify(corpusSelectionStrategy).createViewSql();
		inOrder.verify(jdbcTemplate).update(CREATE_VIEW);
		inOrder.verify(jdbcTemplate).query(SQL, matchRowMapper);
//		inOrder.verify(jdbcTemplate).update(DROP_VIEW);
		verifyNoMoreInteractions(jdbcTemplate);
	}
	
	// corpusList must not be null
	@Test(expected=IllegalArgumentException.class)
	public void findCorpusListNull() {
		annisDao.findMatches(null, DDDQUERY);
	}
	
	@Test
	public void plan() {
		String EXPLAIN_SQL = "EXPLAIN SQL";
		List<String> PLAN_ROWS = Arrays.asList("PLAN 1", "PLAN 2");
		String PLAN = "PLAN 1\nPLAN 2";
		when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(PLAN_ROWS);
		
		String test = annisDao.plan(DDDQUERY, CORPUS_LIST, false);
		assertThat(test, is(PLAN));
		
		verify(jdbcTemplate).query(EXPLAIN_SQL, planRowMapper);
	}
	
	@Test
	public void planAnalyze() {
		String EXPLAIN_SQL = "EXPLAIN ANALYZE SQL";
		List<String> PLAN_ROWS = Arrays.asList("PLAN 1", "PLAN 2");
		String PLAN = "PLAN 1\nPLAN 2";
		when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(PLAN_ROWS);
		
		assertThat(annisDao.plan(DDDQUERY, CORPUS_LIST, true), is(PLAN));

		verify(jdbcTemplate).query(EXPLAIN_SQL, planRowMapper);
	}
	
	// retrieve annotation graph for a number of matches
	@SuppressWarnings("unchecked")
	@Test
	public void retrieveAnnotationGraph() {
		// stub AnnotationGraphHelper to create a dummy SQL query and extract a dummy graph
		final List<AnnotationGraph> ANNOTATION_GRAPHS = mock(List.class);
		when(annotationGraphHelper.createSqlQuery(anyList(), anyInt(), anyInt())).thenReturn(SQL);
		when(jdbcTemplate.query(any(String.class), any(AnnotationGraphDaoHelper.class))).thenReturn(ANNOTATION_GRAPHS);
		
		// call and test
		assertThat(annisDao.retrieveAnnotationGraph(MATCHES, 1, 1), is(ANNOTATION_GRAPHS));
		verify(annotationGraphHelper).createSqlQuery(MATCHES, 1, 1);
		verify(jdbcTemplate).query(SQL, annotationGraphHelper);
	}
	
	// don't retrieve annotations if matches is empty
	@Test
	public void retrieveAnnotationGraphNoMatches() {
		List<AnnotationGraph> result = annisDao.retrieveAnnotationGraph(new ArrayList<Match>(), 0, 0);
		assertThat(result, is(empty()));
		verifyNoMoreInteractions(jdbcTemplate);
	}
	
	// retrieve annotation graph for a complete text
	@Test
	public void retrieveAnnotationGraphText() {
		final long TEXT_ID = 23;
		
		// stub AnnotationGraphHelper to create a dummy SQL query and extract a list with a dummy graph
		final AnnotationGraph GRAPH = mock(AnnotationGraph.class);
		when(annotationGraphHelper.createSqlQuery(anyLong())).thenReturn(SQL);
		when(jdbcTemplate.query(any(String.class), any(AnnotationGraphDaoHelper.class))).thenReturn(Arrays.asList(GRAPH));

		// call and test
		assertThat(annisDao.retrieveAnnotationGraph(TEXT_ID), is(GRAPH));
		verify(annotationGraphHelper).createSqlQuery(TEXT_ID);
		verify(jdbcTemplate).query(SQL, annotationGraphHelper);
	}
	
	// return null if text was not found
	@Test
	public void retrieveAnnotationGraphNoText() {
		when(jdbcTemplate.query(anyString(), any(AnnotationGraphDaoHelper.class))).thenReturn(new ArrayList<AnnotationGraph>());
		assertThat(annisDao.retrieveAnnotationGraph(0), is(nullValue()));
	}
	
	// expect only one graph per text
	@SuppressWarnings("unchecked")
	@Test(expected=IllegalStateException.class)
	public void retrieveAnnotationGraphTextMoreThanOneGraph() {
		// stub returned graph list with more than one entry
		final List<AnnotationGraph> GRAPHS = mock(List.class);
		when(GRAPHS.size()).thenReturn(2);
		when(jdbcTemplate.query(anyString(), any(AnnotationGraphDaoHelper.class))).thenReturn(GRAPHS);
		annisDao.retrieveAnnotationGraph(0);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void annotateMatches() {
		// stub JdbcTemplate to return a dummy list of AnnisNode
		final List<AnnisNode> ANNIS_NODES = mock(List.class);
		when(jdbcTemplate.query(anyString(), any(WekaDaoHelper.class))).thenReturn(ANNIS_NODES);

		// stub WekaHelper to create a dummy SQL query
		when(wekaHelper.createSqlQuery(anyList())).thenReturn(SQL);
		
		// call and test
		assertThat(annisDao.annotateMatches(MATCHES), is(ANNIS_NODES));
		verify(wekaHelper).createSqlQuery(MATCHES);
		verify(jdbcTemplate).query(SQL, wekaHelper);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void listCorpora() {
		// stub JdbcTemplate to return a list of AnnisCorpus
		final List<AnnisCorpus> CORPORA = mock(List.class);
		when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(CORPORA);

		// stub SQL query
		when(listCorpusHelper.createSqlQuery()).thenReturn(SQL);
		
		// call and test
		assertThat(annisDao.listCorpora(), is(CORPORA));
		verify(jdbcTemplate).query(SQL, listCorpusHelper);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void listCorpusAnnotations() {
		// stub JdbcTemplate to return a list of Annotation
		final List<Annotation> ANNOTATIONS = mock(List.class);
		when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(ANNOTATIONS);
		
		// stub SQL query
		final long ID = 42L;
		when(listCorpusAnnotationsHelper.createSqlQuery(anyLong())).thenReturn(SQL);
		
		// call and test
		assertThat(annisDao.listCorpusAnnotations(ID), is(ANNOTATIONS));
		verify(listCorpusAnnotationsHelper).createSqlQuery(ID);
		verify(jdbcTemplate).query(SQL, listCorpusAnnotationsHelper);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void listNodeAnnotations() {
		// stub JdbcTemplate to return a list of AnnisAttribute
		final List<AnnisAttribute> NODE_ANNOTATIONS = mock(List.class);
		when(jdbcTemplate.query(anyString(), any(ResultSetExtractor.class))).thenReturn(NODE_ANNOTATIONS);
		
		// stub SQL query
		when(listNodeAnnotationsSqlHelper.createSqlQuery(anyList(), anyBoolean())).thenReturn(SQL);
		
		// call and test
		assertThat(annisDao.listNodeAnnotations(CORPUS_LIST, false), is(NODE_ANNOTATIONS));
		verify(jdbcTemplate).query(SQL, listNodeAnnotationsSqlHelper);
	}
	
}
