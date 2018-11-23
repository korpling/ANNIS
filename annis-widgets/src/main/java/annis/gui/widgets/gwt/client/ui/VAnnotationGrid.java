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
package annis.gui.widgets.gwt.client.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.Paintable;
import com.vaadin.client.UIDL;
import com.vaadin.client.WidgetUtil;

/**
 *
 * @author thomas
 */
public class VAnnotationGrid extends Composite implements Paintable
{

  /**
   * Set the CSS class name to allow styling.
   */
  public static final String CLASSNAME = "v-annotationgrid";

  /**
   * The client side widget identifier
   */
  protected String paintableId;

  /**
   * Reference to the server connection object.
   */
  ApplicationConnection gClient;

  private AnnotationGridTable table;

  private FlexTable.FlexCellFormatter formatter;

  private BiMap<Position, String> position2id;

  private Map<String, String[]> highlighted;

  private Map<Position, Double> startTimes;

  private Map<Position, Double> endTimes;

  // build maps row and col to a pdf page number
  private Map<Position, String> pdfPageNumbers;

  /**
   * when true, all html tags are rendered as text and are shown in grid cells.
   * Does not effect row captions.
   */
  private boolean escapeHTML = true;

  /**
   * The constructor should first call super() to initialize the component and
   * then handle any initialization relevant to Vaadin.
   */
  public VAnnotationGrid()
  {
    super();

    table = new AnnotationGridTable();

    formatter = table.getFlexCellFormatter();

    // we are wrapping the table element
    initWidget(table);

    // This method call of the Paintable interface sets the component
    // style name in DOM tree
    setStyleName(CLASSNAME);

    highlighted = new HashMap<String, String[]>();
    position2id = HashBiMap.create();
    startTimes = new HashMap<Position, Double>();
    endTimes = new HashMap<Position, Double>();
    pdfPageNumbers = new HashMap<Position, String>();
  }

