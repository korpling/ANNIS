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
package annis.dao;

import annis.executors.DefaultQueryExecutor;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import test.TestHelper;
import annis.AnnisHomeTest;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.ql.parser.AnnisParser;
import annis.ql.parser.QueryAnalysis;
import annis.ql.parser.QueryData;
import annis.service.ifaces.AnnisAttribute;
import annis.service.ifaces.AnnisCorpus;
import annis.sqlgen.AnnotateSqlGenerator;
import annis.sqlgen.ListCorpusAnnotationsSqlHelper;
import annis.sqlgen.ListCorpusSqlHelper;
import annis.sqlgen.ListAnnotationsSqlHelper;
import annis.sqlgen.SqlGenerator;
import annis.ql.node.Start;
import java.util.LinkedList;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"SpringAnnisDao-context.xml"})
public class TestSpringAnnisDao extends AnnisHomeTest {

	// SpringAnnisDao instance that is managed by Spring
	@Autowired private AnnisDao springManagedAnnisDao;
	
	// simple SpringDao instance with mocked dependencies
	private SpringAnnisDao annisDao;
	@Mock private AnnisParser annisParser;
  @Mock private MetaDataFilter metaDataFilter;
	@Mock private SqlGenerator sqlGenerator;
  @Mock private DefaultQueryExecutor defaultQueryExecutor;
  @Mock private AnnotateSqlGenerator graphExtractor;
	@Mock private ParameterizedSingleColumnRowMapper<String> planRowMapper;
	@Mock private JdbcTemplate jdbcTemplate;
	private SimpleJdbcTemplate simpleJdbcTemplate;
//	@Mock private AnnisResultSetBuilder annisResultSetBuilder;
	@Mock private ListCorpusSqlHelper listCorpusHelper;
	@Mock private ListAnnotationsSqlHelper listNodeAnnotationsSqlHelper;
	@Mock private ListCorpusAnnotationsSqlHelper listCorpusAnnotationsHelper;
	@Mock private QueryAnalysis queryAnalysis;

	// constants for flow control verification
	private static final String DDDQUERY = "DDDQUERY";
	private static final Start STATEMENT = new Start();
	private static final String SQL = "SQL";
	private static final List<Long> CORPUS_LIST = new ArrayList<Long>();
  private static final List<Long> DOCUMENT_LIST = new LinkedList<Long>();
	
	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		initMocks(this);
		annisDao = new SpringAnnisDao();
		annisDao.setAqlParser(annisParser);
		annisDao.setSqlGenerator(sqlGenerator);
    annisDao.setDefaultQueryExecutor(defaultQueryExecutor);
    annisDao.setGraphExtractor(graphExtractor);
		annisDao.setPlanRowMapper(planRowMapper);
		annisDao.setJdbcTemplate(jdbcTemplate);
		annisDao.setListCorpusSqlHelper(listCorpusHelper);
		annisDao.setListAnnotationsSqlHelper(listNodeAnnotationsSqlHelper);
		annisDao.setListCorpusAnnotationsSqlHelper(listCorpusAnnotationsHelper);
		annisDao.setQueryAnalysis(queryAnalysis);
    annisDao.setMetaDataFilter(metaDataFilter);

		when(annisParser.parse(anyString())).thenReturn(STATEMENT);
		when(sqlGenerator.toSql(any(QueryData.class))).thenReturn(SQL);
		
