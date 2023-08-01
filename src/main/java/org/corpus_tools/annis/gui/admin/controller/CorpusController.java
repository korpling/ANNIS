/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
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

package org.corpus_tools.annis.gui.admin.controller;

import com.google.common.base.Joiner;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import org.corpus_tools.annis.gui.CriticalServiceQueryException;
import org.corpus_tools.annis.gui.ServiceQueryException;
import org.corpus_tools.annis.gui.admin.model.CorpusManagement;
import org.corpus_tools.annis.gui.admin.view.CorpusListView;
import org.corpus_tools.annis.gui.admin.view.UIView;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class CorpusController implements CorpusListView.Listener, UIView.Listener {

    private static final long serialVersionUID = -3931526662554533493L;
    private final CorpusManagement model;
    private final CorpusListView view;
    private final UIView uiView;
    private boolean isLoggedIn = false;
    private boolean viewIsActive = false;

    public CorpusController(CorpusManagement model, CorpusListView view, UIView uiView, boolean isLoggedIn) {
        this.model = model;
        this.view = view;
        this.uiView = uiView;
        this.isLoggedIn = isLoggedIn;
        view.addListener(CorpusController.this);
        uiView.addListener(CorpusController.this);
    }

    private void clearModel() {
        model.clear();
        view.setAvailableCorpora(model.getCorpora());
    }

    @Override
    public void deleteCorpora(Set<String> corpusName) {
        Set<String> deleted = new LinkedHashSet<>();
        for (String c : corpusName) {
            try {
                model.delete(c);
                deleted.add(c);
            } catch (CriticalServiceQueryException ex) {
              uiView.showWarning(ex.getMessage(), null);
            } catch (ServiceQueryException ex) {
              uiView.showInfo(ex.getMessage(), null);
            }
        }
        if (!deleted.isEmpty()) {
            uiView.showInfo("Deleted corpora: " + Joiner.on(", ").join(deleted), null);
        }
        view.setAvailableCorpora(model.getCorpora());
    }

    private void fetchFromService() {
        try {
            model.fetchFromService();
            view.setAvailableCorpora(model.getCorpora());
        } catch (CriticalServiceQueryException ex) {
            uiView.showWarning("Cannot get the corpus list", null);
            view.setAvailableCorpora(new LinkedList<>());
        } catch (ServiceQueryException ex) {
            uiView.showInfo("Cannot get the corpus list", null);
            view.setAvailableCorpora(new LinkedList<>());
        }
    }

    @Override
    public void loadedTab(Object selectedTab) {
        viewIsActive = selectedTab == view;
        if (isLoggedIn && viewIsActive) {
            fetchFromService();
        }
    }

    @Override
    public void loginChanged(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
        if (isLoggedIn && viewIsActive) {
            fetchFromService();
        } else {
            clearModel();
        }
    }

}
