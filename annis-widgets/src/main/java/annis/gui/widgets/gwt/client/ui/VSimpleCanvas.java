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

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.Paintable;
import com.vaadin.client.UIDL;
import java.util.Iterator;

/**
 *
 * @author thomas
 */
public class VSimpleCanvas extends Composite implements Paintable
{

  /** Set the CSS class name to allow styling. */
  public static final String CLASSNAME = "v-simplecanvas";
  /** The client side widget identifier */
  //protected String paintableId;
  /** Reference to the server connection object. */
  //ApplicationConnection gClient;
  
  static final int height = 2000;
  static final int width = 2000;
  
  Canvas canvas;
  Context2d context;

  /**
   * The constructor should first call super() to initialize the component and
   * then handle any initialization relevant to Vaadin.
   */
  public VSimpleCanvas()
  {
    super();

    canvas = Canvas.createIfSupported();

    if(canvas == null)
    {
      Label lblErrorMessage =
        new Label("Your browser does not support the Canvas element.");
      initWidget(lblErrorMessage);
    }
    else
    {
      initWidget(canvas);
      
      canvas.setHeight("" + height + "px");
      canvas.setWidth("" + width + "px");
      canvas.setCoordinateSpaceHeight(height);
      canvas.setCoordinateSpaceWidth(width);
      
      context = canvas.getContext2d();
    }

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
    //this.gClient = client;

    // Save the client side identifier (paintable id) for the widget
    //paintableId = uidl.getId();

    if(context != null)
    {
      Iterator<Object> it = uidl.getChildIterator();
      while(it.hasNext())
      {
        UIDL child = (UIDL) it.next();

        if("clear".equals(child.getTag()))
        {
          context.clearRect(0, 0, context.getCanvas().getWidth(), context.getCanvas().getHeight());
        }
        else if("line".equals(child.getTag()))
        {
          context.setGlobalAlpha(1.0);
          context.setLineWidth(1.0);
          context.setStrokeStyle("black");
          context.beginPath();
          context.moveTo(child.getIntAttribute("from_x"),
            child.getIntAttribute("from_y"));
          context.lineTo(child.getIntAttribute("to_x"),
            child.getIntAttribute("to_y"));
          context.closePath();
          context.stroke();
        }
        // todo: more commands :)
      }

    }
  }
}
