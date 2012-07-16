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
package annis.gui.visualizers.tree;

import annis.gui.visualizers.VisualizerInput;
import java.util.ArrayList;
import java.util.List;

import annis.model.AnnisNode;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

class AnnisGraphTools
{

  public static final String PRIMEDGE_SUBTYPE = "edge";
  public static final String SECEDGE_SUBTYPE = "secedge";

  public List<DirectedGraph<AnnisNode, Edge>> getSyntaxGraphs(
    VisualizerInput input)
  {
    AnnotationGraph ag = input.getResult().getGraph();
    String namespace = input.getMappings().getProperty("node_ns", input.
      getNamespace());
    List<DirectedGraph<AnnisNode, Edge>> resultGraphs =
      new ArrayList<DirectedGraph<AnnisNode, Edge>>();

    for (AnnisNode n : ag.getNodes())
    {
      if (isRootNode(n, namespace, input))
      {
        resultGraphs.add(extractGraph(ag, n, input));
      }
    }
    return resultGraphs;
  }

  private boolean copyNode(DirectedGraph<AnnisNode, Edge> graph, AnnisNode n,
    VisualizerInput input)
  {
    boolean addToGraph = n.isToken();
    for (Edge e : n.getOutgoingEdges())
    {
      if (includeEdge(e, input) && copyNode(graph, e.getDestination(), input))
      {
        addToGraph |= true;
        graph.addEdge(e, n, e.getDestination());
      }
    }
    if (addToGraph)
    {
      graph.addVertex(n);
    }
    return addToGraph;
  }

  private boolean isRootNode(AnnisNode n, String namespace,
    VisualizerInput input)
  {
    if (!n.getNamespace().equals(namespace))
    {
      return false;
    }
    for (Edge e : n.getIncomingEdges())
    {
      if (hasEdgeSubtype(e, AnnisGraphTools.PRIMEDGE_SUBTYPE, input) && e.
        getSource()
        != null)
      {
        return false;
      }
    }
    return true;
  }

  private DirectedGraph<AnnisNode, Edge> extractGraph(AnnotationGraph ag,
    AnnisNode n, VisualizerInput input)
  {
    DirectedGraph<AnnisNode, Edge> graph =
      new DirectedSparseGraph<AnnisNode, Edge>();
    copyNode(graph, n, input);
    for (Edge e : ag.getEdges())
    {
      if (hasEdgeSubtype(e, AnnisGraphTools.SECEDGE_SUBTYPE, input) && graph.
        containsVertex(e.getDestination())
        && graph.containsVertex(e.getSource()))
      {
        graph.addEdge(e, e.getSource(), e.getDestination());
      }
    }
    return graph;
  }

  private boolean includeEdge(Edge e, VisualizerInput input)
  {
    return hasEdgeSubtype(e, AnnisGraphTools.PRIMEDGE_SUBTYPE, input);
  }

  public static boolean hasEdgeSubtype(Edge e, String edgeSubtype,
    VisualizerInput input)
  {
    String name = e.getName();

    if (PRIMEDGE_SUBTYPE.equals(edgeSubtype))
    {
      edgeSubtype = input.getMappings().getProperty("edge") != null
        ? input.getMappings().getProperty("edge") : PRIMEDGE_SUBTYPE;
    }

    if (SECEDGE_SUBTYPE.equals(edgeSubtype))
    {
      edgeSubtype = input.getMappings().getProperty("secedge") != null
        ? input.getMappings().getProperty("secedge") : SECEDGE_SUBTYPE;
    }

    return e.getEdgeType() == Edge.EdgeType.DOMINANCE && name != null && name.
      equals(edgeSubtype);
  }

  public static HorizontalOrientation detectLayoutDirection(AnnotationGraph ag)
  {
    int withHebrew = 0;
    for (AnnisNode token : ag.getTokens())
    {
      if (isHebrewToken(token.getSpannedText()))
      {
        withHebrew += 1;
      }
    }
    return (withHebrew > ag.getTokens().size() / 3)
      ? HorizontalOrientation.RIGHT_TO_LEFT
      : HorizontalOrientation.LEFT_TO_RIGHT;
  }

  private static boolean isHebrewToken(String text)
  {
    for (int i = 0; i < text.length(); ++i)
    {
      char c = text.charAt(i);
      if ((c >= 0x0590 && c <= 0x06f9) || (c >= 0xfb1e && c <= 0xfdff) || (c
        >= 0xfe70 && c <= 0xfeff))
      {
        return true;
      }
    }
    return false;
  }
}