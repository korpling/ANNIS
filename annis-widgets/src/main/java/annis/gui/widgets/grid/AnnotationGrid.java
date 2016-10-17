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
import annis.libgui.media.PDFController;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.LegacyComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class AnnotationGrid extends AbstractComponent implements LegacyComponent
{

  private Map<String, ArrayList<Row>> rowsByAnnotation;

  private transient MediaController mediaController;

  private transient PDFController pdfController;

  private String resultID;

  private int tokenIndexOffset;

  private String tokRowKey = "tok";
  
  private boolean showCaption = true;
  
  private Set<String> annosWithNamespace;
  
  /**
   * when true, all html tags are rendered as text and are shown in grid cells.
   * Does not effect row captions.
   */
  private boolean escapeHTML = true;

  /**
   * Returns a generic Grid-Object.
   *
   * @param resultID The salt Id of the result.
   */
  public AnnotationGrid(String resultID)
  {
    this(null, null, resultID);
  }

  /**
   * Returns a generic Grid-Object.
   *
   * @param resultID The salt Id of the result.
   * @param tokRowKey Defines the tok row and applies the token style. If null
   * the "tok" value is used.
   */
  public AnnotationGrid(String resultID, String tokRowKey)
  {
    this(null, null, resultID);
    if (tokRowKey != null)
    {
      this.tokRowKey = tokRowKey;
    }
  }

  public AnnotationGrid(MediaController mediaController,
    PDFController pdfController, String resultID)
  {
    this.mediaController = mediaController;
    this.pdfController = pdfController;
    this.resultID = resultID;
    this.tokenIndexOffset = 0;
  }

  @Override
  public void changeVariables(Object source,
    Map<String, Object> variables)
  {

    if (variables.containsKey("play"))
    {
      if (mediaController != null && resultID != null)
      {
        String playString = (String) variables.get("play");
        String[] split = playString.split("-");

        if (split.length == 1)
        {
          mediaController.play(resultID, Double.parseDouble(split[0]));
        }
        else if (split.length == 2)
        {
          mediaController.play(resultID,
            Double.parseDouble(split[0]), Double.parseDouble(split[1]));
        }
      }
    }

    if (variables.containsKey("openPDF"))
    {
      if (pdfController != null && resultID != null)
      {
        pdfController.openPDF(resultID, (String) variables.get("openPDF"));
      }
    }
  }

  @Override
  public void paintContent(PaintTarget target) throws PaintException
  {

    target.addAttribute("escapeHTML", escapeHTML);

    if (rowsByAnnotation != null)
    {
      target.startTag("rows");
      for (Map.Entry<String, ArrayList<Row>> anno : rowsByAnnotation.entrySet())
      {

        for (Row row : anno.getValue())
        {
          target.startTag("row");
          target.addAttribute("caption", anno.getKey());
          target.addAttribute("show-caption", showCaption);
          target.addAttribute("show-namespace", showNamespaceForAnno(anno.getKey()));
          if(row.getStyle() != null)
          {
            target.addAttribute("style", row.getStyle());
          }

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
            target.addAttribute("left", event.getLeft() - tokenIndexOffset);
            target.addAttribute("right", event.getRight() - tokenIndexOffset);
            target.addAttribute("value", event.getValue());

            if (event.getTooltip() != null)
            {
              target.addAttribute("tooltip", event.getTooltip());
            }

            if (event.getStartTime() != null)
            {
              target.addAttribute("startTime", event.getStartTime());
              if (event.getEndTime() != null)
              {
                target.addAttribute("endTime", event.getEndTime());
              }
            }

            if (event.getPageNumber() != null)
            {
              target.addAttribute("openPDF", event.getPageNumber());
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
  
  private boolean showNamespaceForAnno(String qname)
  {
    if(annosWithNamespace != null)
    {
      return annosWithNamespace.contains(qname);
    }
    else
    {
      return false;
    }
  }

  private ArrayList<String> getStyles(GridEvent event, String annoName)
  {
    ArrayList<String> styles = new ArrayList<>();

    if (tokRowKey.equals(annoName))
    {
      styles.add("token");
    }
    else if(event.isSpace())
    {
      styles.add("space");
    }
    else if (event.isGap())
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

  public int getTokenIndexOffset()
  {
    return tokenIndexOffset;
  }

  /**
   * Set an offset for the token index. It is assumed that firstNodeLeft -
   * offset == 0.
   *
   * @param tokenIndexOffset
   */
  public void setTokenIndexOffset(int tokenIndexOffset)
  {
    this.tokenIndexOffset = tokenIndexOffset;
  }

  /**
   * Defines, if the grid visualization render html as text.
   *
   * @param escapeHTML the escapeHTML to set
   */
  public void setEscapeHTML(boolean escapeHTML)
  {
    this.escapeHTML = escapeHTML;
  }

  /**
   * Defines if the caption column should be shown.
   * @return 
   */
  public boolean isShowCaption()
  {
    return showCaption;
  }

  /**
   * Defines if the caption column should be shown.
   * @param showCaption 
   */
  public void setShowCaption(boolean showCaption)
  {
    this.showCaption = showCaption;
  }

  /**
   * A set of qualified names for annotations which namespace should be shown.
   * @return 
   */
  public Set<String> getAnnosWithNamespace()
  {
    return annosWithNamespace;
  }

  public void setAnnosWithNamespace(Set<String> annosWithNamespace)
  {
    this.annosWithNamespace = annosWithNamespace;
  }

  
}
