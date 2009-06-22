package annis.sqlgen;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import annis.ql.parser.QueryData;

public interface SqlSessionModifier {

	void modifySqlSession(SimpleJdbcTemplate simpleJdbcTemplate, QueryData queryData);
	
}
