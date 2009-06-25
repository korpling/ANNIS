package annis.dao;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import annis.ql.parser.QueryData;

public class TimeOutSqlSessionModifier implements SqlSessionModifier {

	private int timeout;
	
	public void modifySqlSession(SimpleJdbcTemplate simpleJdbcTemplate, QueryData queryData) {
		if (timeout > 0)
			simpleJdbcTemplate.update("SET statement_timeout TO " + timeout);
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

}
