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
import java.util.ArrayList;
import java.util.List;

/**
 * Generates a WITH clause sql statement for a list of salt ids.
 *
 * Salt ids are simple URI and are defined like this: salt:/corp1/corp2/doc1 *
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class GraphWithClauseGenerator implements
  WithClauseSqlGenerator<QueryData>
{

  @Override
  public List<String> withClauses(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    List<String> withClauseList = new ArrayList<String>();
    StringBuilder sb = new StringBuilder();

    sb.append("node_ids AS (\n");

    sb.append(
      "SELECT min(facts.token_index) as min, max(facts.token_index) as max, corpus_id as id\n");
    sb.append("FROM corpus, facts\n");

    /**
     * WHERE Clause for WITH clause, TODO: read this path from query object
     */
    sb.append("WHERE path_name ='{11299, pcc}'\n");

    sb.append("AND facts.corpus_ref = corpus.id\n");

    /**
     * TODO: read this token names from query object
     */
    sb.append("AND (facts.node_name = 'tok_14'\n");
    sb.append("OR facts.node_name = 'const_5')\n");

    /**
     * probably not needed
     */
    sb.append("GROUP BY corpus.id\n), ");

    sb.append("matching_nodes AS (\n");
    sb.append(
      "SELECT DISTINCT facts.id, facts.node_name, facts.token_index, facts.span, facts.node_anno_ref, facts.edge_anno_ref, facts.pre\n");

    sb.append("FROM node_ids, facts\n");

    sb.append("WHERE ");

    /**
     * TODO island policy
     */
    sb.append(
      "node_ids.min - 5 <= facts.token_index AND facts.token_index <= node_ids.max + 5\n");
    sb.append("AND corpus_ref = node_ids.id\n");


    withClauseList.add(sb.toString());
    return withClauseList;
  }
}
