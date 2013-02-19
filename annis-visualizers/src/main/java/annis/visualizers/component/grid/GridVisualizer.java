/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.visualizers.component.grid;

import annis.CommonHelper;
import annis.libgui.VisualizationToggle;
import annis.libgui.media.MediaController;
import annis.libgui.media.TimeHelper;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import annis.gui.widgets.grid.AnnotationGrid;
import annis.gui.widgets.grid.GridEvent;
import annis.gui.widgets.grid.Row;
import static annis.model.AnnisConstants.*;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Edge;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpanningRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STYPE_NAME;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SFeature;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@PluginImplementation
public class GridVisualizer extends AbstractVisualizer<GridVisualizer.GridVisualizerComponent>
{
  
  @Override
  public String getShortName()
  {
    return "grid";
  }

  @Override
  public GridVisualizerComponent createComponent(VisualizerInput visInput, VisualizationToggle visToggle)
  {
    MediaController mediaController = VaadinSession.getCurrent().getAttribute(MediaController.class);
    GridVisualizerComponent component = new GridVisualizerComponent(visInput, mediaController);
    return component;
  }

  public static class GridVisualizerComponent extends Panel
  {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(GridVisualizerComponent.class);
    
    public static final String MAPPING_ANNOS_KEY = "annos";
    public static final String MAPPING_ANNO_REGEX_KEY = "anno_regex";
    public static final String MAPPING_HIDE_TOK_KEY = "hide_tok";
    
    private AnnotationGrid grid;
    private transient VisualizerInput input;
    private transient MediaController mediaController;
    private VerticalLayout layout;

    public enum ElementType
    {

      begin,
      end,
      middle,
      single,
      noEvent
    }

    public GridVisualizerComponent(VisualizerInput input, MediaController mediaController)
    {
      this.input = input;
      this.mediaController = mediaController;
      
      setWidth("100%");
      setHeight("-1");
      
      layout = new VerticalLayout();
      setContent(layout);
      layout.setSizeUndefined();
      addStyleName(ChameleonTheme.PANEL_BORDERLESS);
      
      if(input != null)
      {
        String resultID = input.getId();
      
        grid = new AnnotationGrid(mediaController, resultID);
        grid.addStyleName("partitur_table");
        layout.addComponent(grid);

        SDocumentGraph graph = input.getDocument().getSDocumentGraph();

        List<String> annos = computeDisplayAnnotations(graph);

        EList<SToken> token = graph.getSortedSTokenByText();
        long startIndex = token.get(0).getSFeature(ANNIS_NS, FEAT_TOKENINDEX).getSValueSNUMERIC();
        long endIndex = token.get(token.size()-1).getSFeature(ANNIS_NS, FEAT_TOKENINDEX).getSValueSNUMERIC();

        LinkedHashMap<String, ArrayList<Row>> rowsByAnnotation = 
          parseSalt(input.getDocument().getSDocumentGraph(), annos, 
            (int) startIndex, (int) endIndex);

        // add tokens as row
        Row tokenRow = new Row();
        for(SToken t : token)
        {
          long idx = t.getSFeature(ANNIS_NS, FEAT_TOKENINDEX).getSValueSNUMERIC()
            - startIndex;
          String text = CommonHelper.getSpannedText(t);

          GridEvent event = new GridEvent(t.getSId(), (int) idx,(int) idx, text);

          // check if the token is a matched node
          SFeature featMatched = t.getSFeature(ANNIS_NS, FEAT_MATCHEDNODE);
          Long match = featMatched == null ? null : featMatched.
            getSValueSNUMERIC();
          event.setMatch(match);

          tokenRow.addEvent(event);
        }
        ArrayList<Row> tokenRowList = new ArrayList<Row>();
        tokenRowList.add(tokenRow);

        if(Boolean.parseBoolean(
          input.getMappings().getProperty(MAPPING_HIDE_TOK_KEY, "false")) == false)
        {
          rowsByAnnotation.put("tok", tokenRowList);
        }

        grid.setRowsByAnnotation(rowsByAnnotation);
      } // end if input not null
    }
    
