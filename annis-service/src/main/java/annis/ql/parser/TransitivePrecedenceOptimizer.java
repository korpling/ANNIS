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

import annis.model.Join;
import annis.model.QueryNode;
import annis.model.QueryNode.Range;
import annis.sqlgen.model.Precedence;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
 * restrictive than a relation with more relations. Still it is assumed that
 * "normal" AQL queries will satisfiy this condition. And in the end, even
 * a "is after this token somewhere in the text" condition is a huge improvement.
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class TransitivePrecedenceOptimizer implements QueryDataTransformer
{
  
  public TransitivePrecedenceOptimizer()
  {

  }
  @Override
  public QueryData transform(QueryData data)
  {
    // initialize helper variables
    HashSet<Long> visitedNodes = new HashSet<>();
    

    for(List<QueryNode> alternative : data.getAlternatives())
    {
      Map<Long, Set<Precedence>> outJoins = createInitialJoinMap(alternative);
      
      for(QueryNode node : alternative)
      {
        Set<String> segmentations = getAllSegmentations(node);
        
        visitedNodes.clear();
        // we apply the algorithm node by node
        // tok == null segmentation
        propagateNodePrecedence(node, node, visitedNodes, outJoins, null, null);
        for(String s : segmentations)
        {
          propagateNodePrecedence(node, node, visitedNodes, outJoins, null, s);
        }
      }
    }
    
    return data;
  
  }
  
  private Set<String> getAllSegmentations(QueryNode node)
  {
    Set<String> result = new TreeSet<>();
    
    for(Join j : node.getOutgoingJoins())
    {
      if(j instanceof Precedence)
      {
        Precedence p = (Precedence) j;
        if(p.getSegmentationName() != null)
        {
          result.add(p.getSegmentationName());
        }
      }
    }
    
    return result;
  }
  
  private Map<Long, Set<Precedence>> createInitialJoinMap(List<QueryNode> alternative)
  {
    Map<Long, Set<Precedence>> result = new HashMap<>();
    
    for(QueryNode node : alternative)
    {
      Set<Precedence> joinList = new HashSet<>();
      
      for(Join j : node.getOutgoingJoins())
      {
        if(j instanceof Precedence)
        {
          joinList.add((Precedence) j);
        }
      }
      
      result.put(node.getId(), joinList);
    }
    
    return result;
  }
  
  private void propagateNodePrecedence(QueryNode initialNode, 
    QueryNode currentNode, Set<Long> visitedNodes,
    Map<Long, Set<Precedence>> outJoins,
    Range range, String segmentation)
  {
    visitedNodes.add(currentNode.getId());
    
    Map<QueryNode, Range> nextNodes = new HashMap<>();
    
    // iterator over all outgoing precedence joins
    List<Join> originalJoins = new LinkedList<>(currentNode.getOutgoingJoins());
    for(Join join : originalJoins)
    {
      if(join instanceof Precedence)
      { 
        Precedence p = (Precedence) join;
        if((segmentation == null && p.getSegmentationName() == null) 
          || (segmentation != null  && segmentation.equals(p.getSegmentationName())) )
        {
          Range newRange;
    
          if(range == null)
          {
            // create a new range at initial node
            newRange = new Range(p.getMinDistance(), p.getMaxDistance());
          }
          else
          {
            // calculate the new range depending on old one
            if(
              currentNode.isToken() == false
              || (range.getMin() == 0 && range.getMax() == 0) 
              || (p.getMinDistance() == 0 && p.getMaxDistance() == 0))
            {
              // use unlimited range since 
              // a) the node could also be a 
              //    span covering more than one token, 
              // b) the original constraint is an unlimited range
              newRange = new Range(0, 0);
            }
            else
            {
              // add the new precendence values to the old one
              newRange = new Range(range.getMin() + p.getMinDistance(), 
                range.getMax() + p.getMaxDistance());
            }
          }

          // put the target node in the list of nodes to check if not visited yet
          if(!visitedNodes.contains(p.getTarget().getId()))
          {
            nextNodes.put(p.getTarget(), newRange);

            Precedence newJoin = new Precedence(p.getTarget(), newRange.getMin(),
              newRange.getMax());
            Set<Precedence> existingJoins = outJoins.get(initialNode.getId());
            // only add if this join is not already included 
            // (which is always true for the initial node)
            // and the join is more restrictive than any previous one
            boolean moreRestrictive = true;
            for (Precedence oldJoin : existingJoins)
            {
              if(oldJoin.getTarget() == newJoin.getTarget())
              {
                if (!joinMoreRestrictive(oldJoin, newJoin))
                {
                  moreRestrictive = false;
                  break;
                }
              }
            }
            if (moreRestrictive)
            {
              // add newly created discovered transitive precedence
              initialNode.addOutgoingJoin(newJoin);
              existingJoins.add(newJoin);
            }

          } // end if not visited yet
        } // end if segmentation matches
      } // end if is precedence join
    } // end for each join
    
    for(Map.Entry<QueryNode, Range> e : nextNodes.entrySet())
    {
      // call us recursivly but remember the range
      propagateNodePrecedence(initialNode, e.getKey(), visitedNodes, outJoins, 
        e.getValue(), segmentation);
    }
  }
  
  private boolean joinMoreRestrictive(Precedence joinOld, Precedence joinNew)
  {
    // the new one is an unlimited indirect join which can never be better than
    // the original one
    if(joinNew.getMinDistance() == 0 && joinNew.getMaxDistance() == 0)
    {
      return false;
    }
    
    // both values are worse than the old one
    if(joinNew.getMaxDistance() >= joinOld.getMaxDistance() 
      && joinNew.getMinDistance() <= joinOld.getMinDistance())
    {
      return false;
    }
    
    // difference is less than the old one
    if((joinOld.getMaxDistance() - joinOld.getMinDistance()) 
      < (joinNew.getMaxDistance() - joinNew.getMinDistance()) )
    {
      return false;
    }
    
    return true;
  }
  
}
