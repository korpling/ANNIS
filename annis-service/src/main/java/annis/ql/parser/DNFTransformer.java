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
import java.util.ListIterator;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class DNFTransformer
{
//  /**
//   * Transforms a AQL query to the Disjunctive Normal Form.
//   * @param topNode
//   * @return 
//   */
//  public static boolean toDNF(QueryNode topNode)
//  {
//    boolean somethingChanged = false;
//    while(testDNF(topNode) == false)
//    {
//      somethingChanged = true;
//      
//      cleanSubformula(topNode);
//    }
//    return somethingChanged;
//  }
  
//  private static void makeBinary(QueryNode parent, QueryNode node)
//  {
//    if(node.getType() == QueryNode.Type.NODE 
//      || node.getAlternatives().isEmpty())
//    {
//      return;
//    }
//    
//    if(parent != null && node.getAlternatives().size() == 1)
//    {
//      // push this node up
//      
//    }
//    
//  }
  
//  private static void cleanSubformula(QueryNode node)
//  {
//    if(node.getType() == QueryNode.Type.NODE || node.getAlternatives().isEmpty())
//    {
//      return;
//    }
//    QueryNode.Type parentType = node.getType();
//
//    // clean all child formula
//    for(QueryNode c : node.getAlternatives())
//    {
//      cleanSubformula(c);
//    }
//    
//    // check if we can clean the current one
//    boolean allSame = true;
//    for(QueryNode c : node.getAlternatives())
//    {
//      if(c.getType() != parentType)
//      {
//        allSame = false;
//        break;
//      }
//    }
//    
//    if(allSame)
//    {
//      ListIterator<QueryNode> itChildren = node.getAlternatives().listIterator();
//      while(itChildren.hasNext())
//      {
//        QueryNode child = itChildren.next();
//        
//         // add all the children list to the parent list and remove the old node
//        node.getAlternatives().addAll(child.getAlternatives());
//        itChildren.remove();
//      }
//    }
//    
//  }
//  
//  /**
//   * Checks an AQL query if it is already in Disjunctive Normal Form.
//   * @param topNode
//   * @return True if in Disjunctive Normal Form.
//   */
//  public static boolean testDNF(QueryNode topNode)
//  {
//    if(topNode.getType() != QueryNode.Type.OR)
//    {
//      return false;
//    }
//    
//    if(topNode.getAlternatives() == null)
//    {
//      return false;
//    }
//    
//    for(QueryNode nAnd : topNode.getAlternatives())
//    {
//      if(nAnd.getType() != QueryNode.Type.AND)
//      {
//        return false;
//      }
//      
//      if(nAnd.getAlternatives() == null)
//      {
//        return false;
//      }
//      
//      for(QueryNode n : nAnd.getAlternatives())
//      {
//        if(n.getType() != QueryNode.Type.NODE)
//        {
//          return false;
//        }
//      }
//    }
//
//    
//    return true;
//  }
}
