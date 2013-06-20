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

import annis.exceptions.AnnisQLSyntaxException;
import annis.model.QueryAnnotation;
import annis.model.QueryNode;
import annis.ql.AqlBaseListener;
import annis.ql.AqlLexer;
import annis.ql.AqlParser;
import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class AnnisParserAntlr
{
  public QueryData parse(String aql, List<Long> corpusList)
  {
    AqlLexer lexer = new AqlLexer(new ANTLRInputStream(aql));
    AqlParser parser = new AqlParser(new CommonTokenStream(
      lexer));
    
    final List<String> errors = new LinkedList<String>();
    
    parser.removeErrorListeners();
    parser.addErrorListener(new BaseErrorListener()
    {

      @Override
      public void syntaxError(Recognizer recognizer, Token offendingSymbol,
        int line, int charPositionInLine, String msg, RecognitionException e)
      {
        errors.add("line " + line + ":" + charPositionInLine + " " + msg);
      }
      
    });
    
    ParseTree tree = parser.start();
    if(errors.isEmpty())
    {
      ParseTreeWalker walker = new ParseTreeWalker();
      AqlListener listener = new AqlListener();
      walker.walk(listener, tree);
    }
    else
    {
      throw new AnnisQLSyntaxException("Parser error:\n" 
        + Joiner.on("\n").join(errors));
    }
    
    return null;
  }
  
  public static class AqlListener extends AqlBaseListener
  {
    private QueryNode topNode = new QueryNode();
    private List<List<QueryNode>> alternativeStack = new LinkedList<List<QueryNode>>();
    private int aliasCount = 0;
    
    public QueryNode getTopNode()
    {
      return topNode;
    }

    @Override
    public void enterAndTop(AqlParser.AndTopContext ctx)
    {
      topNode.setType(QueryNode.Type.AND);
      alternativeStack.add(0, topNode.getAlternatives());
    }

    @Override
    public void enterOrTop(AqlParser.OrTopContext ctx)
    {
      topNode.setType(QueryNode.Type.OR);
      alternativeStack.add(0, topNode.getAlternatives());
    }

    @Override
    public void exitAndTop(AqlParser.AndTopContext ctx)
    {
      alternativeStack.remove(0);
    }

    @Override
    public void exitOrTop(AqlParser.OrTopContext ctx)
    {
      alternativeStack.remove(0);
    }

    @Override
    public void enterAndExpr(AqlParser.AndExprContext ctx)
    {
      QueryNode exprNode = new QueryNode(QueryNode.Type.AND);
      alternativeStack.get(0).add(exprNode);
      alternativeStack.add(0, exprNode.getAlternatives());
    }

    @Override
    public void enterOrExpr(AqlParser.OrExprContext ctx)
    {
      QueryNode exprNode = new QueryNode(QueryNode.Type.OR);
      alternativeStack.get(0).add(exprNode);
      alternativeStack.add(0, exprNode.getAlternatives());
    }

    @Override
    public void exitAndExpr(AqlParser.AndExprContext ctx)
    {
      alternativeStack.remove(0);
    }
    
    @Override
    public void exitOrExpr(AqlParser.OrExprContext ctx)
    {
      alternativeStack.remove(0);
    }

    @Override
    public void enterTokExpr(AqlParser.TokExprContext ctx)
    {
      QueryNode target = newNode();
      
      target.setToken(true);
      if(ctx.text_spec() != null)
      {
        // TODO: get text spec content
      }
      
    }

    
    
    private QueryNode newNode()
    {
      QueryNode n = new QueryNode(++aliasCount);
      n.setVariable("n" + n.getId());
      n.setMarker(n.getVariable());

      alternativeStack.get(0).add(n);
      
      return n;
    }
    
  }
}
