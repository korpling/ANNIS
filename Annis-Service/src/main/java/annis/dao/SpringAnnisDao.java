package annis.dao;

import annis.executors.DefaultQueryExecutor;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

import annis.WekaDaoHelper;
import annis.executors.AQLConstraints;
import annis.executors.QueryExecutor;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
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
import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.parser.DddQueryParser;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Map;

// FIXME: test and refactor timeout and transaction management
public class SpringAnnisDao extends SimpleJdbcDaoSupport implements AnnisDao
{

  private static Logger log = Logger.getLogger(SpringAnnisDao.class);
  private int timeout;
  /// old
  private SqlGenerator sqlGenerator;
  private AnnotationGraphDaoHelper annotationGraphDaoHelper;
  private WekaDaoHelper wekaSqlHelper;
  private ListCorpusSqlHelper listCorpusSqlHelper;
  private ListNodeAnnotationsSqlHelper listNodeAnnotationsSqlHelper;
  private ListCorpusAnnotationsSqlHelper listCorpusAnnotationsSqlHelper;
  /// new
  private List<SqlSessionModifier> sqlSessionModifiers;
  private SqlGenerator findSqlGenerator;
  private CountExtractor countExtractor;
  private MatchRowMapper findRowMapper;
  private QueryAnalysis queryAnalysis;
  private DddQueryParser dddQueryParser;
  private ParameterizedSingleColumnRowMapper<String> planRowMapper;
  private ListCorpusByNameDaoHelper listCorpusByNameDaoHelper;
  private DefaultQueryExecutor defaultQueryExecutor;
  private GraphExtractor graphExtractor;
  private List<QueryExecutor> executorList;
  private Map<AQLConstraints, QueryExecutor> executorConstraints;

  public SpringAnnisDao()
  {
    planRowMapper = new ParameterizedSingleColumnRowMapper<String>();
    sqlSessionModifiers = new ArrayList<SqlSessionModifier>();
  }

  // really ugly
  private class QueryTemplate<T>
  {

    public List<T> query(List<Long> corpusList, String dddQuery, SqlGenerator sqlGenerator, ParameterizedRowMapper<T> rowMapper)
    {
      String sql = createSqlAndPrepareSession(corpusList, dddQuery, sqlGenerator);
      return getSimpleJdbcTemplate().query(sql, rowMapper);
    }

    public String explain(List<Long> corpusList, String dddQuery, SqlGenerator sqlGenerator, boolean analyze)
    {
      // prepend SQL query with EXPLAIN
      String prefix = analyze ? "EXPLAIN ANALYZE" : "EXPLAIN";
      String sql = prefix + " " + createSqlAndPrepareSession(corpusList, dddQuery, sqlGenerator);

      // execute sql
      List<String> plan = getSimpleJdbcTemplate().query(sql, planRowMapper);
      return StringUtils.join(plan, "\n");
    }

    private String createSqlAndPrepareSession(List<Long> corpusList, String dddQuery, SqlGenerator sqlGenerator)
    {
      // parse the query
      Start statement = dddQueryParser.parse(dddQuery);

      // analyze it
      QueryData queryData = queryAnalysis.analyzeQuery(statement, corpusList);

      // execute session modifiers
      for (SqlSessionModifier sqlSessionModifier : sqlSessionModifiers)
      {
        sqlSessionModifier.modifySqlSession(getSimpleJdbcTemplate(), queryData);
      }

      // create SQL query
      return sqlGenerator.toSql(queryData, corpusList);
    }
  }

  public List<Match> findMatches(List<Long> corpusList, String dddQuery)
  {
    Validate.notNull(corpusList, "corpusList=null passed as argument");

    List<Match> matches = new QueryTemplate<Match>().query(corpusList, dddQuery, findSqlGenerator, findRowMapper);

    return matches;
  }

  public int countMatches(final List<Long> corpusList, final String dddQuery)
  {
    QueryData queryData = createDynamicMatchView(corpusList, dddQuery);

    return countExtractor.queryCount(getJdbcTemplate());

  }

  @Deprecated
  public int doWait(final int seconds)
  {
    throw new UnsupportedOperationException("doWait was only implemented for debug purposes");
  }

  @SuppressWarnings("unchecked")
  public String planCount(String dddQuery, List<Long> corpusList, boolean analyze)
  {
    Validate.notNull(corpusList, "corpusList=null passed as argument");

    createDynamicMatchView(corpusList, dddQuery);
    return countExtractor.explain(getJdbcTemplate(), analyze);
  }

  public String planGraph(String dddQuery, List<Long> corpusList, 
    long offset, long limit, int left, int right,
    boolean analyze)
  {
    Validate.notNull(corpusList, "corpusList=null passed as argument");

    QueryData queryData = createDynamicMatchView(corpusList, dddQuery);

    int nodeCount = queryData.getMaxWidth();
    return graphExtractor.explain(getJdbcTemplate(), corpusList, nodeCount,
      offset, limit, left, right, analyze);
  }

