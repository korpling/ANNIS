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
package annis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import annis.dao.AnnisDao;
import annis.dao.AnnotatedMatch;
import annis.dao.Match;
import annis.dao.MetaDataFilter;
import annis.model.Annotation;
import annis.model.QueryAnnotation;
import annis.model.AnnotationGraph;
import annis.ql.parser.AnnisParser;
import annis.ql.parser.QueryAnalysis;
import annis.ql.parser.QueryData;
import annis.service.ifaces.AnnisAttribute;
import annis.service.ifaces.AnnisCorpus;
import annis.service.objects.AnnisAttributeSetImpl;
import annis.sqlgen.AnnotateSqlGenerator.AnnotateQueryData;
import annis.sqlgen.MatrixSqlGenerator;
import annis.sqlgen.SqlGenerator;
import annis.utils.LegacyGraphConverter;
import annis.utils.Utils;
import de.deutschdiachrondigital.dddquery.DddQueryMapper;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import org.eclipse.emf.common.util.URI;

// TODO: test AnnisRunner
public class AnnisRunner extends AnnisBaseRunner
{

  // logging
  private Logger log = Logger.getLogger(this.getClass());
  // SQL generators for query functions
  private SqlGenerator<QueryData, List<Match>> findSqlGenerator;
  private SqlGenerator<QueryData, Integer> countSqlGenerator;
  private SqlGenerator<QueryData, List<AnnotationGraph>> annotateSqlGenerator;
  private SqlGenerator<QueryData, List<AnnotatedMatch>> matrixSqlGenerator;
  // dependencies
  private AnnisDao annisDao;
  private AnnisParser annisParser;
  // map Annis queries to DDDquery
  private DddQueryMapper dddQueryMapper;
  private QueryAnalysis aqlAnalysis;
  private MetaDataFilter metaDataFilter;
  private int context;
  private int matchLimit;
  private boolean isDDDQueryMode;
  private QueryAnalysis queryAnalysis;
  // settings
  private int limit;
  private int offset;
  private int left;
  private int right;
  private List<Long> corpusList;
  private boolean clearCaches;

  public enum OS
  {

    linux,
    other
  }

  // benchmarking
  private static class Benchmark
  {

    private String functionCall;
    private QueryData queryData;
    private long avgTimeInMilliseconds;
    private long bestTimeInMilliseconds;
    private long worstTimeInMilliseconds;
    private String sql;
    private String plan;
    private int runs;
    private int errors;

    public Benchmark(String functionCall, QueryData queryData)
    {
      super();
      this.functionCall = functionCall;
      this.queryData = queryData;
    }
  }
  private List<AnnisRunner.Benchmark> benchmarks;
  private static final int SEQUENTIAL_RUNS = 5;

  public static void main(String[] args)
  {
    // get runner from Spring
    AnnisBaseRunner.getInstance("annisRunner", Utils.getConfigFile(
      "spring/AnnisRunner-context.xml").getAbsolutePath()).run(args);
  }

  public AnnisRunner()
  {
    corpusList = new LinkedList<Long>();
    benchmarks = new ArrayList<AnnisRunner.Benchmark>();
    isDDDQueryMode = false;
  }

