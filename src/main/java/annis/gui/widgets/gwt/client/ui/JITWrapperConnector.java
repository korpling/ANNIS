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
package annis.gui.widgets.gwt.client.ui;

import annis.gui.widgets.JITWrapper;
import com.vaadin.client.ui.LegacyConnector;
import com.vaadin.shared.ui.Connect;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@Connect(JITWrapper.class)
public class JITWrapperConnector extends LegacyConnector { // NO_UCD (unused code)

  /**
   * 
   */
  private static final long serialVersionUID = 1713938272181550567L;

  @Override
  public VJITWrapper getWidget() {
    return (VJITWrapper) super.getWidget();
  }

}
