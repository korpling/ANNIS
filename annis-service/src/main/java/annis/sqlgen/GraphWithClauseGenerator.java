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

import annis.CommonHelper;
import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import annis.service.objects.Match;
import annis.service.objects.MatchGroup;
import static annis.sqlgen.AbstractSqlGenerator.TABSTOP;
import static annis.sqlgen.SqlConstraints.sqlString;
import static annis.sqlgen.TableAccessStrategy.CORPUS_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import annis.sqlgen.extensions.AnnotateQueryData;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Generates a WITH clause sql statement for a list of salt ids.
 *
 * Salt ids are simple URI and are defined like this:
 *
 * <p>{@code salt:/corp1/corp2/doc1#node}</p>.
 *
 * The leading / of the URI is a must, // would cause an error, because
 * authorities are currently not supported.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class GraphWithClauseGenerator extends CommonAnnotateWithClauseGenerator
{
  
  private static final Escaper ARRAY_ELEM_ESC = 
      Escapers.builder().addEscape(',', "\\,").build();
    
  private String selectForNode(
    TableAccessStrategy tas, AnnotateQueryData annotateQueryData,
    int match,
    int nodeNr, 
    String indent)
  {
    StringBuilder sb = new StringBuilder();

    
    sb.append(match).append(" AS n, ");
    sb.append(nodeNr).append(" AS nodeNr,\n").append(indent);
    
    sb.append(tas.tableName(NODE_TABLE)).append(nodeNr).append(".")
      .append(tas.columnName(NODE_TABLE, "id")).append(" AS ")
      .append("id, ");

    sb.append(tas.tableName(NODE_TABLE)).append(nodeNr).append(".")
      .append(tas.columnName(NODE_TABLE, "text_ref")).append(" AS ")
      .append("text, ");

    sb.append(tas.tableName(NODE_TABLE)).append(nodeNr).append(".")
      .append(tas.columnName(NODE_TABLE, "left_token"));

    if (annotateQueryData.getSegmentationLayer() == null)
    {
      sb.append(" - ").append(annotateQueryData.getLeft());
    }
    sb.append(" AS ").append("min, ");

    sb.append(tas.tableName(NODE_TABLE)).append(nodeNr).append(".")
      .append(tas.columnName(NODE_TABLE, "right_token"));
    if (annotateQueryData.getSegmentationLayer() == null)
    {
      sb.append(" + ").append(annotateQueryData.getRight());
    }
    sb.append(" AS ").append("max, ");

    sb.append(tas.tableName(NODE_TABLE)).append(nodeNr).append(".")
      .append(tas.columnName(NODE_TABLE, "corpus_ref"))
      .append(" AS ").append("corpus");

    return sb.toString();
  }
  
  private String fromForNode(
    TableAccessStrategy tas, String indent,
      int nodeNr, List<Long> corpusList)
  {
    String factsSQL = SelectedFactsFromClauseGenerator.selectedFactsSQL(corpusList, indent);
    StringBuilder sb = new StringBuilder();
    sb.append(indent)
        .append(factsSQL).append(" AS ")
        .append(tas.tableName(NODE_TABLE)).append(nodeNr).append(", ")
        .append(tas.tableName(CORPUS_TABLE)).append(" AS ")
        .append(tas.tableName(CORPUS_TABLE)).append(nodeNr);
    
    return sb.toString();
  }
  
  private String whereForNode(URI uri,
    TableAccessStrategy tas, List<Long> corpusList, String indent,
      int nodeNr)
  {
    StringBuilder sb = new StringBuilder();
    // check for corpus/document by it's path
      sb.append(indent)
        .append(tas.tableName(CORPUS_TABLE)).append(nodeNr).append(".path_name = ")
        .append(generatePathName(uri)).append(" AND\n");

      // join the found corpus/document to the facts table
      sb.append(indent)
        .append(tas.tableName(NODE_TABLE)).append(nodeNr).append(".corpus_ref = ")
        .append(tas.tableName(CORPUS_TABLE)).append(nodeNr).append(".id AND\n");

      // filter the node with the right name
      sb.append(indent)
        .append(tas.tableName(NODE_TABLE)).append(nodeNr).append(".salt_id = ")
        .append("'").append(generateNodeID(uri)).append("'").append(" AND\n");

      // use the toplevel partioning
      sb.append(indent)
        .append(tas.tableName(NODE_TABLE)).append(nodeNr).append(".toplevel_corpus IN ( ")
        .append(StringUtils.join(corpusList, ",")).append(") ");
      return sb.toString();
  }
  
  
  private String subselectForMatch(int match, int nodeNr, URI uri, 
    TableAccessStrategy tas, AnnotateQueryData annoQueryData, List<Long> corpusList, 
    String indent)
  {
    StringBuilder sb = new StringBuilder();
    
    sb.append(indent).append("SELECT ").append(
      selectForNode(tas, annoQueryData, match, nodeNr, indent+TABSTOP)).append("\n");
    sb.append(indent).append("FROM\n").append(fromForNode(tas, indent+TABSTOP, nodeNr, corpusList)).append("\n");
    sb.append(indent).append("WHERE\n").append(whereForNode(uri, tas, corpusList , indent+TABSTOP, nodeNr)).append("\n");
    sb.append(indent).append("LIMIT 1\n");
  
    return sb.toString();
  }
  
  

  @Override
  protected List<String> getMatchesWithClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    TableAccessStrategy tas = createTableAccessStrategy();
    
    List<AnnotateQueryData> extensions =
      queryData.getExtensions(AnnotateQueryData.class);
    AnnotateQueryData annotateQueryData = extensions.isEmpty()
      ? new AnnotateQueryData(5, 5) : extensions.get(0);
    List<MatchGroup> listOfSaltURIs = queryData.getExtensions(MatchGroup.class);
    // only work with the first element
    Validate.isTrue(!listOfSaltURIs.isEmpty());
    
    List<String> subselects = new LinkedList<>();
    
    
    String indent2 = indent + TABSTOP;
    
    MatchGroup groupSet = listOfSaltURIs.get(0);
    int matchNr = 1;
    for(Match match : groupSet.getMatches())
    {
      List<URI> uriList = match.getSaltIDs();
      int nodeNr = 1;
      for (URI uri : uriList)
      {
        String sub
          = indent2 + "(\n"
          + subselectForMatch(matchNr, nodeNr, uri, tas, annotateQueryData,
            queryData.getCorpusList(),
            indent2)
          + indent2 + ")";

        subselects.add(0, sub);
        nodeNr++;
      }
      matchNr++;
    }
    String result =
      indent + "matches AS\n" + indent + "(\n" 
      + Joiner.on("\n" + indent2 +"UNION ALL\n").join(subselects) 
      + "\n" + indent + ")";
    
    return Lists.newArrayList(result);
  }

  private String generatePathName(URI uri)
  {
    StringBuilder sb = new StringBuilder();
    
    List<String> path = CommonHelper.getCorpusPath(uri);
    Collections.reverse(path);

    List<String> escapedPath = new LinkedList<>();
    for (String p : path)
    {
      escapedPath.add(ARRAY_ELEM_ESC.escape(p));
    }

    sb.append("{");
    Joiner.on(", ").appendTo(sb, escapedPath);
    sb.append("}");
    
    return  sqlString(sb.toString());
  }
  
  private String generateNodeID(URI uri)
  { 
    return uri.getFragment();
  }
}
