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

import annis.service.objects.SaltURIGroupSet;
import annis.CommonHelper;
import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import annis.service.objects.SaltURIGroup;
import java.net.URI;
import java.util.List;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.StringUtils;

import static annis.sqlgen.AbstractSqlGenerator.TABSTOP;
import static annis.sqlgen.TableAccessStrategy.CORPUS_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;

import static annis.sqlgen.SqlConstraints.sqlString;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

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
  private String singleMatchClause(int matchNumber, List<URI> saltURIs, 
    TableAccessStrategy tas, AnnotateQueryData annotateQueryData, 
    List<Long> corpusList, int numOfNodes,
    String indent)
  {
    String indent2 = indent + TABSTOP;
    StringBuilder sb = new StringBuilder();
    
    // SELECT
    sb.append(indent).append("SELECT\n");
    sb.append(indent2).append(matchNumber).append(" AS n, \n").append(indent2);

    for (int i = 1; i <= numOfNodes; i++)
    {
      // factsN.id AS idN
      sb.append(tas.tableName(NODE_TABLE)).append(i).append(".")
        .append(tas.columnName(NODE_TABLE, "id")).append(" AS ")
        .append("id").append(i).append(", ");

      sb.append(tas.tableName(NODE_TABLE)).append(i).append(".")
        .append(tas.columnName(NODE_TABLE, "text_ref")).append(" AS ")
        .append("text").append(i).append(", ");

      sb.append(tas.tableName(NODE_TABLE)).append(i).append(".")
        .append(tas.columnName(NODE_TABLE, "left_token"));
        
      if(annotateQueryData.getSegmentationLayer() == null)
      {
        sb.append(" - ").append(annotateQueryData.getLeft());
      }
      sb.append(" AS ").append("min").append(i).append(", ");

      sb.append(tas.tableName(NODE_TABLE)).append(i).append(".")
        .append(tas.columnName(NODE_TABLE, "right_token"));
      if(annotateQueryData.getSegmentationLayer() == null)
      {
        sb.append(" + ").append(annotateQueryData.getRight());
      }
      sb.append(" AS ").append("max").append(i).append(", ");

      sb.append(tas.tableName(NODE_TABLE)).append(i).append(".")
        .append(tas.columnName(NODE_TABLE, "corpus_ref"))
        .append(" AS ").append("corpus").append(i).append(", ");

      sb.append(tas.tableName(NODE_TABLE)).append(i).append(".")
        .append(tas.columnName(NODE_TABLE, "node_name"))
        .append(" AS ").append("name").append(i);

      if (i == numOfNodes)
      {
        sb.append("\n");
      }
      else
      {
        sb.append(", \n").append(indent2);
      }
    }

    // FROM
    sb.append(indent).append("FROM\n");
    for (int i = 1; i <= numOfNodes; i++)
    {
      sb.append(indent2)
        .append(tas.tableName(NODE_TABLE)).append(" AS ")
        .append(tas.tableName(NODE_TABLE)).append(i).append(", ")
        .append(tas.tableName(CORPUS_TABLE)).append(" AS ")
        .append(tas.tableName(CORPUS_TABLE)).append(i);

      if (i == numOfNodes)
      {
        sb.append("\n");
      }
      else
      {
        sb.append(",\n").append(indent2);
      }

    }

    // WHERE
    sb.append(indent).append("WHERE\n");
    for (int i = 1; i <= numOfNodes; i++)
    {
      URI uri = saltURIs.get(i - 1);

      // check for corpus/document by it's path
      sb.append(indent2)
        .append(tas.tableName(CORPUS_TABLE)).append(i).append(".path_name = ")
        .append(generatePathName(uri)).append(" AND\n");

      // join the found corpus/document to the facts table
      sb.append(indent2)
        .append(tas.tableName(NODE_TABLE)).append(i).append(".corpus_ref = ")
        .append(tas.tableName(CORPUS_TABLE)).append(i).append(".id AND\n");

      // filter the node with the right name
      sb.append(indent2)
        .append(tas.tableName(NODE_TABLE)).append(i).append(".node_name = ")
        .append("'").append(uri.getFragment()).append("'").append(" AND\n");

      // use the toplevel partioning
      sb.append(indent2)
        .append(tas.tableName(NODE_TABLE)).append(i).append(".toplevel_corpus IN ( ")
        .append(StringUtils.join(corpusList, ",")).append(") ");

      if (i < numOfNodes)
      {
        sb.append("AND\n");
      }
      else
      {
        sb.append("\n");
      }
    }

    // LIMIT to one row
    sb.append(indent).append("LIMIT 1\n");
    
    return sb.toString();
  }

  @Override
  protected String getMatchesWithClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    TableAccessStrategy tas = createTableAccessStrategy();

    StringBuilder sb = new StringBuilder();

    String indent2 = indent + TABSTOP;

    List<AnnotateQueryData> extensions =
      queryData.getExtensions(AnnotateQueryData.class);
    AnnotateQueryData annotateQueryData = extensions.isEmpty()
      ? new AnnotateQueryData(5, 5) : extensions.get(0);

    List<SaltURIGroupSet> listOfSaltURIs = queryData.getExtensions(SaltURIGroupSet.class);
    // only work with the first element
    Validate.isTrue(!listOfSaltURIs.isEmpty());

    SaltURIGroupSet saltURIs = listOfSaltURIs.get(0);

    sb.append(indent).append("matches AS\n");
    sb.append(indent).append("(\n");

    LinkedList<String> clauses = new LinkedList<String>();
   
    for(Map.Entry<Integer, SaltURIGroup> e : saltURIs.getGroups().entrySet())
    {
      clauses.add(
        indent2 + "(\n"+
        singleMatchClause(e.getKey(), e.getValue().getUris(), tas, annotateQueryData,
          queryData.getCorpusList(),alternative.size(), indent2 + TABSTOP)
        + indent2 + ")\n"
      );
    }
    
    String seperator =indent2 + "UNION ALL\n";
    sb.append(StringUtils.join(clauses, seperator));
    
    // end WITH inner select
    sb.append(indent).append(")");

    return sb.toString();
  }

  private String generatePathName(URI uri)
  {
    StringBuilder sb = new StringBuilder();
    
    List<String> path = CommonHelper.getCorpusPath(uri);
    Collections.reverse(path);

    sb.append("{");
    sb.append(StringUtils.join(path, ", "));
    sb.append("}");
    
    return  sqlString(sb.toString());
  }
}
