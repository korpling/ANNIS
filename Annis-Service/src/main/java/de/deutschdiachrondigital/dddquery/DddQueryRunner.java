package de.deutschdiachrondigital.dddquery;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import annis.AnnisBaseRunner;
import annis.AnnotationGraphDotExporter;
import annis.TableFormatter;
import annis.WekaDaoHelper;
import annis.dao.AnnisDao;
import annis.dao.AnnotationGraphDaoHelper;
import annis.dao.Match;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.service.ifaces.AnnisAttribute;
import annis.service.ifaces.AnnisCorpus;
import annis.service.objects.AnnisResultImpl;
import annis.sqlgen.ListCorpusSqlHelper;
import annis.sqlgen.ListNodeAnnotationsSqlHelper;
import annis.sqlgen.SqlGenerator;
import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.parser.DddQueryParser;
import java.util.HashSet;
import java.util.Set;

public class DddQueryRunner extends AnnisBaseRunner {

//	private static Logger log = Logger.getLogger(DddQueryRunner.class);
	
	// dependencies
	private DddQueryParser dddQueryParser;
	private SqlGenerator findSqlGenerator;
	private AnnisDao annisDao;
	
	private AnnotationGraphDaoHelper annotationGraphDaoHelper;
	private WekaDaoHelper wekaDaoHelper;
	private ListCorpusSqlHelper listCorpusHelper;
	private ListNodeAnnotationsSqlHelper listNodeAnnotationsSqlHelper;

	private AnnotationGraphDotExporter annotationGraphDotExporter;
	private TableFormatter tableFormatter;
	
	// settings
	private int matchLimit;
	private int context;
	private List<Long> corpusList;
	
	public DddQueryRunner() {
		corpusList = new ArrayList<Long>();
	}
	
	public static void main(String[] args) {
		// get runner from Spring
		AnnisBaseRunner.getInstance("dddQueryRunner", "de/deutschdiachrondigital/dddquery/DddQueryRunner-context.xml").run(args);
	}
	
	///// CLI methods
	
	public void doHelp(String dddquery) {
		out.println("not implemented");
	}

	public void doParse(String dddQuery) {
		out.println(DddQueryParser.dumpTree(dddQueryParser.parse(dddQuery)));
	}
	
	// FIXME: missing tests
	public void doSql(String dddQuery) {
		// sql query
		Start statement = dddQueryParser.parse(dddQuery);
		String sql = findSqlGenerator.toSql(statement, corpusList);

		out.println(sql);
	}
	
	public void doFind(String dddQuery) {
		List<Match> matches = annisDao.findMatches(getCorpusList(), dddQuery);
		printAsTable(matches);
	}
	
	public void doCount(String dddQuery) {
		out.println(annisDao.countMatches(getCorpusList(), dddQuery));
	}
	
	public void doPlan(String dddQuery) {
		out.println(annisDao.plan(dddQuery, getCorpusList(), false));
	}
	
	public void doAnalyze(String dddQuery) {
		out.println(annisDao.plan(dddQuery, getCorpusList(), true));
	}
	
	public void doAnnotate(String dddQuery) {
		List<AnnotationGraph> graphs = retrieveAnnotationGraph(dddQuery);
		// FIXME: refactor TableFormatter to optinally use a helper class to make short forms, see AnnotationGraphDaoHelper.toString()
		printAsTable(graphs, "nodes", "edges");
	}

	public void doAnnotate2(String dddQuery) {
		List<AnnotationGraph> graphs = annisDao.retrieveAnnotationGraph(getCorpusList(), dddQuery, 0, matchLimit, context, context);
		printAsTable(graphs, "nodes", "edges");
	}
	
	private List<AnnotationGraph> retrieveAnnotationGraph(String dddQuery) {
		List<Match> matches = annisDao.findMatches(getCorpusList(), dddQuery);
		matches = annotationGraphDaoHelper.slice(matches, 0, matchLimit);
		// FIXME: context konfigurieren
		int context = 2;
		return annisDao.retrieveAnnotationGraph(matches, context, context);
	}

	public void doCorpus(List<Long> corpora) {
		setCorpusList(corpora);
	}

	public void doWait(String seconds) {
		try {
			out.println(annisDao.doWait(Integer.parseInt(seconds)));
		} catch (Exception e) {
			//
		}
	}
	
	public void doWeka(String dddQuery) {
		List<Match> matches = annisDao.findMatches(corpusList, dddQuery);
		if ( ! matches.isEmpty() ) {
			List<AnnisNode> annotatedNodes = annisDao.annotateMatches(matches);
			out.println(wekaDaoHelper.exportAsWeka(annotatedNodes, matches));
		} else
			out.println("(empty)");
	}
	
