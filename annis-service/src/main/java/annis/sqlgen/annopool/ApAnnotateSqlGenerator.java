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
package annis.sqlgen.annopool;

import static annis.sqlgen.TableAccessStrategy.COMPONENT_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;
import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import annis.sqlgen.AbstractFromClauseGenerator;
import annis.sqlgen.AnnotateSqlGenerator;
import annis.sqlgen.extensions.LimitOffsetQueryData;
import annis.sqlgen.TableAccessStrategy;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

import static annis.sqlgen.SqlConstraints.sqlString;

/**
 *
 *  @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class ApAnnotateSqlGenerator<T> extends AnnotateSqlGenerator<T>
{

  @Override
  public String fromClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    TableAccessStrategy tas = tables(null);
    List<Long> corpusList = queryData.getCorpusList();
    StringBuilder sb = new StringBuilder();
    
    sb.append(indent).append("solutions,\n");

    sb.append(indent).append(TABSTOP);
    sb.append(
      AbstractFromClauseGenerator.tableAliasDefinition(tas.getTableAliases(), null, NODE_TABLE, 1));;
    sb.append("\n");
    sb.append(indent).append(TABSTOP);
    sb.append("LEFT OUTER JOIN annotation_pool AS node_anno ON  (")
      .append(tas.aliasedColumn(NODE_TABLE, "node_anno_ref")).append(
      " = node_anno.id AND ")
      .append(tas.aliasedColumn(NODE_TABLE, "toplevel_corpus"))
      .append(" = node_anno.toplevel_corpus AND node_anno.toplevel_corpus IN (")
      .append(StringUtils.join(corpusList, ", "))
      .append("))");
    
    sb.append("\n");
    sb.append(indent).append(TABSTOP);
    sb.append(
      "LEFT OUTER JOIN annotation_pool AS edge_anno ON (")
      .append(tas.aliasedColumn(RANK_TABLE, "edge_anno_ref"))
      .append(" = edge_anno.id AND ")
      .append(tas.aliasedColumn(RANK_TABLE, "toplevel_corpus"))
      .append(" = edge_anno.toplevel_corpus AND "
      + "edge_anno.toplevel_corpus IN (")
      .append(StringUtils.join(corpusList, ", "))
      .append("))");

    sb.append(",\n");

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
    List<String> fields = new ArrayList<String>();
    
    addSelectClauseAttribute(fields, NODE_TABLE, "id");
    addSelectClauseAttribute(fields, NODE_TABLE, "text_ref");
    addSelectClauseAttribute(fields, NODE_TABLE, "corpus_ref");
    addSelectClauseAttribute(fields, NODE_TABLE, "toplevel_corpus");
    addSelectClauseAttribute(fields, NODE_TABLE, "namespace");
    addSelectClauseAttribute(fields, NODE_TABLE, "name");
    addSelectClauseAttribute(fields, NODE_TABLE, "left");
    addSelectClauseAttribute(fields, NODE_TABLE, "right");
    addSelectClauseAttribute(fields, NODE_TABLE, "token_index");
    if (isIncludeIsTokenColumn())
    {
      addSelectClauseAttribute(fields, NODE_TABLE, "is_token");
    }
    addSelectClauseAttribute(fields, NODE_TABLE, "continuous");
    addSelectClauseAttribute(fields, NODE_TABLE, "span");
    addSelectClauseAttribute(fields, NODE_TABLE, "left_token");
    addSelectClauseAttribute(fields, NODE_TABLE, "right_token");
    addSelectClauseAttribute(fields, NODE_TABLE, "seg_name");
    addSelectClauseAttribute(fields, NODE_TABLE, "seg_index");
    addSelectClauseAttribute(fields, RANK_TABLE, "pre");
    addSelectClauseAttribute(fields, RANK_TABLE, "post");
    addSelectClauseAttribute(fields, RANK_TABLE, "parent");
    addSelectClauseAttribute(fields, RANK_TABLE, "root");
    addSelectClauseAttribute(fields, RANK_TABLE, "level");
    addSelectClauseAttribute(fields, COMPONENT_TABLE, "id");
    addSelectClauseAttribute(fields, COMPONENT_TABLE, "type");
    addSelectClauseAttribute(fields, COMPONENT_TABLE, "name");
    addSelectClauseAttribute(fields, COMPONENT_TABLE, "namespace");

    fields.add("node_anno.\"namespace\" AS node_annotation_namespace");
    fields.add("node_anno.\"name\" AS node_annotation_name");
    fields.add("node_anno.\"val\" AS node_annotation_value");
    fields.add("edge_anno.\"namespace\" AS edge_annotation_namespace");
    fields.add("edge_anno.\"name\" AS edge_annotation_name");
    fields.add("edge_anno.\"val\" AS edge_annotation_value");
    
    return fields;
  }

  @Override
  public String getTextQuery(long textID)
  {
    TableAccessStrategy tas = createTableAccessStrategy();    
    List<String> fields = getSelectFields();
    
    String template = "SELECT DISTINCT \n"
      + "\tARRAY[-1::bigint] AS key, ARRAY[''::varchar] AS key_names, 0 as matchstart, " 
      +  StringUtils.join(fields, ", ") +", "
      + "c.path_name as path, c.path_name[1] as document_name,"
      + "node_anno.namespace AS node_annotation_namespace, "
      + "node_anno.\"name\" AS node_annotation_name, "
      + "node_anno.val AS node_annotation_value,\n"
      + "edge_anno.namespace AS edge_annotation_namespace, "
      + "edge_anno.\"name\" AS edge_annotation_name, "
      + "edge_anno.val AS edge_annotation_value\n"
      + "FROM\n"
      + "\t" + AbstractFromClauseGenerator.tableAliasDefinition(tas.getTableAliases(), null, NODE_TABLE, 1) + "\n"
      + "\tLEFT OUTER JOIN annotation_pool AS node_anno ON (" + tas.aliasedColumn(NODE_TABLE, "node_anno_ref") 
        + " = node_anno.id AND " + tas.aliasedColumn(NODE_TABLE, "toplevel_corpus") + " = node_anno.toplevel_corpus)\n"
      + "\tLEFT OUTER JOIN annotation_pool AS edge_anno ON (" + tas.aliasedColumn(RANK_TABLE, "edge_anno_ref")
        + " = edge_anno.id AND " + tas.aliasedColumn(RANK_TABLE, "toplevel_corpus") + " = edge_anno.toplevel_corpus),\n"
      + "\tcorpus AS c\n"
      + "WHERE\n"
      + "\t" + tas.aliasedColumn(NODE_TABLE, "text_ref") + " = :text_id AND " + tas.aliasedColumn(NODE_TABLE, "corpus_ref") + " = c.id\n"
      + "ORDER BY " + tas.aliasedColumn(RANK_TABLE, "pre");
    String sql = template.replace(":text_id", String.valueOf(textID));
    return sql;
  }

  @Override
  public String getDocumentQuery(String toplevelCorpusName, String documentName)
  {
    TableAccessStrategy tas = createTableAccessStrategy();
    List<String> fields = getSelectFields();
    
    String template = "SELECT DISTINCT \n"
      + "\tARRAY[-1::bigint] AS key, ARRAY[''::varchar] AS key_names, 0 as matchstart, "
      +  StringUtils.join(fields, ", ") +", "
      + "c.path_name as path, c.path_name[1] as document_name, "
      + "node_anno.namespace AS node_annotation_namespace, "
      + "node_anno.\"name\" AS node_annotation_name, "
      + "node_anno.val AS node_annotation_value,\n"
      + "edge_anno.namespace AS edge_annotation_namespace, "
      + "edge_anno.\"name\" AS edge_annotation_name, "
      + "edge_anno.val AS edge_annotation_value\n"
      + "FROM\n"
      + "\t" + AbstractFromClauseGenerator.tableAliasDefinition(tas.getTableAliases(), null, NODE_TABLE, 1) + "\n"
      + "\tLEFT OUTER JOIN annotation_pool AS node_anno ON (" + tas.aliasedColumn(NODE_TABLE, "node_anno_ref") 
        + " = node_anno.id AND " + tas.aliasedColumn(NODE_TABLE, "toplevel_corpus") + " = node_anno.toplevel_corpus)\n"
      + "\tLEFT OUTER JOIN annotation_pool AS edge_anno ON (" + tas.aliasedColumn(RANK_TABLE, "edge_anno_ref")
        + " = edge_anno.id AND " + tas.aliasedColumn(RANK_TABLE, "toplevel_corpus") + " = edge_anno.toplevel_corpus),\n"
      + "\tcorpus as c, corpus as toplevel\n"
      + "WHERE\n"
      + "\ttoplevel.name = :toplevel_name AND c.name = :document_name AND " + tas.aliasedColumn(NODE_TABLE, "corpus_ref") + " = c.id\n"
      + "\tAND toplevel.top_level IS TRUE\n"
      + "\tAND c.pre >= toplevel.pre AND c.post <= toplevel.post\n"
      + "ORDER BY "  + tas.aliasedColumn(RANK_TABLE, "pre");
    String sql = template.replace(":toplevel_name", sqlString(toplevelCorpusName))
      .replace(":document_name", sqlString(documentName));
    return sql;
  }
}
