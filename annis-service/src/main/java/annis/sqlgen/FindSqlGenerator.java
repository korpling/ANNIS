/*
 * Copyright 2014 SFB 632.
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
import annis.service.objects.Match;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class FindSqlGenerator extends AbstractUnionSqlGenerator
  implements RowMapper<Match>, SelectClauseSqlGenerator<QueryData>,
  FromClauseSqlGenerator<QueryData>,  ResultSetExtractor<List<Match>> 
{
  
  private static final Logger log = LoggerFactory.getLogger(
    FindSqlGenerator.class);
  
  private CorpusPathExtractor corpusPathExtractor;
  private boolean outputCorpusPath;
  private AnnotationConditionProvider annoCondition;
  private SolutionSqlGenerator solutionSqlGenerator;

  @Override
  public String selectClause(QueryData queryData, List<QueryNode> alternative,
    String indent)
  {
    StringBuilder sb = new StringBuilder();
    
    sb.append(indent).append("solution.*").append("\n");
    
    // add node annotation namespace and name for each query node
    for(QueryNode n : alternative)
    {
      TableAccessStrategy tas = tables(n);
      sb.append(indent).append(annoCondition.getNodeAnnoNamespaceSQL(tas))
        .append(" AS node_annotation_ns").append(n.getId()).append(",\n");
      sb.append(indent).append(annoCondition.getNodeAnnoNameSQL(tas))
        .append(" AS node_annotation_name").append(n.getId()).append(",\n");
      
      // corpus path is only needed ince
      sb.append(indent).append("c.path_name AS path_name\n");
    }
    
    return sb.toString();
  }

  @Override
  public String fromClause(QueryData queryData, List<QueryNode> alternative,
    String indent)
  {
    StringBuilder sb = new StringBuilder();
    
    sb.append(indent).append("(");
    
    sb.append(solutionSqlGenerator.toString());
    
    sb.append(") AS solution \n");
    
    return sb.toString();
  }

  
  
  
  @Override
  public List<Match> extractData(ResultSet rs) throws SQLException,
    DataAccessException
  {
    List<Match> matches = new ArrayList<>();
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
  
  
  public boolean isOutputCorpusPath()
  {
    return outputCorpusPath;
  }

  public void setOutputCorpusPath(boolean outputCorpusPath)
  {
    this.outputCorpusPath = outputCorpusPath;
  }
  
  public CorpusPathExtractor getCorpusPathExtractor()
  {
    return corpusPathExtractor;
  }

  public void setCorpusPathExtractor(CorpusPathExtractor corpusPathExtractor)
  {
    this.corpusPathExtractor = corpusPathExtractor;
  }

  public AnnotationConditionProvider getAnnoCondition()
  {
    return annoCondition;
  }

  public void setAnnoCondition(AnnotationConditionProvider annoCondition)
  {
    this.annoCondition = annoCondition;
  }

  public SolutionSqlGenerator getSolutionSqlGenerator()
  {
    return solutionSqlGenerator;
  }

  public void setSolutionSqlGenerator(SolutionSqlGenerator solutionSqlGenerator)
  {
    this.solutionSqlGenerator = solutionSqlGenerator;
  }
  
  
  
  
}
