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
package annis.sqlgen;

import annis.model.Join;
import annis.model.QueryAnnotation;
import annis.model.QueryNode;
import annis.model.QueryNode.TextMatching;
import annis.ql.parser.QueryData;
import static annis.sqlgen.SqlConstraints.between;
import static annis.sqlgen.SqlConstraints.in;
import static annis.sqlgen.SqlConstraints.isNotNull;
import static annis.sqlgen.SqlConstraints.isNull;
import static annis.sqlgen.SqlConstraints.isTrue;
import static annis.sqlgen.SqlConstraints.join;
import static annis.sqlgen.SqlConstraints.mirrorJoin;
import static annis.sqlgen.SqlConstraints.numberJoin;
import static annis.sqlgen.SqlConstraints.or;
import static annis.sqlgen.SqlConstraints.sqlString;
import static annis.sqlgen.TableAccessStrategy.COMPONENT_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;
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
import annis.sqlgen.model.RankTableJoin;
import annis.sqlgen.model.RightAlignment;
import annis.sqlgen.model.RightDominance;
import annis.sqlgen.model.RightOverlap;
import annis.sqlgen.model.SameSpan;
import annis.sqlgen.model.Sibling;
import com.google.common.base.Joiner;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public class DefaultWhereClauseGenerator extends AbstractWhereClauseGenerator
{

  // allow binding of same node to both operands of sibling
  private boolean allowIdenticalSibling;
  // generate two-sided boundaries for both left and right text borders
  // for the inclusion operators
  private boolean optimizeInclusion;
  // where to attach component constraints for relation operators
  // (lhs, rhs or both)
  private String componentPredicates;
  // use dedicated is_token column
  private boolean useIsTokenColumn;
  // use predicate on toplevel_corpus in EXISTS subquery for common ancestor operator
  private boolean useToplevelCorpusPredicateInCommonAncestorSubquery;
  // use predicate on component_ref before and in EXISTS subquery for common ancestor operator
  private boolean useComponentRefPredicateInCommonAncestorSubquery;
  
  private boolean hackOperatorSameSpan;
  
  private AnnotationConditionProvider annoCondition;
  
  
  
  void joinOnNode(List<String> conditions, QueryNode node, QueryNode target,
    String operator, String leftColumn, String rightColumn)
  {
    conditions.add(join(operator,
      tables(node).aliasedColumn(NODE_TABLE, leftColumn), tables(target).
      aliasedColumn(NODE_TABLE, rightColumn)));
  }
  
    void mirrorJoinOnNode(List<String> conditions, QueryNode node, QueryNode target,
    String operator, String leftColumn, String rightColumn)
  {
    conditions.add(mirrorJoin(operator,
      tables(node).aliasedColumn(NODE_TABLE, leftColumn), tables(target).
      aliasedColumn(NODE_TABLE, rightColumn)));
  }


  void betweenJoinOnNode(List<String> conditions, QueryNode node,
    QueryNode target, String leftColumn, String rightColumn, int min, int max)
  {
    conditions.add(between(tables(node).aliasedColumn(NODE_TABLE, leftColumn),
      tables(target).aliasedColumn(NODE_TABLE, rightColumn), min, max));
  }

  void numberJoinOnNode(List<String> conditions, QueryNode node,
    QueryNode target, String operator, String leftColumn, String rightColumn,
    int offset)
  {
    conditions.add(numberJoin(operator,
      tables(node).aliasedColumn(NODE_TABLE, leftColumn), tables(target).
      aliasedColumn(NODE_TABLE, rightColumn), offset));
  }
  
  
  /**
   * Explicitly disallow reflexivity.
   * 
   * Can be used if the other conditions allow reflexivity but the operator not.
   * It depends on the search conditions if two results are not equal.
   * 
   * <p>
   * <b>For two nodes (including searches for token):</b> <br />
   * node IDs are different
   * </p>
   * 
   * 
   * <p>
   * <b>If both nodes are an annotation:</b> <br />
   * node IDs are different or annotation namespace+name are different
   * </p>
   * 
   * <p>
   * <b>For a node with and one without annotation condition:</b> <br />
   * always different
   * </p>
   * 
   * 
   * @param conditions
   * @param node
   * @param target 
   */
  private void notReflexive(List<String> conditions,
    QueryNode node, QueryNode target)
  {
    Validate.isTrue(node != target, "notReflexive(...) implies that source "
      + "and target node are not the same, but someone is violating this constraint!");
    Validate.notNull(node);
    Validate.notNull(target);
    
    if(node.getNodeAnnotations().isEmpty() && target.getNodeAnnotations().isEmpty())
    {    
      joinOnNode(conditions, node, target, "<>", "id", "id");
    }
    else if(!node.getNodeAnnotations().isEmpty() && !target.getNodeAnnotations().isEmpty())
    {
      TableAccessStrategy tasNode = tables(node);
      TableAccessStrategy tasTarget = tables(target);
      
      String nodeDifferent = join("<>",
        tasNode.aliasedColumn(NODE_TABLE, "id"), 
        tasTarget.aliasedColumn(NODE_TABLE, "id"));
      
      String annoCatDifferent = join("IS DISTINCT FROM",
        tasNode.aliasedColumn(NODE_ANNOTATION_TABLE, "category"),
        tasTarget.aliasedColumn(NODE_ANNOTATION_TABLE, "category"));
      
      conditions.add("(" 
        + Joiner.on(" OR ").join(nodeDifferent, annoCatDifferent) 
        + ")");
      
    }
  }

  
  private void addComponentPredicates(List<String> conditions, QueryNode node,
    final String relationType, String componentName)
  {
    conditions.add(join("=", tables(node).aliasedColumn(COMPONENT_TABLE, "type"),
      sqlString(relationType)));
    if (componentName == null)
    {
      conditions.add(isNull(tables(node).aliasedColumn(COMPONENT_TABLE, "name")));
    }
    else
    {
      conditions.add(join("=",
        tables(node).aliasedColumn(COMPONENT_TABLE, "name"),
        sqlString(componentName)));
    }
  }

  private void addComponentPredicates(List<String> conditions, QueryNode node,
    QueryNode target, String componentName, String relationType, boolean addComponentIDJoin)
  {
    if(addComponentIDJoin)
    {
      conditions.add(join("=", 
        tables(node).aliasedColumn(COMPONENT_TABLE, "id"), 
        tables(target).aliasedColumn(COMPONENT_TABLE, "id")));
    }
    
    if ("lhs".equals(componentPredicates) || "both".equals(componentPredicates))
    {
      addComponentPredicates(conditions, node, relationType, componentName);
    }
    if ("rhs".equals(componentPredicates) || "both".equals(componentPredicates))
    {
      addComponentPredicates(conditions, target, relationType, componentName);
    }
  }

  @Override
  protected void addPointingRelationConditions(List<String> conditions,
    QueryNode node, QueryNode target, PointingRelation join,
    QueryData queryData)
  {
    addSingleRelationCondition(node, target, conditions, join, "p");
  }

  @Override
  protected void addDominanceConditions(List<String> conditions,
    QueryNode node, QueryNode target, Dominance join, QueryData queryData)
  {
    addSingleRelationCondition(node, target, conditions, join, "d");
  }

  @Override
  protected void addRightDominanceConditions(List<String> conditions,
    QueryNode node, QueryNode target, RightDominance join, QueryData queryData)
  {
    addLeftOrRightDominance(conditions, node, target, queryData, join, "max",
      "right_token");
  }

  @Override
  protected void addLeftDominanceConditions(List<String> conditions,
    QueryNode node, QueryNode target, LeftDominance join, QueryData queryData)
  {
    addLeftOrRightDominance(conditions, node, target, queryData, join, "min",
      "left_token");
  }

  // FIXME: why not in addSingleRelationConditions() ?
  protected void addLeftOrRightDominance(List<String> conditions, QueryNode node,
    QueryNode target, QueryData queryData, RankTableJoin join,
    String aggregationFunction, String tokenBoarder)
  {
    RankTableJoin rankTableJoin = (RankTableJoin) join;
    String componentName = rankTableJoin.getName();
    
    // the parent and rank_id are already unique, so don't add extra join on component_id
    addComponentPredicates(conditions, node, target, componentName, "d", false);

    conditions.add(join("=", tables(node).aliasedColumn(RANK_TABLE, "id"),
      tables(target).aliasedColumn(RANK_TABLE, "parent")));

    List<Long> corpusList = queryData.getCorpusList();
    
    
    String innerSelect = 
       "SELECT "
      + aggregationFunction
      + "(lrsub."
      + tokenBoarder
      + ") FROM ";
    
    innerSelect += SelectedFactsFromClauseGenerator.selectedFactsSQL(corpusList, "")
      + " AS lrsub ";

    
    innerSelect +=
        "WHERE parent="
      + tables(node).aliasedColumn(RANK_TABLE, "id")
      + " AND component_id = " + tables(node).aliasedColumn(RANK_TABLE,
        "component_id")
      + " AND corpus_ref="
      + tables(target).aliasedColumn(NODE_TABLE, "corpus_ref")
      + " AND lrsub.toplevel_corpus IN("
      + (corpusList == null || corpusList.isEmpty() ? "NULL"
      : StringUtils.join(corpusList, ",")) + ")";
    
    conditions.add(in(
      tables(target).aliasedColumn(NODE_TABLE, tokenBoarder), innerSelect));
  }

  @Override
  protected void addAnnotationConditions(List<String> conditions,
    QueryNode node, int index, QueryAnnotation annotation, String table,
    QueryData queryData)
  {
    annoCondition.addAnnotationConditions(conditions, index, annotation,
      table, tables(node));
  }

  @Override
  protected void addPrecedenceConditions(List<String> conditions,
    QueryNode node, QueryNode target, Precedence join, QueryData queryData)
  {
    joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");

    int min = join.getMinDistance();
    int max = join.getMaxDistance();

    String left = join.getSegmentationName() == null ? "left_token" : "seg_index";
    String right = join.getSegmentationName() == null ? "right_token" : "seg_index";
    
    // we are using a special segmentation
    if(join.getSegmentationName() != null)
    {
      conditions.add(join("=",  
        tables(node).aliasedColumn(NODE_TABLE, "seg_name"), 
        sqlString(join.getSegmentationName()))); 
      
      conditions.add(join("=",  
        tables(target).aliasedColumn(NODE_TABLE, "seg_name"), 
        sqlString(join.getSegmentationName()))); 
    }
    
    
    // indirect
    if (min == 0 && max == 0)
    {
      joinOnNode(conditions, node, target, "<", right, left);
    }
    // exact distance
    else if (min == max)
    {
      numberJoinOnNode(conditions, node, target, "=", right,
        left, -min);

    }
    // ranged distance
    else
    {
      betweenJoinOnNode(conditions, node, target, right, left,
        -min, -max);
    }
  }

    @Override
  protected void addNearConditions(List<String> conditions,
    QueryNode node, QueryNode target, Near join, QueryData queryData)
  {
    joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");

    int min = join.getMinDistance();
    int max = join.getMaxDistance();

    String left = join.getSegmentationName() == null ? "left_token" : "seg_index";
    String right = join.getSegmentationName() == null ? "right_token" : "seg_index";
    
    // we are using a special segmentation
    if(join.getSegmentationName() != null)
    {
      conditions.add(join("=",  
        tables(node).aliasedColumn(NODE_TABLE, "seg_name"), 
        sqlString(join.getSegmentationName()))); 
      
      conditions.add(join("=",  
        tables(target).aliasedColumn(NODE_TABLE, "seg_name"), 
        sqlString(join.getSegmentationName()))); 
    }
    
    
    // indirect
    if (min == 0 && max == 0)
    {
      mirrorJoinOnNode(conditions, node, target, "<", right, left);
    }
    // exact distance
    else if (min == max)
    {
      conditions.add(
        or(
          numberJoin("=",
            tables(node).aliasedColumn(NODE_TABLE, right), 
            tables(target).aliasedColumn(NODE_TABLE, left), -min),
          numberJoin("=",
            tables(target).aliasedColumn(NODE_TABLE, right), 
            tables(node).aliasedColumn(NODE_TABLE, left), -min)
        ));
    }
    // ranged distance
    else
    {
      
      conditions.add(
        or(
          between(
            tables(node).aliasedColumn(NODE_TABLE, right),
            tables(target).aliasedColumn(NODE_TABLE, left), -min, -max),
          between(
            tables(target).aliasedColumn(NODE_TABLE, right),
            tables(node).aliasedColumn(NODE_TABLE, left), -min, -max)
        ));
    }
  }
  
  
  @Override
  protected void addRightOverlapConditions(List<String> conditions,
    QueryNode target, QueryNode node, RightOverlap join, QueryData queryData)
  {
    joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
    joinOnNode(conditions, node, target, ">=", "right_token", "right_token");
    joinOnNode(conditions, target, node, ">=", "right_token", "left_token");
    joinOnNode(conditions, node, target, ">=", "left_token", "left_token");
    notReflexive(conditions, node, target);
  }

  @Override
  protected void addLeftOverlapConditions(List<String> conditions,
    QueryNode target, QueryNode node, LeftOverlap join, QueryData queryData)
  {
    joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
    joinOnNode(conditions, node, target, "<=", "left_token", "left_token");
    joinOnNode(conditions, target, node, "<=", "left_token", "right_token");
    joinOnNode(conditions, node, target, "<=", "right_token", "right_token");
    notReflexive(conditions, node, target);
  }

  @Override
  protected void addOverlapConditions(List<String> conditions, QueryNode node,
    QueryNode target, Overlap join, QueryData queryData)
  {
    joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
    joinOnNode(conditions, node, target, "<=", "left_token", "right_token");
    joinOnNode(conditions, target, node, "<=", "left_token", "right_token");
    notReflexive(conditions, node, target);
  }

  @Override
  protected void addInclusionConditions(List<String> conditions,
    QueryNode node, QueryNode target, Inclusion join, QueryData queryData)
  {
    joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
    joinOnNode(conditions, node, target, "<=", "left_token", "left_token");
    joinOnNode(conditions, node, target, ">=", "right_token", "right_token");
    if (optimizeInclusion)
    {
      joinOnNode(conditions, target, node, "<=", "left_token", "right_token");
      joinOnNode(conditions, target, node, ">=", "right_token", "left_token");
    }
    notReflexive(conditions, node, target);
  }

  @Override
  protected void addRightAlignmentConditions(List<String> conditions,
    QueryNode node, QueryNode target, RightAlignment join, QueryData queryData)
  {
    joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
    joinOnNode(conditions, node, target, "=", "right_token", "right_token");
    notReflexive(conditions, node, target);
  }

  @Override
  protected void addLeftAlignmentConditions(List<String> conditions,
    QueryNode node, QueryNode target, LeftAlignment join, QueryData queryData)
  {
    joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
    joinOnNode(conditions, node, target, "=", "left_token", "left_token");
    notReflexive(conditions, node, target);
  }

  @Override
  protected void addIdenticalConditions(List<String> conditions,
    QueryNode node, QueryNode target, Identical join, QueryData queryData)
  {
    if(node.getNodeAnnotations().isEmpty() && target.getNodeAnnotations().isEmpty())
    {    
      joinOnNode(conditions, node, target, "=", "id", "id");
    }
    else if(!node.getNodeAnnotations().isEmpty() && !target.getNodeAnnotations().isEmpty())
    {
      TableAccessStrategy tasNode = tables(node);
      TableAccessStrategy tasTarget = tables(target);
      joinOnNode(conditions, node, target, "=", "id", "id");
      
      conditions.add(join("IS NOT DISTINCT FROM", 
        tasNode.aliasedColumn(NODE_ANNOTATION_TABLE, "category"),
        tasTarget.aliasedColumn(NODE_ANNOTATION_TABLE, "category")));
    }
    else
    {
      // the identity join between a node and an annotation condition is always false
      conditions.add("FALSE");
    }
  }

  @Override
  protected void addSameSpanConditions(List<String> conditions, QueryNode node,
    QueryNode target, SameSpan join, QueryData queryData)
  {
    joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
    joinOnNode(conditions, node, target, "=", "left_token", "left_token");
    if(hackOperatorSameSpan)
    {
      /* HACK: 
      When joining on both left_token and right_token 
      PostgreSQL will multiply the selectivity of both operations and this
      is not how the data works. left_token is not independent of right_token
      (the latter one is always larger) which is something the planner won't recognize.
      The actual solution would be to use the range data type for the token coverage
      and hope that PostgreSQL has proper statistics support (at the time of writing
      it hasn't). We use the custom ^=^ operator which is the same as the "="
      operator but has a constant join selectivity of 0.995. At least as long
      as PostgreSQL doesn't change the
      */
      joinOnNode(conditions, node, target, "^=^", "right_token", "right_token");
    }
    else
    {
      joinOnNode(conditions, node, target, "=", "right_token", "right_token");
    }
    
    notReflexive(conditions, node, target);
  }

  @Override
  protected void addEqualValueConditions(List<String> conditions, QueryNode node,
    QueryNode target, EqualValue join, QueryData queryData)
  {
    annoCondition.addEqualValueConditions(conditions, node, target, tables(node), tables(
          target), true);
  }

  @Override
  protected void addNotEqualValueConditions(List<String> conditions,
    QueryNode node, QueryNode target, NotEqualValue join, QueryData queryData)
  {
    annoCondition.addEqualValueConditions(conditions, node, target, tables(node), tables(
          target), false);
  }
  
  
  

  @Override
  protected void addCommonAncestorConditions(List<String> conditions,
    QueryNode node, QueryNode target, CommonAncestor join, QueryData queryData)
  {
    List<Long> corpusList = queryData.getCorpusList();
    String componentName = join.getName();
    addComponentPredicates(conditions, node, target, componentName, "d", true);

    if (!allowIdenticalSibling)
    {
      joinOnNode(conditions, node, target, "<>", "id", "id");
    }

    if (useComponentRefPredicateInCommonAncestorSubquery)
    {
      conditions.add(join("=",
        tables(node).aliasedColumn(RANK_TABLE, "component_ref"),
        tables(target).aliasedColumn(RANK_TABLE, "component_ref")));
    }

    // fugly
    TableAccessStrategy tas = tables(null);
    String pre1 = tables(node).aliasedColumn(RANK_TABLE, "pre");
    String pre2 = tables(target).aliasedColumn(RANK_TABLE, "pre");
    String pre = TableAccessStrategy.column("ancestor", tas.columnName(RANK_TABLE, "pre"));
    String post = TableAccessStrategy.column("ancestor", tas.columnName(RANK_TABLE, "post"));
    String componentAncestor= TableAccessStrategy.column("ancestor", tas.columnName(RANK_TABLE,
      "component_ref"));
    String componentSource
      = tables(node).aliasedColumn(RANK_TABLE, "component_ref");
    String componentTarget
      = tables(target).aliasedColumn(RANK_TABLE, "component_ref");

    String factsTable = SelectedFactsFromClauseGenerator.selectedFactsSQL(corpusList, "");
    
    StringBuffer sb = new StringBuffer();
    sb.append("EXISTS (SELECT 1 FROM " + factsTable
      + " AS ancestor WHERE\n");
    if (useComponentRefPredicateInCommonAncestorSubquery)
    {
      sb.append("\t" + componentAncestor + " = " + componentSource + " AND\n");
      sb.append("\t" + componentAncestor + " = " + componentTarget + " AND\n");
    }
    sb.append("\t" + pre + " < " + pre1 + " AND " + pre1 + " < " + post
      + " AND\n");
    sb.append("\t"
      + pre
      + " < "
      + pre2
      + " AND "
      + pre2
      + " < "
      + post);
    if (useToplevelCorpusPredicateInCommonAncestorSubquery && !(corpusList
      == null || corpusList.isEmpty()))
    {
      sb.append(" AND toplevel_corpus IN(");
      sb.append(StringUtils.join(corpusList, ","));
      sb.append(")");
    }
    // even if EXISTS implies that a single row is enought, PostgreSQL still
    // creates better plans if explicitly limited to one row
    sb.append("\n\tLIMIT 1");
    
    sb.append(")");
    conditions.add(sb.toString());
  }

  // FIXME: Why not in addSingleRelationCondition
  @Override
  protected void addSiblingConditions(List<String> conditions, QueryNode node,
    QueryNode target, Sibling join, QueryData queryData)
  {
    Sibling sibling = (Sibling) join;
    String componentName = sibling.getName();
    
    // "parent" column is unique over all component IDs, thus don't add an extra join
    addComponentPredicates(conditions, node, target, componentName, "d", false);

    conditions.add(join("=", tables(node).aliasedColumn(RANK_TABLE, "parent"),
      tables(target).aliasedColumn(RANK_TABLE, "parent")));

    if (!allowIdenticalSibling)
    {
      joinOnNode(conditions, node, target, "<>", "id", "id");
    }
  }

  @Override
  protected void addSingleRelationCondition(QueryNode node, QueryNode target,
    List<String> conditions, Join join, final String relationType)
  {
    RankTableJoin rankTableJoin = (RankTableJoin) join;
    String componentName = rankTableJoin.getName();
    
    int min = rankTableJoin.getMinDistance();
    int max = rankTableJoin.getMaxDistance();

    // direct
    if (min == 1 && max == 1)
    {
       addComponentPredicates(conditions, node, target, componentName, relationType, false);
      
      conditions.add(join("=", tables(node).aliasedColumn(RANK_TABLE, "id"),
        tables(target).aliasedColumn(RANK_TABLE, "parent")));

      // indirect"
    }
    else
    {
      addComponentPredicates(conditions, node, target, componentName, relationType, true);
      
      conditions.add(join("<", tables(node).aliasedColumn(RANK_TABLE, "pre"),
        tables(target).aliasedColumn(RANK_TABLE, "pre")));
      conditions.add(join("<", tables(target).aliasedColumn(RANK_TABLE, "pre"),
        tables(node).aliasedColumn(RANK_TABLE, "post")));

      // exact
      if (min > 0 && min == max)
      {
        conditions.add(numberJoin("=",
          tables(node).aliasedColumn(RANK_TABLE, "level"), tables(target).
          aliasedColumn(RANK_TABLE, "level"), -min));

        // range
      }
      else if (min > 0 && min < max)
      {
        conditions.add(between(tables(node).aliasedColumn(RANK_TABLE, "level"),
          tables(target).aliasedColumn(RANK_TABLE, "level"), -min, -max));
      }
    }
  }

  @Override
  protected void addTokenArityConditions(List<String> conditions,
    QueryData queryData, QueryNode node)
  {
    QueryNode.Range tokenArity = node.getTokenArity();
    if (tokenArity.getMin() == tokenArity.getMax())
    {
      conditions.add(numberJoin("=",
        tables(node).aliasedColumn(NODE_TABLE, "left_token"), tables(node).
        aliasedColumn(NODE_TABLE, "right_token"),
        -(tokenArity.getMin()) + 1));
    }
    else
    {
      conditions.add(between(
        tables(node).aliasedColumn(NODE_TABLE, "left_token"), tables(node).
        aliasedColumn(NODE_TABLE, "right_token"),
        -(tokenArity.getMin()) + 1, -(tokenArity.getMax()) + 1));
    }
  }

  @Override
  protected void addNodeArityConditions(List<String> conditions,
    QueryData queryData, QueryNode node)
  {
    // fugly
    List<Long> corpusList = queryData.getCorpusList();
    TableAccessStrategy tas = tables(null);
    String id1 = tables(node).aliasedColumn(RANK_TABLE, "id");
    String componentID1 = tables(node).aliasedColumn(COMPONENT_TABLE, "id");
    String corpusRef1 = tables(node).aliasedColumn(NODE_TABLE, "corpus_ref");
    
    String parent = TableAccessStrategy.column("children", tas.columnName(RANK_TABLE, "parent"));
    String id = TableAccessStrategy.column("children", tas.columnName(RANK_TABLE, "id"));
    String componentID = TableAccessStrategy.column("children", tas.columnName(COMPONENT_TABLE, "id"));;
    String corpusRef = TableAccessStrategy.column("children", tas.columnName(NODE_TABLE, "corpus_ref"));;
    
    
    String factsTable = SelectedFactsFromClauseGenerator.selectedFactsSQL(corpusList, "");
    
    StringBuffer sb = new StringBuffer();
    sb.append("(SELECT count(DISTINCT " + id + ")\n");
    sb.append("\tFROM " + factsTable + " AS children\n");
    sb.append("\tWHERE " + parent + " = " + id1 
      + " AND " + componentID1 + " = " + componentID
      + " AND " + corpusRef1 + " = " + corpusRef
      + " AND toplevel_corpus IN("
      + (corpusList.isEmpty() ? "NULL" : StringUtils.join(corpusList, ","))
      + ")" + ")");
    QueryNode.Range arity = node.getArity();
    if (arity.getMin() == arity.getMax())
    {
      conditions.add(join("=", sb.toString(), String.valueOf(arity.getMin())));
    }
    else
    {
      conditions.add(between(sb.toString(), arity.getMin(), arity.getMax()));
    }
  }

  @Override
  protected void addIsRootConditions(List<String> conditions,
    QueryData queryData, QueryNode node)
  {
    conditions.add(isTrue(tables(node).aliasedColumn(RANK_TABLE, "root")));
  }

  @Override
  protected void addIsTokenConditions(List<String> conditions,
    QueryData queryData, QueryNode node)
  {
    if (useIsTokenColumn)
    {
      conditions.add(isTrue(tables(node).aliasedColumn(NODE_TABLE, "is_token")));
    }
    else
    {
      conditions.add(isNotNull(tables(node).aliasedColumn(NODE_TABLE,
        "span")));
      conditions.add(isNull(tables(node).aliasedColumn(NODE_TABLE,
        "seg_name")));
    }
  }
  
  @Override
  protected void addSpanConditions(List<String> conditions,
    QueryData queryData, QueryNode node)
  {
    TextMatching textMatching = node.getSpanTextMatching();
    conditions.add(join(textMatching.sqlOperator(),
      tables(node).aliasedColumn(NODE_TABLE, "span"),
      sqlString(node.getSpannedText(), textMatching)));
  }

  public boolean isAllowIdenticalSibling()
  {
    return allowIdenticalSibling;
  }

  public void setAllowIdenticalSibling(boolean allowIdenticalSibling)
  {
    this.allowIdenticalSibling = allowIdenticalSibling;
  }

  public boolean isOptimizeInclusion()
  {
    return optimizeInclusion;
  }

  public void setOptimizeInclusion(boolean optimizeInclusion)
  {
    this.optimizeInclusion = optimizeInclusion;
  }

  public String getComponentPredicates()
  {
    return componentPredicates;
  }

  public void setComponentPredicates(String componentPredicates)
  {
    this.componentPredicates = componentPredicates;
  }

  public boolean isUseIsTokenColumn()
  {
    return useIsTokenColumn;
  }

  public void setUseIsTokenColumn(boolean useIsTokenColumn)
  {
    this.useIsTokenColumn = useIsTokenColumn;
  }

  public boolean isUseToplevelCorpusPredicateInCommonAncestorSubquery()
  {
    return useToplevelCorpusPredicateInCommonAncestorSubquery;
  }

  public void setUseToplevelCorpusPredicateInCommonAncestorSubquery(
    boolean useToplevelCorpusPredicateInCommonAncestorSubquery)
  {
    this.useToplevelCorpusPredicateInCommonAncestorSubquery =
      useToplevelCorpusPredicateInCommonAncestorSubquery;
  }

  public boolean isUseComponentRefPredicateInCommonAncestorSubquery()
  {
    return useComponentRefPredicateInCommonAncestorSubquery;
  }

  public void setUseComponentRefPredicateInCommonAncestorSubquery(
    boolean useComponentRefPredicateInCommonAncestorSubquery)
  {
    this.useComponentRefPredicateInCommonAncestorSubquery =
      useComponentRefPredicateInCommonAncestorSubquery;
  }

  public AnnotationConditionProvider getAnnoCondition()
  {
    return annoCondition;
  }

  public void setAnnoCondition(AnnotationConditionProvider annoCondition)
  {
    this.annoCondition = annoCondition;
  }
  
  public boolean isHackOperatorSameSpan()
  {
    return hackOperatorSameSpan;
  }

  public void setHackOperatorSameSpan(boolean hackOperatorSameSpan)
  {
    this.hackOperatorSameSpan = hackOperatorSameSpan;
  }
  
}
