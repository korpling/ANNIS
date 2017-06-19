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

import annis.dao.QueryDao;
import annis.dao.MetaDataFilter;
import annis.dao.autogenqueries.QueriesGenerator;
import annis.dao.objects.AnnotatedMatch;
import annis.model.Annotation;
import annis.model.QueryAnnotation;
import annis.ql.parser.AnnisParserAntlr;
import annis.ql.parser.QueryData;
import annis.service.objects.AnnisAttribute;
import annis.service.objects.AnnisCorpus;
import annis.service.objects.FrequencyTable;
import annis.service.objects.FrequencyTableQuery;
import annis.service.objects.Match;
import annis.service.objects.MatchAndDocumentCount;
import annis.service.objects.MatchGroup;
import annis.service.objects.OrderType;
import annis.service.objects.SubgraphFilter;
import annis.sqlgen.AnnotateSqlGenerator;
import annis.sqlgen.FrequencySqlGenerator;
import annis.sqlgen.SqlGenerator;
import annis.sqlgen.SqlGeneratorAndExtractor;
import annis.sqlgen.extensions.AnnotateQueryData;
import annis.sqlgen.extensions.LimitOffsetQueryData;
import annis.utils.Utils;
import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.util.SaltUtil;
import org.eclipse.emf.common.util.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: test AnnisRunner
public class AnnisRunner extends AnnisBaseRunner
{

  // logging
  private static final Logger log = LoggerFactory.getLogger(AnnisRunner.class);
  // SQL generators for query functions

  private SqlGeneratorAndExtractor<QueryData, List<Match>> findSqlGenerator;

  private SqlGeneratorAndExtractor<QueryData, Integer> countSqlGenerator;

  private SqlGeneratorAndExtractor<QueryData, List<AnnotatedMatch>> matrixSqlGenerator;

  private AnnotateSqlGenerator<?> graphSqlGenerator;
  private FrequencySqlGenerator frequencySqlGenerator;
  // dependencies

  private QueryDao queryDao;

  private int context;

  private AnnisParserAntlr annisParser;

  private int matchLimit;

  private QueriesGenerator queriesGenerator;
  // settings

  private int limit = 10;

  private int offset;

  private int left = 5;

  private int right = 5;

  private OrderType order = OrderType.ascending;

  private String segmentationLayer = null;

  private SubgraphFilter filter = SubgraphFilter.all;

  private FrequencyTableQuery frequencyDef = null;

  private List<Long> corpusList;

  private boolean clearCaches;

  public enum BenchmarkMode
  {
    warmup_random,
    sequential_random
  }

  private BenchmarkMode benchMode = BenchmarkMode.warmup_random;

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

    private String name;
    private String functionCall;

    private QueryData queryData;

    private long sumTimeInMilliseconds;

    private long bestTimeInMilliseconds;

    private long worstTimeInMilliseconds;

    private String sql;

    private String plan;

    private int runs;

    private int errors;

    private Integer count;

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
  private String benchmarkName = null;
  private final ArrayList<Benchmark> benchmarks;

  private static final int SEQUENTIAL_RUNS = 5;

  public static void main(String[] args)
  {
    // get runner from Spring
    try
    {
      String path = Utils.getAnnisFile(
        "conf/spring/Shell.xml").getAbsolutePath();
      AnnisBaseRunner.getInstance("annisRunner", "file:" + path).run(args);
    }
    catch (AnnisRunnerException ex)
    {
      log.error(ex.getMessage() + " (error code " + ex.getExitCode() + ")", ex);
      System.exit(ex.getExitCode());
    }
    catch (Throwable ex)
    {
      log.error(ex.getMessage(), ex);
      System.exit(1);
    }
  }

  public AnnisRunner()
  {
    corpusList = new LinkedList<>();
    benchmarks = new ArrayList<>();
  }

