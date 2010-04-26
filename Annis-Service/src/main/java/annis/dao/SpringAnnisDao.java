package annis.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

import annis.WekaDaoHelper;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.ql.parser.QueryAnalysis;
import annis.ql.parser.QueryData;
import annis.resolver.ResolverEntry;
import annis.resolver.SingleResolverRequest;
import annis.service.ifaces.AnnisAttribute;
import annis.service.ifaces.AnnisCorpus;
import annis.sqlgen.CountSqlGenerator;
import annis.sqlgen.ListCorpusAnnotationsSqlHelper;
import annis.sqlgen.ListCorpusSqlHelper;
import annis.sqlgen.ListNodeAnnotationsSqlHelper;
import annis.sqlgen.SqlGenerator;
import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.parser.DddQueryParser;
import java.util.LinkedList;
import org.springframework.jdbc.core.PreparedStatementSetter;

// FIXME: test and refactor timeout and transaction management
public class SpringAnnisDao extends SimpleJdbcDaoSupport implements AnnisDao
{

  private static Logger log = Logger.getLogger(SpringAnnisDao.class);
  private int timeout;
  /// old
  private SqlGenerator sqlGenerator;
  private List<MatchFilter> matchFilters;
  private AnnotationGraphDaoHelper annotationGraphDaoHelper;
  private WekaDaoHelper wekaSqlHelper;
  private ListCorpusSqlHelper listCorpusSqlHelper;
  private ListNodeAnnotationsSqlHelper listNodeAnnotationsSqlHelper;
  private ListCorpusAnnotationsSqlHelper listCorpusAnnotationsSqlHelper;
  /// new
  private List<SqlSessionModifier> sqlSessionModifiers;
  private SqlGenerator findSqlGenerator;
  private CountSqlGenerator countSqlGenerator;
  private MatchRowMapper findRowMapper;
  private QueryAnalysis queryAnalysis;
  private DddQueryParser dddQueryParser;
  private ParameterizedSingleColumnRowMapper<String> planRowMapper;
  private ListCorpusByNameDaoHelper listCorpusByNameDaoHelper;

  public SpringAnnisDao()
  {
    planRowMapper = new ParameterizedSingleColumnRowMapper<String>();
    matchFilters = new ArrayList<MatchFilter>();
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
      return sqlGenerator.toSql(statement, corpusList);
    }
  }

  public List<Match> findMatches(List<Long> corpusList, String dddQuery)
  {
    Validate.notNull(corpusList, "corpusList=null passed as argument");

    List<Match> matches = new QueryTemplate<Match>().query(corpusList, dddQuery, findSqlGenerator, findRowMapper);

    // filter matches
    filter(matches);
    return matches;
  }

  public int countMatches(final List<Long> corpusList, final String dddQuery)
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

    return getSimpleJdbcTemplate().queryForInt(countSqlGenerator.toSql(statement, corpusList));

  }

  @Deprecated
  public int doWait(final int seconds)
  {
    throw new UnsupportedOperationException("doWait was only implemented for debug purposes");
  }

  @SuppressWarnings("unchecked")
  public String plan(String dddQuery, List<Long> corpusList, boolean analyze)
  {
    Validate.notNull(corpusList, "corpusList=null passed as argument");

    return new QueryTemplate().explain(corpusList, dddQuery, countSqlGenerator, analyze);
  }

  @SuppressWarnings("unchecked")
  public List<AnnotationGraph> retrieveAnnotationGraph(List<Match> matches,
    int left, int right)
  {
    if (matches.isEmpty())
    {
      return new ArrayList<AnnotationGraph>();
    }
    return (List<AnnotationGraph>) getJdbcTemplate().query(
      annotationGraphDaoHelper.createSqlQuery(matches, left, right),
      annotationGraphDaoHelper);
  }

  @SuppressWarnings("unchecked")
  public List<AnnotationGraph> retrieveAnnotationGraph(List<Long> corpusList, String dddQuery, long offset, long limit, int left, int right)
  {
    // FIXME: copypaste from QueryTemplate.createSqlAndPrepareSession()
    // still don't know how to refactor this code

    // parse the query
    Start statement = dddQueryParser.parse(dddQuery);

    // analyze it
    QueryData queryData = queryAnalysis.analyzeQuery(statement, corpusList);

    // execute session modifiers
    for (SqlSessionModifier sqlSessionModifier : sqlSessionModifiers)
    {
      sqlSessionModifier.modifySqlSession(getSimpleJdbcTemplate(), queryData);
    }

    return (List<AnnotationGraph>) getJdbcTemplate().query(annotationGraphDaoHelper.createSqlQuery(corpusList, dddQuery, offset, limit, left, right), annotationGraphDaoHelper);
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

  private void filter(List<Match> matches)
  {
    for (MatchFilter filter : matchFilters)
    {
      filter.init();
      List<Match> matchCopy = new ArrayList<Match>(matches);
      for (Match match : matchCopy)
      {
        if (filter.filterMatch(match))
        {
          log.debug("removing match " + match);
          matches.remove(match);
        }
      }
    }
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

  public List<MatchFilter> getMatchFilters()
  {
    return matchFilters;
  }

  public void setMatchFilters(List<MatchFilter> matchFilters)
  {
    this.matchFilters = matchFilters;
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

  public CountSqlGenerator getCountSqlGenerator()
  {
    return countSqlGenerator;
  }

  public void setCountSqlGenerator(CountSqlGenerator countSqlGenerator)
  {
    this.countSqlGenerator = countSqlGenerator;
  }
}
