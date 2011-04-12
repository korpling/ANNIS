package annis.dao;

import annis.executors.DefaultQueryExecutor;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

import annis.executors.QueryExecutor;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.ql.node.Start;
import annis.ql.parser.AnnisParser;
import annis.ql.parser.QueryAnalysis;
import annis.ql.parser.QueryData;
import annis.resolver.ResolverEntry;
import annis.resolver.SingleResolverRequest;
import annis.service.ifaces.AnnisAttribute;
import annis.service.ifaces.AnnisCorpus;
import annis.sqlgen.ListCorpusAnnotationsSqlHelper;
import annis.sqlgen.ListCorpusSqlHelper;
import annis.sqlgen.ListNodeAnnotationsSqlHelper;
import annis.sqlgen.SqlGenerator;
import annis.utils.Utils;
import de.deutschdiachrondigital.dddquery.parser.DddQueryParser;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;

// FIXME: test and refactor timeout and transaction management
public class SpringAnnisDao extends SimpleJdbcDaoSupport implements AnnisDao
{

  private static Logger log = Logger.getLogger(SpringAnnisDao.class);
  /// old
  private SqlGenerator sqlGenerator;
  private ListCorpusSqlHelper listCorpusSqlHelper;
  private ListNodeAnnotationsSqlHelper listNodeAnnotationsSqlHelper;
  private ListCorpusAnnotationsSqlHelper listCorpusAnnotationsSqlHelper;
  /// new
  private List<SqlSessionModifier> sqlSessionModifiers;
  private SqlGenerator findSqlGenerator;
  private CountExtractor countExtractor;
  private MatrixExtractor matrixExtractor;
  private ParameterizedSingleColumnRowMapper<String> planRowMapper;
  private ListCorpusByNameDaoHelper listCorpusByNameDaoHelper;
  private DefaultQueryExecutor defaultQueryExecutor;
  private GraphExtractor graphExtractor;
  private List<QueryExecutor> executorList;
  private MetaDataFilter metaDataFilter;
  private QueryAnalysis queryAnalysis;
  private AnnisParser aqlParser;
  private DddQueryParser dddqueryParser;
  private de.deutschdiachrondigital.dddquery.parser.QueryAnalysis dddqueryAnalysis;
  private HashMap<Long, Properties> corpusConfiguration;

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
    de.deutschdiachrondigital.dddquery.node.Start statement = dddqueryParser.parse(dddquery);
    return dddqueryAnalysis.analyzeQuery(statement, corpusList);
  }

  @Override
  public int countMatches(final List<Long> corpusList, final QueryData aql)
  {
    QueryData queryData = createDynamicMatchView(corpusList, aql);

    return countExtractor.queryCount(getJdbcTemplate());
  }

  @Override
  public List<AnnotatedMatch> matrix(final List<Long> corpusList, final QueryData aql)
  {
    QueryData queryData = createDynamicMatchView(corpusList, aql);

    int nodeCount = queryData.getMaxWidth();

    return matrixExtractor.queryMatrix(getJdbcTemplate(), corpusList, nodeCount);
  }

  @Override
  public String planCount(QueryData aql, List<Long> corpusList, boolean analyze)
  {
    Validate.notNull(corpusList, "corpusList=null passed as argument");

    createDynamicMatchView(corpusList, aql);
    return countExtractor.explain(getJdbcTemplate(), analyze);
  }

  @Override
  public String planGraph(QueryData aql, List<Long> corpusList,
    long offset, long limit, int left, int right,
    boolean analyze)
  {
    Validate.notNull(corpusList, "corpusList=null passed as argument");

    QueryData queryData = createDynamicMatchView(corpusList, aql);

    int nodeCount = queryData.getMaxWidth();
    return graphExtractor.explain(getJdbcTemplate(), corpusList, nodeCount,
      offset, limit, left, right, analyze, corpusConfiguration);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<AnnotationGraph> retrieveAnnotationGraph(
    List<Long> corpusList, QueryData aql, long offset, long limit, int left, int right)
  {
    QueryData queryData = createDynamicMatchView(corpusList, aql);

    int nodeCount = queryData.getMaxWidth();

    // create the Annis graphs
    return graphExtractor.queryAnnotationGraph(getJdbcTemplate(),
      corpusList, nodeCount, offset, limit, left, right, corpusConfiguration);
  }

  private QueryData createDynamicMatchView(List<Long> corpusList, QueryData queryData)
  {

    // execute session modifiers
    for (SqlSessionModifier sqlSessionModifier : sqlSessionModifiers)
    {
      sqlSessionModifier.modifySqlSession(getSimpleJdbcTemplate(), queryData);
    }

    List<Long> documents = metaDataFilter.getDocumentsForMetadata(queryData);

    // generate the view with the matched node IDs
    for (QueryExecutor e : executorList)
    {
      if (e.checkIfApplicable(queryData))
      {
        e.createMatchView(getJdbcTemplate(), corpusList, documents, queryData);
        // leave the loop
        break;
      }
    }

    return queryData;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<AnnisCorpus> listCorpora()
  {
    return (List<AnnisCorpus>) getJdbcTemplate().query(
      listCorpusSqlHelper.createSqlQuery(), listCorpusSqlHelper);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<AnnisAttribute> listNodeAnnotations(List<Long> corpusList,
    boolean listValues)
  {
    return (List<AnnisAttribute>) getJdbcTemplate().query(
      listNodeAnnotationsSqlHelper.createSqlQuery(corpusList,
      listValues), listNodeAnnotationsSqlHelper);
  }

  @SuppressWarnings("unchecked")
  @Override
  public AnnotationGraph retrieveAnnotationGraph(long textId)
  {
    List<AnnotationGraph> graphs = graphExtractor.queryAnnotationGraph(getJdbcTemplate(), textId);
    if (graphs.isEmpty())
    {
      return null;
    }
    if (graphs.size() > 1)
    {
      throw new IllegalStateException("Expected only one annotation graph");
    }
    return graphs.get(0);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Annotation> listCorpusAnnotations(long corpusId)
  {
    final String sql = listCorpusAnnotationsSqlHelper.createSqlQuery(corpusId);
    final List<Annotation> corpusAnnotations =
      (List<Annotation>) getJdbcTemplate().query(sql, listCorpusAnnotationsSqlHelper);
    return corpusAnnotations;
  }

  @Override
  public List<Long> listCorpusByName(List<String> corpusNames)
  {
    final String sql = listCorpusByNameDaoHelper.createSql(corpusNames);
    final List<Long> result = getSimpleJdbcTemplate().query(sql, listCorpusByNameDaoHelper);
    return result;
  }

  @Override
  public List<ResolverEntry> getResolverEntries(SingleResolverRequest[] request)
  {
    try
    {
      ResolverDaoHelper helper = new ResolverDaoHelper(request.length);
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

  private void parseCorpusConfiguration()
  {
    corpusConfiguration = new HashMap<Long, Properties>();
    List<AnnisCorpus> corpora = listCorpora();
    for(AnnisCorpus c : corpora)
    {
      // put in empty default properties
      corpusConfiguration.put(c.getId(), new Properties());

      // parse from configuration folder
      if(System.getProperty("annis.home") != null)
      {
        File confFolder = new File(System.getProperty("annis.home") + "/conf/corpora");
        if(confFolder.isDirectory())
        {

          // try corpus ID first
          File conf = new File(confFolder, "" + c.getId() + ".properties" );
          if(!conf.isFile())
          {
            try
            {
              // try hash of corpus name
              conf = new File(confFolder, Utils.calculateSHAHash(c.getName()) + ".properties");
              if(!conf.isFile())
              {
                // try corpus name
                conf = new File(confFolder, c.getName() + ".properties");
              }
            }
            catch (NoSuchAlgorithmException ex)
            {
              log.log(Level.WARN, null, ex);
            }
            catch (UnsupportedEncodingException ex)
            {
              log.log(Level.WARN, null, ex);
            }
          }

          // parse property file if found
          if(conf.isFile())
          {
            Properties p = corpusConfiguration.get(c.getId());
            try
            {
              p.load(new FileReader(conf));

            }
            catch (IOException ex)
            {
              log.log(Level.WARN, "could not load corpus configuration file " + conf.getAbsolutePath(), ex);
            }
          }
        }
      }
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

  public ListNodeAnnotationsSqlHelper getListNodeAnnotationsSqlHelper()
  {
    return listNodeAnnotationsSqlHelper;
  }

  public void setListNodeAnnotationsSqlHelper(
    ListNodeAnnotationsSqlHelper listNodeAnnotationsSqlHelper)
  {
    this.listNodeAnnotationsSqlHelper = listNodeAnnotationsSqlHelper;
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

  public void setSqlSessionModifiers(List<SqlSessionModifier> sqlSessionModifiers)
  {
    this.sqlSessionModifiers = sqlSessionModifiers;
  }

  public SqlGenerator getFindSqlGenerator()
  {
    return findSqlGenerator;
  }

  public void setFindSqlGenerator(SqlGenerator findSqlGenerator)
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

  public DefaultQueryExecutor getDefaultQueryExecutor()
  {
    return defaultQueryExecutor;
  }

  public void setDefaultQueryExecutor(DefaultQueryExecutor defaultQueryExecutor)
  {
    this.defaultQueryExecutor = defaultQueryExecutor;
  }

  public GraphExtractor getGraphExtractor()
  {
    return graphExtractor;
  }

  public void setGraphExtractor(GraphExtractor graphExtractor)
  {
    this.graphExtractor = graphExtractor;
  }

  public List<QueryExecutor> getExecutorList()
  {
    return executorList;
  }

  public void setExecutorList(List<QueryExecutor> executorList)
  {
    this.executorList = executorList;
  }

  public MetaDataFilter getMetaDataFilter()
  {
    return metaDataFilter;
  }

  public void setMetaDataFilter(MetaDataFilter metaDataFilter)
  {
    this.metaDataFilter = metaDataFilter;
  }

  public MatrixExtractor getMatrixExtractor()
  {
    return matrixExtractor;
  }

  public void setMatrixExtractor(MatrixExtractor matrixExtractor)
  {
    this.matrixExtractor = matrixExtractor;
  }

  public de.deutschdiachrondigital.dddquery.parser.QueryAnalysis getDddqueryAnalysis()
  {
    return dddqueryAnalysis;
  }

  public void setDddqueryAnalysis(de.deutschdiachrondigital.dddquery.parser.QueryAnalysis dddqueryAnalysis)
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

}
