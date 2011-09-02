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

import com.vaadin.ui.Accordion;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author thomas
 */
public class ControlPanel extends Panel
{
  public ControlPanel()
  {
    super("Search Form");
    
    addStyleName("control");
    
    VerticalLayout layout = (VerticalLayout) getContent();
    layout.setHeight(100f, UNITS_PERCENTAGE);
    
    QueryPanel queryPanel = new QueryPanel();
    addComponent(queryPanel);
    queryPanel.setHeight(18f, Layout.UNITS_EM);
    
    Accordion accordion = new Accordion();
    addComponent(accordion);
    accordion.setHeight(100f, Layout.UNITS_PERCENTAGE);
    
    accordion.addTab(new CorpusListPanel(), "Corpus List", null);
    accordion.addTab(new SearchOptionsPanel(), "Search Options", null);
    accordion.addTab(new ExportPanel(), "Export", null);
    
    layout.setExpandRatio(accordion, 1.0f);
        
  }
}
