package annis.sqlgen;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import annis.model.AnnisNode;
import annis.ql.parser.QueryData;


public class SqlGenerator
{

	private Logger log = Logger.getLogger(this.getClass());
	
	// dependencies
	private ClauseSqlGenerator clauseSqlGenerator;
	
	public String toSql(QueryData queryData, List<Long> corpusList) {
		
		// build SQL query
		List<String> subQueries = new ArrayList<String>();
		for (List<AnnisNode> alternative : queryData.getAlternatives()) {
			String clauseSql = clauseSqlGenerator.toSql(alternative, queryData.getMaxWidth(), corpusList, queryData.getMetaData());
			subQueries.add(clauseSql);
		}
		String sql = StringUtils.join(subQueries, "\n\nUNION ALL");
		log.info("SQL:\n" + sql);

		return sql;
	}

	///// Getter / Setter
	
	public ClauseSqlGenerator getClauseSqlGenerator() {
		return clauseSqlGenerator;
	}

	public void setClauseSqlGenerator(ClauseSqlGenerator clauseSqlGenerator) {
		this.clauseSqlGenerator = clauseSqlGenerator;
	}

}