  /**
   * Called whenever an update is received from the server
   */
  @Override
  public void updateFromUIDL(UIDL uidl, ApplicationConnection client)
  {

    // This call should be made first.
    // It handles sizes, captions, tooltips, etc. automatically.
    if (client.updateComponent(this, uidl, true))
    {
      // If client.updateComponent returns true there has been no changes and we
      // do not need to update anything.
      return;
    }

    // Save reference to server connection object to be able to send
    // user interaction later
    this.gClient = client;

    // Save the client side identifier (paintable id) for the widget
    paintableId = uidl.getId();

    try
    {
      if (uidl.hasAttribute("escapeHTML")){
        this.escapeHTML = uidl.getBooleanAttribute("escapeHTML");
      }

      UIDL rows = uidl.getChildByTagName("rows");
      if (rows != null)
      {
        // clear all old table cells
        table.removeAllRows();
        highlighted.clear();
        position2id.clear();

        for (int i = 0; i < rows.getChildCount(); i++)
        {
          UIDL row = rows.getChildUIDL(i);
          if ("row".equals(row.getTag()))
          {
            addRow(row, i);
          }
        }
      }// end if rows not null

      // add end events if necessary to have a nicely aligned regular grid
      int maxCellCount = 0;
      for (int row = 0; row < table.getRowCount(); row++)
      {
        maxCellCount = Math.max(maxCellCount, getRealColumnCount(row));
      }

      for (int row = 0; row < table.getRowCount(); row++)
      {
        int isValue = getRealColumnCount(row);

        if (isValue < maxCellCount)
        {
          int diff = maxCellCount - isValue;
          table.setHTML(row, table.getCellCount(row) + diff - 1, "");
        }
      }

    }
    catch (Exception ex)
    {
      Logger.getLogger(VAnnotationGrid.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
    }
  }

  private int getRealColumnCount(int row)
  {
    int result = 0;
    for (int i = 0; i < table.getCellCount(row); i++)
    {
      result += formatter.getColSpan(row, i);
    }
    return result;
  }

  private void addRow(UIDL row, int rowNumber)
  {
    
    
    String caption = row.getStringAttribute("caption");
    boolean showNamespace = row.getBooleanAttribute("show-namespace");
    String style = row.hasAttribute("style") ? row.getStringAttribute("style") : null;
    
    String name;
    if(showNamespace)
    {
      name = caption;
    }
    else
    {
      String[] captionSplit = caption.split("::");
      name = captionSplit[captionSplit.length - 1];
    }
    
    boolean showCaption = row.getBooleanAttribute("show-caption");

    int startColumn = 0;
    
    if(showCaption)
    {
      table.setHTML(rowNumber, 0, WidgetUtil.escapeHTML(name));
      formatter.addStyleName(rowNumber, 0, "header");
      startColumn = 1;
    }
    
    if(style != null && !style.isEmpty())
    {
      table.getRowFormatter().addStyleName(rowNumber, style);
    }
    
    int colspanOffset = 0;

    UIDL events = row.getChildByTagName("events");
    for (int j = 0; j < events.getChildCount(); j++)
    {
      UIDL event = events.getChildUIDL(j);
      String id = event.getStringAttribute("id");
      int left = event.getIntAttribute("left");
      int right = event.getIntAttribute("right");
      String value = event.getStringAttribute("value");
      if(escapeHTML)
      {
        value = WidgetUtil.escapeHTML(value);
      }
      

      // +1 because we also have a caption column, subtract columns we
      // jumped over by using colspan
      int col = left + startColumn - colspanOffset;

      table.setHTML(rowNumber, col, value);
      
      if (event.hasAttribute("tooltip"))
      {
        Element tdElement = table.getCellFormatter().getElement(rowNumber, col);
        tdElement.setTitle(event.getStringAttribute("tooltip"));
      }
      
      position2id.put(new Position(rowNumber, col), id);

      int colspan = right - left + 1;
      formatter.setColSpan(rowNumber, col, colspan);
      if (colspan > 1)
      {
        colspanOffset += (colspan - 1);
      }

      addStyleForEvent(event, rowNumber, col);

    }
  }

  private void addStyleForEvent(UIDL event, int rowNumber, int col)
  {
    String id = event.getStringAttribute("id");

    // style given by the server component
    if (event.hasAttribute("style"))
    {
      String[] styles = event.getStringArrayAttribute("style");
      for (String s : styles)
      {
        formatter.addStyleName(rowNumber, col, s);
      }
    }
    else
    {
      formatter.addStyleName(rowNumber, col, "single_event");
    }

    // fill highlight map
    if (event.hasAttribute("highlight"))
    {
      highlighted.put(id, event.getStringArrayAttribute("highlight"));
    }

    if (event.hasAttribute("startTime"))
    {
      formatter.addStyleName(rowNumber, col, "speaker");
      startTimes.put(new Position(rowNumber, col), event.getDoubleAttribute(
        "startTime"));
      if (event.hasAttribute("endTime"))
      {
        endTimes.put(new Position(rowNumber, col), event.getDoubleAttribute(
          "endTime"));
      }
    }

    if (event.hasAttribute("openPDF"))
    {
      String number = event.getStringAttribute("openPDF");
      formatter.addStyleName(rowNumber, col, "pdf");
      pdfPageNumbers.put(new Position(rowNumber, col), number);
    }
  }

  public void onClick(int row, int col)
  {

    Position pos = new Position(row, col);
    if (startTimes.containsKey(pos))
    {
      if (endTimes.containsKey(pos))
      {
        gClient.updateVariable(paintableId, "play",
          "" + startTimes.get(pos) + "-" + endTimes.get(pos), true);
      }
      else
      {
        gClient.updateVariable(paintableId, "play", "" + startTimes.get(pos),
          true);
      }

    }

    if (pdfPageNumbers.containsKey(pos))
    {
      gClient.updateVariable(paintableId, "openPDF", pdfPageNumbers.get(pos),
        true);
    }
  }

  public static class Position
  {

    private int column, row;

    public Position(Cell cell)
    {
      this.column = cell.getCellIndex();
      this.row = cell.getRowIndex();
    }

    public Position(int row, int column)
    {
      this.column = column;
      this.row = row;
    }

    public int getColumn()
    {
      return column;
    }

    public void setColumn(int column)
    {
      this.column = column;
    }

    public int getRow()
    {
      return row;
    }

    public void setRow(int row)
    {
      this.row = row;
    }

    @Override
    public int hashCode()
    {
      int hash = 7;
      hash = 71 * hash + this.column;
      hash = 71 * hash + this.row;
      return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
      if (obj == null)
      {
        return false;
      }
      if (getClass() != obj.getClass())
      {
        return false;
      }
      final Position other = (Position) obj;
      if (this.column != other.column)
      {
        return false;
      }
      if (this.row != other.row)
      {
        return false;
      }
      return true;
    }
  }

  public class AnnotationGridTable extends FlexTable
  {

    public AnnotationGridTable()
    {
      sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT | Event.ONCLICK);
    }

    @Override
    public void onBrowserEvent(Event event)
    {
      Element td = getEventTargetCell(event);
      if (td == null)
      {
        return;
      }

      int row = TableRowElement.as(td.getParentElement()).getSectionRowIndex();
      int column = TableCellElement.as(td).getCellIndex();

      String id = position2id.get(new Position(row, column));
      String[] targetIDs = highlighted.get(id);

      // only do something if the cell is highlighting other cells
      if (targetIDs != null && targetIDs.length > 0)
      {
        switch (event.getTypeInt())
        {
          case Event.ONMOUSEOVER:
            td.addClassName("highlight-source");

            for (String targetID : targetIDs)
            {
              Position pos = position2id.inverse().get(targetID);
              if (pos != null)
              {
                formatter.addStyleName(pos.getRow(), pos.getColumn(),
                  "highlight-target");
              }
            }

            break;
          case Event.ONMOUSEOUT:
            td.removeClassName("highlight-source");
            for (String targetID : targetIDs)
            {
              Position pos = position2id.inverse().get(targetID);
              if (pos != null)
              {
                formatter.removeStyleName(pos.getRow(), pos.getColumn(),
                  "highlight-target");
              }
            }
            break;
          case Event.ONCLICK:
            onClick(row, column);
            break;
          default:
            // do nothing
            break;
        }
      }
    }
  }
}
