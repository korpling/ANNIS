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
package annis.sqlgen.fullfacts;


import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import annis.sqlgen.AnnotateSqlGenerator;
import annis.sqlgen.LimitOffsetQueryData;
import annis.sqlgen.SolutionKey;
import annis.sqlgen.TableAccessStrategy;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import static annis.sqlgen.TableAccessStrategy.COMPONENT_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.SqlConstraints.sqlString;

/**
 *
 * @author thomas
 */
public class FfAnnotateSqlGenerator<T> extends AnnotateSqlGenerator<T>
{

  @Override
  public String getTextQuery(long textID)
  {
    String template = "SELECT DISTINCT \n"
      + "\tARRAY[-1::bigint] AS key, ARRAY[''::varchar] AS key_names, 0 as matchstart, facts.*, c.path_name as path, c.path_name[1] as document_name\n"
      + "FROM\n"
      + "\tfacts AS facts, corpus as c\n" + "WHERE\n"
      + "\tfacts.text_ref = :text_id AND facts.corpus_ref = c.id\n"
      + "ORDER BY facts.pre";
    String sql = template.replace(":text_id", String.valueOf(textID));
    return sql;
  }

  @Override
  public String getDocumentQuery(String toplevelCorpusName, String documentName)
  {
    String template = "SELECT DISTINCT \n"
      + "\tARRAY[-1::bigint] AS key, ARRAY[''::varchar] AS key_names, 0 as matchstart, facts.*, c.path_name as path, c.path_name[1] as document_name\n"
      + "FROM\n"
      + "\tfacts AS facts, corpus as c, corpus as toplevel\n" + "WHERE\n"
      + "\ttoplevel.name = :toplevel_name AND c.name = :document_name AND facts.corpus_ref = c.id\n"
      + "\tAND c.pre >= toplevel.pre AND c.post <= toplevel.post\n"
      + "ORDER BY facts.pre";
    String sql = template.replace(":toplevel_name", sqlString(toplevelCorpusName))
      .replace(":document_name", sqlString(documentName));
    return sql;
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
    
    List<LimitOffsetQueryData> extension =
      queryData.getExtensions(LimitOffsetQueryData.class);
    Validate.isTrue(extension.size() > 0);
    sb.append(innerIndent).append(extension.get(0).getOffset()).append(" AS \"matchstart\",\n");
    sb.append(indent).append(TABSTOP + "solutions.n,\n");

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
    if (isIncludeIsTokenColumn())
    {
      addSelectClauseAttribute(fields, NODE_TABLE, "is_token");
    }
    addSelectClauseAttribute(fields, NODE_TABLE, "continuous");
    addSelectClauseAttribute(fields, NODE_TABLE, "span");
    addSelectClauseAttribute(fields, NODE_TABLE, "left_token");
    addSelectClauseAttribute(fields, NODE_TABLE, "right_token");
    addSelectClauseAttribute(fields, NODE_TABLE, "seg_name");
    addSelectClauseAttribute(fields, NODE_TABLE, "seg_left");
    addSelectClauseAttribute(fields, NODE_TABLE, "seg_right");
    addSelectClauseAttribute(fields, RANK_TABLE, "pre");
    addSelectClauseAttribute(fields, RANK_TABLE, "post");
    addSelectClauseAttribute(fields, RANK_TABLE, "parent");
    addSelectClauseAttribute(fields, RANK_TABLE, "root");
    addSelectClauseAttribute(fields, RANK_TABLE, "level");
    addSelectClauseAttribute(fields, COMPONENT_TABLE, "id");
    addSelectClauseAttribute(fields, COMPONENT_TABLE, "type");
    addSelectClauseAttribute(fields, COMPONENT_TABLE, "name");
    addSelectClauseAttribute(fields, COMPONENT_TABLE, "namespace");
    addSelectClauseAttribute(fields, NODE_ANNOTATION_TABLE, "namespace");
    addSelectClauseAttribute(fields, NODE_ANNOTATION_TABLE, "name");
    addSelectClauseAttribute(fields, NODE_ANNOTATION_TABLE, "value");
    addSelectClauseAttribute(fields, EDGE_ANNOTATION_TABLE, "namespace");
    addSelectClauseAttribute(fields, EDGE_ANNOTATION_TABLE, "name");
    addSelectClauseAttribute(fields, EDGE_ANNOTATION_TABLE, "value");

    sb.append(innerIndent).append(StringUtils.join(fields, ",\n" + indent + TABSTOP));
    sb.append(",\n");


    // corpus.path_name
    sb.append(innerIndent).append("corpus.path_name AS path");

    if (isIncludeDocumentNameInAnnotateQuery())
    {
      sb.append(",\n");
      sb.append(innerIndent).append("corpus.path_name[1] AS document_name");
    }
    return sb.toString();
  }

  @Override
  public String fromClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    StringBuffer sb = new StringBuffer();

    sb.append(indent).append("solutions,\n");

    // really ugly
    sb.append(indent).append(TABSTOP).append(
      getTableJoinsInFromClauseSqlGenerator().fromClauseForNode(null, true));
    sb.append(",\n");
    sb.append(indent).append(TABSTOP).append(TableAccessStrategy.CORPUS_TABLE);

    return sb.toString();
  }
}
