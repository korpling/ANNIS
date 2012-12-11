/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.ql.parser;

import annis.exceptions.AnnisQLSemanticsException;
import annis.exceptions.AnnisQLSyntaxException;
import annis.model.QueryNode;
import annis.model.QueryAnnotation;
import annis.ql.analysis.DepthFirstAdapter;
import annis.ql.node.AAndExpr;
import annis.ql.node.AAnnotationSearchExpr;
import annis.ql.node.AAnyNodeSearchExpr;
import annis.ql.node.AArityLingOp;
import annis.ql.node.ADirectDominanceSpec;
import annis.ql.node.ADirectPointingRelationSpec;
import annis.ql.node.ADirectPrecedenceSpec;
import annis.ql.node.ADirectSiblingSpec;
import annis.ql.node.ADocumentConstraintExpr;
import annis.ql.node.AEdgeAnnotation;
import annis.ql.node.AEdgeSpec;
import annis.ql.node.AEqualAnnoValue;
import annis.ql.node.AExactOverlapLingOp;
import annis.ql.node.AGroupedExpr;
import annis.ql.node.AIdentityLingOp;
import annis.ql.node.AImplicitAndExpr;
import annis.ql.node.AInclusionLingOp;
import annis.ql.node.AIndirectDominanceSpec;
import annis.ql.node.AIndirectPointingRelationSpec;
import annis.ql.node.AIndirectPrecedenceSpec;
import annis.ql.node.AIndirectSiblingSpec;
import annis.ql.node.ALeftAlignLingOp;
import annis.ql.node.ALeftLeftOrRight;
import annis.ql.node.ALeftOverlapLingOp;
import annis.ql.node.ALinguisticConstraintExpr;
import annis.ql.node.AMetaConstraintExpr;
import annis.ql.node.AOrExpr;
import annis.ql.node.AOverlapLingOp;
import annis.ql.node.ARangeDominanceSpec;
import annis.ql.node.ARangePointingRelationSpec;
import annis.ql.node.ARangePrecedenceSpec;
import annis.ql.node.ARangeSpec;
import annis.ql.node.ARegexpTextSpec;
import annis.ql.node.ARightAlignLingOp;
import annis.ql.node.ARightLeftOrRight;
import annis.ql.node.ARightOverlapLingOp;
import annis.ql.node.ARootLingOp;
import annis.ql.node.ASameAnnotationGroupLingOp;
import annis.ql.node.ATextSearchExpr;
import annis.ql.node.ATextSearchNotEqualExpr;
import annis.ql.node.ATokenArityLingOp;
import annis.ql.node.AUnequalAnnoValue;
import annis.ql.node.AWildTextSpec;
import annis.ql.node.PAnnoValue;
import annis.ql.node.PEdgeAnnotation;
import annis.ql.node.PExpr;
import annis.ql.node.PLingOp;
import annis.ql.node.Token;
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
import annis.sqlgen.model.SameSpan;
import annis.sqlgen.model.Sibling;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.Validate;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class ClauseAnalysis extends DepthFirstAdapter
{
  
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(ClauseAnalysis.class);

  private Map<String, QueryNode> nodes;
  private int aliasCount;
  private List<QueryAnnotation> metaAnnotations;
  private int precedenceBound;
  
  public ClauseAnalysis()
  {
    this(0, new ArrayList<QueryAnnotation>(), new LinkedHashMap<String, QueryNode>(), 0);
  }

  public ClauseAnalysis(int aliasCount, List<QueryAnnotation> metaAnnotations, Map<String, QueryNode> nodes, int precedenceBound)
  {
    this.aliasCount = aliasCount;
    this.metaAnnotations = metaAnnotations;
    this.nodes = nodes;
    this.precedenceBound = precedenceBound;
  }

  ///// Analysis interface
  public int nodesCount()
  {
    return nodes.size();
  }

  ///// Syntax Tree Walking
  // <editor-fold desc="Currently unsupported or illegal">
  @Override
  public void caseAOrExpr(AOrExpr node)
  {
    throw new UnsupportedOperationException("using OR in a predicate is not supported");
  }

  @Override
  public void caseAImplicitAndExpr(AImplicitAndExpr node)
  {
    throw new UnsupportedOperationException("using implicit AND in a predicate is not (yet?) supported");
  }

  @Override
  public void caseAGroupedExpr(AGroupedExpr node)
  {
    throw new UnsupportedOperationException("using a grouped expression in a predicate is not (yet?) supported");
  }

  @Override
  public void caseASameAnnotationGroupLingOp(ASameAnnotationGroupLingOp node)
  {
    throw new AnnisQLSyntaxException("@ lingop is currently unsupported");
  }

  @Override
  public void caseADocumentConstraintExpr(ADocumentConstraintExpr node)
  {
    throw new UnsupportedOperationException("using a document constraint expression in a predicate is not yet supported");
  }

  // </editor-fold>
  // <editor-fold desc="unary">
  @Override
  public void caseARootLingOp(ARootLingOp node)
  {
    QueryNode nleft = lhs(node);
    Validate.notNull(nleft, errorLHS("root"));
    nleft.setRoot(true);
  }

  @Override
  public void caseAArityLingOp(AArityLingOp node)
  {
    QueryNode nleft = lhs(node);
    Validate.notNull(nleft, errorLHS("arity"));
    nleft.setArity(annisRangeFromARangeSpec((ARangeSpec) node.getRangeSpec()));
  }

  @Override
  public void caseATokenArityLingOp(ATokenArityLingOp node)
  {
    QueryNode nleft = lhs(node);
    Validate.notNull(nleft, errorLHS("token-arity"));
    nleft.setTokenArity(annisRangeFromARangeSpec((ARangeSpec) node.getRangeSpec()));
  }

  @Override
  public void caseAIdentityLingOp(AIdentityLingOp node)
  {
    join(node, Identical.class);
  }


  // </editor-fold>
  // <editor-fold desc="Alignment">
  @Override
  public void caseAExactOverlapLingOp(AExactOverlapLingOp node)
  {
    join(node, SameSpan.class);
  }

  @Override
  public void caseALeftAlignLingOp(ALeftAlignLingOp node)
  {
    join(node, LeftAlignment.class);
  }

  @Override
  public void caseARightAlignLingOp(ARightAlignLingOp node)
  {
    join(node, RightAlignment.class);
  }

  @Override
  public void caseAInclusionLingOp(AInclusionLingOp node)
  {
    join(node, Inclusion.class);
  }

  @Override
  public void caseAOverlapLingOp(AOverlapLingOp node)
  {
    join(node, Overlap.class);
  }

  @Override
  public void caseALeftOverlapLingOp(ALeftOverlapLingOp node)
  {
    join(node, LeftOverlap.class);
  }

  @Override
  public void caseARightOverlapLingOp(ARightOverlapLingOp node)
  {
    join(node, RightOverlap.class);
  }

  // </editor-fold>
  // <editor-fold desc="Precedence">
  @Override
  public void caseADirectPrecedenceSpec(ADirectPrecedenceSpec node)
  {
    PLingOp parent = (PLingOp) node.parent();
    QueryNode left = lhs(parent);
    QueryNode right = rhs(parent);

    Validate.notNull(left, errorLHS(Precedence.class.getSimpleName()));
    Validate.notNull(right, errorRHS(Precedence.class.getSimpleName()));

    String name = token(node.getName());
    
    if(name == null)
    {
      left.addJoin(new Precedence(right, 1));
    }
    else
    {
      left.addJoin(new Precedence(right, 1, 1, name));
    }
  }

  @Override
  public void caseAIndirectPrecedenceSpec(AIndirectPrecedenceSpec node)
  {
    PLingOp parent = (PLingOp) node.parent();
    QueryNode left = lhs(parent);
    QueryNode right = rhs(parent);

    Validate.notNull(left, errorLHS(Precedence.class.getSimpleName()));
    Validate.notNull(right, errorRHS(Precedence.class.getSimpleName()));

    String name = token(node.getName());
    
    if (precedenceBound > 0)
    {
      left.addJoin(new Precedence(right, 1, precedenceBound, name));
    }
    else
    {
      left.addJoin(new Precedence(right, name));
    }
  }

  @Override
  public void caseARangePrecedenceSpec(ARangePrecedenceSpec node)
  {
    PLingOp parent = (PLingOp) node.parent();
    QueryNode left = lhs(parent);
    QueryNode right = rhs(parent);

    Validate.notNull(left, errorLHS(Precedence.class.getSimpleName()));
    Validate.notNull(right, errorRHS(Precedence.class.getSimpleName()));
    
    String name = token(node.getName());

    ARangeSpec spec = (ARangeSpec) node.getRangeSpec();
    int min = number(spec.getMin());
    if (min == 0)
    {
      throw new AnnisQLSyntaxException("Distance can't be 0");
    }
    if (spec.getMax() == null)
    {
      left.addJoin(new Precedence(right, min, name));
    }
    else
    {
      int max = number(spec.getMax());
      if (max == 0)
      {
        throw new AnnisQLSyntaxException("Distance can't be 0");
      }
      left.addJoin(new Precedence(right, min, max, name));
    }
  }

  // </editor-fold>
  // <editor-fold desc="Dominance">
  @Override
  public void caseADirectDominanceSpec(ADirectDominanceSpec node)
  {
    PLingOp parent = (PLingOp) node.parent();
    QueryNode left = lhs(parent);
    QueryNode right = rhs(parent);

    Validate.notNull(left, errorLHS(Dominance.class.getSimpleName()));
    Validate.notNull(right, errorRHS(Dominance.class.getSimpleName()));

    String name = token(node.getName());

    if (node.getLeftOrRight() != null)
    {
      if (node.getLeftOrRight() instanceof ALeftLeftOrRight)
      {
        left.addJoin(new LeftDominance(right, name));
      }
      else if (node.getLeftOrRight() instanceof ARightLeftOrRight)
      {
        left.addJoin(new RightDominance(right, name));
      }
      else
      {
        throw new AnnisQLSemanticsException("unknown direct dominance type, was either left nor right");
      }
    }
    else
    {
      left.addJoin(new Dominance(right, name, 1));
    }

    if (node.getEdgeSpec() != null)
    {
      LinkedList<QueryAnnotation> annotations = fromEdgeAnnotation((AEdgeSpec) node.getEdgeSpec());
      for (QueryAnnotation a : annotations)
      {
        right.addEdgeAnnotation(a);
      }
    }
  }

  @Override
  public void caseAIndirectDominanceSpec(AIndirectDominanceSpec node)
  {
    PLingOp parent = (PLingOp) node.parent();
    QueryNode left = lhs(parent);
    QueryNode right = rhs(parent);

    Validate.notNull(left, errorLHS(Dominance.class.getSimpleName()));
    Validate.notNull(right, errorRHS(Dominance.class.getSimpleName()));

    left.addJoin(new Dominance(right, token(node.getName())));
  }

  @Override
  public void caseARangeDominanceSpec(ARangeDominanceSpec node)
  {
    PLingOp parent = (PLingOp) node.parent();
    QueryNode left = lhs(parent);
    QueryNode right = rhs(parent);

    Validate.notNull(left, errorLHS(Dominance.class.getSimpleName()));
    Validate.notNull(right, errorRHS(Dominance.class.getSimpleName()));

    ARangeSpec rangeSpec = (ARangeSpec) node.getRangeSpec();
    if (rangeSpec.getMax() == null)
    {
      int distance = number(rangeSpec.getMin());
      if (distance == 0)
      {
        throw new AnnisQLSyntaxException("Distance can't be 0");
      }
      left.addJoin(new Dominance(right, token(node.getName()), distance));
    }
    else
    {
      int min = number(rangeSpec.getMin());
      int max = number(rangeSpec.getMax());
      if (min == 0 || max == 0)
      {
        throw new AnnisQLSyntaxException("Distance can't be 0");
      }
      left.addJoin(new Dominance(right, token(node.getName()), min, max));
    }

  }

  // </editor-fold>
  // <editor-fold desc="Sibling">
  @Override
  public void caseADirectSiblingSpec(ADirectSiblingSpec node)
  {
    PLingOp parent = (PLingOp) node.parent();
    QueryNode left = lhs(parent);
    QueryNode right = rhs(parent);

    Validate.notNull(left, errorLHS(Sibling.class.getSimpleName()));
    Validate.notNull(right, errorRHS(Sibling.class.getSimpleName()));

    left.addJoin(new Sibling(right, token(node.getName())));

    if (node.getEdgeSpec() != null)
    {
      LinkedList<QueryAnnotation> annotations = fromEdgeAnnotation((AEdgeSpec) node.getEdgeSpec());
      for (QueryAnnotation a : annotations)
      {
        left.addEdgeAnnotation(a);
        right.addEdgeAnnotation(a);
      }
    }
  }

  @Override
  public void caseAIndirectSiblingSpec(AIndirectSiblingSpec node)
  {
    PLingOp parent = (PLingOp) node.parent();
    QueryNode left = lhs(parent);
    QueryNode right = rhs(parent);

    Validate.notNull(left, errorLHS(CommonAncestor.class.getSimpleName()));
    Validate.notNull(right, errorRHS(CommonAncestor.class.getSimpleName()));

    left.addJoin(new CommonAncestor(right, token(node.getName())));
  }


  // </editor-fold>

  // <editor-fold desc="Pointing relations">

  @Override
  public void caseADirectPointingRelationSpec(ADirectPointingRelationSpec node)
  {
    PLingOp parent = (PLingOp) node.parent();
    QueryNode left = lhs(parent);
    QueryNode right = rhs(parent);

    Validate.notNull(left, errorLHS(PointingRelation.class.getSimpleName()));
    Validate.notNull(right, errorRHS(PointingRelation.class.getSimpleName()));

    String name = token(node.getName());

    left.addJoin(new PointingRelation(right, name, 1));

    if (node.getEdgeSpec() != null)
    {
      LinkedList<QueryAnnotation> annotations = fromEdgeAnnotation((AEdgeSpec) node.getEdgeSpec());
      for (QueryAnnotation a : annotations)
      {
        right.addEdgeAnnotation(a);
      }
    }
  }

  @Override
  public void caseARangePointingRelationSpec(ARangePointingRelationSpec node)
  {
    PLingOp parent = (PLingOp) node.parent();
    QueryNode left = lhs(parent);
    QueryNode right = rhs(parent);

    Validate.notNull(left, errorLHS(PointingRelation.class.getSimpleName()));
    Validate.notNull(right, errorRHS(PointingRelation.class.getSimpleName()));

    ARangeSpec rangeSpec = (ARangeSpec) node.getRangeSpec();
    if (rangeSpec.getMax() == null)
    {
      int distance = number(rangeSpec.getMin());
      if (distance == 0)
      {
        throw new AnnisQLSyntaxException("Distance can't be 0");
      }
      left.addJoin(new PointingRelation(right, token(node.getName()), distance));
    }
    else
    {
      int min = number(rangeSpec.getMin());
      int max = number(rangeSpec.getMax());
      if (min == 0 || max == 0)
      {
        throw new AnnisQLSyntaxException("Distance can't be 0");
      }
      left.addJoin(new PointingRelation(right, token(node.getName()), min, max));
    }
  }


  @Override
  public void caseAIndirectPointingRelationSpec(AIndirectPointingRelationSpec node)
  {
    PLingOp parent = (PLingOp) node.parent();
    QueryNode left = lhs(parent);
    QueryNode right = rhs(parent);

    Validate.notNull(left, errorLHS(PointingRelation.class.getSimpleName()));
    Validate.notNull(right, errorRHS(PointingRelation.class.getSimpleName()));

    left.addJoin(new PointingRelation(right, token(node.getName())));
  }

  // </editor-fold>


  @Override
  public void caseAMetaConstraintExpr(AMetaConstraintExpr node)
  {
    String annoText = textFromAnnoValue(node.getValue());
    QueryNode.TextMatching annoTextMatching = textMatchingFromAnnoValue(node.getValue());
    token(node.getName());

    QueryAnnotation annotation = 
      new QueryAnnotation(token(node.getNamespace()), token(node.getName()), annoText, annoTextMatching);
    metaAnnotations.add(annotation);
  }

  @Override
  public void caseAAndExpr(AAndExpr node)
  {
    for (PExpr expr : node.getExpr())
    {
      analyzeNestedPath(expr);
    }
  }

  // <editor-fold desc="Node expressions">
  @Override
  public void caseAAnnotationSearchExpr(AAnnotationSearchExpr node)
  {
    QueryNode target = newNode();

    QueryAnnotation anno = new QueryAnnotation(token(node.getAnnoNamespace()), 
      token(node.getAnnoType()));

    if("tok".equals(anno.getName()))
    {
      // special handling for explicit token search
      target.setToken(true);
      if(node.getAnnoValue() != null)
      {
        QueryNode.TextMatching textMatching = textMatchingFromAnnoValue(node.getAnnoValue());
        String text = textFromAnnoValue(node.getAnnoValue());
        
        target.setSpannedText(text, textMatching);
      }
    } // end if "tok" is annotation name
    else
    {
      // normal annotation
      if (node.getAnnoValue() != null)
      {
        QueryNode.TextMatching textMatching = textMatchingFromAnnoValue(node.getAnnoValue());
        String text = textFromAnnoValue(node.getAnnoValue());

        anno.setValue(text);
        anno.setTextMatching(textMatching);
      }

      target.addNodeAnnotation(anno);
    } // end else 
  }

  @Override
  public void caseATextSearchExpr(ATextSearchExpr node)
  {
    QueryNode context = newNode();

    Validate.notNull(node.getTextSpec(), 
      "a text search expression always needs a text specification");
    
    if (node.getTextSpec() instanceof AWildTextSpec)
    {
      context.setSpannedText(((AWildTextSpec) node.getTextSpec()).getText().getText(),
        QueryNode.TextMatching.EXACT_EQUAL);
    }
    else if (node.getTextSpec() instanceof ARegexpTextSpec)
    {
      context.setSpannedText(((ARegexpTextSpec) node.getTextSpec()).getRegexp().getText(),
        QueryNode.TextMatching.REGEXP_EQUAL);
    }

  }

  @Override
  public void caseATextSearchNotEqualExpr(ATextSearchNotEqualExpr node)
  {
    QueryNode target = newNode();

    Validate.notNull(node.getTextSpec(), 
      "a negative text search expression always needs a text specification");
    
    
    if (node.getTextSpec() instanceof AWildTextSpec)
    {
      target.setSpannedText(((AWildTextSpec) node.getTextSpec()).getText().getText(),
        QueryNode.TextMatching.EXACT_NOT_EQUAL);
    }
    else if (node.getTextSpec() instanceof ARegexpTextSpec)
    {
      target.setSpannedText(((ARegexpTextSpec) node.getTextSpec()).getRegexp().getText(),
        QueryNode.TextMatching.REGEXP_NOT_EQUAL);
    }

  }

  @Override
  public void caseAAnyNodeSearchExpr(AAnyNodeSearchExpr node)
  {
    newNode();
  }

  // </editor-fold>
  // <editor-fold desc="Complex helper" >
  private QueryNode newNode()
  {
    QueryNode n = new QueryNode(++aliasCount);
    n.setVariable("n" + n.getId());
    n.setMarker(n.getVariable());
    nodes.put(n.getVariable(), n);
    
    return n;
  }

  private ClauseAnalysis analyzeNestedPath(PExpr expr)
  {
    ClauseAnalysis nested = new ClauseAnalysis(aliasCount, metaAnnotations, nodes, precedenceBound);
    expr.apply(nested);
    aliasCount = nested.aliasCount;
    return nested;
  }

  private QueryNode.Range annisRangeFromARangeSpec(ARangeSpec spec)
  {
    String min = spec.getMin().getText();
    String max = spec.getMax() != null ? spec.getMax().getText() : null;

    if (max == null)
    {
      return new QueryNode.Range(Integer.parseInt(min), Integer.parseInt(min));
    }
    else
    {
      return new QueryNode.Range(Integer.parseInt(min), Integer.parseInt(max));
    }
  }

  /**
   * Automatically create a join from a node and a join class.
   *
   * This will automatically get the parent node, it's left and right hand node
   * and will construct a new join specified by the type using reflection.
   *
   * @node
   * @type
   */
  private void join(PLingOp node, Class<? extends Join> type)
  {
    QueryNode left = lhs(node);
    QueryNode right = rhs(node);

    Validate.notNull(left, errorLHS(type.getSimpleName()));
    Validate.notNull(right, errorRHS(type.getSimpleName()));
    try
    {
      Constructor<? extends Join> c = type.getConstructor(QueryNode.class);
      Join newJoin = c.newInstance(right);
      left.addJoin(newJoin);
    }
    catch (Exception ex)
    {
      log.error(null, ex);
    }
  }

  private LinkedList<QueryAnnotation> fromEdgeAnnotation(AEdgeSpec spec)
  {
    LinkedList<QueryAnnotation> result = new LinkedList<QueryAnnotation>();
    for (PEdgeAnnotation pAnno : spec.getEdgeAnnotation())
    {
      AEdgeAnnotation anno = (AEdgeAnnotation) pAnno;
      if (anno.getValue() != null)
      {
        String text = textFromAnnoValue(anno.getValue());
        QueryNode.TextMatching textMatching = textMatchingFromAnnoValue(anno.getValue());
        result.add(new QueryAnnotation(token(anno.getNamespace()), token(anno.getType()),
          text, textMatching));
      }
      else
      {
        result.add(new QueryAnnotation(token(anno.getNamespace()), token(anno.getType())));
      }
    }
    return result;
  }

  // </editor-fold>


  // <editor-fold desc="Conversion">
  private String textFromAnnoValue(PAnnoValue value)
  {
    Token text = null;

    if (value instanceof AUnequalAnnoValue)
    {
      AUnequalAnnoValue val = (AUnequalAnnoValue) value;
      if (val.getTextSpec() instanceof AWildTextSpec)
      {
        text = ((AWildTextSpec) val.getTextSpec()).getText();
      }
      else if (val.getTextSpec() instanceof ARegexpTextSpec)
      {
        text = ((ARegexpTextSpec) val.getTextSpec()).getRegexp();
      }
    }
    else if (value instanceof AEqualAnnoValue)
    {
      AEqualAnnoValue val = (AEqualAnnoValue) value;
      if (val.getTextSpec() instanceof AWildTextSpec)
      {
        text = ((AWildTextSpec) val.getTextSpec()).getText();
      }
      else if (val.getTextSpec() instanceof ARegexpTextSpec)
      {
        text = ((ARegexpTextSpec) val.getTextSpec()).getRegexp();
      }
    }

    Validate.notNull(text, "No text given for annotation search expression");
    return token(text);
  }

  private QueryNode.TextMatching textMatchingFromAnnoValue(PAnnoValue value)
  {
    QueryNode.TextMatching textMatching = null;

    if (value instanceof AUnequalAnnoValue)
    {
      AUnequalAnnoValue val = (AUnequalAnnoValue) value;
      if (val.getTextSpec() instanceof AWildTextSpec)
      {
        textMatching = QueryNode.TextMatching.EXACT_NOT_EQUAL;
      }
      else if (val.getTextSpec() instanceof ARegexpTextSpec)
      {
        textMatching = QueryNode.TextMatching.REGEXP_NOT_EQUAL;
      }
    }
    else if (value instanceof AEqualAnnoValue)
    {
      AEqualAnnoValue val = (AEqualAnnoValue) value;
      if (val.getTextSpec() instanceof AWildTextSpec)
      {
        textMatching = QueryNode.TextMatching.EXACT_EQUAL;
      }
      else if (val.getTextSpec() instanceof ARegexpTextSpec)
      {
        textMatching = QueryNode.TextMatching.REGEXP_EQUAL;
      }
    }

    Validate.notNull(textMatching, "No match operator given for annotation search expression");
    return textMatching;
  }

  private int number(Token token)
  {
    return Integer.parseInt(token(token));
  }

  private String token(Token token)
  {
    return token != null ? token.getText() : null;
  }

  private QueryNode lhs(PLingOp node)
  {
    String tok = lhsStr(node);
    if (tok == null)
    {
      return null;
    }
    return nodes.get("n" + tok);
  }

  private QueryNode rhs(PLingOp node)
  {
    String tok = rhsStr(node);
    if (tok == null)
    {
      return null;
    }
    return nodes.get("n" + tok);
  }

  private String lhsStr(PLingOp node)
  {
    return token(((ALinguisticConstraintExpr) node.parent()).getLhs());
  }

  private String rhsStr(PLingOp node)
  {
    return token(((ALinguisticConstraintExpr) node.parent()).getRhs());
  }

  private String errorLHS(String function)
  {
    return function + " operator needs a left-hand-side";
  }

  private String errorRHS(String function)
  {
    return function + " operator needs a right-hand-side";
  }

  // </editor-fold>
  // <editor-fold desc="Getter and Setter">
  public List<QueryAnnotation> getMetaAnnotations()
  {
    return metaAnnotations;
  }

  public Collection<QueryNode> getNodes()
  {
    return nodes.values();
  }

  public int getPrecedenceBound()
  {
    return precedenceBound;
  }

  public void setPrecedenceBound(int precedenceBound)
  {
    this.precedenceBound = precedenceBound;
  }
  
  
  // </editor-fold>

}
