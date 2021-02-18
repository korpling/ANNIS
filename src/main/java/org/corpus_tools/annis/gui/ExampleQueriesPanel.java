/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
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
package org.corpus_tools.annis.gui;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.api.CorporaApi;
import org.corpus_tools.annis.api.model.CorpusConfiguration;
import org.corpus_tools.annis.api.model.ExampleQuery;
import org.corpus_tools.annis.gui.controlpanel.ControlPanel;
import org.corpus_tools.annis.gui.controlpanel.CorpusListPanel;
import org.corpus_tools.annis.gui.controlpanel.QueryPanel;
import org.corpus_tools.annis.gui.objects.Query;
import org.corpus_tools.annis.gui.objects.QueryLanguage;
import org.corpus_tools.annis.gui.resultview.ResultViewPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps the auto generated queries.
 *
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
public class ExampleQueriesPanel extends CssLayout {

    private class ExampleFetcher implements Runnable {

        private final Set<String> selectedCorpora;

        private UI ui;

        public ExampleFetcher(Set<String> selectedCorpora, UI ui) {
            this.selectedCorpora = selectedCorpora;
            this.ui = ui;
        }

        @Override
        public void run() {
            final List<Entry> result = new LinkedList<>();
            try {
                result.addAll(loadExamplesFromRemote(selectedCorpora, ui));
            } finally {
                ui.access(() -> {
                    loadingIndicator.setVisible(false);
                    table.setVisible(true);

                    try {
                        table.setItems(result);
                        if (result.isEmpty()) {
                            hideTabSheet();
                        } else {
                            showTab();
                        }
                    } catch (Exception ex) {
                        log.error("removing or adding of example queries failed for {}",
                                selectedCorpora, ex);
                    }
                });
            }

        }

    }

    /**
     * 
     */
    private static final long serialVersionUID = -2676130295297213669L;

    // gets the
    private final static Logger log = LoggerFactory.getLogger(ExampleQueriesPanel.class);

    private static final Resource SEARCH_ICON = VaadinIcons.SEARCH;

    /**
     * Loads the available example queries for a specific corpus.
     *
     * @param corpusNames Specifies the corpora example queries are fetched for. If it is null or
     *        empty all available example queries are fetched.
     */
    private static List<Entry> loadExamplesFromRemote(Set<String> corpusNames, UI ui) {

        List<Entry> result = new LinkedList<>();
        CorporaApi api = new CorporaApi(Helper.getClient(ui));
        try {
            if (corpusNames != null && !corpusNames.isEmpty()) {
                for (String c : corpusNames) {
                    CorpusConfiguration config = api.corpusConfiguration(c);
                    if(config.getExampleQueries() != null) {
                        for (ExampleQuery q : config.getExampleQueries()) {
                            Entry e = new Entry();
                            e.corpus = c;
                            e.example = q;
                            result.add(e);
                        }
                    }
                }
            }
        } catch (ApiException ex) {
            log.error("problems with getting example queries from remote for {}", corpusNames, ex);
        }
        return result;
    }

    private final String COLUMN_EXAMPLE_QUERY = "exampleQuery";

    private final String COLUMN_OPEN_CORPUS_BROWSER = "open corpus browser";

    private final String COLUMN_DESCRIPTION = "description";

    // main ui window
    private final AnnisUI ui;

    private final Grid<Entry> table;

    private final ProgressBar loadingIndicator;


    // reference to the tab which holds this component
    private TabSheet.Tab tab;

    // hold the parent tab of annis3
    private final HelpPanel parentTab;

    public static class Entry {
        String corpus;
        ExampleQuery example;
    }

    public ExampleQueriesPanel(AnnisUI ui, HelpPanel parentTab) {
        super();
        this.ui = ui;
        this.parentTab = parentTab;

        loadingIndicator = new ProgressBar();
        loadingIndicator.setIndeterminate(true);
        loadingIndicator.setCaption("Loading example queries...");
        loadingIndicator.setVisible(false);
        addComponent(loadingIndicator);

        table = new Grid<Entry>();
        table.setVisible(true);
        addComponent(table);

        setUpTable();
    }

    private Component getOpenCorpusPanel(final String corpusName) {
        final Button btn = new Button(corpusName);

        btn.setStyleName(ValoTheme.BUTTON_LINK);
        btn.addClickListener(event -> {
            CorpusListPanel corpusList = ui.getSearchView().getControlPanel().getCorpusList();
            corpusList.initCorpusBrowser(corpusName, btn);
        });

        return btn;
    }

