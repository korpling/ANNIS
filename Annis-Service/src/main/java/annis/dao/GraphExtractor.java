/*
 *  Copyright 2010 thomas.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package annis.dao;

import static annis.sqlgen.TableAccessStrategy.FACTS_TABLE;

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import annis.sqlgen.TableAccessStrategy;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;

/**
 *
 * @author thomas
 */
public class GraphExtractor implements ResultSetExtractor
{

  private static final Logger log = Logger.getLogger(GraphExtractor.class);
  private String matchedNodesViewName;
  private boolean allowIslands;
  private AnnotationRowMapper nodeAnnotationRowMapper;
  private AnnotationRowMapper edgeAnnotationRowMapper;
  private EdgeRowMapper edgeRowMapper;
  private AnnisNodeRowMapper annisNodeRowMapper;

  public GraphExtractor()
  {
    // FIXME: totally ugly, but the query has fixed column names (and needs its own column aliasing)
    // TableAccessStrategyFactory wants a corpus selection strategy
    // solution: build AnnisNodes with API and refactor SqlGenerator to accept GROUP BY nodes
    Map<String, String> nodeColumns = new HashMap<String, String>();
    nodeColumns.put("namespace", "node_namespace");
    nodeColumns.put("name", "node_name");

    Map<String, String> nodeAnnotationColumns = new HashMap<String, String>();
    nodeAnnotationColumns.put("node_ref", "id");
    nodeAnnotationColumns.put("namespace", "node_annotation_namespace");
    nodeAnnotationColumns.put("name", "node_annotation_name");
    nodeAnnotationColumns.put("value", "node_annotation_value");

    Map<String, String> edgeAnnotationColumns = new HashMap<String, String>();
    nodeAnnotationColumns.put("rank_ref", "pre");
    edgeAnnotationColumns.put("namespace", "edge_annotation_namespace");
    edgeAnnotationColumns.put("name", "edge_annotation_name");
    edgeAnnotationColumns.put("value", "edge_annotation_value");

    Map<String, String> edgeColumns = new HashMap<String, String>();
    edgeColumns.put("node_ref", "id");
    edgeColumns.put("name", "edge_name");
    edgeColumns.put("namespace", "edge_namespace");

    Map<String, Map<String, String>> columnAliases = new HashMap<String, Map<String, String>>();
    columnAliases.put(TableAccessStrategy.NODE_TABLE, nodeColumns);
    columnAliases.put(TableAccessStrategy.NODE_ANNOTATION_TABLE, nodeAnnotationColumns);
    columnAliases.put(TableAccessStrategy.EDGE_ANNOTATION_TABLE, edgeAnnotationColumns);
    columnAliases.put(TableAccessStrategy.RANK_TABLE, edgeColumns);

    TableAccessStrategy tableAccessStrategy = new TableAccessStrategy(null);
    tableAccessStrategy.setColumnAliases(columnAliases);

    edgeRowMapper = new EdgeRowMapper();
    edgeRowMapper.setTableAccessStrategy(tableAccessStrategy);

    annisNodeRowMapper = new AnnisNodeRowMapper();
    annisNodeRowMapper.setTableAccessStrategy(tableAccessStrategy);

    nodeAnnotationRowMapper = new AnnotationRowMapper(TableAccessStrategy.NODE_ANNOTATION_TABLE);
    nodeAnnotationRowMapper.setTableAccessStrategy(tableAccessStrategy);

    edgeAnnotationRowMapper = new AnnotationRowMapper(TableAccessStrategy.EDGE_ANNOTATION_TABLE);
    edgeAnnotationRowMapper.setTableAccessStrategy(tableAccessStrategy);
  }

  public String explain(JdbcTemplate jdbcTemplate, List<Long> corpusList, int nodeCount, long offset, long limit, int left, int right, boolean analyze)
  {
    ParameterizedSingleColumnRowMapper<String> planRowMapper =
      new ParameterizedSingleColumnRowMapper<String>();

    List<String> plan = jdbcTemplate.query((analyze ? "EXPLAIN ANALYZE " : "EXPLAIN ")
      + "\n" + getContextQuery(corpusList, left, right, limit, offset, nodeCount), planRowMapper);
    return StringUtils.join(plan, "\n");
  }

  public List<AnnotationGraph> queryAnnotationGraph(JdbcTemplate jdbcTemplate, List<Long> corpusList, int nodeCount, long offset, long limit, int left, int right)
  {
    return (List<AnnotationGraph>) jdbcTemplate.query(getContextQuery(corpusList, left, right, limit, offset, nodeCount), this);
  }

