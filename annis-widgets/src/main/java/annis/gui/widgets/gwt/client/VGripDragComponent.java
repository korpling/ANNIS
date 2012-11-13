/*
 * Copyright 2012 SFB 632.
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

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.MouseEventDetails;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.Util;
import com.vaadin.terminal.gwt.client.ui.VCustomComponent;
import com.vaadin.terminal.gwt.client.ui.dd.VDragAndDropManager;
import com.vaadin.terminal.gwt.client.ui.dd.VDragEvent;
import com.vaadin.terminal.gwt.client.ui.dd.VTransferable;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class VGripDragComponent extends VCustomComponent 
{
  
   /** Set the CSS class name to allow styling. */
  public static final String CLASSNAME = "v-moveablepanel";
  
  public VGripDragComponent()
  {
    super();
    addDomHandler(new MouseDownHandler()
    {
      public void onMouseDown(MouseDownEvent event)
      {
        if (startDrag(event.getNativeEvent()))
        {
          event.preventDefault(); // prevent text selection
        }
      }
    }, MouseDownEvent.getType());

    addDomHandler(new TouchStartHandler()
    {
      public void onTouchStart(TouchStartEvent event)
      {
        if (startDrag(event.getNativeEvent()))
        {
          /*
           * Dont let eg. panel start scrolling.
           */
          event.stopPropagation();
        }
      }
    }, TouchStartEvent.getType());

    sinkEvents(Event.TOUCHEVENTS);
    
  }
  
  private boolean startDrag(NativeEvent event) 
  {
    VTransferable transferable = new VTransferable();
    transferable.setDragSource(VGripDragComponent.this);

    Element targetElement = (Element) event.getEventTarget().cast();
    
    Paintable paintable;
    Widget w = Util.findWidget(targetElement, null);
    
    if(!w.getStyleName().contains("drag-source-enabled"))
    {
      return false;
    }
    
    while (w != null && !(w instanceof Paintable)) 
    {
        w = w.getParent();
    }
    paintable = (Paintable) w;

    transferable.setData("component", paintable);
    VDragEvent dragEvent = VDragAndDropManager.get().startDrag(
            transferable, event, true);

    transferable.setData("mouseDown",
      new MouseEventDetails(event).serialize());


    dragEvent.createDragImage(getElement(), true);

    return true;

  }

}
