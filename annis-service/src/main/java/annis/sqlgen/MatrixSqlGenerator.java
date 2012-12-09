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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import org.springframework.dao.DataAccessException;

import annis.dao.AnnotatedMatch;
import annis.dao.AnnotatedSpan;
import annis.model.QueryNode;
import annis.model.Annotation;
import annis.ql.parser.QueryData;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
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

    while (resultSet.next())
    {
      long id = resultSet.getLong("id");
      String coveredText = resultSet.getString("span");

      Array arrayAnnotation = resultSet.getArray("annotations");
      Array arrayMeta = resultSet.getArray("metadata");

      List<Annotation> annotations = extractAnnotations(arrayAnnotation);
      List<Annotation> metaData = extractAnnotations(arrayMeta);

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

      // set annotation spans for *all* positions of the id
      // (node could have matched several times)
      for (int posInMatch = 0; posInMatch < key.size(); posInMatch++)
      {
        if (key.get(posInMatch) == id)
        {
          matchesByGroup.get(key)[posInMatch] =
            new AnnotatedSpan(id, coveredText, annotations, metaData);
        }
      }
    }

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
    
    sb.append(indent).append(TABSTOP);
    sb.append(StringUtils.join(getSelectFields(tables), ",\n" + indent + TABSTOP));
    sb.append("\n");

    return sb.toString();
  }
  
  protected List<String> getSelectFields(TableAccessStrategy tas)
  {
    List<String> result = new LinkedList<String>();
    
    result.add(selectIdString(tas));    
    result.add(selectSpanString(tas));
    result.add(selectAnnotationsString(tas));
    result.add(selectMetadataString(tas));
    
    return result;
  }
  
  protected String selectIdString(TableAccessStrategy tas)
  {
    return tas.aliasedColumn(NODE_TABLE,"id") + " AS id";
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
    StringBuilder sb = new StringBuilder();

    sb.append(indent).append("(\n");
    sb.append(indent);

    sb.append(innerQuerySqlGenerator.toSql(queryData, indent + TABSTOP));
    sb.append(indent).append(") AS solutions,\n");

    sb.append(indent).append(TABSTOP);
    // really ugly
    sb.append(
      tableJoinsInFromClauseGenerator.fromClauseForNode(null, true));

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
    sb.append(indent).append(TABSTOP);
    sb.append("LEFT OUTER JOIN ");
    sb.append(CORPUS_ANNOTATION_TABLE);
    sb.append(" ON (");
    sb.append(tas.aliasedColumn(CORPUS_ANNOTATION_TABLE, "corpus_ref"));
    sb.append(" = ");
    sb.append(tas.aliasedColumn(NODE_TABLE, "corpus_ref"));
    sb.append(")");
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
  public String orderByClause(QueryData queryData, List<QueryNode> alternative, String indent)
  {
    TableAccessStrategy tas = tables(null);
    return "key, " 
      + tas.aliasedColumn(NODE_TABLE, "corpus_ref") + ", "
      + tas.aliasedColumn(NODE_TABLE, "text_ref") + ", "
      + tas.aliasedColumn(NODE_TABLE, "token_index") + ", "
      + tas.aliasedColumn(NODE_TABLE, "id");
  }

  private List<Annotation> extractAnnotations(Array array) throws SQLException
  {
    List<Annotation> result = new ArrayList<Annotation>();

    if (array != null)
    {
      String[] arrayLines = (String[]) array.getArray();

      for (String line : arrayLines)
      {
        if (line != null)
        {
          String namespace = null;
          String name = null;
          String value = null;

          String[] split = line.split(":");
          if (split.length > 2)
          {
            namespace = split[0];
            name = split[1];
            value = split[2];
          }
          else if (split.length > 1)
          {
            name = split[0];
            value = split[1];
          }
          else
          {
            name = split[0];
          }

          if (value != null)
          {
            try
            {
              value = new String(Base64.decodeBase64(value), "UTF-8");
            }
            catch (UnsupportedEncodingException ex)
            {
              log.error(null, ex);
            }
          }

          result.add(new annis.model.Annotation(namespace, name, value));
        } // if line not null
      }
    }

    return result;
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

}
