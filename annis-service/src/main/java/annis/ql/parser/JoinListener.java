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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import annis.exceptions.AnnisQLSemanticsException;
import annis.model.Join;
import annis.model.QueryAnnotation;
import annis.model.QueryNode;
import annis.ql.AqlParser;
import annis.ql.AqlParserBaseListener;
import annis.sqlgen.model.CommonAncestor;
import annis.sqlgen.model.Dominance;
import annis.sqlgen.model.EqualValue;
import annis.sqlgen.model.Identical;
import annis.sqlgen.model.Inclusion;
import annis.sqlgen.model.LeftAlignment;
import annis.sqlgen.model.LeftDominance;
import annis.sqlgen.model.LeftOverlap;
import annis.sqlgen.model.Near;
import annis.sqlgen.model.NotEqualValue;
import annis.sqlgen.model.Overlap;
import annis.sqlgen.model.PointingRelation;
import annis.sqlgen.model.Precedence;
import annis.sqlgen.model.RightAlignment;
import annis.sqlgen.model.RightDominance;
import annis.sqlgen.model.RightOverlap;
import annis.sqlgen.model.SameSpan;
import annis.sqlgen.model.Sibling;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class JoinListener extends AqlParserBaseListener
{
  
  private static final Logger log = LoggerFactory.getLogger(JoinListener.class);
  
  private final int precedenceBound;
  /** An array which has an entry for each alternative. 
   *  Each entry maps node variables to a collection of query nodes.
   */
  private final ArrayList<Map<String, QueryNode>> alternativeNodes;
  /** Maps a token interval to a query nodes.
   */
  private final List<Map<Interval, QueryNode>> tokenPositions;
  private int alternativeIndex;
  
  private ArrayList<QueryNode> relationChain = new ArrayList<>();
  private int relationIdx;
 
  /**
   * Constructor.
   * @param data The {@link QueryData} containing the already parsed nodes.
   * @param precedenceBound  maximal range of precedence
   * @param tokenPositionToNode maps a token interval to a query nodes
   */
  public JoinListener(QueryData data, int precedenceBound, List<Map<Interval, QueryNode>> tokenPositionToNode)
  {
    this.precedenceBound = precedenceBound;
    this.alternativeNodes = new ArrayList<>(data.getAlternatives().size());
    this.tokenPositions = tokenPositionToNode;
    
    for(List<QueryNode> alternative : data.getAlternatives())
    {
      HashMap<String, QueryNode> m = new HashMap<>();
      alternativeNodes.add(m);
      for(QueryNode n : alternative)
      {
        if(m.containsKey(n.getVariable()))
        {
          throw new AnnisQLSemanticsException(n, "A node variable name is only allowed once per normalized alternative");
        }
        m.put(n.getVariable(), n);
      }
    }
  }

  @Override
  public void enterAndExpr(AqlParser.AndExprContext ctx)
  {
    Preconditions.checkArgument(alternativeIndex < alternativeNodes.size());
  }

  @Override
  public void exitAndExpr(AqlParser.AndExprContext ctx)
  {
    alternativeIndex++;
  }


  @Override
  public void exitOperator(AqlParser.OperatorContext ctx)
  {
    relationIdx++;
  }

  @Override
  public void enterBindingRelation(AqlParser.BindingRelationContext ctx)
  {
    int numOfReferences = ctx.refOrNode().size();
    relationIdx = 0;
    relationChain.clear();
    relationChain.ensureCapacity(numOfReferences);
    
    for(int i=0; i < numOfReferences; i++)
    {
      QueryNode n = node(ctx.refOrNode(i));
      if(n == null)
      {
        throw new AnnisQLSemanticsException(
          AnnisParserAntlr.getLocation(ctx.getStart(), ctx.getStop()), 
          "invalid reference to '" + ctx.refOrNode(i).getText() + "'");
      }
      relationChain.add(i, n);
    }
  }

  @Override
  public void enterNonBindingRelation(AqlParser.NonBindingRelationContext ctx)
  {
    int numOfReferences = ctx.REF().size();
    relationIdx = 0;
    relationChain.clear();
    relationChain.ensureCapacity(numOfReferences);
    
    for(int i=0; i < numOfReferences; i++)
    {
      QueryNode n = nodeByRef(ctx.REF(i).getSymbol());
      if(n == null)
      {
        throw new AnnisQLSemanticsException(
          AnnisParserAntlr.getLocation(ctx.getStart(), ctx.getStop()), 
          "invalid reference to '" + ctx.REF(i).getText() + "'");
      }
      relationChain.add(i, n);
    }
  }
  
  @Override
  public void enterRootTerm(AqlParser.RootTermContext ctx)
  {
    QueryNode target = nodeByRef(ctx.left);
    Preconditions.checkArgument(target != null, errorLHS("root") 
      + ": " + ctx.getText());
    target.setRoot(true);
  }

  @Override
  public void enterArityTerm(AqlParser.ArityTermContext ctx)
  {
    QueryNode target = nodeByRef(ctx.left);
    Preconditions.checkArgument(target != null, errorLHS("arity") 
      + ": " + ctx.getText());
    target.setArity(annisRangeFromARangeSpec(ctx.rangeSpec()));
    
  }

  @Override
  public void enterTokenArityTerm(AqlParser.TokenArityTermContext ctx)
  {
    QueryNode target = nodeByRef(ctx.left);
    Preconditions.checkArgument(target != null, errorLHS("token-arity") 
      + ": " + ctx.getText());
    
    target.setTokenArity(annisRangeFromARangeSpec(ctx.rangeSpec()));
    
  }

  @Override
  public void enterDirectPrecedence(
    AqlParser.DirectPrecedenceContext ctx)
  {
    QueryNode left = relationChain.get(relationIdx);
    QueryNode right = relationChain.get(relationIdx+1);

    
    String segmentationName = null;
    if(ctx.NAMED_PRECEDENCE() != null)
    {
      segmentationName=ctx.NAMED_PRECEDENCE().getText().substring(1);
    }
    left.addOutgoingJoin(addParsedLocation(ctx, new Precedence(right, 1, segmentationName)));
    
  }
  

    @Override
  public void enterDirectNear(
    AqlParser.DirectNearContext ctx)
  {
    QueryNode left = relationChain.get(relationIdx);
    QueryNode right = relationChain.get(relationIdx+1);

    
    String segmentationName = getLayerName(ctx.NAMED_NEAR());
    left.addOutgoingJoin(addParsedLocation(ctx, new Near(right, 1, segmentationName)));
    
  }
  
  @Override
  public void enterEqualvalue(AqlParser.EqualvalueContext ctx)
  {
    QueryNode left = relationChain.get(relationIdx);
    QueryNode right = relationChain.get(relationIdx+1);
    
    left.addOutgoingJoin(addParsedLocation(ctx, new EqualValue(right)));
  }

  @Override
  public void enterNotequalvalue(annis.ql.AqlParser.NotequalvalueContext ctx)
  {
    QueryNode left = relationChain.get(relationIdx);
    QueryNode right = relationChain.get(relationIdx+1);
    
    left.addOutgoingJoin(addParsedLocation(ctx, new NotEqualValue(right)));
  }
  
  


  @Override
  public void enterIndirectPrecedence(
    AqlParser.IndirectPrecedenceContext ctx)
  {
    QueryNode left = relationChain.get(relationIdx);
    QueryNode right = relationChain.get(relationIdx+1);

    String segmentationName = getLayerName(ctx.NAMED_PRECEDENCE());
    
    if (precedenceBound > 0)
    {
      left.addOutgoingJoin(
        addParsedLocation(ctx, new Precedence(right, 1, precedenceBound, segmentationName)));
    }
    else
    {
      left.addOutgoingJoin(addParsedLocation(ctx, new Precedence(right, segmentationName)));
    }
  }

  
  @Override
  public void enterIndirectNear(
    AqlParser.IndirectNearContext ctx)
  {
    QueryNode left = relationChain.get(relationIdx);
    QueryNode right = relationChain.get(relationIdx+1);

    String segmentationName = getLayerName(ctx.NAMED_NEAR());
    
    if (precedenceBound > 0)
    {
      left.addOutgoingJoin(
        addParsedLocation(ctx, new Near(right, 1, precedenceBound, segmentationName)));
    }
    else
    {
      left.addOutgoingJoin(addParsedLocation(ctx, new Near(right, segmentationName)));
    }
  }
  
  @Override
  public void enterRangePrecedence(AqlParser.RangePrecedenceContext ctx)
  {
    QueryNode left = relationChain.get(relationIdx);
    QueryNode right = relationChain.get(relationIdx+1);

    QueryNode.Range range = annisRangeFromARangeSpec(ctx.rangeSpec());
    if(range.getMin() == 0 || range.getMax() == 0)
    {
       throw new AnnisQLSemanticsException(
         AnnisParserAntlr.getLocation(ctx.getStart(), ctx.getStop()),
         "Distance can't be 0");
    }
    else if(range.getMin() > range.getMax())
    {
      throw new AnnisQLSemanticsException(
         AnnisParserAntlr.getLocation(ctx.getStart(), ctx.getStop()),
         "Minimal distance can't be larger than maximal distance");
    }
    else
    {
      String segmentationName = getLayerName(ctx.NAMED_PRECEDENCE());
      
      left.addOutgoingJoin(
        addParsedLocation(ctx,
          new Precedence(right, range.getMin(), range.getMax(),
            segmentationName)));

    }
  }

    @Override
  public void enterRangeNear(AqlParser.RangeNearContext ctx)
  {
    QueryNode left = relationChain.get(relationIdx);
    QueryNode right = relationChain.get(relationIdx+1);

    QueryNode.Range range = annisRangeFromARangeSpec(ctx.rangeSpec());
    if(range.getMin() == 0 || range.getMax() == 0)
    {
      throw new AnnisQLSemanticsException(
         AnnisParserAntlr.getLocation(ctx.getStart(), ctx.getStop()),
         "Distance can't be 0");
    }
    else if(range.getMin() > range.getMax())
    {
      throw new AnnisQLSemanticsException(
         AnnisParserAntlr.getLocation(ctx.getStart(), ctx.getStop()),
         "Minimal distance can't be larger than maximal distance");
    }
    else
    {
      String segmentationName = getLayerName(ctx.NAMED_NEAR());
      
      left.addOutgoingJoin(
        addParsedLocation(ctx, new Near(right, range.getMin(), range.getMax(),
            segmentationName)));
      
    }
  }
  
  @Override
  public void enterIdenticalCoverage(AqlParser.IdenticalCoverageContext ctx)
  {
    join(ctx, SameSpan.class);
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
    QueryNode left = relationChain.get(relationIdx);
    QueryNode right = relationChain.get(relationIdx+1);
    
    String layer = getLayerName(ctx.NAMED_DOMINANCE());    

    Join j;
    

    if(ctx.LEFT_CHILD() != null)
    {
      j = new LeftDominance(right, layer);
    }
    else if(ctx.RIGHT_CHILD() != null)
    {
      j = new RightDominance(right, layer);
    }
    else
    {
      j = new Dominance(right, layer, 1);
    }
    left.addOutgoingJoin(addParsedLocation(ctx, j));
    if(ctx.anno != null)
    {
      LinkedList<QueryAnnotation> annotations = fromRelationAnnotation(ctx.anno);
      for (QueryAnnotation a : annotations)
      {
        j.addEdgeAnnotation(a);
      }
    }

  }

  @Override
  public void enterIndirectDominance(AqlParser.IndirectDominanceContext ctx)
  {
    QueryNode left = relationChain.get(relationIdx);
    QueryNode right = relationChain.get(relationIdx+1);

    String layer = getLayerName(ctx.NAMED_DOMINANCE());
   
    left.addOutgoingJoin(addParsedLocation(ctx, new Dominance(right, layer)));
  }

  @Override
  public void enterRangeDominance(AqlParser.RangeDominanceContext ctx)
  {
    QueryNode left = relationChain.get(relationIdx);
    QueryNode right = relationChain.get(relationIdx+1);
    
    String layer = getLayerName(ctx.NAMED_DOMINANCE());
   
    QueryNode.Range range = annisRangeFromARangeSpec(ctx.rangeSpec());
    if(range.getMin() == 0 || range.getMax() == 0)
    {
      throw new AnnisQLSemanticsException(
         AnnisParserAntlr.getLocation(ctx.getStart(), ctx.getStop()),
         "Distance can't be 0");
    }
    else if(range.getMin() > range.getMax())
    {
      throw new AnnisQLSemanticsException(
         AnnisParserAntlr.getLocation(ctx.getStart(), ctx.getStop()),
         "Minimal distance can't be larger than maximal distance");
    }
    
    left.addOutgoingJoin(addParsedLocation(ctx, new Dominance(right, layer, range.getMin(), range.getMax())));
  }

  @Override
  public void enterDirectPointing(AqlParser.DirectPointingContext ctx)
  {
    QueryNode left = relationChain.get(relationIdx);
    QueryNode right = relationChain.get(relationIdx+1);
    
    String label = getLayerName(ctx.POINTING(), 2);
    
    Join j = new PointingRelation(right, label, 1);
    if (ctx.anno != null)
    {
      LinkedList<QueryAnnotation> annotations = fromRelationAnnotation(ctx.anno);
      for (QueryAnnotation a : annotations)
      {
        j.addEdgeAnnotation(a);
      }
    }

    left.addOutgoingJoin(addParsedLocation(ctx, j));

  }

  @Override
  public void enterIndirectPointing(AqlParser.IndirectPointingContext ctx)
  {
    QueryNode left = relationChain.get(relationIdx);
    QueryNode right = relationChain.get(relationIdx+1);
    
    String label = getLayerName(ctx.POINTING(), 2);
   
    left.addOutgoingJoin(addParsedLocation(ctx, new PointingRelation(right, label)));
    
  }

  @Override
  public void enterRangePointing(AqlParser.RangePointingContext ctx)
  {
    QueryNode left = relationChain.get(relationIdx);
    QueryNode right = relationChain.get(relationIdx+1);
    
    String label = getLayerName(ctx.POINTING(), 2);
   
    QueryNode.Range range = annisRangeFromARangeSpec(ctx.rangeSpec());
    if(range.getMin() == 0 || range.getMax() == 0)
    {
      throw new AnnisQLSemanticsException(
         AnnisParserAntlr.getLocation(ctx.getStart(), ctx.getStop()),
         "Distance can't be 0");
    }
    else if(range.getMin() > range.getMax())
    {
      throw new AnnisQLSemanticsException(
         AnnisParserAntlr.getLocation(ctx.getStart(), ctx.getStop()),
         "Minimal distance can't be larger than maximal distance");
    }
    
    left.addOutgoingJoin(addParsedLocation(ctx, new PointingRelation(right, label, range.getMin(), range.getMax())));
  }

  @Override
  public void enterCommonparent(AqlParser.CommonparentContext ctx)
  {
    QueryNode left = relationChain.get(relationIdx);
    QueryNode right = relationChain.get(relationIdx+1);
    
    String label = ctx.label == null ? null : ctx.label.getText();
    
    left.addOutgoingJoin(addParsedLocation(ctx, new Sibling(right, label)));
  }

  @Override
  public void enterCommonancestor(AqlParser.CommonancestorContext ctx)
  {
    QueryNode left = relationChain.get(relationIdx);
    QueryNode right = relationChain.get(relationIdx+1);
    
    String label = ctx.label == null ? null : ctx.label.getText();
    
    left.addOutgoingJoin(addParsedLocation(ctx, new CommonAncestor(right, label)));
  }
  
  @Override
  public void enterIdentity(AqlParser.IdentityContext ctx)
  {
    join(ctx, Identical.class);
  }
  
  /**
   * Automatically create a join from a node and a join class.
   *
   * This will automatically get the left and right hand refs
   * and will construct a new join specified by the type using reflection.
   * 
   * It will also add an parsed location to the join.
   *
   * @node
   * @type00
   */
  private void join(ParserRuleContext ctx, Class<? extends Join> type)
  {
    QueryNode left = relationChain.get(relationIdx);
    QueryNode right = relationChain.get(relationIdx+1);

    try
    {
      Constructor<? extends Join> c = type.getConstructor(QueryNode.class);
      Join newJoin = c.newInstance(right);
      left.addOutgoingJoin(addParsedLocation(ctx, newJoin));
      
    }
    catch (NoSuchMethodException ex)
    {
      log.error(null, ex);
    }
    catch (InstantiationException ex)
    {
      log.error(null, ex);
    }
    catch (IllegalAccessException ex)
    {
      log.error(null, ex);
    }
    catch (InvocationTargetException ex)
    {
      log.error(null, ex);
    }

  }
  
  private Join addParsedLocation(ParserRuleContext ctx, Join j)
  {
    j.setParseLocation(AnnisParserAntlr.getLocation(ctx.getStart(), ctx.getStop()));
    return j;
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
  
  private LinkedList<QueryAnnotation> fromRelationAnnotation(
    AqlParser.EdgeSpecContext ctx)
  {
    LinkedList<QueryAnnotation> annos = new LinkedList<>();
    for(AqlParser.EdgeAnnoContext annoCtx : ctx.edgeAnno())
    {
      String namespace = annoCtx.qName().namespace == null
        ? null : annoCtx.qName().namespace.getText();
      String name = annoCtx.qName().name.getText();
      String value = QueryNodeListener.textFromSpec(annoCtx.value);
      QueryNode.TextMatching matching = QueryNodeListener.textMatchingFromSpec(
        annoCtx.value, annoCtx.NEQ() != null);
      
      annos.add(new QueryAnnotation(namespace, name, value, matching));
      
    }
    return annos;
  }
  
  private String getLayerName(TerminalNode node)
  {
    return getLayerName(node, 1);
  }
  
  private String getLayerName(TerminalNode node, int lengthOfOperator)
  {
    if(node == null || node.getText() == null)
    {
      return null;
    }
    return node.getText().substring(lengthOfOperator);
  }
  
  private QueryNode node(AqlParser.RefOrNodeContext ctx)
  {
    if(ctx instanceof AqlParser.ReferenceNodeContext)
    {
      return nodeByDef((AqlParser.ReferenceNodeContext) ctx);
    }
    else if(ctx instanceof AqlParser.ReferenceRefContext)
    {
      return nodeByRef(((AqlParser.ReferenceRefContext) ctx).REF().getSymbol());
    }
    else
    {
      return null;
    }
  }
  
  private QueryNode nodeByDef(AqlParser.ReferenceNodeContext ctx)
  {
    if(ctx.VAR_DEF() == null)
    {
      QueryNode result = tokenPositions.get(alternativeIndex).get(ctx.variableExpr().getSourceInterval());
      if(result == null)
      {
        return null;
      }
      else
      {
        return result;
      }
    }
    else
    {
      String varDefText = ctx.VAR_DEF().getText();
      // remove trailing #
      varDefText = varDefText.substring(0, varDefText.length()-1);
      return alternativeNodes.get(alternativeIndex).get(varDefText);
    }
  }
  
  private QueryNode nodeByRef(Token ref)
  {
    return alternativeNodes.get(alternativeIndex).get("" + ref.getText().substring(1));
  }
  
  
  private String errorLHS(String function)
  {
    return function + " operator needs a left-hand-side";
  }
/*
  private String errorRHS(String function)
  {
    return function + " operator needs a right-hand-side";
  }
  */
}