  public List<AnnotationGraph> queryAnnotationGraph(JdbcTemplate jdbcTemplate, long textID)
  {
    return (List<AnnotationGraph>) jdbcTemplate.query(getTextQuery(textID), this);
  }

  public String getContextQuery(List<Long> corpusList, int left, int right, long limit, long offset, int nodeCount)
  {

    // key for annotation graph matches
    StringBuilder keySb = new StringBuilder();
    keySb.append("ARRAY[matches.id1");
    for (int i = 2; i <= nodeCount; ++i)
    {
      keySb.append(",");
      keySb.append("matches.id");
      keySb.append(i);
    }
    keySb.append("] AS key");
    String key = keySb.toString();

    // sql for matches
    StringBuilder matchSb = new StringBuilder();
    matchSb.append("SELECT * FROM ");
    matchSb.append(matchedNodesViewName);
    matchSb.append(" ORDER BY ");
    matchSb.append("id1");
    for (int i = 2; i <= nodeCount; ++i)
    {
      matchSb.append(", ");
      matchSb.append("id");
      matchSb.append(i);
    }
    matchSb.append(" OFFSET ");
    matchSb.append(offset);
    matchSb.append(" LIMIT ");
    matchSb.append(limit);
    String matchSql = matchSb.toString();

    StringBuilder sb = new StringBuilder();
    sb.append("SELECT DISTINCT \n");
    sb.append("\t");
    sb.append(key);
    sb.append(", facts.*\n");
    sb.append("FROM\n");
    sb.append("\t(");
    sb.append(matchSql);
    sb.append(") AS matches,\n");
    sb.append("\t");
    sb.append(FACTS_TABLE);
    sb.append(" AS facts\n");
    sb.append("WHERE\n");
    if (corpusList != null)
    {
      sb.append("facts.toplevel_corpus IN (");
      sb.append(corpusList.isEmpty() ? "NULL" : StringUtils.join(corpusList, ","));
      sb.append(") AND\n");
    }
    sb.append("\t(\n");
    for (int i = 1;  i <= nodeCount; ++i)
    {
      if(i > 1)
      {
        sb.append("\n\t\tOR\n");
      }


      sb.append("\t\t(\n"
        + "\t\t\tfacts.text_ref = matches.text_ref");
      sb.append(i);
      sb.append("\n"
        + "\t\t\tAND\n");

      if(false && allowIslands)
      {

      }
      else
      {
        sb.append("\t\t\t(\n"
          + "\t\t\t\t(facts.left_token >= matches.left_token");
        sb.append(i);
        sb.append(" - ");
        sb.append(left);
        sb.append(" AND facts.right_token <= matches.right_token");
        sb.append(i);
        sb.append(" + ");
        sb.append(right);
        sb.append(")\n"
          + "\t\t\t\tOR (facts.left_token <= matches.left_token");
        sb.append(i);
        sb.append(" - ");
        sb.append(left);
        sb.append(" AND matches.left_token");
        sb.append(i);
        sb.append(" - ");
        sb.append(left);
        sb.append(" <= facts.right_token)\n"
          + "\t\t\t\tOR (facts.left_token <= matches.right_token");
        sb.append(i);
        sb.append(" + ");
        sb.append(right);
        sb.append(" AND matches.right_token");
        sb.append(i);
        sb.append(" + ");
        sb.append(right);
        sb.append(" <= facts.right_token)\n"
          + "\t\t\t)");
      }

      sb.append("\n"
        + "\t\t)");
    }
    sb.append("\n\t)\n");
    sb.append("\nORDER BY key, facts.pre");
    return sb.toString();
  }

  public String getTextQuery(long textID)
  {
    String template = "SELECT DISTINCT \n"
      + "\tARRAY[-1::numeric] AS key, facts.*\n"
      + "FROM\n"
      + "\tfacts AS facts\n"
      + "WHERE\n" + "\tfacts.text_ref = :text_id\n"
      + "ORDER BY facts.pre";
    String sql = template.replace(":text_id", String.valueOf(textID));
    return sql;
  }

