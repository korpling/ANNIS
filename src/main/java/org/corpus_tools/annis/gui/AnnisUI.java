/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * Licsense is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.corpus_tools.annis.gui;

import static org.corpus_tools.annis.gui.Helper.DEFAULT_CONFIG;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ErrorHandler;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Component;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.ServletContext;

import org.corpus_tools.annis.ApiClient;
import org.corpus_tools.annis.api.model.CorpusConfiguration;
import org.corpus_tools.annis.gui.admin.AdminView;
import org.corpus_tools.annis.gui.components.ExceptionDialog;
import org.corpus_tools.annis.gui.exporter.ExporterPlugin;
import org.corpus_tools.annis.gui.objects.QueryUIState;
import org.corpus_tools.annis.gui.query_references.UrlShortener;
import org.corpus_tools.annis.gui.querybuilder.QueryBuilderPlugin;
import org.corpus_tools.annis.gui.requesthandler.BinaryRequestHandler;
import org.corpus_tools.annis.gui.security.SecurityConfiguration;
import org.corpus_tools.annis.gui.visualizers.VisualizerPlugin;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * GUI for searching in corpora.
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@Theme("annis")
@Widgetset("org.corpus_tools.annis.gui.widgets.gwt.AnnisWidgetSet")
@SpringUI(path = "/*")
@Push(value = PushMode.AUTOMATIC)
public class AnnisUI extends CommonUI implements ErrorHandler, ViewChangeListener {

  private static final Profiles DESKTOP_PROFILES = Profiles.of("desktop & !test");
  private static final long serialVersionUID = 3022711576267350005L;
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(AnnisUI.class);

  private transient Cache<String, CorpusConfiguration> corpusConfigCache;

  private final QueryUIState queryState = new QueryUIState();

  private QueryController queryController;

  private SearchView searchView;

  @Autowired
  private List<VisualizerPlugin> visualizerPlugins;

  @Autowired
  private List<QueryBuilderPlugin<com.vaadin.ui.Component>> queryBuilderPlugins;

  @Autowired
  private List<ExporterPlugin> exporterPlugins;

  @Autowired
  private UrlShortener urlShortener;

  @Autowired
  private UIConfig config;

  @Autowired
  private WebClient webClient;

  private AdminView adminView;

  private Navigator nav;

  @Autowired
  private Environment environment;


  @Autowired(required = false)
  private OAuth2ClientProperties oauth2Clients;

  @Autowired
  private transient ServletContext servletContext;

  /**
   * A re-usable toolbar for different views.
   */
  private MainToolbar toolbar;

  public AnnisUI(ServiceStarter serviceStarter) {
    super("", serviceStarter);
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
    while (source.getCause() != null) {
      source = source.getCause();
    }
    ExceptionDialog.show(source, this);
  }

  /**
   * Get a cached version of the {@link CorpusConfiguration} for a corpus.
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

    getSession().addRequestHandler(new BinaryRequestHandler(getUrlPrefix(), this));

    String id = request.getParameter("id");
    if (id != null) {
      // Check if this is a valid URL shortener ID
      Optional<URI> uri = urlShortener.unshorten(UUID.fromString(id));
      if (uri.isPresent()) {
        Page.getCurrent().setLocation(uri.get());
        return;
      }
    }

    setErrorHandler(this);

    adminView = new AdminView(AnnisUI.this);

    toolbar = new MainToolbar(getConfig(), oauth2Clients);
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

    if (Helper.getUser().isPresent()) {
      getToolbar().onLogin();
    }
    
    Object fragmentToRestore =
        VaadinSession.getCurrent().getAttribute(SecurityConfiguration.FRAGMENT_TO_RESTORE);
    if (fragmentToRestore instanceof String) {
      request.getWrappedSession().setAttribute(SecurityConfiguration.FRAGMENT_TO_RESTORE, null);
      Page.getCurrent().setUriFragment((String) fragmentToRestore);
    }

  }

  @Override
  public ApiClient getClient() {
    ApiClient result = new ApiClient(webClient);
    result.setBasePath(getConfig().getWebserviceUrl());
    return result;
  }

  public WebClient getWebClient() {
    return webClient;
  }


  public UIConfig getConfig() {
    return config;
  }

  public boolean isDesktopMode() {
    return environment.acceptsProfiles(DESKTOP_PROFILES);
  }

  private void initTransients() {
    corpusConfigCache = CacheBuilder.newBuilder().maximumSize(250).build();
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    initTransients();
  }

  public void reportBug(Throwable cause) {
    toolbar.reportBug(cause);
  }

  public MainToolbar getToolbar() {
    return toolbar;
  }

  public List<VisualizerPlugin> getVisualizerPlugins() {
    return visualizerPlugins;
  }

  public List<QueryBuilderPlugin<Component>> getQueryBuilderPlugins() {
    return queryBuilderPlugins;
  }

  public List<ExporterPlugin> getExporterPlugins() {
    return exporterPlugins;
  }

  public UrlShortener getUrlShortener() {
    return urlShortener;
  }

  public void setUrlShortener(UrlShortener urlShortener) {
    this.urlShortener = urlShortener;
  }

  @Override
  public ServletContext getServletContext() {
    return servletContext;
  }

  @Override
  public OAuth2ClientProperties getOauth2ClientProperties() {
    return this.oauth2Clients;
  }
}
