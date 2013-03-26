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

import annis.AnnisXmlContextLoader;
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
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import annis.test.TestHelper;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.ql.parser.AnnisParser;
import annis.ql.parser.QueryAnalysis;
import annis.ql.parser.QueryData;
import annis.service.objects.AnnisCorpus;
import annis.sqlgen.AnnotateSqlGenerator;
import annis.sqlgen.ListCorpusAnnotationsSqlHelper;
import annis.sqlgen.ListCorpusSqlHelper;
import annis.sqlgen.ListAnnotationsSqlHelper;
import annis.sqlgen.SqlGenerator;
import annis.ql.node.Start;
import annis.service.objects.AnnisAttribute;
import annis.sqlgen.SaltAnnotateExtractor;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import java.util.LinkedList;
import javax.annotation.Resource;
import org.springframework.context.annotation.PropertySource;

@RunWith(SpringJUnit4ClassRunner.class)
// TODO: do not test context only for annopool
@ContextConfiguration(locations =
{
  "file:src/main/distribution/conf/spring/Common.xml"
}, loader=AnnisXmlContextLoader.class)
public class TestSpringAnnisDao
{

  @Resource(name="annisDao")
  private AnnisDao annisDao;

  // simple SpringDao instance with mocked dependencies
  private SpringAnnisDao simpleAnnisDao;
  @Mock
  private AnnisParser annisParser;
  @Mock
  private MetaDataFilter metaDataFilter;
  @Mock
  private SqlGenerator sqlGenerator;
  @Mock
  private AnnotateSqlGenerator annotateSqlGenerator;
  @Mock
  private SaltAnnotateExtractor saltAnnotateExtractor;
  @Mock
  private ParameterizedSingleColumnRowMapper<String> planRowMapper;
  @Mock
  private JdbcTemplate jdbcTemplate;
  private SimpleJdbcTemplate simpleJdbcTemplate;
//	@Mock private AnnisResultSetBuilder annisResultSetBuilder;
  @Mock
  private ListCorpusSqlHelper listCorpusHelper;
  @Mock
  private ListAnnotationsSqlHelper listNodeAnnotationsSqlHelper;
  @Mock
  private ListCorpusAnnotationsSqlHelper listCorpusAnnotationsHelper;
  @Mock
  private QueryAnalysis queryAnalysis;
  // constants for flow control verification
  private static final String DDDQUERY = "DDDQUERY";
  private static final Start STATEMENT = new Start();
  private static final String SQL = "SQL";
  private static final List<Long> CORPUS_LIST = new ArrayList<Long>();
  private static final List<Long> DOCUMENT_LIST = new LinkedList<Long>();

  @SuppressWarnings("unchecked")
  @Before
  public void setup()
  {
    initMocks(this);
    simpleAnnisDao = new SpringAnnisDao();
    simpleAnnisDao.setAqlParser(annisParser);
    simpleAnnisDao.setSqlGenerator(sqlGenerator);
    simpleAnnisDao.setAnnotateSqlGenerator(annotateSqlGenerator);
    simpleAnnisDao.setSaltAnnotateExtractor(saltAnnotateExtractor);
    simpleAnnisDao.setPlanRowMapper(planRowMapper);
    simpleAnnisDao.setJdbcTemplate(jdbcTemplate);
    simpleAnnisDao.setListCorpusSqlHelper(listCorpusHelper);
    simpleAnnisDao.setListAnnotationsSqlHelper(listNodeAnnotationsSqlHelper);
    simpleAnnisDao.setListCorpusAnnotationsSqlHelper(listCorpusAnnotationsHelper);
    simpleAnnisDao.setQueryAnalysis(queryAnalysis);
    simpleAnnisDao.setMetaDataFilter(metaDataFilter);

    when(annisParser.parse(anyString())).thenReturn(STATEMENT);
    when(sqlGenerator.toSql(any(QueryData.class))).thenReturn(SQL);

    simpleJdbcTemplate = spy(simpleAnnisDao.getSimpleJdbcTemplate());
  }

