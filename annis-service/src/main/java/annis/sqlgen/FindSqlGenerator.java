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
import com.google.common.base.Preconditions;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class FindSqlGenerator extends AbstractSqlGenerator
  implements RowMapper<Match>, SelectClauseSqlGenerator<QueryData>,
  FromClauseSqlGenerator<QueryData>,  
  WhereClauseSqlGenerator<QueryData>,
  SqlGeneratorAndExtractor<QueryData, List<Match>> 
{
  
  private static final Logger log = LoggerFactory.getLogger(
    FindSqlGenerator.class);
  
  private CorpusPathExtractor corpusPathExtractor;
  private boolean outputCorpusPath = true;
  private AnnotationConditionProvider annoCondition;
  private SolutionSqlGenerator solutionSqlGenerator;

  private final Escaper fragmentEscaper = UrlEscapers.urlFragmentEscaper();
  private final Escaper pathEscaper = UrlEscapers.urlPathSegmentEscaper();
  
  @Override
  public String selectClause(QueryData queryData, List<QueryNode> alternative,
    String indent)
  {
    StringBuilder sb = new StringBuilder();
    
    String indent2 = indent + TABSTOP;
    
    sb.append(indent2).append("solution.*,\n");
    
    // add node annotation namespace and name for each query node
    int i=0;
    Iterator<QueryNode> itNodes = alternative.iterator();
    while(itNodes.hasNext())
    {
      i++;
      
      QueryNode n = itNodes.next();
      TableAccessStrategy tas = tables(n);
      sb.append(indent2).append(annoCondition.getNodeAnnoNamespaceSQL(tas))
        .append(" AS node_annotation_ns").append(i).append(",\n");
      sb.append(indent2).append(annoCondition.getNodeAnnoNameSQL(tas))
        .append(" AS node_annotation_name").append(i).append(",\n");
      
      // corpus path is only needed once
      sb.append(indent2).append("c.path_name AS path_name");
      if(itNodes.hasNext())
      {
        sb.append(",\n");
      }
    }
    
    return sb.toString();
  }

  @Override
  public String fromClause(QueryData queryData, List<QueryNode> alternative,
    String indent)
  {
    StringBuilder sb = new StringBuilder();
    
    sb.append(indent).append("(\n");
    
    sb.append(solutionSqlGenerator.toSql(queryData, indent+TABSTOP));
    
    sb.append(") AS solution \n");
    
    Preconditions.checkArgument(!alternative.isEmpty(), "There must be at least one query node in the alternative");
    // add the left joins with the annotation category table
    int i=0;
    Iterator<QueryNode> itNodes = alternative.iterator();
    while(itNodes.hasNext())
    {
      i++;
      
      QueryNode n = itNodes.next();
      sb.append(indent)
        .append("LEFT JOIN annotation_category AS annotation_category")
        .append(n.getId())
        .append(" ON (solution.toplevel_corpus = annotation_category")
        .append(n.getId())
        .append(".toplevel_corpus")
        .append(" AND solution.cat")
        .append(i)
        .append(" = annotation_category")
        .append(n.getId())
        .append(".id")
        .append(")");
        if(!itNodes.hasNext())
        {
          sb.append(",");
        }
        sb.append("\n");
    }
    
    sb.append(indent).append("corpus AS c");
    
    return sb.toString();
  }

  @Override
  public Set<String> whereConditions(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    Set<String> conditions = new LinkedHashSet<>();
    
    conditions.add("c.id = solution.corpus_ref");
    
    return conditions;
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

      // collect the salt id, node name appendix, annotation namespaces and annotation names
      Map<Integer, String> saltIDs = new TreeMap<>();
      Map<Integer, String> nodeAnnoNamespaces = new HashMap<>();
      Map<Integer, String> nodeAnnoNames = new HashMap<>();

      for (int column = 1; column <= columnCount; ++column)
      {
        String columnName = metaData.getColumnName(column);
        if (columnName.startsWith("salt_id"))
        {
          String numberAsString = columnName.substring("salt_id".length());
          try
          {
            int number = Integer.parseInt(numberAsString);
            String saltIDForNode = rs.getString(column);
            if(saltIDForNode != null)
            {
              saltIDs.put(number, rs.getString(column));

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

          }
          catch (NumberFormatException ex)
          {
            log.error("Could not extract the number for column " + columnName,
              ex);
          }
        }
      }

      for (Map.Entry<Integer, String> saltIDEntry : saltIDs.entrySet())
      {
        URI saltID = buildSaltId(corpus_path, saltIDEntry.getValue());
        
        String qualifiedAnnoName = 
          buildAnnoName(nodeAnnoNamespaces.get(saltIDEntry.getKey()),
            nodeAnnoNames.get(saltIDEntry.getKey()));
        
        match.addSaltId(saltID, qualifiedAnnoName);
      }

    } // end if output path

    return match;
  }
  
  private String buildAnnoName(String ns, String name)
  {
    if(name != null)
    {
      if(ns == null)
      {
        return name;
      }
      else
      {
        return ns + "::" + name;
      }
    }
    return null;
  }
  
  /**
   * Builds a proper salt ID.
   * @param path
   * @param saltID
   * @return 
   */
  private URI buildSaltId(List<String> path, String saltID)
  {
    StringBuilder sb = new StringBuilder("salt:/");

    Iterator<String> itPath = path.iterator();
    while(itPath.hasNext())
    {
      String dir = itPath.next();
      sb.append(pathEscaper.escape(dir));
      if(itPath.hasNext())
      {
        sb.append("/");
      }
    }
    
    sb.append("#").append(fragmentEscaper.escape(saltID));


    URI result;
    try
    {
      result = new URI(sb.toString());
      return result;
    }
    catch (URISyntaxException ex)
    {
      log.error("Could not generate valid ID from path "
        + path.toString() + " and node name " + saltID, ex);
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
