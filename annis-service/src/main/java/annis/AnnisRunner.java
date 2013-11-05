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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annis.dao.AnnisDao;
import annis.dao.objects.AnnotatedMatch;
import annis.dao.MetaDataFilter;
import annis.model.Annotation;
import annis.model.QueryAnnotation;
import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import annis.service.objects.AnnisAttribute;
import annis.service.objects.AnnisCorpus;
import annis.service.objects.Match;
import annis.service.objects.SaltURIGroup;
import annis.service.objects.SaltURIGroupSet;
import annis.sqlgen.AnnotateSqlGenerator;
import annis.sqlgen.FrequencySqlGenerator;
import annis.sqlgen.extensions.LimitOffsetQueryData;
import annis.sqlgen.SqlGenerator;
import annis.sqlgen.extensions.AnnotateQueryData;
import annis.utils.Utils;
import au.com.bytecode.opencsv.CSVWriter;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import annis.dao.autogenqueries.QueriesGenerator;
import annis.ql.parser.AnnisParserAntlr;
import annis.service.objects.SubgraphFilter;


// TODO: test AnnisRunner
public class AnnisRunner extends AnnisBaseRunner
{

  // logging
  private static final Logger log = LoggerFactory.getLogger(AnnisRunner.class);
  // SQL generators for query functions

  private SqlGenerator<QueryData, List<Match>> findSqlGenerator;

  private SqlGenerator<QueryData, Integer> countSqlGenerator;

  private AnnotateSqlGenerator<SaltProject> annotateSqlGenerator;

  private SqlGenerator<QueryData, List<AnnotatedMatch>> matrixSqlGenerator;

  private AnnotateSqlGenerator<?> graphSqlGenerator;
  private FrequencySqlGenerator frequencySqlGenerator;
  // dependencies

  private AnnisDao annisDao;

  private int context;

  private AnnisParserAntlr annisParser;
  
  private int matchLimit;

  private QueriesGenerator queriesGenerator;
  // settings

  private int limit = 10;

  private int offset;

  private int left = 5;

  private int right = 5;

  private String segmentationLayer = null;
  
  private SubgraphFilter filter = SubgraphFilter.All;

  private List<Long> corpusList;

  private boolean clearCaches;

  private MetaDataFilter metaDataFilter;

  /**
   * @return the graphSqlGenerator
   */
  public AnnotateSqlGenerator<?> getGraphSqlGenerator()
  {
    return graphSqlGenerator;
  }

  /**
   * @param graphSqlGenerator the graphSqlGenerator to set
   */
  public void setGraphSqlGenerator(AnnotateSqlGenerator<?> graphSqlGenerator)
  {
    this.graphSqlGenerator = graphSqlGenerator;
  }

  /**
   * @return the queriesGenerator
   */
  public QueriesGenerator getQueriesGenerator()
  {
    return queriesGenerator;
  }

  /**
   * @param queriesGenerator the queriesGenerator to set
   */
  public void setQueriesGenerator(
    QueriesGenerator queriesGenerator)
  {
    this.queriesGenerator = queriesGenerator;
  }

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

    private final List<Long> values = Collections.synchronizedList(
      new ArrayList<Long>());

    public Benchmark(String functionCall, QueryData queryData)
    {
      super();
      this.functionCall = functionCall;
      this.queryData = queryData;
    }

