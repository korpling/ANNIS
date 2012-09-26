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
package annis.gui.widgets.gwt.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;
import com.vaadin.terminal.gwt.client.ui.VLabel;
import java.util.Iterator;

/**
 *
 * @author thomas
 */
public class VAnnotationGrid extends Composite implements Paintable
{

  /** Set the CSS class name to allow styling. */
  public static final String CLASSNAME = "v-annotationgrid";
  /** The client side widget identifier */
  protected String paintableId;
  /** Reference to the server connection object. */
  ApplicationConnection gClient;
  
  private FlexTable table;
  private FlexTable.FlexCellFormatter formatter;
  
  /**
   * The constructor should first call super() to initialize the component and
   * then handle any initialization relevant to Vaadin.
   */
  public VAnnotationGrid()
  {
    super();

    table = new FlexTable();
    formatter = table.getFlexCellFormatter();
    
    // we are wrapping the table element
    initWidget(table);
        
    // This method call of the Paintable interface sets the component
    // style name in DOM tree
    setStyleName(CLASSNAME);

  }

  /**
   * Called whenever an update is received from the server 
   */
  @Override
  public void updateFromUIDL(UIDL uidl, ApplicationConnection client)
  {

    // This call should be made first. 
    // It handles sizes, captions, tooltips, etc. automatically.
    if(client.updateComponent(this, uidl, true))
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
    
    UIDL rows = uidl.getChildByTagName("rows");
    if(rows != null)
    {
      // clear all old table cells
      table.removeAllRows();

      for(int i=0; i < rows.getChildCount(); i++)
      {
        UIDL row = rows.getChildUIDL(i);
        if("row".equals(row.getTag()))
        {
          String caption = row.getStringAttribute("caption");

          String[] captionSplit = caption.split("::");
          String name = captionSplit[captionSplit.length-1];

          VLabel lblCaption = new VLabel(name);
          table.setWidget(i, 0, lblCaption);
          formatter.addStyleName(i, 0, "header");
          
          UIDL events = row.getChildByTagName("events");
          for(int j=0; j < events.getChildCount(); j++)
          {
            UIDL event = events.getChildUIDL(j);
            int left = event.getIntAttribute("left");
            int right = event.getIntAttribute("right");
            String value = event.getStringAttribute("value");
            
            VLabel label = new VLabel(value);
            label.setTitle(caption);
            
            int col = left+1; // +1 because we also have a caption column
            
            table.setWidget(i, col, label);
            formatter.setColSpan(i, col, (right-left+1));
            
            if(event.hasAttribute("style"))
            {
              formatter.addStyleName(i, col, event.getStringAttribute("style"));
            }
            else
            {
              formatter.addStyleName(i, col, "single_event");
            }
          }
        }
      }
    }
  }
}
