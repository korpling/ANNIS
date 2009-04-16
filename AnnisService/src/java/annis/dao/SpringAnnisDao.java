package annis.dao;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import annis.WekaDaoHelper;
import annis.model.AnnisNode;
import annis.model.AnnotationGraph;
import annis.service.ifaces.AnnisAttribute;
import annis.service.ifaces.AnnisCorpus;
import annis.sqlgen.ListCorpusSqlHelper;
import annis.sqlgen.ListNodeAnnotationsSqlHelper;
import annis.sqlgen.SqlGenerator;
import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.parser.DddQueryParser;

// FIXME: test and refactor timeout and transaction management
public class SpringAnnisDao extends SimpleJdbcDaoSupport implements AnnisDao {

	private static Logger log = Logger.getLogger(SpringAnnisDao.class);

	private int timeout;

	private DddQueryParser dddQueryParser;
	private SqlGenerator sqlGenerator;
	private List<MatchFilter> matchFilters;
	private MatchRowMapper matchRowMapper;
	private ParameterizedSingleColumnRowMapper<String> planRowMapper;

	private AnnotationGraphDaoHelper annotationGraphDaoHelper;
	private WekaDaoHelper wekaSqlHelper;
	private ListCorpusSqlHelper listCorpusSqlHelper;
	private ListNodeAnnotationsSqlHelper listNodeAnnotationsSqlHelper;

	private CorpusSelectionStrategyFactory corpusSelectionStrategyFactory;
	private PlatformTransactionManager transactionManager;

	public SpringAnnisDao() {
		planRowMapper = new ParameterizedSingleColumnRowMapper<String>();
		matchFilters = new ArrayList<MatchFilter>();
	}

	// /// Interface

	private class QueryTemplate<T> {
		private ParameterizedRowMapper<T> rowMapper;
		private String prefix;

		public QueryTemplate(ParameterizedRowMapper<T> rowMapper) {
			this(rowMapper, null);
		}

		public QueryTemplate(ParameterizedRowMapper<T> rowMapper, String prefix) {
			this.rowMapper = rowMapper;
			this.prefix = prefix;
		}

		public List<T> query(List<Long> corpusList, String dddQuery) {
			return query(corpusList, dddQuery, 0);
		}

		@SuppressWarnings("unchecked")
		public List<T> query(final List<Long> corpusList,
				final String dddQuery, final int timeout) {

			TransactionTemplate transactionTemplate = new TransactionTemplate(
					transactionManager);
			return (List<T>) transactionTemplate
					.execute(new TransactionCallback() {

						public Object doInTransaction(TransactionStatus status) {
							// roll back the transaction at the end
							status.setRollbackOnly();

							// parse query
							Start parsedStatement = dddQueryParser
									.parse(dddQuery);

							// create corpus selection strategy
							CorpusSelectionStrategy corpusSelectionStrategy = corpusSelectionStrategyFactory
									.createCorpusSelectionStrategy(corpusList);

							// generate sql query
							String sqlQuery = sqlGenerator.toSql(
									parsedStatement, corpusSelectionStrategy);
							if (prefix != null) // XXX: ugly
								sqlQuery = prefix + " " + sqlQuery;

							// optional view creation
							if (corpusSelectionStrategy.usesViews())
								getSimpleJdbcTemplate()
										.update(
												corpusSelectionStrategy
														.createViewSql());

							// optional timeout
							if (timeout > 0)
								getSimpleJdbcTemplate().update(
										"SET statement_timeout TO " + timeout);

							// execute sql
							List<T> results = getSimpleJdbcTemplate().query(
									sqlQuery, rowMapper);

							return results;
						}

					});
		}
	}

	public List<Match> findMatches(List<Long> corpusList, String dddQuery) {
		Validate.notNull(corpusList, "corpusList=null passed as argument");
		QueryTemplate<Match> findTemplate = new QueryTemplate<Match>(
				matchRowMapper);
		List<Match> matches = findTemplate.query(corpusList, dddQuery, timeout);

		// FIXME: make sure Charset is imported, so defaultCharset can be checked during debugging
		Charset.defaultCharset();
		
		// filter matches
		filter(matches);
		return matches;
	}

	public int countMatches(List<Long> corpusList, String dddQuery) {
		return findMatches(corpusList, dddQuery).size();
	}

	@Deprecated
	public int doWait(final int seconds) {
		TransactionTemplate transactionTemplate = new TransactionTemplate(
				transactionManager);
		return (Integer) transactionTemplate.execute(new TransactionCallback() {

			public Object doInTransaction(TransactionStatus status) {
				SimpleJdbcTemplate simpleJdbcTemplate = getSimpleJdbcTemplate();
				simpleJdbcTemplate.update("SET statement_timeout TO 3000");
				simpleJdbcTemplate.update("CREATE TABLE foo (bar INTEGER)");
				status.setRollbackOnly();
				return 0;
				// return simpleJdbcTemplate.queryForInt("select wait( ? )",
				// seconds);
			}

		});

	}

	public String plan(String dddQuery, List<Long> corpusList, boolean analyze) {
		QueryTemplate<String> planTemplate = new QueryTemplate<String>(
				planRowMapper, analyze ? "EXPLAIN ANALYZE" : "EXPLAIN");
		List<String> plan = planTemplate.query(corpusList, dddQuery);
		return StringUtils.join(plan, "\n");
	}