    public long getMedian()
    {
      synchronized (values)
      {
        // sort list
        Collections.sort(values);
        // get item in the middle
        int pos = Math.round((float) values.size() / 2.0f);
        if (pos >= 0 && pos < values.size())
        {
          return values.get(pos);
        }
      }

      // indicate an error
      return -1;
    }
  }
  private List<AnnisRunner.Benchmark> benchmarks;

  private static final int SEQUENTIAL_RUNS = 5;

  public static void main(String[] args)
  {
    // get runner from Spring
    String path = Utils.getAnnisFile(
      "conf/spring/Shell.xml").getAbsolutePath();
    AnnisBaseRunner.getInstance("annisRunner", "file:" + path).run(args);
  }

  public AnnisRunner()
  {
    corpusList = new LinkedList<Long>();
    benchmarks = new ArrayList<AnnisRunner.Benchmark>();
  }

  ///// Commands
  public void doBenchmarkFile(String filename)
  {
    try
    {
      BufferedReader reader = new BufferedReader(new InputStreamReader(
        new FileInputStream(filename), "UTF-8"));
      try
      {
        Map<String, Integer> queryRun = new HashMap<String, Integer>();
        Map<String, Integer> queryId = new HashMap<String, Integer>();
        int maxId = 0;
        for (String line = reader.readLine(); line != null; line = reader.
          readLine())
        {
          // get the id of this query
          if (!queryId.containsKey(line))
          {
            ++maxId;
            queryId.put(line, maxId);
          }
          int id = queryId.get(line);
          // get the repetition of this query
          if (!queryRun.containsKey(line))
          {
            queryRun.put(line, 0);
          }
          int run = queryRun.get(line) + 1;
          queryRun.put(line, run);
          String[] split = line.split(" ", 2);
          String queryFunction = split[0];
          String annisQuery = split[1];
          boolean error = false;
          QueryData queryData = null;
          try
          {
            queryData = analyzeQuery(annisQuery, queryFunction);
          }
          catch (RuntimeException e)
          {
            error = true;
          }
          if ("count".equals(queryFunction))
          {
            long start = new Date().getTime();
            if (!error)
            {
              try
              {
                int result = annisDao.count(queryData);
                long end = new Date().getTime();
                long runtime = end - start;
                Object[] output =
                {
                  queryFunction, annisQuery, result, runtime, id, run
                };
                System.out.println(StringUtils.join(output, "\t"));
              }
              catch (RuntimeException e)
              {
                error = true;
              }
            }
            if (error)
            {
              String result = "ERROR";
              long end = new Date().getTime();
              long runtime = end - start;
              Object[] output =
              {
                queryFunction, annisQuery, result, runtime, id, run
              };
              System.out.println(StringUtils.join(output, "\t"));
            }
          }
        }
      }
      finally
      {
        if (reader != null)
        {
          reader.close();
        }
      }
    }
    catch (IOException e)
    {
      error(e);
    }
  }

  public void doDebug(String ignore)
  {
    doCorpus("pcc2");
    doCount("\"das\" & ( x#\"Haus\" | x#\"Schaf\") & #1 . #x");
  }

  public void doParse(String annisQuery)
  {
    out.println(annisParser.dumpTree(annisQuery));
  }

  /**
   * Clears all example queries.
   *
   */
  public void doClearExampleQueries(String unused)
  {
    for (Long corpusId : corpusList)
    {
      System.out.println("delete example queries for " + corpusId);
      queriesGenerator.delExampleQueries(corpusId);
    }
  }

  /**
   * Enables the auto generating of example queries for the annis shell.
   *
   * @param args If args is not set the new example queries are added to the old
   * ones. Supported value is <code>overwrite</code>, wich generates new example
   * queries and delete the old ones.
   *
   *
   */
  public void doGenerateExampleQueries(String args)
  {
    Boolean del = false;
    if (args != null && "overwrite".equals(args))
    {
      del = true;
    }

    if (corpusList != null)
    {
      for (Long corpusId : corpusList)
      {
        System.out.println("generate example queries " + corpusId);
        queriesGenerator.generateQueries(corpusId, del);
      }
    }
  }

  // FIXME: missing tests
  public void doSql(String funcCall)
  {

    String doSqlFunctionName = "sql_" + funcCall.split("\\s", 2)[0];
    SqlGenerator<QueryData, ?> gen = getGeneratorForQueryFunction(funcCall);
    String annisQuery = getAnnisQueryFromFunctionCall(funcCall);
    QueryData queryData = analyzeQuery(annisQuery, doSqlFunctionName);


    out.println("NOTICE: left = " + left + "; right = " + right + "; limit = "
      + limit + "; offset = " + offset);

    out.println(gen.toSql(queryData));
  }

  public void doExplain(String functionCall, boolean analyze)
  {
    SqlGenerator<QueryData, ?> generator = getGeneratorForQueryFunction(
      functionCall);
    String function = getAnnisQueryFromFunctionCall(functionCall);
    String annisQuery = getAnnisQueryFromFunctionCall(functionCall);
    QueryData queryData = analyzeQuery(annisQuery, function);
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
    String funcCall)
  {
    String[] split = funcCall.split(" ", 2);

    Validate.isTrue(split.length == 2, "bad call to plan");
    String function = split[0];

    SqlGenerator<QueryData, ?> generator = null;
    if ("count".equals(function))
    {
      generator = countSqlGenerator;
    }
    else if ("find".equals(function))
    {
      generator = findSqlGenerator;
    }
    else if ("annotate".equals(function))
    {
      generator = annotateSqlGenerator;
    }
    else if ("matrix".equals(function))
    {
      generator = matrixSqlGenerator;
    }
    else if ("subgraph".equals(function))
    {
      generator = getGraphSqlGenerator();
    }
    else if("frequency".equals(function))
    {
      generator = frequencySqlGenerator;
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
      benchmark.sql = getGeneratorForQueryFunction(benchmark.functionCall).
        toSql(
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
        boolean error = false;
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
        benchmark.values.add(runtime);
        benchmark.bestTimeInMilliseconds =
          Math.min(benchmark.bestTimeInMilliseconds, runtime);
        benchmark.worstTimeInMilliseconds =
          Math.max(benchmark.worstTimeInMilliseconds, runtime);
        ++benchmark.runs;
        if (error)
        {
          ++benchmark.errors;
        }

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
      benchmark.values.add(runtime);
      benchmark.bestTimeInMilliseconds =
        Math.min(benchmark.bestTimeInMilliseconds, runtime);
      benchmark.worstTimeInMilliseconds =
        Math.max(benchmark.worstTimeInMilliseconds, runtime);

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
      benchmark.avgTimeInMilliseconds = Math.round(
        (double) benchmark.avgTimeInMilliseconds
        / (double) benchmark.runs);
      String options = benchmarkOptions(benchmark.queryData);
      out.println(benchmark.getMedian() + " ms (median for "
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
      out.println(benchmark.worstTimeInMilliseconds + " ms "
        + (benchmark.errors > 0 ? "("
        + benchmark.errors + " errors)" : "") + " for '"
        + benchmark.functionCall + ("".equals(options) ? "'" : "' with "
        + options));
    }

    // show best runtime for each query
    out.println();
    out.println("---> best times");
    for (AnnisRunner.Benchmark benchmark : benchmarks)
    {
      String options = benchmarkOptions(benchmark.queryData);
      out.println(benchmark.bestTimeInMilliseconds + " ms "
        + (benchmark.errors > 0 ? "("
        + benchmark.errors + " errors)" : "") + " for '"
        + benchmark.functionCall + ("".equals(options) ? "'" : "' with "
        + options));
    }
    out.println();

    // CSV output
    try
    {
      CSVWriter csv = new CSVWriter(new FileWriterWithEncoding(new File(
        "annis_benchmark_result.csv"), "UTF-8"));

      String[] header = new String[]
      {
        "corpora", "query", "median", "diff-best", "diff-worst"
      };
      csv.writeNext(header);
      for (AnnisRunner.Benchmark benchmark : benchmarks)
      {
        long median = benchmark.getMedian();

        String[] line = new String[5];
        line[0] = StringUtils.join(benchmark.queryData.getCorpusList(), ",");
        line[1] = benchmark.functionCall;
        line[2] = "" + median;
        line[3] = "" + Math.abs(benchmark.bestTimeInMilliseconds - median);
        line[4] = "" + Math.abs(median - benchmark.worstTimeInMilliseconds);
        csv.writeNext(line);
      }

      csv.close();

    }
    catch (IOException ex)
    {
      log.error(null, ex);
    }

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
        Writer w = null;
        try
        {
          log.info("resetting caches");
          log.debug("syncing");
          Runtime.getRuntime().exec("sync").waitFor();
          File dropCaches = new File("/proc/sys/vm/drop_caches");
          if (dropCaches.canWrite())
          {
            log.debug("clearing file system cache");
            w = new FileWriterWithEncoding(dropCaches, "UTF-8");
            w.write("3");
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
        catch (IOException ex)
        {
          log.error(null, ex);
        }
        catch (InterruptedException ex)
        {
          log.error(null, ex);
        }
        finally
        {
          if (w != null)
          {
            try
            {
              w.close();
            }
            catch (IOException ex1)
            {
              log.error(null, ex1);
            }
          }
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
      Validate.isTrue(split.length == 3 && "TO".toLowerCase().equals(split[1]),
        "syntax error: set "
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
    else if ("seg".equals(setting))
    {

      if (show)
      {
        value = segmentationLayer;
      }
      else
      {
        segmentationLayer = value;
      }
    }
    else if("filter".equals(setting))
    {
      if(show)
      {
        value = filter.name();
      }
      else
      {
        filter = SubgraphFilter.valueOf(value);
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

  /**
   * Does the setup for the QueryData object.
   *
   * If the query function is "subgraph" or "sql_subgraph" the annisQuery string
   * should contain space separated salt ids. In this case the annisQuery is not
   * parsed and the {@link QueryData#getAlternatives()} method should return a
   * List with dummy QueryNode entries. Instead of parsing the annisQuery it
   * extracts the salt ids and put it into the extension's of {@link QueryData}.
   *
   * @param annisQuery should include a valid annis query
   * @param queryFunction should include a method name of {@link AnnisRunner}
   * which starts with do.
   * @return {@link QueryData} object, which contains a parsed annis query, the
   * default context {@link AnnisRunner#left} and {@link AnnisRunner#left}
   * values and the default {@link AnnisRunner#limit} and
   * {@link AnnisRunner#offset} values
   */
  private QueryData analyzeQuery(String annisQuery, String queryFunction)
  {

    QueryData queryData;
    log.debug("analyze query for " + queryFunction + " function");


    if (queryFunction != null && !queryFunction.matches("(sql_)?subgraph"))
    {
      queryData = annisDao.parseAQL(annisQuery, corpusList);
    }
    else
    {
      queryData = extractSaltIds(annisQuery);
    }

    queryData.setCorpusConfiguration(annisDao.getCorpusConfiguration());

    // filter by meta data
    queryData.setDocuments(metaDataFilter.getDocumentsForMetadata(queryData));


    if (queryFunction != null && queryFunction.matches("(sql_)?(annotate|find)"))
    {
      queryData.addExtension(new AnnotateQueryData(left, right,
        segmentationLayer, filter));
      queryData.addExtension(new LimitOffsetQueryData(offset, limit));
    }
    else if (queryFunction != null && queryFunction.matches("(sql_)?subgraph"))
    {
      queryData.addExtension(new AnnotateQueryData(left, right,
        segmentationLayer, filter));
    }


    if (annisQuery != null)
    {
      benchmarks.add(new AnnisRunner.Benchmark(queryFunction + " " + annisQuery,
        queryData));
    }
    // printing of NOTICE conflicts with benchmarkFile
    // out.println("NOTICE: corpus = " + queryData.getCorpusList());

    return queryData;
  }

  public void doCount(String annisQuery)
  {
    out.println(annisDao.count(analyzeQuery(annisQuery, "count")));
  }

  public void doMatrix(String annisQuery)
  {
//    List<AnnotatedMatch> matches = annisDao.matrix(analyzeQuery(annisQuery,
//      "matrix"));
    annisDao.matrix(analyzeQuery(annisQuery, "matrix"), System.out);
//    if (matches.isEmpty())
//    {
//      out.println("(empty");
//    }
//    else
//    {
//      WekaHelper.exportAsArff(matches, out);
//      out.println();
//    }
  }

  public void doFind(String annisQuery)
  {
    List<Match> matches = annisDao.find(analyzeQuery(annisQuery, "find"));
    JAXBContext jc = null;
    try
    {
      jc = JAXBContext.newInstance(annis.service.objects.Match.class);
    }
    catch (JAXBException ex)
    {
      log.error("Problems with writing XML", ex);
    }


    for (int i = 0; i < matches.size(); i++)
    {

      try
      {

        jc.createMarshaller().marshal(matches.get(i), out);
      }
      catch (JAXBException ex)
      {
        log.error("Problems with writing XML", ex);
      }

      out.println();
    }
  }

  public void doSubgraph(String saltIds)
  {
    QueryData queryData = analyzeQuery(saltIds, "subgraph");

    out.println("NOTICE: left = " + left + "; right = " + right + "; limit = "
      + limit + "; offset = " + offset + "; filter = " + filter.name());

    SaltProject result = annisDao.graph(queryData);

    // write result to File
    URI path = URI.createFileURI("/tmp/annissalt");
    result.saveSaltProject_DOT(path);
    System.out.println("graph as dot written to /tmp/annissalt");
  }

  public void doAnnotate(String annisQuery)
  {
    QueryData queryData = analyzeQuery(annisQuery, "annotate");

    out.println("NOTICE: left = " + left + "; right = " + right + "; limit = "
      + limit + "; offset = " + offset);
    SaltProject result = annisDao.annotate(queryData);

    URI uri = URI.createFileURI("/tmp/annissalt");
    result.saveSaltProject_DOT(uri);
    System.out.println("graph as dot written to /tmp/annissalt");
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
        corpusList.addAll(annisDao.mapCorpusNamesToIds(splitList));
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
    try
    {
      ObjectMapper om = new ObjectMapper();
      AnnotationIntrospector ai = new JaxbAnnotationIntrospector();
      DeserializationConfig config = om.getDeserializationConfig().
        withAnnotationIntrospector(ai);
      om.setDeserializationConfig(config);
      om.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);

      System.out.println(om.writeValueAsString(annotations));
    }
    catch (IOException ex)
    {
      log.error("problems with writing result", ex);
    }

  }

  public void doMeta(String corpusId)
  {
    LinkedList<Long> corpusIdAsList = new LinkedList<Long>();
    try
    {
      corpusIdAsList.add(Long.parseLong(corpusId));
      List<String> toplevelNames = annisDao.mapCorpusIdsToNames(corpusIdAsList);

      List<Annotation> corpusAnnotations =
        annisDao.listCorpusAnnotations(toplevelNames.get(0));
      printAsTable(corpusAnnotations, "namespace", "name", "value");
    }
    catch (NumberFormatException ex)
    {
      System.out.print("not a number: " + corpusId);
    }

  }

  public void doSqlDoc(String docCall)
  {
    String[] splitted = docCall.split("( )+");

    Validate.isTrue(splitted.length > 1,
      "must have to arguments (toplevel corpus name and document name");
    System.out.println(annotateSqlGenerator.getDocumentQuery(splitted[0],
      splitted[1]));
  }

  public void doDoc(String docCall)
  {
    String[] splitted = docCall.split("( )+");

    Validate.isTrue(splitted.length > 1,
      "must have to arguments (toplevel corpus name and document name");
    SaltProject p = annisDao.retrieveAnnotationGraph(splitted[0], splitted[1]);
    System.out.println(printSaltAsXMI(p));
  }

  public void doQuit(String dummy)
  {
    System.out.println("bye bye!");
    System.exit(0);
  }

  private void printAsTable(List<? extends Object> list, String... fields)
  {
    out.println(new TableFormatter().formatAsTable(list, fields));
  }

  private String printSaltAsXMI(SaltProject project)
  {
    try
    {
      Resource resource = new XMIResourceImpl();
      // add the project itself
      resource.getContents().add(project);

      // add all SDocumentGraph elements
      for (SCorpusGraph corpusGraph : project.getSCorpusGraphs())
      {
        for (SDocument doc : corpusGraph.getSDocuments())
        {
          if (doc.getSDocumentGraph() != null)
          {
            resource.getContents().add(doc.getSDocumentGraph());
          }
        }
      }
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      resource.save(outStream, null);
      return new String(outStream.toByteArray(), "UTF-8");

    }
    catch (IOException ex)
    {
      log.error(null, ex);
    }
    return "";
  }

  public AnnisParserAntlr getAnnisParser()
  {
    return annisParser;
  }

  public void setAnnisParser(AnnisParserAntlr annisParser)
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

  public List<Long> getCorpusList()
  {
    return corpusList;
  }

  public void setCorpusList(List<Long> corpusList)
  {
    this.corpusList = corpusList;
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

  public SqlGenerator<QueryData, Integer> getCountSqlGenerator()
  {
    return countSqlGenerator;
  }

  public void setCountSqlGenerator(
    SqlGenerator<QueryData, Integer> countSqlGenerator)
  {
    this.countSqlGenerator = countSqlGenerator;
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

  public MetaDataFilter getMetaDataFilter()
  {
    return metaDataFilter;
  }

  public void setMetaDataFilter(MetaDataFilter metaDataFilter)
  {
    this.metaDataFilter = metaDataFilter;
  }

  public FrequencySqlGenerator getFrequencySqlGenerator()
  {
    return frequencySqlGenerator;
  }

  public void setFrequencySqlGenerator(FrequencySqlGenerator frequencySqlGenerator)
  {
    this.frequencySqlGenerator = frequencySqlGenerator;
  }
  
  

  private QueryData extractSaltIds(String param)
  {
    QueryData queryData = new QueryData();
    SaltURIGroupSet saltIDs = new SaltURIGroupSet();

    Set<String> corpusNames = new TreeSet<String>();

    int i = 0;
    for (String group : param.split("\\s*;\\s*"))
    {
      SaltURIGroup urisForGroup = new SaltURIGroup();

      for (String id : group.split("[,\\s]+"))
      {
        java.net.URI uri;
        try
        {
          uri = new java.net.URI(id);

          if (!"salt".equals(uri.getScheme()) || uri.getFragment() == null)
          {
            throw new URISyntaxException("not a salt id", uri.toString());
          }
        }
        catch (URISyntaxException ex)
        {
          log.error(null, ex);
          continue;
        }
        urisForGroup.getUris().add(uri);
      }

      // collect list of used corpora and created pseudo QueryNodes for each URI
      List<QueryNode> pseudoNodes = new ArrayList<QueryNode>(urisForGroup.
        getUris().size());
      for (java.net.URI u : urisForGroup.getUris())
      {
        pseudoNodes.add(new QueryNode());
        corpusNames.add(CommonHelper.getCorpusPath(u).get(0));
      }
      queryData.addAlternative(pseudoNodes);
      saltIDs.getGroups().put(++i, urisForGroup);
    }
    List<Long> corpusIDs = annisDao.mapCorpusNamesToIds(new LinkedList<String>(
      corpusNames));

    queryData.setCorpusList(corpusIDs);

    log.debug(saltIDs.toString());
    queryData.addExtension(saltIDs);
    return queryData;
  }
}
