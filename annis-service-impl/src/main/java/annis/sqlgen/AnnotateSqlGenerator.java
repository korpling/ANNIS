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

import static annis.sqlgen.TableAccessStrategy.CORPUS_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 *
 * @param <T> Type into which the JDBC result set is transformed
 *
 * @author thomas
 */
public abstract class AnnotateSqlGenerator<T>
  extends AbstractSqlGenerator<T>
  implements SelectClauseSqlGenerator<QueryData>,
  FromClauseSqlGenerator<QueryData>,
  WhereClauseSqlGenerator<QueryData>, OrderByClauseSqlGenerator<QueryData>,
  AnnotateExtractor<T>
{

  // include document name in SELECT clause
  private boolean includeDocumentNameInAnnotateQuery;
  // include is_token column in SELECT clause
  private boolean includeIsTokenColumn;
  private AnnotateInnerQuerySqlGenerator innerQuerySqlGenerator;
  private TableJoinsInFromClauseSqlGenerator tableJoinsInFromClauseSqlGenerator;
  private TableAccessStrategy outerQueryTableAccessStrategy;
  private boolean optimizeOverlap;
  private ResultSetExtractor<T> resultExtractor;
  // helper to extract the corpus path from a JDBC result set
  private CorpusPathExtractor corpusPathExtractor;

  public static class AnnotateQueryData
  {

    private int offset;
    private int limit;
    private int left;
    private int right;

    public AnnotateQueryData(int offset, int limit, int left,
      int right)
    {
      super();
      this.offset = offset;
      this.limit = limit;
      this.left = left;
      this.right = right;
    }

    public int getOffset()
    {
      return offset;
    }

    public int getLimit()
    {
      return limit;
    }

    public int getLeft()
    {
      return left;
    }

    public int getRight()
    {
      return right;
    }

    public boolean isPaged()
    {
      return offset != 0 || limit != 0;
    }

    @Override
    public String toString()
    {
      List<String> fields = new ArrayList<String>();
      if (limit > 0)
      {
        fields.add("limit = " + limit);
      }
      if (offset > 0)
      {
        fields.add("offset = " + offset);
      }
      if (left > 0)
      {
        fields.add("left = " + left);
      }
      if (right > 0)
      {
        fields.add("right = " + right);
      }
      return StringUtils.join(fields, ", ");
    }
  }

  // old
  public enum IslandPolicies
  {

    context, none
  }
  private String matchedNodesViewName;
  private String defaultIslandsPolicy;

  public AnnotateSqlGenerator()
  {
  }

  /**
   * Create a solution key to be used inside a single call to
   * {@code extractData}.
   *
   * This method must be overridden in child classes or by Spring.
   */
  protected SolutionKey<?> createSolutionKey()
  {
    throw new NotImplementedException(
      "BUG: This method needs to be overwritten by ancestors or through Spring");
  }

  /**
   *
   * @param jdbcTemplate
   * @param textID
   * @return
   * @deprecated use {@link #queryAnnotationGraph(org.springframework.jdbc.core.JdbcTemplate, java.lang.String, java.lang.String)}
   * instead
   */
  @Deprecated
  public T queryAnnotationGraph(
    JdbcTemplate jdbcTemplate, long textID)
  {
    return (T) jdbcTemplate.query(getTextQuery(textID), this);
  }

  public T queryAnnotationGraph(
    JdbcTemplate jdbcTemplate, String toplevelCorpusName, String documentName)
  {
    return (T) jdbcTemplate.query(getDocumentQuery(toplevelCorpusName,
      documentName), this);
  }

  private IslandPolicies getMostRestrictivePolicy(
    List<Long> corpora, Map<Long, Properties> props)
  {
    IslandPolicies[] all = IslandPolicies.values();
    IslandPolicies result = all[all.length - 1];

    for (Long l : corpora)
    {
      if (props.get(l) != null)
      {
        IslandPolicies newPolicy =
          IslandPolicies.valueOf(props.get(l).getProperty("islands-policy",
          defaultIslandsPolicy));
        if (newPolicy.ordinal() < result.ordinal())
        {
          result = newPolicy;
        }
      }
    }
    return result;
  }

  @Deprecated
  public abstract String getTextQuery(long textID);

  public abstract String getDocumentQuery(String toplevelCorpusName,
    String documentName);

  public String getMatchedNodesViewName()
  {
    return matchedNodesViewName;
  }

  public void setMatchedNodesViewName(String matchedNodesViewName)
  {
    this.matchedNodesViewName = matchedNodesViewName;
  }

  public String getDefaultIslandsPolicy()
  {
    return defaultIslandsPolicy;
  }

  public void setDefaultIslandsPolicy(String defaultIslandsPolicy)
  {
    this.defaultIslandsPolicy = defaultIslandsPolicy;
  }

  @Override
  public abstract String selectClause(QueryData queryData,
    List<QueryNode> alternative, String indent);

  protected void addSelectClauseAttribute(List<String> fields,
    String table, String column)
  {
    TableAccessStrategy tas = tables(null);
    fields.add(tas.aliasedColumn(table, column) + " AS \""
      + outerQueryTableAccessStrategy.columnName(table, column)
      + "\"");
  }

  @Override
  public Set<String> whereConditions(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    TableAccessStrategy tables = tables(null);

    StringBuffer sb = new StringBuffer();

    // restrict node table to corpus list
    List<Long> corpusList = queryData.getCorpusList();
    if (corpusList != null && !corpusList.isEmpty())
    {
      indent(sb, indent);
      sb.append(tables.aliasedColumn(NODE_TABLE, "toplevel_corpus"));
      sb.append(" IN (");
      sb.append(StringUtils.join(corpusList, ", "));
      sb.append(") AND\n");

      if (!tables.isMaterialized(RANK_TABLE, NODE_TABLE))
      {
        indent(sb, indent);
        sb.append(tables.aliasedColumn(RANK_TABLE, "toplevel_corpus"));
        sb.append(" IN (");
        sb.append(StringUtils.join(corpusList, ", "));
        sb.append(") AND\n");
      }

    }

    // island policies
    HashMap<Long, Properties> corpusProperties =
      queryData.getCorpusConfiguration();
    IslandPolicies islandsPolicy =
      getMostRestrictivePolicy(corpusList, corpusProperties);
    if (islandsPolicy == IslandPolicies.context)
    {
      indent(sb, indent);
      sb.append("(\n");
      indent(sb, indent + TABSTOP + TABSTOP);
      List<String> overlapForOneSpan = new ArrayList<String>();
      for (int i = 1; i <= alternative.size(); ++i)
      {

        StringBuffer sb2 = new StringBuffer();

        sb2.append("(\n");
        indent(sb2, indent + TABSTOP + TABSTOP + TABSTOP);

        sb2.append(tables.aliasedColumn(NODE_TABLE, "text_ref")).
          append(" = solutions.text").append(i).append(" AND\n");
        indent(sb2, indent + TABSTOP + TABSTOP + TABSTOP);

        String rangeMin = "solutions.min" + i;
        String rangeMax = "solutions.max" + i;

        sb2.append(overlapForOneRange(indent + TABSTOP + TABSTOP,
          rangeMin, rangeMax, tables));
        sb2.append("\n");
        indent(sb2, indent + TABSTOP + TABSTOP);
        sb2.append(")");
        overlapForOneSpan.add(sb2.toString());
      }
      sb.append(StringUtils.join(overlapForOneSpan,
        " OR "));
      sb.append("\n");
      indent(sb, indent + TABSTOP);
      sb.append(")");
    }
    else
    {
      indent(sb, indent);
      sb.append(tables.aliasedColumn(NODE_TABLE, "text_ref"));
      sb.append(" IN (");
      List<String> solutionTexts = new ArrayList<String>();
      for (int i = 1; i <= alternative.size(); ++i)
      {
        solutionTexts.add("solutions.text" + i);
      }
      sb.append(StringUtils.join(solutionTexts, ", "));
      sb.append(") AND\n");
      indent(sb, indent + TABSTOP);

      StringBuilder minSb = new StringBuilder();
      StringBuilder maxSb = new StringBuilder();
      minSb.append("ANY(ARRAY[");
      maxSb.append("ANY(ARRAY[");
      List<String> mins = new ArrayList<String>();
      List<String> maxs = new ArrayList<String>();
      for (int i = 1; i <= alternative.size(); ++i)
      {
        mins.add("solutions.min" + i);
        maxs.add("solutions.max" + i);
      }
      minSb.append(StringUtils.join(mins, ", "));
      maxSb.append(StringUtils.join(maxs, ", "));
      minSb.append("])");
      maxSb.append("])");
      String rangeMin = minSb.toString();
      String rangeMax = maxSb.toString();
      sb.append(overlapForOneRange(indent, rangeMin, rangeMax, tables));
    }

    // corpus constriction
    sb.append(" AND\n");
    indent(sb, indent + TABSTOP);
    sb.append(tables.aliasedColumn(CORPUS_TABLE, "id"));
    sb.append(" = ");
    sb.append(tables.aliasedColumn(NODE_TABLE, "corpus_ref"));

    HashSet<String> result = new HashSet<String>();

    result.add(sb.toString());

    return result;
  }

  private String overlapForOneRange(String indent,
    String rangeMin, String rangeMax, TableAccessStrategy tables)
  {
    StringBuffer sb = new StringBuffer();

    sb.append("(\n");
    indent(sb, indent + TABSTOP + TABSTOP);
    if (optimizeOverlap)
    {
      sb.append("(");
      sb.append(tables.aliasedColumn(
        NODE_TABLE, "left_token")).append(" >= ").append(rangeMin);
      sb.append(" AND ");
      sb.append(tables.aliasedColumn(
        NODE_TABLE, "right_token")).append(" <= ").append(rangeMax);
      sb.append(") OR\n");
      indent(sb, indent + TABSTOP + TABSTOP);
      sb.append("(");
      sb.append(tables.aliasedColumn(
        NODE_TABLE, "left_token")).append(" <= ").append(rangeMin);
      sb.append(" AND ");
      sb.append(rangeMin).append(" <= ").append(tables.aliasedColumn(NODE_TABLE,
        "right_token"));
      sb.append(") OR\n");
      indent(sb, indent + TABSTOP + TABSTOP);
      sb.append("(");
      sb.append(tables.aliasedColumn(
        NODE_TABLE, "left_token")).append(" <= ").append(rangeMax);
      sb.append(" AND ");
      sb.append(rangeMax).append(" <= ").append(tables.aliasedColumn(NODE_TABLE,
        "right_token"));
      sb.append(")");
    }
    else
    {
      sb.append(tables.aliasedColumn(
        NODE_TABLE, "left_token")).append(" <= ").append(rangeMax);
      sb.append(" AND ");
      sb.append(tables.aliasedColumn(
        NODE_TABLE, "right_token")).append(" >= ").append(rangeMin);
    }
    sb.append("\n");
    indent(sb, indent + TABSTOP);
    sb.append(")");

    String overlapForOneRange = sb.toString();
    return overlapForOneRange;
  }

  @Override
  public String orderByClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("solutions.n, ");
    String preColumn = tables(null).aliasedColumn(RANK_TABLE, "pre");
    sb.append(preColumn);
    String orderByClause = sb.toString();
    return orderByClause;
  }

  @Override
  public abstract String fromClause(QueryData queryData,
    List<QueryNode> alternative, String indent);

  @Override
  public T extractData(ResultSet resultSet)
    throws SQLException, DataAccessException
  {
    return resultExtractor.extractData(resultSet);
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

  public TableJoinsInFromClauseSqlGenerator getTableJoinsInFromClauseSqlGenerator()
  {
    return tableJoinsInFromClauseSqlGenerator;
  }

  public void setTableJoinsInFromClauseSqlGenerator(
    TableJoinsInFromClauseSqlGenerator tableJoinsInFromClauseSqlGenerator)
  {
    this.tableJoinsInFromClauseSqlGenerator = tableJoinsInFromClauseSqlGenerator;
  }

  public boolean isOptimizeOverlap()
  {
    return optimizeOverlap;
  }

  public void setOptimizeOverlap(boolean optimizeOverlap)
  {
    this.optimizeOverlap = optimizeOverlap;
  }

  public TableAccessStrategy getOuterQueryTableAccessStrategy()
  {
    return outerQueryTableAccessStrategy;
  }

  public boolean isIncludeDocumentNameInAnnotateQuery()
  {
    return includeDocumentNameInAnnotateQuery;
  }

  public void setIncludeDocumentNameInAnnotateQuery(
    boolean includeDocumentNameInAnnotateQuery)
  {
    this.includeDocumentNameInAnnotateQuery = includeDocumentNameInAnnotateQuery;
  }

  public boolean isIncludeIsTokenColumn()
  {
    return includeIsTokenColumn;
  }

  public void setIncludeIsTokenColumn(boolean includeIsTokenColumn)
  {
    this.includeIsTokenColumn = includeIsTokenColumn;
  }

  public CorpusPathExtractor getCorpusPathExtractor()
  {
    return corpusPathExtractor;
  }

  public void setCorpusPathExtractor(CorpusPathExtractor corpusPathExtractor)
  {
    this.corpusPathExtractor = corpusPathExtractor;
  }

  public void setOuterQueryTableAccessStrategy(
    TableAccessStrategy outerQueryTableAccessStrategy)
  {
    this.outerQueryTableAccessStrategy = outerQueryTableAccessStrategy;
  }

  public ResultSetExtractor<T> getResultExtractor()
  {
    return resultExtractor;
  }

  public void setResultExtractor(ResultSetExtractor<T> resultExtractor)
  {
    this.resultExtractor = resultExtractor;
  }
}
