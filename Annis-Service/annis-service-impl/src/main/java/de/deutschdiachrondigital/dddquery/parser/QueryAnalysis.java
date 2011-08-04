/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
