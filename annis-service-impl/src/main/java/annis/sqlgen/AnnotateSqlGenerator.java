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

import annis.administration.SchemeType;
import static annis.sqlgen.TableAccessStrategy.COMPONENT_TABLE;
import static annis.sqlgen.TableAccessStrategy.CORPUS_TABLE;
import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
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
import org.apache.commons.lang.Validate;
import org.springframework.jdbc.core.JdbcTemplate;

import annis.model.QueryNode;
import annis.ql.parser.QueryData;

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
  WhereClauseSqlGenerator<QueryData>, OrderByClauseSqlGenerator<QueryData>
{

  // include document name in SELECT clause
  private boolean includeDocumentNameInAnnotateQuery;
  
  // include is_token column in SELECT clause
  private boolean includeIsTokenColumn;
  
  private AnnotateInnerQuerySqlGenerator innerQuerySqlGenerator;
  private TableJoinsInFromClauseSqlGenerator tableJoinsInFromClauseSqlGenerator;
  private TableAccessStrategy factsTas;
  private boolean optimizeOverlap;
  private SchemeType tableLayout = SchemeType.ANNO_POOL;

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
    // FIXME: totally ugly, but the query has fixed column names 
    // (and needs its own column aliasing)
    // TableAccessStrategyFactory wants a corpus selection 
    // strategy
    // solution: build AnnisNodes with API and refactor 
    // SqlGenerator to accept GROUP BY nodes
    Map<String, String> nodeColumns = new HashMap<String, String>();
    nodeColumns.put("namespace", "node_namespace");
    nodeColumns.put("name", "node_name");

    Map<String, String> nodeAnnotationColumns = new HashMap<String, String>();
    nodeAnnotationColumns.put("node_ref", "id");
    nodeAnnotationColumns.put("namespace", "node_annotation_namespace");
    nodeAnnotationColumns.put("name", "node_annotation_name");
    nodeAnnotationColumns.put("value", "node_annotation_value");

    Map<String, String> edgeAnnotationColumns = new HashMap<String, String>();
    nodeAnnotationColumns.put("rank_ref", "pre");
    edgeAnnotationColumns.put("namespace", "edge_annotation_namespace");
    edgeAnnotationColumns.put("name", "edge_annotation_name");
    edgeAnnotationColumns.put("value", "edge_annotation_value");

    Map<String, String> edgeColumns = new HashMap<String, String>();
    edgeColumns.put("node_ref", "id");

    Map<String, String> componentColumns = new HashMap<String, String>();
    componentColumns.put("id", "component_id");
    componentColumns.put("name", "edge_name");
    componentColumns.put("namespace", "edge_namespace");
    componentColumns.put("type", "edge_type");

    edgeColumns.put("name", "edge_name");
    edgeColumns.put("namespace", "edge_namespace");

    Map<String, Map<String, String>> columnAliases =
      new HashMap<String, Map<String, String>>();
    columnAliases.put(TableAccessStrategy.NODE_TABLE, nodeColumns);
    columnAliases.put(TableAccessStrategy.NODE_ANNOTATION_TABLE,
      nodeAnnotationColumns);
    columnAliases.put(TableAccessStrategy.EDGE_ANNOTATION_TABLE,
      edgeAnnotationColumns);
    columnAliases.put(TableAccessStrategy.RANK_TABLE, edgeColumns);
    columnAliases.put(COMPONENT_TABLE, componentColumns);

    factsTas = new TableAccessStrategy(null);
    factsTas.setColumnAliases(columnAliases);


  }

  protected SolutionKey<?> createSolutionKey()
  {
    throw new NotImplementedException("BUG: This method needs to be overwritten by ancestors or through Spring");
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
  public String getTextQuery(long textID)
  {
    if (tableLayout == SchemeType.ANNO_POOL)
    {
      String template = "SELECT DISTINCT \n"
        + "\tARRAY[-1::bigint] AS key, ARRAY[''::varchar] AS key_names, 0 as matchstart, facts.*, c.path_name as path, c.path_name[1] as document_name,"
        + "node_anno.namespace AS node_annotation_namespace, "
        + "node_anno.\"name\" AS node_annotation_name, "
        + "node_anno.val AS node_annotation_value,\n"
        + "edge_anno.namespace AS edge_annotation_namespace, "
        + "edge_anno.\"name\" AS edge_annotation_name, "
        + "edge_anno.val AS edge_annotation_value\n"
        + "FROM\n"
        + "\tfacts AS facts, corpus as c, annotation_pool as node_anno, annotation_pool as edge_anno\n"
        + "WHERE\n"
        + "\tfacts.text_ref = :text_id AND facts.corpus_ref = c.id\n"
        + "\tAND node_anno.id = facts.node_anno_ref\n"
        + "\tAND edge_anno.id = facts.edge_anno_ref\n"
        + "ORDER BY facts.pre";
      String sql = template.replace(":text_id", String.valueOf(textID));
      return sql;
    }
    else
    {
      String template = "SELECT DISTINCT \n"
        + "\tARRAY[-1::bigint] AS key, ARRAY[''::varchar] AS key_names, 0 as matchstart, facts.*, c.path_name as path, c.path_name[1] as document_name,"
        + "node_anno.namespace AS node_annotation_namespace, "
        + "node_anno.\"name\" AS node_annotation_name, "
        + "node_anno.val AS node_annotation_value,\n"
        + "edge_anno.namespace AS edge_annotation_namespace, "
        + "edge_anno.\"name\" AS edge_annotation_name, "
        + "edge_anno.val AS edge_annotation_value\n"
        + "FROM\n"
        + "\tfacts AS facts, corpus as c, annotation_pool as node_anno, annotation_pool as edge_anno\n"
        + "WHERE\n"
        + "\tfacts.text_ref = :text_id AND facts.corpus_ref = c.id\n"
        + "\tAND node_anno.id = facts.node_anno_ref AND node_anno.\"type\" = 'node'\n"
        + "\tAND edge_anno.id = facts.edge_anno_ref AND edge_anno.\"type\" = 'edge'\n"
        + "ORDER BY facts.pre";
      String sql = template.replace(":text_id", String.valueOf(textID));
      return sql;
    }
  }

  public String getDocumentQuery(String toplevelCorpusName, String documentName)
  {
    if (tableLayout == SchemeType.ANNO_POOL)
    {
      String template = "SELECT DISTINCT \n"
        + "\tARRAY[-1::bigint] AS key, ARRAY[''::varchar] AS key_names, 0 as matchstart, facts.*, c.path_name as path, c.path_name[1] as document_name, "
        + "node_anno.namespace AS node_annotation_namespace, "
        + "node_anno.\"name\" AS node_annotation_name, "
        + "node_anno.val AS node_annotation_value,\n"
        + "edge_anno.namespace AS edge_annotation_namespace, "
        + "edge_anno.\"name\" AS edge_annotation_name, "
        + "edge_anno.val AS edge_annotation_value\n"
        + "FROM\n"
        + "\tfacts AS facts, corpus as c, corpus as toplevel, \n"
        + "\tannotation_pool as node_anno, annotation_pool as edge_anno\n"
        + "WHERE\n"
        + "\ttoplevel.name = ':toplevel_name' AND c.name = ':document_name' AND facts.corpus_ref = c.id\n"
        + "\tAND c.pre >= toplevel.pre AND c.post <= toplevel.post\n"
        + "\tAND node_anno.id = facts.node_anno_ref AND node_anno.toplevel_corpus = toplevel.id\n"
        + "\tAND edge_anno.id = facts.edge_anno_ref AND edge_anno.toplevel_corpus = toplevel.id\n"
        + "ORDER BY facts.pre";
      String sql = template.replace(":toplevel_name", String.valueOf(
        toplevelCorpusName)).replace(":document_name", documentName);
      return sql;
    }
    else
    {
      String template = "SELECT DISTINCT \n"
        + "\tARRAY[-1::bigint] AS key, ARRAY[''::varchar] AS key_names, 0 as matchstart, facts.*, c.path_name as path, c.path_name[1] as document_name, "
        + "node_anno.namespace AS node_annotation_namespace, "
        + "node_anno.\"name\" AS node_annotation_name, "
        + "node_anno.val AS node_annotation_value,\n"
        + "edge_anno.namespace AS edge_annotation_namespace, "
        + "edge_anno.\"name\" AS edge_annotation_name, "
        + "edge_anno.val AS edge_annotation_value\n"
        + "FROM\n"
        + "\tfacts AS facts, corpus as c, corpus as toplevel, \n"
        + "\tannotation_pool as node_anno, annotation_pool as edge_anno\n"
        + "WHERE\n"
        + "\ttoplevel.name = ':toplevel_name' AND c.name = ':document_name' AND facts.corpus_ref = c.id\n"
        + "\tAND c.pre >= toplevel.pre AND c.post <= toplevel.post\n"
        + "\tAND node_anno.id = facts.node_anno_ref AND node_anno.\"type\" = 'node' AND node_anno.toplevel_corpus = toplevel.id\n"
        + "\tAND edge_anno.id = facts.edge_anno_ref AND edge_anno.\"type\" = 'edge' AND edge_anno.toplevel_corpus = toplevel.id\n"
        + "ORDER BY facts.pre";
      String sql = template.replace(":toplevel_name", String.valueOf(
        toplevelCorpusName)).replace(":document_name", documentName);
      return sql;
    }
  }

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

// new
  @Override
  public String selectClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    StringBuilder sb = new StringBuilder();
    SolutionKey<?> key = createSolutionKey();
    
    sb.append("DISTINCT\n");
    List<String> keyColumns = key.generateOuterQueryColumns(createTableAccessStrategy(), alternative.size());
    for (String keyColumn : keyColumns) {
      indent(sb, indent + TABSTOP);
      sb.append(keyColumn);
      sb.append(",\n");
    }
    indent(sb, indent + TABSTOP);
    List<AnnotateQueryData> extension =
      queryData.getExtensions(AnnotateQueryData.class);
    Validate.isTrue(extension.size() > 0);
    sb.append(extension.get(0).getOffset()).append("::integer AS matchstart,\n");

    List<String> fields = new ArrayList<String>();
    // facts.fid is never evaluated in the result set
    // addSelectClauseAttribute(fields, FACTS_TABLE, "fid");
    addSelectClauseAttribute(fields, NODE_TABLE, "id");
    addSelectClauseAttribute(fields, NODE_TABLE, "text_ref");
    addSelectClauseAttribute(fields, NODE_TABLE, "corpus_ref");
    addSelectClauseAttribute(fields, NODE_TABLE, "toplevel_corpus");
    addSelectClauseAttribute(fields, NODE_TABLE, "namespace");
    addSelectClauseAttribute(fields, NODE_TABLE, "name");
    addSelectClauseAttribute(fields, NODE_TABLE, "left");
    addSelectClauseAttribute(fields, NODE_TABLE, "right");
    addSelectClauseAttribute(fields, NODE_TABLE, "token_index");
    if (includeIsTokenColumn) {
      addSelectClauseAttribute(fields, NODE_TABLE, "is_token");
    }
    addSelectClauseAttribute(fields, NODE_TABLE, "continuous");
    addSelectClauseAttribute(fields, NODE_TABLE, "span");
    addSelectClauseAttribute(fields, NODE_TABLE, "left_token");
    addSelectClauseAttribute(fields, NODE_TABLE, "right_token");
    addSelectClauseAttribute(fields, RANK_TABLE, "pre");
    addSelectClauseAttribute(fields, RANK_TABLE, "post");
    addSelectClauseAttribute(fields, RANK_TABLE, "parent");
    addSelectClauseAttribute(fields, RANK_TABLE, "root");
    addSelectClauseAttribute(fields, RANK_TABLE, "level");
    addSelectClauseAttribute(fields, COMPONENT_TABLE, "id");
    addSelectClauseAttribute(fields, COMPONENT_TABLE, "type");
    addSelectClauseAttribute(fields, COMPONENT_TABLE, "name");
    addSelectClauseAttribute(fields, COMPONENT_TABLE, "namespace");
    if (tableLayout == SchemeType.FULLFACTS)
    {
      addSelectClauseAttribute(fields, NODE_ANNOTATION_TABLE, "namespace");
      addSelectClauseAttribute(fields, NODE_ANNOTATION_TABLE, "name");
      addSelectClauseAttribute(fields, NODE_ANNOTATION_TABLE, "value");
      addSelectClauseAttribute(fields, EDGE_ANNOTATION_TABLE, "namespace");
      addSelectClauseAttribute(fields, EDGE_ANNOTATION_TABLE, "name");
      addSelectClauseAttribute(fields, EDGE_ANNOTATION_TABLE, "value");
    }

    sb.append(indent).append(TABSTOP);
    sb.append(StringUtils.join(fields, ",\n" + indent + TABSTOP));
    sb.append(",\n").append(indent).append(TABSTOP);

    if (tableLayout == SchemeType.ANNO_POOL)
    {
      sb.append("node_anno.namespace AS node_annotation_namespace,\n").append(
        indent).append(TABSTOP);
      sb.append("node_anno.\"name\" AS node_annotation_name,\n").append(indent).
        append(TABSTOP);
      sb.append("node_anno.\"val\" AS node_annotation_value,\n").append(indent).
        append(TABSTOP);
      sb.append("edge_anno.namespace AS edge_annotation_namespace,\n").append(
        indent).append(TABSTOP);
      sb.append("edge_anno.\"name\" AS edge_annotation_name,\n").append(indent).
        append(TABSTOP);
      sb.append("edge_anno.\"val\" AS edge_annotation_value,\n");
      sb.append(indent).append(TABSTOP);
    }

    // corpus.path_name
    sb.append("corpus.path_name AS path");
    
    if (includeDocumentNameInAnnotateQuery) {
      sb.append(",\n");
      indent(sb, indent +TABSTOP);
      sb.append("corpus.path_name[1] AS document_name");
    }
    return sb.toString();
  }

  private void addSelectClauseAttribute(List<String> fields,
    String table, String column)
  {
    TableAccessStrategy tas = tables(null);
    fields.add(tas.aliasedColumn(table, column) + " AS "
      + factsTas.columnName(table, column));
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

    HashSet<String> conditions = new HashSet<String>();

    if (tableLayout == SchemeType.ANNO_POOL)
    {

      // join with node annotations
      sb.append(" AND\n");
      indent(sb, indent + TABSTOP);
      sb.append(tables.aliasedColumn(NODE_ANNOTATION_TABLE, "anno_ref"));
      sb.append(" = node_anno.id");
      sb.append(" AND\n");
      indent(sb, indent + TABSTOP);
      sb.append(" node_anno.\"type\" = 'node'");
      sb.append(" AND\n");
      // join with edge annotations
      indent(sb, indent + TABSTOP);
      sb.append(tables.aliasedColumn(EDGE_ANNOTATION_TABLE, "anno_ref"));
      sb.append(" = edge_anno.id AND\n");
      indent(sb, indent + TABSTOP);
      sb.append(" edge_anno.\"type\" = 'edge'");
      sb.append(" AND\n");
      
      // restrict toplevel corpus
      indent(sb, indent + TABSTOP);
      sb.append("node_anno.toplevel_corpus IN (");
      sb.append(StringUtils.join(corpusList, ", "));
      sb.append(") AND \n");
      indent(sb, indent + TABSTOP);
      sb.append("edge_anno.toplevel_corpus IN (");
      sb.append(StringUtils.join(corpusList, ", "));
      sb.append(")\n");
    }

    conditions.add(sb.toString());

    return conditions;
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
    SolutionKey<?> key = createSolutionKey();
    int size = alternative.size();
    List<String> keyColumns = key.getKeyColumns(size);
    StringBuilder sb = new StringBuilder();
    for (String keyColumn : keyColumns) {
      sb.append(keyColumn);
      sb.append(", ");
    }
    String preColumn = tables(null).aliasedColumn(RANK_TABLE, "pre");
    sb.append(preColumn);
    String orderByClause = sb.toString();
    return orderByClause;
  }

  @Override
  public String fromClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    StringBuffer sb = new StringBuffer();

    indent(sb, indent);
    sb.append("(\n");
    indent(sb, indent);
    int indentBy = indent.length() / 2 + 2;
    sb.append(innerQuerySqlGenerator.toSql(queryData, indentBy));
    indent(sb, indent + TABSTOP);
    sb.append(") AS solutions,\n");

    indent(sb, indent + TABSTOP);
    // really ugly
    sb.append(
      tableJoinsInFromClauseSqlGenerator.fromClauseForNode(null, true));
    sb.append(",\n");

    indent(sb, indent + TABSTOP);
    sb.append(TableAccessStrategy.CORPUS_TABLE);

    if (tableLayout == SchemeType.ANNO_POOL)
    {
      sb.append(", \n");
      indent(sb, indent + TABSTOP);
      sb.append(TableAccessStrategy.ANNOTATION_POOL_TABLE);
      sb.append(" AS node_anno, \n");
      indent(sb, indent + TABSTOP);
      sb.append(TableAccessStrategy.ANNOTATION_POOL_TABLE);
      sb.append(" AS edge_anno");
    }

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

  public TableAccessStrategy getFactsTas()
  {
    return factsTas;
  }


  public String getTableLayout()
  {
    return tableLayout.name().toLowerCase();
  }
  

  public void setTableLayout(String tableLayout)
  {
    this.tableLayout = SchemeType.valueOf(tableLayout.toUpperCase());
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

}
