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
package annis.gui.widgets;

import annis.gui.widgets.gwt.client.VGripDragComponent;
import com.vaadin.event.Transferable;
import com.vaadin.event.TransferableImpl;
import com.vaadin.event.dd.DragSource;
import com.vaadin.terminal.gwt.client.MouseEventDetails;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import java.util.Map;

/**
 * A draggabe panel where only a specific grip is the starting point for the drag action.
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@ClientWidget(VGripDragComponent.class)
public class GripDragComponent extends CustomComponent implements DragSource
{
  public GripDragComponent(Component panel)
  {
    super(panel);
    
  }

  public Transferable getTransferable(Map<String, Object> rawVariables)
  {
    return new MouseEventTransferable(getCompositionRoot(), rawVariables);
  }
  
  public static class MouseEventTransferable extends TransferableImpl
  {
    public MouseEventTransferable(Component sourceComponent, Map<String, Object> rawVariables)
    {
      super(sourceComponent, rawVariables);
    }
    
    public MouseEventDetails getMouseDownEvent()
    {
      return MouseEventDetails.deSerialize((String) getData("mouseDown"));
    }

    
  }
  
  
}
