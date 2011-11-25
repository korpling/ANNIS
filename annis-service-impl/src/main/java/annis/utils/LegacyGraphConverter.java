/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.utils;

import annis.model.AnnisConstants;
import annis.model.AnnisNode;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import annis.model.Edge.EdgeType;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Node;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDominanceRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SPointingRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpanningRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SFeature;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SProcessingAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.NotImplementedException;

/**
 * This class can convert the current Salt graph model into the legacy model 
 *  AOM (Annis Object Model)
 *  and
 *  "PaulaInline"
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class LegacyGraphConverter
{

  public static List<AnnotationGraph> convertToAOM(SaltProject p)
  {
    List<AnnotationGraph> result = new ArrayList<AnnotationGraph>();

    for (SCorpusGraph corpusGraph : p.getSCorpusGraphs())
    {
      Long[] matchedIDs = new Long[0];
      SFeature featMatchedIDs = corpusGraph.getSFeature("annis",
        AnnisConstants.FEAT_MATCHEDIDS);
      if (featMatchedIDs != null && featMatchedIDs.getSValueSTEXT() != null)
      {
        matchedIDs = Utils.split2Long(featMatchedIDs.getSValueSTEXT(), ',');
      }
      if (corpusGraph.getSDocuments().size() > 0)
      {
        SDocument doc = corpusGraph.getSDocuments().get(0);
        SDocumentGraph docGraph = doc.getSDocumentGraph();

        result.add(convertToAnnotationGraph(docGraph, matchedIDs));
      }
    }

    throw new NotImplementedException();
    // return result;
  }

  public static AnnotationGraph convertToAnnotationGraph(SDocumentGraph docGraph,
    Long[] matchedIDs)
  {
    Set<Long> matchSet = new HashSet<Long>(Arrays.asList(matchedIDs));
    AnnotationGraph annoGraph = new AnnotationGraph();

    annoGraph.setDocumentName(docGraph.getSDocument().getSName());

    Map<Node, AnnisNode> allNodes = new HashMap<Node, AnnisNode>();

    for (SNode sNode : docGraph.getSNodes())
    {
      SProcessingAnnotation procAnno =
        sNode.getSProcessingAnnotation(AnnisConstants.NAMESPACE + "::"
        + AnnisConstants.PROC_INTERNALID);
      if (procAnno != null)
      {
        long internalID = procAnno.getSValueSNUMERIC();
        AnnisNode aNode = new AnnisNode(internalID);

        // TODO: add annotations
        // TODO: what else to add to node?

        annoGraph.addNode(aNode);
        allNodes.put(sNode, aNode);
      }
    }

    for (SRelation rel : docGraph.getSDominanceRelations())
    {
      Edge aEdge = new Edge();
      aEdge.setSource(allNodes.get(rel.getSource()));
      aEdge.setDestination(allNodes.get(rel.getTarget()));

      aEdge.setEdgeType(EdgeType.UNKNOWN);
      aEdge.setPre(rel.getSProcessingAnnotation(AnnisConstants.NAMESPACE + ":"
        + AnnisConstants.PROC_INTERNALID).getSValueSNUMERIC());

      // TODO: use layer for namespace

      if (rel instanceof SDominanceRelation)
      {
        aEdge.setEdgeType(EdgeType.DOMINANCE);
      }
      else if (rel instanceof SPointingRelation)
      {
        aEdge.setEdgeType(EdgeType.POINTING_RELATION);
      }
      else if (rel instanceof SSpanningRelation)
      {
        aEdge.setEdgeType(EdgeType.COVERAGE);
      }

      // TODO: add annotations
    }

    return annoGraph;
  }

  public static String convertToPaulaInline(SCorpusGraph g)
  {
    throw new NotImplementedException();
  }
}
