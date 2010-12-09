package annis;

import annis.dao.AnnisDao;
import annis.dao.AnnotatedMatch;
import annis.dao.GraphExtractor;
import annis.dao.MetaDataFilter;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.ql.parser.QueryAnalysis;
import annis.ql.parser.AnnisParser;
import annis.ql.parser.QueryData;
import annis.service.ifaces.AnnisAttribute;
import annis.service.ifaces.AnnisCorpus;
import annis.sqlgen.SqlGenerator;
import de.deutschdiachrondigital.dddquery.DddQueryMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

// TODO: test AnnisRunner
public class AnnisRunner extends AnnisBaseRunner
{

  private List<Long> corpusList;
  private AnnisDao annisDao;
  private AnnisParser annisParser;
  // map Annis queries to DDDquery
  private DddQueryMapper dddQueryMapper;
  private QueryAnalysis aqlAnalysis;
  private SqlGenerator findSqlGenerator;
  private MetaDataFilter metaDataFilter;
  private int context;
  private int matchLimit;
  private boolean isDDDQueryMode;

  public static void main(String[] args)
  {
    // get runner from Spring
    AnnisBaseRunner.getInstance("annisRunner", "annis/AnnisRunner-context.xml").run(args);
  }

  public AnnisRunner()
  {
    corpusList = new LinkedList<Long>();
    isDDDQueryMode = false;
  }


  // switch between AQL as input mode and DDDQuery
  public void doLanguage(String newLanguage)
  {
    if("ddd".equalsIgnoreCase(newLanguage) || "dddquery".equalsIgnoreCase(newLanguage))
    {
      isDDDQueryMode = true;
      System.out.println("new input language is DDDQuery");
    }
    else
    {
      isDDDQueryMode = false;
      System.out.println("new input language is AQL");
    }
  }

  ///// Commands
  public void doDebug(String ignore)
  {
    doDddquery("node & node & #1 > #2");
  }

  public void doProposedIndex(String ignore)
  {
    File fInput = new File("queries.txt");

    Map<String, List<String>> output = new HashMap<String, List<String>>();

    if (fInput.exists())
    {
      try
      {
        String[] content = FileUtils.readFileToString(fInput).split("\n");

        for (String query : content)
        {
          if (query.trim().length() > 0)
          {
            Map<String, Set<String>> map = proposedIndexHelper(query.trim());
            for (String table : map.keySet())
            {
              if (!output.containsKey(table))
              {
                output.put(table, new LinkedList<String>());
              }
              Set<String> l = map.get(table);
              if (l.size() > 0)
              {
                output.get(table).add(StringUtils.join(l, ","));
              }
              out.println(query + "/" + table + ": " + map.get(table));
            }
          }
        }

        for (String table : output.keySet())
        {
          File fOutput = new File(table + "_attributes.csv");
          FileUtils.writeLines(fOutput, output.get(table));
        }

      }
      catch (IOException ex)
      {
        Logger.getLogger(AnnisRunner.class.getName()).log(Level.SEVERE, null, ex);
      }

    }
    else
    {
      out.println("Could not find queries.txt");
    }
  }

  public void doDddquery(String annisQuery)
  {
    out.println(translate(annisQuery));
  }

  public void doParse(String annisQuery)
  {
    out.println(annisParser.dumpTree(annisQuery));
  }

  public void doSql(String annisQuery)
  {
    // sql query
    QueryData queryData = parse(annisQuery);

    String sql = findSqlGenerator.toSql(queryData, corpusList, metaDataFilter.getDocumentsForMetadata(queryData));

    out.println(sql);
  }

  public void doSqlGraph(String annisQuery)
  {
    // sql query
    QueryData queryData = parse(annisQuery);

    String sql = findSqlGenerator.toSql(queryData, corpusList, metaDataFilter.getDocumentsForMetadata(queryData));

    out.println("CREATE TEMPORARY VIEW matched_nodes AS " + sql + ";");

    GraphExtractor ge = new GraphExtractor();
    ge.setMatchedNodesViewName("matched_nodes");
    out.println(ge.getContextQuery(corpusList, context, context, matchLimit, 0, queryData.getMaxWidth())
      + ";");
  }

