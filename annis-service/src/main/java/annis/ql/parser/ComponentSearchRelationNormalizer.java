/*
 * Copyright 2013 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.ql.parser;

import annis.model.Join;
import annis.model.QueryNode;
import annis.sqlgen.model.Dominance;
import annis.sqlgen.model.Identical;
import annis.sqlgen.model.PointingRelation;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Fix queries where a node is contained in two linguistic relations that could
 * query different components.
 *
 * The normalization is needed since our SQL generation does not work well on
 * database tuples from different components which represent the same node. E.g.
 * a query like
 * <pre>
 * node & node & #1 > #2 & #1 ->dep #2
 * </pre> must be normalized to
 * <pre>
 * node & node & node & node
 * & #1 > #2 & #3 ->dep #4
 * & #1 = #3 & #2 = #4
 * </pre> in order to actually get the results the user expects.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ComponentSearchRelationNormalizer implements QueryDataTransformer
{

  @Override
  public QueryData transform(QueryData data)
  {
    String originalAQL = data.toAQL();
    AtomicLong maxID = new AtomicLong();
    for (List<QueryNode> alternative : data.getAlternatives())
    {
      for(QueryNode n : alternative)
      {
        maxID.set(Math.max(maxID.get(), n.getId()));
      }
    }
    for (List<QueryNode> alternative : data.getAlternatives())
    {
      int disasterCounter = 0;
      while(checkForViolation(alternative, maxID))
      {
        // repeat
        disasterCounter++;
        Preconditions.checkArgument(disasterCounter < 5000, 
          "Possible endless loop in component search relation normalization for query " + originalAQL);
      }
      data.setMaxWidth(Math.max(data.getMaxWidth(), alternative.size()));
    }

    return data;
  }

  private boolean checkForViolation(List<QueryNode> nodes, AtomicLong maxID)
  {
    Multimap<QueryNode, Join> joins = createJoinMap(nodes);

    LinkedList<QueryNode> nodeCopy = new LinkedList<>(nodes);
    
    for(QueryNode n : nodeCopy)
    {
      if(joins.get(n).size() > 1)
      {
        Iterator<Join> itJoinsForNode = joins.get(n).iterator();
        Join joinToSplit = itJoinsForNode.next();
        
        if(joinToSplit.getTarget().getId() == n.getId())
        {
          replicateFromJoinTarget(joinToSplit, n, nodes, maxID);
        }
        else
        {
          replicateFromJoinSource(joinToSplit, n, nodes, maxID);
        }
        
        return true;
      }
    }

    return false;
  }
  
  private void replicateFromJoinTarget(Join join, QueryNode targetNode, 
    List<QueryNode> nodes, AtomicLong maxID)
  {
    
    QueryNode newTargetNode = new QueryNode(maxID.incrementAndGet(), targetNode, false); 
    newTargetNode.setArtificial(true);
    newTargetNode.setVariable("x" + newTargetNode.getId() + "(" + targetNode.getVariable() + ")");
    
    newTargetNode.setThisNodeAsTarget(join);
    
    Identical identJoin = new Identical(newTargetNode);
    targetNode.addOutgoingJoin(identJoin);
    
    nodes.add(newTargetNode);
  }
  
  private void replicateFromJoinSource(Join join, QueryNode sourceNode,
    List<QueryNode> nodes, AtomicLong maxID)
  {
    Preconditions.checkState(sourceNode.removeOutgoingJoin(join), "The join was not attached to the source node.");
    
    QueryNode newNode = new QueryNode(maxID.incrementAndGet(), sourceNode, false);
    newNode.setVariable("x" + newNode.getId() + "(" + sourceNode.getVariable() + ")");
    newNode.addOutgoingJoin(join);
    newNode.setArtificial(true);
    
    Identical identJoin = new Identical(newNode);
    sourceNode.addOutgoingJoin(identJoin);
    
    nodes.add(newNode);
  }
  
  private Multimap<QueryNode, Join> createJoinMap(List<QueryNode> nodes)
  {
    Multimap<QueryNode, Join> result = HashMultimap.create();
    for (QueryNode n : nodes)
    {
      for (Join j : n.getOutgoingJoins())
      {
        if (j instanceof Dominance || j instanceof PointingRelation)
        {
          if (j.getTarget() != null)
          {
            result.put(n, j);
            result.put(j.getTarget(), j);
          }
        }
      }
    }
    return result;
  }

}
