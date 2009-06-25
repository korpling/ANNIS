package annis.benchmarking;

import static annis.utils.Utils.avg;
import static annis.utils.Utils.max;
import static annis.utils.Utils.min;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import annis.AnnisBaseRunner;
import annis.administration.SpringAnnisAdministrationDao;
import annis.dao.AnnisDao;
import de.deutschdiachrondigital.dddquery.DddQueryMapper;

// FIXME: - show used indexes
public class BenchmarkRunner extends AnnisBaseRunner {

	// logging
	private Logger log = Logger.getLogger(this.getClass());

	// constants
	private static int RUNS = 3;
	
	// dependencies
	private AnnisDao annisDao;
	private SpringAnnisAdministrationDao administrationDao;
	private DddQueryMapper dddQueryMapper;
	
	public class Task {
		private String corpusName;
		private long corpusId;
		private String annisQuery;
		private String dddQuery;
		private String plan;
		private long matchCount;
		private List<Long> sequential;
		private List<Long> random;
		
		public Task() {
			sequential = new ArrayList<Long>();
			random = new ArrayList<Long>();
		}

		public String getCorpusName() {
			return corpusName;
		}

		public void setCorpusName(String corpusName) {
			this.corpusName = corpusName;
		}

		public Long getCorpusId() {
			return corpusId;
		}

		public void setCorpusId(Long corpusId) {
			this.corpusId = corpusId;
		}

		public String getAnnisQuery() {
			return annisQuery;
		}

		public void setAnnisQuery(String annisQuery) {
			this.annisQuery = annisQuery;
		}

		public long getMatchCount() {
			return matchCount;
		}

		public void setMatchCount(long matchCount) {
			this.matchCount = matchCount;
		}

		public List<Long> getSequential() {
			return sequential;
		}

		public void setSequential(List<Long> sequential) {
			this.sequential = sequential;
		}

		public List<Long> getRandom() {
			return random;
		}

		public void setRandom(List<Long> random) {
			this.random = random;
		}

		public String getPlan() {
			return plan;
		}

		public void setPlan(String plan) {
			this.plan = plan;
		}

		public String getDddQuery() {
			return dddQuery;
		}

		public void setDddQuery(String dddQuery) {
			this.dddQuery = dddQuery;
		}

	}
	
	public static void main(String[] args) {
		// get runner from Spring
		AnnisBaseRunner.getInstance("benchmarkRunner", "annis/benchmarking/BenchmarkRunner-context.xml").run(args);
	}
	
	// custom run method, no need for interactive commands
	public void run(String[] args) {
		log.info("test runs for each query: " + RUNS);
		List<Task> tasks = readBenchmarkScript();
		computeTaskInfo(tasks);
//		listIndexes();
		boolean reset = resetIndexes();
		runSequentially(tasks);
		runRandomly(tasks);
		listUsedIndexes(reset);
		printResults(tasks);
	}

	private List<Task> readBenchmarkScript() {
		BufferedReader inputFile = new BufferedReader(new InputStreamReader(System.in));
		
		// get test queries from input file
		List<Task> tasks = new ArrayList<Task>();
		
		log.info("reading benchmark file...");
		for (String line = readInputFile(inputFile); line != null; line = readInputFile(inputFile)) {
			log.debug(line);
			
			if (line.startsWith("#"))
				continue;
			
			// each line is a test query (with test corpus)
			Task task = new Task();

			// first word ist token name, then comes the query
			int spacePos = line.indexOf(" ");
			String corpusName = line.substring(0, spacePos);
			String annisQuery = line.substring(spacePos + 1);

			// look corpus id
			try {
				Long corpusId = convertCorpusNameToId(corpusName);
				task.setCorpusId(corpusId);
				task.setCorpusName(corpusName);
				task.setAnnisQuery(annisQuery);
				task.setDddQuery(dddQueryMapper.translate(annisQuery));
				tasks.add(task);
			} catch (IndexOutOfBoundsException e) {
				log.info("no corpus found with name: " + corpusName + "; skipping line");
				continue;
			}
		}
		return tasks;
	}

	private void computeTaskInfo(List<Task> tasks) {
		log.info("computing matchcount and plan for test queries...");
		for (Task task : tasks) {
			String corpusName = task.getCorpusName();
			Long corpusId = task.getCorpusId();
			
			// run query once to load changes into db
			String query = task.getDddQuery();
			countMatches(corpusId, query);
			
			// query plan
			String plan = annisDao.plan(query, Arrays.asList(corpusId), true);
			task.setPlan(plan);
			
			// match count
			long matchCount = annisDao.countMatches(Arrays.asList(corpusId), query);
			task.setMatchCount(matchCount);
			log.info("test query: " + task.getAnnisQuery() + " on corpus " + corpusId + " (" + corpusName + ") has " + matchCount + " matches; plan:\n" + plan);
		}
	}

