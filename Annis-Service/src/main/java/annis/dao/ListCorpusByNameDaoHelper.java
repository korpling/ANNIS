package annis.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;

public class ListCorpusByNameDaoHelper extends ParameterizedSingleColumnRowMapper<Long> {

	public String createSql(List<String> corpusNames) {
		Validate.notEmpty(corpusNames, "Need at least one corpus name");
		
		// turn corpus names into sql strings (enclosed with ')
		List<String> corpusNamesSqlStrings = new ArrayList<String>();
		for (String corpus : corpusNames) {
			corpusNamesSqlStrings.add("'" + corpus + "'");
		}
		
		// build sql query
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT id FROM corpus WHERE name IN ( ");
		sb.append(StringUtils.join(corpusNamesSqlStrings, ", "));
		sb.append(" ) AND top_level = 't'");
		return sb.toString();
	}
	
}
