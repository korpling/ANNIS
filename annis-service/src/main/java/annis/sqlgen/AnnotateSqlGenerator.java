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

import static annis.sqlgen.SqlConstraints.sqlString;
import static annis.sqlgen.TableAccessStrategy.COMPONENT_TABLE;
import static annis.sqlgen.TableAccessStrategy.CORPUS_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import annis.service.objects.SubgraphFilter;
import annis.sqlgen.extensions.AnnotateQueryData;
import annis.sqlgen.extensions.LimitOffsetQueryData;

/**
 *
 * @param <T> Type into which the JDBC result set is transformed
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class AnnotateSqlGenerator<T>
  extends AbstractSqlGenerator
  implements SelectClauseSqlGenerator<QueryData>,
  FromClauseSqlGenerator<QueryData>,
  WhereClauseSqlGenerator<QueryData>, OrderByClauseSqlGenerator<QueryData>,
  AnnotateExtractor<T>,
  SqlGeneratorAndExtractor<QueryData, T>
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
   * @return 
   */
  protected SolutionKey<?> createSolutionKey()
  {
    throw new UnsupportedOperationException(
      "BUG: This method needs to be overwritten by ancestors or through Spring");
  }

  public T queryAnnotationGraph(
    JdbcTemplate jdbcTemplate, long toplevelCorpusID, String documentName,
     List<String> nodeAnnotationFilter)
  {
    return (T) jdbcTemplate.query(getDocumentQuery(toplevelCorpusID,
      documentName, nodeAnnotationFilter), this);
  }

  public String getMatchedNodesViewName()
  {
    return matchedNodesViewName;
  }

  public void setMatchedNodesViewName(String matchedNodesViewName)
  {
    this.matchedNodesViewName = matchedNodesViewName;
  }

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
    
    HashSet<String> result = new HashSet<>();
    
    TableAccessStrategy tables = tables(null);

    List<AnnotateQueryData> annoExtList = queryData.getExtensions(AnnotateQueryData.class);
    if(!annoExtList.isEmpty())
    {
      AnnotateQueryData annoExt = annoExtList.get(0);

      if(annoExt.getFilter() == SubgraphFilter.token)
      {
        result.add(tables.aliasedColumn(NODE_TABLE, "is_token") + " IS TRUE");
      }
    }
    
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

    result.add(sb.toString());

    return result;
  }


  @Override
  public String orderByClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("solutions.n, ");
    sb.append(tables(null).aliasedColumn(COMPONENT_TABLE, "name")).append(", ");
    sb.append(tables(null).aliasedColumn(COMPONENT_TABLE, "id")).append(", ");
    String preColumn = tables(null).aliasedColumn(RANK_TABLE, "pre");
    sb.append(preColumn);
    String orderByClause = sb.toString();
    return orderByClause;
  }

  @Override
  public String fromClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    List<Long> corpusList = queryData.getCorpusList();
    StringBuilder sb = new StringBuilder();
    
    sb.append(indent).append("solutions,\n");
    
    String factsTable = SelectedFactsFromClauseGenerator.selectedFactsSQL(corpusList, indent + TABSTOP);
    sb.append(factsTable);
    sb.append(" AS facts,\n");

    sb.append(indent).append(TABSTOP);
    sb.append(TableAccessStrategy.CORPUS_TABLE);

    return sb.toString();
  }

  @Override
  public String selectClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    String innerIndent = indent + TABSTOP;
    StringBuilder sb = new StringBuilder();

    sb.append("DISTINCT\n");
    
    sb.append(innerIndent).append("solutions.\"key\",\n");
    sb.append(innerIndent);
    
    int matchStart = 0;
    List<LimitOffsetQueryData> extension =
      queryData.getExtensions(LimitOffsetQueryData.class);
    if(extension.size() > 0)
    {
      matchStart = extension.get(0).getOffset();
    }

    sb.append(matchStart).append(" AS \"matchstart\",\n");
    sb.append(innerIndent).append("solutions.n,\n");

    List<String> fields = getSelectFields();

    sb.append(innerIndent).append(StringUtils.join(fields, ",\n" + innerIndent));
    sb.append(innerIndent).append(",\n");

    // corpus.path_name
    sb.append(innerIndent).append("corpus.path_name AS path");

    if (isIncludeDocumentNameInAnnotateQuery())
    {
      sb.append(",\n");
      sb.append(innerIndent).append("corpus.path_name[1] AS document_name");
    }
    return sb.toString();
  }
  
  protected List<String> getSelectFields()
  {
    List<String> fields = new ArrayList<>();
    
    addSelectClauseAttribute(fields, NODE_TABLE, "id");
    addSelectClauseAttribute(fields, NODE_TABLE, "text_ref");
    addSelectClauseAttribute(fields, NODE_TABLE, "corpus_ref");
    addSelectClauseAttribute(fields, NODE_TABLE, "toplevel_corpus");
    addSelectClauseAttribute(fields, NODE_TABLE, "namespace");
    addSelectClauseAttribute(fields, NODE_TABLE, "name");
    addSelectClauseAttribute(fields, NODE_TABLE, "salt_id");
    addSelectClauseAttribute(fields, NODE_TABLE, "left");
    addSelectClauseAttribute(fields, NODE_TABLE, "right");
    addSelectClauseAttribute(fields, NODE_TABLE, "token_index");
    if (isIncludeIsTokenColumn())
    {
      addSelectClauseAttribute(fields, NODE_TABLE, "is_token");
    }
    addSelectClauseAttribute(fields, NODE_TABLE, "span");
    addSelectClauseAttribute(fields, NODE_TABLE, "left_token");
    addSelectClauseAttribute(fields, NODE_TABLE, "right_token");
    addSelectClauseAttribute(fields, NODE_TABLE, "seg_name");
    addSelectClauseAttribute(fields, NODE_TABLE, "seg_index");
    addSelectClauseAttribute(fields, RANK_TABLE, "id");
    addSelectClauseAttribute(fields, RANK_TABLE, "pre");
    addSelectClauseAttribute(fields, RANK_TABLE, "post");
    addSelectClauseAttribute(fields, RANK_TABLE, "parent");
    addSelectClauseAttribute(fields, RANK_TABLE, "root");
    addSelectClauseAttribute(fields, RANK_TABLE, "level");
    addSelectClauseAttribute(fields, COMPONENT_TABLE, "id");
    addSelectClauseAttribute(fields, COMPONENT_TABLE, "type");
    addSelectClauseAttribute(fields, COMPONENT_TABLE, "name");
    addSelectClauseAttribute(fields, COMPONENT_TABLE, "namespace");
    
    fields.add("(splitanno(node_qannotext))[1] as node_annotation_namespace");
    fields.add("(splitanno(node_qannotext))[2] as node_annotation_name");
    fields.add("(splitanno(node_qannotext))[3] as node_annotation_value");
    
    fields.add("(splitanno(edge_qannotext))[1] as edge_annotation_namespace");
    fields.add("(splitanno(edge_qannotext))[2] as edge_annotation_name");
    fields.add("(splitanno(edge_qannotext))[3] as edge_annotation_value");

    
    return fields;
  }

  public String getDocumentQuery(long toplevelCorpusID, String documentName,
     List<String> nodeAnnotationFilter)
  {
    TableAccessStrategy tas = createTableAccessStrategy();
    List<String> fields = getSelectFields();
    
    boolean filter = false;
    Set<String> qualifiedNodeAnnos = new LinkedHashSet<>();
    Set<String> unqualifiedNodeAnnos = new LinkedHashSet<>();
    if(nodeAnnotationFilter != null)
    {
      Splitter namespaceSplitter = Splitter.on("::").trimResults().limit(2);
      filter = true;
      for(String anno : nodeAnnotationFilter)
      {
        List<String> splitted = namespaceSplitter.splitToList(anno);
        if(splitted.size() > 1)
        {
          qualifiedNodeAnnos.add(
            AnnotationConditionProvider.regexEscaper.escape(splitted.get(0)) 
            + ":" 
            + AnnotationConditionProvider.regexEscaper.escape(splitted.get(1)));
        }
        else
        {
          unqualifiedNodeAnnos.add(
            AnnotationConditionProvider.regexEscaper.escape(splitted.get(0)));
        }
      }
    }
    
    
    StringBuilder template = new StringBuilder();
    template.append("SELECT DISTINCT \n"
      + "\tARRAY[-1::bigint] AS key, ARRAY[''::varchar] AS key_names, 0 as matchstart, "
      +  StringUtils.join(fields, ", ") +", "
      + "c.path_name as path, c.path_name[1] as document_name\n"
      + "FROM\n"
      + "\tfacts_:top AS facts,\n"
      + "\tcorpus as c, corpus as toplevel\n"
      + "WHERE\n"
      + "\ttoplevel.id = :top AND c.name = :document_name AND " + tas.aliasedColumn(NODE_TABLE, "corpus_ref") + " = c.id\n"
      + "\tAND toplevel.top_level IS TRUE\n"
      + "\tAND c.pre >= toplevel.pre AND c.post <= toplevel.post\n");
    
    if(filter)
    {
      
      template.append("\tAND (is_token IS TRUE");
      
      if(!qualifiedNodeAnnos.isEmpty())
      {
        String orExpr = Joiner.on(")|(").join(qualifiedNodeAnnos);
        template.append(" OR node_qannotext ~ '(^((").append(orExpr).append(")):(.*)$)' ");
      }
      if(!unqualifiedNodeAnnos.isEmpty())
      {
        String orExpr = Joiner.on(")|(").join(unqualifiedNodeAnnos);
        template.append(" OR node_annotext ~ '(^((").append(orExpr).append(")):(.*)$)' ");
      }
      template.append(")\n");
    }
      
    template.append("ORDER BY ").
      append(tas.aliasedColumn(COMPONENT_TABLE, "name")).append(", ").
      append(tas.aliasedColumn(COMPONENT_TABLE, "id")).append(", ").
      append(tas.aliasedColumn(RANK_TABLE, "pre"));
    String sql = template.toString().replace(":top", "" + toplevelCorpusID)
      .replace(":document_name", sqlString(documentName));
    return sql;
    
  }

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
