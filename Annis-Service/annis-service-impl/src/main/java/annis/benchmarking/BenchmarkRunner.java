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
package annis.benchmarking;

import static annis.utils.Utils.avg;
import static annis.utils.Utils.max;
import static annis.utils.Utils.min;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import java.util.logging.Level;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;

import annis.AnnisBaseRunner;
import annis.administration.SpringAnnisAdministrationDao;
import annis.dao.AnnisDao;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.PropertyConfigurator;

// FIXME: - show used indexes
public class BenchmarkRunner extends AnnisBaseRunner
{

  public enum OS
  {

    linux,
    other
  }
  // logging
  private Logger log = Logger.getLogger(this.getClass());
  // constants
  private static int RUNS = 3;
  private static final int OFFSET = 0;
  private static final int LIMIT = 25;
  private static final int LEFT = 5;
  private static final int RIGHT = 5;
  // dependencies
  private AnnisDao annisDao;
  private SpringAnnisAdministrationDao administrationDao;

  public class Task
  {

    private String corpusName;
    private long corpusId;
    private String annisQuery;
    private String plan;
    private long matchCount;
    private LinkedList<Long> count;
    private LinkedList<Long> context;

    public Task()
    {
      count = new LinkedList<Long>();
      context = new LinkedList<Long>();
    }

    public String getCorpusName()
    {
      return corpusName;
    }

    public void setCorpusName(String corpusName)
    {
      this.corpusName = corpusName;
    }

    public Long getCorpusId()
    {
      return corpusId;
    }

    public void setCorpusId(Long corpusId)
    {
      this.corpusId = corpusId;
    }

    public String getAnnisQuery()
    {
      return annisQuery;
    }

    public void setAnnisQuery(String annisQuery)
    {
      this.annisQuery = annisQuery;
    }

    public long getMatchCount()
    {
      return matchCount;
    }

    public void setMatchCount(long matchCount)
    {
      this.matchCount = matchCount;
    }

    public LinkedList<Long> getCount()
    {
      return count;
    }

    public void setCount(LinkedList<Long> sequential)
    {
      this.count = sequential;
    }

    public LinkedList<Long> getContext()
    {
      return context;
    }

    public void setContext(LinkedList<Long> context)
    {
      this.context = context;
    }

    public String getPlan()
    {
      return plan;
    }

    public void setPlan(String plan)
    {
      this.plan = plan;
    }
  }

  public static void main(String[] args)
  {
    // get runner from Spring
    AnnisBaseRunner.getInstance("benchmarkRunner", "annis/benchmarking/BenchmarkRunner-context.xml").run(args);
  }

  // custom run method, no need for interactive commands
  @Override
  public void run(String[] args)
  {
    Options opts = new Options();
    opts.addOption("f", "file", true, "Input file");
    opts.addOption("h", "help", false, "Print this help");

    opts.addOption("c", "clear-cache", false,
      "Attempt to clear the cache before each new test AQL query (might need super user rights)");

    CommandLineParser parser = new PosixParser();

    try
    {
      CommandLine cmd = parser.parse(opts, args);

      if(cmd.hasOption("h"))
      {
        HelpFormatter fmt = new HelpFormatter();
        fmt.printHelp("benchmark.sh", opts);
      }
      else
      {
        log.info("test runs for each query: " + RUNS);

        if(!cmd.hasOption("f"))
        {
          log.error("You need to give a query file as argument with \"-f\" or \"--file\"");
          return;
        }

        boolean clearCache = cmd.hasOption("c");
        OS currentOS = OS.other;
        try
        {
          currentOS = OS.valueOf(System.getProperty("os.name").toLowerCase());
        }
        catch(IllegalArgumentException ex)
        {
        }
        BufferedReader inputFile = new BufferedReader(new FileReader(cmd.getOptionValue("f")));
        List<Task> tasks = readBenchmarkScript(inputFile);

        boolean reset = resetIndexes();
        runSequentially(tasks, clearCache, currentOS);
        listUsedIndexes(reset);
        printResults(tasks);
      }



    }
    catch(ParseException ex)
    {
      log.error("Could not parse command line", ex);
    }
    catch(FileNotFoundException ex)
    {
      log.error("The input file does not exist", ex);
    }
  }

