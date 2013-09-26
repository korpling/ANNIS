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

import annis.gui.ExampleQueriesPanel;
import annis.libgui.InstanceConfig;
import annis.gui.QueryController;
import annis.gui.SearchUI;
import annis.gui.frequency.FrequencyResultPanel;
import annis.gui.resultview.ResultViewPanel;
import com.vaadin.ui.*;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.themes.ChameleonTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This panel allows the user to control and execute queries.
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class ControlPanel extends VerticalLayout
{

  private static final Logger log = LoggerFactory.getLogger(ControlPanel.class);

  private static final long serialVersionUID = -2220211539424865671L;

  private QueryPanel queryPanel;
  private CorpusListPanel corpusList;

  private SearchOptionsPanel searchOptions;
  
  private Tab optionTab;
  private TabSheet optionsTab;
  private SearchUI ui;


  public ControlPanel(QueryController controller, InstanceConfig instanceConfig,
    ExampleQueriesPanel autoGenQueries, SearchUI ui)
  {
    this.ui = ui;
    
    setSizeFull();

    setStyleName(ChameleonTheme.PANEL_BORDERLESS);
    addStyleName("control");

    optionsTab = new TabSheet();
    optionsTab.setHeight(100f, Layout.UNITS_PERCENTAGE);
    optionsTab.setWidth(100f, Layout.UNITS_PERCENTAGE);
    optionsTab.addStyleName("blue-tab");

    corpusList = new CorpusListPanel(controller, instanceConfig, autoGenQueries, ui);
    
    searchOptions = new SearchOptionsPanel();

    queryPanel = new QueryPanel(ui);
    queryPanel.setHeight("-1px");
    queryPanel.setWidth("100%");
    
    optionsTab.addTab(corpusList, "Corpus List", null);
    optionTab = optionsTab.addTab(searchOptions, "Search Options", null);

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
