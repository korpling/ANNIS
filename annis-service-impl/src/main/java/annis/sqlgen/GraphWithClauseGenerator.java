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
import java.util.List;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.StringUtils;

import static annis.sqlgen.AbstractSqlGenerator.TABSTOP;
import static annis.sqlgen.TableAccessStrategy.CORPUS_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;

import static annis.sqlgen.SqlConstraints.sqlString;

/**
 * Generates a WITH clause sql statement for a list of salt ids.
 *
 * Salt ids are simple URI and are defined like this:
 *
 * <p>{@code salt:/corp1/corp2/doc1}</p>.
 *
 * The leading / of the URI is a must, // would cause an error, because
 * authorities are currently not supported.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class GraphWithClauseGenerator extends CommonAnnotateWithClauseGenerator
{

  @Override
  protected String getMatchesWithClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    TableAccessStrategy tas = createTableAccessStrategy();

    StringBuilder sb = new StringBuilder();

    String indent2 = indent + TABSTOP;
    String indent3 = indent2 + TABSTOP;

    List<AnnotateQueryData> extensions =
      queryData.getExtensions(AnnotateQueryData.class);
    AnnotateQueryData annotateQueryData = extensions.isEmpty()
      ? new AnnotateQueryData(5, 5) : extensions.get(0);

    List<SaltURIs> listOfSaltURIs = queryData.getExtensions(SaltURIs.class);
    // only work with the first element
    Validate.isTrue(!listOfSaltURIs.isEmpty());

    SaltURIs saltURIs = listOfSaltURIs.get(0);

    sb.append(indent).append("matches AS\n");
    sb.append(indent).append("(\n");

    // SELECT
    sb.append(indent2).append("SELECT\n");
    sb.append(indent3).append("1 AS n, \n").append(indent3);

    for (int i = 1; i <= alternative.size(); i++)
    {
      // factsN.id AS idN
      sb.append(tas.tableName(NODE_TABLE)).append(i).append(".")
        .append(tas.columnName(NODE_TABLE, "id")).append(" AS ")
        .append("id").append(i).append(", ");

      sb.append(tas.tableName(NODE_TABLE)).append(i).append(".")
        .append(tas.columnName(NODE_TABLE, "text_ref")).append(" AS ")
        .append("text").append(i).append(", ");

      sb.append(tas.tableName(NODE_TABLE)).append(i).append(".")
        .append(tas.columnName(NODE_TABLE, "left_token"))
        .append(" - ").append(annotateQueryData.getLeft())
        .append(" AS ").append("min").append(i).append(", ");

      sb.append(tas.tableName(NODE_TABLE)).append(i).append(".")
        .append(tas.columnName(NODE_TABLE, "right_token"))
        .append(" + ").append(annotateQueryData.getRight())
        .append(" AS ").append("max").append(i).append(", ");

      sb.append(tas.tableName(NODE_TABLE)).append(i).append(".")
        .append(tas.columnName(NODE_TABLE, "corpus_ref"))
        .append(" AS ").append("corpus").append(i).append(", ");

      sb.append(tas.tableName(NODE_TABLE)).append(i).append(".")
        .append(tas.columnName(NODE_TABLE, "node_name"))
        .append(" AS ").append("name").append(i);

      if (i == alternative.size())
      {
        sb.append("\n");
      }
      else
      {
        sb.append(", \n").append(indent3);
      }
    }

    // FROM
    sb.append(indent2).append("FROM\n");
    for (int i = 1; i <= alternative.size(); i++)
    {
      sb.append(indent3)
        .append(tas.tableName(NODE_TABLE)).append(" AS ")
        .append("facts").append(i).append(", ")
        .append(tas.tableName(CORPUS_TABLE)).append(" AS ")
        .append("corpus").append(i);

      if (i == alternative.size())
      {
        sb.append("\n");
      }
      else
      {
        sb.append(",\n").append(indent3);
      }

    }

    // WHERE
    sb.append(indent2).append("WHERE\n");
    for (int i = 1; i <= alternative.size(); i++)
    {
      URI uri = saltURIs.get(i - 1);

      // check for corpus/document by it's path
      sb.append(indent3)
        .append("corpus").append(i).append(".path_name = ")
        .append(generatePathName(uri)).append(" AND\n");

      // join the found corpus/document to the facts table
      sb.append(indent3)
        .append("facts").append(i).append(".corpus_ref = ")
        .append("corpus").append(i).append(".id AND\n");

      // filter the node with the right name
      sb.append(indent3)
        .append("facts").append(i).append(".node_name = ")
        .append("'").append(uri.getFragment()).append("'").append(" AND\n");

      // use the toplevel partioning
      sb.append(indent3)
        .append("facts").append(i).append(".toplevel_corpus IN ( ")
        .append(StringUtils.join(queryData.getCorpusList(), ",")).append(") ");

      if (i < alternative.size())
      {
        sb.append("AND\n");
      }
      else
      {
        sb.append("\n");
      }
    }

    // LIMIT to one row
    sb.append(indent2).append("LIMIT 1\n");

    // end WITH inner select
    sb.append(indent).append(")");

    return sb.toString();
  }

  private String generatePathName(URI uri)
  {
    StringBuilder sb = new StringBuilder();
    String rawPath = StringUtils.strip(uri.getPath(), "/ \t");
    String[] path = rawPath.split("/");

    sb.append("{");
    for (int j = path.length - 1; j >= 0; j--)
    {
      sb.append(path[j]);

      if (j > 0)
      {
        sb.append(", ");
      }
    }

    sb.append("}");
    return  sqlString(sb.toString());
  }
}