    /**
     * Returns 
     * @return 
     */
    private List<String> computeDisplayAnnotations(SDocumentGraph graph)
    {
      if(input == null)
      {
        return new LinkedList<String>();
      }
      
      Set<String> annoPool = getAnnotationLevelSet(graph, input.getNamespace());
      List<String> annos = new LinkedList<String>(annoPool);
      
      String annosConfiguration = input.getMappings().getProperty(MAPPING_ANNOS_KEY);
      if(annosConfiguration != null && annosConfiguration.trim().length() > 0)
      {
        String[] split = annosConfiguration.split(",");
        annos.clear();
        for(String s : split)
        {
          s = s.trim();
          // is regular expression?
          if(s.startsWith("/") && s.endsWith("/"))
          {
            // go over all remaining items in our pool of all annotations and
            // check if they match
            Pattern regex = Pattern.compile(StringUtils.strip(s, "/"));
            
            LinkedList<String> matchingAnnos = new LinkedList<String>();
            for(String a : annoPool)
            {
              if(regex.matcher(a).matches())
              {
                matchingAnnos.add(a);
              }
            }
            
            annos.addAll(matchingAnnos);
            annoPool.removeAll(matchingAnnos);
            
          }
          else
          {
            annos.add(s);
            annoPool.remove(s);
          }
        }
      }
      
      // filter already found annotation names by regular expression 
      // if this was given as mapping
      String regexFilterRaw = input.getMappings().getProperty(MAPPING_ANNO_REGEX_KEY);
      if(regexFilterRaw != null)
      {
        try
        {
          Pattern regexFilter = Pattern.compile(regexFilterRaw);
          ListIterator<String> itAnnos = annos.listIterator();
          while(itAnnos.hasNext())
          {
            String a = itAnnos.next();
            // remove entry if not matching
            if(!regexFilter.matcher(a).matches())
            {
              itAnnos.remove();
            }
          }
        }
        catch(PatternSyntaxException ex)
        {
          log.warn("invalid regular expression in mapping for grid visualizer", ex);
        }
      }
      return annos;
    }
    
    
    
    private long clip(long value, long min, long max)
    {
      if(value > max)
      {
        return max;
      }
      else if(value < min)
      {
        return min;
      }
      else
      {
        return value;
      }
      
    }
    
    private Set<String> getAnnotationLevelSet(SDocumentGraph graph, String namespace)
    {
      Set<String> result = new TreeSet<String>();

      if(graph != null)
      {
        for(SSpan n : graph.getSSpans())
        {
          for(SLayer layer : n.getSLayers())
          {
            if(namespace.equals(layer.getSName()))
            {
              for (SAnnotation anno : n.getSAnnotations())
              {
                result.add(anno.getQName());
              }
              // we got all annotations of this node, jump to next span
              break;
            } // end if namespace equals layer name
          } // end for each layer
        } // end for each span
      }

      return result;
    }
    
    /**
     * Converts Salt document graph to rows.
     * 
     * @param graph
     * @param annotationNames
     * @param startTokenIndex token index of the first token in the match
     * @param endTokenIndex  token index of the last token in the match
     * @return 
     */
    private LinkedHashMap<String, ArrayList<Row>> parseSalt(SDocumentGraph graph,
      List<String> annotationNames, long startTokenIndex, long endTokenIndex)
    {
      // only look at annotations which were defined by the user
      LinkedHashMap<String, ArrayList<Row>> rowsByAnnotation = 
        new LinkedHashMap<String, ArrayList<Row>>();
      
      for(String anno : annotationNames)
      {
        rowsByAnnotation.put(anno, new ArrayList<Row>());
      }
      
      
      EList<STYPE_NAME> types = new BasicEList<STYPE_NAME>();
      types.add(STYPE_NAME.SSPANNING_RELATION);
      types.add(STYPE_NAME.STEXTUAL_RELATION);
      types.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);
      types.add(STYPE_NAME.SSEQUENTIAL_RELATION);
      
      int eventCounter = 0;
      
