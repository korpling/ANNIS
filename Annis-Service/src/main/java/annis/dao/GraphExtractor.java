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

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import annis.sqlgen.TableAccessStrategy;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 *
 * @author thomas
 */
public class GraphExtractor  implements ResultSetExtractor
{

	private static final Logger log = Logger.getLogger(GraphExtractor.class);

  private String matchedNodesViewName;
  private String nodeTableViewName;
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
		edgeColumns.put("namespace", "edge_name");

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

  public List<AnnotationGraph> queryAnnotationGraph(JdbcTemplate jdbcTemplate, List<Long> corpusList, int nodeCount, long offset, long limit, int left, int right)
  {
    createLimitedView(jdbcTemplate, nodeCount, offset, limit);
    createResultView(jdbcTemplate, nodeCount);

    return (List<AnnotationGraph>) jdbcTemplate.query(getContextQuery(left, right), this);
  }

  private void createLimitedView(JdbcTemplate jdbcTemplate, int nodeCount, long offset, long limit)
  {
    StringBuilder sbOrder = new StringBuilder();
    for(int i=1; i <= nodeCount; i++)
    {
      if(i > 1)
      {
        sbOrder.append(", ");
      }
      sbOrder.append("id");
      sbOrder.append(i);
    }

    String query =
      "CREATE TEMPORARY VIEW limited AS\n"
      + "SELECT row_number() OVER () AS resultid, *\n"
      + "FROM (SELECT * FROM " + matchedNodesViewName + " ORDER BY " + sbOrder.toString() + ") AS m LIMIT " + limit + " OFFSET " + offset;

    
    jdbcTemplate.execute(query);

  }

  private void createResultView(JdbcTemplate jdbcTemplate, int nodeCount)
  {
    StringBuilder q = new StringBuilder();

    String[] fields = new String[] 
    {
      "id", "text_ref","left_token","right_token"
    };

    
    // map the indexed columns to their "native" form without appended index
    // and code the index in an extra column

    q.append("CREATE TEMPORARY VIEW result AS\n");
    for(int i=1; i<=nodeCount; i++)
    {
      if(i > 1)
      {
        q.append("\nUNION ALL\n");
      }

      q.append("SELECT resultid AS resultid, ");
      q.append(i);
      q.append(" AS match_index");
      for(String s : fields)
      {
        q.append(", ");
        q.append(s);
        q.append(i);
        q.append(" AS ");
        q.append(s);
      }
      q.append("\nFROM limited\n");
    }

    jdbcTemplate.execute(q.toString());

  }

  private String getContextQuery(int left, int right)
  {
    StringBuilder q = new StringBuilder();

    q.append("SELECT * FROM (\n");
    q.append("SELECT r.resultid AS resultid, CAST(NULL as numeric) AS match_index, f.* FROM result AS r, ");
    q.append(nodeTableViewName);
    q.append(" AS f \n"
      + "WHERE 	f.text_ref = r.text_ref AND ((f.left_token >= r.left_token - ");
    q.append(left);
    q.append(" AND f.right_token <= r.right_token + ");
    q.append(right);
    q.append(") OR (f.left_token <= r.left_token - ");
    q.append(left);
    q.append(" AND r.left_token - ");
    q.append(left);
    q.append(" <= f.right_token) OR (f.left_token <= r.right_token + ");
    q.append(right);
    q.append(" AND r.right_token + ");
    q.append(right);
    q.append(" <= f.right_token))");
    q.append("	\n"
      + "AND f.id <> r.id \n"
      + "UNION ALL\n"
      + "SELECT r.resultid AS resultid, r.match_index AS match_index, f.* FROM result AS r\n,");
    q.append(nodeTableViewName);
    q.append(" AS f\n"
      + "WHERE f.id = r.id\n");
    q.append("\n) as temp ORDER BY resultid, pre\n");


    return q.toString();
  }

  @Override
  public List<AnnotationGraph> extractData(ResultSet resultSet) throws SQLException, DataAccessException
  {
    List<AnnotationGraph> graphs = new LinkedList<AnnotationGraph>();

    // fn: match group -> annotation graph
    Map<Long, AnnotationGraph> graphByMatchGroup = new HashMap<Long, AnnotationGraph>();

    // fn: node id -> node
    Map<Long, AnnisNode> nodeById = new HashMap<Long, AnnisNode>();

    // fn: edge pre order value -> edge
    Map<Long, Edge> edgeByPre = new HashMap<Long, Edge>();

    int rowNum = 0;
    while (resultSet.next())
    {
      // process result by match group
      // match group is identified by the ids of the matched nodes
      Long key = resultSet.getLong("resultid");
      Validate.isTrue(!resultSet.wasNull(), "Match group identifier must not be null");

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

      // add matched node id to the graph
      long matchIndex = resultSet.getLong("match_index");
      if (!resultSet.wasNull())
      {
        graph.addMatchedNodeId(id);
        node.setMatchedNodeInQuery(matchIndex);
      }
      else if (!graph.getMatchedNodeIds().contains(id))
      {
        node.setMatchedNodeInQuery(null);
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

  public String getNodeTableViewName()
  {
    return nodeTableViewName;
  }

  public void setNodeTableViewName(String nodeTableViewName)
  {
    this.nodeTableViewName = nodeTableViewName;
  }

  
}
