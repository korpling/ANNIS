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
import com.vaadin.ui.*;
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

  public ControlPanel(QueryController controller, InstanceConfig instanceConfig,
    ExampleQueriesPanel autoGenQueries)
  {
    setSizeFull();

    setStyleName(ChameleonTheme.PANEL_BORDERLESS);
    addStyleName("control");

    Accordion accordion = new Accordion();
    accordion.setHeight(100f, Layout.UNITS_PERCENTAGE);
    accordion.setWidth(100f, Layout.UNITS_PERCENTAGE);

    corpusList = new CorpusListPanel(controller, instanceConfig, autoGenQueries);

    searchOptions = new SearchOptionsPanel();

    queryPanel = new QueryPanel(controller, instanceConfig);
    queryPanel.setHeight("-1px");
    queryPanel.setWidth("100%");

    accordion.addTab(corpusList, "Corpus List", null);
    accordion.addTab(searchOptions, "Search Options", null);
    accordion.addTab(new ExportPanel(queryPanel, corpusList, controller), "Export", null);

    addComponent(queryPanel);
    addComponent(accordion);

    setExpandRatio(accordion, 1.0f);
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
