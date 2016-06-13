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

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import annis.gui.AnnisUI;
import annis.gui.ExampleQueriesPanel;
import annis.libgui.InstanceConfig;

/**
 * This panel allows the user to control and execute queries.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ControlPanel extends VerticalLayout
{

  private static final long serialVersionUID = -2220211539424865671L;

  private QueryPanel queryPanel;
  private CorpusListPanel corpusList;

  private SearchOptionsPanel searchOptions;
  
  private TabSheet optionsTab;
  
  public ControlPanel(InstanceConfig instanceConfig,
    ExampleQueriesPanel autoGenQueries, AnnisUI ui)
  {
    setSizeFull();
    setMargin(true);

    setStyleName(ValoTheme.PANEL_BORDERLESS);

    queryPanel = new QueryPanel(ui);
    queryPanel.setHeight("-1px");
    queryPanel.setWidth("100%");
    
    optionsTab = new TabSheet();
    optionsTab.setHeight("100%");
    optionsTab.setWidth("100%");
    optionsTab.addStyleName(ValoTheme.TABSHEET_FRAMED);

    corpusList = new CorpusListPanel(instanceConfig, autoGenQueries, ui);
    
    searchOptions = new SearchOptionsPanel();

    optionsTab.addTab(corpusList, "Corpus List", null);
    optionsTab.addTab(searchOptions, "Search Options", null);
   
    addComponent(queryPanel);
    addComponent(optionsTab);
    
    setExpandRatio(optionsTab, 1.0f);
  }
  
  public CorpusListPanel getCorpusList()
  {
    return corpusList;
  }

  public QueryPanel getQueryPanel()
  {
    return queryPanel;
  }

  public SearchOptionsPanel getSearchOptions()
  {
    return searchOptions;
  }  
}
