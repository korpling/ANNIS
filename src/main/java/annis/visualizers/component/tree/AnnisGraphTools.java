/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package annis.visualizers.component.tree;

import annis.gui.AnnisUI;
import annis.libgui.Helper;
import annis.libgui.visualizers.VisualizerInput;
import com.vaadin.ui.UI;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
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

public class AnnisGraphTools implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -1478561641149654759L;
  private static final String PRIMEDGE_SUBTYPE = "edge";
  private static final String SECEDGE_SUBTYPE = "secedge";

  public static HorizontalOrientation detectLayoutDirection(SDocumentGraph docGraph, UI ui) {
    if (ui instanceof AnnisUI && ((AnnisUI) ui).getConfig().isDisableRTL()) {
      return HorizontalOrientation.LEFT_TO_RIGHT;
    }

    int withhRTL = 0;
    for (SToken token : docGraph.getTokens()) {
      if (Helper.containsRTLText(docGraph.getText(token))) {
        withhRTL += 1;
      }
    }
    return (withhRTL > docGraph.getTokens().size() / 3) ? HorizontalOrientation.RIGHT_TO_LEFT
        : HorizontalOrientation.LEFT_TO_RIGHT;
  }

  public static String extractAnnotation(Set<SAnnotation> annotations, String namespace,
      String featureName) {
    if (annotations != null) {
      for (SAnnotation a : annotations) {
        if (namespace == null) {
          if (a.getName().equals(featureName)) {
            return a.getValue_STEXT();
          }
        } else {
          if (a.getNamespace().equals(namespace) && a.getName().equals(featureName)) {
            return a.getValue_STEXT();
          }
        }
      }
    }
    return null;
  }

  public static boolean isTerminal(SNode n, VisualizerInput input) {
    String terminalName = (input == null ? null
        : input.getMappings().get(TigerTreeVisualizer.TERMINAL_NAME_KEY));

    if (terminalName == null) {
      return n instanceof SToken;
    } else {
      String terminalNamespace = (input == null ? null
          : input.getMappings().get(TigerTreeVisualizer.TERMINAL_NS_KEY));

      SAnnotation anno = n.getAnnotation(terminalNamespace, terminalName);

      return anno != null;
    }
  }

  private final VisualizerInput input;


  public AnnisGraphTools(VisualizerInput input) {
    this.input = input;
  }

  private boolean copyNode(DirectedGraph<SNode, SRelation> graph, SNode n, String terminalNamespace,
      String terminalName) {
    boolean addToGraph = AnnisGraphTools.isTerminal(n, input);

    if (!addToGraph) {
      for (SRelation<? extends SNode, ? extends SNode> rel : n.getOutRelations()) {
        if (includeEdge(rel) && copyNode(graph, rel.getTarget(), terminalNamespace, terminalName)) {
          addToGraph |= true;
          graph.addEdge(rel, n, rel.getTarget());
        }
      }
    }

    if (addToGraph) {
      graph.addVertex(n);
    }
    return addToGraph;
  }

  private DirectedGraph<SNode, SRelation> extractGraph(SDocumentGraph docGraph, SNode n,
      String terminalNamespace, String terminalName) {
    DirectedGraph<SNode, SRelation> graph = new DirectedSparseGraph<>();
    copyNode(graph, n, terminalNamespace, terminalName);
    for (SDominanceRelation rel : docGraph.getDominanceRelations()) {
      if (hasEdgeSubtype(rel, getSecEdgeSubType()) && graph.containsVertex(rel.getTarget())
          && graph.containsVertex(rel.getSource())) {
        graph.addEdge(rel, rel.getSource(), rel.getTarget());
      }
    }
    return graph;
  }

  /**
   * Gets the edge type and takes into account the user defined mappings.
   *
   * @return the name of the edge type. Is never null.
   */
  public String getPrimEdgeSubType() {
    return input.getMappings().getOrDefault("edge_type", PRIMEDGE_SUBTYPE);
  }

  /**
   * Gets the secedge type and takes into account the user defined mappings.
   *
   * @return the name of the secedge type. Is never null.
   */
  public String getSecEdgeSubType() {
    return input.getMappings().getOrDefault("secedge_type", SECEDGE_SUBTYPE);
  }

  public List<DirectedGraph<SNode, SRelation>> getSyntaxGraphs() {
    final SDocumentGraph docGraph = input.getDocument().getDocumentGraph();
    String namespace = input.getMappings().getOrDefault("node_ns", input.getNamespace());
    String terminalName = input.getMappings().get(TigerTreeVisualizer.TERMINAL_NAME_KEY);
    String terminalNamespace = input.getMappings().get(TigerTreeVisualizer.TERMINAL_NS_KEY);

    List<DirectedGraph<SNode, SRelation>> resultGraphs = new ArrayList<>();

    List<SNode> rootNodes = new LinkedList<>();
    for (SNode n : docGraph.getRootsByRelation(SALT_TYPE.SDOMINANCE_RELATION)) {
      if (Helper.checkSLayer(namespace, n)) {
        rootNodes.add(n);
      }
    }

    // sort root nodes according to their left-most covered token
    HorizontalOrientation orientation = detectLayoutDirection(docGraph, input.getUI());
    if (orientation == HorizontalOrientation.LEFT_TO_RIGHT) {
      Collections.sort(rootNodes, (o1, o2) -> {
        DataSourceSequence seq1 = docGraph
            .getOverlappedDataSourceSequence(o1, SALT_TYPE.STEXT_OVERLAPPING_RELATION).get(0);
        DataSourceSequence seq2 = docGraph
            .getOverlappedDataSourceSequence(o2, SALT_TYPE.STEXT_OVERLAPPING_RELATION).get(0);
        return Long.compare(seq1.getStart().longValue(), seq2.getStart().longValue());
      });
    } else if (orientation == HorizontalOrientation.RIGHT_TO_LEFT) {
      Collections.sort(rootNodes, (o1, o2) -> {
        DataSourceSequence seq1 = docGraph
            .getOverlappedDataSourceSequence(o1, SALT_TYPE.STEXT_OVERLAPPING_RELATION).get(0);
        DataSourceSequence seq2 = docGraph
            .getOverlappedDataSourceSequence(o2, SALT_TYPE.STEXT_OVERLAPPING_RELATION).get(0);

        return Long.compare(seq2.getStart().longValue(), seq1.getStart().longValue());
      });
    }
    for (SNode r : rootNodes) {
      resultGraphs.add(extractGraph(docGraph, r, terminalNamespace, terminalName));
    }

    return resultGraphs;
  }

  public boolean hasEdgeSubtype(SRelation rel, String edgeSubtype) {
    String type = rel.getType();

    if (getPrimEdgeSubType().equals(edgeSubtype)) {
      edgeSubtype =
          input.getMappings().get("edge") != null ? input.getMappings().get("edge")
              : getPrimEdgeSubType();
    }

    if (getSecEdgeSubType().equals(edgeSubtype)) {
      edgeSubtype = input.getMappings().get("secedge") != null ? input.getMappings().get("secedge")
          : getSecEdgeSubType();
    }

    boolean result =
        rel instanceof SDominanceRelation && ((type == null && "null".equals(edgeSubtype))
            || (type != null && type.equals(edgeSubtype)));
    return result;
  }

  private boolean includeEdge(SRelation e) {
    boolean result = hasEdgeSubtype(e, getPrimEdgeSubType());
    return result;
  }
}
