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

import static annis.sqlgen.TableAccessStrategy.COMPONENT_TABLE;
import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;
import static annis.sqlgen.TableAccessStrategy.FACTS_TABLE;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnisNode.TextMatching;
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
import annis.sqlgen.model.RangedJoin;
import annis.sqlgen.model.RankTableJoin;
import annis.sqlgen.model.RightAlignment;
import annis.sqlgen.model.RightDominance;
import annis.sqlgen.model.RightOverlap;
import annis.sqlgen.model.SameSpan;
import annis.sqlgen.model.Sibling;
import java.util.LinkedList;
import org.apache.commons.lang.StringUtils;

public class DefaultWhereClauseSqlGenerator
  extends BaseNodeSqlGenerator
  implements WhereClauseSqlGenerator
{

  @Override
  public List<String> whereConditions(AnnisNode node, List<Long> corpusList, List<Long> documents)
  {
    List<String> conditions = new ArrayList<String>();

    addNodeConditions(node, conditions, node.getNodeAnnotations(), corpusList);
    addAnnotationConditions(node, conditions, node.getNodeAnnotations(),
      NODE_ANNOTATION_TABLE, "node_annotation_");

    if (tables(node).usesPartialFacts())
    {
      addAnnotationConditions(node, conditions, node.getNodeAnnotations(), FACTS_TABLE,
        "node_annotation_");
    }

    addNodeJoinConditions(node, conditions);
    addEdgeJoinConditions(node, conditions, corpusList);

    addAnnotationConditions(node, conditions, node.getEdgeAnnotations(),
      EDGE_ANNOTATION_TABLE, "edge_annotation_");

    return conditions;
  }

  protected void addNodeJoinConditions(AnnisNode node, List<String> conditions)
  {
    for (Join join : node.getJoins())
    {
      AnnisNode target = join.getTarget();

      if (join instanceof SameSpan)
      {
        joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
        joinOnNode(conditions, node, target, "=", "left", "left");
        joinOnNode(conditions, node, target, "=", "right", "right");
      }
      else if(join instanceof Identical)
      {
        joinOnNode(conditions, node, target, "=", "id", "id");
      }
      else if (join instanceof LeftAlignment)
      {
        joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
        joinOnNode(conditions, node, target, "=", "left", "left");
      }
      else if (join instanceof RightAlignment)
      {
        joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
        joinOnNode(conditions, node, target, "=", "right", "right");
      }
      else if (join instanceof Inclusion)
      {
        joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
        joinOnNode(conditions, node, target, "<=", "left", "left");
        joinOnNode(conditions, node, target, ">=", "right", "right");
      }
      else if (join instanceof Overlap)
      {
        joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
        joinOnNode(conditions, node, target, "<=", "left", "right");
        joinOnNode(conditions, target, node, "<=", "left", "right");
      }
      else if (join instanceof LeftOverlap)
      {
        joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
        joinOnNode(conditions, node, target, "<=", "left", "left");
        joinOnNode(conditions, target, node, "<=", "left", "right");
        joinOnNode(conditions, node, target, "<=", "right", "right");
      }
      else if (join instanceof RightOverlap)
      {
        joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");
        joinOnNode(conditions, node, target, ">=", "right", "right");
        joinOnNode(conditions, target, node, ">=", "right", "left");
        joinOnNode(conditions, node, target, ">=", "left", "left");
      }
      else if (join instanceof Precedence)
      {
        joinOnNode(conditions, node, target, "=", "text_ref", "text_ref");

        RangedJoin precedence = (RangedJoin) join;
        int min = precedence.getMinDistance();
        int max = precedence.getMaxDistance();

        // indirect
        if (min == 0 && max == 0)
        {
          joinOnNode(conditions, node, target, "<", "right_token", "left_token");

        }
        // exact distance
        else if (min == max)
        {
          numberJoinOnNode(conditions, node, target, "=", "right_token", "left_token", -min);

        }
        // ranged distance
        else
        {
          betweenJoinOnNode(conditions, node, target, "right_token", "left_token", -min, -max);
//					conditions.add(numberJoin("<=", tables(node).aliasedColumn(NODE_TABLE, "right_token"), tables(target).aliasedColumn(NODE_TABLE, "left_token"), -min));
//					conditions.add(numberJoin(">=", tables(node).aliasedColumn(NODE_TABLE, "right_token"), tables(target).aliasedColumn(NODE_TABLE, "left_token"), -max));
        }

      }
    }
  }

  protected void addEdgeJoinConditions(AnnisNode node, List<String> conditions, List<Long> corpusList)
  {
    for (Join join : node.getJoins())
    {
      AnnisNode target = join.getTarget();

      if (join instanceof Sibling)
      {
        conditions.add(join("=", tables(node).aliasedColumn(COMPONENT_TABLE, "type"), sqlString("d")));
        Sibling sibling = (Sibling) join;
        if (sibling.getName() != null)
        {
          conditions.add(join("=", tables(node).aliasedColumn(COMPONENT_TABLE, "name"), sqlString(sibling.getName())));
        }
        else
        {
          conditions.add(tables(node).aliasedColumn(COMPONENT_TABLE, "name") + " IS NULL");
        }
        conditions.add(join("=", tables(node).aliasedColumn(RANK_TABLE, "parent"), tables(target).aliasedColumn(RANK_TABLE, "parent")));
        joinOnNode(conditions, node, target, "<>", "id", "id");
      }
      else if (join instanceof CommonAncestor)
      {
        conditions.add(join("=", tables(node).aliasedColumn(COMPONENT_TABLE, "type"), sqlString("d")));
        CommonAncestor commonAncestor = (CommonAncestor) join;
        if (commonAncestor.getName() != null)
        {
          conditions.add(join("=", tables(node).aliasedColumn(COMPONENT_TABLE, "name"), sqlString(commonAncestor.getName())));
        }
        else
        {
          conditions.add(tables(node).aliasedColumn(COMPONENT_TABLE, "name") + " IS NULL");
        }
        
        joinOnNode(conditions, node, target, "<>", "id", "id");
        
        // fugly
        TableAccessStrategy tas = tables(null);
        String pre1 = tables(node).aliasedColumn(RANK_TABLE, "pre");
        String pre2 = tables(target).aliasedColumn(RANK_TABLE, "pre");
        String pre = tas.column("ancestor", tas.columnName(RANK_TABLE, "pre"));
        String post = tas.column("ancestor", tas.columnName(RANK_TABLE, "post"));

        StringBuffer sb = new StringBuffer();
        sb.append("EXISTS (SELECT 1 FROM " + tas.tableName(RANK_TABLE) + " AS ancestor WHERE\n");
        sb.append("\t" + pre + " < " + pre1 + " AND " + pre1 + " < " + post + " AND\n");
        sb.append("\t" + pre + " < " + pre2 + " AND " + pre2 + " < " + post
          + " AND toplevel_corpus IN(" 
          + (corpusList.isEmpty() ? "NULL" : StringUtils.join(corpusList,","))
          + "))");
        conditions.add(sb.toString());

      }
      else if (join instanceof LeftDominance)
      {
        addLeftRightDominance(node, target, conditions, true, (RankTableJoin) join);
      }
      else if (join instanceof RightDominance)
      {
        addLeftRightDominance(node, target, conditions, false, (RankTableJoin) join);
      }
      else if (join instanceof Dominance)
      {
        addSingleEdgeCondition(node, target, conditions, join, "d");

      }
      else if (join instanceof PointingRelation)
      {
        addSingleEdgeCondition(node, target, conditions, join, "p");

      }
    }
  }

  protected void addSingleEdgeCondition(AnnisNode node, AnnisNode target, List<String> conditions, Join join, final String edgeType)
  {
    conditions.add(join("=", tables(target).aliasedColumn(COMPONENT_TABLE, "type"), sqlString(edgeType)));

    RankTableJoin rankTableJoin = (RankTableJoin) join;
    if (rankTableJoin.getName() != null)
    {
      conditions.add(join("=", tables(target).aliasedColumn(COMPONENT_TABLE, "name"), sqlString(rankTableJoin.getName())));
    }
    else
    {
      conditions.add(tables(target).aliasedColumn(COMPONENT_TABLE, "name") + " IS NULL");
    }

    int min = rankTableJoin.getMinDistance();
    int max = rankTableJoin.getMaxDistance();

    // direct
    if (min == 1 && max == 1)
    {
      conditions.add(join("=", tables(node).aliasedColumn(RANK_TABLE, "pre"), tables(target).aliasedColumn(RANK_TABLE, "parent")));

      // indirect
    }
    else
    {
      conditions.add(join("<", tables(node).aliasedColumn(RANK_TABLE, "pre"), tables(target).aliasedColumn(RANK_TABLE, "pre")));
      conditions.add(join("<", tables(target).aliasedColumn(RANK_TABLE, "pre"), tables(node).aliasedColumn(RANK_TABLE, "post")));

      // exact
      if (min > 0 && min == max)
      {
        conditions.add(numberJoin("=", tables(node).aliasedColumn(RANK_TABLE, "level"), tables(target).aliasedColumn(RANK_TABLE, "level"), -min));

        // range
      }
      else if (min > 0 && min < max)
      {
        conditions.add(between(tables(node).aliasedColumn(RANK_TABLE, "level"), tables(target).aliasedColumn(RANK_TABLE, "level"), -min, -max));
      }
    }
  }

  protected void addNodeConditions(AnnisNode node,
    List<String> conditions, Set<Annotation> annotations, List<Long> corpusList)
  {
    boolean usesFacts = tables(node).usesPartialFacts();

    if (node.getSpannedText() != null)
    {
      TextMatching textMatching = node.getSpanTextMatching();
      conditions.add(join(textMatching.sqlOperator(), tables(node).aliasedColumn(NODE_TABLE, "span"), sqlString(node.getSpannedText(), textMatching)));
      if (usesFacts)
      {
        conditions.add(join(textMatching.sqlOperator(), tables(node).aliasedColumn(FACTS_TABLE, "span"), sqlString(node.getSpannedText(), textMatching)));
      }
    }

    if (node.isToken())
    {
      conditions.add(tables(node).aliasedColumn(NODE_TABLE, "is_token") + " IS TRUE");
      if (usesFacts)
      {
        conditions.add(tables(node).aliasedColumn(FACTS_TABLE, "is_token") + " IS TRUE");
      }
    }
    if (node.isRoot())
    {
      conditions.add(tables(node).aliasedColumn(RANK_TABLE, "root") + " IS TRUE");
    }

    if (node.getNamespace() != null)
    {
      conditions.add(join("=", tables(node).aliasedColumn(NODE_TABLE, "namespace"), sqlString(node.getNamespace())));
      if (usesFacts)
      {
        conditions.add(join("=", tables(node).aliasedColumn(FACTS_TABLE, "namespace"), sqlString(node.getNamespace())));
      }
    }
    if (node.getName() != null)
    {
      conditions.add(join("=", tables(node).aliasedColumn(NODE_TABLE, "name"), sqlString(node.getName())));
      if (usesFacts)
      {
        conditions.add(join("=", tables(node).aliasedColumn(FACTS_TABLE, "name"), sqlString(node.getName())));
      }
    }
    if (node.getArity() != null)
    {
      // fugly
      TableAccessStrategy tas = tables(null);
      String pre1 = tables(node).aliasedColumn(RANK_TABLE, "pre");
      String parent = tas.column("children", tas.columnName(RANK_TABLE, "parent"));
      String pre = tas.column("children", tas.columnName(RANK_TABLE, "pre"));
      StringBuffer sb = new StringBuffer();
      sb.append("(SELECT count(DISTINCT " + pre + ")\n");
      sb.append("\tFROM " + tas.tableName(RANK_TABLE) + " AS children\n");
      sb.append("\tWHERE " + parent + " = " + pre1 
        + " AND toplevel_corpus IN(" + (corpusList.isEmpty() ? "NULL" : StringUtils.join(corpusList,",")) + ")"
        + ")");
      AnnisNode.Range arity = node.getArity();
      if (arity.getMin() == arity.getMax())
      {
        conditions.add(join("=", sb.toString(), String.valueOf(arity.getMin())));
      }
      else
      {
        conditions.add(between(sb.toString(), arity.getMin(), arity.getMax()));
      }
    }

    if (node.getTokenArity() != null)
    {
      AnnisNode.Range tokenArity = node.getTokenArity();
      if (tokenArity.getMin() == tokenArity.getMax())
      {
        conditions.add(numberJoin("=", tables(node).aliasedColumn(NODE_TABLE, "left_token"), tables(node).aliasedColumn(NODE_TABLE, "right_token"), -(tokenArity.getMin()) + 1));
      }
      else
      {
        conditions.add(between(tables(node).aliasedColumn(NODE_TABLE, "left_token"), tables(node).aliasedColumn(NODE_TABLE, "right_token"), -(tokenArity.getMin()) + 1, -(tokenArity.getMax()) + 1));
      }
    }

  }

  protected void addAnnotationConditions(AnnisNode node,
    List<String> conditions, Set<Annotation> annotations, String table, String prefix)
  {
    int i = 0;
    for (Annotation annotation : annotations)
    {
      ++i;
      if (annotation.getNamespace() != null)
      {
        conditions.add(join("=", tables(node).aliasedColumn(table, prefix + "namespace", i), sqlString(annotation.getNamespace())));
      }
      conditions.add(join("=", tables(node).aliasedColumn(table, prefix + "name", i), sqlString(annotation.getName())));
      if (annotation.getValue() != null)
      {
        TextMatching textMatching = annotation.getTextMatching();
        conditions.add(join(textMatching.sqlOperator(), tables(node).aliasedColumn(table, prefix + "value", i), sqlString(annotation.getValue(), textMatching)));
      }
    }
  }

  protected void joinOnNode(List<String> conditions, AnnisNode node, AnnisNode target,
    String operator, String leftColumn, String rightColumn)
  {
    conditions.add(join(operator, tables(node).aliasedColumn(NODE_TABLE, leftColumn),
      tables(target).aliasedColumn(NODE_TABLE, rightColumn)));

    // if both nodes are used in a facts-table relation also apply this constraint to facts
    if (tables(node).usesPartialFacts() && tables(target).usesPartialFacts())
    {
      conditions.add(join(operator, tables(node).aliasedColumn(FACTS_TABLE, leftColumn),
        tables(target).aliasedColumn(FACTS_TABLE, rightColumn)));
    }
  }

  protected void betweenJoinOnNode(List<String> conditions, AnnisNode node, AnnisNode target,
    String leftColumn, String rightColumn, int min, int max)
  {
    conditions.add(between(tables(node).aliasedColumn(NODE_TABLE, leftColumn),
      tables(target).aliasedColumn(NODE_TABLE, rightColumn), min, max));

    // if both nodes are used in a facts-table relation also apply this constraint to facts
    if (tables(node).usesPartialFacts() && tables(target).usesPartialFacts())
    {
      conditions.add(between(tables(node).aliasedColumn(FACTS_TABLE, leftColumn),
        tables(target).aliasedColumn(FACTS_TABLE, rightColumn), min, max));
    }
  }

  protected void numberJoinOnNode(List<String> conditions, AnnisNode node, AnnisNode target,
    String operator, String leftColumn, String rightColumn, int offset)
  {
    conditions.add(numberJoin(operator, tables(node).aliasedColumn(NODE_TABLE, leftColumn),
      tables(target).aliasedColumn(NODE_TABLE, rightColumn), offset));

    // if both nodes are used in a facts-table relation also apply this constraint to facts
    if (tables(node).usesPartialFacts() && tables(target).usesPartialFacts())
    {
      conditions.add(numberJoin(operator, tables(node).aliasedColumn(FACTS_TABLE, leftColumn),
        tables(target).aliasedColumn(FACTS_TABLE, rightColumn), offset));
    }
  }
  
  protected void addLeftRightDominance(AnnisNode node, AnnisNode target , 
    List<String> conditions, boolean left, RankTableJoin rankTableJoin)
  {
    conditions.add(join("=", tables(target).aliasedColumn(COMPONENT_TABLE, "type"), sqlString("d")));
    conditions.add(join("=", tables(node).aliasedColumn(RANK_TABLE, "pre"), tables(target).aliasedColumn(RANK_TABLE, "parent")));
    
    if (rankTableJoin.getName() != null)
    {
      conditions.add(join("=", tables(target).aliasedColumn(COMPONENT_TABLE, "name"), sqlString(rankTableJoin.getName())));
    }
    else
    {
      conditions.add(tables(target).aliasedColumn(COMPONENT_TABLE, "name") + " IS NULL");
    }
    
    String agg = left ? "min" : "max";
    String tok = left ? "left_token" : "right_token";

    conditions.add(
      in(tables(target).aliasedColumn(NODE_TABLE, tok),
        "SELECT " + agg + "(lrsub." + tok + ") FROM " + FACTS_TABLE + " as lrsub "
        + "WHERE parent=" + tables(node).aliasedColumn(RANK_TABLE, "pre")
        + " AND corpus_ref=" + tables(target).aliasedColumn(NODE_TABLE, "corpus_ref") 
    ));
  }
  

  @Override
  public List<String> commonWhereConditions(List<AnnisNode> nodes, List<Long> corpusList, List<Long> documents)
  {
    return null;
  }
}
