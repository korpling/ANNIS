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

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import annis.service.internal.QueryServiceImpl;
import static annis.sqlgen.AbstractSqlGenerator.TABSTOP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates identifers for salt which are needed for the
 * {@link QueryServiceImpl#subgraph(java.lang.String, java.lang.String, java.lang.String)}
 *
 * @author Benjamin Weißenfels
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class SolutionSqlGenerator extends AbstractUnionSqlGenerator
  implements SelectClauseSqlGenerator<QueryData>,
  OrderByClauseSqlGenerator<QueryData>,
  GroupByClauseSqlGenerator<QueryData>
{

  private static final Logger log = LoggerFactory.getLogger(
    SolutionSqlGenerator.class);

  private boolean outputToplevelCorpus = true;

  private boolean outputNodeName = true;

  private AnnotationConditionProvider annoCondition;

  @Override
  public String selectClause(QueryData queryData, List<QueryNode> alternative,
    String indent)
  {
    int maxWidth = queryData.getMaxWidth();
    Validate.isTrue(alternative.size() <= maxWidth,
      "BUG: nodes.size() > maxWidth");

    List<String> cols = new ArrayList<>();
    int i = 0;

    boolean needsDistinct = isDistinctNeeded(alternative);
    
    for (QueryNode node : alternative)
    {
      ++i;

      TableAccessStrategy tblAccessStr = tables(node);
      cols.add(tblAccessStr.aliasedColumn(NODE_TABLE, "id") + " AS id" + i);
      if (annoCondition != null)
      {
        if (node.getNodeAnnotations().isEmpty())
        {
          // If a query node is not using annotations, fallback to NULL as the value.
          // This is important for the DISTINCT clause, since we don't want to match 
          // the annotation itself but the node.
          cols.add("NULL::int AS cat" + i);
        }
        else
        {
          cols.add(
            tblAccessStr.aliasedColumn("node_annotation", "category") + " AS cat" + i);
        }
      }
      if (outputNodeName)
      {
        
        if(needsDistinct)
        {
          cols.add("min(" + tblAccessStr.aliasedColumn(NODE_TABLE, "salt_id") 
            + ") AS salt_id" + i);
        }
        else
        {
          cols.add(tblAccessStr.aliasedColumn(NODE_TABLE, "salt_id") + " AS salt_id" + i);
        }
      }
    }

    // add additional empty columns in or clauses with different node sizes
    for (i = alternative.size() + 1; i <= maxWidth; ++i)
    {
      cols.add("NULL::bigint AS id" + i);
      cols.add("NULL::integer AS cat" + i);
      if (outputNodeName)
      {
        cols.add("NULL::varchar AS node_name" + i);
      }
    }

    if (!alternative.isEmpty() && outputNodeName)
    {

      TableAccessStrategy tblAccessStr = tables(alternative.get(0));

      String corpusRefAlias = tblAccessStr.aliasedColumn(NODE_TABLE,
        "corpus_ref");
      
      if(needsDistinct)
      {
        cols.add(
          "(SELECT c.path_name FROM corpus AS c WHERE c.id = min(" + corpusRefAlias
          + ") LIMIT 1) AS path_name");
      }
      else
      {
        cols.add(
          "(SELECT c.path_name FROM corpus AS c WHERE c.id = " + corpusRefAlias
          + " LIMIT 1) AS path_name");
      }
    }

    if (outputToplevelCorpus)
    {
      if(needsDistinct)
      {
        cols.add("min(" + tables(alternative.get(0)).aliasedColumn(NODE_TABLE,
          "toplevel_corpus")
          + ") AS toplevel_corpus");
      }
      else
      {
        cols.add(tables(alternative.get(0)).aliasedColumn(NODE_TABLE,
          "toplevel_corpus")
          + " AS toplevel_corpus");
      }
    }

    if(needsDistinct)
    {
      cols.add("min(" + tables(alternative.get(0)).aliasedColumn(NODE_TABLE,
        "corpus_ref")
        + ") AS corpus_ref");
    }
    else
    {
      cols.add(tables(alternative.get(0)).aliasedColumn(NODE_TABLE,
        "corpus_ref")
        + " AS corpus_ref");
    }
    String colIndent = indent + TABSTOP + TABSTOP;

    return "\n" + colIndent + StringUtils.join(cols, ",\n" + colIndent);
  }

  @Override
  public String orderByClause(QueryData queryData, List<QueryNode> alternative,
    String indent)
  {
    List<String> ids = new ArrayList<>();
    for (int i = 1; i <= queryData.getMaxWidth(); ++i)
    {
      ids.add("id" + i);
      if (annoCondition != null)
      {
        ids.add("cat" + i);
      }
    }
    return StringUtils.join(ids, ", ");
  }

  @Override
  public String groupByAttributes(QueryData queryData,
    List<QueryNode> alternative)
  {
    if(isDistinctNeeded(alternative))
    {
      List<String> ids = new ArrayList<>();
      for (int i = 1; i <= queryData.getMaxWidth(); ++i)
      {
        ids.add("id" + i);
        if (annoCondition != null)
        {
          ids.add("cat" + i);
        }
      }
      return StringUtils.join(ids, ", "); 
    }
    else
    {
      return null;
    }
  }
  
  private boolean isDistinctNeeded(List<QueryNode> alternative)
  {
    
    /* 
    If no node uses the rank table we can assume the result is already
    distinct. Of course this only works as long the SampleWhereClause generator
    is active and filters out node annotation duplicates.
    */
    for(QueryNode node : alternative)
    {
      TableAccessStrategy tas = tables(node);
      if(tas.usesRankTable())
      {
        return true;
      }
    }
    return false;
  }

  public boolean isOutputToplevelCorpus()
  {
    return outputToplevelCorpus;
  }

  public void setOutputToplevelCorpus(boolean outputToplevelCorpus)
  {
    this.outputToplevelCorpus = outputToplevelCorpus;
  }

  public AnnotationConditionProvider getAnnoCondition()
  {
    return annoCondition;
  }

  public void setAnnoCondition(AnnotationConditionProvider annoCondition)
  {
    this.annoCondition = annoCondition;
  }

  public boolean isOutputNodeName()
  {
    return outputNodeName;
  }

  public void setOutputNodeName(boolean outputNodeName)
  {
    this.outputNodeName = outputNodeName;
  }
  
  
  
}
