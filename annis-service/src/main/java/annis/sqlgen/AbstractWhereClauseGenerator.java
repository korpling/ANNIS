package annis.sqlgen;

import annis.model.Join;
import annis.model.QueryAnnotation;
import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractWhereClauseGenerator extends
    TableAccessStrategyFactory implements WhereClauseSqlGenerator<QueryData>
{

  @Override
  public Set<String> whereConditions(QueryData queryData,
      List<QueryNode> alternative, String indent)
  {
    List<String> conditions = new ArrayList<>();

    for (QueryNode node : alternative)
    {

      // node constraints
      if (node.getSpannedText() != null)
      {
        addSpanConditions(conditions, queryData, node);
      }
      if (node.isToken())
      {
        addIsTokenConditions(conditions, queryData, node);
      }
      if (node.isRoot())
      {
        addIsRootConditions(conditions, queryData, node);
      }
      if (node.getArity() != null)
      {
        addNodeArityConditions(conditions, queryData, node);
      }
      if (node.getTokenArity() != null)
      {
        addTokenArityConditions(conditions, queryData, node);
      }

      // node joins
      for (Join join : node.getOutgoingJoins())
      {
        QueryNode target = join.getTarget();
        if (join instanceof SameSpan)
        {
          addSameSpanConditions(conditions, node, target, (SameSpan) join,
              queryData);
        } else if (join instanceof Identical)
        {
          addIdenticalConditions(conditions, node, target, (Identical) join,
              queryData);
        } else if (join instanceof LeftAlignment)
        {
          addLeftAlignmentConditions(conditions, node, target,
              (LeftAlignment) join, queryData);
        } else if (join instanceof RightAlignment)
        {
          addRightAlignmentConditions(conditions, node, target,
              (RightAlignment) join, queryData);
        } else if (join instanceof Inclusion)
        {
          addInclusionConditions(conditions, node, target, (Inclusion) join,
              queryData);
        } else if (join instanceof Overlap)
        {
          addOverlapConditions(conditions, node, target, (Overlap) join,
              queryData);
        } else if (join instanceof LeftOverlap)
        {
          addLeftOverlapConditions(conditions, target, node,
              (LeftOverlap) join, queryData);
        } else if (join instanceof RightOverlap)
        {
          addRightOverlapConditions(conditions, target, node,
              (RightOverlap) join, queryData);
        } else if (join instanceof Precedence)
        {
          addPrecedenceConditions(conditions, node, target, (Precedence) join,
              queryData);
        } else if (join instanceof Near)
        {
          addNearConditions(conditions, node, target, (Near) join,
              queryData);
        } else if (join instanceof Sibling)
        {
          addSiblingConditions(conditions, node, target, (Sibling) join,
              queryData);
        } else if (join instanceof CommonAncestor)
        {
          addCommonAncestorConditions(conditions, node, target,
              (CommonAncestor) join, queryData);
        } else if (join instanceof LeftDominance)
        {
          addLeftDominanceConditions(conditions, node, target,
              (LeftDominance) join, queryData);
        } else if (join instanceof RightDominance)
        {
          addRightDominanceConditions(conditions, node, target,
              (RightDominance) join, queryData);
        } else if (join instanceof Dominance)
        {
          addDominanceConditions(conditions, node, target, (Dominance) join,
              queryData);
        } else if (join instanceof PointingRelation)
        {
          addPointingRelationConditions(conditions, node, target,
              (PointingRelation) join, queryData);
        } else if (join instanceof EqualValue)
        {
          addEqualValueConditions(conditions, node, target, (EqualValue) join, queryData);
        } else if (join instanceof NotEqualValue)
        {
          addNotEqualValueConditions(conditions, node, target, (NotEqualValue) join, queryData);
        }
      }

      // node annotations
      int i = 0;
      for (QueryAnnotation annotation : node.getNodeAnnotations())
      {
        ++i;
        addAnnotationConditions(conditions, node, i, annotation,
            NODE_ANNOTATION_TABLE, queryData);
      }

      // relation annotations
      int j = 0;
      for (QueryAnnotation annotation : node.getEdgeAnnotations())
      {
        ++j;
        addAnnotationConditions(conditions, node, j, annotation,
            EDGE_ANNOTATION_TABLE, queryData);
      }
    }

    return new HashSet<>(conditions);
  }

  protected abstract void addSpanConditions(List<String> conditions,
      QueryData queryData, QueryNode node);

  protected abstract void addIsTokenConditions(List<String> conditions,
      QueryData queryData, QueryNode node);

  protected abstract void addIsRootConditions(List<String> conditions,
      QueryData queryData, QueryNode node);

  protected abstract void addNodeArityConditions(List<String> conditions,
      QueryData queryData, QueryNode node);

  protected abstract void addTokenArityConditions(List<String> conditions,
      QueryData queryData, QueryNode node);

  protected abstract void addSingleRelationCondition(QueryNode node,
      QueryNode target, List<String> conditions, Join join,
      final String relationType);

  protected abstract void addSiblingConditions(List<String> conditions,
      QueryNode node, QueryNode target, Sibling join, QueryData queryData);

  protected abstract void addCommonAncestorConditions(List<String> conditions,
      QueryNode node, QueryNode target, CommonAncestor join, QueryData queryData);

  protected abstract void addSameSpanConditions(List<String> conditions,
      QueryNode node, QueryNode target, SameSpan join, QueryData queryData);

  protected abstract void addIdenticalConditions(List<String> conditions,
      QueryNode node, QueryNode target, Identical join, QueryData queryData);

  protected abstract void addLeftAlignmentConditions(List<String> conditions,
      QueryNode node, QueryNode target, LeftAlignment join, QueryData queryData);

  protected abstract void addRightAlignmentConditions(List<String> conditions,
      QueryNode node, QueryNode target, RightAlignment join, QueryData queryData);

  protected abstract void addInclusionConditions(List<String> conditions,
      QueryNode node, QueryNode target, Inclusion join, QueryData queryData);

  protected abstract void addOverlapConditions(List<String> conditions,
      QueryNode node, QueryNode target, Overlap join, QueryData queryData);

  protected abstract void addLeftOverlapConditions(List<String> conditions,
      QueryNode target, QueryNode node, LeftOverlap join, QueryData queryData);

  protected abstract void addRightOverlapConditions(List<String> conditions,
      QueryNode target, QueryNode node, RightOverlap join, QueryData queryData);

  protected abstract void addPrecedenceConditions(List<String> conditions,
      QueryNode node, QueryNode target, Precedence join, QueryData queryData);

  protected abstract void addNearConditions(List<String> conditions,
      QueryNode node, QueryNode target, Near join, QueryData queryData);

  protected abstract void addAnnotationConditions(List<String> conditions,
      QueryNode node, int index, QueryAnnotation annotation, String table,
      QueryData queryData);

  protected abstract void addLeftDominanceConditions(List<String> conditions,
      QueryNode node, QueryNode target, LeftDominance join, QueryData queryData);

  protected abstract void addRightDominanceConditions(List<String> conditions,
      QueryNode node, QueryNode target, RightDominance join, QueryData queryData);

  protected abstract void addDominanceConditions(List<String> conditions,
      QueryNode node, QueryNode target, Dominance join, QueryData queryData);

  protected abstract void addPointingRelationConditions(
      List<String> conditions, QueryNode node, QueryNode target,
      PointingRelation join, QueryData queryData);
  
  protected abstract void addEqualValueConditions(
      List<String> conditions, QueryNode node, QueryNode target,
      EqualValue join, QueryData queryData);
  
  protected abstract void addNotEqualValueConditions(
      List<String> conditions, QueryNode node, QueryNode target,
      NotEqualValue join, QueryData queryData);

}