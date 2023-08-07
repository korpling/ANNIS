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

import com.vaadin.ui.UI;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.SearchView;
import org.corpus_tools.annis.gui.objects.DisplayedResultQuery;
import org.corpus_tools.annis.gui.resultview.ResultViewPanel;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class PagingCallback {

    private static final long serialVersionUID = 2454609714467964162L;

    private final ResultViewPanel panel;

    private final SearchView searchView;
    private final QueryController queryController;
    private final DisplayedResultQuery query;

    public PagingCallback(QueryController queryController, SearchView searchView,
        ResultViewPanel panel,
            DisplayedResultQuery initialQuery) {
        this.panel = panel;
        this.queryController = queryController;
        this.searchView = searchView;
        this.query = initialQuery;
    }

    public void switchPage(long offset, int limit) {
        query.setOffset(offset);
        query.setLimit(limit);
        queryController.setQuery(query);
        // execute the result query again
        updateMatches(queryController.getSearchQuery(), panel);

    }

    private void updateMatches(DisplayedResultQuery newQuery, ResultViewPanel panel) {
        if (panel != null) {
            searchView.updateFragment(newQuery);
            searchView.getControlPanel().getQueryPanel().getPiCount().setVisible(true);
            if (UI.getCurrent() instanceof AnnisUI) {
              queryController.executeFindSearch(newQuery, panel, (AnnisUI) UI.getCurrent());
            }
        }
    }

}
