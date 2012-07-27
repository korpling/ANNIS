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

import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.dao.DataAccessException;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class GraphSqlGenerator<T> extends AbstractSqlGenerator<T>
  implements FromClauseSqlGenerator<QueryData>,
  SelectClauseSqlGenerator<QueryData>, OrderByClauseSqlGenerator<QueryData>,
  WhereClauseSqlGenerator<QueryData>
{

  @Override
  public String toSql(QueryData queryData, String indent)
  {
    StringBuffer sb = new StringBuffer();
    sb.append(indent);
    sb.append(createSqlForAlternative(queryData, null, indent));
    appendOrderByClause(sb, queryData, null, indent);
    appendLimitOffsetClause(sb, queryData, null, indent);
    return sb.toString();
  }

  @Override
  public String fromClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("\n").append(TABSTOP);
    sb.append("node_ids, corpus, matching_nodes \n");
    sb.append(
      "LEFT OUTER JOIN annotation_pool as node_anno ON(matching_nodes.node_anno_ref = node_anno.id)\n");
    sb.append(
      "LEFT OUTER JOIN annotation_pool as edge_anno ON(matching_nodes.edge_anno_ref = edge_anno.id)\n");

    return sb.toString();
  }

  @Override
  public String selectClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    StringBuilder sb = new StringBuilder();
    List<SaltURIs> listOfSaltURIs = queryData.getExtensions(SaltURIs.class);
    // only work with the first element
    Validate.isTrue(!listOfSaltURIs.isEmpty());
    SaltURIs saltURIs = listOfSaltURIs.get(0);

    sb.append("ARRAY[");
    for (int i = 1; i <= saltURIs.size(); i++)
    {
      sb.append("node_ids.id").append(i);

      if (i < saltURIs.size())
      {
        sb.append(", ");
      }
    }

    sb.append("] AS key,\n").append(TABSTOP);

    ArrayList<String> fields = new ArrayList<String>();

    fields.add("0 AS matchstart");
    fields.add("1 AS n");

    fields.add("matching_nodes.id AS id");
    fields.add("matching_nodes.text_ref AS text_ref");
    fields.add("matching_nodes.corpus_ref AS corpus_ref");
    fields.add("matching_nodes.toplevel_corpus AS toplevel_corpus");
    fields.add("matching_nodes.node_namespace AS node_namespace");
    fields.add("matching_nodes.node_name AS node_name");
    fields.add("matching_nodes.left AS left");
    fields.add("matching_nodes.right AS right");
    fields.add("matching_nodes.token_index AS token_index");
    fields.add("matching_nodes.is_token AS is_token");
    fields.add("matching_nodes.continuous AS continuous");
    fields.add("matching_nodes.span AS span");
    fields.add("matching_nodes.left_token AS left_token");
    fields.add("matching_nodes.right_token AS right_token");
    fields.add("matching_nodes.seg_name AS seg_name");
    fields.add("matching_nodes.seg_left AS seg_left");
    fields.add("matching_nodes.seg_right AS seg_right");
    fields.add("matching_nodes.pre AS pre");
    fields.add("matching_nodes.post AS post");
    fields.add("matching_nodes.parent AS parent");
    fields.add("matching_nodes.root AS root");
    fields.add("matching_nodes.level AS level");
    fields.add("matching_nodes.component_id AS component_id");
    fields.add("matching_nodes.edge_type AS edge_type");
    fields.add("matching_nodes.edge_name AS edge_name");
    fields.add("matching_nodes.edge_namespace AS edge_namespace");
    fields.add("node_anno.namespace AS node_annotation_namespace");
    fields.add("node_anno.name AS node_annotation_name");
    fields.add("node_anno.val AS node_annotation_value");
    fields.add("edge_anno.namespace AS edge_annotation_namespace");
    fields.add("edge_anno.name AS edge_annotation_name");
    fields.add("edge_anno.val AS edge_annotation_value");
    fields.add("corpus.path_name AS path");

    appendField(sb, fields);
    return sb.toString();
  }

  @Override
  public String orderByClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    return "matching_nodes.pre\n";
  }

  @Override
  public T extractData(ResultSet rs) throws SQLException, DataAccessException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  private void appendField(StringBuilder sb, ArrayList<String> fields)
  {
    sb.append(StringUtils.join(fields, ",\n" + TABSTOP));
  }

  @Override
  public Set<String> whereConditions(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    Set<String> conditions = new HashSet<String>();

    conditions.add("matching_nodes.corpus_ref = corpus.id");
    return conditions;
  }
}
