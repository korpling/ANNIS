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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import annis.dao.AnnisDao;
import annis.dao.AnnotatedMatch;
import annis.dao.Match;
import annis.dao.MetaDataFilter;
import annis.model.Annotation;
import annis.model.QueryAnnotation;
import annis.ql.parser.AnnisParser;
import annis.ql.parser.QueryAnalysis;
import annis.ql.parser.QueryData;
import annis.service.objects.AnnisAttribute;
import annis.service.objects.AnnisCorpus;

import annis.sqlgen.*;

import annis.sqlgen.AnnotateQueryData;
import annis.sqlgen.AnnotateSqlGenerator;
import annis.sqlgen.LimitOffsetQueryData;
import annis.sqlgen.SqlGenerator;

import annis.utils.Utils;
import au.com.bytecode.opencsv.CSVWriter;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import java.io.*;
import java.net.URISyntaxException;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

// TODO: test AnnisRunner
public class AnnisRunner extends AnnisBaseRunner
{

  // logging
  private Logger log = Logger.getLogger(this.getClass());
  // SQL generators for query functions
  private SqlGenerator<QueryData, List<Match>> findSqlGenerator;
  private SqlGenerator<QueryData, Integer> countSqlGenerator;
  private AnnotateSqlGenerator<SaltProject> annotateSqlGenerator;
  private SqlGenerator<QueryData, List<AnnotatedMatch>> matrixSqlGenerator;
  private GraphSqlGenerator graphSqlGenerator;
  // dependencies
  private AnnisDao annisDao;
  private AnnisParser annisParser;
  private QueryAnalysis aqlAnalysis;
  private int context;
  private int matchLimit;
  private QueryAnalysis queryAnalysis;
  // settings
  private int limit = 10;
  private int offset;
  private int left = 5;
  private int right = 5;
  private String segmentationLayer = null;
  private List<Long> corpusList;
  private boolean clearCaches;
  private MetaDataFilter metaDataFilter;

  /**
   * @return the graphSqlGenerator
   */
  public GraphSqlGenerator getGraphSqlGenerator()
  {
    return graphSqlGenerator;
  }

  /**
   * @param graphSqlGenerator the graphSqlGenerator to set
   */
  public void setGraphSqlGenerator(GraphSqlGenerator graphSqlGenerator)
  {
    this.graphSqlGenerator = graphSqlGenerator;
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
  public void doDebug(String ignore)
  {
    doCorpus("2032");

    doSet("seg to clean");

    doSql("count tok & tok & #1 .clean,20 #2");
    doAnnotate("tok");
  }

  public void doParse(String annisQuery)
  {
    out.println(annisParser.dumpTree(annisQuery));
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

    Validate.notNull(generator, "don't now query function: " + function);

    return generator;
  }

  private String getAnnisQueryFromFunctionCall(String functionCall)
  {
    String[] split = functionCall.split(" ", 2);

    Validate.isTrue(split.length == 2, "bad call to plan");
    return split[1];
  }

  private String getAnnisFunctionyFromFunctionCall(String functionCall)
  {
    String[] split = functionCall.split(" ", 2);

    Validate.isTrue(split.length == 2, "bad call to plan");
    return split[0];
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
      benchmark.avgTimeInMilliseconds = Math.round((double) benchmark.avgTimeInMilliseconds
        / (double) benchmark.runs);
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

    // CSV output
    try
    {
      CSVWriter csv = new CSVWriter(new FileWriter(new File(
        "annis_benchmark_result.csv")));

      String[] header = new String[]
      {
        "corpora", "query", "avg", "diff-best", "diff-worst"
      };
      csv.writeNext(header);
      for (AnnisRunner.Benchmark benchmark : benchmarks)
      {
        String[] line = new String[5];
        line[0] = StringUtils.join(benchmark.queryData.getCorpusList(), ",");
        line[1] = benchmark.functionCall;
        line[2] = "" + benchmark.avgTimeInMilliseconds;
        line[3] = "" + Math.abs(benchmark.bestTimeInMilliseconds
          - benchmark.avgTimeInMilliseconds);
        line[4] = "" + Math.abs(benchmark.avgTimeInMilliseconds
          - benchmark.worstTimeInMilliseconds);
        csv.writeNext(line);
      }

      csv.close();

    }
    catch (IOException ex)
    {
      java.util.logging.Logger.getLogger(AnnisRunner.class.getName()).
        log(Level.SEVERE, null, ex);
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
   * parsed and the {@link QueryData#getAlternatives()} method should return an
   * empty List. Instead of parsing the annisQuery it extracts the salt ids and
   * put it into the extension's of {@link QueryData}.
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


    if ("annotate".equals(queryFunction))
    {
      queryData.addExtension(new AnnotateQueryData(left, right,
        segmentationLayer));
      queryData.addExtension(new LimitOffsetQueryData(offset, limit));
    }
    else if ("find".equals(queryFunction))
    {
      queryData.addExtension(new AnnotateQueryData(left, right,
        segmentationLayer));
    }


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
    JAXBContext jc = null;
    try
    {
      jc = JAXBContext.newInstance(annis.dao.Match.class);
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
      + limit + "; offset = " + offset);

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
    try
    {
      ObjectMapper om = new ObjectMapper();
      AnnotationIntrospector ai = new JaxbAnnotationIntrospector();
      om.getDeserializationConfig().withAnnotationIntrospector(ai);
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
    List<Annotation> corpusAnnotations = annisDao.listCorpusAnnotations(Long.
      parseLong(corpusId));
    printAsTable(corpusAnnotations, "namespace", "name", "value");
  }

  public void doSqlText(String textID)
  {
    long l = Long.parseLong(textID);
    System.out.println(annotateSqlGenerator.getTextQuery(l));
  }

  public void doText(String textID)
  {
    long l = Long.parseLong(textID);
    SaltProject p = annisDao.retrieveAnnotationGraph(Long.parseLong(textID));
    System.out.println(printSaltAsXMI(p));
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

  private QueryData parse(String input)
  {

    return annisDao.parseAQL(input, getCorpusList());

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
      return new String(outStream.toByteArray());

    }
    catch (IOException ex)
    {
      log.error(ex);
    }
    return "";
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

  private QueryData extractSaltIds(String saltIds)
  {
    QueryData queryData = new QueryData();
    SaltURIs uris = new SaltURIs();
    for (String id : saltIds.split("\\s"))
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
        log.error(ex);
        continue;
      }

      uris.add(uri);
    }

    log.debug(uris);
    queryData.addExtension(uris);
    return queryData;
  }
}