	public void doList(String unused) {
		List<AnnisCorpus> corpora = annisDao.listCorpora();
		printAsTable(corpora, "id", "name", "textCount", "tokenCount");
	}
	
	public void doNodeAnnotations(String doListValues) {
		boolean listValues = "values".equals(doListValues);
		List<AnnisAttribute> nodeAnnotations = annisDao.listNodeAnnotations(corpusList, listValues);
		printAsTable(nodeAnnotations, "name", "distinctValues");
	}
	
	public void doDot(String dddQuery) {
		List<AnnotationGraph> graphs = retrieveAnnotationGraph(dddQuery);
		for (AnnotationGraph graph : graphs)
			annotationGraphDotExporter.writeDotFile(graph);
		out.println(graphs.size() + " graphs written to " + annotationGraphDotExporter.getPath());
	}
	
	public void doPaula(String dddQuery) {
		List<AnnotationGraph> graphs = retrieveAnnotationGraph(dddQuery);
		for (AnnotationGraph graph : graphs) {
			AnnisResultImpl annisResultImpl = new AnnisResultImpl(graph);
			out.println("PAULA-Unart of annotation graph for nodes: " + StringUtils.join(graph.getMatchedNodeIds(), ", "));
			out.println(annisResultImpl.getPaula());
		}
	}
	
	public void doMeta(String corpusId) {
		List<Annotation> corpusAnnotations = annisDao.listCorpusAnnotations(Long.parseLong(corpusId));
		printAsTable(corpusAnnotations, "namespace", "name", "value");
	}
	
  public void doResolvertest(String arg)
  {
    Set<String> nsNode = new HashSet<String>();
    Set<String> nsEdge = new HashSet<String>();
    
    annisDao.getResolverEntries(0, nsNode, nsEdge);
  }

	///// Helper
	
	private void printAsTable(List<? extends Object> list, String... fields) {
		out.println(tableFormatter.formatAsTable(list, fields));
	}
	
	///// Getter / Setter
	
	public DddQueryParser getDddQueryParser() {
		return dddQueryParser;
	}
	
	public void setDddQueryParser(DddQueryParser dddQueryParser) {
		this.dddQueryParser = dddQueryParser;
	}
	
	public SqlGenerator getFindSqlGenerator() {
		return findSqlGenerator;
	}
	
	public void setFindSqlGenerator(SqlGenerator sqlGenerator) {
		this.findSqlGenerator = sqlGenerator;
	}
	
	public AnnisDao getAnnisDao() {
		return annisDao;
	}
	
	public void setAnnisDao(AnnisDao annisDao) {
		this.annisDao = annisDao;
	}
	
	public AnnotationGraphDaoHelper getAnnotationGraphDaoHelper() {
		return annotationGraphDaoHelper;
	}
	
	public void setAnnotationGraphDaoHelper(
			AnnotationGraphDaoHelper annotationGraphDaoHelper) {
		this.annotationGraphDaoHelper = annotationGraphDaoHelper;
	}
	
	public int getMatchLimit() {
		return matchLimit;
	}
	
	public void setMatchLimit(int matchLimit) {
		this.matchLimit = matchLimit;
	}
	
	public List<Long> getCorpusList() {
		return corpusList;
	}
	
	public void setCorpusList(List<Long> corpusList) {
		this.corpusList = corpusList;
	}

	public TableFormatter getTableFormatter() {
		return tableFormatter;
	}

	public void setTableFormatter(TableFormatter tableFormatter) {
		this.tableFormatter = tableFormatter;
	}

	public WekaDaoHelper getWekaDaoHelper() {
		return wekaDaoHelper;
	}

	public void setWekaDaoHelper(WekaDaoHelper wekaDaoHelper) {
		this.wekaDaoHelper = wekaDaoHelper;
	}

	public ListCorpusSqlHelper getListCorpusHelper() {
		return listCorpusHelper;
	}

	public void setListCorpusHelper(ListCorpusSqlHelper listCorpusHelper) {
		this.listCorpusHelper = listCorpusHelper;
	}

	public ListNodeAnnotationsSqlHelper getListNodeAnnotationsSqlHelper() {
		return listNodeAnnotationsSqlHelper;
	}

	public void setListNodeAnnotationsSqlHelper(
			ListNodeAnnotationsSqlHelper listNodeAnnotationsSqlHelper) {
		this.listNodeAnnotationsSqlHelper = listNodeAnnotationsSqlHelper;
	}

	public AnnotationGraphDotExporter getAnnotationGraphDotExporter() {
		return annotationGraphDotExporter;
	}

	public void setAnnotationGraphDotExporter(
			AnnotationGraphDotExporter annotationGraphDotExporter) {
		this.annotationGraphDotExporter = annotationGraphDotExporter;
	}

	public int getContext() {
		return context;
	}

	public void setContext(int context) {
		this.context = context;
	}

}
