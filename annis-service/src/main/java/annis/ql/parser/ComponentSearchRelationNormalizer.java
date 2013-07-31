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

import annis.model.QueryNode;
import annis.sqlgen.model.Dominance;
import annis.sqlgen.model.Identical;
import annis.sqlgen.model.Join;
import annis.sqlgen.model.PointingRelation;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import java.util.ArrayList;
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
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class ComponentSearchRelationNormalizer implements QueryDataTransformer
{

  @Override
  public QueryData transform(QueryData data)
  {
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
      while(checkForViolation(alternative, maxID))
      {
        // repeat
      }
      data.setMaxWidth(Math.max(data.getMaxWidth(), alternative.size()));
    }

    return data;
  }

  private boolean checkForViolation(List<QueryNode> nodes, AtomicLong maxID)
  {
    Multimap<QueryNode, Join> joins = createJoinMap(nodes);

    LinkedList<QueryNode> nodeCopy = new LinkedList<QueryNode>(nodes);
    
    for(QueryNode n : nodeCopy)
    {
      if(joins.get(n).size() > 1)
      {
        // it is computational easier to replicate the a known target node, thus
        // search for a join where our current node is the target node
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
  
  private void replicateFromJoinTarget(Join join, QueryNode node, 
    List<QueryNode> nodes, AtomicLong maxID)
  {
    QueryNode newNode = new QueryNode(maxID.incrementAndGet(), node); 
    newNode.setVariable(newNode.getVariable() + "'");
    
    join.setTarget(newNode);
    
    Identical identJoin = new Identical(newNode);
    node.addJoin(identJoin);
    
    nodes.add(newNode);
  }
  
  private void replicateFromJoinSource(Join join, QueryNode node,
    List<QueryNode> nodes, AtomicLong maxID)
  {
    Preconditions.checkState(node.getJoins().remove(join), "The join was not attached to the source node.");
    
    QueryNode newNode = new QueryNode(maxID.incrementAndGet(), node);
    newNode.setVariable(newNode.getVariable() + "'");
    newNode.addJoin(join);
    
    Identical identJoin = new Identical(newNode);
    node.addJoin(identJoin);
    
    nodes.add(node);
  }
  
  private QueryNode searchSourceNode(Join j, List<QueryNode> nodes)
  {
    for(QueryNode n : nodes)
    {
      if(n.getJoins().contains(j))
      {
        return n;
      }
    }
    return null;
  }

  private Multimap<QueryNode, Join> createJoinMap(List<QueryNode> nodes)
  {
    Multimap<QueryNode, Join> result = HashMultimap.create();
    for (QueryNode n : nodes)
    {
      for (Join j : n.getJoins())
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
