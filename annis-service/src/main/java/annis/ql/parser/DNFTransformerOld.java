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

import annis.model.LogicClauseOld;
import annis.model.QueryNode;
import annis.sqlgen.model.Join;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * Utility functions for transforming an AQL query to the Disjunctive Normal Form.
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class DNFTransformerOld
{
  /**
   * Transforms an AQL query to the Disjunctive Normal Form.
   * @param topNode
   * @return 
   */
  public static void toDNF(LogicClauseOld topNode)
  {
    
    makeBinary(topNode);
    while(makeDNF(topNode) == false)
    {
      // do nothing, just repeat
    }
    cleanEmptyLeafs(topNode);
    flattenDNF(topNode);
    for(LogicClauseOld alternative : topNode.getChildren())
    {
      cleanRelationForAlternative(alternative);
    }
  }
  
  private static void makeBinary(LogicClauseOld node)
  {
    if(node.getOp() == LogicClauseOld.Operator.LEAF 
      || node.getChildren().isEmpty())
    {
      return;
    }
    
    // check if we have a degraded path with only one sibling
    LogicClauseOld parent = node.getParent();
    if(node.getChildren().size() == 1)
    {
      if(node.getParent() == null)
      {
        // replace this node with it's only child
        LogicClauseOld child = node.getChildren().get(0);
        node.clearChildren();
        
        node.setOp(child.getOp());
        node.setContent(child.getContent());
        
        for(LogicClauseOld newChild : child.getChildren())
        {
          node.addChild(newChild);
        }
      }
      else
      {
        // push this node up
        int idx = parent.getChildren().indexOf(node);
        parent.removeChild(idx);
        parent.addChild(0, node.getChildren().get(0));
      }
    }
    else if(node.getChildren().size() > 2)
    {
      LogicClauseOld firstChild = node.getChildren().get(0);
      // merge together under a new node
      LogicClauseOld newSubClause = new LogicClauseOld(node.getOp());
      
      ListIterator<LogicClauseOld> itChildren = node.getChildren().listIterator(1);
      while(itChildren.hasNext())
      {
        LogicClauseOld n = itChildren.next();
        newSubClause.addChild(n);
      }
      
      // rebuild the children
      node.clearChildren();
      node.addChild(firstChild);
      node.addChild(newSubClause);
    }
    
    // do the same thing for all children
    int cSize = node.getChildren().size();
    if(cSize >= 1)
    {
      makeBinary(node.getChildren().get(0));
    }
    
    if(cSize == 2)
    {
      makeBinary(node.getChildren().get(1));
    }    
  }
  

  /**
   * Iteration step to transform a {@link LogicClauseOld} into DNF.
   * 
   * In DNF all OR relations must be toplevel. Thus constructions like
   * 
   * <pre>
   *  AND             AND
   *  / \      or     / \
   * X  OR          OR   X
   *    / \        /  \
   *   Y   Z       Y  Z
   * </pre>
   * 
   * are illegal and will be replaced with
   * 
   * <pre>
   *       OR       
   *     /    \    
   *   AND    AND  
   *   / \    / \    
   *  X   Y  X   Z
   * </pre>
   * 
   * according to the distributivity rule of Boolean Algebra. We assume that
   * every input formula is already binary.
   * 
   * Only one transformation will be done in this function, repeat it
   * in order to replace all illegal constructions.
   * 
   * @param node The node to transform into DNF.
   * @return True if already in DNF
   */
  private static boolean makeDNF(LogicClauseOld node)
  {
    if(node.getOp() == LogicClauseOld.Operator.LEAF || node.getChildren().size() < 2)
    {
      return true;
    }
    
    LogicClauseOld left = node.getChildren().get(0);
    LogicClauseOld right = node.getChildren().get(1);
    if(node.getOp() == LogicClauseOld.Operator.AND)
    {
      // check if operator of this node and one of it's children is the same
      
      LogicClauseOld x1 = null;
      LogicClauseOld x2 = null;
      LogicClauseOld y = null;
      LogicClauseOld z = null;
      
      if(right.getOp() == LogicClauseOld.Operator.OR)
      {
        x1 = left;
        x2 = new LogicClauseOld(x1);
        if(x1.getContent() != null)
        {
          // set the content to a real copy
          x2.setContent(new QueryNode(x1.getContent()));
        }
        
        Preconditions.checkArgument(right.getChildren().size() == 2, 
          "OR nodes must always have exactly two children");
        y = right.getChildren().get(0);
        z = right.getChildren().get(1);
      }
      else if(left.getOp() == LogicClauseOld.Operator.OR)
      {
        x1 = right;
        x2 = new LogicClauseOld(x1);
        
        Preconditions.checkArgument(left.getChildren().size() == 2, 
          "OR nodes must always have exactly two children");
        y = left.getChildren().get(0);
        z = left.getChildren().get(1);
      }
      
      if(x1 != null && x2 != null && y != null && z != null)
      {
        node.setOp(LogicClauseOld.Operator.OR);
        node.setContent(null);
        node.clearChildren();
        
        LogicClauseOld leftParent = new LogicClauseOld(LogicClauseOld.Operator.AND);
        LogicClauseOld rightParent = new LogicClauseOld(LogicClauseOld.Operator.AND);
        
        node.addChild(leftParent);
        node.addChild(rightParent);
        
        leftParent.addChild(x1);
        leftParent.addChild(y);
        
        rightParent.addChild(x2);
        rightParent.addChild(z);
        
        // if the nodes have a content,
        // clean up any references from x1/x2 that where removed in this split
        //cleanRelations(x1.getContent(), z);
        //cleanRelations(x2.getContent(), y);
        
        // start over again
        return false;
      }
    }
    
    // recursivly check children
    return makeDNF(left) && makeDNF(right);
  }
  
  private static void cleanRelationForAlternative(LogicClauseOld alternative)
  {
    Preconditions.checkNotNull(alternative);
    Preconditions.checkArgument(alternative.getOp() == LogicClauseOld.Operator.AND);
    
    Set<Long> validNodeIDs = new HashSet<Long>();
    
    // first collect all IDs that exist in this alternative
    for(LogicClauseOld c : alternative.getChildren())
    {
      if(c.getOp() == LogicClauseOld.Operator.LEAF && c.getContent() != null)
      {
        QueryNode node = c.getContent();
        validNodeIDs.add(node.getId());
      }
    }
    
    // second remove all joins that refer to non-existing IDs
    for(LogicClauseOld c : alternative.getChildren())
    {
      if(c.getContent() != null)
      {
        ListIterator<Join> joins = c.getContent().getJoins().listIterator();
        while(joins.hasNext())
        {
          Join j = joins.next();
          if(! validNodeIDs.contains(j.getTarget().getId()))
          {
            joins.remove();
          }
        }
      }
    }
  }
  
  /**
   * Cleaning up relations that where removed when splitting the node.
   * @param node The node that might have references left
   * @param toRemove {@link LogicClauseOld} node that was removed from the clause.
   */
  private static void cleanRelations(QueryNode node, LogicClauseOld toRemove)
  {
    if(node != null)
    {
      ListIterator<Join> itJoins = node.getJoins().listIterator();
      // only search for node IDs when there are any joins
      if(itJoins.hasNext())
      {
        Set<Long> nodesToRemove = new HashSet<Long>();
        findQueryNodeIDs(toRemove, nodesToRemove);

        while(itJoins.hasNext())
        {
          Join j = itJoins.next();
          if(nodesToRemove.contains(j.getTarget().getId()))
          {
            itJoins.remove();
          }
        }
      }
    }
  }
  
  private static void findQueryNodeIDs(LogicClauseOld clause, Set<Long> queryNodeIDs)
  {
    if(queryNodeIDs != null)
    {
      if(clause.getContent() == null)
      {
        for(LogicClauseOld childClause : clause.getChildren())
        {
          findQueryNodeIDs(childClause, queryNodeIDs);
        }
      }
      else
      {
        queryNodeIDs.add(clause.getContent().getId());
      }
    }
  }
  
  /**
   * During parsing there will be leafs created with no QueryNode attached (e.g.
   * binary terms). This functions removes all these leafs.   * 
   * @param clause The clause to remove the empty leafs from.
   */
  private static void cleanEmptyLeafs(LogicClauseOld clause)
  {
    if(clause == null || clause.getOp() == LogicClauseOld.Operator.LEAF)
    {
      return;
    }
    
    LinkedList<LogicClauseOld> childListCopy = new LinkedList<LogicClauseOld>(clause.getChildren());
    
    clause.clearChildren();
    
    ListIterator<LogicClauseOld> itChildren = childListCopy.listIterator();
    while(itChildren.hasNext())
    {
      LogicClauseOld n = itChildren.next();
      
      // don't add empty leafs, add everything else
      if(!(n.getOp() == LogicClauseOld.Operator.LEAF && n.getContent() == null)) 
      {
        clause.addChild(n);
      }
    }
    
    // clean all sub clauses
    for(LogicClauseOld c : clause.getChildren())
    {
      cleanEmptyLeafs(c);
    }
  }
  
  /**
   * Flatten the clause in the sense that there is only one toplevel OR layer
   * and one layer of AND-clauses.
   * @param top 
   */
  private static void flattenDNF(LogicClauseOld top)
  {
    if(top.getOp() == LogicClauseOld.Operator.LEAF || top.getOp() == LogicClauseOld.Operator.AND)
    {
      List<LogicClauseOld> children = new ArrayList<LogicClauseOld>();
      findAllChildrenForAnd(top, children);
      
      top.setOp(LogicClauseOld.Operator.OR);
      top.clearChildren();
      top.setContent(null);
      
      LogicClauseOld andClause = new LogicClauseOld(LogicClauseOld.Operator.AND);
      top.addChild(andClause);
      
      for(LogicClauseOld c : children)
      {
        andClause.addChild(c);
      }
    }
    else if(top.getOp() == LogicClauseOld.Operator.OR)
    {
      
      // find sub and-clauses for all or-clauses
      for(LogicClauseOld subclause : top.getChildren())
      {
        if(subclause.getOp() == LogicClauseOld.Operator.LEAF)
        { 
          // add an artificial "and" node
          QueryNode content = subclause.getContent();
          subclause.clearChildren();
          subclause.setOp(LogicClauseOld.Operator.AND);
          subclause.setContent(null);
          
          LogicClauseOld newLeaf = new LogicClauseOld(LogicClauseOld.Operator.LEAF);
          newLeaf.setContent(content);
          subclause.addChild(newLeaf);
          
        }
        else if (subclause.getOp() == LogicClauseOld.Operator.AND)
        {

          List<LogicClauseOld> children = new ArrayList<LogicClauseOld>();
          findAllChildrenForAnd(subclause, children);

          subclause.clearChildren();

          for(LogicClauseOld c : children)
          {
            subclause.addChild(c);
          }
        }
        else
        {
          Preconditions.checkArgument(false, "input is not in DNF");
        }
       
      }
    }
  }
  
  private static void findAllChildrenForAnd(LogicClauseOld node, List<LogicClauseOld> followers)
  {
    if(node.getOp() == LogicClauseOld.Operator.LEAF)
    {
      followers.add(new LogicClauseOld(node));
      return;
    }
    
    Preconditions.checkArgument(node.getOp() == LogicClauseOld.Operator.AND);
    
    for(LogicClauseOld c : node.getChildren())
    {
      findAllChildrenForAnd(c, followers);
    }
  }

}
