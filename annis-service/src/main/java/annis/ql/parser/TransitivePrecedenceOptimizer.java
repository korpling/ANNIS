/*
 * Copyright 2009-2012 Collaborative Research Centre SFB 632 
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
import annis.model.QueryNode.Range;
import annis.sqlgen.model.Join;
import annis.sqlgen.model.Precedence;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Extends precedence relations to other nodes that are only transitivly connected.
 * 
 * The algorithm calculates the reachability graph for each node of the query
 * (as defined by the precedence operator) and inherits and extends the precedence
 * property to the nodes connected with this node. The Goal is to preserve as
 * much restrictive information as possible. 
 * 
 * Breadth-first search is used in order to find the shortest precedence 
 * relation between nodes . This is just an approximation since beeing near in 
 * the reachability graph does not necessary mean the relation is more 
 * restrictive than a relation with more edges. Still it is assumed that
 * "normal" AQL queries will satisfiy this condition. And in the end, even
 * a "is after this token somewhere in the text" condition is a huge improvement.
 * 
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class TransitivePrecedenceOptimizer implements QueryDataTransformer
{
  
  public TransitivePrecedenceOptimizer()
  {

  }
  @Override
  public QueryData transform(QueryData data)
  {
    
    
    for(List<QueryNode> alternative : data.getAlternatives())
    {
      // initialize helper variables
      HashSet<Long> visitedNodes = new HashSet<Long>();
      
      for(QueryNode node : alternative)
      {
        // we apply the algorithm node by node
        propagateNodePrecedence(node, node, visitedNodes, null);
      }
    }
    
    return data;
  
  }
  
  private void propagateNodePrecedence(QueryNode initialNode, QueryNode currentNode, Set<Long> visitedNodes, Range range)
  {
    visitedNodes.add(currentNode.getId());
    
    Map<QueryNode, Range> nextNodes = new HashMap<QueryNode, Range>();
    
    // iterator over all outgoing precedence joins
    List<Join> originalJoins = new LinkedList<Join>(currentNode.getJoins());
    for(Join join : originalJoins)
    {
      if(join instanceof Precedence)
      {
        Range newRange;
    
        Precedence p = (Precedence) join;
        if(range == null)
        {
          // create a new range at initial node
          newRange = new Range(p.getMinDistance(), p.getMaxDistance());
        }
        else
        {
          // calculate the new range depending on old one
          if((range.getMin() == 0 && range.getMax() == 0) 
            || (p.getMinDistance() == 0 && p.getMaxDistance() == 0))
          {
            // unlimited range, nothing to calculate
            newRange = new Range(0, 0);
          }
          else
          {
            // add the new precendence values to the old one
            newRange = new Range(range.getMin() + p.getMinDistance(), range.getMax() + p.getMaxDistance());
          }
        }
        
        // only add if this join is not already included 
        // (which is always true for the initial node)
        if(initialNode != currentNode)
        {
          // add newly created discovered transitive precedence
          initialNode.addJoin(new Precedence(p.getTarget(), newRange.getMin(), newRange.getMax()));
        }
        
        // only follow new path if the range is more restrictive
        boolean add = true;
        Range existingRange = nextNodes.get(p.getTarget());
        if(existingRange != null)
        {
          add = false;
          if(existingRange.getMin() != 0 && existingRange.getMax() != 0)
          {
            add = true;
          }
        }
        
        if(add)
        {
          nextNodes.put(p.getTarget(), newRange);
        }
      } // end if is precedence join
    } // end for each join
    
    for(Map.Entry<QueryNode, Range> e : nextNodes.entrySet())
    {
      // call us recursivly but remember the range
      if(!visitedNodes.contains(e.getKey().getId()))
      {
        propagateNodePrecedence(initialNode, e.getKey(), visitedNodes, e.getValue());
      }
    }
  }
  
  
}
