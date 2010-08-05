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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import test.TestHelper;
import annis.AnnisHomeTest;
import annis.WekaDaoHelper;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.ql.parser.QueryAnalysis;
import annis.ql.parser.QueryData;
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
  @Mock private DefaultQueryExecutor defaultQueryExecutor;
  @Mock private GraphExtractor graphExtractor;
	@Mock private ParameterizedSingleColumnRowMapper<String> planRowMapper;
	@Mock private MatchRowMapper matchRowMapper;
	@Mock private JdbcTemplate jdbcTemplate;
	private SimpleJdbcTemplate simpleJdbcTemplate;
//	@Mock private AnnisResultSetBuilder annisResultSetBuilder;
	@Mock private AnnotationGraphDaoHelper annotationGraphDaoHelper;
	@Mock private WekaDaoHelper wekaHelper;
	@Mock private ListCorpusSqlHelper listCorpusHelper;
	@Mock private ListNodeAnnotationsSqlHelper listNodeAnnotationsSqlHelper;
	@Mock private ListCorpusAnnotationsSqlHelper listCorpusAnnotationsHelper;
	@Mock private QueryAnalysis queryAnalysis;

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
    annisDao.setDefaultQueryExecutor(defaultQueryExecutor);
    annisDao.setGraphExtractor(graphExtractor);
		annisDao.setPlanRowMapper(planRowMapper);
		annisDao.setJdbcTemplate(jdbcTemplate);
		annisDao.setAnnotationGraphDaoHelper(annotationGraphDaoHelper);
		annisDao.setWekaSqlHelper(wekaHelper);
		annisDao.setListCorpusSqlHelper(listCorpusHelper);
		annisDao.setListNodeAnnotationsSqlHelper(listNodeAnnotationsSqlHelper);
		annisDao.setListCorpusAnnotationsSqlHelper(listCorpusAnnotationsHelper);
		annisDao.setQueryAnalysis(queryAnalysis);

		when(dddQueryParser.parse(anyString())).thenReturn(STATEMENT);
		when(sqlGenerator.toSql(any(QueryData.class), anyList())).thenReturn(SQL);
		
		simpleJdbcTemplate = spy(annisDao.getSimpleJdbcTemplate());
	}
	
	// check dependencies
	@Test
	public void springManagedInstanceHasAllDependencies() {
		SpringAnnisDao springAnnisDao = (SpringAnnisDao) TestHelper.proxyTarget(springManagedAnnisDao);
		assertThat(springAnnisDao.getSimpleJdbcTemplate(), is(not(nullValue())));
		assertThat(springAnnisDao.getDddQueryParser(), is(not(nullValue())));
		assertThat(springAnnisDao.getSqlGenerator(), is(not(nullValue())));
		assertThat(springAnnisDao.getPlanRowMapper(), is(not(nullValue())));
		assertThat(springAnnisDao.getAnnotationGraphDaoHelper(), is(not(nullValue())));
		assertThat(springAnnisDao.getListCorpusSqlHelper(), is(not(nullValue())));
		assertThat(springAnnisDao.getListCorpusAnnotationsSqlHelper(), is(not(nullValue())));
		assertThat(springAnnisDao.getWekaSqlHelper(), is(not(nullValue())));
		assertThat(springAnnisDao.getListNodeAnnotationsSqlHelper(), is(not(nullValue())));
		assertThat(springAnnisDao.getCountSqlGenerator(), is(not(nullValue())));
		
		// new
		assertThat(springAnnisDao.getQueryAnalysis(), is(not(nullValue())));
		assertThat(springAnnisDao.getFindSqlGenerator(), is(not(nullValue())));
		assertThat(springAnnisDao.getFindRowMapper(), is(not(nullValue())));
		assertThat(springAnnisDao.getSqlSessionModifiers(), is(not(nullValue())));
		assertThat(springAnnisDao.getListCorpusByNameDaoHelper(), is(not(nullValue())));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void find() {
		// setup data that is passed around
		List<Annotation> metaData = mock(List.class);
		QueryData queryData = mock(QueryData.class);
		when(queryData.getMetaData()).thenReturn(metaData);
		when(queryData.getCorpusList()).thenReturn(CORPUS_LIST);
		
		// setup dependencies
		DddQueryParser dddQueryParser = mock(DddQueryParser.class);
		annisDao.setDddQueryParser(dddQueryParser);
		when(dddQueryParser.parse(DDDQUERY)).thenReturn(STATEMENT);
		
		QueryAnalysis queryAnalysis = mock(QueryAnalysis.class);
		annisDao.setQueryAnalysis(queryAnalysis);
		when(queryAnalysis.analyzeQuery(STATEMENT, CORPUS_LIST)).thenReturn(queryData);
		
		JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
		annisDao.setJdbcTemplate(jdbcTemplate);
		SimpleJdbcTemplate simpleJdbcTemplate = annisDao.getSimpleJdbcTemplate();
		when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(MATCHES);
		
		SqlSessionModifier sqlSessionModifier = mock(SqlSessionModifier.class);
		annisDao.setSqlSessionModifiers(Arrays.asList(sqlSessionModifier));
		
		SqlGenerator findSqlGenerator = mock(SqlGenerator.class);
		annisDao.setFindSqlGenerator(findSqlGenerator);
		when(findSqlGenerator.toSql(any(QueryData.class), anyList())).thenReturn(SQL);
		
		MatchRowMapper findRowMapper = mock(MatchRowMapper.class);
		annisDao.setFindRowMapper(findRowMapper);
		
		// call and test
		assertThat(annisDao.findMatches(CORPUS_LIST, DDDQUERY), is(MATCHES));
		
		verify(dddQueryParser).parse(DDDQUERY);
		verify(queryAnalysis).analyzeQuery(STATEMENT, CORPUS_LIST);
		verify(sqlSessionModifier).modifySqlSession(simpleJdbcTemplate, queryData);
		verify(findSqlGenerator).toSql(queryData, CORPUS_LIST);
		verify(jdbcTemplate).query(SQL, findRowMapper);
	}
	
	// corpusList must not be null
	@Test(expected=IllegalArgumentException.class)
	public void findCorpusListNull() {
		annisDao.findMatches(null, DDDQUERY);
	}
	
	@Ignore
	public void plan() {
		String EXPLAIN_SQL = "EXPLAIN SQL";
		List<String> PLAN_ROWS = Arrays.asList("PLAN 1", "PLAN 2");
		String PLAN = "PLAN 1\nPLAN 2";
		when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(PLAN_ROWS);
		
		String test = annisDao.plan(DDDQUERY, CORPUS_LIST, false);
		assertThat(test, is(PLAN));
		
		verify(jdbcTemplate).query(EXPLAIN_SQL, planRowMapper);
	}
	
	@Ignore
	public void planAnalyze() {
		String EXPLAIN_SQL = "EXPLAIN ANALYZE SQL";
		List<String> PLAN_ROWS = Arrays.asList("PLAN 1", "PLAN 2");
		String PLAN = "PLAN 1\nPLAN 2";
		when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(PLAN_ROWS);
		
		assertThat(annisDao.plan(DDDQUERY, CORPUS_LIST, true), is(PLAN));

		verify(jdbcTemplate).query(EXPLAIN_SQL, planRowMapper);
	}
	
	// retrieve annotation graph for a dddquery
	@SuppressWarnings("unchecked")
	// TODO: understand the sense of this test @Test
	public void retrieveAnnotationGraphInlineMatching() throws DataAccessException, SQLException {
		final List<AnnotationGraph> ANNOTATION_GRAPHS = mock(List.class);
		final int CONTEXT = 2;
		final int LIMIT = 1;
		final int OFFSET = 0;
    final int NODECOUNT = 1;
		
		// stub generation of sql query and result set conversion
		when(annotationGraphDaoHelper.createSqlQuery(anyList(), anyInt() ,anyLong(), anyLong(), anyInt(), anyInt())).thenReturn(SQL);
		when(jdbcTemplate.query(anyString(), any(AnnotationGraphDaoHelper.class))).thenReturn(ANNOTATION_GRAPHS);
		
		// call and test
		List<AnnotationGraph> actual = annisDao.retrieveAnnotationGraph(CORPUS_LIST, DDDQUERY, OFFSET, LIMIT, CONTEXT, CONTEXT);
		assertThat(actual, is(ANNOTATION_GRAPHS));
		verify(annotationGraphDaoHelper).createSqlQuery(CORPUS_LIST, NODECOUNT, OFFSET, LIMIT, CONTEXT, CONTEXT);
		verify(jdbcTemplate).query(SQL, annotationGraphDaoHelper);
	}
	
	// retrieve annotation graph for a complete text
	@Test
	public void retrieveAnnotationGraphText() {
		final long TEXT_ID = 23;
		
		// stub AnnotationGraphHelper to create a dummy SQL query and extract a list with a dummy graph
		final AnnotationGraph GRAPH = mock(AnnotationGraph.class);
		when(annotationGraphDaoHelper.createSqlQuery(anyLong())).thenReturn(SQL);
		when(jdbcTemplate.query(any(String.class), any(AnnotationGraphDaoHelper.class))).thenReturn(Arrays.asList(GRAPH));

		// call and test
		assertThat(annisDao.retrieveAnnotationGraph(TEXT_ID), is(GRAPH));
		verify(annotationGraphDaoHelper).createSqlQuery(TEXT_ID);
		verify(jdbcTemplate).query(SQL, annotationGraphDaoHelper);
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
	
	@SuppressWarnings("unchecked")
	@Ignore
	public void listCorpusByName() {
		final List<String> CORPUS_NAMES = mock(List.class);
		
		ListCorpusByNameDaoHelper listCorpusByNameDaoHelper = mock(ListCorpusByNameDaoHelper.class);
		annisDao.setListCorpusByNameDaoHelper(listCorpusByNameDaoHelper);
		when(listCorpusByNameDaoHelper.createSql(anyList())).thenReturn(SQL);
		
//		String sql = "SELECT id FROM corpus WHERE name IN (?) AND top_level = 't'";
		
		// must use expected values here, otherwise the verify below breaks because it is operating on a spy
		List<Long> wtf = simpleJdbcTemplate.query(anyString(), any(ListCorpusByNameDaoHelper.class));
		when(wtf).thenReturn(CORPUS_LIST);
		
		assertThat(annisDao.listCorpusByName(CORPUS_NAMES), is(CORPUS_LIST));
		
		verify(listCorpusByNameDaoHelper).createSql(CORPUS_NAMES);
		verify(simpleJdbcTemplate).query(SQL, listCorpusByNameDaoHelper);
	}
	
}