	@SuppressWarnings("unchecked")
	public List<AnnotationGraph> retrieveAnnotationGraph(List<Match> matches,
			int left, int right) {
		if (matches.isEmpty())
			return new ArrayList<AnnotationGraph>();
		return (List<AnnotationGraph>) getJdbcTemplate().query(
				annotationGraphDaoHelper.createSqlQuery(matches, left, right),
				annotationGraphDaoHelper);
	}

	@SuppressWarnings("unchecked")
	public List<AnnisNode> annotateMatches(List<Match> matches) {
		return (List<AnnisNode>) getJdbcTemplate().query(
				wekaSqlHelper.createSqlQuery(matches), wekaSqlHelper);
	}

	@SuppressWarnings("unchecked")
	public List<AnnisCorpus> listCorpora() {
		return (List<AnnisCorpus>) getJdbcTemplate().query(
				listCorpusSqlHelper.createSqlQuery(), listCorpusSqlHelper);
	}

	@SuppressWarnings("unchecked")
	public List<AnnisAttribute> listNodeAnnotations(List<Long> corpusList,
			boolean listValues) {
		return (List<AnnisAttribute>) getJdbcTemplate().query(
				listNodeAnnotationsSqlHelper.createSqlQuery(corpusList,
						listValues), listNodeAnnotationsSqlHelper);
	}

	@SuppressWarnings("unchecked")
	public AnnotationGraph retrieveAnnotationGraph(long textId) {
		List<AnnotationGraph> graphs = (List<AnnotationGraph>) getJdbcTemplate()
				.query(annotationGraphDaoHelper.createSqlQuery(textId),
						annotationGraphDaoHelper);
		if (graphs.isEmpty())
			return null;
		if (graphs.size() > 1)
			throw new IllegalStateException("Expected only one annotation graph");
		return graphs.get(0);
	}

	// /// private helper

	private void filter(List<Match> matches) {
		for (MatchFilter filter : matchFilters) {
			filter.init();
			List<Match> matchCopy = new ArrayList<Match>(matches);
			for (Match match : matchCopy)
				if (filter.filterMatch(match)) {
					log.debug("removing match " + match);
					matches.remove(match);
				}
		}
	}

	// /// Getter / Setter

	public DddQueryParser getDddQueryParser() {
		return dddQueryParser;
	}

	public void setDddQueryParser(DddQueryParser parser) {
		this.dddQueryParser = parser;
	}

	public SqlGenerator getSqlGenerator() {
		return sqlGenerator;
	}

	public void setSqlGenerator(SqlGenerator sqlGenerator) {
		this.sqlGenerator = sqlGenerator;
	}

	public List<MatchFilter> getMatchFilters() {
		return matchFilters;
	}

	public void setMatchFilters(List<MatchFilter> matchFilters) {
		this.matchFilters = matchFilters;
	}

	public MatchRowMapper getMatchRowMapper() {
		return matchRowMapper;
	}

	public void setMatchRowMapper(MatchRowMapper matchRowMapper) {
		this.matchRowMapper = matchRowMapper;
	}

	public ParameterizedSingleColumnRowMapper<String> getPlanRowMapper() {
		return planRowMapper;
	}

	public void setPlanRowMapper(
			ParameterizedSingleColumnRowMapper<String> planRowMapper) {
		this.planRowMapper = planRowMapper;
	}

	public AnnotationGraphDaoHelper getAnnotateMatchesQueryHelper() {
		return getAnnotationGraphDaoHelper();
	}

	public AnnotationGraphDaoHelper getAnnotationGraphDaoHelper() {
		return annotationGraphDaoHelper;
	}

	public void setAnnotateMatchesQueryHelper(
			AnnotationGraphDaoHelper annotateMatchesQueryHelper) {
		setAnnotationGraphDaoHelper(annotateMatchesQueryHelper);
	}

	public void setAnnotationGraphDaoHelper(
			AnnotationGraphDaoHelper annotationGraphDaoHelper) {
		this.annotationGraphDaoHelper = annotationGraphDaoHelper;
	}

	public CorpusSelectionStrategyFactory getCorpusSelectionStrategyFactory() {
		return corpusSelectionStrategyFactory;
	}

	public void setCorpusSelectionStrategyFactory(
			CorpusSelectionStrategyFactory corpusSelectionStrategyFactory) {
		this.corpusSelectionStrategyFactory = corpusSelectionStrategyFactory;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(
			PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public WekaDaoHelper getWekaSqlHelper() {
		return wekaSqlHelper;
	}

	public void setWekaSqlHelper(WekaDaoHelper wekaHelper) {
		this.wekaSqlHelper = wekaHelper;
	}

	public ListCorpusSqlHelper getListCorpusSqlHelper() {
		return listCorpusSqlHelper;
	}

	public void setListCorpusSqlHelper(ListCorpusSqlHelper listCorpusHelper) {
		this.listCorpusSqlHelper = listCorpusHelper;
	}

	public ListNodeAnnotationsSqlHelper getListNodeAnnotationsSqlHelper() {
		return listNodeAnnotationsSqlHelper;
	}

	public void setListNodeAnnotationsSqlHelper(
			ListNodeAnnotationsSqlHelper listNodeAnnotationsSqlHelper) {
		this.listNodeAnnotationsSqlHelper = listNodeAnnotationsSqlHelper;
	}

}