  public void doMatrix(String annisQuery)
  {
    List<AnnotatedMatch> matches = annisDao.matrix(getCorpusList(), parse(annisQuery));
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

  public void doCount(String annisQuery)
  {
    out.println(annisDao.countMatches(getCorpusList(), parse(annisQuery)));
  }

  public void doPlanCount(String annisQuery)
  {
    out.println(annisDao.planCount(parse(annisQuery), getCorpusList(), false));
  }

  public void doAnalyzeCount(String annisQuery)
  {
    out.println(annisDao.planCount(parse(annisQuery), getCorpusList(), true));
  }

  public void doPlanGraph(String annisQuery)
  {
    out.println(annisDao.planGraph(parse(annisQuery), getCorpusList(),
      0, matchLimit, context, context, false));
  }

  public void doAnalyzeGraph(String annisQuery)
  {
    out.println(annisDao.planGraph(parse(annisQuery), getCorpusList(),
      0, matchLimit, context, context, true));
  }

  public void doAnnotate(String annisQuery)
  {
    List<AnnotationGraph> graphs = annisDao.retrieveAnnotationGraph(getCorpusList(),
      parse(annisQuery), 0, matchLimit, context, context);
    printAsTable(graphs, "nodes", "edges");
  }

  public void doCorpus(String list)
  {
    corpusList = dddQueryMapper.translateCorpusList(list);
    if (corpusList.isEmpty())
    {
      setPrompt("no corpus>");
    }
    else
    {
      setPrompt(StringUtils.join(corpusList, ",") + ">");
    }
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

  public void doText(String textID)
  {
    List<AnnotationGraph> result = new LinkedList<AnnotationGraph>();
    AnnotationGraph graph =  annisDao.retrieveAnnotationGraph(Long.parseLong(textID));
    result.add(graph);
    printAsTable(result, "nodes", "edges");
  }

  public void doQuit(String dummy)
  {
    System.out.println("bye bye!");
    System.exit(0);
  }

  public void doCompareParser(String query)
  {
    QueryData qdAQL = annisDao.parseAQL(query, null);
    QueryData qdDDD = annisDao.parseDDDQuery(translate(query), null);

    String strAQL = qdAQL.toString();
    String strDDD = qdDDD.toString();

    if(strAQL.equals(strDDD))
    {
      System.out.println(strAQL);
      System.out.println("both are equal");
    }
    else
    {
      System.out.println("AQL:");
      System.out.println(strAQL);
      System.out.println("DDD:");
      System.out.println(strDDD);
      System.out.println("NOT EQUAL");
    }
  }

  ///// Delegates for convenience
  private String translate(String annisQuery)
  {
    return dddQueryMapper.translate(annisQuery);
  }


  public Map<String, Set<String>> proposedIndexHelper(String aql)
  {
    Map<String, Set<String>> result = new HashMap<String, Set<String>>();
    result.put("facts", new TreeSet<String>());
    result.put("node", new TreeSet<String>());
    result.put("node_annotation", new TreeSet<String>());

    // sql query
    QueryData queryData = parse(aql);

    String sql = findSqlGenerator.toSql(queryData, corpusList, metaDataFilter.getDocumentsForMetadata(queryData));

    // extract WHERE clause

    Matcher mWhere = Pattern.compile("WHERE\n").matcher(sql);
    if (mWhere.find())
    {
      String whereClause = sql.substring(mWhere.end());
      //out.println("WHERE clause:\n" + whereClause);

      for (String table : result.keySet())
      {
        Set<String> attr = result.get(table);
        Matcher mFacts = Pattern.compile(table + "[0-9]+\\.([a-zA-Z0-9_]+)").matcher(whereClause);
        while (mFacts.find())
        {
          attr.add(mFacts.group(1).trim());
        }
      }

      // print result
      //out.println("facts: " + StringUtils.join(factsAttributes, ", "));
      //out.println("node: " + StringUtils.join(nodeAttributes, ", "));
      //out.println("suggested index: ");
      //out.println("CREATE INDEX idx__facts__noname ON facts (" + StringUtils.join(factsAttributes, ", ") + ");");
      //out.println("CREATE INDEX idx__node__noname ON node (" + StringUtils.join(nodeAttributes, ", ") + ");");
    }
    else
    {
      out.println("Could not find the WHERE clause");
    }
    return result;
  }

  private void printAsTable(List<? extends Object> list, String... fields)
  {
    out.println(new TableFormatter().formatAsTable(list, fields));
  }

  private QueryData parse(String input)
  {
    if(isDDDQueryMode)
    {
      return annisDao.parseDDDQuery(input, getCorpusList());
    }
    else
    {
      return annisDao.parseAQL(input, getCorpusList());
    }
  }

  ///// Getter / Setter
  public DddQueryMapper getDddQueryMapper()
  {
    return dddQueryMapper;
  }

  public void setDddQueryMapper(DddQueryMapper dddQueryMapper)
  {
    this.dddQueryMapper = dddQueryMapper;
  }

  public AnnisParser getAnnisParser()
  {
    return annisParser;
  }

  public void setAnnisParser(AnnisParser annisParser)
  {
    this.annisParser = annisParser;
  }

    public AnnisDao getAnnisDao()
  {
    return annisDao;
  }

  public void setAnnisDao(AnnisDao annisDao)
  {
    this.annisDao = annisDao;
  }

  public QueryAnalysis getAqlAnalysis()
  {
    return aqlAnalysis;
  }

  public void setAqlAnalysis(QueryAnalysis aqlAnalysis)
  {
    this.aqlAnalysis = aqlAnalysis;
  }

  public SqlGenerator getFindSqlGenerator()
  {
    return findSqlGenerator;
  }

  public void setFindSqlGenerator(SqlGenerator findSqlGenerator)
  {
    this.findSqlGenerator = findSqlGenerator;
  }

  public List<Long> getCorpusList()
  {
    return corpusList;
  }

  public void setCorpusList(List<Long> corpusList)
  {
    this.corpusList = corpusList;
  }

  public MetaDataFilter getMetaDataFilter()
  {
    return metaDataFilter;
  }

  public void setMetaDataFilter(MetaDataFilter metaDataFilter)
  {
    this.metaDataFilter = metaDataFilter;
  }

  public int getContext()
  {
    return context;
  }

  public void setContext(int context)
  {
    this.context = context;
  }

  public int getMatchLimit()
  {
    return matchLimit;
  }

  public void setMatchLimit(int matchLimit)
  {
    this.matchLimit = matchLimit;
  }
}