  // switch between AQL as input mode and DDDQuery
  public void doLanguage(String newLanguage)
  {
    if ("ddd".equalsIgnoreCase(newLanguage) || "dddquery".equalsIgnoreCase(
      newLanguage))
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
    doCorpus("tiger2");
    doSet("limit to 10");
    doSet("offset to 100");
    doSet("left to 5");
    doSet("right to 5");
    doAnnotate("cat=\"S\" & \"das\" & #1 >* #2");
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
            for (Map.Entry<String, Set<String>> t : map.entrySet())
            {
              String table = t.getKey();
              Set<String> l = t.getValue();

              if (!output.containsKey(table))
              {
                output.put(table, new LinkedList<String>());
              }

              if (l.size() > 0)
              {
                output.get(table).add(StringUtils.join(l, ","));
              }
              out.println(query + "/" + table + ": " + l);
            }
          }
        }

        for (Entry<String, List<String>> entry : output.entrySet())
        {
          File fOutput = new File(entry.getKey() + "_attributes.csv");
          FileUtils.writeLines(fOutput, entry.getValue());
        }

      }
      catch (IOException ex)
      {
        log.warn("Problem reading queries.txt", ex);
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

  @Deprecated
  public void doSqlOld(String annisQuery)
  {
    // sql query
    QueryData queryData = parse(annisQuery);
    queryData.setCorpusList(corpusList);
    queryData.setDocuments(metaDataFilter.getDocumentsForMetadata(queryData));

    String sql = findSqlGenerator.toSql(queryData);

    out.println(sql);
  }

  // FIXME: missing tests
  public void doSql(String functionCall)
  {
    SqlGenerator<QueryData, ?> generator = getGeneratorForQueryFunction(
      functionCall);
    String annisQuery = getAnnisQueryFromFunctionCall(functionCall);
    QueryData queryData = analyzeQuery(annisQuery, null);
    out.println("NOTICE: left = " + left + "; right = " + right + "; limit = "
      + limit + "; offset = " + offset);

    out.println(generator.toSql(queryData));
  }

  public void doExplain(String functionCall, boolean analyze)
  {
    SqlGenerator<QueryData, ?> generator = getGeneratorForQueryFunction(
      functionCall);
    String annisQuery = getAnnisQueryFromFunctionCall(functionCall);
    QueryData queryData = analyzeQuery(annisQuery, null);
    out.println("NOTICE: left = " + left + "; right = " + right + "; limit = "
      + limit + "; offset = " + offset);

    out.println(annisDao.explain(generator, queryData, analyze));
  }

  public void doPlan(String functionCall)
  {
    doExplain(functionCall, false);
  }

  public void doAnalyze(String functionCall)
  {
    doExplain(functionCall, true);
  }

  private SqlGenerator<QueryData, ?> getGeneratorForQueryFunction(
    String functionCall)
  {
    String[] split = functionCall.split(" ", 2);

    Validate.isTrue(split.length == 2, "bad call to plan");
    String function = split[0];

    SqlGenerator<QueryData, ?> generator = null;
    if ("count".equals(function))
    {
      generator = countSqlGenerator;
    }
    if ("find".equals(function))
    {
      generator = findSqlGenerator;
    }
    if ("annotate".equals(function))
    {
      generator = annotateSqlGenerator;
    }
    if ("matrix".equals(function))
    {
      generator = matrixSqlGenerator;
    }
    Validate.notNull(generator, "don't now query function: " + function);

    return generator;
  }

  private String getAnnisQueryFromFunctionCall(String functionCall)
  {
    String[] split = functionCall.split(" ", 2);

    Validate.isTrue(split.length == 2, "bad call to plan");
    return split[1];
  }

  public void doRecord(String dummy)
  {
    out.println("recording new benchmark session");
    benchmarks.clear();
  }

  public void doBenchmark(String benchmarkCount)
  {
    int count = Integer.parseInt(benchmarkCount);
    out.println("---> executing " + benchmarks.size() + " queries " + count
      + " times");

    AnnisRunner.OS currentOS = AnnisRunner.OS.other;
    try
    {
      currentOS = AnnisRunner.OS.valueOf(System.getProperty("os.name").
        toLowerCase());
    }
    catch (IllegalArgumentException ex)
    {
    }

    List<AnnisRunner.Benchmark> session = new ArrayList<AnnisRunner.Benchmark>();

    // create sql + plan for each query and create count copies for each benchmark
    for (AnnisRunner.Benchmark benchmark : benchmarks)
    {
      if (clearCaches)
      {
        resetCaches(currentOS);
      }

      SqlGenerator<QueryData, ?> generator =
        getGeneratorForQueryFunction(benchmark.functionCall);
      benchmark.sql = getGeneratorForQueryFunction(benchmark.functionCall).toSql(
        benchmark.queryData);
      out.println("---> SQL query for: " + benchmark.functionCall);
      out.println(benchmark.sql);
      try
      {
        benchmark.plan = annisDao.explain(generator, benchmark.queryData, false);
        out.println("---> query plan for: " + benchmark.functionCall);
        out.println(benchmark.plan);
      }
      catch (RuntimeException e)
      { // nested DataAccessException would be better
        out.println("---> query plan failed for " + benchmark.functionCall);
      }
      benchmark.bestTimeInMilliseconds = Long.MAX_VALUE;
      benchmark.worstTimeInMilliseconds = Long.MIN_VALUE;
      out.println("---> running query sequentially " + SEQUENTIAL_RUNS
        + " times");
      String options = benchmarkOptions(benchmark.queryData);
      for (int i = 0; i < SEQUENTIAL_RUNS; ++i)
      {
        if (i > 0)
        {
          out.print(", ");
        }
        long start = new Date().getTime();
        try
        {
          annisDao.executeQueryFunction(benchmark.queryData, generator);
        }
        catch (RuntimeException e)
        {
          // don't care
        }
        long end = new Date().getTime();
        long runtime = end - start;
        benchmark.bestTimeInMilliseconds =
          Math.min(benchmark.bestTimeInMilliseconds, runtime);
        benchmark.worstTimeInMilliseconds =
          Math.max(benchmark.worstTimeInMilliseconds, runtime);
        out.print(runtime + " ms");
      }
      out.println();
      out.println(benchmark.bestTimeInMilliseconds + " ms best time for '"
        + benchmark.functionCall + ("".equals(options) ? "'" : "' with "
        + options));
      session.addAll(Collections.nCopies(count, benchmark));
    }

    // clear cache again in order to treat the last query in the list equal to 
    // the others
    if (clearCaches)
    {
      resetCaches(currentOS);
    }

    // shuffle the benchmark queries
    Collections.shuffle(session);
    out.println();
    out.println("---> running queries in random order");

    // execute the queries, record test times
    for (AnnisRunner.Benchmark benchmark : session)
    {
      if (benchmark.errors >= 3)
      {
        continue;
      }
      boolean error = false;
      SqlGenerator<QueryData, ?> generator =
        getGeneratorForQueryFunction(benchmark.functionCall);
      long start = new Date().getTime();
      try
      {
        annisDao.executeQueryFunction(benchmark.queryData, generator);
      }
      catch (RuntimeException e)
      {
        error = true;
      }
      long end = new Date().getTime();
      long runtime = end - start;
      benchmark.avgTimeInMilliseconds += runtime;
      ++benchmark.runs;
      if (error)
      {
        ++benchmark.errors;
      }
      String options = benchmarkOptions(benchmark.queryData);
      out.println(runtime + " ms for '" + benchmark.functionCall + ("".equals(
        options) ? "'" : "' with " + options) + (error ? " ERROR" : ""));
    }

    // compute average runtime for each query
    out.println();
    out.println("---> benchmark complete");
    for (AnnisRunner.Benchmark benchmark : benchmarks)
    {
      benchmark.avgTimeInMilliseconds = Math.round(benchmark.avgTimeInMilliseconds
        / benchmark.runs);
      String options = benchmarkOptions(benchmark.queryData);
      out.println(benchmark.avgTimeInMilliseconds + " ms (avg for "
        + benchmark.runs + " runs" + (benchmark.errors > 0 ? ", "
        + benchmark.errors + " errors)" : ")") + " for '"
        + benchmark.functionCall + ("".equals(options) ? "'" : "' with "
        + options));
    }


    // show best runtime for each query
    out.println();
    out.println("---> worst times");
    for (AnnisRunner.Benchmark benchmark : benchmarks)
    {
      String options = benchmarkOptions(benchmark.queryData);
      out.println(benchmark.worstTimeInMilliseconds + " ms ("
        + (benchmark.errors > 0 ? "?"
        + benchmark.errors + " errors)" : ")") + " for '"
        + benchmark.functionCall + ("".equals(options) ? "'" : "' with "
        + options));
    }

    // show best runtime for each query
    out.println();
    out.println("---> best times");
    for (AnnisRunner.Benchmark benchmark : benchmarks)
    {
      String options = benchmarkOptions(benchmark.queryData);
      out.println(benchmark.bestTimeInMilliseconds + " ms ("
        + (benchmark.errors > 0 ? "?"
        + benchmark.errors + " errors)" : ")") + " for '"
        + benchmark.functionCall + ("".equals(options) ? "'" : "' with "
        + options));
    }
    out.println();
  }

  public String benchmarkOptions(QueryData queryData)
  {
    List<Long> corpusList = queryData.getCorpusList();
    List<QueryAnnotation> metaData = queryData.getMetaData();
    Set<Object> extensions = queryData.getExtensions();
    List<String> fields = new ArrayList<String>();
    if (!corpusList.isEmpty())
    {
      fields.add("corpus = " + corpusList);
    }
    if (!metaData.isEmpty())
    {
      fields.add("meta = " + metaData);
    }
    for (Object extension : extensions)
    {
      String toString = extension.toString();
      if (!"".equals(toString))
      {
        fields.add(toString);
      }
    }
    return StringUtils.join(fields, ", ");
  }

  private void resetCaches(AnnisRunner.OS currentOS)
  {
    switch (currentOS)
    {
      case linux:
        try
        {
          log.info("resetting caches");
          log.debug("syncing");
          Runtime.getRuntime().exec("sync").waitFor();
          File dropCaches = new File("/proc/sys/vm/drop_caches");
          if (dropCaches.canWrite())
          {
            log.debug("clearing file system cache");
            Writer w = new FileWriter(dropCaches);
            w.write("3");
            w.close();
          }
          else
          {
            log.warn("Cannot clear file system cache of the operating system");
          }

          File postgresScript = new File("/etc/init.d/postgresql");
          if (postgresScript.exists() && postgresScript.isFile())
          {
            log.debug("restarting postgresql");
            Runtime.getRuntime().exec(postgresScript.getAbsolutePath()
              + " restart").waitFor();
          }
          else
          {
            log.warn("Cannot restart postgresql");
          }

        }
        catch (Exception ex)
        {
          log.error(null, ex);
        }

        break;
      default:
        log.warn("Cannot reset cache on this operating system");
    }
  }

  public void doSet(String callToSet)
  {
    String[] split = callToSet.split(" ", 3);
    Validate.isTrue(split.length > 0, "syntax error: set " + callToSet);

    String setting = split[0];
    String value = null;

    boolean show = split.length == 1 && setting.startsWith("?");
    if (show)
    {
      setting = setting.substring(1);
    }
    else
    {
      Validate.isTrue(split.length == 3 && "TO".toLowerCase().equals(split[1]), "syntax error: set "
        + callToSet);
      value = split[2];
    }

    if ("limit".equals(setting))
    {
      if (show)
      {
        value = String.valueOf(limit);
      }
      else
      {
        limit = Integer.parseInt(value);
      }
    }
    else if ("offset".equals(setting))
    {
      if (show)
      {
        value = String.valueOf(offset);
      }
      else
      {
        offset = Integer.parseInt(value);
      }
    }
    else if ("left".equals(setting))
    {
      if (show)
      {
        value = String.valueOf(left);
      }
      else
      {
        left = Integer.parseInt(value);
      }
    }
    else if ("right".equals(setting))
    {
      if (show)
      {
        value = String.valueOf(right);
      }
      else
      {
        right = Integer.parseInt(value);
      }
    }
    else if ("context".equals(setting))
    {
      if (show)
      {
        if (left != right)
        {
          value = "(left != right)";
        }
        else
        {
          value = String.valueOf(left);
        }
      }
      else
      {
        left = Integer.parseInt(value);
        right = left;
      }
    }
    else if ("timeout".equals(setting))
    {
      if (show)
      {
        value = String.valueOf(annisDao.getTimeout());
      }
      else
      {
        annisDao.setTimeout(Integer.parseInt(value));
      }
    }
    else if ("clear-caches".equals(setting))
    {
      if (show)
      {
        value = String.valueOf(clearCaches);
      }
      else
      {
        clearCaches = Boolean.parseBoolean(value);
      }
    }
    else if ("corpus-list".equals(setting))
    {
      corpusList.clear();
      if (!"all".equals(value))
      {
        String[] list = value.split(" ");
        for (String corpus : list)
        {
          corpusList.add(Long.parseLong(corpus));
        }
      }
    }
    else
    {
      out.println("ERROR: unknown option: " + setting);
    }

    out.println(setting + ": " + value);
  }

  public void doShow(String setting)
  {
    doSet("?" + setting);
  }

  public void doSqlMatrix(String annisQuery)
  {
    // sql query
    QueryData queryData = parse(annisQuery);
    queryData.setCorpusList(corpusList);
    queryData.setDocuments(metaDataFilter.getDocumentsForMetadata(queryData));

    String sql = findSqlGenerator.toSql(queryData);

    out.println("CREATE OR REPLACE TEMPORARY VIEW matched_nodes AS " + sql + ";");

    MatrixSqlGenerator me = new MatrixSqlGenerator();
    me.setMatchedNodesViewName("matched_nodes");
    out.println(me.getMatrixQuery(corpusList, queryData.getMaxWidth())
      + ";");
  }

  private QueryData analyzeQuery(String annisQuery, String queryFunction)
  {
    QueryData queryData = annisDao.parseAQL(annisQuery, corpusList);
    queryData.setCorpusConfiguration(annisDao.getCorpusConfiguration());
    queryData.addExtension(new AnnotateQueryData(offset, limit, left, right));
    if (annisQuery != null)
    {
      benchmarks.add(new AnnisRunner.Benchmark(queryFunction + " " + annisQuery,
        queryData));
    }
    out.println("NOTICE: corpus = " + queryData.getCorpusList());
    return queryData;
  }

  public void doCount(String annisQuery)
  {
    out.println(annisDao.count(analyzeQuery(annisQuery, "count")));
  }

  public void doMatrix(String annisQuery)
  {
//	    List<AnnotatedMatch> matches = annisDao.matrix(getCorpusList(), parse(annisQuery));
    List<AnnotatedMatch> matches = annisDao.matrix(analyzeQuery(annisQuery,
      "matrix"));
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

  public void doFind(String annisQuery)
  {
    List<Match> matches = annisDao.find(analyzeQuery(annisQuery, "find"));
    printAsTable(matches);
  }

  public void doAnnotate(String annisQuery)
  {
    QueryData queryData = analyzeQuery(annisQuery, "annotate");
    out.println("NOTICE: left = " + left + "; right = " + right + "; limit = "
      + limit + "; offset = " + offset);
    SaltProject result = annisDao.annotate(queryData);

    List<AnnotationGraph> asAOM = LegacyGraphConverter.convertToAOM(result);

    URI uri = URI.createFileURI("/tmp/annissalt");
    result.saveSaltProject_DOT(uri);


    //		out.println("Returned " + graphs.size() + " annotations graphs.");
    // FIXME: annotations graphen visualisieren
//    printAsTable(graphs, "nodes", "edges");
  }

  public void doCorpus(String list)
  {
    corpusList = new LinkedList<Long>();
    String[] splits = StringUtils.split(list, " ");
    for (String split : splits)
    {
      try
      {
        corpusList.add(Long.parseLong(split));
      }
      catch (NumberFormatException e)
      {
        // check if there is a corpus with this name
        LinkedList<String> splitList = new LinkedList<String>();
        splitList.add(split);
        corpusList.addAll(annisDao.listCorpusByName(splitList));
      }
    }

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

  public void doAnnotations(String doListValues)
  {
    boolean listValues = "values".equals(doListValues);
    List<AnnisAttribute> annotations =
      annisDao.listAnnotations(getCorpusList(), listValues, true);
    AnnisAttributeSetImpl set = new AnnisAttributeSetImpl(annotations);
    System.out.println(set.getJSON());
  }

  public void doMeta(String corpusId)
  {
    List<Annotation> corpusAnnotations = annisDao.listCorpusAnnotations(Long.
      parseLong(corpusId));
    printAsTable(corpusAnnotations, "namespace", "name", "value");
  }

  public void doText(String textID)
  {
    List<SaltProject> result = new LinkedList<SaltProject>();
    SaltProject p = annisDao.retrieveAnnotationGraph(Long.parseLong(textID));
    result.add(p);
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

    if (strAQL.equals(strDDD))
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
    queryData.setCorpusList(corpusList);
    queryData.setDocuments(metaDataFilter.getDocumentsForMetadata(queryData));

    String sql = findSqlGenerator.toSql(queryData);

    // extract WHERE clause

    Matcher mWhere = Pattern.compile("WHERE\n").matcher(sql);
    if (mWhere.find())
    {
      String whereClause = sql.substring(mWhere.end());
      //out.println("WHERE clause:\n" + whereClause);

      for (String table : result.keySet())
      {
        Set<String> attr = result.get(table);
        Matcher mFacts = Pattern.compile(table + "[0-9]+\\.([a-zA-Z0-9_]+)").
          matcher(whereClause);
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
    if (isDDDQueryMode)
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

  public QueryAnalysis getQueryAnalysis()
  {
    return queryAnalysis;
  }

  public void setQueryAnalysis(QueryAnalysis queryAnalysis)
  {
    this.queryAnalysis = queryAnalysis;
  }

  public SqlGenerator<QueryData, Integer> getCountSqlGenerator()
  {
    return countSqlGenerator;
  }

  public void setCountSqlGenerator(
    SqlGenerator<QueryData, Integer> countSqlGenerator)
  {
    this.countSqlGenerator = countSqlGenerator;
  }

  public SqlGenerator<QueryData, List<AnnotationGraph>> getAnnotateSqlGenerator()
  {
    return annotateSqlGenerator;
  }

  public void setAnnotateSqlGenerator(
    SqlGenerator<QueryData, List<AnnotationGraph>> annotateSqlGenerator)
  {
    this.annotateSqlGenerator = annotateSqlGenerator;
  }

  public SqlGenerator<QueryData, List<Match>> getFindSqlGenerator()
  {
    return findSqlGenerator;
  }

  public void setFindSqlGenerator(
    SqlGenerator<QueryData, List<Match>> findSqlGenerator)
  {
    this.findSqlGenerator = findSqlGenerator;
  }

  public SqlGenerator<QueryData, List<AnnotatedMatch>> getMatrixSqlGenerator()
  {
    return matrixSqlGenerator;
  }

  public void setMatrixSqlGenerator(
    SqlGenerator<QueryData, List<AnnotatedMatch>> matrixSqlGenerator)
  {
    this.matrixSqlGenerator = matrixSqlGenerator;
  }
}
