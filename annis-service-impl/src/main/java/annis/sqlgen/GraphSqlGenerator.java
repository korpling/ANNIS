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
import java.util.List;
import org.springframework.dao.DataAccessException;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class GraphSqlGenerator<T> extends AbstractUnionSqlGenerator<QueryData>
  implements FromClauseSqlGenerator<QueryData>,
  SelectClauseSqlGenerator<QueryData>, OrderByClauseSqlGenerator<QueryData>
{

  @Override
  public QueryData extractData(ResultSet rs) throws SQLException,
    DataAccessException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public String fromClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    StringBuilder sb = new StringBuilder();

    sb.append("FROM matching_nodes \n");
    sb.append(
      "LEFT OUTER JOIN annotation_pool as anno_node ON(matching_nodes.node_anno_ref = anno_node.id)\n");
    sb.append(
      "LEFT OUTER JOIN annotation_pool as anno_edge ON(matching_nodes.edge_anno_ref = anno_edge.id)\n");

    return sb.toString();
  }

  @Override
  public String selectClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    return "SELECT ARRAY[matching_nodes.id] AS key, * \n";
  }

  @Override
  public String orderByClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    return "ORDER BY token_index, matching_nodes.pre";
  }
}
