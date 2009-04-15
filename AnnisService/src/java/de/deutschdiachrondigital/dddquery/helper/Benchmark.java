package de.deutschdiachrondigital.dddquery.helper;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import annisservice.exceptions.AnnisQLSemanticsException;
import de.deutschdiachrondigital.dddquery.sql.AnnotationRetriever;
import de.deutschdiachrondigital.dddquery.sql.GraphMatcher;
import de.deutschdiachrondigital.dddquery.sql.PathTranslator4;

public class Benchmark {

	private AnnisQlTranslator dddQueryMapper;
	private GraphMatcher graphMatcher;
	private AnnotationRetriever annotationRetriever;
	private QueryExecution planQueryExecution;
	private QueryExecution queryExecution;
	private List<String> queries;
	private int repetitions;

	private static Logger log = Logger.getLogger(Benchmark.class);
	
	public static void main(String[] args) throws AnnisQLSemanticsException {
		
		String[][] tableNames = {
//				{ "rank", "rank_anno", "struct", "anno", "anno_attribute" },
//				{ "rank", "rank_anno", "struct", "annotations", "annotations" },
//				{ "rank", "rank_anno", "struct_annotations", "struct_annotations", "struct_annotations" },
//				{ "rank_annotations", "rank_annotations", "struct_annotations", "struct_annotations", "struct_annotations" },
//				{ "rank_struct_annotations", "rank_anno", "rank_struct_annotations", "rank_struct_annotations", "rank_struct_annotations" },
		};

		for (String[] tables : tableNames) {
			new BeanFactory().getBenchmark().run(tables);
		}
	}

	public void run(String[] tables) throws AnnisQLSemanticsException {
		PathTranslator4 translator = (PathTranslator4) graphMatcher.getGraphTranslator().getPathTranslator();
		translator.setRankTable(tables[0]);
		translator.setRankAnnoTable(tables[1]);
		translator.setStructTable(tables[2]);
		translator.setAnnoTable(tables[3]);
		translator.setAnnoAttributeTable(tables[4]);
		log.info("type;query;matches;best;worst;average");
		for (String query : queries) {
			String dddQuery = dddQueryMapper.translate(query);
			long[] times = new long[repetitions];
			int count = 0;
			for (int i = 0; i < repetitions; ++i) {
				count = graphMatcher.matchGraph(null, dddQuery).size();
				times[i] = graphMatcher.getQueryExecution().getTime();
			}
			long best = Long.MAX_VALUE;
			long worst = Long.MIN_VALUE;
			long avg = 0;
			for (int i = 0; i < repetitions; ++i) {
				best = best > times[i] ? times[i] : best;
				worst = worst < times[i] ? times[i] : worst;
				avg += times[i];
			}
			avg /= repetitions;
			log.info("best;" + query + ";" + count + ";" + best + ";" + worst + ";" + avg + ";" + Arrays.asList(tables));
		}

//		Map<String, Long> results = new HashMap<String, Long>();
//		for (String query : queries)
//			results.put(query, 0L);
//		
//		List<String> tests = new ArrayList<String>();
//		for (String query : queries) 
//			tests.addAll(Collections.nCopies(repetitions, query));
//		Collections.shuffle(tests);
//		
//		for (String test : tests) {
//			String dddQuery = dddQueryMapper.translate(test);
//			graphMatcher.matchGraph(null, dddQuery).size();
//			results.put(test, results.get(test) + graphMatcher.getQueryExecution().getTime());
//		}
//		
//		for (Entry<String, Long> entry : results.entrySet()) {
//			log.info("average;" + entry.getKey() + ";?;?;?;" + entry.getValue() / repetitions + ";" + Arrays.asList(tables));
//		}
		
//		for (String test : tests) {
//			log.info("query: " + test);
//			test = dddQueryMapper.translate(test);
//			List<Match> matches = graphMatcher.matchGraph(null, test);
//			
//			log.info("matches found: " + matches.size());
//
//			if ( ! matches.isEmpty() ) {
//				new AnnisResultSetBuilder().buildResultSet(annotationRetriever.retrieveAnnotations(matches, 1, 1, 25, 0));
////				new AnnisResultSetBuilder().buildResultSet(annotationRetriever.retrieveAnnotations(matches, 1, 1));
//			}
//		}
	}

	public GraphMatcher getGraphMatcher() {
		return graphMatcher;
	}

	public void setGraphMatcher(GraphMatcher generator) {
		this.graphMatcher = generator;
	}

	public AnnotationRetriever getAnnotationRetriever() {
		return annotationRetriever;
	}

	public void setAnnotationRetriever(AnnotationRetriever annotationRetriever) {
		this.annotationRetriever = annotationRetriever;
	}

	public AnnisQlTranslator getDddQueryMapper() {
		return dddQueryMapper;
	}

	public void setDddQueryMapper(AnnisQlTranslator dddQueryMapper) {
		this.dddQueryMapper = dddQueryMapper;
	}

	public List<String> getQueries() {
		return queries;
	}

	public void setQueries(List<String> queries) {
		this.queries = queries;
	}

	public int getRepetitions() {
		return repetitions;
	}

	public void setRepetitions(int repetitions) {
		this.repetitions = repetitions;
	}

	public QueryExecution getQueryExecution() {
		return queryExecution;
	}

	public void setQueryExecution(QueryExecution queryExecution) {
		this.queryExecution = queryExecution;
	}

	public QueryExecution getPlanQueryExecution() {
		return planQueryExecution;
	}

	public void setPlanQueryExecution(QueryExecution planQueryExecution) {
		this.planQueryExecution = planQueryExecution;
	}

}
