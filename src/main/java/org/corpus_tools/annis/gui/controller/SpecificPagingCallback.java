/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
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
package org.corpus_tools.annis.gui.controller;

import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.SearchView;
import org.corpus_tools.annis.gui.objects.DisplayedResultQuery;
import org.corpus_tools.annis.gui.paging.PagingCallback;
import org.corpus_tools.annis.gui.resultview.ResultViewPanel;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class SpecificPagingCallback implements PagingCallback {
    /**
     * 
     */
    private static final long serialVersionUID = 2454609714467964162L;

    private final ResultViewPanel panel;

    private final SearchView searchView;
    private final AnnisUI ui;
    private final DisplayedResultQuery query;

    public SpecificPagingCallback(AnnisUI ui, SearchView searchView, ResultViewPanel panel,
            DisplayedResultQuery initialQuery) {
        this.panel = panel;
        this.ui = ui;
        this.searchView = searchView;
        this.query = initialQuery;
    }

    @Override
    public void switchPage(long offset, int limit) {
        query.setOffset(offset);
        query.setLimit(limit);
        ui.getQueryController().setQuery(query);
        // execute the result query again
        updateMatches(ui.getQueryController().getSearchQuery(), panel);

    }

    private void updateMatches(DisplayedResultQuery newQuery, ResultViewPanel panel) {
        if (panel != null) {
            searchView.updateFragment(newQuery);
            searchView.getControlPanel().getQueryPanel().getPiCount().setVisible(true);
            ui.getQueryController().executeFindSearch(newQuery, panel, ui);
        }
    }

}
