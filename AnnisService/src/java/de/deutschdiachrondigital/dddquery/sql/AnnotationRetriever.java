package de.deutschdiachrondigital.dddquery.sql;

import java.sql.ResultSet;
import java.util.List;

import org.apache.log4j.Logger;

import de.deutschdiachrondigital.dddquery.helper.QueryExecution;

public class AnnotationRetriever {

	private Logger log = Logger.getLogger(this.getClass());
	
	public interface SqlGenerator {
		
		public String generateSql(List<Match> matches, int left, int right);
		
	}
	
	private QueryExecution queryExecution;
	private SqlGenerator sqlGenerator;
	
	public ResultSet retrieveAnnotations(List<Match> matches, int left, int right) {
		log.info("retrieving annotations for matches: " + matches);
		
		String sqlQuery = sqlGenerator.generateSql(matches, left, right);
		log.debug("SQL query is:\n" + sqlQuery);
		
		return queryExecution.executeQuery(sqlQuery);
	}
	
	public ResultSet retrieveAnnotations(List<Match> matches, int left, int right, int limit, int offset) {
		if (offset > matches.size())
			throw new DddException("offset is too large; offset = " + offset + "; matches.size() = " + matches.size());
		
		int toIndex = Math.min(offset + limit, matches.size());
		
		return retrieveAnnotations(matches.subList(offset, toIndex), left, right);
	}
	
	public QueryExecution getQueryExecution() {
		return queryExecution;
	}

	public void setQueryExecution(QueryExecution queryExecution) {
		this.queryExecution = queryExecution;
	}

	public SqlGenerator getSqlGenerator() {
		return sqlGenerator;
	}

	public void setSqlGenerator(SqlGenerator sqlGenerator) {
		this.sqlGenerator = sqlGenerator;
	}

}