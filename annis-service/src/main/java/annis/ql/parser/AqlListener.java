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
import annis.model.LogicClause;
import annis.model.QueryAnnotation;
import annis.model.QueryNode;
import annis.model.QueryNode.Range;
import annis.ql.AqlParser;
import annis.ql.AqlParserBaseListener;
import static annis.service.objects.AnnisAttribute.SubType.n;
import annis.sqlgen.model.CommonAncestor;
import annis.sqlgen.model.Dominance;
import annis.sqlgen.model.Identical;
import annis.sqlgen.model.Inclusion;
import annis.sqlgen.model.Join;
import annis.sqlgen.model.LeftAlignment;
import annis.sqlgen.model.LeftDominance;
import annis.sqlgen.model.LeftOverlap;
import annis.sqlgen.model.Overlap;
import annis.sqlgen.model.PointingRelation;
import annis.sqlgen.model.Precedence;
import annis.sqlgen.model.RightAlignment;
import annis.sqlgen.model.RightDominance;
import annis.sqlgen.model.RightOverlap;
import annis.sqlgen.model.Sibling;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import static org.apache.commons.lang3.StringUtils.right;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class AqlListener extends AqlParserBaseListener
{
  private static final Logger log = LoggerFactory.getLogger(AqlListener.class);
  
  private LogicClause top = null;

  private List<LogicClause> alternativeStack = new LinkedList<LogicClause>();

  private int aliasCount = 0;
  private String lastVariableDefinition = null;

  private Multimap<String, QueryNode> nodes = HashMultimap.create();

  private int precedenceBound;
  
  private List<QueryAnnotation> metaData = new ArrayList<QueryAnnotation>();

  public AqlListener(int precedenceBound)
  {
    this.precedenceBound = precedenceBound;
  }

  public LogicClause getTop()
  {
    return top;
  }

  public List<QueryAnnotation> getMetaData()
  {
    return metaData;
  }
  

  @Override
  public void enterAndTop(AqlParser.AndTopContext ctx)
  {
    top = new LogicClause(LogicClause.Operator.AND);
    top.setOp(LogicClause.Operator.AND);
    alternativeStack.add(0, top);
  }

  @Override
  public void enterOrTop(AqlParser.OrTopContext ctx)
  {
    top = new LogicClause(LogicClause.Operator.OR);
    top.setOp(LogicClause.Operator.OR);
    alternativeStack.add(0, top);
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
    LogicClause andClause = new LogicClause(LogicClause.Operator.AND);
    if(!alternativeStack.isEmpty())
    {
      alternativeStack.get(0).addChild(andClause);
    }
    alternativeStack.add(0, andClause);
  }

  @Override
  public void enterOrExpr(AqlParser.OrExprContext ctx)
  {
    LogicClause orClause = new LogicClause(LogicClause.Operator.OR);
    if(!alternativeStack.isEmpty())
    {
      alternativeStack.get(0).addChild(orClause);
    }
    alternativeStack.add(0, orClause);
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
  public void enterBinaryTermExpr(AqlParser.BinaryTermExprContext ctx)
  {
    LogicClause leaf = new LogicClause(LogicClause.Operator.LEAF);
    if(!alternativeStack.isEmpty())
    {
      alternativeStack.get(0).addChild(leaf);
    }
  }

  @Override
  public void enterUnaryTermExpr(AqlParser.UnaryTermExprContext ctx)
  {
    LogicClause leaf = new LogicClause(LogicClause.Operator.LEAF);
    if(!alternativeStack.isEmpty())
    {
      alternativeStack.get(0).addChild(leaf);
    }
  }

  
  
  

  @Override
  public void enterTokOnlyExpr(AqlParser.TokOnlyExprContext ctx)
  {
    QueryNode target = newNode();
    target.setToken(true);
  }

  @Override
  public void enterNodeExpr(AqlParser.NodeExprContext ctx)
  {
    newNode();
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
    String namespace = ctx.qName().namespace == null ? 
      null : ctx.qName().namespace.getText();
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
    Collection<QueryNode> targets = nodesByRef(ctx.left);
    Preconditions.checkArgument(!targets.isEmpty(), errorLHS("root"));
    for(QueryNode target : targets)
    {
      target.setRoot(true);
    }
  }

  @Override
  public void enterArityTerm(AqlParser.ArityTermContext ctx)
  {
    Collection<QueryNode> targets = nodesByRef(ctx.left);
    Preconditions.checkArgument(!targets.isEmpty(), errorLHS("arity"));
    
    for(QueryNode target : targets)
    {
      target.setArity(annisRangeFromARangeSpec(ctx.rangeSpec()));
    }
  }

  @Override
  public void enterTokenArityTerm(AqlParser.TokenArityTermContext ctx)
  {
    Collection<QueryNode> targets = nodesByRef(ctx.left);
    Preconditions.checkArgument(!targets.isEmpty(), errorLHS("token-arity"));
    
    for(QueryNode target : targets)
    {
      target.setTokenArity(annisRangeFromARangeSpec(ctx.rangeSpec()));
    }
  }

  @Override
  public void enterDirectPrecedence(
    AqlParser.DirectPrecedenceContext ctx)
  {
    Collection<QueryNode> nodesLeft = nodesByRef(ctx.left);
    Collection<QueryNode> nodesRight = nodesByRef(ctx.right);
    Preconditions.checkArgument(!nodesLeft.isEmpty(), errorLHS("precendence"));
    Preconditions.checkArgument(!nodesRight.isEmpty(), errorRHS("precendence"));
    
    String segmentationName = null;
    if(ctx.layer != null)
    {
      segmentationName=ctx.layer.getText();
    }
    for(QueryNode left : nodesLeft)
    {
      for(QueryNode right : nodesRight)
      {
        left.addJoin(new Precedence(right, 1, segmentationName));
      }
    }
  }

  @Override
  public void enterIndirectPrecedence(
    AqlParser.IndirectPrecedenceContext ctx)
  {
    Collection<QueryNode> nodesLeft = nodesByRef(ctx.left);
    Collection<QueryNode> nodesRight = nodesByRef(ctx.right);
    Preconditions.checkArgument(!nodesLeft.isEmpty(), errorLHS("precendence"));
    Preconditions.checkNotNull(!nodesRight.isEmpty(), errorRHS("precendence"));
    
    String segmentationName = null;
    if(ctx.layer != null)
    {
      segmentationName=ctx.layer.getText();
    }
    
    for (QueryNode left : nodesLeft)
    {
      for (QueryNode right : nodesRight)
      {
        if (precedenceBound > 0)
        {
          left.addJoin(
            new Precedence(right, 1, precedenceBound, segmentationName));
        }
        else
        {
          left.addJoin(new Precedence(right, segmentationName));
        }
      }
    }
  }

  @Override
  public void enterRangePrecedence(AqlParser.RangePrecedenceContext ctx)
  {
    Collection<QueryNode> nodesLeft = nodesByRef(ctx.left);
    Collection<QueryNode> nodesRight = nodesByRef(ctx.right);
    Preconditions.checkNotNull(!nodesLeft.isEmpty(), errorLHS("precendence"));
    Preconditions.checkArgument(!nodesRight.isEmpty(), errorRHS("precendence"));
    
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
      
      for (QueryNode left : nodesLeft)
      {
        for (QueryNode right : nodesRight)
        {
          left.addJoin(
            new Precedence(right, range.getMin(), range.getMax(),
            segmentationName));
        }
      }
      
    }
  }

  @Override
  public void enterIdenticalCoverage(AqlParser.IdenticalCoverageContext ctx)
  {
    join(ctx, Identical.class);
  }

  @Override
  public void enterLeftAlign(AqlParser.LeftAlignContext ctx)
  {
    join(ctx, LeftAlignment.class);
  }

  @Override
  public void enterRightAlign(AqlParser.RightAlignContext ctx)
  {
    join(ctx, RightAlignment.class);
  }

  @Override
  public void enterInclusion(AqlParser.InclusionContext ctx)
  {
    join(ctx, Inclusion.class);
  }

  @Override
  public void enterOverlap(AqlParser.OverlapContext ctx)
  {
    join(ctx, Overlap.class);
  }

  @Override
  public void enterRightOverlap(AqlParser.RightOverlapContext ctx)
  {
    join(ctx, RightOverlap.class);
  }

  @Override
  public void enterLeftOverlap(AqlParser.LeftOverlapContext ctx)
  {
    join(ctx, LeftOverlap.class);
  }

  @Override
  public void enterDirectDominance(AqlParser.DirectDominanceContext ctx)
  {
    Collection<QueryNode> nodesLeft = nodesByRef(ctx.left);
    Collection<QueryNode> nodesRight = nodesByRef(ctx.right);
    
    Preconditions.checkArgument(!nodesLeft.isEmpty(), errorLHS("dominance"));
    Preconditions.checkArgument(!nodesRight.isEmpty(), errorRHS("dominance"));
    
    String layer = ctx.layer == null ? null : ctx.layer.getText();
    
    for(QueryNode left : nodesLeft)
    {
      for(QueryNode right : nodesRight)
      {
    
        if(ctx.anno != null)
        {
          LinkedList<QueryAnnotation> annotations = fromEdgeAnnotation(ctx.anno);
          for (QueryAnnotation a : annotations)
          {
            right.addEdgeAnnotation(a);
          }
        }

        if(ctx.LEFT_CHILD() != null)
        {
          left.addJoin(new LeftDominance(right, layer));
        }
        else if(ctx.RIGHT_CHILD() != null)
        {
          left.addJoin(new RightDominance(right, layer));
        }
        else
        {
          left.addJoin(new Dominance(right, layer, 1));
        }
      }
    }
  }

  @Override
  public void enterIndirectDominance(AqlParser.IndirectDominanceContext ctx)
  {
    Collection<QueryNode> nodesLeft = nodesByRef(ctx.left);
    Collection<QueryNode> nodesRight = nodesByRef(ctx.right);
    Preconditions.checkArgument(!nodesLeft.isEmpty(), errorLHS("dominance"));
    Preconditions.checkArgument(!nodesRight.isEmpty(), errorRHS("dominance"));
    
    String layer = ctx.layer == null ? null : ctx.layer.getText();
   
    for(QueryNode left : nodesLeft)
    {
      for(QueryNode right : nodesRight)
      {
        left.addJoin(new Dominance(right, layer));
      }
    }
  }

  @Override
  public void enterRangeDominance(AqlParser.RangeDominanceContext ctx)
  {
    Collection<QueryNode> nodesLeft = nodesByRef(ctx.left);
    Collection<QueryNode> nodesRight = nodesByRef(ctx.right);
    Preconditions.checkArgument(!nodesLeft.isEmpty(), errorLHS("dominance"));
    Preconditions.checkArgument(!nodesRight.isEmpty(), errorRHS("dominance"));
    
    String layer = ctx.layer == null ? null : ctx.layer.getText();
   
    Range range = annisRangeFromARangeSpec(ctx.rangeSpec());
    Preconditions.checkArgument(range.getMax() != 0, "Distance can't be 0");
    Preconditions.checkArgument(range.getMin() != 0, "Distance can't be 0");
    
    for(QueryNode left : nodesLeft)
    {
      for(QueryNode right : nodesRight)
      {
        left.addJoin(new Dominance(right, layer, range.getMin(), range.getMax()));
      }
    }
  }

  @Override
  public void enterDirectPointing(AqlParser.DirectPointingContext ctx)
  {
    Collection<QueryNode> nodesLeft = nodesByRef(ctx.left);
    Collection<QueryNode> nodesRight = nodesByRef(ctx.right);
    Preconditions.checkArgument(!nodesLeft.isEmpty(), errorLHS("pointing"));
    Preconditions.checkArgument(!nodesRight.isEmpty(), errorRHS("pointing"));
    
    String label = ctx.label == null ? null : ctx.label.getText();
    
    for (QueryNode right : nodesRight)
    {
      if (ctx.anno != null)
      {
        LinkedList<QueryAnnotation> annotations = fromEdgeAnnotation(ctx.anno);
        for (QueryAnnotation a : annotations)
        {
          right.addEdgeAnnotation(a);
        }
      }
      
      for (QueryNode left : nodesLeft)
      {
        left.addJoin(new PointingRelation(right, label, 1));
      }
    }
  }

  @Override
  public void enterIndirectPointing(AqlParser.IndirectPointingContext ctx)
  {
    Collection<QueryNode> nodesLeft = nodesByRef(ctx.left);
    Collection<QueryNode> nodesRight = nodesByRef(ctx.right);
    Preconditions.checkArgument(!nodesLeft.isEmpty(), errorLHS("pointing"));
    Preconditions.checkArgument(!nodesRight.isEmpty(), errorRHS("pointing"));
    
    String label = ctx.label == null ? null : ctx.label.getText();
   
    for(QueryNode left : nodesLeft)
    {
      for(QueryNode right : nodesRight)
      {
        left.addJoin(new PointingRelation(right, label));
      }
    }
  }

  @Override
  public void enterRangePointing(AqlParser.RangePointingContext ctx)
  {
    Collection<QueryNode> nodesLeft = nodesByRef(ctx.left);
    Collection<QueryNode> nodesRight = nodesByRef(ctx.right);
    Preconditions.checkArgument(!nodesLeft.isEmpty(), errorLHS("pointing"));
    Preconditions.checkArgument(!nodesRight.isEmpty(), errorRHS("pointing"));
    
    String label = ctx.label == null ? null : ctx.label.getText();
   
    Range range = annisRangeFromARangeSpec(ctx.rangeSpec());
    Preconditions.checkArgument(range.getMax() != 0, "Distance can't be 0");
    Preconditions.checkArgument(range.getMin() != 0, "Distance can't be 0");
    
    for(QueryNode left : nodesLeft)
    {
      for(QueryNode right : nodesRight)
      {
        left.addJoin(new PointingRelation(right, label, range.getMin(), range.getMax()));
      }
    }
  }

  @Override
  public void enterCommonParent(AqlParser.CommonParentContext ctx)
  {
    Collection<QueryNode> nodesLeft = nodesByRef(ctx.left);
    Collection<QueryNode> nodesRight = nodesByRef(ctx.right);
    Preconditions.checkArgument(!nodesLeft.isEmpty(), errorLHS("common parent"));
    Preconditions.checkArgument(!nodesRight.isEmpty(), errorRHS("common parent"));
    
    String label = ctx.label == null ? null : ctx.label.getText();
    
    for(QueryNode left : nodesLeft)
    {
      for(QueryNode right : nodesRight)
      {
        left.addJoin(new Sibling(right, label));
      }
    }
  }

  @Override
  public void enterCommonAncestor(AqlParser.CommonAncestorContext ctx)
  {
    Collection<QueryNode> nodesLeft = nodesByRef(ctx.left);
    Collection<QueryNode> nodesRight = nodesByRef(ctx.right);
    Preconditions.checkArgument(!nodesLeft.isEmpty(), errorLHS("common ancestor"));
    Preconditions.checkArgument(!nodesRight.isEmpty(), errorRHS("common ancestor"));
    
    String label = ctx.label == null ? null : ctx.label.getText();
    
    for(QueryNode left : nodesLeft)
    {
      for(QueryNode right : nodesRight)
      {
        left.addJoin(new CommonAncestor(right, label));
      }
    }
  }
  
  
  
  
  
  @Override
  public void enterMetaTermExpr(AqlParser.MetaTermExprContext ctx)
  {
    // TODO: we have to disallow OR expressions with metadata, how can we
    // achvieve that?
    String namespace = ctx.id.namespace == null ? 
      null : ctx.id.namespace.getText();
    QueryAnnotation anno = new QueryAnnotation(namespace,
      ctx.id.name.getText());
    metaData.add(anno);
  }

  @Override
  public void enterIdentity(AqlParser.IdentityContext ctx)
  {
    join(ctx, Identical.class);
  }

  @Override
  public void enterVariableTermExpr(AqlParser.VariableTermExprContext ctx)
  {
    lastVariableDefinition = null;
    if(ctx != null)
    {
      String text = ctx.VAR_DEF().getText();
      // remove the trailing "#"
      if(text.endsWith("#"))
      {
        lastVariableDefinition = text.substring(0, text.length()-1);
      }
      else
      {
        lastVariableDefinition = text;
      }
    }
  }
  
  
  
  
  private LinkedList<QueryAnnotation> fromEdgeAnnotation(
    AqlParser.EdgeSpecContext ctx)
  {
    LinkedList<QueryAnnotation> annos = new LinkedList<QueryAnnotation>();
    for(AqlParser.EdgeAnnoContext annoCtx : ctx.edgeAnno())
    {
      String namespace = annoCtx.qName().namespace == null
        ? null : annoCtx.qName().namespace.getText();
      String name = annoCtx.qName().name.getText();
      String value = textFromSpec(annoCtx.value);
      QueryNode.TextMatching matching = textMatchingFromSpec(
        annoCtx.value, annoCtx.NEQ() != null);
      
      annos.add(new QueryAnnotation(namespace, name, value, matching));
      
    }
    return annos;
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
      return not ? QueryNode.TextMatching.EXACT_NOT_EQUAL : 
        QueryNode.TextMatching.EXACT_EQUAL;
    }
    else if (txt instanceof AqlParser.RegexTextSpecContext)
    {
     return  not ? QueryNode.TextMatching.REGEXP_NOT_EQUAL : 
       QueryNode.TextMatching.REGEXP_EQUAL;
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

  private Collection<QueryNode> nodesByRef(Token ref)
  {
    return nodes.get("" + ref.getText().substring(1));
  }

  /**
   * Automatically create a join from a node and a join class.
   *
   * This will automatically get the left and right hand refs
   * and will construct a new join specified by the type using reflection.
   *
   * @node
   * @type00
   */
  private void join(ParserRuleContext ctx, Class<? extends Join> type)
  {
    Collection<QueryNode> leftNodes = nodesByRef(ctx.getToken(AqlParser.REF, 0).getSymbol());
    Collection<QueryNode> rightNodes = nodesByRef(ctx.getToken(AqlParser.REF, 1).getSymbol());

    Preconditions.checkArgument(!leftNodes.isEmpty(), errorLHS(type.getSimpleName()));
    Preconditions.checkNotNull(!rightNodes.isEmpty(), errorRHS(type.getSimpleName()));
    
    for (QueryNode left : leftNodes)
    {
      for (QueryNode right : rightNodes)
      {
        try
        {
          Constructor<? extends Join> c = type.getConstructor(QueryNode.class);
          Join newJoin = c.newInstance(right);
          
          Preconditions.checkState(!alternativeStack.isEmpty(),
            "There must be an alternative on the stack in order to add a join");
          
          LogicClause clause = new LogicClause(LogicClause.Operator.LEAF);
          clause.setContent(left);
          clause.setJoin(newJoin);
        }
        catch (Exception ex)
        {
          log.error(null, ex);
        }
      }
    }
    
  }
  
  private QueryNode newNode()
  {
    QueryNode n = new QueryNode(++aliasCount);
    if(lastVariableDefinition == null)
    {
      n.setVariable("" + n.getId());
    }
    else
    {
      n.setVariable(lastVariableDefinition);
    }
    lastVariableDefinition = null;
    LogicClause c = new LogicClause(LogicClause.Operator.LEAF);
    c.setContent(n);
    alternativeStack.get(0).addChild(c);
    
    
    nodes.put(n.getVariable(), n);
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
