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

import static annis.sqlgen.TableAccessStrategy.CORPUS_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.TEXT_TABLE;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import org.springframework.dao.DataAccessException;

import annis.dao.objects.AnnotatedMatch;
import annis.dao.objects.AnnotatedSpan;
import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class MatrixSqlGenerator
  extends AbstractSqlGenerator<List<AnnotatedMatch>>
  implements SelectClauseSqlGenerator<QueryData>,
  FromClauseSqlGenerator<QueryData>,
  WhereClauseSqlGenerator<QueryData>, GroupByClauseSqlGenerator<QueryData>,
  OrderByClauseSqlGenerator<QueryData>
{

  private final Logger log = LoggerFactory.getLogger(MatrixSqlGenerator.class);

  @Deprecated
  private String matchedNodesViewName;

  private SqlGenerator<QueryData, ?> innerQuerySqlGenerator;

  private TableJoinsInFromClauseSqlGenerator tableJoinsInFromClauseGenerator;

  private AnnotatedSpanExtractor spanExtractor;

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
  public List<AnnotatedMatch> extractData(ResultSet resultSet)
    throws SQLException, DataAccessException
  {
    List<AnnotatedMatch> matches = new ArrayList<AnnotatedMatch>();

    Map<List<Long>, AnnotatedSpan[]> matchesByGroup =
      new HashMap<List<Long>, AnnotatedSpan[]>();

    int rowNum = 0;
    while (resultSet.next())
    {
      long id = resultSet.getLong("id");

      // create key
      Array sqlKey = resultSet.getArray("key");
      Validate.isTrue(!resultSet.wasNull(),
        "Match group identifier must not be null");
      Validate.isTrue(sqlKey.getBaseType() == Types.BIGINT,
        "Key in database must be from the type \"bigint\" but was \"" + sqlKey.
        getBaseTypeName() + "\"");

      Long[] keyArray = (Long[]) sqlKey.getArray();
      int matchWidth = keyArray.length;
      List<Long> key = Arrays.asList(keyArray);


      if (!matchesByGroup.containsKey(key))
      {
        matchesByGroup.put(key, new AnnotatedSpan[matchWidth]);
      }
      AnnotatedSpan span = spanExtractor.mapRow(resultSet, rowNum);

      // set annotation spans for *all* positions of the id
      // (node could have matched several times)
      for (int posInMatch = 0; posInMatch < key.size(); posInMatch++)
      {
        if (key.get(posInMatch) == id)
        {
          matchesByGroup.get(key)[posInMatch] = span;
        }
      }
      rowNum++;
    } // end for each row

    for (AnnotatedSpan[] match : matchesByGroup.values())
    {
      matches.add(new AnnotatedMatch(Arrays.asList(match)));
    }

    return matches;
  }

  @Override
  public String selectClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {

    StringBuilder sb = new StringBuilder();
    sb.append("\n");


    TableAccessStrategy tables = tables(null);

    SolutionKey<?> key = createSolutionKey();
//    TableAccessStrategy tas = createTableAccessStrategy();
    List<String> keyColumns =
      key.generateOuterQueryColumns(tables, alternative.size());

    // key
    sb.append(indent).append(TABSTOP);
    sb.append(StringUtils.join(keyColumns, ", "));
    sb.append(",\n");

    // fields

    List<MatrixQueryData> matrixExtList = queryData.getExtensions(
      MatrixQueryData.class);

    sb.append(indent).append(TABSTOP);
    sb.append(StringUtils.join(getSelectFields(tables,
      matrixExtList.isEmpty() ? null : matrixExtList.get(0)),
      ",\n" + indent + TABSTOP));
    sb.append("\n");

    return sb.toString();
  }

  protected List<String> getSelectFields(TableAccessStrategy tas,
    MatrixQueryData matrixExt)
  {
    List<String> result = new LinkedList<String>();

    result.add(selectIdString(tas));
    result.add(selectSpanString(tas));
    result.add(selectAnnotationsString(tas));

    if (matrixExt != null && matrixExt.getMetaKeys() != null && !matrixExt.
      getMetaKeys().isEmpty())
    {
      result.add(selectMetadataString(tas));
    }
    return result;
  }

  protected String selectIdString(TableAccessStrategy tas)
  {
    return tas.aliasedColumn(NODE_TABLE, "id") + " AS id";
  }

  protected String selectSpanString(TableAccessStrategy tas)
  {
    return "min(substr(" + tas.aliasedColumn(TEXT_TABLE, "text") + ", "
      + tas.aliasedColumn(NODE_TABLE, "left") + " + 1, "
      + tas.aliasedColumn(NODE_TABLE, "right") + " - "
      + tas.aliasedColumn(NODE_TABLE, "left") + ")) AS span";
  }

  protected String selectAnnotationsString(TableAccessStrategy tas)
  {
    return "array_agg(DISTINCT coalesce("
      + tas.aliasedColumn(NODE_ANNOTATION_TABLE, "namespace")
      + " || ':', '') || "
      + tas.aliasedColumn(NODE_ANNOTATION_TABLE, "name")
      + " || ':' || encode("
      + tas.aliasedColumn(NODE_ANNOTATION_TABLE, "value")
      + "::bytea, 'base64')) AS annotations";
  }

  protected String selectMetadataString(TableAccessStrategy tas)
  {
    return "array_agg(DISTINCT coalesce("
      + tas.aliasedColumn(CORPUS_ANNOTATION_TABLE, "namespace")
      + " || ':', '') || "
      + tas.aliasedColumn(CORPUS_ANNOTATION_TABLE, "name")
      + " || ':' || encode("
      + tas.aliasedColumn(CORPUS_ANNOTATION_TABLE, "value")
      + "::bytea, 'base64')) AS metadata";
  }

  @Override
  public String fromClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    TableAccessStrategy tas = tables(null);
    
    StringBuilder sb = new StringBuilder();

    sb.append(indent).append("(\n");
    sb.append(indent);

    sb.append(innerQuerySqlGenerator.toSql(queryData, indent + TABSTOP));
    sb.append(indent).append(") AS solutions,\n");

    sb.append(indent).append(TABSTOP);
    sb.append(
      AbstractFromClauseGenerator.tableAliasDefinition(tas.getTableAliases(), null, NODE_TABLE, 1));

    sb.append("\n");

    TableAccessStrategy tables = tables(null);

    addFromOuterJoins(sb, queryData, tables, indent);
    sb.append(",\n");

    sb.append(indent).append(TABSTOP);
    sb.append(TEXT_TABLE);

    return sb.toString();
  }

  protected void addFromOuterJoins(StringBuilder sb, QueryData queryData,
    TableAccessStrategy tas, String indent)
  {

    List<MatrixQueryData> matrixExtList = queryData.getExtensions(
      MatrixQueryData.class);

    // restrict to certain annnotation or metadata keys if wanted
    if (!matrixExtList.isEmpty())
    {
      MatrixQueryData matrixExt = matrixExtList.get(0);

      if (matrixExt.getMetaKeys() != null && !matrixExt.getMetaKeys().isEmpty())
      {
        sb.append(indent).append(TABSTOP);
        sb.append("LEFT OUTER JOIN ");
        sb.append(CORPUS_ANNOTATION_TABLE);
        sb.append(" ON (");
        sb.append(tas.aliasedColumn(CORPUS_ANNOTATION_TABLE, "corpus_ref"));
        sb.append(" = ");
        sb.append(tas.aliasedColumn(NODE_TABLE, "corpus_ref"));

        Set<String> conditions = new TreeSet<String>();
        addAnnoSelectionCondition(conditions, matrixExt.getMetaKeys(),
          CORPUS_ANNOTATION_TABLE, tas);
        sb.append(" AND ").append(StringUtils.join(conditions, " AND "));

        sb.append(")\n");
      }
    }

  }

  @Override
  public Set<String> whereConditions(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {


    Set<String> conditions = new HashSet<String>();
    StringBuilder sb = new StringBuilder();
    TableAccessStrategy tables = tables(null);

    // corpus selection
    List<Long> corpusList = queryData.getCorpusList();
    if (corpusList != null && !corpusList.isEmpty())
    {
      sb.append(tables.aliasedColumn(NODE_TABLE, "toplevel_corpus"));
      sb.append(" IN (");
      sb.append(StringUtils.join(corpusList, ", "));
      sb.append(")");
      conditions.add(sb.toString());
    }

    // text table table joining (FIXME: why not in from clause)
    sb.setLength(0);
    sb.append(tables.aliasedColumn(TEXT_TABLE, "id"));
    sb.append(" = ");
    sb.append(tables.aliasedColumn(NODE_TABLE, "text_ref"));
    conditions.add(sb.toString());

    conditions.add(tables.aliasedColumn(TEXT_TABLE, "corpus_ref") + " = "
      + tables.aliasedColumn(NODE_TABLE, "corpus_ref"));


    // nodes selected by id
    sb.setLength(0);
    sb.append("(\n");

    sb.append(indent).append(TABSTOP).append(TABSTOP);
    List<String> ors = new ArrayList<String>();
    for (int i = 1; i <= queryData.getMaxWidth(); ++i)
    {
      ors.add(
        tables.aliasedColumn(NODE_TABLE, "id") + " = solutions.id" + String.
        valueOf(i));
    }
    sb.append(StringUtils.join(ors, " OR\n" + indent + TABSTOP + TABSTOP));
    sb.append("\n");
    sb.append(indent).append(TABSTOP);
    sb.append(")");
    conditions.add(sb.toString());

    return conditions;

  }

  private void addAnnoSelectionCondition(Set<String> conditions,
    List<MatrixQueryData.QName> selected, String tableName,
    TableAccessStrategy tables)
  {
    List<String> orConditions = new LinkedList<String>();
    Iterator<MatrixQueryData.QName> itMatrix = selected.iterator();
    while (itMatrix.hasNext())
    {
      MatrixQueryData.QName qname = itMatrix.next();

      if (qname.name != null && qname.namespace != null)
      {
        orConditions.add(
          tables.aliasedColumn(tableName, "name")
          + " = " + SqlConstraints.sqlString(qname.name) + " AND "
          + tables.aliasedColumn(tableName, "namespace")
          + " = " + SqlConstraints.sqlString(qname.namespace));
      }
      else if (qname.namespace != null)
      {
        orConditions.add(tables.aliasedColumn(tableName,
          "namespace")
          + " = " + SqlConstraints.sqlString(qname.namespace));
      }
      else if (qname.name != null)
      {
        orConditions.add(tables.aliasedColumn(tableName,
          "name")
          + " = " + SqlConstraints.sqlString(qname.name));
      }
    }
    if (orConditions.isEmpty())
    {
      //  add an always false condition on corpus_annotation
      conditions.add(tables.aliasedColumn(tableName, "name") + " IS NULL");
    }
    else
    {
      conditions.add("(" + StringUtils.join(orConditions, " OR ") + ")");
    }
  }

  @Override
  public String groupByAttributes(QueryData queryData,
    List<QueryNode> alternative)
  {
    TableAccessStrategy tas = tables(null);
    return "key, "
      + tas.aliasedColumn(NODE_TABLE, "corpus_ref") + ", "
      + tas.aliasedColumn(NODE_TABLE, "text_ref") + ", "
      + tas.aliasedColumn(NODE_TABLE, "token_index") + ", "
      + tas.aliasedColumn(NODE_TABLE, "id");
  }

  @Override
  public String orderByClause(QueryData queryData, List<QueryNode> alternative,
    String indent)
  {
    TableAccessStrategy tas = tables(null);
    return "key, "
      + tas.aliasedColumn(NODE_TABLE, "corpus_ref") + ", "
      + tas.aliasedColumn(NODE_TABLE, "text_ref") + ", "
      + tas.aliasedColumn(NODE_TABLE, "token_index") + ", "
      + tas.aliasedColumn(NODE_TABLE, "id");
  }

  public String getMatchedNodesViewName()
  {
    return matchedNodesViewName;
  }

  public void setMatchedNodesViewName(String matchedNodesViewName)
  {
    this.matchedNodesViewName = matchedNodesViewName;
  }

  public SqlGenerator<QueryData, ?> getInnerQuerySqlGenerator()
  {
    return innerQuerySqlGenerator;
  }

  public void setInnerQuerySqlGenerator(
    SqlGenerator<QueryData, ?> innerQuerySqlGenerator)
  {
    this.innerQuerySqlGenerator = innerQuerySqlGenerator;
  }

  public TableJoinsInFromClauseSqlGenerator getTableJoinsInFromClauseGenerator()
  {
    return tableJoinsInFromClauseGenerator;
  }

  public void setTableJoinsInFromClauseGenerator(
    TableJoinsInFromClauseSqlGenerator tableJoinsInFromClauseGenerator)
  {
    this.tableJoinsInFromClauseGenerator = tableJoinsInFromClauseGenerator;
  }

  public AnnotatedSpanExtractor getSpanExtractor()
  {
    return spanExtractor;
  }

  public void setSpanExtractor(AnnotatedSpanExtractor spanExtractor)
  {
    this.spanExtractor = spanExtractor;
  }
}
