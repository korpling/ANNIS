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
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SDominanceRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.util.DataSourceSequence;

public class AnnisGraphTools implements Serializable
{

  private static final String PRIMEDGE_SUBTYPE = "edge";
  private static final String SECEDGE_SUBTYPE = "secedge";
  private final VisualizerInput input;

  public AnnisGraphTools(VisualizerInput input)
  {
      this.input = input;
  }

  public List<DirectedGraph<SNode, SRelation>> getSyntaxGraphs()
  {
    final SDocumentGraph docGraph = input.getDocument().getDocumentGraph();
    String namespace = input.getMappings().getProperty("node_ns", input.
      getNamespace());
    String terminalName =  input.getMappings().getProperty(
      TigerTreeVisualizer.TERMINAL_NAME_KEY);
    String terminalNamespace =  input.getMappings().getProperty(
      TigerTreeVisualizer.TERMINAL_NS_KEY);
    
    List<DirectedGraph<SNode, SRelation>> resultGraphs = new ArrayList<>();

    List<SNode> rootNodes = new LinkedList<>();
    for(SNode n : docGraph.getRootsByRelation(SALT_TYPE.SDOMINANCE_RELATION))
    {
      if(CommonHelper.checkSLayer(namespace, n))
      {
        rootNodes.add(n);
      }
    }
    
    //sort root nodes according to their left-most covered token
    HorizontalOrientation orientation = detectLayoutDirection(docGraph);
    if (orientation == HorizontalOrientation.LEFT_TO_RIGHT)
    {
      Collections.sort(rootNodes, new Comparator<SNode>()
      {
        @Override
        public int compare(SNode o1, SNode o2)
        {
          DataSourceSequence seq1 =  docGraph.getOverlappedDataSourceSequence(o1, SALT_TYPE.STEXT_OVERLAPPING_RELATION).get(0);
          DataSourceSequence seq2 =  docGraph.getOverlappedDataSourceSequence(o2, SALT_TYPE.STEXT_OVERLAPPING_RELATION).get(0);
          return Long.compare(seq1.getStart().longValue(), seq2.getStart().longValue());
        }
      }
      );
    }
    else if (orientation == HorizontalOrientation.RIGHT_TO_LEFT)
    {
      Collections.sort(rootNodes, new Comparator<SNode>()
      {
        @Override
        public int compare(SNode o1, SNode o2)
        {
          DataSourceSequence seq1 =  docGraph.getOverlappedDataSourceSequence(o1, SALT_TYPE.STEXT_OVERLAPPING_RELATION).get(0);
          DataSourceSequence seq2 =  docGraph.getOverlappedDataSourceSequence(o2, SALT_TYPE.STEXT_OVERLAPPING_RELATION).get(0);
          
          return Long.compare(seq2.getStart().longValue(), seq1.getStart().longValue());
        }
      }
      );
    }
    for(SNode r : rootNodes)
    {
      resultGraphs.add(extractGraph(docGraph, r, terminalNamespace, terminalName));
    }
    
    return resultGraphs;
  }

  private boolean copyNode(DirectedGraph<SNode, SRelation> graph, SNode n,
     String terminalNamespace, String terminalName)
  {
    boolean addToGraph = AnnisGraphTools.isTerminal(n, input);
    
    if(!addToGraph)
    {
      for (SRelation<? extends SNode, ? extends SNode> rel : n.getOutRelations())
      {
        if (includeEdge(rel) && copyNode(graph, rel.getTarget(), terminalNamespace, terminalName))
        {
          addToGraph |= true;
          graph.addEdge(rel, n, rel.getTarget());
        }
      }
    }
    
    if (addToGraph)
    {
      graph.addVertex(n);
    }
    return addToGraph;
  }

  
  public static boolean isTerminal(SNode n, VisualizerInput input)
  {
    String terminalName = (input == null ? null : input.getMappings().getProperty(
      TigerTreeVisualizer.TERMINAL_NAME_KEY));
    
    if(terminalName == null)
    {
      return n instanceof SToken;
    }
    else
    {
      String terminalNamespace = (input == null ? null : input.getMappings().getProperty(
        TigerTreeVisualizer.TERMINAL_NS_KEY));

      SAnnotation anno = n.getAnnotation(terminalNamespace, terminalName);
      
      return anno != null;
    }
  }
  private DirectedGraph<SNode, SRelation> extractGraph(SDocumentGraph docGraph,
    SNode n, String terminalNamespace, String terminalName)
  {
    DirectedGraph<SNode, SRelation> graph
      = new DirectedSparseGraph<>();
    copyNode(graph, n, terminalNamespace, terminalName);
    for (SDominanceRelation rel : docGraph.getDominanceRelations())
    {
      if (hasEdgeSubtype(rel, getSecEdgeSubType()) && graph.
        containsVertex(rel.getTarget())
        && graph.containsVertex(rel.getSource()))
      {
        graph.addEdge(rel, rel.getSource(), rel.getTarget());
      }
    }
    return graph;
  }

  private boolean includeEdge(SRelation e)
  {
    boolean result = hasEdgeSubtype(e, getPrimEdgeSubType());
    return result;
  }

  public boolean hasEdgeSubtype(SRelation rel, String edgeSubtype)
  {
    String type = rel.getType();

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
      rel instanceof SDominanceRelation &&
      (
        (type == null && "null".equals(edgeSubtype)) 
        || (type != null && type.equals(edgeSubtype))
      );
    return result; 
  }

  public static HorizontalOrientation detectLayoutDirection(SDocumentGraph docGraph)
  {
    if(Helper.isRTLDisabled())
    {
      return HorizontalOrientation.LEFT_TO_RIGHT;
    }
    
    int withhRTL = 0;
    for (SToken token : docGraph.getTokens())
    {
      if (CommonHelper.containsRTLText(docGraph.getText(token)))
      {
        withhRTL += 1;
      }
    }
    return (withhRTL > docGraph.getTokens().size() / 3)
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
  
  public static String extractAnnotation(Set<SAnnotation> annotations,
    String namespace, String featureName)
  {
    if(annotations != null)
    {
      for (SAnnotation a : annotations)
      {
        if(namespace == null)
        {
          if (a.getName().equals(featureName))
          {
            return a.getValue_STEXT();
          }
        }
        else
        {
          if (a.getNamespace().equals(namespace) && a.getName().equals(featureName))
          {
            return a.getValue_STEXT();
          }
        }
      }
    }
    return null;
  }
}