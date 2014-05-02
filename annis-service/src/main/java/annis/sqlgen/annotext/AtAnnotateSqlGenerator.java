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
package annis.sqlgen.annotext;

import static annis.sqlgen.TableAccessStrategy.COMPONENT_TABLE;
import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
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
 *  @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class AtAnnotateSqlGenerator<T> extends AnnotateSqlGenerator<T>
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
      AbstractFromClauseGenerator.tableAliasDefinition(tas, 
        null, NODE_TABLE, 1, queryData.getCorpusList()));
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
    
    fields.add("(splitanno(node_qannotext))[1] as node_annotation_namespace");
    fields.add("(splitanno(node_qannotext))[2] as node_annotation_name");
    fields.add("(splitanno(node_qannotext))[3] as node_annotation_value");
    
    fields.add("(splitanno(edge_qannotext))[1] as edge_annotation_namespace");
    fields.add("(splitanno(edge_qannotext))[2] as edge_annotation_name");
    fields.add("(splitanno(edge_qannotext))[3] as edge_annotation_value");

    
    return fields;
  }

  @Override
  public String getDocumentQuery(String toplevelCorpusName, String documentName)
  {
    TableAccessStrategy tas = createTableAccessStrategy();
    List<String> fields = getSelectFields();
    
    //TODO: implement document query for annotext
    
    return "TODO";
  }
}
