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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import java.util.ArrayList;
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
    }

    return data;
  }

  private boolean checkForViolation(List<QueryNode> nodes, AtomicLong maxID)
  {
    Multimap<QueryNode, Join> joins = createJoinMap(nodes);

    Multiset<QueryNode> keys = joins.keys();
//    for (QueryNode source : keys)
//    {
//      if (keys.count(source) > 1)
//      {
//        ArrayList<Join> joinsForKey = new ArrayList<Join>(joins.get(source));
//        Join joinToSplit = joinsForKey.get(0);
        
        // it is easier to replicate the target nodes, thus
        // search for a join where our current node
        
        //TODO:
//        while(joinToSplit.getTarget() != )
//        
//        QueryNode target = joinToCopy.getTarget();
//        
//        if (target != source)
//        {
//          // remove the join that will be copied
//          source.getJoins().remove(joinToCopy);
//
//          QueryNode newSource = new QueryNode(maxID.incrementAndGet(),
//            source);
//          QueryNode newTarget = new QueryNode(maxID.incrementAndGet(),
//            target);
//
//          newSource.getJoins().clear();
//          newTarget.getJoins().clear();
//
//          // add the copied join to the new target
//          joinToCopy.setTarget(newTarget);
//          newSource.addJoin(joinToCopy);
//
//          // add additional joins that ensures the nodes are equal
//          Identical identSource = new Identical(newSource);
//          Identical identTarget = new Identical(newTarget);
//
//          source.addJoin(identSource);
//          target.addJoin(identTarget);
//
//          nodes.add(newSource);
//          nodes.add(newTarget);
//        } // end if target node is not the current node
//
//        return true;
//
//      }
//    }
    return false;
  }
  
  private void replicateTargetOfJoin(Join join, QueryNode target, 
    List<QueryNode> nodes, AtomicLong maxID)
  {
    QueryNode newTarget = new QueryNode(maxID.incrementAndGet(), target);    
    join.setTarget(newTarget);
    
    Identical identJoin = new Identical(newTarget);
    target.addJoin(identJoin);
    
    nodes.add(newTarget);
  }
  
  private void replicateSourceOfJoin(Join join, QueryNode source,
    List<QueryNode> nodes, AtomicLong maxID)
  {
    QueryNode newSource = new QueryNode(maxID.incrementAndGet(), source);
    source.getJoins().remove(join);
    newSource.addJoin(join);
    
    Identical identJoin = new Identical(newSource);
    source.addJoin(identJoin);
    
    nodes.add(newSource);
  }
  
  private QueryNode searchSourceNode(Join j, List<QueryNode> nodes)
  {
    for(QueryNode n : nodes)
    {
      if(n.getJoins().contains(j));
      return n;
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
