package annis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SOrderRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.SStructuredNode;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STimeline;
import org.corpus_tools.salt.common.STimelineRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.GraphTraverseHandler;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SGraph.GRAPH_TRAVERSE_TYPE;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Allows to reconstruct a proper {@link SDocumentGraph} with an {@link STimeline} and
 * several {@link SToken} connected by {@link SOrderRelation} from of a virtual tokenization.
 * 
 * @author Thomas Krause <thomaskrause@posteo.de>
 *
 */
public class TimelineReconstructor 
{
  
  private final SDocumentGraph graph;
  
  private final Map<String, STextualDS> textsByName = new HashMap<>();
  private final Map<String, StringBuilder> textDataByName = new HashMap<>();
  private final Multimap<SStructuredNode, Integer> spans2TimelinePos = HashMultimap.create();
  
  private final Set<SStructuredNode> nodesToDelete = new HashSet<>();
  
  private TimelineReconstructor(SDocumentGraph graph)
  {
    this.graph = graph;
  }
  
  private void addTimeline()
  {
    
    STimeline timeline = graph.createTimeline();
    for(SToken virtualTok : graph.getSortedTokenByText())
    {
      timeline.increasePointOfTime();
      // find all spans that are connected to this token and are part of an SOrderRelation
      for(SRelation<?,?> inRel : virtualTok.getInRelations())
      {
        if(inRel instanceof SSpanningRelation)
        {
          SSpanningRelation spanRel = (SSpanningRelation) inRel;
          SSpan overlappingSpan = spanRel.getSource();
          if(overlappingSpan != null)
          {
            spans2TimelinePos.put(overlappingSpan, timeline.getEnd());
          }
        }
      }
    }
  }
  
  private void createTokenFromSOrder()
  {    
    List<SNode> orderRelRoots = graph.getRootsByRelation(SALT_TYPE.SORDER_RELATION);
    if(orderRelRoots != null) 
    {
      // convert all root nodes to span
      for(SNode root : orderRelRoots)
      {
        if(root instanceof SSpan)
        {
          String orderName = null;
          for(SRelation<?,?> outRel : root.getOutRelations())
          {
            if(outRel instanceof SOrderRelation)
            {
              orderName = ((SOrderRelation) outRel).getType();
            }
          }
          if(orderName != null)
          {
            convertSpanToToken((SSpan) root, orderName);
          }
        }
      }
      
      // traverse through all SOrderRelations in order
      graph.traverse(orderRelRoots, GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST, "TimeReconstructSOrderRelations",
          new GraphTraverseHandler()
          {
            
            @Override
            public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode,
                SRelation relation, SNode fromNode, long order)
            {
              if(relation instanceof SOrderRelation && currNode instanceof SSpan)
              {
                convertSpanToToken((SSpan) currNode, ((SOrderRelation) relation).getType());
              }
            }
            
            @Override
            public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode,
                SRelation relation, SNode fromNode, long order)
            {              
            }
            
            @Override
            public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
                SRelation relation, SNode currNode, long order)
            {
              if(relation == null || relation instanceof SOrderRelation)
              {
                return true;
              }
              else
              {
                return false;
              }
            }
          });
    }
    
    // update the text of the TextualDSs
    for(Map.Entry<String, StringBuilder> textDataEntry : textDataByName.entrySet())
    {
      STextualDS textDS = textsByName.get(textDataEntry.getKey());
      if(textDS != null)
      {
        textDS.setText(textDataEntry.getValue().toString());
      }
    }
  }
  
  private SToken convertSpanToToken(SStructuredNode span, String orderName)
  {
    if(!nodesToDelete.contains(span))
    {
      nodesToDelete.add(span);
      
      if(textsByName.get(orderName) == null)
      {
        STextualDS newText = graph.createTextualDS("");
        newText.setName(orderName);
        textsByName.put(orderName, newText);
        textDataByName.put(orderName, new StringBuilder());
      }
      STextualDS textDS = textsByName.get(orderName);
      StringBuilder textData = textDataByName.get(orderName);
      
      TreeSet<Integer> coveredIdx = new TreeSet<>(spans2TimelinePos.get(span));
      if(!coveredIdx.isEmpty())
      {
        SAnnotation textValueAnno = span.getAnnotation(null, orderName);
        if(textValueAnno != null)
        {
          String textValue = textValueAnno.getValue_STEXT();
          
          int startTextIdx = textData.length();
          textData.append(textValue);
          int endTextIdx = textData.length();
          SToken newToken = graph.createToken(textDS, startTextIdx, endTextIdx);
          
          STimelineRelation timeRel = SaltFactory.createSTimelineRelation();
          timeRel.setSource(newToken);
          timeRel.setTarget(graph.getTimeline());
          timeRel.setStart(coveredIdx.first());
          timeRel.setEnd(coveredIdx.last());
          
          graph.addRelation(timeRel);
          
          return newToken;
        }
      }
    }
    
    return null;
  }
  
  private void cleanup()
  {
    for(SStructuredNode node : nodesToDelete)
    {
      graph.removeNode(node);
    }
  }
  
  
  
  /**
   * Removes the virtual tokenization from a {@link SDocumentGraph} and replaces it with an
   * {@link STimeline} and multiple {@link STextualDS}.
   * 
   * This alters the original graph.
   * @param orig
   * @return
   */
  public static void removeVirtualTokenization(SDocumentGraph graph)
  {
    if(graph.getTimeline() != null)
    {
      // do nothing if the graph does not contain any virtual tokenization
      return;
    }
    
    TimelineReconstructor reconstructor = new TimelineReconstructor(graph);
    reconstructor.addTimeline();
    reconstructor.createTokenFromSOrder();
    
    reconstructor.cleanup();
    
  }
}