      for(SSpan span : graph.getSSpans())
      {  
        // calculate the left and right values of a span
        // TODO: howto get these numbers with Salt?
        long leftLong = span.getSFeature(ANNIS_NS, FEAT_LEFTTOKEN).getSValueSNUMERIC();
        long rightLong = span.getSFeature(ANNIS_NS, FEAT_RIGHTTOKEN).getSValueSNUMERIC();
        
        leftLong = clip(leftLong, startTokenIndex, endTokenIndex);
        rightLong = clip(rightLong, startTokenIndex, endTokenIndex);
        
        int left = (int) (leftLong - startTokenIndex);
        int right = (int) (rightLong - startTokenIndex);
        
        for(SAnnotation anno : span.getSAnnotations())
        {
          ArrayList<Row> rows = rowsByAnnotation.get(anno.getQName());
          if(rows == null)
          {
            // try again with only the name
            rows = rowsByAnnotation.get(anno.getSName());
          }
          if(rows != null)
          {
            // only do something if the annotation was defined before

            // 1. give each annotation of each span an own row
            Row r = new Row();
            
            String id = "event_" + eventCounter++;
            GridEvent event = new GridEvent(id, left, right,
              anno.getSValueSTEXT());
            
            // check if the span is a matched node
            SFeature featMatched = span.getSFeature(ANNIS_NS, FEAT_MATCHEDNODE);
            Long match = featMatched == null ? null : featMatched.
              getSValueSNUMERIC();
            event.setMatch(match);
            
            // calculate overlapped SToken
            EList<Edge> outEdges = graph.getOutEdges(span.getSId());
            if(outEdges != null)
            {
              for(Edge e : outEdges)
              {
                if(e instanceof SSpanningRelation)
                {
                  SSpanningRelation spanRel = (SSpanningRelation) e;
                  event.getCoveredIDs().add(spanRel.getSTarget().getSId());
                }
              }
            }
            
            // try to get time annotations
            double[] startEndTime = TimeHelper.getOverlappedTime(span);
            if(startEndTime.length == 1)
            {
              event.setStartTime(startEndTime[0]);
            }
            else if(startEndTime.length == 2)
            {
              event.setStartTime(startEndTime[0]);
              event.setEndTime(startEndTime[1]);
            }
            
            r.addEvent(event);
            rows.add(r);
          }
        } // end for each annotation of span
      } // end for each span
      
      // 2. merge rows when possible
      for(Map.Entry<String, ArrayList<Row>> e : rowsByAnnotation.entrySet())
      {
        mergeAllRowsIfPossible(e.getValue());
      }
      
      // 3. sort events on one row by left token index
      for(Map.Entry<String, ArrayList<Row>> e : rowsByAnnotation.entrySet())
      {
        for(Row r : e.getValue())
        {
          sortEventsByTokenIndex(r);
        }
      }
      
      // 4. split up events if they have gaps
      for(Map.Entry<String, ArrayList<Row>> e : rowsByAnnotation.entrySet())
      {
        for(Row r : e.getValue())
        {
          splitRowsOnGaps(r, graph, startTokenIndex, endTokenIndex);
        }
      }
      return rowsByAnnotation;
    }
    