  ///// Commands
  public void doBenchmarkFile(String filename)
  {
    try
    {

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(
        new FileInputStream(filename), "UTF-8"));)
      {
        Map<String, Integer> queryRun = new HashMap<>();
        Map<String, Integer> queryId = new HashMap<>();
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
                int result = queryDao.count(queryData);
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
    }
    catch (IOException e)
    {
      error(e);
    }
  }

  public void doDebug(String ignore)
  {
    doDoc("NoSta-D-Kafka NoSta-D-Kafka");
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
    SqlGenerator<QueryData> gen = getGeneratorForQueryFunction(funcCall);
    String annisQuery = getAnnisQueryFromFunctionCall(funcCall);
    QueryData queryData = analyzeQuery(annisQuery, doSqlFunctionName);

    out.println("NOTICE: left = " + left + "; right = " + right + "; limit = "
      + limit + "; offset = " + offset);

    out.println(gen.toSql(queryData));
  }

  public void doExplain(String functionCall, boolean analyze)
  {
    SqlGenerator<QueryData> generator = getGeneratorForQueryFunction(
      functionCall);
    String function = getAnnisQueryFromFunctionCall(functionCall);
    String annisQuery = getAnnisQueryFromFunctionCall(functionCall);
    QueryData queryData = analyzeQuery(annisQuery, function);
    out.println("NOTICE: left = " + left + "; right = " + right + "; limit = "
      + limit + "; offset = " + offset);

    out.println(queryDao.explain(generator, queryData, analyze));
  }

  public void doPlan(String functionCall)
  {
    doExplain(functionCall, false);
  }

  public void doAnalyze(String functionCall)
  {
    doExplain(functionCall, true);
  }

  private SqlGeneratorAndExtractor<QueryData, ?> getGeneratorForQueryFunction(
    String funcCall)
  {
    String[] split = funcCall.split(" ", 2);

    Validate.isTrue(split.length == 2, "bad call to plan");
    String function = split[0];

    SqlGeneratorAndExtractor<QueryData, ?> generator = null;
    if ("count".equals(function))
    {
      generator = countSqlGenerator;
    }
    else if ("find".equals(function))
    {
      generator = findSqlGenerator;
    }
    else if ("matrix".equals(function))
    {
      generator = matrixSqlGenerator;
    }
    else if ("subgraph".equals(function))
    {
      generator = getGraphSqlGenerator();
    }
    else if ("frequency".equals(function))
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
    benchmarkName = null;
  }

  public void doBenchmarkName(String name)
  {
    this.benchmarkName = name;
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

    List<AnnisRunner.Benchmark> session = new ArrayList<>();

    // create sql + plan for each query and create count copies for each benchmark
    for (Benchmark benchmark : benchmarks)
    {
      if (clearCaches)
      {
        resetCaches(currentOS);
      }

      SqlGeneratorAndExtractor<QueryData, ?> generator
        = getGeneratorForQueryFunction(benchmark.functionCall);
      benchmark.sql = getGeneratorForQueryFunction(benchmark.functionCall).
        toSql(
          benchmark.queryData);
      out.println("---> SQL query for: " + benchmark.functionCall);
      out.println(benchmark.sql);
      try
      {
        benchmark.plan = queryDao.explain(generator, benchmark.queryData, false);
        out.println("---> query plan for: " + benchmark.functionCall);
        out.println(benchmark.plan);
      }
      catch (RuntimeException ex)
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
          queryDao.executeQueryFunction(benchmark.queryData, generator);
        }
        catch (RuntimeException ex)
        {
          error = true;
        }
        long end = new Date().getTime();
        long runtime = end - start;

        if (benchMode == BenchmarkMode.sequential_random)
        {
          // record the runtime and other benchmark values when the sequental run is counted
          benchmark.sumTimeInMilliseconds += runtime;
          benchmark.values.add(runtime);
          benchmark.bestTimeInMilliseconds
            = Math.min(benchmark.bestTimeInMilliseconds, runtime);
          benchmark.worstTimeInMilliseconds
            = Math.max(benchmark.worstTimeInMilliseconds, runtime);

          ++benchmark.runs;

          if (error)
          {
            ++benchmark.errors;
          }
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

    // execute the random query order, record test times
    for (AnnisRunner.Benchmark benchmark : session)
    {
      if (benchmark.errors >= 3)
      {
        continue;
      }
      boolean error = false;
      SqlGeneratorAndExtractor<QueryData, ?> generator
        = getGeneratorForQueryFunction(benchmark.functionCall);
      long start = new Date().getTime();
      try
      {
        queryDao.executeQueryFunction(benchmark.queryData, generator);
      }
      catch (RuntimeException e)
      {
        error = true;
      }
      long end = new Date().getTime();
      long runtime = end - start;

      benchmark.sumTimeInMilliseconds += runtime;
      benchmark.values.add(runtime);
      benchmark.bestTimeInMilliseconds
        = Math.min(benchmark.bestTimeInMilliseconds, runtime);
      benchmark.worstTimeInMilliseconds
        = Math.max(benchmark.worstTimeInMilliseconds, runtime);

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
    try (CSVWriter csv = new CSVWriter(new FileWriterWithEncoding(new File(
      "annis_benchmark_result.csv"), "UTF-8"));)
    {

      String[] header = new String[]
      {
        "corpora", "query", "median", "diff-best", "diff-worst", "mean"
      };
      csv.writeNext(header);
      for (AnnisRunner.Benchmark benchmark : benchmarks)
      {
        double mean = (double) benchmark.sumTimeInMilliseconds / (double) benchmark.runs;
        long median = benchmark.getMedian();

        String[] line = new String[6];
        line[0] = StringUtils.join(benchmark.queryData.getCorpusList(), ",");
        line[1] = benchmark.functionCall;
        line[2] = "" + median;
        line[3] = "" + Math.abs(benchmark.bestTimeInMilliseconds - median);
        line[4] = "" + Math.abs(median - benchmark.worstTimeInMilliseconds);
        line[5] = "" + mean;
        csv.writeNext(line);
      }
    }
    catch (IOException ex)
    {
      log.error(null, ex);
    }

    // property output format for Jenkins Plot plugin
    try
    {
      File outputDir = new File("annis_benchmark_results");
      if (outputDir.isDirectory() || outputDir.mkdirs())
      {
        int i = 1;
        for (AnnisRunner.Benchmark b : benchmarks)
        {
          Properties props = new Properties();
          props.put("YVALUE", "" + b.getMedian());
          try (FileWriterWithEncoding writer
            = new FileWriterWithEncoding(new File(outputDir, i + ".properties"), "UTF-8"))
          {
            props.store(writer, "");
          }

          i++;

          // also write out a "time" and "count" file which can be used by the ANNIS4 prototype
          if (b.name != null)
          {
            double mean = (double) b.sumTimeInMilliseconds / (double) b.runs;
            Files.write("" + mean, new File(outputDir, b.name + ".time"), StandardCharsets.UTF_8);
            if (b.count != null)
            {
              Files.write("" + b.count, new File(outputDir, b.name + ".count"), StandardCharsets.UTF_8);
            }
          }
        }
      }
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
    List<String> fields = new ArrayList<>();
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
            try (Writer w = new FileWriterWithEncoding(dropCaches, "UTF-8");)
            {
              w.write("3");
            }
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
        catch (IOException | InterruptedException ex)
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
    else if ("order".equals(setting))
    {
      if (show)
      {
        value = order.toString();
      }
      else
      {
        order = OrderType.valueOf(value);
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
        value = String.valueOf(queryDao.getTimeout());
      }
      else
      {
        queryDao.setTimeout(Integer.parseInt(value));
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
    else if ("bench-mode".equals(setting))
    {
      if (show)
      {
        value = benchMode.name();
      }
      else
      {
        try
        {
          benchMode = BenchmarkMode.valueOf(value);
        }
        catch (IllegalArgumentException ex)
        {
          out.println("Invalid value, allowed values are: " + Joiner.on(", ").join(BenchmarkMode.values()));
        }
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
    else if ("filter".equals(setting))
    {
      if (show)
      {
        value = filter.name();
      }
      else
      {
        filter = SubgraphFilter.valueOf(value);
      }
    }
    else if ("freq-def".equals(setting))
    {
      if (show)
      {
        value = (frequencyDef == null ? "<not set>" : frequencyDef.toString());
      }
      else
      {
        frequencyDef = FrequencyTableQuery.parse(value);
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

  public void doVersion(String ignore)
  {
    out.println(VersionInfo.getVersion());
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
      queryData = queryDao.parseAQL(annisQuery, corpusList);
    }
    else
    {
      queryData = GraphHelper.createQueryData(MatchGroup.parseString(annisQuery), queryDao);
    }

    queryData.setCorpusConfiguration(queryDao.getCorpusConfiguration());

    // filter by meta data
    queryData.setDocuments(metaDataFilter.getDocumentsForMetadata(queryData));

    if (queryFunction != null && queryFunction.matches("(sql_)?(annotate|find)"))
    {
      queryData.addExtension(new AnnotateQueryData(left, right,
        segmentationLayer, filter));
      queryData.addExtension(new LimitOffsetQueryData(offset, limit, order));
    }
    else if (queryFunction != null && queryFunction.matches("(sql_)?subgraph"))
    {
      queryData.addExtension(new AnnotateQueryData(left, right,
        segmentationLayer, filter));
    }
    else if (queryFunction != null && queryFunction.matches("(sql_)?frequency"))
    {
      if (frequencyDef == null)
      {
        out.println("You have to set the 'freq-def' property first");
      }
      else
      {
        queryData.addExtension(frequencyDef);
      }
    }

    if (annisQuery != null)
    {
      if (benchmarkName == null)
      {
        benchmarkName = "auto_" + benchmarks.size();
      }
      Benchmark b = new AnnisRunner.Benchmark(queryFunction + " " + annisQuery,
        queryData);
      b.name = benchmarkName;
      benchmarks.add(b);
      benchmarkName = null;
    }
    // printing of NOTICE conflicts with benchmarkFile
    // out.println("NOTICE: corpus = " + queryData.getCorpusList());

    return queryData;
  }

  public void doCount(String annisQuery)
  {
    MatchAndDocumentCount count = queryDao.countMatchesAndDocuments(analyzeQuery(annisQuery, "count"));
    if (!benchmarks.isEmpty())
    {
      Benchmark lastBench = benchmarks.get(benchmarks.size() - 1);
      lastBench.count = count.getMatchCount();
    }
    out.println(count);
  }

  public void doMatrix(String annisQuery)
  {
//    List<AnnotatedMatch> matches = queryDao.matrix(analyzeQuery(annisQuery,
//      "matrix"));
    queryDao.matrix(analyzeQuery(annisQuery, "matrix"), false, System.out);
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
    List<Match> matches = queryDao.find(analyzeQuery(annisQuery, "find"));
    MatchGroup group = new MatchGroup(matches);
    out.println(group.toString());
  }

  public void doFrequency(String definitions)
  {
    FrequencyTable result = queryDao.frequency(analyzeQuery(definitions, "frequency"));
    for (FrequencyTable.Entry e : result.getEntries())
    {
      out.println(e.toString());
    }
  }

  public void doSubgraph(String saltIds)
  {
    QueryData queryData = analyzeQuery(saltIds, "subgraph");

    out.println("NOTICE: left = " + left + "; right = " + right + "; limit = "
      + limit + "; offset = " + offset + "; filter = " + filter.name());

    SaltProject result = queryDao.graph(queryData);

    // write result to File
    URI path = URI.createFileURI("/tmp/annissalt");
    SaltUtil.save_DOT(result, path);
    System.out.println("graph as dot written to /tmp/annissalt");
  }

  public void doCorpus(String list)
  {
    corpusList = new LinkedList<>();
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
        LinkedList<String> splitList = new LinkedList<>();
        splitList.add(split);
        corpusList.addAll(queryDao.mapCorpusNamesToIds(splitList));
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
    List<AnnisCorpus> corpora = queryDao.listCorpora();
    printAsTable(corpora, "id", "name", "textCount", "tokenCount");
  }

  public void doAnnotations(String doListValues)
  {
    boolean listValues = "values".equals(doListValues);
    List<AnnisAttribute> annotations
      = queryDao.listAnnotations(getCorpusList(), listValues, true);
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
    LinkedList<Long> corpusIdAsList = new LinkedList<>();
    try
    {
      corpusIdAsList.add(Long.parseLong(corpusId));
      List<String> toplevelNames = queryDao.mapCorpusIdsToNames(corpusIdAsList);

      List<Annotation> corpusAnnotations
        = queryDao.listCorpusAnnotations(toplevelNames.get(0));
      printAsTable(corpusAnnotations, "namespace", "name", "value");
    }
    catch (NumberFormatException ex)
    {
      System.out.print("not a number: " + corpusId);
    }

  }

  public void doSqlDoc(String docCall)
  {
    List<String> splitted = Splitter.on(' ').trimResults().omitEmptyStrings()
      .splitToList(docCall);

    List<String> annoFilter = null;
    if (splitted.size() > 2)
    {
      annoFilter = Splitter.on(',').trimResults().omitEmptyStrings().splitToList(
        splitted.get(2));
    }

    Validate.isTrue(splitted.size() > 1,
      "must have to arguments (toplevel corpus name and document name");

    long corpusID = queryDao.mapCorpusNameToId(splitted.get(0));
    System.out.println(graphSqlGenerator.getDocumentQuery(
      corpusID,
      splitted.get(1),
      annoFilter));
  }

  public void doDoc(String docCall)
  {
    List<String> splitted = Splitter.on(' ').trimResults().omitEmptyStrings()
      .splitToList(docCall);

    List<String> annoFilter = null;
    if (splitted.size() > 2)
    {
      annoFilter = Splitter.on(',').trimResults().omitEmptyStrings().splitToList(
        splitted.get(2));
    }

    Validate.isTrue(splitted.size() > 1,
      "must have two arguments (toplevel corpus name and document name");
    SaltProject p = queryDao.retrieveAnnotationGraph(splitted.get(0),
      splitted.get(1), annoFilter);
    System.out.println(printSaltAsXMI(p));
  }

  public void doExport(String args)
  {
    List<String> splitted = Splitter.on(' ').trimResults().omitEmptyStrings()
      .limit(2)
      .splitToList(args);
    Validate.isTrue(splitted.size() == 2, "must have two arguments: toplevel corpus name and output directory");

    queryDao.exportCorpus(splitted.get(0), new File(splitted.get(1)));
  }

  public void doQuit(String dummy)
  {
    System.out.println("bye bye!");
    System.exit(0);
  }

  public void doExit(String dummy)
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
    // TODO: actuall transform it
    throw new UnsupportedOperationException("Not implemented yet");

  }

  public AnnisParserAntlr getAnnisParser()
  {
    return annisParser;
  }

  public void setAnnisParser(AnnisParserAntlr annisParser)
  {
    this.annisParser = annisParser;
  }

  public QueryDao getQueryDao()
  {
    return queryDao;
  }

  public void setQueryDao(QueryDao queryDao)
  {
    this.queryDao = queryDao;
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

  public SqlGeneratorAndExtractor<QueryData, Integer> getCountSqlGenerator()
  {
    return countSqlGenerator;
  }

  public void setCountSqlGenerator(
    SqlGeneratorAndExtractor<QueryData, Integer> countSqlGenerator)
  {
    this.countSqlGenerator = countSqlGenerator;
  }

  public SqlGeneratorAndExtractor<QueryData, List<Match>> getFindSqlGenerator()
  {
    return findSqlGenerator;
  }

  public void setFindSqlGenerator(
    SqlGeneratorAndExtractor<QueryData, List<Match>> findSqlGenerator)
  {
    this.findSqlGenerator = findSqlGenerator;
  }

  public SqlGeneratorAndExtractor<QueryData, List<AnnotatedMatch>> getMatrixSqlGenerator()
  {
    return matrixSqlGenerator;
  }

  public void setMatrixSqlGenerator(
    SqlGeneratorAndExtractor<QueryData, List<AnnotatedMatch>> matrixSqlGenerator)
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

}