  // check dependencies
  @Test
  public void springManagedInstanceHasAllDependencies()
  {

    SpringAnnisDao springManagedDao = (SpringAnnisDao) TestHelper.proxyTarget(annisDao);
    assertThat(springManagedDao.getSimpleJdbcTemplate(), is(not(nullValue())));
    assertThat(springManagedDao.getAqlParser(), is(not(nullValue())));
    assertThat(springManagedDao.getSqlGenerator(), is(not(nullValue())));
    assertThat(springManagedDao.getPlanRowMapper(), is(not(nullValue())));
    assertThat(springManagedDao.getListCorpusSqlHelper(), is(not(nullValue())));
    assertThat(springManagedDao.getListCorpusAnnotationsSqlHelper(),
      is(not(nullValue())));
    assertThat(springManagedDao.getListAnnotationsSqlHelper(),
      is(not(nullValue())));

    // new
    assertThat(springManagedDao.getQueryAnalysis(), is(not(nullValue())));
    assertThat(springManagedDao.getFindSqlGenerator(), is(not(nullValue())));
    assertThat(springManagedDao.getSqlSessionModifiers(), is(not(nullValue())));
    assertThat(springManagedDao.getListCorpusByNameDaoHelper(), is(
      not(nullValue())));
    assertThat(springManagedDao.getMetaDataFilter(), is(not(nullValue())));
    assertThat(springManagedDao.getFrequencySqlGenerator(), is(not(nullValue())));
  }


  @SuppressWarnings("unchecked")
  @Test
  public void listCorpora()
  {
    // stub JdbcTemplate to return a list of AnnisCorpus
    final List<AnnisCorpus> CORPORA = mock(List.class);
    when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(
      CORPORA);

    // stub SQL query
    when(listCorpusHelper.createSqlQuery()).thenReturn(SQL);

    // call and test
    assertThat(simpleAnnisDao.listCorpora(), is(CORPORA));
    verify(jdbcTemplate).query(SQL, listCorpusHelper);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void listCorpusAnnotations()
  {
    // stub JdbcTemplate to return a list of Annotation
    final List<Annotation> ANNOTATIONS = mock(List.class);
    when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(
      ANNOTATIONS);

    // stub SQL query
    final String ID = "toplevelcorpus";
    when(listCorpusAnnotationsHelper.createSqlQuery(anyString(), anyString(), anyBoolean())).thenReturn(SQL);

    // call and test
    assertThat(simpleAnnisDao.listCorpusAnnotations(ID), is(ANNOTATIONS));
    verify(listCorpusAnnotationsHelper).createSqlQuery(ID, ID, true);
    verify(jdbcTemplate).query(SQL, listCorpusAnnotationsHelper);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void listNodeAnnotations()
  {
    // stub JdbcTemplate to return a list of AnnisAttribute
    final List<AnnisAttribute> NODE_ANNOTATIONS = mock(List.class);
    when(jdbcTemplate.query(anyString(), any(ResultSetExtractor.class))).
      thenReturn(NODE_ANNOTATIONS);

    // stub SQL query
    when(listNodeAnnotationsSqlHelper.createSqlQuery(anyList(), anyBoolean(),
      anyBoolean())).thenReturn(SQL);

    // call and test
    assertThat(simpleAnnisDao.listAnnotations(CORPUS_LIST, false, false), is(
      NODE_ANNOTATIONS));
    verify(jdbcTemplate).query(SQL, listNodeAnnotationsSqlHelper);
  }

  @SuppressWarnings("unchecked")
  @Ignore
  public void listCorpusByName()
  {
    final List<String> CORPUS_NAMES = mock(List.class);

    ListCorpusByNameDaoHelper listCorpusByNameDaoHelper =
      mock(ListCorpusByNameDaoHelper.class);
    simpleAnnisDao.setListCorpusByNameDaoHelper(listCorpusByNameDaoHelper);
    when(listCorpusByNameDaoHelper.createSql(anyList())).thenReturn(SQL);

//		String sql = "SELECT id FROM corpus WHERE name IN (?) AND top_level = 't'";

    // must use expected values here, otherwise the verify below breaks because it is operating on a spy
    List<Long> wtf = simpleJdbcTemplate.query(anyString(),
      any(ListCorpusByNameDaoHelper.class));
    when(wtf).thenReturn(CORPUS_LIST);

    assertThat(simpleAnnisDao.mapCorpusNamesToIds(CORPUS_NAMES), is(CORPUS_LIST));

    verify(listCorpusByNameDaoHelper).createSql(CORPUS_NAMES);
    verify(simpleJdbcTemplate).query(SQL, listCorpusByNameDaoHelper);
  }

  @Test
  public void sessionTimeout()
  {
    // time out after 100 seconds
    int timeout = 100;
    simpleAnnisDao.setTimeout(timeout);

    // call (query data not needed)
    simpleAnnisDao.modifySqlSession(jdbcTemplate, null);

    // verify correct session timeout
    verify(jdbcTemplate).update("SET statement_timeout TO " + timeout);
  }

  @Test
  public void noTimeout()
  {
    // 0 indicates no timeout
    simpleAnnisDao.setTimeout(0);

    // call
    simpleAnnisDao.modifySqlSession(jdbcTemplate, null);

    // verify that nothing has happened
    verifyNoMoreInteractions(simpleJdbcTemplate);
  }
}
