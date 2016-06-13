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
package annis.gui.widgets.gwt.client.ui;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.ConnectorMap;
import com.vaadin.client.Paintable;
import com.vaadin.client.WidgetUtil;
import com.vaadin.client.ui.VCustomComponent;
import com.vaadin.client.ui.dd.VDragAndDropManager;
import com.vaadin.client.ui.dd.VDragEvent;
import com.vaadin.client.ui.dd.VTransferable;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class VGripDragComponent extends VCustomComponent 
{
  
   /** Set the CSS class name to allow styling. */
  public static final String CLASSNAME = "v-moveablepanel";
  public ApplicationConnection client;
  
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
    transferable.setDragSource(ConnectorMap.get(client).getConnector(
      this));

    Element targetElement = (Element) event.getEventTarget().cast();
    
    Paintable paintable;
    Widget w = WidgetUtil.findWidget(targetElement, null);
    
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

    transferable.setData("clientX", event.getClientX());
    transferable.setData("clientY", event.getClientY());

    dragEvent.createDragImage(getElement(), true);

    return true;

  }

}