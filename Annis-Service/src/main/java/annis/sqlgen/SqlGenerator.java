package annis.sqlgen;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.ql.parser.QueryAnalysis;
import annis.ql.parser.QueryData;
import de.deutschdiachrondigital.dddquery.node.PExpr;
import de.deutschdiachrondigital.dddquery.node.Start;


public class SqlGenerator {

	private Logger log = Logger.getLogger(this.getClass());
	
	// dependencies
	private ClauseSqlGenerator clauseSqlGenerator;
	private QueryAnalysis queryAnalysis;
	
	public String toSql(Start statement, List<Long> corpusList) {

		// analyze the statement
		QueryData queryData = queryAnalysis.analyzeQuery(statement, corpusList);
		
		// build SQL query
		List<String> subQueries = new ArrayList<String>();
		for (List<AnnisNode> alternative : queryData.getAlternatives()) {
			String clauseSql = clauseSqlGenerator.toSql(alternative, queryData.getMaxWidth(), corpusList, queryData.getMetaData());
			subQueries.add(clauseSql);
		}
		String sql = StringUtils.join(subQueries, "\n\nUNION ");
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

	public QueryAnalysis getQueryAnalysis() {
		return queryAnalysis;
	}

	public void setQueryAnalysis(QueryAnalysis queryAnalysis) {
		this.queryAnalysis = queryAnalysis;
	}

}
