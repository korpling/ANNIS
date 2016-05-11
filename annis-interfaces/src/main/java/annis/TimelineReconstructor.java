package annis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SOrderRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.SStructuredNode;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STimeline;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
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
    for(SOrderRelation orderRel : graph.getOrderRelations())
    {
      SStructuredNode source = orderRel.getSource();
      SStructuredNode target = orderRel.getTarget();
      String orderName = orderRel.getType();
      
      convertSpanToToken(source, orderName);
      convertSpanToToken(target, orderName);
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
      }
      STextualDS textDS = textsByName.get(orderName);
      TreeSet<Integer> coveredIdx = new TreeSet<>(spans2TimelinePos.get(span));
      if(!coveredIdx.isEmpty())
      {
        SToken newToken = graph.createToken(textDS, coveredIdx.first(), coveredIdx.last());
        SAnnotation textValueAnno = span.getAnnotation(null, orderName);
        if(textValueAnno != null)
        {
          String textValue = textValueAnno.getValue_STEXT();
          // TODO: Howto extends the existing text? we don't know the order of the sorder relation for sure.
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
