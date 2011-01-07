/*
 *  Copyright 2010 thomas.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package annis.ql.parser;

import annis.model.AnnisNode;
import annis.ql.node.PExpr;
import annis.ql.node.Start;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author thomas
 */
public class QueryAnalysis
{
  private final Logger log = Logger.getLogger(QueryAnalysis.class);

  private DnfTransformer dnfTransformer;
  private ClauseAnalysis clauseAnalysis;

  public QueryData analyzeQuery(Start statement, List<Long> corpusList)
  {
    QueryData queryData = new QueryData();
    
    queryData.setCorpusList(corpusList);
		
    List<PExpr> clauses = new LinkedList<PExpr>();

		// split statement into list of clauses

    statement.apply(dnfTransformer);
    clauses = dnfTransformer.listClauses(statement);

		log.debug(clauses.size() + " clause(s) in statement");
		
		// analyze each clause independently
		for (PExpr clause : clauses)
    {
      NodeRelationNormalizer nodeRelationNormalizer = new NodeRelationNormalizer();
      clause.apply(nodeRelationNormalizer);

			// get a fresh clause analyzer from Spring
			ClauseAnalysis myClauseAnalysis = getClauseAnalysis();
			clause.apply(myClauseAnalysis);
			
			// save nodes and update column width
			queryData.addAlternative(new LinkedList<AnnisNode>(myClauseAnalysis.getNodes()));
			queryData.setMaxWidth(Math.max(queryData.getMaxWidth(), myClauseAnalysis.nodesCount()));
			
			// collect meta data
			queryData.addMetaAnnotations(myClauseAnalysis.getMetaAnnotations());
		}
		log.debug("maximum column width is " + queryData.getMaxWidth());
		
    return queryData;
  }

  public DnfTransformer getDnfTransformer()
  {
    return dnfTransformer;
  }

  public void setDnfTransformer(DnfTransformer dnfTransformer)
  {
    this.dnfTransformer = dnfTransformer;
  }

  public ClauseAnalysis getClauseAnalysis()
  {
    return clauseAnalysis;
  }

  public void setClauseAnalysis(ClauseAnalysis clauseAnalysis)
  {
    this.clauseAnalysis = clauseAnalysis;
  }


}
