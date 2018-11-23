/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annis.sqlgen;

import annis.dao.DocumentNameMapRow;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import annis.model.Edge.EdgeType;
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
import java.util.Map.Entry;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 *
 * @author thomas
 */
public class AomAnnotateExtractor implements ResultSetExtractor<List<AnnotationGraph>>
{
  
  private static final Logger log =
    LoggerFactory.getLogger(AomAnnotateExtractor.class);

  private TableAccessStrategy outerQueryTableAccessStrategy;
  
  public AnnisNode mapNode(ResultSet resultSet, TableAccessStrategy tableAccessStrategy, 
    Map<Long, ComponentEntry> spans) throws SQLException
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
    
    
    String typeAsString = stringValue(resultSet, COMPONENT_TABLE, "type",
      tableAccessStrategy);
    
    if(spans != null && "c".equals(typeAsString))
    {
      ComponentEntry entry = new ComponentEntry(
        longValue(resultSet, COMPONENT_TABLE, "id", tableAccessStrategy), 
        typeAsString.charAt(0), 
        stringValue(resultSet, COMPONENT_TABLE, "namespace", tableAccessStrategy),
        stringValue(resultSet, COMPONENT_TABLE, "name", tableAccessStrategy));
      spans.put(annisNode.getId(), entry);
    }
    
