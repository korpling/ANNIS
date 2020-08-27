/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui;

import annis.QueryGenerator;
import annis.gui.beans.CitationProvider;
import annis.gui.query_references.ShareQueryReferenceWindow;
import annis.libgui.Helper;
import annis.model.ContextualizedQuery;
import annis.model.DisplayedResultQuery;
import annis.model.Query;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.ui.Table;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author thomas
 */
public class CitationLinkGenerator implements Table.ColumnGenerator, Button.ClickListener {
    private static class CitationProviderForQuery implements CitationProvider {
        private final Query query;

        public CitationProviderForQuery(Query query) {
            this.query = query;
        }

        @Override
        public Set<String> getCorpora() {
            if (query == null) {
                return new HashSet<>();
            }
            return query.getCorpora();
        }

        @Override
        public int getLeftContext() {
            if (query instanceof ContextualizedQuery) {
                return ((ContextualizedQuery) query).getLeftContext();
            }
            return 5;
        }

        @Override
        public String getQuery() {
            if (query == null) {
                return null;
            }
            return query.getQuery();
        }

        @Override
        public int getRightContext() {
            if (query instanceof ContextualizedQuery) {
                return ((ContextualizedQuery) query).getRightContext();
            }
            return 5;
        }

    }

    private static class LinkClickListener implements Button.ClickListener {

        private static final long serialVersionUID = 7347095995969160586L;
        private final CitationProvider citationProvider;
        private final DisplayedResultQuery query;

        public LinkClickListener(CitationProvider citationProvider) {
            this.citationProvider = citationProvider;
            this.query = null;
        }

        public LinkClickListener(DisplayedResultQuery query) {
            this.citationProvider = null;
            this.query = query;
        }

        @Override
        public void buttonClick(ClickEvent event) {
            UI ui = UI.getCurrent();
            boolean desktopMode = false;
            if (ui instanceof AnnisUI) {
                desktopMode = ((AnnisUI) ui).getConfig().isDesktopMode();
            }

            if (query != null) {
                ShareQueryReferenceWindow c = new ShareQueryReferenceWindow(query, !desktopMode);
                UI.getCurrent().addWindow(c);
                c.center();
            } else if (citationProvider != null) {
                ShareQueryReferenceWindow c = new ShareQueryReferenceWindow(QueryGenerator
                        .displayed().query(citationProvider.getQuery())
                        .corpora(citationProvider.getCorpora())
                        .left(citationProvider.getLeftContext())
                        .right(citationProvider.getRightContext()).offset(0).limit(10).build(),
                        !desktopMode);
                UI.getCurrent().addWindow(c);
                c.center();
            } else {
                Notification.show("Internal error", "No valid citation link was found",
                        Notification.Type.WARNING_MESSAGE);
            }

        }
    }

    private static final long serialVersionUID = -3225593134882374700L;

    @Override
    public void buttonClick(ClickEvent event) {}

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId) {
        Button btLink = new Button();
        btLink.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        btLink.setIcon(FontAwesome.SHARE_ALT);
        btLink.setDescription("Share query reference link");
        btLink.addClickListener(this);

        if (itemId instanceof DisplayedResultQuery) {
            btLink.addClickListener(new LinkClickListener((DisplayedResultQuery) itemId));
        } else if (itemId instanceof Query) {
            final CitationProvider citationProvider = new CitationProviderForQuery((Query) itemId);
            btLink.addClickListener(new LinkClickListener(citationProvider));
        } else if (itemId instanceof CitationProvider) {
            final CitationProvider citationProvider = (CitationProvider) itemId;
            btLink.addClickListener(new LinkClickListener(citationProvider));
        }

        return btLink;
    }
}
