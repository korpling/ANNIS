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
import annis.model.AqlParseError;
import annis.model.Join;
import annis.model.QueryNode;
import annis.sqlgen.model.Inclusion;
import annis.sqlgen.model.LeftAlignment;
import annis.sqlgen.model.LeftOverlap;
import annis.sqlgen.model.NonBindingJoin;
import annis.sqlgen.model.Overlap;
import annis.sqlgen.model.RightAlignment;
import annis.sqlgen.model.RightOverlap;
import annis.sqlgen.model.SameSpan;
import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Performs semantic checks on the parsed query.
 *
 * <ul>
 * <li>checks if there is no search expression at all</li>
 * <li>no binary linguistic relations are allowed if there is only one node</li> 
 * <li>checks if all nodes of an alternative are connected</li>
 * <li>every node variable name should be given only once in an alternative</li>
 * </ul>
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class SemanticValidator implements QueryDataTransformer
{

  @Override
  public QueryData transform(QueryData data)
  {
    int i = 1;
    for (List<QueryNode> alternative : data.getAlternatives())
    {
      checkAlternative(data, alternative, i++, data.getAlternatives().size() > 1);
    }
    // we actually don't manipulate the output
    return data;
  }

  public void checkAlternative(QueryData data, List<QueryNode> alternative, int alternativeIndex, boolean queryWasNormalized)
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
      for (Join j : n.getOutgoingJoins())
      {
        if (j.getTarget() != null)
        {
          throw new AnnisQLSemanticsException(j.getParseLocation(),
            "No binary linguistic relations allowed if there is only one node in query.");
        }
      }
    }
    
    // get all nodes connected to the first one
    Multimap<Long, QueryNode> connected = calculateConnected(alternative);
    Set<Long> transitiveHull = new HashSet<>();
    transitiveHull.add(alternative.get(0).getId());
    createTransitiveHull(alternative.get(0), 
      connected, transitiveHull);
    
    Multiset<String> variableNames = TreeMultiset.create();
   
    Set<Long> unconnectedNodes = new HashSet<>();
    for(QueryNode n : alternative)
    {
      unconnectedNodes.add(n.getId());
      variableNames.add(n.getVariable());
    }
    unconnectedNodes.removeAll(transitiveHull);
   
    // check if each node is contained in the connected nodes
    if (!unconnectedNodes.isEmpty())
    {
      List<AqlParseError> errors = new LinkedList<>();
      
      for (QueryNode n : alternative)
      {
        if(unconnectedNodes.contains(n.getId()))
        {
          errors.add(new AqlParseError(n, "variable \""
            + n.getVariable() + "\" not bound (use linguistic operators)"));

        }
      }
      
      if(!errors.isEmpty())
      {
        if(queryWasNormalized)
        {
          // add the normalized query as "error" so the user is able to see it
          errors.add(new AqlParseError("Normalized query is: \n"
          + data.toAQL()));
        }
        
        throw new AnnisQLSemanticsException("Not all variables bound", errors);
      }
    }
    
    // check if any variable name was given more than once
    List<String> invalidNames = new LinkedList<>();
    for(Multiset.Entry<String> e : variableNames.entrySet())
    {
      if(e.getCount() > 1)
      {
        invalidNames.add(e.getElement());
      }
    }
    if(!invalidNames.isEmpty())
    {
      throw new AnnisQLSemanticsException("The following variable names are "
        + "used for more than one node: " + Joiner.on(", ").join(invalidNames)
        + "\nNormalized Query is: \n"
        + data.toAQL());
    }
    
    // check no non-reflexive operator is used with the same operands
    for(QueryNode source : alternative)
    {
      for(Join join : source.getOutgoingJoins())
      {
        if(join instanceof Inclusion || join instanceof SameSpan
          || join instanceof  Overlap || join instanceof RightOverlap || join instanceof LeftOverlap 
          || join instanceof RightAlignment || join instanceof LeftAlignment)
        {
          if(source.equals(join.getTarget()))
          {
            throw new AnnisQLSemanticsException(join, "Not-reflexive operator used with the same node as argument.");
          }
        }
      }
    }
  }
  
  private Multimap<Long, QueryNode> calculateConnected(List<QueryNode> nodes)
  {
    Multimap<Long, QueryNode> result = HashMultimap.create();
    
    for(QueryNode n : nodes)
    {
      for(Join j : n.getOutgoingJoins())
      {
        if(j.getTarget() != null && !(j instanceof NonBindingJoin))
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