    return annisNode;
  }
  
  public Edge mapEdge(ResultSet resultSet, TableAccessStrategy tableAccessStrategy)
      throws SQLException {
    Edge edge = new Edge();
    
    edge.setPre(longValue(resultSet, RANK_TABLE, "pre", tableAccessStrategy));
    edge.setComponentID(longValue(resultSet, RANK_TABLE, "component_id",
      tableAccessStrategy));
    edge.setEdgeType(EdgeType.parseEdgeType(stringValue(resultSet, RANK_TABLE, "edge_type", tableAccessStrategy)));
    edge.setNamespace(stringValue(resultSet, COMPONENT_TABLE, "namespace", tableAccessStrategy));
    edge.setName(stringValue(resultSet, COMPONENT_TABLE, "name", tableAccessStrategy));
    edge.setDestination(new AnnisNode(longValue(resultSet, RANK_TABLE, "node_ref", tableAccessStrategy)));
    edge.setComponentID(longValue(resultSet, COMPONENT_TABLE, "id",
      tableAccessStrategy));
    edge.setId(longValue(resultSet, RANK_TABLE, "id", tableAccessStrategy));
    
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
    TableAccessStrategy tableAccessStrategy = outerQueryTableAccessStrategy;
    
    // function result
    List<AnnotationGraph> graphs =
      new LinkedList<>();

    // fn: match group -> annotation graph

    Map<List<Long>, AnnotationGraph> graphByMatchGroup =
      new HashMap<>();

    // fn: node id -> node
    Map<Long, AnnisNode> nodeById = new HashMap<>();

    // fn: edge pre order value -> edge
    Map<Long, Edge> edgeByRankID = new HashMap<>();
    
    // maps span that are continous to their coverage component
    Map<List<Long>, Map<Long, ComponentEntry>> keyToSpanToComponent 
      = new HashMap<>();
    
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
        
        log.debug("starting annotation graph for match: " + key);
        Map<Long, ComponentEntry> spans = new HashMap<>();
        AnnotationGraph graph = new AnnotationGraph();
        graphs.add(graph);
        graphByMatchGroup.put(key, graph);
        keyToSpanToComponent.put(key, spans);

        // clear mapping functions for this graph
        // assumes that the result set is sorted by key, pre
        nodeById.clear();
        edgeByRankID.clear();

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
      Map<Long, ComponentEntry> spanToComponent = keyToSpanToComponent.
        get(key);
      
      graph.setDocumentName(new DocumentNameMapRow().mapRow(
        resultSet, rowNum));

      Array path = resultSet.getArray("path");
      graph.setPath((String[]) path.getArray());

      // get node data
      AnnisNode node = mapNode(resultSet, tableAccessStrategy, spanToComponent);

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
      long rank_id = edge.getId();
      if (!edgeByRankID.containsKey(rank_id))
      {
        // fix source references in edge
        edge.setDestination(node);
        fixNodes(edge, edgeByRankID, nodeById);

        // add edge to src and dst nodes
        node.addIncomingEdge(edge);
        AnnisNode source = edge.getSource();
        if (source != null)
        {
          source.addOutgoingEdge(edge);
        }

        log.debug("new edge: " + edge);
        edgeByRankID.put(edge.getId(), edge);
        graph.addEdge(edge);
      }
      else
      {
        edge = edgeByRankID.get(rank_id);
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
    } // end for each row
    
    
    // remove edges from the graph with a source node inside the match
    for(Entry<List<Long>,AnnotationGraph> entry : graphByMatchGroup.entrySet())
    {
      AnnotationGraph graph = entry.getValue();
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
      
      Map<Long, ComponentEntry> spans = keyToSpanToComponent.get(entry.getKey());
      // filter out the continuous spans by finding all discontinuous spans
      // discontinuos spans will have a an entry for token
      
      createMissingSpanningRelations(graph, spans, nodeById);
      
    }

    return graphs;
  }
  
  private void createMissingSpanningRelations(AnnotationGraph graph,
    Map<Long, ComponentEntry> allSpans, Map<Long, AnnisNode> nodeById)
  { 
    for(Map.Entry<Long, ComponentEntry> spanEntry : allSpans.entrySet())
    {
      AnnisNode span = nodeById.get(spanEntry.getKey());
      
      // Check all covered token if there is already a coverage edge between the span
      // and the token. If at least one edge already exists, it must have been
      // a discontinuos span and we don't need to add any missing edges.
      boolean anyTokenConnected = false;
      for(long i=span.getLeftToken(); i <= span.getRightToken() && !anyTokenConnected; i++)
      {
        AnnisNode token = graph.getToken(i);
        // the span border might be behind the result set, so ignore this entries
        if(token != null)
        {
          for (Edge e : token.getIncomingEdges())
          {
            if (e.getSource() == span && e.getEdgeType() == EdgeType.COVERAGE)
            {
              anyTokenConnected = true;
              break;
            }
          }
        }
      }
     
      if(!anyTokenConnected)
      {
        long pre = 1;
        for(long i=span.getLeftToken(); i <= span.getRightToken(); i++)
        {
          AnnisNode tok = graph.getToken(i);
          if(tok != null)
          {
            Edge edge = new Edge();
            ComponentEntry component = spanEntry.getValue();

            edge.setPre(pre++);
            edge.setComponentID(component.getId());
            edge.setEdgeType(EdgeType.COVERAGE);
            edge.setNamespace(component.getNamespace());
            edge.setName(null);
            edge.setDestination(tok);
            edge.setSource(span);
            graph.addEdge(edge);
            span.addOutgoingEdge(edge);
            tok.addIncomingEdge(edge);
          }     
        }
      }
    } // end for each node
  }

  protected void fixNodes(Edge edge, Map<Long, Edge> edgeByRankID,
    Map<Long, AnnisNode> nodeById)
  {
    // pull source node from parent edge
    AnnisNode source = edge.getSource();
    if (source == null)
    {
      return;
    }
    long nodeID = source.getId();
    Edge parentEdge = edgeByRankID.get(nodeID);
    AnnisNode parent = parentEdge != null
      ? parentEdge.getDestination() : null;
    // log.debug("looking for node with rank.pre = 
    // " + pre + "; found: " + parent);
    edge.setSource(parent);

    // pull destination node from mapping function
    long destinationId = edge.getDestination().getId();
    edge.setDestination(nodeById.get(destinationId));
  }

  public TableAccessStrategy getOuterQueryTableAccessStrategy()
  {
    return outerQueryTableAccessStrategy;
  }

  public void setOuterQueryTableAccessStrategy(TableAccessStrategy outerQueryTableAccessStrategy)
  {
    this.outerQueryTableAccessStrategy = outerQueryTableAccessStrategy;
  }
  
  public static class ComponentEntry
  {
    
    private final long id;
    private final char type;
    private final String namespace;
    private final String name;

    public ComponentEntry(long id, char type, String namespace, String name)
    {
      this.id = id;
      this.type = type;
      this.namespace = namespace;
      this.name = name;
    }

    public long getId()
    {
      return id;
    }

    public char getType()
    {
      return type;
    }

    public String getNamespace()
    {
      return namespace;
    }

    public String getName()
    {
      return name;
    }

    @Override
    public String toString()
    {
      return "ComponentEntry{" + "id=" + id + ", type=" + type + ", namespace=" + namespace + ", name=" + name + '}';
    }

    
    
  }
  
  

}
