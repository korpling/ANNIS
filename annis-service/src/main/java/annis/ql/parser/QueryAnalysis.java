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
package annis.ql.parser;

import annis.model.QueryNode;
import annis.ql.node.PExpr;
import annis.ql.node.Start;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author thomas
 */
public class QueryAnalysis
{
  // logging
  private final static Logger log = LoggerFactory.getLogger(QueryAnalysis.class);

  // use each node in exactly one edge relation
  private boolean normalizeNodesInEdgeRelations;
  
  private DnfTransformer dummyDnfTransformer;
  private ClauseAnalysis dummyClauseAnalysis;
  private NodeRelationNormalizer nodeRelationNormalizer;

  public QueryData analyzeQuery(Start statement, List<Long> corpusList)
  {
    QueryData queryData = new QueryData();
    
    queryData.setCorpusList(new ArrayList<Long>(corpusList));
		
		// split statement into list of clauses

    DnfTransformer dnfTransformer = getDnfTransformer();
    statement.apply(dnfTransformer);
    List<PExpr> clauses = dnfTransformer.listClauses(statement);

		log.debug(clauses.size() + " clause(s) in statement");
		
		// analyze each clause independently
		for (PExpr clause : clauses)
    {
		  if (normalizeNodesInEdgeRelations)
		  {
		    NodeRelationNormalizer nodeRelationNormalizer = getNodeRelationNormalizer();
		    clause.apply(nodeRelationNormalizer);
		  }

			// get a fresh clause analyzer from Spring
			ClauseAnalysis clauseAnalysis = getClauseAnalysis();
			clause.apply(clauseAnalysis);
			
			// save nodes and update column width
			queryData.addAlternative(new LinkedList<QueryNode>(clauseAnalysis.getNodes()));
			queryData.setMaxWidth(Math.max(queryData.getMaxWidth(), clauseAnalysis.nodesCount()));
			
			// collect meta data
			queryData.addMetaAnnotations(clauseAnalysis.getMetaAnnotations());
		}
		log.debug("maximum column width is " + queryData.getMaxWidth());
		
    return queryData;
  }

  public DnfTransformer getDnfTransformer()
  {
    return dummyDnfTransformer;
  }

  public void setDnfTransformer(DnfTransformer dnfTransformer)
  {
    this.dummyDnfTransformer = dnfTransformer;
  }

  public ClauseAnalysis getClauseAnalysis()
  {
    return dummyClauseAnalysis;
  }

  public void setClauseAnalysis(ClauseAnalysis clauseAnalysis)
  {
    this.dummyClauseAnalysis = clauseAnalysis;
  }

  public NodeRelationNormalizer getNodeRelationNormalizer()
  {
    return nodeRelationNormalizer;
  }

  public void setNodeRelationNormalizer(
      NodeRelationNormalizer nodeRelationNormalizer)
  {
    this.nodeRelationNormalizer = nodeRelationNormalizer;
  }

  public boolean isNormalizeNodesInEdgeRelations()
  {
    return normalizeNodesInEdgeRelations;
  }

  public void setNormalizeNodesInEdgeRelations(boolean normalizeNodesInEdgeRelations)
  {
    this.normalizeNodesInEdgeRelations = normalizeNodesInEdgeRelations;
  }


}
