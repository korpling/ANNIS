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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import annis.model.Annotation;
import annis.ql.node.Start;
import annis.ql.parser.AnnisParser;
import annis.ql.parser.QueryAnalysis;
import annis.ql.parser.QueryData;
import annis.resolver.ResolverEntry;
import annis.resolver.SingleResolverRequest;
import annis.service.ifaces.AnnisAttribute;
import annis.service.ifaces.AnnisBinary;
import annis.service.objects.AnnisCorpus;
import annis.sqlgen.AnnotateSqlGenerator;
import annis.sqlgen.ByteHelper;
import annis.sqlgen.CountSqlGenerator;
import annis.sqlgen.FindSqlGenerator;
import annis.sqlgen.ListAnnotationsSqlHelper;
import annis.sqlgen.ListCorpusAnnotationsSqlHelper;
import annis.sqlgen.ListCorpusSqlHelper;
import annis.sqlgen.MatrixSqlGenerator;
import annis.sqlgen.SaltAnnotateExtractor;
import annis.sqlgen.SqlGenerator;
import annis.utils.Utils;
import de.deutschdiachrondigital.dddquery.parser.DddQueryParser;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import org.springframework.jdbc.core.ResultSetExtractor;

// FIXME: test and refactor timeout and transaction management
public class SpringAnnisDao extends SimpleJdbcDaoSupport implements AnnisDao,
  SqlSessionModifier
{

  // SQL generators for the different query functions
  private FindSqlGenerator findSqlGenerator;
  private CountSqlGenerator countSqlGenerator;
  private AnnotateSqlGenerator<SaltProject> annotateSqlGenerator;
  private SaltAnnotateExtractor saltAnnotateExtractor;
  private MatrixSqlGenerator matrixSqlGenerator;
  // configuration
  private int timeout;
  // fn: corpus id -> corpus name
  private Map<Long, String> corpusNamesById;

  @Override
  public SaltProject graph(QueryData data)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

//	private MatrixSqlGenerator matrixSqlGenerator;
  // SqlGenerator that prepends EXPLAIN to a query
  private final class ExplainSqlGenerator implements
    SqlGenerator<QueryData, String>
  {

    private final boolean analyze;
    private final SqlGenerator<QueryData, ?> generator;

    private ExplainSqlGenerator(SqlGenerator<QueryData, ?> generator,
      boolean analyze)
    {
      this.generator = generator;
      this.analyze = analyze;
    }

    @Override
    public String toSql(QueryData queryData)
    {
      StringBuilder sb = new StringBuilder();
      sb.append("EXPLAIN ");
      if (analyze)
      {
        sb.append("ANALYZE ");
      }
      sb.append(generator.toSql(queryData));
      return sb.toString();
    }

    @Override
    public String extractData(ResultSet rs) throws SQLException,
      DataAccessException
    {
      StringBuilder sb = new StringBuilder();
      while (rs.next())
      {
        sb.append(rs.getString(1));
        sb.append("\n");
      }
      return sb.toString();
    }

    @Override
    public String toSql(QueryData queryData, String indent)
    {
      // dont indent
      return toSql(queryData);
    }
  }
  private static Logger log = Logger.getLogger(SpringAnnisDao.class);
  // / old
  private SqlGenerator sqlGenerator;
  private ListCorpusSqlHelper listCorpusSqlHelper;
  private ListAnnotationsSqlHelper listAnnotationsSqlHelper;
  private ListCorpusAnnotationsSqlHelper listCorpusAnnotationsSqlHelper;
  // / new
  private List<SqlSessionModifier> sqlSessionModifiers;
//  private SqlGenerator findSqlGenerator;
  private CountExtractor countExtractor;
  private ParameterizedSingleColumnRowMapper<String> planRowMapper;
  private ListCorpusByNameDaoHelper listCorpusByNameDaoHelper;
  private AnnotateSqlGenerator graphExtractor;
  private MetaDataFilter metaDataFilter;
  private QueryAnalysis queryAnalysis;
  private AnnisParser aqlParser;
  private DddQueryParser dddqueryParser;
  private de.deutschdiachrondigital.dddquery.parser.QueryAnalysis dddqueryAnalysis;
  private HashMap<Long, Properties> corpusConfiguration;
  private ByteHelper byteHelper;

  public SpringAnnisDao()
  {
    planRowMapper = new ParameterizedSingleColumnRowMapper<String>();
    sqlSessionModifiers = new ArrayList<SqlSessionModifier>();
  }

  public void init()
  {
    parseCorpusConfiguration();
  }

  @Override
  public <T> T executeQueryFunction(QueryData queryData,
    final SqlGenerator<QueryData, T> generator)
  {
    return executeQueryFunction(queryData, generator, generator);
  }

  @Override
  public List<String> mapCorpusIdsToNames(List<Long> ids)
  {
    List<String> names = new ArrayList<String>();
    if (corpusNamesById == null)
    {
      corpusNamesById = new TreeMap<Long, String>();
      List<AnnisCorpus> corpora = listCorpora();
      for (AnnisCorpus corpus : corpora)
      {
        corpusNamesById.put(corpus.getId(), corpus.getName());
      }
    }
    for (Long id : ids)
    {
      names.add(corpusNamesById.get(id));
    }
    return names;
  }

  // query functions
  @Transactional
  @Override
  public <T> T executeQueryFunction(QueryData queryData,
    final SqlGenerator<QueryData, T> generator,
    final ResultSetExtractor<T> extractor)
  {

    JdbcTemplate jdbcTemplate = getJdbcTemplate();

    // FIXME: muss corpusConfiguration an jeden Query angehangen werden?
    // oder nur an annotate-Queries?

    queryData.setCorpusConfiguration(corpusConfiguration);

    // filter by meta data
    queryData.setDocuments(metaDataFilter.getDocumentsForMetadata(queryData));

    // execute session modifiers if any
    for (SqlSessionModifier sqlSessionModifier : sqlSessionModifiers)
    {
      sqlSessionModifier.modifySqlSession(jdbcTemplate, queryData);
    }

    // execute query and return result
    return jdbcTemplate.query(generator.toSql(queryData), extractor);
  }

  @Override
  public void modifySqlSession(JdbcTemplate jdbcTemplate, QueryData queryData)
  {
    if (timeout > 0)
    {
      jdbcTemplate.update("SET statement_timeout TO " + timeout);
    }
  }

  @Transactional(readOnly = true)
  @Override
  public List<Match> find(QueryData queryData)
  {
    return executeQueryFunction(queryData, findSqlGenerator);
  }

  @Transactional(readOnly = true)
  @Override
  public int count(QueryData queryData)
  {
    return executeQueryFunction(queryData, countSqlGenerator);
  }

  @Override
  @Transactional(readOnly = true)
  public SaltProject annotate(QueryData queryData)
  {
    return executeQueryFunction(queryData, annotateSqlGenerator,
      saltAnnotateExtractor);
  }

  @Transactional(readOnly = true)
  @Override
  public List<AnnotatedMatch> matrix(QueryData queryData)
  {
    return executeQueryFunction(queryData, matrixSqlGenerator);
  }

  @Override
  @Transactional(readOnly = true)
  public String explain(SqlGenerator<QueryData, ?> generator,
    QueryData queryData,
    final boolean analyze)
  {
    return executeQueryFunction(queryData, new ExplainSqlGenerator(generator,
      analyze));
  }

  @Override
  public QueryData parseAQL(String aql, List<Long> corpusList)
  {
    // parse the query
    Start statement = aqlParser.parse(aql);
    // analyze it
    return queryAnalysis.analyzeQuery(statement, corpusList);
  }

  @Override
  public QueryData parseDDDQuery(String dddquery, List<Long> corpusList)
  {
    de.deutschdiachrondigital.dddquery.node.Start statement = dddqueryParser.
      parse(dddquery);
    return dddqueryAnalysis.analyzeQuery(statement, corpusList);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AnnisCorpus> listCorpora()
  {
    return (List<AnnisCorpus>) getJdbcTemplate().query(
      listCorpusSqlHelper.createSqlQuery(), listCorpusSqlHelper);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AnnisAttribute> listAnnotations(List<Long> corpusList,
    boolean listValues, boolean onlyMostFrequentValues)
  {
    return (List<AnnisAttribute>) getJdbcTemplate().query(
      listAnnotationsSqlHelper.createSqlQuery(corpusList, listValues,
      onlyMostFrequentValues), listAnnotationsSqlHelper);
  }

  @Override
  @Transactional(readOnly = true)
  public SaltProject retrieveAnnotationGraph(long textId)
  {
    SaltProject p =
      annotateSqlGenerator.queryAnnotationGraph(getJdbcTemplate(), textId);

    return p;
  }

  @Override
  @Transactional(readOnly = true)
  public SaltProject retrieveAnnotationGraph(String toplevelCorpusName,
    String documentName)
  {
    SaltProject p =
      annotateSqlGenerator.queryAnnotationGraph(getJdbcTemplate(),
      toplevelCorpusName, documentName);
    return p;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Annotation> listCorpusAnnotations(long corpusId)
  {
    final String sql = listCorpusAnnotationsSqlHelper.createSqlQuery(corpusId);
    final List<Annotation> corpusAnnotations =
      (List<Annotation>) getJdbcTemplate().query(sql,
      listCorpusAnnotationsSqlHelper);
    return corpusAnnotations;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Annotation> listCorpusAnnotations(String toplevelCorpusName,
    String documentName)
  {
    final String sql = listCorpusAnnotationsSqlHelper.createSqlQuery(
      toplevelCorpusName, documentName);
    final List<Annotation> corpusAnnotations =
      (List<Annotation>) getJdbcTemplate().query(sql,
      listCorpusAnnotationsSqlHelper);
    return corpusAnnotations;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Long> listCorpusByName(List<String> corpusNames)
  {
    final String sql = listCorpusByNameDaoHelper.createSql(corpusNames);
    final List<Long> result = getJdbcTemplate().query(sql,
      listCorpusByNameDaoHelper);
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public List<ResolverEntry> getResolverEntries(SingleResolverRequest request)
  {
    try
    {
      ResolverDaoHelper helper = new ResolverDaoHelper();
      PreparedStatement stmt = helper.createPreparedStatement(getConnection());
      helper.fillPreparedStatement(request, stmt);
      List<ResolverEntry> result = helper.extractData(stmt.executeQuery());
      return result;
    }
    catch (SQLException ex)
    {
      log.error("Could not get resolver entries from database", ex);
      return new LinkedList<ResolverEntry>();
    }
  }

  @Override
  public Map<String, String> getCorpusConfiguration(String corpusName)
  {
    Map<String, String> result = new TreeMap<String, String>();

    // parse from configuration folder
    if (System.getProperty("annis.home") != null)
    {
      File confFolder = new File(System.getProperty("annis.home")
        + "/conf/corpora");
      if (confFolder.isDirectory())
      {

        File conf = null;
        try
        {
          // try  hash of corpus name first
          conf = new File(confFolder, Utils.calculateSHAHash(corpusName));
        }
        catch (NoSuchAlgorithmException ex)
        {
          log.log(Level.WARN, null, ex);
        }
        catch (UnsupportedEncodingException ex)
        {
          log.log(Level.WARN, null, ex);
        }

        if (conf == null || !conf.isFile())
        {
          // try corpus name next
          conf = new File(confFolder, corpusName + ".properties");
        }
        // parse property file if found
        if (conf.isFile())
        {
          Properties p = new Properties();
          try
          {
            p.load(new FileReader(conf));
            for (Map.Entry<Object, Object> e : p.entrySet())
            {
              result.put(e.getKey().toString(), e.getValue().toString());
            }
          }
          catch (IOException ex)
          {
            log.log(Level.WARN, "could not load corpus configuration file "
              + conf.getAbsolutePath(), ex);
          }
        }
      } // end if conf is a directory
    } // end if annis.home was set
    return result;
  }

  private void parseCorpusConfiguration()
  {
    corpusConfiguration = new HashMap<Long, Properties>();

    try
    {
      List<AnnisCorpus> corpora = listCorpora();
      for (AnnisCorpus c : corpora)
      {
        // copy properties from map
        Properties p = new Properties();
        Map<String, String> map = getCorpusConfiguration(c.getName());
        for (Map.Entry<String, String> e : map.entrySet())
        {
          p.setProperty(e.getKey(), e.getValue());
        }
        corpusConfiguration.put(c.getId(), p);
      }
    }
    catch (org.springframework.jdbc.CannotGetJdbcConnectionException ex)
    {
      log.log(Level.WARN,
        "No corpus configuration loaded due to missing database connection.");
    }
  }

  public AnnisParser getAqlParser()
  {
    return aqlParser;
  }

  public void setAqlParser(AnnisParser aqlParser)
  {
    this.aqlParser = aqlParser;
  }

  // /// Getter / Setter
  public SqlGenerator getSqlGenerator()
  {
    return sqlGenerator;
  }

  public void setSqlGenerator(SqlGenerator sqlGenerator)
  {
    this.sqlGenerator = sqlGenerator;
  }

  public ParameterizedSingleColumnRowMapper<String> getPlanRowMapper()
  {
    return planRowMapper;
  }

  public void setPlanRowMapper(
    ParameterizedSingleColumnRowMapper<String> planRowMapper)
  {
    this.planRowMapper = planRowMapper;
  }

  public ListCorpusSqlHelper getListCorpusSqlHelper()
  {
    return listCorpusSqlHelper;
  }

  public void setListCorpusSqlHelper(ListCorpusSqlHelper listCorpusHelper)
  {
    this.listCorpusSqlHelper = listCorpusHelper;
  }

  public ListAnnotationsSqlHelper getListAnnotationsSqlHelper()
  {
    return listAnnotationsSqlHelper;
  }

  public void setListAnnotationsSqlHelper(
    ListAnnotationsSqlHelper listNodeAnnotationsSqlHelper)
  {
    this.listAnnotationsSqlHelper = listNodeAnnotationsSqlHelper;
  }

  public ListCorpusAnnotationsSqlHelper getListCorpusAnnotationsSqlHelper()
  {
    return listCorpusAnnotationsSqlHelper;
  }

  public void setListCorpusAnnotationsSqlHelper(
    ListCorpusAnnotationsSqlHelper listCorpusAnnotationsHelper)
  {
    this.listCorpusAnnotationsSqlHelper = listCorpusAnnotationsHelper;
  }

  public List<SqlSessionModifier> getSqlSessionModifiers()
  {
    return sqlSessionModifiers;
  }

  public void setSqlSessionModifiers(
    List<SqlSessionModifier> sqlSessionModifiers)
  {
    this.sqlSessionModifiers = sqlSessionModifiers;
  }

  public FindSqlGenerator getFindSqlGenerator()
  {
    return findSqlGenerator;
  }

  public void setFindSqlGenerator(FindSqlGenerator findSqlGenerator)
  {
    this.findSqlGenerator = findSqlGenerator;
  }

  public QueryAnalysis getQueryAnalysis()
  {
    return queryAnalysis;
  }

  public void setQueryAnalysis(QueryAnalysis queryAnalysis)
  {
    this.queryAnalysis = queryAnalysis;
  }

  public ListCorpusByNameDaoHelper getListCorpusByNameDaoHelper()
  {
    return listCorpusByNameDaoHelper;
  }

  public void setListCorpusByNameDaoHelper(
    ListCorpusByNameDaoHelper listCorpusByNameDaoHelper)
  {
    this.listCorpusByNameDaoHelper = listCorpusByNameDaoHelper;
  }

  public CountExtractor getCountExtractor()
  {
    return countExtractor;
  }

  public void setCountExtractor(CountExtractor countExtractor)
  {
    this.countExtractor = countExtractor;
  }

  public AnnotateSqlGenerator getGraphExtractor()
  {
    return graphExtractor;
  }

  public void setGraphExtractor(AnnotateSqlGenerator graphExtractor)
  {
    this.graphExtractor = graphExtractor;
  }

  public MetaDataFilter getMetaDataFilter()
  {
    return metaDataFilter;
  }

  public void setMetaDataFilter(MetaDataFilter metaDataFilter)
  {
    this.metaDataFilter = metaDataFilter;
  }

  public de.deutschdiachrondigital.dddquery.parser.QueryAnalysis getDddqueryAnalysis()
  {
    return dddqueryAnalysis;
  }

  public void setDddqueryAnalysis(
    de.deutschdiachrondigital.dddquery.parser.QueryAnalysis dddqueryAnalysis)
  {
    this.dddqueryAnalysis = dddqueryAnalysis;
  }

  public DddQueryParser getDddqueryParser()
  {
    return dddqueryParser;
  }

  public void setDddqueryParser(DddQueryParser dddqueryParser)
  {
    this.dddqueryParser = dddqueryParser;
  }

  public CountSqlGenerator getCountSqlGenerator()
  {
    return countSqlGenerator;
  }

  public void setCountSqlGenerator(CountSqlGenerator countSqlGenerator)
  {
    this.countSqlGenerator = countSqlGenerator;
  }

  @Override
  public HashMap<Long, Properties> getCorpusConfiguration()
  {
    return corpusConfiguration;
  }

  @Override
  public void setCorpusConfiguration(
    HashMap<Long, Properties> corpusConfiguration)
  {
    this.corpusConfiguration = corpusConfiguration;
  }

  @Override
  public int getTimeout()
  {
    return timeout;
  }

  @Override
  public void setTimeout(int timeout)
  {
    this.timeout = timeout;
  }

  public MatrixSqlGenerator getMatrixSqlGenerator()
  {
    return matrixSqlGenerator;
  }

  public void setMatrixSqlGenerator(MatrixSqlGenerator matrixSqlGenerator)
  {
    this.matrixSqlGenerator = matrixSqlGenerator;
  }

  public SaltAnnotateExtractor getSaltAnnotateExtractor()
  {
    return saltAnnotateExtractor;
  }

  public void setSaltAnnotateExtractor(
    SaltAnnotateExtractor saltAnnotateExtractor)
  {
    this.saltAnnotateExtractor = saltAnnotateExtractor;
  }

  public ByteHelper getByteHelper()
  {
    return byteHelper;
  }

  public void setByteHelper(ByteHelper byteHelper)
  {
    this.byteHelper = byteHelper;
  }

  @Override
  public AnnisBinary getBinary(String corpusName, int offset, int length)
  {
    return (AnnisBinary) getJdbcTemplate().query(byteHelper.generateSql(
      corpusName, offset, length), byteHelper);
  }

  public AnnotateSqlGenerator<SaltProject> getAnnotateSqlGenerator()
  {
    return annotateSqlGenerator;
  }

  public void setAnnotateSqlGenerator(
    AnnotateSqlGenerator<SaltProject> annotateSqlGenerator)
  {
    this.annotateSqlGenerator = annotateSqlGenerator;
  }
}
