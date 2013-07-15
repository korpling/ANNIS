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

import annis.model.LogicClause;
import annis.model.QueryNode;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class DNFTransformer
{
  /**
   * Transforms a AQL query to the Disjunctive Normal Form.
   * @param topNode
   * @return 
   */
  public static void toDNF(LogicClause topNode)
  {
    
    makeBinary(topNode);
    while(makeDNF(topNode) == false)
    {
      // do nothing, just repeat
    }
    flattenDNF(topNode);
  }
  
  private static void makeBinary(LogicClause c)
  {
    if(c.getOp() == LogicClause.Operator.LEAF 
      || c.getChildren().isEmpty())
    {
      return;
    }
    
    // check if we have a degraded path with only one sibling
    LogicClause parent = c.getParent();
    if(c.getParent() != null && c.getChildren().size() == 1)
    {
      // push this node up
      int idx = parent.getChildren().indexOf(c);
      parent.removeChild(idx);
      parent.addChild(0, c.getChildren().get(0));
    }
    else if(c.getChildren().size() > 2)
    {
      LogicClause firstChild = c.getChildren().get(0);
      // merge together under a new node
      LogicClause newSubClause = new LogicClause(c.getOp());
      
      ListIterator<LogicClause> itChildren = c.getChildren().listIterator(1);
      while(itChildren.hasNext())
      {
        LogicClause n = itChildren.next();
        newSubClause.addChild(n);
      }
      
      // rebuild the children
      c.clearChildren();
      c.addChild(firstChild);
      c.addChild(newSubClause);
    }
    
    // do the same thing for all children
    int cSize = c.getChildren().size();
    if(cSize >= 1)
    {
      makeBinary(c.getChildren().get(0));
    }
    
    if(cSize == 2)
    {
      makeBinary(c.getChildren().get(1));
    }    
  }
  

  /**
   * 
   * @param node
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
      
      if(right.getOp() == LogicClause.Operator.OR)
      {
        x1 = left;
        x2 = new LogicClause(x1);
        
        Preconditions.checkArgument(right.getChildren().size() == 2, 
          "OR nodes must always have exactly two children");
        y = right.getChildren().get(0);
        z = right.getChildren().get(1);
      }
      else if(left.getOp() == LogicClause.Operator.OR)
      {
        x1 = right;
        x2 = new LogicClause(x1);
        
        Preconditions.checkArgument(left.getChildren().size() == 2, 
          "OR nodes must always have exactly two children");
        y = left.getChildren().get(0);
        z = left.getChildren().get(1);
      }
      
      if(x1 != null && x2 != null && y != null && z != null)
      {
        node.setOp(LogicClause.Operator.OR);
        node.setContent(null);
        node.clearChildren();
        
        LogicClause leftParent = new LogicClause(LogicClause.Operator.AND);
        LogicClause rightParent = new LogicClause(LogicClause.Operator.AND);
        
        node.addChild(leftParent);
        node.addChild(rightParent);
        
        leftParent.addChild(x1);
        leftParent.addChild(y);
        
        rightParent.addChild(x2);
        rightParent.addChild(z);
        
        // start over again
        return false;
      }
    }
    
    // recursivly check children
    return makeDNF(left) && makeDNF(right);

  }
  
  private static void flattenDNF(LogicClause top)
  {
    if(top.getOp() == LogicClause.Operator.LEAF || top.getOp() == LogicClause.Operator.AND)
    {
      List<LogicClause> children = new ArrayList<LogicClause>();
      findAllChildrenForAnd(top, children);
      
      top.setOp(LogicClause.Operator.OR);
      top.clearChildren();
      top.setContent(null);
      
      LogicClause andClause = new LogicClause(LogicClause.Operator.AND);
      top.addChild(andClause);
      
      for(LogicClause c : children)
      {
        andClause.addChild(c);
      }
    }
    else if(top.getOp() == LogicClause.Operator.OR)
    {
      
      // find sub and-clauses for all or-clauses
      for(LogicClause subclause : top.getChildren())
      {
        if(subclause.getOp() == LogicClause.Operator.LEAF)
        { 
          // add an artificial "and" node
          QueryNode content = subclause.getContent();
          subclause.clearChildren();
          subclause.setOp(LogicClause.Operator.AND);
          subclause.setContent(null);
          
          LogicClause newLeaf = new LogicClause(LogicClause.Operator.LEAF);
          newLeaf.setContent(content);
          subclause.addChild(newLeaf);
          
        }
        else if (subclause.getOp() == LogicClause.Operator.AND)
        {

          List<LogicClause> children = new ArrayList<LogicClause>();
          findAllChildrenForAnd(subclause, children);

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
  
  private static void findAllChildrenForAnd(LogicClause node, List<LogicClause> followers)
  {
    if(node.getOp() == LogicClause.Operator.LEAF)
    {
      followers.add(node);
      return;
    }
    
    Preconditions.checkArgument(node.getOp() == LogicClause.Operator.AND);
    
    for(LogicClause c : node.getChildren())
    {
      findAllChildrenForAnd(c, followers);
    }
  }

}
