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
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 * Generates a WITH clause sql statement for a list of salt ids.
 *
 * Salt ids are simple URI and are defined like this: salt:/corp1/corp2/doc1 *
 *
 * TODO support table access strategy
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class GraphWithClauseGenerator implements
  WithClauseSqlGenerator<QueryData>
{

  private Logger log = Logger.getLogger(GraphWithClauseGenerator.class);

  @Override
  public List<String> withClauses(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    List<String> withClauseList = new ArrayList<String>();
    StringBuilder sb = new StringBuilder();

    sb.append("node_ids AS (\n");

    sb.append(
      "SELECT min(facts.left_token) as min, max(facts.right_token) as max, corpus.id as id\n");
    sb.append("FROM corpus, facts\n");

    getCorpusPath(sb, queryData);
    sb.append("AND facts.corpus_ref = corpus.id\n");
    getTokenNames(sb, queryData);

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
    sb.append("ORDER BY facts.token_index)");

    withClauseList.add(sb.toString());
    return withClauseList;
  }

  private String getCorpusPath(StringBuilder sb, QueryData queryData)
  {
    List<SaltURIs> listOfSaltURIs = queryData.getExtensions(SaltURIs.class);

    // only work with the first element
    Validate.isTrue(!listOfSaltURIs.isEmpty());
    SaltURIs saltURIs = listOfSaltURIs.get(0);
    sb.append("WHERE ");


    for (int i = 0; i < saltURIs.size(); i++)
    {
      URI uri = saltURIs.get(i);
      String[] path = uri.getPath().split("/");

      // the path is reversed in relAnnis saved
      sb.append("path_name = '{");
      for (int j = path.length - 1; j > 0; j--)
      {
        sb.append(path[j]);
        sb.append(", ");
      }

      sb.append(uri.getHost());
      sb.append("}'\n");

      // concate conditions
      if (i < saltURIs.size() - 1)
      {
        sb.append("OR\n");
      }
    }

    return sb.toString();
  }

  private StringBuilder getTokenNames(StringBuilder sb, QueryData queryData)
  {
    List<SaltURIs> listOfSaltURIs = queryData.getExtensions(SaltURIs.class);

    // only work with the first element
    Validate.isTrue(!listOfSaltURIs.isEmpty());
    SaltURIs saltURIs = listOfSaltURIs.get(0);

    sb.append("AND(\n");
    for (int i = 0; i < saltURIs.size(); i++)
    {
      URI uri = saltURIs.get(i);
      sb.append("facts.node_name='").append(uri.getFragment()).append("'");
      sb.append("\n");

      // concate conditions
      if (i < saltURIs.size() -1)
      {
        sb.append("OR\n");
      }
    }

    sb.append(")\n");

    return sb;
  }
}
