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
 * distributed under the Licsense is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.gui;

import static annis.libgui.Helper.DEFAULT_CONFIG;

import annis.gui.components.ExceptionDialog;
import annis.gui.objects.QueryUIState;
import annis.gui.querybuilder.QueryBuilderPlugin;
import annis.libgui.AnnisBaseUI;
import annis.libgui.Helper;
import annis.libgui.exporter.ExporterPlugin;
import annis.libgui.visualizers.VisualizerPlugin;
import annis.service.objects.CorpusConfig;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ErrorHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Component;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.corpus_tools.annis.api.model.CorpusConfiguration;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * GUI for searching in corpora.
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@Theme("annis")
@Widgetset("annis.gui.widgets.gwt.AnnisWidgetSet")
@SpringUI
@Push(value = PushMode.AUTOMATIC, transport = Transport.WEBSOCKET_XHR)
public class AnnisUI extends CommonUI implements ErrorHandler, ViewChangeListener {

    private static final long serialVersionUID = 3022711576267350005L;
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(AnnisUI.class);

    private transient Cache<String, CorpusConfiguration> corpusConfigCache;

    private final QueryUIState queryState = new QueryUIState();

    private QueryController queryController;

    private SearchView searchView;

    @Autowired
    private List<VisualizerPlugin<com.vaadin.ui.Component>> visualizerPlugins;


    @Autowired
    private List<QueryBuilderPlugin<com.vaadin.ui.Component>> queryBuilderPlugins;

    private List<ExporterPlugin> exporterPlugins = new LinkedList<>();

    private AdminView adminView;

    private Navigator nav;

    /**
     * A re-usable toolbar for different views.
     */
    private MainToolbar toolbar;

    public AnnisUI() {
        super("");
        initTransients();
    }

    @Override
    public void afterViewChange(ViewChangeEvent event) {

    }

    @Override
    public boolean beforeViewChange(ViewChangeEvent event) {
        // make sure the toolbar is removed from the old view
        searchView.setToolbar(null);
        adminView.setToolbar(null);
        toolbar.setSidebar(null);

        if (event.getNewView() == searchView) {
            searchView.setToolbar(toolbar);
            toolbar.setSidebar(searchView);
            toolbar.setNavigationTarget(MainToolbar.NavigationTarget.ADMIN, AnnisUI.this);
        } else if (event.getNewView() == adminView) {
            adminView.setToolbar(toolbar);
            toolbar.setNavigationTarget(MainToolbar.NavigationTarget.SEARCH, AnnisUI.this);
        } else {
            toolbar.setNavigationTarget(null, AnnisUI.this);
        }

        return true;
    }

    public boolean canReportBugs() {
        if (toolbar != null) {
            return toolbar.canReportBugs();
        }
        return false;
    }

    public void clearCorpusConfigCache() {
        if (corpusConfigCache != null) {
            corpusConfigCache.invalidateAll();
        }
    }

    @Override
    public void error(com.vaadin.server.ErrorEvent event) {
        log.error("Unknown error in some component: " + event.getThrowable().getLocalizedMessage(),
                event.getThrowable());
        // get the source throwable (thus the one that triggered the error)
        Throwable source = event.getThrowable();
        if (!AnnisBaseUI.handleCommonError(source, null) && source != null) {
            while (source.getCause() != null) {
                source = source.getCause();
            }
            ExceptionDialog.show(source, this);
        }
    }

    /**
     * Get a cached version of the {@link CorpusConfig} for a corpus.
     *
     * @param corpus
     * @return
     */
    public CorpusConfiguration getCorpusConfigWithCache(String corpus) {
      CorpusConfiguration config = new CorpusConfiguration();
        if (corpusConfigCache != null) {
            config = corpusConfigCache.getIfPresent(corpus);
            if (config == null) {
                if (corpus.equals(DEFAULT_CONFIG)) {
                  config = Helper.getDefaultCorpusConfig();
                } else {
                    config = Helper.getCorpusConfig(corpus, AnnisUI.this);
                }

                corpusConfigCache.put(corpus, config);
            }
        }

        return config;
    }

    public QueryController getQueryController() {
        return queryController;
    }

    public QueryUIState getQueryState() {
        return queryState;
    }

    public SearchView getSearchView() {
        return searchView;
    }

    @Override
    protected void init(VaadinRequest request) {
        super.init(request);
        setErrorHandler(this);

        adminView = new AdminView(AnnisUI.this);

        toolbar = new MainToolbar();
        toolbar.setQueryController(queryController);


        this.searchView = new SearchView(this);
        this.queryController = new QueryController(this, searchView, queryState);

        toolbar.addLoginListener(searchView);
        toolbar.addLoginListener(adminView);

        nav = new Navigator(AnnisUI.this, AnnisUI.this);
        nav.addView(SearchView.NAME, searchView);
        nav.addView(AdminView.NAME, adminView);
        nav.addViewChangeListener(AnnisUI.this);

        addExtension(toolbar.getScreenshotExtension());

        loadInstanceFonts();

    }

    private void initTransients() {
        corpusConfigCache = CacheBuilder.newBuilder().maximumSize(250).build();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        initTransients();
    }

    public void reportBug() {
        toolbar.reportBug();
    }

    public void reportBug(Throwable cause) {
        toolbar.reportBug(cause);
    }

    public MainToolbar getToolbar() {
        return toolbar;
    }

    public List<VisualizerPlugin<com.vaadin.ui.Component>> getVisualizerPlugins() {
      return visualizerPlugins;
    }

    public List<QueryBuilderPlugin<Component>> getQueryBuilderPlugins() {
      return queryBuilderPlugins;
    }

    public List<ExporterPlugin> getExporterPlugins() {
      return exporterPlugins;
    }

}