  @Override
  public List<AnnotationGraph> extractData(ResultSet resultSet) throws SQLException, DataAccessException
  {
    List<AnnotationGraph> graphs = new LinkedList<AnnotationGraph>();

    // fn: match group -> annotation graph

    Map<List<Long>, AnnotationGraph> graphByMatchGroup = new HashMap<List<Long>, AnnotationGraph>();

    // fn: node id -> node
    Map<Long, AnnisNode> nodeById = new HashMap<Long, AnnisNode>();

    // fn: edge pre order value -> edge
    Map<Long, Edge> edgeByPre = new HashMap<Long, Edge>();

    int rowNum = 0;
    while (resultSet.next())
    {
      // process result by match group
      // match group is identified by the ids of the matched nodes
      Array sqlKey = resultSet.getArray("key");
      Validate.isTrue(!resultSet.wasNull(), "Match group identifier must not be null");
      Validate.isTrue(sqlKey.getBaseType() == Types.NUMERIC,
        "Key in database must be from the type \"numeric\" but was \"" + sqlKey.getBaseTypeName() + "\"");

      BigDecimal[] keyArray = (BigDecimal[]) sqlKey.getArray();
      ArrayList<Long> key = new ArrayList<Long>();
      for (BigDecimal bd : keyArray)
      {
        key.add(bd == null ? null : bd.longValue());
      }

      if (!graphByMatchGroup.containsKey(key))
      {
        log.debug("starting annotation graph for match: " + key);
        AnnotationGraph graph = new AnnotationGraph();
        graphs.add(graph);
        graphByMatchGroup.put(key, graph);

        // clear mapping functions for this graph
        // assumes that the result set is sorted by key, pre
        nodeById.clear();
        edgeByPre.clear();

        // set the matched keys
        for (Long l : key)
        {
          if(l != null)
          {
            graph.addMatchedNodeId(l);
          }
        }
      }

      AnnotationGraph graph = graphByMatchGroup.get(key);

      // get node data
      AnnisNode node = annisNodeRowMapper.mapRow(resultSet, rowNum);

      // add node to graph if it is new, else get known copy
      long id = node.getId();
      if (!nodeById.containsKey(id))
      {
        log.debug("new node: " + id);
        nodeById.put(id, node);
        graph.addNode(node);
      }
      else
      {
        node = nodeById.get(id);
      }

      // we now have the id of the node and the general key, so we can 
      // add the matched node index to the graph (if matched)
      long matchIndex = 1;
      //node.setMatchedNodeInQuery(null);
      for (Long l : key)
      {
        if(l != null)
        {
          if (id == l)
          {
            node.setMatchedNodeInQuery(matchIndex);
            break;
          }
          matchIndex++;
        }
      }

      // get edge data
      Edge edge = edgeRowMapper.mapRow(resultSet, rowNum);

      // add edge to graph if it is new, else get known copy
      long pre = edge.getPre();
      if (!edgeByPre.containsKey(pre))
      {
        // fix source references in edge
        edge.setDestination(node);
        fixNodes(edge, edgeByPre, nodeById);

        // add edge to src and dst nodes
        node.addIncomingEdge(edge);
        AnnisNode source = edge.getSource();
        if (source != null)
        {
          source.addOutgoingEdge(edge);
        }

        log.debug("new edge: " + edge);
        edgeByPre.put(pre, edge);
        graph.addEdge(edge);
      }
      else
      {
        edge = edgeByPre.get(pre);
      }

      // add annotation data
      Annotation nodeAnnotation = nodeAnnotationRowMapper.mapRow(resultSet, rowNum);
      if (nodeAnnotation != null)
      {
        node.addNodeAnnotation(nodeAnnotation);
      }
      Annotation edgeAnnotation = edgeAnnotationRowMapper.mapRow(resultSet, rowNum);
      if (edgeAnnotation != null)
      {
        edge.addAnnotation(edgeAnnotation);
      }

      rowNum++;
    }


    return graphs;
  }

  protected void fixNodes(Edge edge, Map<Long, Edge> edgeByPre, Map<Long, AnnisNode> nodeById)
  {
    // pull source node from parent edge
    AnnisNode source = edge.getSource();
    if (source == null)
    {
      return;
    }
    long pre = source.getId();
    Edge parentEdge = edgeByPre.get(pre);
    AnnisNode parent = parentEdge != null ? parentEdge.getDestination() : null;
//		log.debug("looking for node with rank.pre = " + pre + "; found: " + parent);
    edge.setSource(parent);

    // pull destination node from mapping function
    long destinationId = edge.getDestination().getId();
    edge.setDestination(nodeById.get(destinationId));
  }

  public String getMatchedNodesViewName()
  {
    return matchedNodesViewName;
  }

  public void setMatchedNodesViewName(String matchedNodesViewName)
  {
    this.matchedNodesViewName = matchedNodesViewName;
  }

  public boolean isAllowIslands()
  {
    return allowIslands;
  }

  public void setAllowIslands(boolean allowIslands)
  {
    this.allowIslands = allowIslands;
  }



  
}
