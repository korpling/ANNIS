package de.deutschdiachrondigital.dddquery;

import de.deutschdiachrondigital.dddquery.parser.QueryAnalysis;
import java.util.ArrayList;
import java.util.List;


import annis.AnnisBaseRunner;
import annis.AnnotationGraphDotExporter;
import annis.TableFormatter;
import annis.WekaHelper;
import annis.dao.AnnisDao;
import annis.dao.AnnotatedMatch;
import annis.dao.GraphExtractor;
import annis.dao.MetaDataFilter;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.ql.parser.QueryData;
import annis.service.ifaces.AnnisAttribute;
import annis.service.ifaces.AnnisCorpus;
import annis.sqlgen.ListCorpusSqlHelper;
import annis.sqlgen.ListNodeAnnotationsSqlHelper;
import annis.sqlgen.SqlGenerator;
import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.parser.DddQueryParser;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

public class DddQueryRunner extends AnnisBaseRunner
{

//	private static Logger log = Logger.getLogger(DddQueryRunner.class);
  // dependencies
  private DddQueryParser dddQueryParser;
  private SqlGenerator findSqlGenerator;
  private MetaDataFilter metaDataFilter;
  private AnnisDao annisDao;
  private QueryAnalysis queryAnalysis;
  private WekaHelper wekaHelper;
  private ListCorpusSqlHelper listCorpusHelper;
  private ListNodeAnnotationsSqlHelper listNodeAnnotationsSqlHelper;
  private AnnotationGraphDotExporter annotationGraphDotExporter;
  private TableFormatter tableFormatter;
  // settings
  private int matchLimit;
  private int context;
  private List<Long> corpusList;

  public DddQueryRunner()
  {
    corpusList = new ArrayList<Long>();
  }

  public static void main(String[] args)
  {
    // get runner from Spring
    AnnisBaseRunner.getInstance("dddQueryRunner", "de/deutschdiachrondigital/dddquery/DddQueryRunner-context.xml").run(args);
  }

  ///// CLI methods
  public void doParse(String dddQuery)
  {
    out.println(DddQueryParser.dumpTree(dddQueryParser.parse(dddQuery)));
  }

  // FIXME: missing tests
  public void doSql(String dddQuery)
  {
    // sql query
    Start statement = dddQueryParser.parse(dddQuery);
    QueryData queryData = queryAnalysis.analyzeQuery(statement, corpusList);

    String sql = findSqlGenerator.toSql(queryData, corpusList, metaDataFilter.getDocumentsForMetadata(queryData));

    out.println(sql);
  }

  public void doSqlGraph(String dddQuery)
  {
    // sql query
    Start statement = dddQueryParser.parse(dddQuery);
    QueryData queryData = queryAnalysis.analyzeQuery(statement, corpusList);

    String sql = findSqlGenerator.toSql(queryData, corpusList, metaDataFilter.getDocumentsForMetadata(queryData));

    out.println("CREATE TEMPORARY VIEW matched_nodes AS " + sql + ";");

    GraphExtractor ge = new GraphExtractor();
    ge.setMatchedNodesViewName("matched_nodes");
    out.println(ge.getContextQuery(corpusList, context, context, matchLimit, 0, queryData.getMaxWidth())
      + ";");

  }

  public void doMatrix(String dddQuery)
  {
    List<AnnotatedMatch> matches = annisDao.matrix(getCorpusList(), annisDao.parseDDDQuery(dddQuery, getCorpusList()));
    if (matches.isEmpty())
    {
      out.println("(empty");
    }
    else
    {
      WekaHelper helper = new WekaHelper();
      out.println(helper.exportAsArff(matches));
    }
  }

  public void doCount(String dddQuery)
  {
    out.println(annisDao.countMatches(getCorpusList(), annisDao.parseDDDQuery(dddQuery, getCorpusList())));
  }

  public void doPlanCount(String dddQuery)
  {
    out.println(annisDao.planCount(annisDao.parseDDDQuery(dddQuery, getCorpusList()), getCorpusList(), false));
  }

  public void doPlanGraph(String dddQuery)
  {
    out.println(annisDao.planGraph(annisDao.parseDDDQuery(dddQuery, getCorpusList()), getCorpusList(),
      0, matchLimit, context, context, false));
  }

  public void doAnalyzeCount(String dddQuery)
  {
    out.println(annisDao.planCount(annisDao.parseDDDQuery(dddQuery, getCorpusList()), getCorpusList(), true));
  }

