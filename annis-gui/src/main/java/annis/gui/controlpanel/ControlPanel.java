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
package annis.gui.controlpanel;

import com.vaadin.terminal.gwt.server.WebBrowser;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;

/**
 *
 * @author thomas
 */
public class ControlPanel extends Panel
{
  public ControlPanel()
  {
    setWidth(345f, Layout.UNITS_PIXELS);    
    setHeight(100, Layout.UNITS_PERCENTAGE);
    
    addStyleName("control");
    addComponent(new Label("background should be orange"));
  }
}
