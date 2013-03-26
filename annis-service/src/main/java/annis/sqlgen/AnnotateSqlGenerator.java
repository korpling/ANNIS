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

import static annis.sqlgen.TableAccessStrategy.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
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
  private TableAccessStrategy outerQueryTableAccessStrategy;
  private ResultSetExtractor<T> resultExtractor;
  // helper to extract the corpus path from a JDBC result set
  private CorpusPathExtractor corpusPathExtractor;
  private String matchedNodesViewName;

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
    throw new UnsupportedOperationException(
      "BUG: This method needs to be overwritten by ancestors or through Spring");
  }

  public T queryAnnotationGraph(
    JdbcTemplate jdbcTemplate, String toplevelCorpusName, String documentName)
  {
    return (T) jdbcTemplate.query(getDocumentQuery(toplevelCorpusName,
      documentName), this);
  }

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

    StringBuilder sb = new StringBuilder();

    // restrict node table to corpus list
    List<Long> corpusList = queryData.getCorpusList();
    if (corpusList != null && !corpusList.isEmpty())
    {
      sb.append(indent);
      sb.append(tables.aliasedColumn(NODE_TABLE, "toplevel_corpus"));
      sb.append(" IN (");
      sb.append(StringUtils.join(corpusList, ", "));
      sb.append(") AND\n");

      if (!tables.isMaterialized(RANK_TABLE, NODE_TABLE))
      {
        sb.append(indent);
        sb.append(tables.aliasedColumn(RANK_TABLE, "toplevel_corpus"));
        sb.append(" IN (");
        sb.append(StringUtils.join(corpusList, ", "));
        sb.append(") AND\n");
      }

    }


    String overlap = CommonAnnotateWithClauseGenerator.overlapForOneRange(indent + TABSTOP,
      "solutions.\"min\"", "solutions.\"max\"", "solutions.text", "solutions.corpus",
      tables);
    sb.append(overlap);
    sb.append("\n");



    // corpus constriction
    sb.append(" AND\n");
    sb.append(indent).append(TABSTOP);
    sb.append(tables.aliasedColumn(CORPUS_TABLE, "id"));
    sb.append(" = ");
    sb.append(tables.aliasedColumn(NODE_TABLE, "corpus_ref"));

    HashSet<String> result = new HashSet<String>();

    result.add(sb.toString());

    return result;
  }


  @Override
  public String orderByClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("solutions.n, ");
    sb.append(tables(null).aliasedColumn(COMPONENT_TABLE, "id")).append(", ");
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

  @Override
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
