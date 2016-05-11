package annis;

import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SOrderRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STimeline;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SRelation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

/**
 * Allows to reconstruct a proper {@link SDocumentGraph} with an {@link STimeline} and
 * several {@link SToken} connected by {@link SOrderRelation} from of a virtual tokenization.
 * 
 * @author Thomas Krause <thomaskrause@posteo.de>
 *
 */
public class TimelineReconstructor {
  
  private static Multimap<SSpan, Integer> addTimeline(SDocumentGraph graph)
  {
    Multimap<SSpan, Integer> spans2TimelinePos = HashMultimap.create();
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
    return spans2TimelinePos;
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
      return;
    }
    
    Multimap<SSpan, Integer> spans2TimelinePos = addTimeline(graph);
  }
}
