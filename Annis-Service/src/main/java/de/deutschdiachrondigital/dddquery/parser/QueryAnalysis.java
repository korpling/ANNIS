package de.deutschdiachrondigital.dddquery.parser;

import annis.ql.parser.QueryData;
import java.util.List;

import org.apache.log4j.Logger;

import de.deutschdiachrondigital.dddquery.parser.ClauseAnalysis;
import de.deutschdiachrondigital.dddquery.parser.DnfTransformer;
import de.deutschdiachrondigital.dddquery.node.PExpr;
import de.deutschdiachrondigital.dddquery.node.Start;
import java.util.LinkedList;

public class QueryAnalysis {

	private Logger log = Logger.getLogger(this.getClass());
	
	private DnfTransformer dnfTransformer;
	private ClauseAnalysis clauseAnalysis;
	
	public QueryData analyzeQuery(Start statement, List<Long> corpusList) {
		QueryData queryData = new QueryData();
		
		// save the corpus list
		queryData.setCorpusList(corpusList);
		
    List<PExpr> clauses = new LinkedList<PExpr>();

		// split statement into list of clauses

    statement.apply(dnfTransformer);
    clauses = dnfTransformer.listClauses(statement);

		log.debug(clauses.size() + " clause(s) in statement");
		
		// analyze each clause independently
		for (PExpr clause : clauses) {
			// get a fresh clause analyzer from Spring
			ClauseAnalysis clauseAnalysis = getClauseAnalysis();
			clause.apply(clauseAnalysis);
			
			// save nodes and update column width
			queryData.addAlternative(clauseAnalysis.getNodes());
			queryData.setMaxWidth(Math.max(queryData.getMaxWidth(), clauseAnalysis.nodesCount()));
			
			// collect meta data
			queryData.addMetaAnnotations(clauseAnalysis.getMetaAnnotations());
		}
		log.debug("maximum column width is " + queryData.getMaxWidth());
		
		return queryData;
	}

	public DnfTransformer getDnfTransformer() {
		return dnfTransformer;
	}

	public void setDnfTransformer(DnfTransformer dnfTransformer) {
		this.dnfTransformer = dnfTransformer;
	}

	public ClauseAnalysis getClauseAnalysis() {
		return clauseAnalysis;
	}

	public void setClauseAnalysis(ClauseAnalysis clauseAnalysis) {
		this.clauseAnalysis = clauseAnalysis;
	}

	
	
}
