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

import static annis.model.AnnisConstants.ANNIS_NS;
import static annis.model.AnnisConstants.FEAT_MATCHEDIDS;
import static annis.model.AnnisConstants.FEAT_RELANNIS_NODE;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SDominanceRelation;
import org.corpus_tools.salt.common.SPointingRelation;
import org.corpus_tools.salt.common.SSequentialDS;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.util.DataSourceSequence;
import org.corpus_tools.salt.util.SaltUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import annis.CommonHelper;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import annis.model.Edge.EdgeType;
import annis.model.RelannisEdgeFeature;
import annis.model.RelannisNodeFeature;
import annis.service.ifaces.AnnisResultSet;
import annis.service.objects.AnnisResultImpl;
import annis.service.objects.AnnisResultSetImpl;
import annis.service.objects.Match;

/**
 * This class can convert the current Salt graph model into the legacy model 
 *  AOM (Annis Object Model)
 *  and
 *  "PaulaInline"
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class LegacyGraphConverter
{
  
  private final static Logger log = LoggerFactory.getLogger(LegacyGraphConverter.class
    );

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
    
    if(p != null)
    {
      for (SCorpusGraph corpusGraph : p.getCorpusGraphs())
      {
        for (SDocument doc : corpusGraph.getDocuments())
        {
          result.add(convertToAnnotationGraph(doc));
        }
      }
    }
    
    return result;
  }

  public static AnnotationGraph convertToAnnotationGraph(SDocument document)
  {
    
    SDocumentGraph docGraph = document.getDocumentGraph();
    SFeature featMatchedIDs = docGraph.getFeature(ANNIS_NS, FEAT_MATCHEDIDS);
    Match match = new Match();
    if (featMatchedIDs != null && featMatchedIDs.getValue_STEXT() != null)
    {    
       match = Match.parseFromString(featMatchedIDs.getValue_STEXT(), ',');
    }
    
    // get matched node names by using the IDs
    List<Long> matchedNodeIDs = new ArrayList<>();
    for(URI u : match.getSaltIDs())
    {
      SNode node = docGraph.getNode(u.toASCIIString());
      if(node == null)
      {
        // that's weird, fallback to the id
        log.warn("Could not get matched node from id {}", u.toASCIIString());
        matchedNodeIDs.add(-1l);
      }
      else
      {
        RelannisNodeFeature relANNISFeat = 
          (RelannisNodeFeature) node.getFeature(
            SaltUtil.createQName(ANNIS_NS, FEAT_RELANNIS_NODE)).getValue();
        
        matchedNodeIDs.add(relANNISFeat.getInternalID());
      }
    }
    
    AnnotationGraph result = convertToAnnotationGraph(docGraph, matchedNodeIDs);

    return result;
  }

  public static AnnotationGraph convertToAnnotationGraph(SDocumentGraph docGraph,
    List<Long> matchedNodeIDs)
  {
    Set<Long> matchSet = new HashSet<>(matchedNodeIDs);
    AnnotationGraph annoGraph = new AnnotationGraph();

    List<String> pathList =  CommonHelper.getCorpusPath(
      docGraph.getDocument().getGraph(), docGraph.getDocument());
    
    annoGraph.setPath(pathList.toArray(new String[pathList.size()]));
    annoGraph.setDocumentName(docGraph.getDocument().getName());

    Map<SNode, AnnisNode> allNodes = new HashMap<>();

    for (SNode sNode : docGraph.getNodes())
    {
      SFeature featNodeRaw = sNode.getFeature(SaltUtil.createQName(ANNIS_NS, FEAT_RELANNIS_NODE));
      if (featNodeRaw != null)
      {
        RelannisNodeFeature featNode = (RelannisNodeFeature) featNodeRaw.getValue();
        long internalID = featNode.getInternalID();
        AnnisNode aNode = new AnnisNode(internalID);

        for (SAnnotation sAnno : sNode.getAnnotations())
        {
          aNode.addNodeAnnotation(new Annotation(sAnno.getNamespace(),
            sAnno.getName(),
            sAnno.getValue_STEXT()));
        }
        aNode.setName(sNode.getName());
        Set<SLayer> layers = sNode.getLayers();
        if(!layers.isEmpty())
        {
          aNode.setNamespace(layers.iterator().next().getName());
        }

        RelannisNodeFeature feat = (RelannisNodeFeature) sNode.getFeature(
          SaltUtil.createQName(ANNIS_NS, FEAT_RELANNIS_NODE)).getValue();
        
        if (sNode instanceof SToken)
        {
          List<DataSourceSequence> seqList =
            docGraph.getOverlappedDataSourceSequence(sNode,
            SALT_TYPE.STEXT_OVERLAPPING_RELATION);
          if (seqList != null)
          {
            DataSourceSequence seq = seqList.get(0);
            Preconditions.checkNotNull(seq, "DataSourceSequence is null for token %s", sNode.getId());
            SSequentialDS seqDS = seq.getDataSource();
            Preconditions.checkNotNull(seqDS, "SSequentalDS is null for token %s", sNode.getId());
            Preconditions.checkNotNull(seqDS.getData(), "SSequentalDS data is null for token %s", sNode.getId());
            
            String seqDSData = (String) seqDS.getData();
            Preconditions.checkNotNull(seqDSData, "casted SSequentalDS data is null for token %s", sNode.getId());
            
            Preconditions.checkNotNull(seq.getStart(), "SSequentalDS start is null for token %s", sNode.getId());
            Preconditions.checkNotNull(seq.getEnd(), "SSequentalDS end is null for supposed token %s", sNode.getId());
            
            int start = seq.getStart().intValue();
            int end = seq.getEnd().intValue();
            
            Preconditions.checkState(start >= 0 && start <= end && end <= seqDSData.length(), "Illegal start or end of textual DS for token (start %s, end: %s)", sNode.getId(), start, end);
            
            String spannedText = seqDSData.substring(start, end);
            Preconditions.checkNotNull(spannedText, "spanned text is null for supposed token %s (start: %s, end: %s)",
              sNode.getId(), start, end);
            
            
            aNode.setSpannedText(spannedText);
            aNode.setToken(true);
            aNode.setTokenIndex(feat.getTokenIndex());
          }
        }
        else
        {
          aNode.setToken(false);
          aNode.setTokenIndex(null);
        }

        aNode.setCorpus(feat.getCorpusRef());
        aNode.setTextId(feat.getTextRef());
        aNode.setLeft(feat.getLeft());
        aNode.setLeftToken(feat.getLeftToken());
        aNode.setRight(feat.getRight());
        aNode.setRightToken(feat.getRightToken());
        if (matchSet.contains(aNode.getId()))
        {
          aNode.setMatchedNodeInQuery((long) matchedNodeIDs.indexOf(aNode.getId()) + 1);
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

    for (SRelation rel : docGraph.getRelations())
    {
      RelannisEdgeFeature featRelation = RelannisEdgeFeature.extract(rel);
      if (featRelation != null)
      {
        addRelation(rel, 
          featRelation.getPre(), featRelation.getComponentID(),
          allNodes, annoGraph);
      }
    }
    
    // add relations with empty relation name for every dominance relation
    List<SDominanceRelation> dominanceRelations = new LinkedList<>(docGraph.getDominanceRelations());
    for(SDominanceRelation rel : dominanceRelations)
    {
      RelannisEdgeFeature featEdge = RelannisEdgeFeature.extract(rel);
      if(featEdge != null 
        && featEdge.getArtificialDominanceComponent() != null 
        && featEdge.getArtificialDominancePre() != null)
      {
        
        addRelation(SDominanceRelation.class,
          null, rel.getAnnotations(),
          rel.getSource(), rel.getTarget(), rel.getLayers(), 
          featEdge.getArtificialDominancePre(), 
          featEdge.getArtificialDominanceComponent(), allNodes, annoGraph);

      }
    }

    return annoGraph;
  }
  
  private static void addRelation(SRelation<? extends SNode, ? extends SNode> rel,
    long pre, long componentID, 
    Map<SNode, AnnisNode> allNodes, AnnotationGraph annoGraph)
  {
    addRelation(rel.getClass(), 
      rel.getType(),
      rel.getAnnotations(),
      rel.getSource(), rel.getTarget(), rel.getLayers(), 
      pre, componentID, allNodes, annoGraph);
  }
  
  private static void addRelation(
    Class<? extends SRelation> clazz,
    String type,
    Collection<SAnnotation> annotations,
    SNode source, SNode target,
    Set<SLayer> relLayers,
    long pre, long componentID, 
    Map<SNode, AnnisNode> allNodes, AnnotationGraph annoGraph)
  {
    Edge aEdge = new Edge();
    
    aEdge.setSource(allNodes.get(source));
    aEdge.setDestination(allNodes.get(target));

    aEdge.setEdgeType(EdgeType.UNKNOWN);
    aEdge.setPre(pre);
    aEdge.setComponentID(componentID);

    if(!relLayers.isEmpty())
    {
       aEdge.setNamespace(relLayers.iterator().next().getName());
    }
    aEdge.setName(type);
    
    if (SDominanceRelation.class.isAssignableFrom(clazz))
    {
      aEdge.setEdgeType(EdgeType.DOMINANCE);
    }
    else if (SPointingRelation.class.isAssignableFrom(clazz))
    {
      aEdge.setEdgeType(EdgeType.POINTING_RELATION);
    }
    else if (SSpanningRelation.class.isAssignableFrom(clazz))
    {
      aEdge.setEdgeType(EdgeType.COVERAGE);
    }

    for (SAnnotation sAnno : annotations)
    {
      aEdge.addAnnotation(new Annotation(sAnno.getNamespace(), sAnno.getName(),
        sAnno.getValue_STEXT()));
    }

    annoGraph.addEdge(aEdge);
    aEdge.getDestination().addIncomingEdge(aEdge);
    if(aEdge.getSource() != null)
    {
      aEdge.getSource().addOutgoingEdge(aEdge);
    }
  }
  
}
