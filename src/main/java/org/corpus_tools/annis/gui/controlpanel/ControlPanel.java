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
package org.corpus_tools.annis.gui.controlpanel;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.HelpPanel;

/**
 * This panel allows the user to control and execute queries.
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class ControlPanel extends VerticalLayout {

    private static final long serialVersionUID = -2220211539424865671L;

    private final AnnisUI ui;

    private final QueryPanel queryPanel;

    private final CorpusListPanel corpusList;

    private final SearchOptionsPanel searchOptions;

    private final TabSheet optionsTab;

    public ControlPanel(AnnisUI ui, HelpPanel helpPanel) {
      this.ui = ui;
        setSizeFull();
        setMargin(true);

        setStyleName(ValoTheme.PANEL_BORDERLESS);

        optionsTab = new TabSheet();
        optionsTab.setHeight("100%");
        optionsTab.setWidth("100%");
        optionsTab.addStyleName(ValoTheme.TABSHEET_FRAMED);

        queryPanel = new QueryPanel(ui);
        corpusList = new CorpusListPanel(ui, helpPanel.getExamples());
        searchOptions = new SearchOptionsPanel();

        optionsTab.addTab(corpusList, "Corpus List", null);
        optionsTab.addTab(searchOptions, "Search Options", null);

        addComponent(queryPanel);
        addComponent(optionsTab);


        queryPanel.setHeightUndefined();
        queryPanel.setWidthFull();

        setExpandRatio(optionsTab, 1.0f);


    }

    public CorpusListPanel getCorpusList() {
        return corpusList;
    }

    public QueryPanel getQueryPanel() {
        return queryPanel;
    }

    public SearchOptionsPanel getSearchOptions() {
        return searchOptions;
    }
}
