/*
 * Copyright 2012 SFB 632.
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
package annis.sqlgen;

import annis.sqlgen.extensions.AnnotateQueryData;
import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;

import static annis.sqlgen.TableAccessStrategy.*;
import static annis.sqlgen.AbstractSqlGenerator.TABSTOP;
import static annis.sqlgen.SqlConstraints.sqlString;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class CommonAnnotateWithClauseGenerator
  extends TableAccessStrategyFactory
  implements WithClauseSqlGenerator<QueryData>
{
  
  private AnnotateInnerQuerySqlGenerator innerQuerySqlGenerator;
  private IslandsPolicy islandsPolicy;
  
  /**
   * Create a solution key to be used inside a single call to
   * {@code extractData}.
   *
   * This method must be overridden in child classes or by Spring.
   */
  protected SolutionKey<?> createSolutionKey()
  {
    throw new UnsupportedOperationException(
      "BUG: This method needs to be overwritten by ancestors or through Spring");
  }

  @Override
  public List<String> withClauses(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    List<Long> corpusList = queryData.getCorpusList();
    HashMap<Long, Properties> corpusProperties =
      queryData.getCorpusConfiguration();
    IslandsPolicy.IslandPolicies policy = getIslandsPolicy().getMostRestrictivePolicy(corpusList,
      corpusProperties);

    List<String> result = new LinkedList<String>();

    List<AnnotateQueryData> ext = queryData.getExtensions(
      AnnotateQueryData.class);
    if (!ext.isEmpty())
    {
      AnnotateQueryData annoQueryData = ext.get(0);

      if (annoQueryData.getSegmentationLayer() == null)
      {
        // token index based method

        // first get the raw matches
        result.add(getMatchesWithClause(queryData, alternative, indent));
        result.add(getKeyWithClause(indent));

        // break the columns down in a way that every matched node has it's own
        // row
        result.add(getSolutionFromMatchesWithClause("matches", indent));

      }
      else
      {
        // segmentation layer based method

        result.add(getMatchesWithClause(queryData, alternative, indent));
        result.add(getKeyWithClause(indent));
        result.add(getNearestSeqWithClause(queryData, annoQueryData, alternative,
          "matches", indent));
        result.add(getSolutionFromNearestSegWithClause(queryData, annoQueryData,
          alternative, policy, "nearestseg", indent));

      }
    }

    return result;
  }

  /**
   * Uses the inner SQL generator and provides an ordered and limited view on
   * the matches with a match number.
   */
  protected String getMatchesWithClause(QueryData queryData, List<QueryNode> alternative,  String indent)
  {
    StringBuilder sb = new StringBuilder();
    sb.append(indent).append("matches AS\n");
    sb.append(indent).append("(\n");
    sb.append(indent).append(getInnerQuerySqlGenerator().toSql(queryData, indent
      + TABSTOP));
    sb.append("\n").append(indent).append(")");

    return sb.toString();
  }
  
  protected String getKeyWithClause(String indent)
  {
    String indent2 = indent + TABSTOP;
    
    return indent + "keys AS (\n" +
     indent2 + "SELECT n, array_agg(id ORDER BY nodenr DESC) AS \"key\" FROM matches\n" +
     indent2 + "GROUP BY n\n" +
     indent + ")";
  }

  /**
   * Breaks down the matches table, so that each node of each match has it's own
   * row.
   */
  protected String getSolutionFromMatchesWithClause(
    String matchesName,
    String indent)
  {
    String indent2 = indent + TABSTOP;
    return  indent + "solutions AS\n" 
      + indent + "(\n"
      + indent2 + "SELECT keys.key AS key, " + matchesName + ".*\n "
      + indent2 + "FROM " + matchesName + ", keys "
      + indent2 + "WHERE keys.n =" + matchesName + ".n\n" +
      indent + ")";

  }

  
  protected String getNearestSeqWithClause(
    QueryData queryData, AnnotateQueryData annoQueryData,
    List<QueryNode> alternative, String matchesName, String indent)
  {
    String indent2 = indent + TABSTOP;
    String indent3 = indent2 + TABSTOP;
    
    SolutionKey<?> key = createSolutionKey();
    // use copy constructor in order not to mess up the global TableAccessStrategy bean
    TableAccessStrategy tas = new TableAccessStrategy(createTableAccessStrategy());
    tas.addTableAlias("solutions", matchesName);
   
    StringBuilder sb = new StringBuilder();
    sb.append(indent).append("nearestseg AS\n");
    sb.append(indent).append("(\n");

    
    sb.append(indent2).append("SELECT\n");

    sb.append(indent3);
    sb.append("matches.nodeNr AS nodeNr");
    sb.append(", matches.n AS n");
    sb.append(", matches.id AS id,\n");
    sb.append(indent3).append(tas.aliasedColumn(NODE_TABLE, "seg_index")).append(" - ").append(annoQueryData.
      getLeft()).append(" AS \"min\",\n");
    sb.append(indent3).append(tas.aliasedColumn(NODE_TABLE,
      "seg_index")).append(" + ").append(annoQueryData.getRight()).append(
      " AS \"max\",\n");
    sb.append(indent3).append(tas.aliasedColumn(NODE_TABLE, "text_ref")).append(
      " AS \"text\", \n");
    sb.append(indent3).append(tas.aliasedColumn(NODE_TABLE, "corpus_ref")).append(
      " AS \"corpus\", \n");

    String distLeft = "min"
      + " - " 
      + tas.aliasedColumn(NODE_TABLE, "left_token");
    String distRight = tas.aliasedColumn(NODE_TABLE, "right_token") 
      + " - max" ;

    // create ordered window partition
    // values are ordered by their distance to the min or max token index
    // NULLIF( dist+1, -abs(dist+1) will give negative entries NULL which means
    // their are put last in the ordered list
    // +1 is there to ensure that positive equal values (thus dist=0) are not ignored
    sb.append(indent3).append("row_number() OVER (PARTITION BY ")
      .append(tas.aliasedColumn(NODE_TABLE, "corpus_ref")).append(", ")
      .append(tas.aliasedColumn(NODE_TABLE, "text_ref"))
      .append(" ORDER BY NULLIF(")
      .append(distLeft).append("+ 1, -abs(").append(distLeft)
      .append(" + 1)) ASC) AS rank_left,\n");

    sb.append(indent3).append("row_number() OVER (PARTITION BY ")
      .append(tas.aliasedColumn(NODE_TABLE, "corpus_ref")).append(", ")
      .append(tas.aliasedColumn(NODE_TABLE, "text_ref"))
      .append(" ORDER BY NULLIF(")
      .append(distRight).append(" + 1, -abs(").append(distRight)
      .append(" + 1)) ASC) AS rank_right\n");


    sb.append(indent2).append("FROM ").append(tas.tableName(NODE_TABLE)).append(
      ", matches\n");
    sb.append(indent2).append("WHERE\n");

    sb.append(indent3).append(tas.aliasedColumn(NODE_TABLE, "toplevel_corpus")).
      append(" IN (").append(StringUtils.join(queryData.getCorpusList(), ",")).
      append(") AND\n");

    sb.append(indent3).append(tas.aliasedColumn(NODE_TABLE, "n_sample")).append(
      " IS TRUE AND\n");

    sb.append(indent3).append(tas.aliasedColumn(NODE_TABLE, "seg_name"))
      .append(" = ").append(sqlString(annoQueryData.getSegmentationLayer()))
      .append(" AND\n");

    sb.append(indent3).append(tas.aliasedColumn(NODE_TABLE, "text_ref"))
      .append(" = matches.text").append(" AND\n");

     sb.append(indent3).append(tas.aliasedColumn(NODE_TABLE, "corpus_ref"))
      .append(" = matches.corpus").append("\n");

    sb.append(indent).append(")");

    return sb.toString();
  }

  /**
   * Get solution from a covered segmentation using a WITH clause.
   *
   * @param queryData
   * @param annoQueryData
   * @param alternative
   * @param islandsPolicy
   * @param coveredName
   * @param indent
   * @return
   *
   */
  protected String getSolutionFromNearestSegWithClause(
    QueryData queryData, AnnotateQueryData annoQueryData,
    List<QueryNode> alternative, IslandsPolicy.IslandPolicies islandsPolicy,
    String coveredName, String indent)
  {

    TableAccessStrategy tas = createTableAccessStrategy();

    String indent2 = indent + TABSTOP;
    String indent3 = indent2 + TABSTOP;

    List<Long> corpusList = queryData.getCorpusList();

    StringBuilder sb = new StringBuilder();

    sb.append(indent).append("solutions AS\n");
    sb.append(indent).append("(\n");

    sb.append(indent2).append("SELECT DISTINCT ");

    if (islandsPolicy == IslandsPolicy.IslandPolicies.none)
    {
      sb.append("min(").append("keys.key) AS key, ")
        .append(coveredName).append(".n AS n, ")
        .append("min(").append(tas.aliasedColumn(NODE_TABLE, "left_token")).append(") AS \"min\", ")
        .append("max(").append(tas.aliasedColumn(NODE_TABLE, "right_token")).append(") AS \"max\", ")
        .append(tas.aliasedColumn(NODE_TABLE, "text_ref")).append(" AS \"text\", ")
        .append(tas.aliasedColumn(NODE_TABLE, "corpus_ref")).append(" AS \"corpus\"\n");
    }
    else if (islandsPolicy == IslandsPolicy.IslandPolicies.context)
    {
      sb.append("keys.key AS key, ")
        .append(coveredName).append(".n AS n, ")
        .append(tas.aliasedColumn(NODE_TABLE, "left_token")).append(" AS \"min\", ")
        .append(tas.aliasedColumn(NODE_TABLE, "right_token")).append(" AS \"max\", ")
        .append(tas.aliasedColumn(NODE_TABLE, "text_ref")).append(" AS \"text\", ")
        .append(tas.aliasedColumn(NODE_TABLE, "corpus_ref")).append(" AS \"corpus\"\n");
    }
    else
    {
      throw new UnsupportedOperationException("No implementation for island policy "
        + islandsPolicy.toString());
    }

    sb.append(indent2).append("FROM ")
      .append(coveredName).append(", ")
      .append(tas.tableName(NODE_TABLE)).append(", ")
      .append("keys")
      .append("\n");

    sb.append(indent2).append("WHERE\n");
    
    sb.append(indent3).append("keys.n = ").append(coveredName).append(".n AND\n");

    sb.append(indent3).append(tas.aliasedColumn(NODE_TABLE, "toplevel_corpus")).
      append(" IN (").append(StringUtils.join(corpusList, ",")).append(") AND\n");

    sb.append(indent3).append(tas.aliasedColumn(NODE_TABLE, "n_sample")).append(
      " IS TRUE AND\n");

    sb.append(indent3).append(tas.aliasedColumn(NODE_TABLE, "seg_name")).append(
      " = ")
      .append(sqlString(annoQueryData.getSegmentationLayer())).append(" AND\n");

    sb.append(indent3).append(tas.aliasedColumn(NODE_TABLE, "text_ref")).append(
      " = ").append(coveredName).append(".\"text\" AND\n");
    
    sb.append(indent3).append(tas.aliasedColumn(NODE_TABLE, "corpus_ref")).append(
      " = ").append(coveredName).append(".\"corpus\" AND\n");

    sb.append(indent3).append(tas.aliasedColumn(NODE_TABLE, "seg_index")).append(
      " <= ").append(coveredName).append(".\"max\" AND\n");
    sb.append(indent3).append(tas.aliasedColumn(NODE_TABLE, "seg_index")).append(
      " >= ").append(coveredName).append(".\"min\" AND\n");
    
    sb.append(indent3).append("(").append(coveredName)
      .append(".rank_left = 1 OR ").append(coveredName)
      .append(".rank_right = 1)\n");

    if (islandsPolicy == IslandsPolicy.IslandPolicies.none)
    {
      sb.append(indent2).append("GROUP BY ")
        .append(tas.aliasedColumn(NODE_TABLE, "corpus_ref")).append(", ")
        .append(tas.aliasedColumn(NODE_TABLE, "text_ref"))
        .append(", n\n");
    }

    sb.append(indent).append(")\n");

    return sb.toString();
  }
  
  
  public static String overlapForOneRange(String indent,
    String rangeMin, String rangeMax, String textRef, String corpus_ref, TableAccessStrategy tables)
  {
    StringBuilder sb = new StringBuilder();

    sb.append(indent).append("(");

    sb.append(tables.aliasedColumn(NODE_TABLE, "left_token")).append(" <= ").append(rangeMax)
      .append(" AND ")
      .append(tables.aliasedColumn(NODE_TABLE, "right_token")).append(" >= ").append(rangeMin)
      .append(" AND ")
      .append(tables.aliasedColumn(NODE_TABLE, "text_ref")).append(" = ").append(textRef)
      .append(" AND ")
      .append(tables.aliasedColumn(NODE_TABLE, "corpus_ref")).append(" = ").append(corpus_ref);

    sb.append(")");

    return sb.toString();
  }
  

  public AnnotateInnerQuerySqlGenerator getInnerQuerySqlGenerator()
  {
    return innerQuerySqlGenerator;
  }

  public void setInnerQuerySqlGenerator(
    AnnotateInnerQuerySqlGenerator innerQuerySqlGenerator)
  {
    this.innerQuerySqlGenerator = innerQuerySqlGenerator;
  }
  
  /**
   * @return the islandsPolicy
   */
  public IslandsPolicy getIslandsPolicy()
  {
    return islandsPolicy;
  }

  /**
   * @param islandsPolicy the islandsPolicy to set
   */
  public void setIslandsPolicy(IslandsPolicy islandsPolicy)
  {
    this.islandsPolicy = islandsPolicy;
  }
  
  
}
