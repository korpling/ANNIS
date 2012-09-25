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
package annis.gui.visualizers.component.grid;

import static annis.model.AnnisConstants.*;

import annis.gui.visualizers.AbstractVisualizer;
import annis.gui.visualizers.VisualizerInput;
import annis.model.AnnisNode;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDataSourceSequence;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STYPE_NAME;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import net.xeoh.plugins.base.annotations.PluginImplementation;
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
  public GridVisualizerComponent createComponent(VisualizerInput visInput)
  {
    GridVisualizerComponent component = new GridVisualizerComponent(visInput);
    return component;
  }

  public static class GridVisualizerComponent extends GridLayout
  {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(GridVisualizerComponent.class);
    private List<AnnisNode> nodes;
    private List<AnnisNode> token;

    public enum ElementType
    {

      begin,
      end,
      middle,
      single,
      noEvent
    }

    public GridVisualizerComponent(VisualizerInput input)
    {
      addStyleName("partitur_table");
      
      SDocumentGraph graph = input.getDocument().getSDocumentGraph();
     
      List<String> annos = new LinkedList<String>(getAnnotationLevelSet(graph, 
        input.getNamespace()));
      
      Map<String, ArrayList<Row>> rowsByAnnotation = 
        parseSalt(input.getDocument().getSDocumentGraph(), annos);
      
      // we can now calculate the size of the grid
      int gridRowCount = 0;
      for(ArrayList<Row> rows : rowsByAnnotation.values())
      {
        gridRowCount += rows.size();
      }
      setRows(gridRowCount);
      setColumns(graph.getSTokens().size());
      
      // output every line
      int currentRow=0;
      for(Map.Entry<String, ArrayList<Row>> annotationSet : rowsByAnnotation.entrySet())
      {
        for(Row row : annotationSet.getValue())
        {
          // copy events
          ArrayList<GridEvent> events = new ArrayList<GridEvent>(row.getEvents());
          
          // sort row events by their order (there are no conflicts so only "left"
          // needs to be considered)
          Collections.sort(events, new Comparator<GridEvent>() 
          {
            @Override
            public int compare(GridEvent o1, GridEvent o2)
            {
              return Long.compare(o1.getLeft(), o2.getRight());
            }
          });
          
          
          for(GridEvent e : row.getEvents())
          {
            Label lblEvent = new Label();
            lblEvent.setValue(e.getValue());
            lblEvent.setDescription(annotationSet.getKey());

            // clip events to displayed token range
            int left =  (int) clip(e.getLeft(), 0, getColumns()-1);
            int right = (int) clip(e.getRight(), 0, getColumns()-1);
            addComponent(lblEvent, left, currentRow, right, currentRow);
          } // for each event
          
          currentRow++;
        } // end for each row
      } // end for each annotation
      
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
    
    private Map<String, ArrayList<Row>> parseSalt(SDocumentGraph graph, List<String> annotationNames)
    {
      // only look at annotations which were defined by the user
      Map<String, ArrayList<Row>> rowsByAnnotation = 
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
      for(SSpan span : graph.getSSpans())
      {  
        // calculate the left and right values of a span
        // TODO: howto get these numbers with Salt?
        long leftLong = span.getSFeature(ANNIS_NS, FEAT_LEFTTOKEN).getSValueSNUMERIC();
        long rightLong = span.getSFeature(ANNIS_NS, FEAT_RIGHTTOKEN).getSValueSNUMERIC();
        
        int left = (int) leftLong;
        int right = (int) rightLong;
        
        for(SAnnotation anno : span.getSAnnotations())
        {
          ArrayList<Row> rows = rowsByAnnotation.get(anno.getQName());
          if(rows != null)
          {
            // only do something if the annotation was defined before

            // 1. give each annotation of each span an own row
            Row r = new Row();
            r.addEvent(new GridEvent(left, right, anno.getSValueSTEXT()));
            rows.add(r);
          }
        } // end for each annotation of span
      } // end for each span
      
      // 2. merge rows when possible
      for(Map.Entry<String, ArrayList<Row>> e : rowsByAnnotation.entrySet())
      {
        mergeAllRowsIfPossible(e.getValue());
      }
      
      return rowsByAnnotation;
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
