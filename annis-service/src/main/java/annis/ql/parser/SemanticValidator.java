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

import annis.exceptions.AnnisQLSemanticsException;
import annis.model.QueryNode;
import annis.sqlgen.model.Join;
import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Performs semantic checks on the parsed query.
 *
 * - checks if there is no search expression at all - no binary linguistic
 * relations are allowed if there is only one node - checks if all nodes of an
 * alternative are connected (TODO)
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class SemanticValidator implements QueryDataTransformer
{

  @Override
  public QueryData transform(QueryData data)
  {
    int i = 1;
    for (List<QueryNode> alternative : data.getAlternatives())
    {
      checkAlternative(data, alternative, i++);
    }
    // we actually don't manipulate the output
    return data;
  }

  public void checkAlternative(QueryData data, List<QueryNode> alternative, int alternativeIndex)
  {
    // check if there is at least one search expression
    if (alternative.isEmpty())
    {
      throw new AnnisQLSemanticsException("Missing search expression.");
    }

    // there are not linguistic binary relations allowed if there is only one node
    if (alternative.size() == 1)
    {
      QueryNode n = alternative.get(0);
      for (Join j : n.getJoins())
      {
        if (j.getTarget() != null)
        {
          throw new AnnisQLSemanticsException(
            "No binary linguistic relations allowed if there is only one node in query.");
        }
      }
    }
    
    // get all nodes connected to the first one
    Multimap<Long, QueryNode> connected = calculateConnected(alternative);
    Set<Long> transitiveHull = new HashSet<Long>();
    transitiveHull.add(alternative.get(0).getId());
    createTransitiveHull(alternative.get(0), 
      connected, transitiveHull);
   
    Set<Long> unconnectedNodes = new HashSet<Long>();
    for(QueryNode n : alternative)
    {
      unconnectedNodes.add(n.getId());
    }
    unconnectedNodes.removeAll(transitiveHull);
   
    // check if each node is contained in the connected nodes
    if (!unconnectedNodes.isEmpty())
    {
      
      List<String> variables = new LinkedList<String>();
      for (QueryNode n : alternative)
      {
        if(unconnectedNodes.contains(n.getId()))
        {
          variables.add(n.getVariable());
        }
      }
      
      if(alternative.size() == 1)
      {
        throw new AnnisQLSemanticsException("Variable(s) ["
          + Joiner.on(",").join(variables)
          + "] not bound (use linguistic operators).");
      }
      else
      {
        throw new AnnisQLSemanticsException("Variable(s) ["
          + Joiner.on(",").join(variables)
          + "] not bound in alternative "
          + alternativeIndex + "(use linguistic operators). "
          + "Normalized query is: \n"
          + data.toAQL());
      }
    }
  }
  
  private Multimap<Long, QueryNode> calculateConnected(List<QueryNode> nodes)
  {
    Multimap<Long, QueryNode> result = HashMultimap.create();
    
    for(QueryNode n : nodes)
    {
      for(Join j : n.getJoins())
      {
        if(j.getTarget() != null)
        {
          long left = n.getId();
          long right = j.getTarget().getId();
          
          result.put(left, j.getTarget());
          result.put(right, n);
        }
      }
    }
    
    return result;
  }
  
  private void createTransitiveHull(QueryNode n, 
    final Multimap<Long, QueryNode> connected, 
    Set<Long> transitiveHull)
  {
    Collection<QueryNode> outgoing = connected.get(n.getId());
    if(outgoing != null)
    {
      for(QueryNode otherNode : outgoing)
      {
        if(transitiveHull.add(otherNode.getId()))
        {
          createTransitiveHull(otherNode, connected, transitiveHull);
        }
      }
    }
  }
}
