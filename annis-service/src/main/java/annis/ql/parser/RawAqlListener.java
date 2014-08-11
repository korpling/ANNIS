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

import annis.ql.RawAqlPreParser;
import annis.ql.RawAqlPreParserBaseListener;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.LinkedList;
import java.util.List;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class RawAqlListener extends RawAqlPreParserBaseListener
{
  private LogicClause root;
  private LogicClause current;

  @Override
  public void enterStart(RawAqlPreParser.StartContext ctx)
  {
    root = null;
  }

  
  @Override
  public void enterAndExpr(RawAqlPreParser.AndExprContext ctx)
  {
    LogicClause nodeAnd = new LogicClause(LogicClause.Operator.AND);
    nodeAnd.setContent(Lists.newArrayList(ctx.AND().getSymbol()));
    if(current != null)
    {
      current.addChild(nodeAnd);
    }
    
    current = nodeAnd;
  }

  @Override
  public void exitAndExpr(RawAqlPreParser.AndExprContext ctx)
  {
    switchToParent();
  }
  
  @Override
  public void enterOrExpr(RawAqlPreParser.OrExprContext ctx)
  {
    LogicClause nodeOr = new LogicClause(LogicClause.Operator.OR);
    nodeOr.setContent(Lists.newArrayList(ctx.OR().getSymbol()));
    if(current != null)
    {
      current.addChild(nodeOr);
    }
    current = nodeOr;
  }

  @Override
  public void exitOrExpr(RawAqlPreParser.OrExprContext ctx)
  {
    switchToParent();
  }
  
  

  @Override
  public void enterLeafExpr(RawAqlPreParser.LeafExprContext ctx)
  {
    LogicClause nodeLeaf = new LogicClause(LogicClause.Operator.LEAF);
    if(current != null)
    {
      current.addChild(nodeLeaf);
    }
    
    // get all token covered by this node
    nodeLeaf.setContent(collectToken(ctx));
    
    current = nodeLeaf;
  }
  
  private static List<Token> collectToken(ParseTree node)
  {
    List<Token> token = new LinkedList<>();
    collectToken(node, token);
    return token;
  }
  
  private static void collectToken(ParseTree node, List<Token> token)
  {
    for(int i=0; i < node.getChildCount(); i++)
    {
      ParseTree child = node.getChild(i);
      if(child.getPayload() instanceof Token)
      {
        token.add((Token) child.getPayload());
      }
      else
      {
        collectToken(child, token);
      }
    }
  }

  @Override
  public void exitLeafExpr(RawAqlPreParser.LeafExprContext ctx)
  {
    switchToParent();
  }
  
  private void switchToParent()
  {
    if(current.getParent() == null)
    {
      Preconditions.checkArgument(root == null, "There is only one root node allowed");
      root = current;
    }
    current = current.getParent();
  }

  public LogicClause getRoot()
  {
    return root;
  }
  
  
}
