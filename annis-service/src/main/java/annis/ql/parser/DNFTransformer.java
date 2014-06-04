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

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.antlr.v4.runtime.Token;

/**
 * Utility functions for transforming an AQL query to the Disjunctive Normal Form.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class DNFTransformer
{
  /**
   * Transforms an AQL query to the Disjunctive Normal Form.
   * @param topNode
   * @return 
   */
  public static void toDNF(LogicClause topNode)
  {
    
    while(makeDNF(topNode) == false)
    {
      // do nothing, just repeat
    }
    cleanEmptyLeafs(topNode);
    flattenDNF(topNode);
  }
  

  /**
   * Iteration step to transform a {@link LogicClause} into DNF.
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
  private static boolean makeDNF(LogicClause node)
  {
    if(node.getOp() == LogicClause.Operator.LEAF || node.getChildren().size() < 2)
    {
      return true;
    }
    
    LogicClause left = node.getChildren().get(0);
    LogicClause right = node.getChildren().get(1);
    if(node.getOp() == LogicClause.Operator.AND)
    {
      // check if operator of this node and one of it's children is the same
      
      LogicClause x1 = null;
      LogicClause x2 = null;
      LogicClause y = null;
      LogicClause z = null;
      
      List<? extends Token> orToken = null;
      
      if(right.getOp() == LogicClause.Operator.OR)
      {
        x1 = left;
        x2 = new LogicClause(x1);
        
        orToken = right.getContent();
        
        Preconditions.checkArgument(right.getChildren().size() == 2, 
          "OR nodes must always have exactly two children");
        y = right.getChildren().get(0);
        z = right.getChildren().get(1);
      }
      else if(left.getOp() == LogicClause.Operator.OR)
      {
        x1 = right;
        x2 = new LogicClause(x1);
        
        orToken = left.getContent();
        
        Preconditions.checkArgument(left.getChildren().size() == 2, 
          "OR nodes must always have exactly two children");
        y = left.getChildren().get(0);
        z = left.getChildren().get(1);
      }
      
      if(x1 != null && x2 != null && y != null && z != null)
      {
        
        LogicClause leftParentAnd = new LogicClause(LogicClause.Operator.AND);
        LogicClause rightParentAnd = new LogicClause(LogicClause.Operator.AND);
        
        // replicate the original "&" token to both new AND nodes
        leftParentAnd.setContent(new ArrayList<>(node.getContent()));
        rightParentAnd.setContent(new ArrayList<>(node.getContent()));
        
        node.setOp(LogicClause.Operator.OR);
        node.setContent(orToken);
        node.clearChildren();
        
        node.addChild(leftParentAnd);
        node.addChild(rightParentAnd);
        
        leftParentAnd.addChild(x1);
        leftParentAnd.addChild(y);
        
        rightParentAnd.addChild(x2);
        rightParentAnd.addChild(z);
        
        // start over again
        return false;
      }
    }
    
    // recursivly check children
    return makeDNF(left) && makeDNF(right);
  }

  
  /**
   * During parsing there will be leafs created with no QueryNode attached (e.g.
   * binary terms). This functions removes all these leafs.   * 
   * @param clause The clause to remove the empty leafs from.
   */
  private static void cleanEmptyLeafs(LogicClause clause)
  {
    if(clause == null || clause.getOp() == LogicClause.Operator.LEAF)
    {
      return;
    }
    
    LinkedList<LogicClause> childListCopy = new LinkedList<>(clause.getChildren());
    
    clause.clearChildren();
    
    ListIterator<LogicClause> itChildren = childListCopy.listIterator();
    while(itChildren.hasNext())
    {
      LogicClause n = itChildren.next();
      
      // don't add empty leafs, add everything else
      if(!(n.getOp() == LogicClause.Operator.LEAF && n.getContent() == null)) 
      {
        clause.addChild(n);
      }
    }
    
    // clean all sub clauses
    for(LogicClause c : clause.getChildren())
    {
      cleanEmptyLeafs(c);
    }
  }
  
  /**
   * Flatten the clause in the sense that there is only one toplevel OR layer
   * and one layer of AND-clauses.
   * @param top 
   */
  private static void flattenDNF(LogicClause top)
  {
    if(top.getOp() == LogicClause.Operator.LEAF || top.getOp() == LogicClause.Operator.AND)
    {
      List<LogicClause> children = new ArrayList<>();
      findAllChildrenForOp(top, children, LogicClause.Operator.AND);
      
      List<? extends Token> orginalAndContent = top.getContent();
      
      top.setOp(LogicClause.Operator.OR);
      top.clearChildren();
      // there is no original "|" in the token stream which we can refer to
      
      top.setContent(null);
      
      LogicClause andClause = new LogicClause(LogicClause.Operator.AND);
      andClause.setContent(orginalAndContent);
      top.addChild(andClause);
      
      andClause.addAllChildren(children);
      
    }
    else if(top.getOp() == LogicClause.Operator.OR)
    { 
      // first flatten the OR operator
      List<LogicClause> allOrNodes = new ArrayList<>();
      findAllChildrenForOp(top, allOrNodes, LogicClause.Operator.OR, true);
      
      top.clearChildren();
      top.addAllChildren(allOrNodes);
      
      // find sub and-clauses for all or-clauses
      for(LogicClause subclause : top.getChildren())
      {
        if(subclause.getOp() == LogicClause.Operator.LEAF)
        { 
          // add an artificial "and" node
          List<? extends Token> content = subclause.getContent();
          subclause.clearChildren();
          subclause.setOp(LogicClause.Operator.AND);
          // there is no original "&" in the token stream which we can refer to
          subclause.setContent(null);
          
          LogicClause newLeaf = new LogicClause(LogicClause.Operator.LEAF);
          newLeaf.setContent(content);
          subclause.addChild(newLeaf);
          
        }
        else if (subclause.getOp() == LogicClause.Operator.AND)
        {

          List<LogicClause> children = new ArrayList<>();
          findAllChildrenForOp(subclause, children, LogicClause.Operator.AND);

          subclause.clearChildren();

          for(LogicClause c : children)
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
  
  private static void findAllChildrenForOp(LogicClause node, List<LogicClause> followers, 
    LogicClause.Operator op)
  {
    findAllChildrenForOp(node, followers, op, false);
  }
  
  private static void findAllChildrenForOp(LogicClause node, List<LogicClause> followers, 
    LogicClause.Operator op, boolean addOtherOpsAsChild)
  {
    if(node.getOp() == LogicClause.Operator.LEAF 
      || (addOtherOpsAsChild && node.getOp() != op))
    {
      followers.add(new LogicClause(node));
      return;
    }
    
    
    if(!addOtherOpsAsChild)
    {
      Preconditions.checkArgument(node.getOp() == op, "BUG: Wrong operator found");
    }
    
    for(LogicClause c : node.getChildren())
    {
      findAllChildrenForOp(c, followers, op, addOtherOpsAsChild);
    }
  }

}