  @SuppressWarnings("unchecked")
  public List<AnnotationGraph> retrieveAnnotationGraph(List<Long> corpusList, String dddQuery, long offset, long limit, int left, int right)
  {
    QueryData queryData = createDynamicMatchView(corpusList, dddQuery);

    int nodeCount = queryData.getMaxWidth();

    // create the Annis graphs
    return graphExtractor.queryAnnotationGraph(getJdbcTemplate(), corpusList, nodeCount, offset, limit, left, right);
    //return annotationGraphDaoHelper.queryAnnotationGraph(getJdbcTemplate(), nodeCount, corpusList, dddQuery, offset, limit, left, right);
  }

  private QueryData createDynamicMatchView(List<Long> corpusList, String dddQuery)
  {
    // parse the query
    Start statement = dddQueryParser.parse(dddQuery);

    // analyze it
    QueryData queryData = queryAnalysis.analyzeQuery(statement, corpusList);

    // execute session modifiers
    for (SqlSessionModifier sqlSessionModifier : sqlSessionModifiers)
    {
      sqlSessionModifier.modifySqlSession(getSimpleJdbcTemplate(), queryData);
    }

    // generate the view with the matched node IDs
    // TODO: use the constraint approach to filter the executors before we iterate over them
    for (QueryExecutor e : executorList)
    {
      if (e.checkIfApplicable(queryData))
      {
        e.createMatchView(getJdbcTemplate(), corpusList, queryData);
        // leave the loop
        break;
      }
    }

    return queryData;
  }

  @SuppressWarnings("unchecked")
  public List<AnnisNode> annotateMatches(List<Match> matches)
  {
    return (List<AnnisNode>) getJdbcTemplate().query(
      wekaSqlHelper.createSqlQuery(matches), wekaSqlHelper);
  }

  @SuppressWarnings("unchecked")
  public List<AnnisCorpus> listCorpora()
  {
    return (List<AnnisCorpus>) getJdbcTemplate().query(
      listCorpusSqlHelper.createSqlQuery(), listCorpusSqlHelper);
  }

  @SuppressWarnings("unchecked")
  public List<AnnisAttribute> listNodeAnnotations(List<Long> corpusList,
    boolean listValues)
  {
    return (List<AnnisAttribute>) getJdbcTemplate().query(
      listNodeAnnotationsSqlHelper.createSqlQuery(corpusList,
      listValues), listNodeAnnotationsSqlHelper);
  }

  @SuppressWarnings("unchecked")
  public AnnotationGraph retrieveAnnotationGraph(long textId)
  {
    List<AnnotationGraph> graphs = (List<AnnotationGraph>) getJdbcTemplate().query(annotationGraphDaoHelper.createSqlQuery(textId),
      annotationGraphDaoHelper);
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
  public List<Annotation> listCorpusAnnotations(long corpusId)
  {
    final String sql = listCorpusAnnotationsSqlHelper.createSqlQuery(corpusId);
    final List<Annotation> corpusAnnotations =
      (List<Annotation>) getJdbcTemplate().query(sql, listCorpusAnnotationsSqlHelper);
    return corpusAnnotations;
  }

  public List<Long> listCorpusByName(List<String> corpusNames)
  {
    final String sql = listCorpusByNameDaoHelper.createSql(corpusNames);
    final List<Long> result = getSimpleJdbcTemplate().query(sql, listCorpusByNameDaoHelper);
    return result;
  }

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

  ///// private helper
  private MapSqlParameterSource makeArgs()
  {
    return new MapSqlParameterSource();
  }

  // /// Getter / Setter
  public DddQueryParser getDddQueryParser()
  {
    return dddQueryParser;
  }

  public void setDddQueryParser(DddQueryParser parser)
  {
    this.dddQueryParser = parser;
  }

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

  public AnnotationGraphDaoHelper getAnnotateMatchesQueryHelper()
  {
    return getAnnotationGraphDaoHelper();
  }

  public AnnotationGraphDaoHelper getAnnotationGraphDaoHelper()
  {
    return annotationGraphDaoHelper;
  }

  public void setAnnotateMatchesQueryHelper(
    AnnotationGraphDaoHelper annotateMatchesQueryHelper)
  {
    setAnnotationGraphDaoHelper(annotateMatchesQueryHelper);
  }

  public void setAnnotationGraphDaoHelper(
    AnnotationGraphDaoHelper annotationGraphDaoHelper)
  {
    this.annotationGraphDaoHelper = annotationGraphDaoHelper;
  }

  public int getTimeout()
  {
    return timeout;
  }

  public void setTimeout(int timeout)
  {
    this.timeout = timeout;
  }

  public WekaDaoHelper getWekaSqlHelper()
  {
    return wekaSqlHelper;
  }

  public void setWekaSqlHelper(WekaDaoHelper wekaHelper)
  {
    this.wekaSqlHelper = wekaHelper;
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

  public MatchRowMapper getFindRowMapper()
  {
    return findRowMapper;
  }

  public void setFindRowMapper(MatchRowMapper findRowMapper)
  {
    this.findRowMapper = findRowMapper;
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

    executorConstraints = new EnumMap<AQLConstraints, QueryExecutor>(AQLConstraints.class);

    for (QueryExecutor q : this.executorList)
    {
      EnumSet<AQLConstraints> constraints = q.getNeededConstraints();
      for (AQLConstraints c : constraints)
      {
        executorConstraints.put(c, q);
      }
    }

  }
}
