/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annis.sqlgen;

import annis.dao.AnnisNodeRowMapper;
import annis.dao.AnnotationRowMapper;
import annis.dao.DocumentNameMapRow;
import annis.dao.EdgeRowMapper;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.Validate;
import org.springframework.dao.DataAccessException;

/**
 *
 * @author thomas
 */
public class AOMAnnotateSqlGenerator extends AnnotateSqlGenerator<List<AnnotationGraph>>
{

  private static final Logger log =
    Logger.getLogger(AOMAnnotateSqlGenerator.class.getName());
  private EdgeRowMapper edgeRowMapper;
  private AnnisNodeRowMapper annisNodeRowMapper;
  private AnnotationRowMapper nodeAnnotationRowMapper;
  private AnnotationRowMapper edgeAnnotationRowMapper;

  public AOMAnnotateSqlGenerator()
  {
    edgeRowMapper = new EdgeRowMapper();
    edgeRowMapper.setTableAccessStrategy(getFactsTas());

    annisNodeRowMapper = new AnnisNodeRowMapper();
    annisNodeRowMapper.setTableAccessStrategy(getFactsTas());

    nodeAnnotationRowMapper = new AnnotationRowMapper(
      TableAccessStrategy.NODE_ANNOTATION_TABLE);
    nodeAnnotationRowMapper.setTableAccessStrategy(getFactsTas());

    edgeAnnotationRowMapper = new AnnotationRowMapper(
      TableAccessStrategy.EDGE_ANNOTATION_TABLE);
    edgeAnnotationRowMapper.setTableAccessStrategy(getFactsTas());
  }

  @Override
  public List<AnnotationGraph> extractData(ResultSet resultSet)
    throws SQLException, DataAccessException
  {
    // function result
    List<AnnotationGraph> graphs =
      new LinkedList<AnnotationGraph>();

    // fn: match group -> annotation graph

    Map<List<Long>, AnnotationGraph> graphByMatchGroup =
      new HashMap<List<Long>, AnnotationGraph>();

    // fn: node id -> node
    Map<Long, AnnisNode> nodeById = new HashMap<Long, AnnisNode>();

    // fn: edge pre order value -> edge
    Map<Long, Edge> edgeByPre = new HashMap<Long, Edge>();

    int rowNum = 0;
    while (resultSet.next())
    {
      // process result by match group
      // match group is identified by the ids of the matched 
      // nodes
      Array sqlKey = resultSet.getArray("key");
      Validate.isTrue(!resultSet.wasNull(),
        "Match group identifier must not be null");
      Validate.isTrue(sqlKey.getBaseType() == Types.BIGINT,
        "Key in database must be from the type \"bigint\" but was \""
        + sqlKey.getBaseTypeName() + "\"");

      Long[] keyArray = (Long[]) sqlKey.getArray();
      List<Long> key = Arrays.asList(keyArray);

      if (!graphByMatchGroup.containsKey(key))
      {
        log.log(Level.FINE, "starting annotation graph for match: {0}", key);
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
          if (l != null)
          {
            graph.addMatchedNodeId(l);
          }
        }
      }

      AnnotationGraph graph = graphByMatchGroup.get(key);

      graph.setDocumentName(new DocumentNameMapRow().mapRow(
        resultSet, rowNum));

      Array path = resultSet.getArray("path");
      graph.setPath((String[]) path.getArray());

      // get node data
      AnnisNode node = annisNodeRowMapper.mapRow(resultSet,
        rowNum);

      // add node to graph if it is new, else get known copy
      long id = node.getId();
      if (!nodeById.containsKey(id))
      {
        log.log(Level.FINE, "new node: {0}", id);
        nodeById.put(id, node);
        graph.addNode(node);
      }
      else
      {
        node = nodeById.get(id);
      }

      // we now have the id of the node and the general key, 
      // so we can
      // add the matched node index to the graph (if matched)
      long matchIndex = 1;
      // node.setMatchedNodeInQuery(null);
      for (Long l : key)
      {
        if (l != null)
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

        log.log(Level.FINE, "new edge: {0}", edge);
        edgeByPre.put(pre, edge);
        graph.addEdge(edge);
      }
      else
      {
        edge = edgeByPre.get(pre);
      }

      // add annotation data
      Annotation nodeAnnotation =
        nodeAnnotationRowMapper.mapRow(resultSet, rowNum);
      if (nodeAnnotation != null)
      {
        node.addNodeAnnotation(nodeAnnotation);
      }
      Annotation edgeAnnotation =
        edgeAnnotationRowMapper.mapRow(resultSet, rowNum);
      if (edgeAnnotation != null)
      {
        edge.addAnnotation(edgeAnnotation);
      }

      rowNum++;
    }

    return graphs;
  }

  protected void fixNodes(Edge edge, Map<Long, Edge> edgeByPre,
    Map<Long, AnnisNode> nodeById)
  {
    // pull source node from parent edge
    AnnisNode source = edge.getSource();
    if (source == null)
    {
      return;
    }
    long pre = source.getId();
    Edge parentEdge = edgeByPre.get(pre);
    AnnisNode parent = parentEdge != null
      ? parentEdge.getDestination() : null;
    // log.debug("looking for node with rank.pre = 
    // " + pre + "; found: " + parent);
    edge.setSource(parent);

    // pull destination node from mapping function
    long destinationId = edge.getDestination().getId();
    edge.setDestination(nodeById.get(destinationId));
  }

  public AnnisNodeRowMapper getAnnisNodeRowMapper()
  {
    return annisNodeRowMapper;
  }

  public EdgeRowMapper getEdgeRowMapper()
  {
    return edgeRowMapper;
  }

  public AnnotationRowMapper getEdgeAnnotationRowMapper()
  {
    return edgeAnnotationRowMapper;
  }

  public AnnotationRowMapper getNodeAnnotationRowMapper()
  {
    return nodeAnnotationRowMapper;
  }
}
