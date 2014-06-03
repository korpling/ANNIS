/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.sqlgen;

import annis.sqlgen.extensions.LimitOffsetQueryData;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.dao.DataAccessException;

import annis.service.objects.Match;
import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import annis.service.internal.QueryServiceImpl;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

/**
 * Generates identifers for salt which are needed for the
 * {@link QueryServiceImpl#subgraph(java.lang.String, java.lang.String, java.lang.String)}
 *
 * @author Benjamin Weißenfels
 */
public class FindSqlGenerator extends AbstractUnionSqlGenerator<List<Match>>
  implements SelectClauseSqlGenerator<QueryData>,
  OrderByClauseSqlGenerator<QueryData>, RowMapper<Match>
{

  private static final Logger log = LoggerFactory.getLogger(
    FindSqlGenerator.class);

  // optimize DISTINCT operation in SELECT clause
  private boolean optimizeDistinct;

  private boolean sortSolutions;

  private boolean outputCorpusPath;

  private boolean outputToplevelCorpus;

  private CorpusPathExtractor corpusPathExtractor;

  private AnnotationConditionProvider annoCondition;

  @Override
  public String selectClause(QueryData queryData, List<QueryNode> alternative,
    String indent)
  {
    int maxWidth = queryData.getMaxWidth();
    Validate.isTrue(alternative.size() <= maxWidth,
      "BUG: nodes.size() > maxWidth");

    boolean needsDistinct = false || !optimizeDistinct;
    List<String> cols = new ArrayList<String>();
    int i = 0;

    for (QueryNode node : alternative)
    {
      ++i;

      TableAccessStrategy tblAccessStr = tables(node);
      cols.add(tblAccessStr.aliasedColumn(NODE_TABLE, "id") + " AS id" + i);
      if (annoCondition != null)
      {
        cols.add(
          annoCondition.getNodeAnnoNamespaceSQL(tblAccessStr) + " AS node_annotation_ns" + i);
        cols.add(
          annoCondition.getNodeAnnoNameSQL(tblAccessStr) + " AS node_annotation_name" + i);
      }
      if (outputCorpusPath)
      {
        cols.add(tblAccessStr.aliasedColumn(NODE_TABLE, "node_name")
          + " AS node_name" + i);
      }

      if (tblAccessStr.usesRankTable() || !node.getNodeAnnotations().isEmpty())
      {
        needsDistinct = true;
      }
    }

    // add additional empty columns in or clauses with different node sizes
    for (i = alternative.size() + 1; i <= maxWidth; ++i)
    {
      cols.add("NULL::bigint AS id" + i);
      if (outputCorpusPath)
      {
        cols.add("NULL::varchar AS node_name" + i);
      }
    }

    if (!alternative.isEmpty() && outputCorpusPath)
    {

      TableAccessStrategy tblAccessStr = tables(alternative.get(0));

      String corpusRefAlias = tblAccessStr.aliasedColumn(NODE_TABLE,
        "corpus_ref");
      cols.add(
        "(SELECT c.path_name FROM corpus AS c WHERE c.id = " + corpusRefAlias
        + " LIMIT 1) AS path_name");
    }

    if (outputToplevelCorpus)
    {
      cols.add(tables(alternative.get(0)).aliasedColumn(NODE_TABLE,
        "toplevel_corpus"));
    }

    cols.add(tables(alternative.get(0)).aliasedColumn(NODE_TABLE,
      "corpus_ref"));

    String colIndent = indent + TABSTOP + TABSTOP;

    return (needsDistinct ? "DISTINCT" : "") + "\n" + colIndent
      + StringUtils.join(cols, ",\n" + colIndent);
  }

  @Override
  protected void appendOrderByClause(StringBuffer sb, QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    // only use ORDER BY clause if result has to be sorted
    if (!sortSolutions)
    {
      return;
    }
    // don't use ORDER BY clause if we are only counting saves a sort
    List<LimitOffsetQueryData> extensions
      = queryData.getExtensions(LimitOffsetQueryData.class);

    if (extensions.size() > 0)
    {
      super.appendOrderByClause(sb, queryData, alternative, indent);
    }
  }

  @Override
  public String orderByClause(QueryData queryData, List<QueryNode> alternative,
    String indent)
  {
    List<String> ids = new ArrayList<String>();
    for (int i = 1; i <= queryData.getMaxWidth(); ++i)
    {
      ids.add("id" + i);
      if (annoCondition != null)
      {
        ids.add("node_annotation_ns" + i);
        ids.add("node_annotation_name" + i);
      }
    }
    return StringUtils.join(ids, ", ");
  }

  @Override
  public List<Match> extractData(ResultSet rs) throws SQLException,
    DataAccessException
  {
    List<Match> matches = new ArrayList<Match>();
    int rowNum = 0;
    while (rs.next())
    {
      matches.add(mapRow(rs, ++rowNum));
    }
    return matches;
  }

  @Override
  public Match mapRow(ResultSet rs, int rowNum) throws SQLException
  {
    Match match = new Match();

    // get size of solution
    ResultSetMetaData metaData = rs.getMetaData();
    int columnCount = metaData.getColumnCount();

    // the order of columns is not determined and I have to combined two
    // values, so save them here and combine later
    List<String> corpus_path = null;

    //get path
    if (outputCorpusPath)
    {
      for (int column = 1; column <= columnCount; ++column)
      {
        if (corpusPathExtractor != null && metaData.getColumnName(column).
          startsWith("path_name"))
        {
          List<String> genCorpusPath = corpusPathExtractor.extractCorpusPath(rs,
            metaData.getColumnName(column));
          // only use corpus path if valid
          if (genCorpusPath != null)
          {
            corpus_path = genCorpusPath;
            // all corpus paths are the same
            break;
          }
        }
      }

      // collect the node names, annotation namespaces and annotation names
      Map<Integer, String> nodeNames = new TreeMap<>();
      Map<Integer, String> nodeAnnoNamespaces = new HashMap<>();
      Map<Integer, String> nodeAnnoNames = new HashMap<>();

      for (int column = 1; column <= columnCount; ++column)
      {
        String columnName = metaData.getColumnName(column);
        if (columnName.startsWith("node_name"))
        {
          String numberAsString = columnName.substring("node_name".length());
          try
          {
            int number = Integer.parseInt(numberAsString);
            nodeNames.put(number, rs.getString(column));

            String annoNamespace = rs.getString("node_annotation_ns" + number);
            if (annoNamespace != null)
            {
              nodeAnnoNamespaces.put(number, annoNamespace);
            }
            String annoName = rs.getString("node_annotation_name" + number);
            if (annoName != null)
            {
              nodeAnnoNames.put(number, annoName);
            }

          }
          catch (NumberFormatException ex)
          {
            log.error("Could not extract the number for column " + columnName,
              ex);
          }
        }
      }

      for (Map.Entry<Integer, String> nodeNameEntry : nodeNames.entrySet())
      {
        URI saltID
          = buildSaltId(corpus_path, nodeNameEntry.getValue(),
            nodeAnnoNamespaces.get(nodeNameEntry.getKey()),
            nodeAnnoNames.get(nodeNameEntry.getKey()));
        match.addSaltId(saltID);
      }

    } // end if output path

    return match;
  }

  public boolean isOptimizeDistinct()
  {
    return optimizeDistinct;
  }

  public void setOptimizeDistinct(boolean optimizeDistinct)
  {
    this.optimizeDistinct = optimizeDistinct;
  }

  private URI buildSaltId(List<String> path, String node_name,
    String nodeAnnotatioNamespace, String nodeAnnotatioName)
  {
    StringBuilder sb = new StringBuilder("salt:/");

    for (String dir : path)
    {
      try
      {
        sb.append(URLEncoder.encode(dir, "UTF-8")).append("/");
      }
      catch (UnsupportedEncodingException ex)
      {
        log.error(null, ex);
        // fallback, cross fingers there are no invalid characters
        sb.append(dir).append("/");
      }
    }
    sb.append("#").append(node_name);

    // append information about the matched annotation
    if (nodeAnnotatioName != null)
    {
      if (nodeAnnotatioNamespace == null)
      {
        sb.append("@").append(nodeAnnotatioName);
      }
      else
      {
        sb.append("@")
          .append(nodeAnnotatioNamespace)
          .append("::")
          .append(nodeAnnotatioName);
      }
    }

    URI result;
    try
    {
      result = new URI(sb.toString());
      return result;
    }
    catch (URISyntaxException ex)
    {
      log.error("Could not generate valid ID from path "
        + path.toString() + " and node name " + node_name, ex);
    }
    return null;
  }

  public CorpusPathExtractor getCorpusPathExtractor()
  {
    return corpusPathExtractor;
  }

  public void setCorpusPathExtractor(CorpusPathExtractor corpusPathExtractor)
  {
    this.corpusPathExtractor = corpusPathExtractor;
  }

  public boolean isSortSolutions()
  {
    return sortSolutions;
  }

  public void setSortSolutions(boolean sortSolutions)
  {
    this.sortSolutions = sortSolutions;
  }

  public boolean isOutputCorpusPath()
  {
    return outputCorpusPath;
  }

  public void setOutputCorpusPath(boolean outputCorpusPath)
  {
    this.outputCorpusPath = outputCorpusPath;
  }

  public boolean isOutputToplevelCorpus()
  {
    return outputToplevelCorpus;
  }

  public void setOutputToplevelCorpus(boolean outputToplevelCorpus)
  {
    this.outputToplevelCorpus = outputToplevelCorpus;
  }

  public AnnotationConditionProvider getAnnoCondition()
  {
    return annoCondition;
  }

  public void setAnnoCondition(AnnotationConditionProvider annoCondition)
  {
    this.annoCondition = annoCondition;
  }

}