  private List<Task> readBenchmarkScript(BufferedReader inputFile)
  {
    // get test queries from input file
    List<Task> tasks = new ArrayList<Task>();

    log.info("reading benchmark file...");
    for(String line = readInputFile(inputFile); line != null; line = readInputFile(inputFile))
    {
      log.debug(line);

      if(line.startsWith("#"))
      {
        continue;
      }

      // each line is a test query (with test corpus)
      Task task = new Task();

      // first word ist token name, then comes the query
      int spacePos = line.indexOf(" ");
      String corpusName = line.substring(0, spacePos);
      String annisQuery = line.substring(spacePos + 1);

      // look corpus id
      try
      {
        Long corpusId = convertCorpusNameToId(corpusName);
        task.setCorpusId(corpusId);
        task.setCorpusName(corpusName);
        task.setAnnisQuery(annisQuery);
        tasks.add(task);
      }
      catch(IndexOutOfBoundsException e)
      {
        log.info("no corpus found with name: " + corpusName + "; skipping line");
        continue;
      }
    }
    return tasks;
  }

  private void runSequentially(List<Task> tasks, boolean clearCache, OS currentOS)
  {

    log.info("computing match count, plan, uncached and cached runtime for test queries...");
    for(Task task : tasks)
    {
      String corpusName = task.getCorpusName();
      Long corpusId = task.getCorpusId();
      log.info("running query: " + task.getAnnisQuery() + " on corpus " + corpusId + " (" + corpusName + ")");

      String query = task.getAnnisQuery();
      LinkedList<Long> countRuntimes = task.getCount();
      LinkedList<Long> contextRuntimes = task.getContext();

      // run query once to load data from disk

      if(clearCache)
      {
        resetCaches(currentOS);
      }

      long matchCount = timeCountMatches(corpusId, query, countRuntimes);
      logRuntime("query", task, countRuntimes);
      task.setMatchCount(matchCount);

      for(int i = 0; i < RUNS; i++)
      {
        // run again to see cached performance
        timeCountMatches(corpusId, query, countRuntimes);
        logRuntime("query", task, countRuntimes);
      }

      for(int i = 0; i < RUNS; i++)
      {
        timeAnnotateFirst25(corpusId, query, contextRuntimes);
        logRuntime("1st 25 matches for query", task, contextRuntimes);
      }

      // query plan
      String plan = annisDao.planCount(annisDao.parseAQL(query, Arrays.asList(corpusId)),
        Arrays.asList(corpusId), true);
      log.debug("plan: " + plan);
      task.setPlan(plan);

      List<Long> countCached = countRuntimes.subList(1, countRuntimes.size());
      log.info("test query: " + task.getAnnisQuery() + " "
        + "on corpus " + corpusId + " (" + corpusName + ")\n"
        + "has " + matchCount + " matches;\n"
        + "runtime uncached/cached: " + countRuntimes.get(0) + " ms / " + avg(countCached) + " ms;\n"
        + "runtime for first 25 annotation graphs: " + avg(contextRuntimes) + " ms;");
    }
  }

  private void logRuntime(String msg, Task task, LinkedList<Long> runtimes)
  {
    log.debug("runtime: " + runtimes.getLast() + " ms for " + msg + ": " + task.getAnnisQuery() + " on corpus: " + task.getCorpusName());
  }

  private void resetCaches(OS currentOS)
  {
    switch(currentOS)
    {
      case linux:
        try
        {
          log.info("resetting caches");
          log.debug("syncing");
          Runtime.getRuntime().exec("sync").waitFor();
          File dropCaches = new File("/proc/sys/vm/drop_caches");
          if(dropCaches.canWrite())
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
          if(postgresScript.exists() && postgresScript.isFile())
          {
            log.debug("restarting postgresql");
            Runtime.getRuntime().exec(postgresScript.getAbsolutePath() + " restart")
              .waitFor();
          }
          else
          {
            log.warn("Cannot restart postgresql");
          }
          
        }
        catch(Exception ex)
        {
          log.error( null, ex);
        }

        break;
      default:
        log.warn("Cannot reset cache on this operating system");
    }
  }