  public void doAnalyzeGraph(String dddQuery)
  {
    out.println(annisDao.planGraph(annisDao.parseDDDQuery(dddQuery, getCorpusList()), getCorpusList(),
      0, matchLimit, context, context, true));
  }

  public void doAnnotate(String dddQuery)
  {
    List<AnnotationGraph> graphs = annisDao.retrieveAnnotationGraph(getCorpusList(), annisDao.parseDDDQuery(dddQuery, getCorpusList()), 0, matchLimit, context, context);
    printAsTable(graphs, "nodes", "edges");
  }

  public void doCorpus(List<Long> corpora)
  {
    if (corpora.isEmpty())
    {
      setPrompt("no corpus>");
    }
    else
    {
      setPrompt(StringUtils.join(corpora, ",") + ">");
    }
    setCorpusList(corpora);
  }

  public void doList(String unused)
  {
    List<AnnisCorpus> corpora = annisDao.listCorpora();
    printAsTable(corpora, "id", "name", "textCount", "tokenCount");
  }

  public void doNodeAnnotations(String doListValues)
  {
    boolean listValues = "values".equals(doListValues);
    List<AnnisAttribute> nodeAnnotations = annisDao.listNodeAnnotations(getCorpusList(), listValues);
    printAsTable(nodeAnnotations, "name", "distinctValues");
  }

  public void doMeta(String corpusId)
  {
    List<Annotation> corpusAnnotations = annisDao.listCorpusAnnotations(Long.parseLong(corpusId));
    printAsTable(corpusAnnotations, "namespace", "name", "value");
  }
  ///// Helper

  private void printAsTable(List<? extends Object> list, String... fields)
  {
    out.println(tableFormatter.formatAsTable(list, fields));
  }

  ///// Getter / Setter
  public DddQueryParser getDddQueryParser()
  {
    return dddQueryParser;
  }

  public void setDddQueryParser(DddQueryParser dddQueryParser)
  {
    this.dddQueryParser = dddQueryParser;
  }

  public SqlGenerator getFindSqlGenerator()
  {
    return findSqlGenerator;
  }

  public void setFindSqlGenerator(SqlGenerator sqlGenerator)
  {
    this.findSqlGenerator = sqlGenerator;
  }

  public AnnisDao getAnnisDao()
  {
    return annisDao;
  }

  public void setAnnisDao(AnnisDao annisDao)
  {
    this.annisDao = annisDao;
  }

  public int getMatchLimit()
  {
    return matchLimit;
  }

  public void setMatchLimit(int matchLimit)
  {
    this.matchLimit = matchLimit;
  }

  public List<Long> getCorpusList()
  {
    return corpusList;
  }

  public void setCorpusList(List<Long> corpusList)
  {
    this.corpusList = corpusList;
  }

  public TableFormatter getTableFormatter()
  {
    return tableFormatter;
  }

  public void setTableFormatter(TableFormatter tableFormatter)
  {
    this.tableFormatter = tableFormatter;
  }

  public WekaHelper getWekaHelper()
  {
    return wekaHelper;
  }

  public void setWekaHelper(WekaHelper wekaDaoHelper)
  {
    this.wekaHelper = wekaDaoHelper;
  }

  public ListCorpusSqlHelper getListCorpusHelper()
  {
    return listCorpusHelper;
  }

  public void setListCorpusHelper(ListCorpusSqlHelper listCorpusHelper)
  {
    this.listCorpusHelper = listCorpusHelper;
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

  public AnnotationGraphDotExporter getAnnotationGraphDotExporter()
  {
    return annotationGraphDotExporter;
  }

  public void setAnnotationGraphDotExporter(
    AnnotationGraphDotExporter annotationGraphDotExporter)
  {
    this.annotationGraphDotExporter = annotationGraphDotExporter;
  }

  public int getContext()
  {
    return context;
  }

  public void setContext(int context)
  {
    this.context = context;
  }

  public QueryAnalysis getQueryAnalysis()
  {
    return queryAnalysis;
  }

  public void setQueryAnalysis(QueryAnalysis queryAnalysis)
  {
    this.queryAnalysis = queryAnalysis;
  }

  public MetaDataFilter getMetaDataFilter()
  {
    return metaDataFilter;
  }

  public void setMetaDataFilter(MetaDataFilter metaDataFilter)
  {
    this.metaDataFilter = metaDataFilter;
  }
}
