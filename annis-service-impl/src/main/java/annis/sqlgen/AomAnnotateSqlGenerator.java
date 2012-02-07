/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.COMPONENT_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;
import org.springframework.dao.DataAccessException;

import annis.dao.DocumentNameMapRow;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import annis.model.Edge.EdgeType;

/**
 *
 * @author thomas
 */
public class AomAnnotateSqlGenerator extends AnnotateSqlGenerator<List<AnnotationGraph>>
{

  private static final Logger log =
    Logger.getLogger(AomAnnotateSqlGenerator.class.getName());

  public AnnisNode mapNode(ResultSet resultSet, TableAccessStrategy tableAccessStrategy) throws SQLException
  {
    AnnisNode annisNode = new AnnisNode(longValue(resultSet, NODE_TABLE, "id", tableAccessStrategy));
    
    annisNode.setCorpus(longValue(resultSet, NODE_TABLE, "corpus_ref", tableAccessStrategy));
    annisNode.setTextId(longValue(resultSet, NODE_TABLE, "text_ref", tableAccessStrategy));
    annisNode.setLeft(longValue(resultSet, NODE_TABLE, "left", tableAccessStrategy));
    annisNode.setRight(longValue(resultSet, NODE_TABLE, "right", tableAccessStrategy));
    annisNode.setNamespace(stringValue(resultSet, NODE_TABLE, "namespace", tableAccessStrategy));
    annisNode.setName(stringValue(resultSet, NODE_TABLE, "name", tableAccessStrategy));
    annisNode.setTokenIndex(longValue(resultSet, NODE_TABLE, "token_index", tableAccessStrategy));
    if (resultSet.wasNull())
      annisNode.setTokenIndex(null);
    annisNode.setSpannedText(stringValue(resultSet, NODE_TABLE, "span", tableAccessStrategy));
    annisNode.setLeftToken(longValue(resultSet, NODE_TABLE, "left_token", tableAccessStrategy));
    annisNode.setRightToken(longValue(resultSet, NODE_TABLE, "right_token", tableAccessStrategy));
    
    return annisNode;
  }
  
  public Edge mapEdge(ResultSet resultSet, TableAccessStrategy tableAccessStrategy)
      throws SQLException {
    Edge edge = new Edge();
    
    edge.setPre(longValue(resultSet, RANK_TABLE, "pre", tableAccessStrategy));
    edge.setEdgeType(EdgeType.parseEdgeType(stringValue(resultSet, RANK_TABLE, "edge_type", tableAccessStrategy)));
    edge.setNamespace(stringValue(resultSet, COMPONENT_TABLE, "namespace", tableAccessStrategy));
    edge.setName(stringValue(resultSet, COMPONENT_TABLE, "name", tableAccessStrategy));
    edge.setDestination(new AnnisNode(longValue(resultSet, RANK_TABLE, "node_ref", tableAccessStrategy)));
    
    // create nodes for src with rank value (parent) as id.
    // this must later be fixed by AnnotationGraphDaoHelper.fixSourceNodeIds().
    // this is simpler than chaining the edgeByPre map in AnnisResultSetBuilder
    // and making the EdgeRowMapper thread-safe.
    // FIXME: use custum mapRow(resultSet, edgeByPre) function, throw Exception here
    // also, no need to implement Spring RowMapper
    long parent = longValue(resultSet, RANK_TABLE, "parent", tableAccessStrategy);
    if ( ! resultSet.wasNull() )
      edge.setSource(new AnnisNode(parent));
    
    return edge;
  }
  
  public Annotation mapAnnotation(ResultSet resultSet, TableAccessStrategy tableAccessStrategy, String table) throws SQLException
  {
    // NOT NULL constraint on NAME => NULL indicates no annotation (of this type)
    String name = stringValue(resultSet, table, "name", tableAccessStrategy);
    if (resultSet.wasNull())
      return null;
    
    String namespace = stringValue(resultSet, table, "namespace", tableAccessStrategy);
    String value = stringValue(resultSet, table, "value", tableAccessStrategy);
    
    return new Annotation(namespace, name, value);
  }

  private long longValue(ResultSet resultSet, String table, String column, TableAccessStrategy tableAccessStrategy) throws SQLException {
    return resultSet.getLong(tableAccessStrategy.columnName(table, column));
  }

  private String stringValue(ResultSet resultSet, String table, String column, TableAccessStrategy tableAccessStrategy) throws SQLException {
    return resultSet.getString(tableAccessStrategy.columnName(table, column));
  }

  @Override
  public List<AnnotationGraph> extractData(ResultSet resultSet)
    throws SQLException, DataAccessException
  {
    TableAccessStrategy tableAccessStrategy = createTableAccessStrategy();
    
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
      AnnisNode node = mapNode(resultSet, tableAccessStrategy);

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
      Edge edge = mapEdge(resultSet, tableAccessStrategy);

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
        mapAnnotation(resultSet, tableAccessStrategy, TableAccessStrategy.NODE_ANNOTATION_TABLE);
      if (nodeAnnotation != null)
      {
        node.addNodeAnnotation(nodeAnnotation);
      }
      Annotation edgeAnnotation =
          mapAnnotation(resultSet, tableAccessStrategy, TableAccessStrategy.EDGE_ANNOTATION_TABLE);
      if (edgeAnnotation != null)
      {
        edge.addAnnotation(edgeAnnotation);
      }
      rowNum++;
    }
    
    // remove edges from the graph with a source node inside the match
    for(AnnotationGraph graph : graphByMatchGroup.values())
    {
      ListIterator<Edge> itEdge = graph.getEdges().listIterator();
      while(itEdge.hasNext())
      {
        Edge edge = itEdge.next();
        if(edge.getSource() == null)
        {
          edge.getDestination().getIncomingEdges().remove(edge);
          itEdge.remove();
        }
      }
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

}
