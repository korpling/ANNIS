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

import annis.service.objects.AnnisResultImpl;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import annis.model.Edge.EdgeType;
import annis.service.ifaces.AnnisResultSet;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Node;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDataSourceSequence;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDominanceRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SPointingRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpanningRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STYPE_NAME;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SFeature;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static annis.model.AnnisConstants.*;
import annis.service.objects.AnnisResultSetImpl;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;
import java.util.LinkedList;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;

/**
 * This class can convert the current Salt graph model into the legacy model 
 *  AOM (Annis Object Model)
 *  and
 *  "PaulaInline"
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class LegacyGraphConverter
{

  public static AnnisResultSet convertToResultSet(SaltProject p)
  {
    List<AnnotationGraph> annotationGraphs = convertToAOM(p);
    AnnisResultSetImpl annisResultSet = new AnnisResultSetImpl();
    for (AnnotationGraph annotationGraph : annotationGraphs)
    {
      annisResultSet.add(new AnnisResultImpl(annotationGraph));
    }
    return annisResultSet;
  }

  public static List<AnnotationGraph> convertToAOM(SaltProject p)
  {
    List<AnnotationGraph> result = new ArrayList<AnnotationGraph>();

    for (SCorpusGraph corpusGraph : p.getSCorpusGraphs())
    {
      for (SDocument doc : corpusGraph.getSDocuments())
      {
        result.add(convertToAnnotationGraph(doc));
      }
    }

    return result;
  }

  public static AnnotationGraph convertToAnnotationGraph(SDocument document)
  {
    List<String> matchedIDs = new ArrayList<String>();
    SFeature featMatchedIDs = document.getSFeature(ANNIS_NS,
      FEAT_MATCHEDIDS);
    if (featMatchedIDs != null && featMatchedIDs.getSValueSTEXT() != null)
    {
      matchedIDs =
        Arrays.asList(StringUtils.split(featMatchedIDs.getSValueSTEXT(), ','));
    }
    SDocumentGraph docGraph = document.getSDocumentGraph();
    AnnotationGraph result = convertToAnnotationGraph(docGraph, matchedIDs);

    return result;
  }

  public static AnnotationGraph convertToAnnotationGraph(SDocumentGraph docGraph,
    List<String> matchedIDs)
  {
    Set<String> matchSet = new HashSet<String>(matchedIDs);
    AnnotationGraph annoGraph = new AnnotationGraph();

    annoGraph.setDocumentName(docGraph.getSDocument().getSName());

    Map<Node, AnnisNode> allNodes = new HashMap<Node, AnnisNode>();

    for (SNode sNode : docGraph.getSNodes())
    {
      SFeature featInternalID =
        sNode.getSFeature(ANNIS_NS, FEAT_INTERNALID);
      if (featInternalID != null)
      {
        long internalID = featInternalID.getSValueSNUMERIC();
        AnnisNode aNode = new AnnisNode(internalID);

        for (SAnnotation sAnno : sNode.getSAnnotations())
        {
          aNode.addNodeAnnotation(new Annotation(sAnno.getSNS(),
            sAnno.getSName(),
            sAnno.getSValueSTEXT()));
        }
        aNode.setName(sNode.getSName());
        aNode.setNamespace(sNode.getSLayers().get(0).getSName());

        if (sNode instanceof SToken)
        {
          BasicEList<STYPE_NAME> textualRelation = new BasicEList<STYPE_NAME>();
          textualRelation.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);
          EList<SDataSourceSequence> seqList =
            docGraph.getOverlappedDSSequences(sNode,
            textualRelation);
          if (seqList != null)
          {
            SDataSourceSequence seq = seqList.get(0);
            aNode.setSpannedText(((String) seq.getSSequentialDS().getSData()).
              substring(seq.getSStart(), seq.getSEnd()));
            aNode.setToken(true);
            aNode.setTokenIndex(sNode.getSFeature(ANNIS_NS, FEAT_TOKENINDEX).
              getSValueSNUMERIC());
          }
        }
        else
        {
          aNode.setToken(false);
          aNode.setTokenIndex(null);
        }

        aNode.setCorpus(sNode.getSFeature(ANNIS_NS, FEAT_CORPUSREF).
          getSValueSNUMERIC());
        aNode.setTextId(sNode.getSFeature(ANNIS_NS, FEAT_TEXTREF)
          .getSValueSNUMERIC());
        aNode.setLeft(sNode.getSFeature(ANNIS_NS, FEAT_LEFT).getSValueSNUMERIC());
        aNode.setLeftToken(sNode.getSFeature(ANNIS_NS, FEAT_LEFTTOKEN).
          getSValueSNUMERIC());
        aNode.setRight(
          sNode.getSFeature(ANNIS_NS, FEAT_RIGHT).getSValueSNUMERIC());
        aNode.setRightToken(sNode.getSFeature(ANNIS_NS, FEAT_RIGHTTOKEN).
          getSValueSNUMERIC());
        if (matchSet.contains(aNode.getName()))
        {
          aNode.setMatchedNodeInQuery((long) matchedIDs.indexOf(aNode.getName()) + 1);
          annoGraph.getMatchedNodeIds().add(aNode.getId());
        }
        else
        {
          aNode.setMatchedNodeInQuery(null);
        }

        annoGraph.addNode(aNode);
        allNodes.put(sNode, aNode);
      }
    }

    for (SRelation rel : docGraph.getSRelations())
    {
      SFeature featPre = rel.getSFeature(ANNIS_NS, FEAT_INTERNALID);
      SFeature featComponentID = rel.getSFeature(ANNIS_NS, FEAT_COMPONENTID);

      if (featPre != null)
      {
        addEdge(rel, featPre.getSValueSNUMERIC(), featComponentID.getSValueSNUMERIC(),
          allNodes, annoGraph);
      }
    }
    
    // add edges with empty edge name for every dominance edge
    for(SDominanceRelation rel : docGraph.getSDominanceRelations())
    {
      SFeature featComp = rel.getSFeature(ANNIS_NS, FEAT_ARTIFICIAL_DOMINANCE_COMPONENT);
      SFeature featPre = rel.getSFeature(ANNIS_NS, FEAT_ARTIFICIAL_DOMINANCE_PRE);
      if(featComp != null && featPre != null)
      {
        SDominanceRelation newRel = SaltFactory.eINSTANCE.createSDominanceRelation();
        newRel.setSSource(rel.getSSource());
        newRel.setSTarget(rel.getSTarget());
        for(SLayer layer : rel.getSLayers())
        {
          newRel.getSLayers().add(layer);
        }
        for(SAnnotation anno : rel.getSAnnotations())
        {
          newRel.addSAnnotation(anno);
        }
        addEdge(newRel, featPre.getSValueSNUMERIC(), featComp.getSValueSNUMERIC(), allNodes, annoGraph);
      }
    }

    return annoGraph;
  }
  
  private static void addEdge(SRelation rel, long pre, long componentID, 
    Map<Node, AnnisNode> allNodes, AnnotationGraph annoGraph)
  {
    Edge aEdge = new Edge();
    aEdge.setSource(allNodes.get(rel.getSource()));
    aEdge.setDestination(allNodes.get(rel.getTarget()));

    aEdge.setEdgeType(EdgeType.UNKNOWN);
    aEdge.setPre(pre);
    aEdge.setComponentID(componentID);

    aEdge.setNamespace(rel.getSLayers().get(0).getSName());
    aEdge.setName((rel.getSTypes() != null && rel.getSTypes().size() > 0)
      ? rel.getSTypes().get(0) : null);

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

    for (SAnnotation sAnno : rel.getSAnnotations())
    {
      aEdge.addAnnotation(new Annotation(sAnno.getSNS(), sAnno.getSName(),
        sAnno.getSValueSTEXT()));
    }

    annoGraph.addEdge(aEdge);
    aEdge.getDestination().addIncomingEdge(aEdge);
    if(aEdge.getSource() != null)
    {
      aEdge.getSource().addOutgoingEdge(aEdge);
    }
  }
  
}
