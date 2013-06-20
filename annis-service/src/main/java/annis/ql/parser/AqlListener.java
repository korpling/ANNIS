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
import annis.ql.AqlParser;
import annis.sqlgen.model.Precedence;
import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.antlr.v4.runtime.Token;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class AqlListener extends AqlBaseListener
{
  private QueryNode topNode = new QueryNode();

  private List<List<QueryNode>> alternativeStack = new LinkedList<List<QueryNode>>();

  private int aliasCount = 0;

  private Map<String, QueryNode> nodes = new HashMap<String, QueryNode>();

  private int precedenceBound;

  public AqlListener(int precedenceBound)
  {
    this.precedenceBound = precedenceBound;
  }

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
  public void enterTokOnlyExpr(AqlParser.TokOnlyExprContext ctx)
  {
    QueryNode target = newNode();
    target.setToken(true);
  }

  @Override
  public void enterTokTextExpr(AqlParser.TokTextExprContext ctx)
  {
    QueryNode target = newNode();
    target.setToken(true);
    QueryNode.TextMatching txtMatch = textMatchingFromSpec(ctx.textSpec(),
      ctx.NEQ() != null);
    String content = textFromSpec(ctx.textSpec());
    target.setSpannedText(content, txtMatch);
  }

  @Override
  public void enterTextOnly(AqlParser.TextOnlyContext ctx)
  {
    QueryNode target = newNode();
    target.setSpannedText(textFromSpec(ctx.txt),
      textMatchingFromSpec(ctx.txt, false));
  }

  @Override
  public void enterAnnoOnlyExpr(AqlParser.AnnoOnlyExprContext ctx)
  {
    QueryNode target = newNode();
    String namespace = ctx.qName().namespace == null ? null : ctx.qName().namespace.getText();
    QueryAnnotation anno = new QueryAnnotation(namespace,
      ctx.qName().name.getText());
    target.addNodeAnnotation(anno);
  }

  @Override
  public void enterAnnoEqTextExpr(AqlParser.AnnoEqTextExprContext ctx)
  {
    QueryNode target = newNode();
    String namespace = ctx.qName().namespace == null ? null : ctx.qName().namespace.getText();
    String name = ctx.qName().name.getText();
    String value = textFromSpec(ctx.txt);
    QueryNode.TextMatching matching = textMatchingFromSpec(ctx.txt,
      ctx.NEQ() != null);
    QueryAnnotation anno = new QueryAnnotation(namespace, name, value, matching);
    target.addNodeAnnotation(anno);
  }

  @Override
  public void enterRootTerm(AqlParser.RootTermContext ctx)
  {
    QueryNode target = nodeByRef(ctx.left);
    Preconditions.checkNotNull(target, errorLHS("root"));
    target.setRoot(true);
  }

  @Override
  public void enterArityTerm(AqlParser.ArityTermContext ctx)
  {
    QueryNode target = nodeByRef(ctx.left);
    Preconditions.checkNotNull(target, errorLHS("arity"));
    target.setArity(annisRangeFromARangeSpec(ctx.rangeSpec()));
  }

  @Override
  public void enterTokenArityTerm(AqlParser.TokenArityTermContext ctx)
  {
    QueryNode target = nodeByRef(ctx.left);
    Preconditions.checkNotNull(target, errorLHS("token-arity"));
    target.setTokenArity(annisRangeFromARangeSpec(ctx.rangeSpec()));
  }

  @Override
  public void enterDirectPrecedence(
    AqlParser.DirectPrecedenceContext ctx)
  {
    QueryNode left = nodeByRef(ctx.left);
    QueryNode right = nodeByRef(ctx.right);
    Preconditions.checkNotNull(left, errorLHS("precendence"));
    Preconditions.checkNotNull(right, errorRHS("precendence"));
    
    String segmentationName = null;
    if(ctx.layer != null)
    {
      segmentationName=ctx.layer.getText();
    }
    left.addJoin(new Precedence(right, 1, segmentationName));
  }

  @Override
  public void enterIndirectPrecedence(
    AqlParser.IndirectPrecedenceContext ctx)
  {
    QueryNode left = nodeByRef(ctx.left);
    QueryNode right = nodeByRef(ctx.right);
    Preconditions.checkNotNull(left, errorLHS("precendence"));
    Preconditions.checkNotNull(right, errorRHS("precendence"));
    
    String segmentationName = null;
    if(ctx.layer != null)
    {
      segmentationName=ctx.layer.getText();
    }
    if(precedenceBound > 0)
    {
      left.addJoin(
        new Precedence(right, 1, precedenceBound, segmentationName));
    }
    else
    {
      left.addJoin(new Precedence(right, segmentationName));
    }
  }

  @Override
  public void enterRangePrecedence(AqlParser.RangePrecedenceContext ctx)
  {
    QueryNode left = nodeByRef(ctx.left);
    QueryNode right = nodeByRef(ctx.right);
    Preconditions.checkNotNull(left, errorLHS("precendence"));
    Preconditions.checkNotNull(right, errorRHS("precendence"));
    
    QueryNode.Range range = annisRangeFromARangeSpec(ctx.rangeSpec());
    if(range.getMin() == 0 || range.getMax() == 0)
    {
       throw new AnnisQLSyntaxException("Distance can't be 0");
    }
    else
    {
      String segmentationName = null;
      if(ctx.layer != null)
      {
        segmentationName=ctx.layer.getText();
      }

      left.addJoin(
        new Precedence(right, range.getMin(), range.getMax(), segmentationName));
    }
  }
  

  private String textFromSpec(AqlParser.TextSpecContext txtCtx)
  {
    if (txtCtx instanceof AqlParser.EmptyExactTextSpecContext || txtCtx instanceof AqlParser.EmptyRegexTextSpecContext)
    {
      return "";
    }
    else if (txtCtx instanceof AqlParser.ExactTextSpecContext)
    {
      return ((AqlParser.ExactTextSpecContext) txtCtx).content.getText();
    }
    else if (txtCtx instanceof AqlParser.RegexTextSpecContext)
    {
      return ((AqlParser.RegexTextSpecContext) txtCtx).content.getText();
    }
    return null;
  }

  private QueryNode.TextMatching textMatchingFromSpec(
    AqlParser.TextSpecContext txt, boolean not)
  {
    if (txt instanceof AqlParser.ExactTextSpecContext)
    {
      return not ? QueryNode.TextMatching.EXACT_NOT_EQUAL : QueryNode.TextMatching.EXACT_EQUAL;
    }
    else if (txt instanceof AqlParser.RegexTextSpecContext)
    {
      QueryNode.TextMatching matching = not ? QueryNode.TextMatching.REGEXP_NOT_EQUAL : QueryNode.TextMatching.REGEXP_EQUAL;
    }
    return null;
  }

  private QueryNode.Range annisRangeFromARangeSpec(
    AqlParser.RangeSpecContext spec)
  {
    String min = spec.min.getText();
    String max = spec.max != null ? spec.max.getText() : null;
    if (max == null)
    {
      return new QueryNode.Range(Integer.parseInt(min), Integer.parseInt(min));
    }
    else
    {
      return new QueryNode.Range(Integer.parseInt(min), Integer.parseInt(max));
    }
  }

  private QueryNode nodeByRef(Token ref)
  {
    return nodes.get("n" + ref.getText().substring(1));
  }

  private QueryNode newNode()
  {
    QueryNode n = new QueryNode(++aliasCount);
    n.setVariable("n" + n.getId());
    n.setMarker(n.getVariable());
    alternativeStack.get(0).add(n);
    return n;
  }

  private String errorLHS(String function)
  {
    return function + " operator needs a left-hand-side";
  }

  private String errorRHS(String function)
  {
    return function + " operator needs a right-hand-side";
  }
  
}
