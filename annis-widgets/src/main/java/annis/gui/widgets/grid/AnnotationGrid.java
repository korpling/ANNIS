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
package annis.gui.widgets.grid;

import annis.libgui.MatchedNodeColors;
import annis.libgui.media.MediaController;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.LegacyComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */

public class AnnotationGrid extends AbstractComponent implements LegacyComponent
{

  private Map<String, ArrayList<Row>> rowsByAnnotation;
  private transient MediaController mediaController;
  private String resultID;

  public AnnotationGrid(MediaController mediaController, String resultID)
  {
    this.mediaController = mediaController;
    this.resultID = resultID;
  }

  @Override
  public void changeVariables(Object source,
    Map<String, Object> variables)
  {
    
    if(variables.containsKey("play"))
    {
      if(mediaController != null && resultID != null)
      {
        String playString = (String) variables.get("play");
        String[] split = playString.split("-");
        if(split.length == 1)
        {
          mediaController.play(resultID, Double.parseDouble(split[0]));
        }
        else if(split.length == 2)
        {
          mediaController.play(resultID, 
            Double.parseDouble(split[0]), Double.parseDouble(split[1]));
        }
      }
    }
  }
  

  @Override
  public void paintContent(PaintTarget target) throws PaintException
  {
    if (rowsByAnnotation != null)
    {
      target.startTag("rows");
      for (Map.Entry<String, ArrayList<Row>> anno : rowsByAnnotation.entrySet())
      {

        for (Row row : anno.getValue())
        {
          target.startTag("row");
          target.addAttribute("caption", anno.getKey());

          ArrayList<GridEvent> rowEvents = row.getEvents();
          // sort the events by their natural order
          Collections.sort(rowEvents, new Comparator<GridEvent>() 
          {
            @Override
            public int compare(GridEvent o1, GridEvent o2)
            {
              return ((Integer) o1.getLeft()).compareTo(o2.getLeft());
            }
          });
          
          target.startTag("events");
          for (GridEvent event : rowEvents)
          {
            target.startTag("event");
            target.addAttribute("id", event.getId());
            target.addAttribute("left", event.getLeft());
            target.addAttribute("right", event.getRight());
            target.addAttribute("value", event.getValue());
            
            if(event.getStartTime() != null)
            {
              target.addAttribute("startTime", event.getStartTime());
              if(event.getEndTime() != null)
              {
                target.addAttribute("endTime", event.getEndTime());
              }
            }

            ArrayList<String> styles = getStyles(event, anno.getKey());
            if (styles.size() > 0)
            {
              target.addAttribute("style", styles.toArray());
            }

            // define a list of covered token that are hightlighted whenever this
            // event is hovered
            target.addAttribute("highlight", event.getCoveredIDs().toArray());

            target.endTag("event");
          }
          target.endTag("events");

          target.endTag("row");
        }
      }
      target.endTag("rows");
    }

  }

  private ArrayList<String> getStyles(GridEvent event, String annoName)
  {
    ArrayList<String> styles = new ArrayList<String>();

    if ("tok".equals(annoName))
    {
      styles.add("token");
    }
    else if(event.isGap())
    {
      styles.add("gap");
    }
    else
    {
      styles.add("single_event");
    }
    
    if (event.getMatch() != null)
    {
      // re-use the style from KWIC, which means we have to add a vaadin-specific
      // prefix
      styles.add("v-table-cell-content-" 
        + MatchedNodeColors.colorClassByMatch(event.getMatch()));
    }


    return styles;
  }
  

  public Map<String, ArrayList<Row>> getRowsByAnnotation()
  {
    return rowsByAnnotation;
  }

  public void setRowsByAnnotation(Map<String, ArrayList<Row>> rowsByAnnotation)
  {
    this.rowsByAnnotation = rowsByAnnotation;
  }
}
