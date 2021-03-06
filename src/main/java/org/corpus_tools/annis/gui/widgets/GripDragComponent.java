/*
 * Copyright 2012 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corpus_tools.annis.gui.widgets;

import com.vaadin.event.Transferable;
import com.vaadin.event.TransferableImpl;
import com.vaadin.event.dd.DragSource;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.LegacyComponent;
import java.util.Map;

/**
 * A draggabe panel where only a specific grip is the starting point for the drag action.
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class GripDragComponent extends CustomComponent implements DragSource, LegacyComponent {
  public static class MouseEventTransferable extends TransferableImpl {
    /**
     * 
     */
    private static final long serialVersionUID = -6202563429683486884L;

    public MouseEventTransferable(Component sourceComponent, Map<String, Object> rawVariables) {
      super(sourceComponent, rawVariables);
    }

    public int getClientX() {
      return (Integer) getData("clientX");
    }

    public int getClientY() {
      return (Integer) getData("clientY");
    }

  }

  /**
   * 
   */
  private static final long serialVersionUID = 7226834145811552162L;

  public GripDragComponent(Component panel) {
    super(panel);

  }

  @Override
  public void changeVariables(Object source, Map<String, Object> variables) {

  }

  @Override
  public Transferable getTransferable(Map<String, Object> rawVariables) {
    return new MouseEventTransferable(getCompositionRoot(), rawVariables);
  }

  @Override
  public void paintContent(PaintTarget target) throws PaintException {}


}