    private void hideTabSheet() {
        if (parentTab != null) {
            tab = parentTab.getTab(this);

            if (tab != null) {
                tab.setEnabled(false);
            }
        }
    }

    /**
     * Sets the selected corpora and causes a reload
     *
     * @param selectedCorpora Specifies the corpora example queries are fetched for. If it is null,
     *        all available example queries are fetched.
     */
    public void setSelectedCorpusInBackground(final Set<String> selectedCorpora) {
        loadingIndicator.setVisible(true);
        table.setVisible(false);
        Background.run(new ExampleFetcher(selectedCorpora, UI.getCurrent()));
    }

    /**
     * Sets some layout properties.
     */
    private void setUpTable() {
        setSizeFull();
        // expand the table
        table.setSizeFull();

        // Allow selecting items from the table.
        table.setSelectionMode(SelectionMode.SINGLE);

        // set custom style
        table.addStyleName("example-queries-table");

        // configure columns
        Column<Entry, ?> corpusBrowserColumn = table.addComponentColumn(e -> {
            return getOpenCorpusPanel(e.corpus);
        });
        corpusBrowserColumn.setId(COLUMN_OPEN_CORPUS_BROWSER);
        corpusBrowserColumn.setCaption("open corpus browser");

        Column<Entry, ?> exampleQueryColumn = table.addComponentColumn(e -> {
            Button btn = new Button();
            btn.setDescription("show corpus browser for " + e.corpus);
            btn.addStyleName(ValoTheme.BUTTON_LINK);
            btn.setIcon(SEARCH_ICON);
            btn.setCaption(e.example.getQuery());
            btn.setDescription("show results for \"" + e.example.getQuery() + "\" in " + e.corpus);
            btn.addStyleName(Helper.CORPUS_FONT_FORCE);

            btn.addClickListener(event -> {
                if (ui != null) {
                    ControlPanel controlPanel = ui.getSearchView().getControlPanel();
                    QueryPanel queryPanel;

                    if (controlPanel == null) {
                        log.error("controlPanel is not initialized");
                        return;
                    }

                    queryPanel = controlPanel.getQueryPanel();
                    if (queryPanel == null) {
                        log.error("queryPanel is not initialized");
                        return;
                    }

                    Set<String> corpusNameSet = new HashSet<>();
                    corpusNameSet.add(e.corpus);
                    if (ui.getQueryController() != null) {
                        QueryLanguage ql = QueryLanguage.AQL;
                        if (e.example
                                .getQueryLanguage() == org.corpus_tools.annis.api.model.QueryLanguage.AQLQUIRKSV3) {
                            ql = QueryLanguage.AQL_QUIRKS_V3;
                        }
                        ui.getQueryController()
                                .setQuery(new Query(e.example.getQuery(), ql, corpusNameSet));
                        // ensure the selected corpus is shown
                        ui.getSearchView().getControlPanel().getCorpusList()
                                .scrollToSelectedCorpus();

                        // execute query
                        ui.getQueryController().executeSearch(true, true);
                    }
                }
            });
            return btn;
        });
        exampleQueryColumn.setId(COLUMN_EXAMPLE_QUERY);
        exampleQueryColumn.setCaption("Example Query");

        Column<Entry, ?> descriptionColumn = table.addComponentColumn(e -> {
            Label l = new Label(e.example.getDescription());
            l.setContentMode(ContentMode.TEXT);
            l.addStyleName(Helper.CORPUS_FONT_FORCE);
            return l;
        });
        descriptionColumn.setCaption("Description");
        descriptionColumn.setId(COLUMN_DESCRIPTION);

        table.setColumns(COLUMN_EXAMPLE_QUERY, COLUMN_DESCRIPTION, COLUMN_OPEN_CORPUS_BROWSER);

        exampleQueryColumn.setExpandRatio(1);
        descriptionColumn.setExpandRatio(1);

    }

    /**
     * Shows the tab and put into the foreground, if no query is executed yet.
     */
    private void showTab() {
        if (parentTab != null) {
            tab = parentTab.getTab(this);
            if (tab != null) {
                // FIXME: this should be added by the constructor or by the panel that adds this
                // tab
                // tab.getComponent().addStyleName("example-queries-tab");
                tab.setEnabled(true);

                if (!(parentTab.getSelectedTab() instanceof ResultViewPanel)) {
                    parentTab.setSelectedTab(tab);
                }
            }
        }

    }
}
