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
package annis.visualizers.component.tree;

import annis.CommonHelper;
import annis.libgui.Helper;
import annis.libgui.visualizers.VisualizerInput;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class AnnisGraphTools implements Serializable
{

  private static final String PRIMEDGE_SUBTYPE = "edge";
  private static final String SECEDGE_SUBTYPE = "secedge";
  private final VisualizerInput input;

  public AnnisGraphTools(VisualizerInput input)
  {
      this.input = input;
  }

  public List<DirectedGraph<AnnisNode, Edge>> getSyntaxGraphs()
  {
    AnnotationGraph ag = input.getResult().getGraph();
    String namespace = input.getMappings().getProperty("node_ns", input.
      getNamespace());
    String terminalName =  input.getMappings().getProperty(
      TigerTreeVisualizer.TERMINAL_NAME_KEY);
    String terminalNamespace =  input.getMappings().getProperty(
      TigerTreeVisualizer.TERMINAL_NS_KEY);
    
    List<DirectedGraph<AnnisNode, Edge>> resultGraphs = new ArrayList<>();

    List<AnnisNode> rootNodes = new LinkedList<>();
    
    for (AnnisNode n : ag.getNodes())
    {
      if (isRootNode(n, namespace))
      {
        rootNodes.add(n);
      }
    }
    
    //sort root nodes according to their left-most covered token
    HorizontalOrientation orientation = detectLayoutDirection(ag);
    if (orientation == HorizontalOrientation.LEFT_TO_RIGHT)
    {
      Collections.sort(rootNodes, new Comparator<AnnisNode>()
      {
        @Override
        public int compare(AnnisNode o1, AnnisNode o2)
        {
          return Long.compare(o1.getLeftToken(), o2.getLeftToken());
        }
      }
      );
    }
    else if (orientation == HorizontalOrientation.RIGHT_TO_LEFT)
    {
      Collections.sort(rootNodes, new Comparator<AnnisNode>()
      {
        @Override
        public int compare(AnnisNode o1, AnnisNode o2)
        {
          return Long.compare(o2.getLeftToken(), o1.getLeftToken());
        }
      }
      );
    }
    for(AnnisNode r : rootNodes)
    {
      resultGraphs.add(extractGraph(ag, r, terminalNamespace, terminalName));
    }
    
    return resultGraphs;
  }

  private boolean copyNode(DirectedGraph<AnnisNode, Edge> graph, AnnisNode n,
     String terminalNamespace, String terminalName)
  {
    boolean addToGraph = AnnisGraphTools.isTerminal(n, input);
    
    if(!addToGraph)
    {
      for (Edge e : n.getOutgoingEdges())
      {
        if (includeEdge(e) && copyNode(graph, e.getDestination(), terminalNamespace, terminalName))
        {
          addToGraph |= true;
          graph.addEdge(e, n, e.getDestination());
        }
      }
    }
    
    if (addToGraph)
    {
      graph.addVertex(n);
    }
    return addToGraph;
  }

  private boolean isRootNode(AnnisNode n, String namespace)
  {
    if (!n.getNamespace().equals(namespace))
    {
      return false;
    }
    for (Edge e : n.getIncomingEdges())
    {
      if (hasEdgeSubtype(e, getPrimEdgeSubType()) && e.getSource()
        != null)
      {
        return false;
      }
    }
    return true;
  }
  
  public static boolean isTerminal(AnnisNode n, VisualizerInput input)
  {
    String terminalName = (input == null ? null : input.getMappings().getProperty(
      TigerTreeVisualizer.TERMINAL_NAME_KEY));
    
    if(terminalName == null)
    {
      return n.isToken();
    }
    else
    {
      String terminalNamespace = (input == null ? null : input.getMappings().getProperty(
        TigerTreeVisualizer.TERMINAL_NS_KEY));

      String annoValue = extractAnnotation(n.getNodeAnnotations(),
        terminalNamespace,
        terminalName);
      
      return annoValue != null;
    }
  }
  private DirectedGraph<AnnisNode, Edge> extractGraph(AnnotationGraph ag,
    AnnisNode n, String terminalNamespace, String terminalName)
  {
    DirectedGraph<AnnisNode, Edge> graph
      = new DirectedSparseGraph<AnnisNode, Edge>();
    copyNode(graph, n, terminalNamespace, terminalName);
    for (Edge e : ag.getEdges())
    {
      if (hasEdgeSubtype(e, getSecEdgeSubType()) && graph.
        containsVertex(e.getDestination())
        && graph.containsVertex(e.getSource()))
      {
        graph.addEdge(e, e.getSource(), e.getDestination());
      }
    }
    return graph;
  }

  private boolean includeEdge(Edge e)
  {
    boolean result = hasEdgeSubtype(e, getPrimEdgeSubType());
    return result;
  }

  public boolean hasEdgeSubtype(Edge e, String edgeSubtype)
  {
    String name = e.getName();

    if (getPrimEdgeSubType().equals(edgeSubtype))
    {
      edgeSubtype = input.getMappings().getProperty("edge") != null
        ? input.getMappings().getProperty("edge") : getPrimEdgeSubType();
    }

    if (getSecEdgeSubType().equals(edgeSubtype))
    {
      edgeSubtype = input.getMappings().getProperty("secedge") != null
        ? input.getMappings().getProperty("secedge") : getSecEdgeSubType();
    }
    
    boolean result = 
      e.getEdgeType() == Edge.EdgeType.DOMINANCE && 
      (
        (name == null && "null".equals(edgeSubtype)) 
        || (name != null && name.equals(edgeSubtype))
      );
    return result; 
  }

  public static HorizontalOrientation detectLayoutDirection(AnnotationGraph ag)
  {
    if(Helper.isRTLDisabled())
    {
      return HorizontalOrientation.LEFT_TO_RIGHT;
    }
    
    int withhRTL = 0;
    for (AnnisNode token : ag.getTokens())
    {
      if (CommonHelper.containsRTLText(token.getSpannedText()))
      {
        withhRTL += 1;
      }
    }
    return (withhRTL > ag.getTokens().size() / 3)
      ? HorizontalOrientation.RIGHT_TO_LEFT
      : HorizontalOrientation.LEFT_TO_RIGHT;
  }

  /**
   * Gets the edge type and takes into account the user defined mappings.
   *
   * @return the name of the edge type. Is never null.
   */
  public String getPrimEdgeSubType()
  {
    return input.getMappings().getProperty("edge_type", PRIMEDGE_SUBTYPE);
  }

  /**
   * Gets the secedge type and takes into account the user defined mappings.
   *
   * @return the name of the secedge type. Is never null.
   */
  public String getSecEdgeSubType()
  {
    return input.getMappings().getProperty("secedge_type", SECEDGE_SUBTYPE);
  }
  
  public static String extractAnnotation(Set<Annotation> annotations,
    String namespace, String featureName)
  {
    if(annotations != null)
    {
      for (Annotation a : annotations)
      {
        if(namespace == null)
        {
          if (a.getName().equals(featureName))
          {
            return a.getValue();
          }
        }
        else
        {
          if (a.getNamespace().equals(namespace) && a.getName().equals(featureName))
          {
            return a.getValue();
          }
        }
      }
    }
    return null;
  }
}