	private void printResults(List<Task> tasks) {
		log.info("benchmark results...");
		printLine("Query", "Corpus", "Count", "Min seq", "Avg seq", "Max seq", "Min rand", "Avg rand", "Max rand");
		for (Task task : tasks) {
			List<Long> seq = task.getSequential();
			List<Long> rand = task.getRandom();
			printLine(task.getAnnisQuery(), task.getCorpusName(), String.valueOf(task.getMatchCount()), min(seq), avg(seq), max(seq), min(rand), avg(rand), max(rand));
		}
	}

	private void listUsedIndexes(boolean reset) {
		List<String> usedIndexes = administrationDao.listUsedIndexes("facts");
		
		if (reset)
			log.info("Used indexes...");
		else
			log.info("Used indexes... (statistics could not be reset, values below may not be accurate!)");

		for (String index : usedIndexes)
			log.info(index);
	}

	private void runRandomly(List<Task> tasks) {
		log.info("running test queries randomly...");
		// run test queries randomly
		List<Task> random = new ArrayList<Task>();
		for (Task task : tasks) {
			for (int i = 0; i < RUNS; ++i) {
				random.add(task);
			}
		}
		Collections.shuffle(random);
		for (Task task : random) {
			long corpusId = task.getCorpusId();
			String query = task.getDddQuery();
			List<Long> runtimeList = task.getRandom();
			timeCountMatches(corpusId, query, runtimeList);
			log.info("runtime: " + lastRuntime(runtimeList) + " ms for query: " + task.getAnnisQuery() + " on corpus: " + task.getCorpusName());
		}
	}

	private void runSequentially(List<Task> tasks) {
		log.info("running test queries sequentially...");
		// run test queries sequentially
		for (Task task : tasks) {
			long corpusId = task.getCorpusId();
			String dddQuery = task.getDddQuery();
			for (int i = 0; i < RUNS; ++i) {
				List<Long> runtimeList = task.getSequential();
				timeCountMatches(corpusId, dddQuery, runtimeList);
				log.info("runtime: " + lastRuntime(runtimeList) + " ms for query: " + task.getAnnisQuery() + " on corpus: " + task.getCorpusName());
			}
		}
	}

	private boolean resetIndexes() {
		boolean reset = administrationDao.resetStatistics();
		if (reset)
			log.info("reset index and table statistics");
		else
			log.info("index and table statistics could not be reset");
		return reset;
	}

	private void listIndexes() {
		log.info("Indices on fact table:");
		List<String> indexDefinitions = administrationDao.listIndexDefinitions("facts");
		for (String definition : indexDefinitions)
			log.info(definition);
	}
	
	private void printLine(String... fields) {
		List<String> quoted = new ArrayList<String>();
		for (String field : fields)
			quoted.add(quotedField(field));
		System.out.println(StringUtils.join(quoted, ","));
	}

	private String quotedField(String field) {
		return "'" + field + "'";
	}

	private Long lastRuntime(List<Long> runtimeList) {
		return runtimeList.get(runtimeList.size() - 1);
	}

	private void countMatches(Long corpusId, String query) {
		timeCountMatches(corpusId, query, null);
	}

	private void timeCountMatches(long corpusId, String query, List<Long> times) {
		long start = new Date().getTime();
		annisDao.countMatches(Arrays.asList(corpusId), query);
		long end = new Date().getTime();
		if (times != null)
			times.add(end - start);
	}

	private long convertCorpusNameToId(String corpusName) {
		return annisDao.listCorpusByName(Arrays.asList(corpusName)).get(0);
	}

	private String readInputFile(BufferedReader inputFile) {
		try {
			return inputFile.readLine();
		} catch (IOException e) {
			log.error("Could not read input file", e);
			return null;
		}
	}

	public AnnisDao getAnnisDao() {
		return annisDao;
	}

	public void setAnnisDao(AnnisDao annisDao) {
		this.annisDao = annisDao;
	}

	public DddQueryMapper getDddQueryMapper() {
		return dddQueryMapper;
	}

	public void setDddQueryMapper(DddQueryMapper dddQueryMapper) {
		this.dddQueryMapper = dddQueryMapper;
	}

	public SpringAnnisAdministrationDao getAdministrationDao() {
		return administrationDao;
	}

	public void setAdministrationDao(SpringAnnisAdministrationDao administrationDao) {
		this.administrationDao = administrationDao;
	}

}
