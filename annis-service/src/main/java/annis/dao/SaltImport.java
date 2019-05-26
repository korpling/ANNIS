/*
 * Copyright 2016 Thomas Krause.
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
package annis.dao;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.corpus_tools.graphannis.GraphUpdate;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SDominanceRelation;
import org.corpus_tools.salt.common.SPointingRelation;
import org.corpus_tools.salt.common.SStructure;
import org.corpus_tools.salt.common.SStructuredNode;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.eclipse.emf.common.util.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

/**
 * A class which helps to import salt documents into graphANNIS.
 *
 * @author Thomas Krause <thomaskrause@posteo.de>
 */
public class SaltImport {

  public static final String ANNIS_NS = "annis";

  private static final Logger log = LoggerFactory.getLogger(SaltImport.class);

  private final GraphUpdate updateList = new GraphUpdate();

  public GraphUpdate finish() {
    return updateList;
  }

  public SaltImport map(SDocumentGraph g) {
    
    // create the (sub-) corpus and the document nodes
    URI docPath = g.getPath();
    String documentNodeName = null;
    if(docPath != null) {
      String[] segments = docPath.segments();
      if(segments != null) {
        for(int i=0; i < segments.length; i++) {
          String nodeName = Joiner.on('/').join(Arrays.copyOfRange(segments, 0, i+1));
          updateList.addNode(nodeName, "corpus");
          updateList.addNodeLabel(nodeName, "annis", "doc", segments[i]);
          if(i == segments.length-1) {
            documentNodeName = nodeName;
          }
        }
      }      
    }
    
    // add all nodes and their annotations
    for (SNode n : g.getNodes()) {
      addNode(n, documentNodeName);
    }

    addTokenInformation(g);

    for (SNode n : g.getNodes()) {
      addCoverageInformation(n, g);
    }

    for (SDominanceRelation rel : g.getDominanceRelations()) {
      String sourceName = nodeName(rel.getSource());
      String targetName = nodeName(rel.getTarget());

      for (String l : getLayerNames(rel.getLayers())) {
        // add an edge both for the named component and for the un-named
        if (rel.getType() != null) {
          updateList.addEdge(sourceName, targetName, l, "Dominance", rel.getType());
          addEdgeLabels(rel, l, "Dominance", rel.getType());
        }
        updateList.addEdge(sourceName, targetName, l, "Dominance", "");
        addEdgeLabels(rel, l, "Dominance", "");
      }
    }

    for (SPointingRelation rel : g.getPointingRelations()) {
      String sourceName = nodeName(rel.getSource());
      String targetName = nodeName(rel.getTarget());

      for (String l : getLayerNames(rel.getLayers())) {
        // add an edge both for the named component (or "null" if not named)
        updateList.addEdge(sourceName, targetName, l, "Pointing", "" + rel.getType());
        addEdgeLabels(rel, l, "Pointing", "" + rel.getType());
      }
    }

    return this;
  }

  private void addTokenInformation(SDocumentGraph g) {

    SToken lastToken = null;
    STextualDS lastTextDS = null;

    List<SToken> sortedToken = g.getSortedTokenByText();

    if (sortedToken != null) {
      for (SToken t : sortedToken) {

        String nodeName = nodeName(t);
        // each token must have it's spanned text as label
        updateList.addNodeLabel(nodeName, ANNIS_NS, "tok", g.getText(t));

        STextualDS textDS = getTextForToken(t);
        if (lastToken != null && textDS == lastTextDS) {
          // add an explicit ORDERING edge between the token
          updateList.addEdge(nodeName(lastToken), nodeName, ANNIS_NS, "Ordering", "");
        }
        lastToken = t;
        lastTextDS = textDS;
      }
    }
  }

  /**
  * Add edges related to coverage.
  *
  * This will add the LEFT_TOKEN, RIGHT_TOKEN, COVERAGE and INVERSE_COVERAGE edges.
  * 
  * @param node
  * @param graph
  */
  private void addCoverageInformation(SNode node, SDocumentGraph graph) {
    List<SToken> overlappedToken;
    if (node instanceof SToken) {
      overlappedToken = Arrays.asList((SToken) node);
    } else if (node instanceof SStructure) {
      overlappedToken = graph.getOverlappedTokens(node, SALT_TYPE.SSPANNING_RELATION, SALT_TYPE.SDOMINANCE_RELATION);
    } else if (node instanceof STextualDS) {
      // ignore
      return;
    } else {
      overlappedToken = graph.getOverlappedTokens(node, SALT_TYPE.SSPANNING_RELATION);
    }
    if (overlappedToken.isEmpty()) {
      log.warn("Node {} is not connected to any token. This is invalid for graphANNIS and the node will be excluded.",
          node.getId());
      return;
    }

    String name = nodeName(node);


    // add the COVERAGE edges
    for (SToken covered : overlappedToken) {
      updateList.addEdge(name, nodeName(covered), ANNIS_NS, "Coverage", "");
    }

  }


  private static String documentPath(SNode node) {
    if (node != null) {
      String[] segments = node.getPath().segments();
      if (segments.length > 0) {
        return Joiner.on("/").join(segments);
      }
    }

    return null;
  }

  private static String nodeName(SNode node) {
    if (node != null) {
      String path = documentPath(node);
      return path == null ? "#" + node.getPath().fragment() : path + "#" + node.getPath().fragment();
    } else {
      return null;
    }
  }

  private static STextualDS getTextForToken(SToken t) {
    List<SRelation> out = t.getOutRelations();
    if (out != null) {
      for (SRelation<?, ?> rel : out) {
        if (rel instanceof STextualRelation) {
          return ((STextualRelation) rel).getTarget();
        }
      }
    }
    return null;
  }

  private static Set<String> getLayerNames(Set<SLayer> layers) {
    Set<String> result = new LinkedHashSet<>();

    if (layers == null || layers.isEmpty()) {
      // add the edge to the default empty layer
      result.add("");
    } else {
      for (SLayer l : layers) {
        result.add(l.getName());
      }
    }

    return result;
  }

  private void addNode(SNode n, String documentNodeName) {
    if (n instanceof SStructuredNode) {
      // use the unique name
      String name = nodeName(n);
      updateList.addNode(name);
      // add all annotations
      for (SAnnotation anno : n.getAnnotations()) {
        updateList.addNodeLabel(name, anno.getNamespace(), anno.getName(), anno.getValue_STEXT());
      }
      
      // add connection to document node if available
      if(documentNodeName != null) {
        updateList.addEdge(name, documentNodeName, "annis", "PartOf", "");
      }
    }
  }

  private void addEdgeLabels(SRelation<?, ?> rel, String layer, String componentType, String componentName) {
    Set<SAnnotation> annos = rel.getAnnotations();
    if (annos != null) {
      for (SAnnotation anno : annos) {
        updateList.addEdgeLabel(nodeName(rel.getSource()), nodeName(rel.getTarget()), layer, componentType,
            componentName, anno.getNamespace(), anno.getName(), anno.getValue_STEXT());
      }
    }
  }

}