		simpleJdbcTemplate = spy(annisDao.getSimpleJdbcTemplate());
	}
	
	// check dependencies
	@Test
	public void springManagedInstanceHasAllDependencies() {
		SpringAnnisDao springAnnisDao = (SpringAnnisDao) TestHelper.proxyTarget(springManagedAnnisDao);
		assertThat(springAnnisDao.getSimpleJdbcTemplate(), is(not(nullValue())));
		assertThat(springAnnisDao.getAqlParser(), is(not(nullValue())));
		assertThat(springAnnisDao.getSqlGenerator(), is(not(nullValue())));
		assertThat(springAnnisDao.getPlanRowMapper(), is(not(nullValue())));
		assertThat(springAnnisDao.getListCorpusSqlHelper(), is(not(nullValue())));
		assertThat(springAnnisDao.getListCorpusAnnotationsSqlHelper(), is(not(nullValue())));
		assertThat(springAnnisDao.getListAnnotationsSqlHelper(), is(not(nullValue())));
		assertThat(springAnnisDao.getCountExtractor(), is(not(nullValue())));
		
		// new
		assertThat(springAnnisDao.getQueryAnalysis(), is(not(nullValue())));
		assertThat(springAnnisDao.getFindSqlGenerator(), is(not(nullValue())));
		assertThat(springAnnisDao.getSqlSessionModifiers(), is(not(nullValue())));
		assertThat(springAnnisDao.getListCorpusByNameDaoHelper(), is(not(nullValue())));
    assertThat(springAnnisDao.getExecutorList(), is(not(nullValue())));
    assertThat(springAnnisDao.getMetaDataFilter(), is(not(nullValue())));
	}
	
	
	@Ignore
	public void planCount() {
		String EXPLAIN_SQL = "EXPLAIN SQL";
		List<String> PLAN_ROWS = Arrays.asList("PLAN 1", "PLAN 2");
		String PLAN = "PLAN 1\nPLAN 2";
		when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(PLAN_ROWS);
		
		String test = annisDao.planCount(annisDao.parseDDDQuery(DDDQUERY, CORPUS_LIST), CORPUS_LIST, false);
		assertThat(test, is(PLAN));
		
		verify(jdbcTemplate).query(EXPLAIN_SQL, planRowMapper);
	}
	
	@Ignore
	public void planCountAnalyze() {
		String EXPLAIN_SQL = "EXPLAIN ANALYZE SQL";
		List<String> PLAN_ROWS = Arrays.asList("PLAN 1", "PLAN 2");
		String PLAN = "PLAN 1\nPLAN 2";
		when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(PLAN_ROWS);
		
		assertThat(annisDao.planCount(annisDao.parseDDDQuery(DDDQUERY, CORPUS_LIST), CORPUS_LIST, true), is(PLAN));

		verify(jdbcTemplate).query(EXPLAIN_SQL, planRowMapper);
	}
	
	// retrieve annotation graph for a complete text
	@Test
	public void retrieveAnnotationGraphText() {
		final long TEXT_ID = 23;
		
		// stub AnnotationGraphHelper to create a dummy SQL query and extract a list with a dummy graph
		final AnnotationGraph GRAPH = mock(AnnotationGraph.class);

    when(graphExtractor.queryAnnotationGraph(any(JdbcTemplate.class), anyLong())).thenReturn(Arrays.asList(GRAPH));
		
		// call and test
		assertThat(annisDao.retrieveAnnotationGraph(TEXT_ID), is(GRAPH));
	}
	
	// return null if text was not found
	@Test
	public void retrieveAnnotationGraphNoText() {
		when(jdbcTemplate.query(anyString(), any(AnnotateSqlGenerator.class))).thenReturn(new ArrayList<AnnotationGraph>());
		assertThat(annisDao.retrieveAnnotationGraph(0), is(nullValue()));
	}
	
	// expect only one graph per text
	@SuppressWarnings("unchecked")
	@Test(expected=IllegalStateException.class)
	public void retrieveAnnotationGraphTextMoreThanOneGraph() {
		// stub returned graph list with more than one entry
		final List<AnnotationGraph> GRAPHS = mock(List.class);
		when(GRAPHS.size()).thenReturn(2);
    when(graphExtractor.queryAnnotationGraph(any(JdbcTemplate.class),
      anyLong())).thenReturn(GRAPHS);
		annisDao.retrieveAnnotationGraph(0);
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
		when(listNodeAnnotationsSqlHelper.createSqlQuery(anyList(), anyBoolean(), anyBoolean())).thenReturn(SQL);
		
		// call and test
		assertThat(annisDao.listAnnotations(CORPUS_LIST, false, false), is(NODE_ANNOTATIONS));
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
	
	@Test
	public void sessionTimeout() {
		// time out after 100 seconds
		int timeout = 100;
		annisDao.setTimeout(timeout);
		
		// call (query data not needed)
		annisDao.modifySqlSession(jdbcTemplate, null);
		
		// verify correct session timeout
		verify(jdbcTemplate).update("SET statement_timeout TO " + timeout);
	}
	
	@Test
	public void noTimeout() {
		// 0 indicates no timeout
		annisDao.setTimeout(0);
		
		// call
		annisDao.modifySqlSession(jdbcTemplate, null);
		
		// verify that nothing has happened
		verifyNoMoreInteractions(simpleJdbcTemplate);
	}
	

	
}
