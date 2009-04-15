package de.deutschdiachrondigital.dddquery.helper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class PlanQueryExecution extends QueryExecution {

	private Logger log = Logger.getLogger(this.getClass());
	
	private boolean analyze = true;
	private QueryExecution queryExecution;
	
	public static class QueryPlanResultSetConverter implements ResultSetConverter<String> {
		
		private Logger log = Logger.getLogger(this.getClass());

		public String convertResultSet(ResultSet resultSet) {
			StringBuffer sb = new StringBuffer();
			try {
				while (resultSet.next()) {
					sb.append(resultSet.getString(1));
					sb.append("\n");					
				}
			} catch (SQLException e) {
				log.warn("an exception occured while processing the query plan", e);
				throw new RuntimeException(e);
			}
			return sb.toString();
		}

	}
	
	private ResultSetConverter<String> resultSetConverter = new QueryPlanResultSetConverter();
	
	@Override
	public ResultSet executeQuery(String sqlQuery) {
		String explainSqlQuery = "explain " + ( analyze ? "analyze " : "") + sqlQuery;
		
		log.debug("queyer:\n " + sqlQuery);
		log.debug("plan:\n" + resultSetConverter.convertResultSet(queryExecution.executeQuery(explainSqlQuery)));
			
		return queryExecution.executeQuery(sqlQuery);
	}

	public boolean isAnalyze() {
		return analyze;
	}

	public void setAnalyze(boolean analyze) {
		this.analyze = analyze;
	}

	public QueryExecution getQueryExecution() {
		return queryExecution;
	}

	public void setQueryExecution(QueryExecution queryExecution) {
		this.queryExecution = queryExecution;
	}

}