  private void printResults(List<Task> tasks)
  {
    BufferedWriter writer = null;
    try
    {
      log.info(" writing benchmark results...");
      writer = new BufferedWriter(new FileWriter("benchmark.csv"));
      printLine(writer, "Query", "Corpus", "Count", "Uncached (Count)", "Avg Cached (Count)",
        "Max Cached (Count)", "Min Cached (Count)", "Avg Cached (First 25)");
      for(Task task : tasks)
      {
        long uncachedCount = task.getCount().getFirst();
        List<Long> count = task.getCount().subList(1, task.getCount().size());
        printLine(writer,
          task.getAnnisQuery(),
          task.getCorpusName(),
          String.valueOf(task.getMatchCount()),
          str(uncachedCount),
          str(avg(count)),
          str(max(count)),
          str(min(count)),
          str(avg(task.getContext())));
      }
    }
    catch(IOException ex)
    {
      log.error("Could not write benchmark result CSV file", ex);
    }
    finally
    {
      try
      {
        writer.close();
      }
      catch(IOException ex)
      {
        log.error(null, ex);
      }
    }
  }

  private String str(Object obj)
  {
    return String.valueOf(obj);
  }

  private void listUsedIndexes(boolean reset)
  {
    try
    {
      List<String> usedIndexes = administrationDao.listUsedIndexes("facts");

      if(reset)
      {
        log.info("Used indexes...");
      }
      else
      {
        log.info("Used indexes... (statistics could not be reset, values below may not be accurate!)");
      }

      for(String index : usedIndexes)
      {
        log.info(index);
      }
    }
    catch(DataAccessException e)
    {
      log.info("Could not access used indices, probably bad postgres superuser password");
    }
  }

  private boolean resetIndexes()
  {
    boolean reset = administrationDao.resetStatistics();
    if(reset)
    {
      log.info("reset index and table statistics");
    }
    else
    {
      log.info("index and table statistics could not be reset");
    }
    return reset;
  }

  private void printLine(BufferedWriter writer, String... fields) throws IOException
  {

    List<String> quoted = new ArrayList<String>();
    for(String field : fields)
    {
      quoted.add(quotedField(field));
    }
    writer.write(StringUtils.join(quoted, ","));
    writer.newLine();
  }

  private String quotedField(String field)
  {
    return "'" + field + "'";
  }

  private int timeCountMatches(long corpusId, String query, List<Long> times)
  {
    long start = new Date().getTime();
    int count = annisDao.countMatches(Arrays.asList(corpusId),
      annisDao.parseAQL(query, Arrays.asList(corpusId)));
    long end = new Date().getTime();
    if(times != null)
    {
      times.add(end - start);
    }
    return count;
  }

  private void timeAnnotateFirst25(long corpusId, String query, List<Long> times)
  {
    long start = new Date().getTime();
    annisDao.retrieveAnnotationGraph(Arrays.asList(corpusId), annisDao.parseAQL(query, Arrays.asList(corpusId)), OFFSET, LIMIT, LEFT, RIGHT);
    long end = new Date().getTime();
    if(times != null)
    {
      times.add(end - start);
    }
  }

  private long convertCorpusNameToId(String corpusName)
  {
    return annisDao.listCorpusByName(Arrays.asList(corpusName)).get(0);
  }

  private String readInputFile(BufferedReader inputFile)
  {
    try
    {
      return inputFile.readLine();
    }
    catch(IOException e)
    {
      log.error("Could not read input file", e);
      return null;
    }
  }

  public AnnisDao getAnnisDao()
  {
    return annisDao;
  }

  public void setAnnisDao(AnnisDao annisDao)
  {
    this.annisDao = annisDao;
  }

  public SpringAnnisAdministrationDao getAdministrationDao()
  {
    return administrationDao;
  }

  public void setAdministrationDao(SpringAnnisAdministrationDao administrationDao)
  {
    this.administrationDao = administrationDao;
  }

}
