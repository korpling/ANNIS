/*
 * Copyright 2013 SFB 632.
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
package org.corpus_tools.annis.gui.widgets.gwt.client.ui;

import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.Paintable;
import com.vaadin.client.UIDL;
import com.vaadin.client.ui.customcomponent.CustomComponentConnector;
import com.vaadin.shared.ui.Connect;
import org.corpus_tools.annis.gui.widgets.GripDragComponent;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@Connect(GripDragComponent.class)
public class GripDragComponentConnector extends CustomComponentConnector implements Paintable { // NO_UCD
                                                                                                // (unused
                                                                                                // code)

  /**
   * 
   */
  private static final long serialVersionUID = 7549396191697093527L;

  public GripDragComponentConnector() {

  }

  @Override
  public final VGripDragComponent getWidget() {
    return (VGripDragComponent) super.getWidget();
  }

  @Override
  public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
    getWidget().client = client;
  }



}
