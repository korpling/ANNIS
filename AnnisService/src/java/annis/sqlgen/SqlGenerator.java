package annis.sqlgen;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import annis.dao.CorpusSelectionStrategy;
import de.deutschdiachrondigital.dddquery.node.PExpr;
import de.deutschdiachrondigital.dddquery.node.Start;


public class SqlGenerator {

	private Logger log = Logger.getLogger(this.getClass());
	
	// dependencies
	private DnfTransformer disjunctiveNormalForm;
	private ClauseAnalysis clauseAnalysis;
	private ClauseSqlAdapter clauseSqlAdapter;
	
	public String toSql(Start statement, CorpusSelectionStrategy corpusSelectionStrategy, SelectClauseSqlAdapter selectClauseSqlAdapter) {
		// split statement into list of clauses
		statement.apply(disjunctiveNormalForm);
		List<PExpr> clauses = disjunctiveNormalForm.listClauses(statement);
		List<ClauseAnalysis> analysis = new ArrayList<ClauseAnalysis>();
		log.debug(analysis.size() + " clause(s) in statement");
		
		// analyze each clause independently
		int maxWidth = 0;
		for (PExpr clause : clauses) {
			// get a fresh clause analyzer from Spring
			ClauseAnalysis clauseAnalysis = getClauseAnalysis();
			clause.apply(clauseAnalysis);
			
			// save analysis and update column width
			analysis.add(clauseAnalysis);
			maxWidth = Math.max(maxWidth, clauseAnalysis.nodesCount());
		}
		log.debug("maximum column width is " + maxWidth);
		
		// build SQL query
		List<String> subQueries = new ArrayList<String>();
		for (ClauseAnalysis clauseAnalysis : analysis) {
			corpusSelectionStrategy.addMetaAnnotations(clauseAnalysis.getMetaAnnotations());
			String clauseSql = clauseSqlAdapter.toSql(clauseAnalysis.getNodes(), maxWidth, corpusSelectionStrategy, selectClauseSqlAdapter);
			subQueries.add(clauseSql);
		}
		String sql = StringUtils.join(subQueries, "\n\nUNION ");
		log.info("SQL:\n" + sql);

		return sql;
	}

	///// Getter / Setter
	
	public void setDisjunctiveNormalForm(DnfTransformer dnfTransformer) {
		this.disjunctiveNormalForm = dnfTransformer;
	}

	public DnfTransformer getDisjunctiveNormalForm() {
		return disjunctiveNormalForm;
	}

	public void setClauseAnalysis(ClauseAnalysis clauseAnalyzer) {
		this.clauseAnalysis = clauseAnalyzer;
	}

	public ClauseAnalysis getClauseAnalysis() {
		return clauseAnalysis;
	}

	public ClauseSqlAdapter getClauseSqlAdapter() {
		return clauseSqlAdapter;
	}

	public void setClauseSqlAdapter(ClauseSqlAdapter clauseSqlAdapter) {
		this.clauseSqlAdapter = clauseSqlAdapter;
	}

}