    /**
     * Splits events of a row if they contain a gap.
     * Gaps are found using the token index (provided as ANNIS specific 
     * {@link SFeature}. Inserted events have a special style to mark them as
     * gaps.
     * @param row 
     * @param graph 
     * @param startTokenIndex token index of the first token in the match
     * @param endTokenIndex  token index of the last token in the match
     */
    private void splitRowsOnGaps(Row row, final SDocumentGraph graph, long startTokenIndex, long endTokenIndex)
    {
      ListIterator<GridEvent> itEvents = row.getEvents().listIterator();
      while(itEvents.hasNext())
      {
        GridEvent event = itEvents.next();
        
        int lastTokenIndex = Integer.MIN_VALUE;
        
        // sort the coveredIDs
        LinkedList<String> sortedCoveredToken = new LinkedList<String>(event.getCoveredIDs());
        Collections.sort(sortedCoveredToken, new Comparator<String>() 
        {
          @Override
          public int compare(String o1, String o2)
          {
            SNode node1 = graph.getSNode(o1);
            SNode node2 = graph.getSNode(o2);
            
            if (node1 == node2)
            {
              return 0;
            }
            if (node1 == null)
            {
              return -1;
            }
            if (node2 == null)
            {
              return +1;
            }
            
            long tokenIndex1 = node1.getSFeature(ANNIS_NS, FEAT_TOKENINDEX).getSValueSNUMERIC();
            long tokenIndex2 = node2.getSFeature(ANNIS_NS, FEAT_TOKENINDEX).getSValueSNUMERIC();
            
            return ((Long) (tokenIndex1)).compareTo(tokenIndex2);
          }
        });
        
        // first calculate all gaps
        List<GridEvent> gaps = new LinkedList<GridEvent>();
        for(String id : sortedCoveredToken)
        {
          
          SNode node = graph.getSNode(id);
          long tokenIndexRaw = node.getSFeature(ANNIS_NS, FEAT_TOKENINDEX).getSValueSNUMERIC();
          
          tokenIndexRaw = clip(tokenIndexRaw, startTokenIndex, endTokenIndex);

          int tokenIndex = (int) (tokenIndexRaw - startTokenIndex);
          int diff = tokenIndex - lastTokenIndex;

          if(lastTokenIndex >= 0 && diff > 1)
          {
            // we detected a gap
            GridEvent gap = new GridEvent(event.getId() + "_gap", 
              lastTokenIndex+1, tokenIndex - 1, "");
            gap.setGap(true);
            gaps.add(gap);
          }

          lastTokenIndex = tokenIndex;
        } // end for each covered token id
        
        for(GridEvent gap : gaps)
        {
          // remember the old right value
          int oldRight = event.getRight();
          
          // shorten last event
          event.setRight(gap.getLeft()-1);
          
          // insert the real gap
          itEvents.add(gap);
          
          // insert a new event node that covers the rest of the event
          GridEvent after = new GridEvent(event.getId() + "_after", 
            gap.getRight()+1, oldRight, event.getValue());
          after.getCoveredIDs().addAll(event.getCoveredIDs());
          itEvents.add(after);
        }
        
      }
    }
    
    /**
     * Sort events of a row.
     * The sorting is depending on the left value of the event
     * @param row 
     */
    private void sortEventsByTokenIndex(Row row)
    {
      Collections.sort(row.getEvents(), new Comparator<GridEvent>() 
      {
        @Override
        public int compare(GridEvent o1, GridEvent o2)
        {
          if (o1 == o2)
          {
            return 0;
          }
          if (o1 == null)
          {
            return -1;
          }
          if (o2 == null)
          {
            return +1;
          }
          
          return ((Integer) o1.getLeft()).compareTo(o2.getLeft());
        }
      });
    }
    
    
    /**
     * Merges the rows.
     * This function uses a heuristical approach that guarantiess to merge 
     * all rows into one if there is no conflict at all. If there are conflicts
     * the heuristic will be best-efford but with linear runtime (given a a number
     * of rows).
     * @param rows Will be altered, if no conflicts occcured this wil have only
     *              one element.
     */
    private void mergeAllRowsIfPossible(ArrayList<Row> rows)
    {
      // use fixed seed in order to get consistent results (with random properties)
      Random rand = new Random(5711l);
      int tries = 0;
      // this should be enough to be quite sure we don't miss any optimalization
      // possibility
      final int maxTries = rows.size()*2;
      
      // do this loop until we successfully merged everything into one row
      // or we give up until too much tries
      while(rows.size() > 1 && tries < maxTries)
      {
        // choose two random entries
        int oneIdx = rand.nextInt(rows.size());
        int secondIdx = rand.nextInt(rows.size());
        if(oneIdx == secondIdx)
        {
          // try again if we choose the same rows by accident
          continue;
        }
        
        Row one = rows.get(oneIdx);
        Row second = rows.get(secondIdx);
        
        if(one.merge(second))
        {
          // remove the second one since it is merged into the first
          rows.remove(secondIdx);
          
          // success: reset counter
          tries = 0;
        }
        else
        {
          // increase counter to avoid endless loops if no improvement is possible
          tries++;
        }
      }
    }
  } // end GridVisualizerComponent

}
