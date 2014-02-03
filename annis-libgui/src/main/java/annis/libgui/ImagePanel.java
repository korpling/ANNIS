package annis.libgui;

/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
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


import com.vaadin.ui.Embedded;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;

/**
 * Panel that displays an image.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ImagePanel extends Panel
{
  public ImagePanel(Embedded image)
  {
    setWidth("100%");
    setHeight("-1px");
    
    VerticalLayout layout = new VerticalLayout();
    setContent(layout);
    
    // enable scrolling
    layout.setSizeUndefined();
    
    addStyleName(ChameleonTheme.PANEL_BORDERLESS);
    layout.addComponent(image);
  }
}
