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

import annis.gui.components.ExceptionDialog;
import annis.gui.flatquerybuilder.FlatQueryBuilderPlugin;
import annis.gui.objects.QueryUIState;
import annis.gui.querybuilder.TigerQueryBuilderPlugin;
import annis.gui.servlets.ResourceServlet;
import annis.libgui.Helper;
import static annis.libgui.Helper.CORPUS_FONT;
import static annis.libgui.Helper.CORPUS_FONT_FORCE;
import static annis.libgui.Helper.DEFAULT_CONFIG;
import annis.libgui.InstanceConfig;
import annis.service.objects.CorpusConfig;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ErrorHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.communication.PushMode;
import java.io.IOException;
import java.util.Map;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.util.uri.ClassURI;
import org.slf4j.LoggerFactory;

/**
 * GUI for searching in corpora.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@Theme("annis")
@Push(value = PushMode.AUTOMATIC)
public class AnnisUI extends CommonUI
  implements ErrorHandler, ViewChangeListener
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(
    AnnisUI.class);

  private transient Cache<String, CorpusConfig> corpusConfigCache;

  private final QueryUIState queryState = new QueryUIState();

  private QueryController queryController;

  private SearchView searchView;

  private AdminView adminView;

  private Navigator nav;

  /**
   * A re-usable toolbar for different views.
   */
  private MainToolbar toolbar;

  public AnnisUI()
  {
    super("");
    initTransients();
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    in.defaultReadObject();
    initTransients();
  }

  private void initTransients()
  {
    corpusConfigCache = CacheBuilder.newBuilder().maximumSize(250).build();
  }

  @Override
  protected void init(VaadinRequest request)
  {
    super.init(request);
    setErrorHandler(this);

    searchView = new SearchView(AnnisUI.this);
    adminView = new AdminView(AnnisUI.this);
    queryController = new QueryController(searchView, AnnisUI.this);
    
    toolbar = new MainToolbar();
    toolbar.setQueryController(queryController);
    
    toolbar.addLoginListener(searchView);
    toolbar.addLoginListener(adminView);
    
    nav = new Navigator(AnnisUI.this, AnnisUI.this);
    nav.addView(SearchView.NAME, searchView);
    nav.addView(AdminView.NAME, adminView);
    nav.addViewChangeListener(AnnisUI.this);
    
    addExtension(toolbar.getScreenshotExtension());
    
    loadInstanceFonts();
  }

  @Override
  public boolean beforeViewChange(ViewChangeEvent event)
  {
    // make sure the toolbar is removed from the old view
    searchView.setToolbar(null);
    adminView.setToolbar(null);
    toolbar.setSidebar(null);

    if (event.getNewView() == searchView)
    {
      searchView.setToolbar(toolbar);
      toolbar.setSidebar(searchView);
      toolbar.setNavigationTarget(MainToolbar.NavigationTarget.ADMIN);
    }
    else if (event.getNewView() == adminView)
    {
      adminView.setToolbar(toolbar);
      toolbar.setNavigationTarget(MainToolbar.NavigationTarget.SEARCH);
    }
    else
    {
      toolbar.setNavigationTarget(null);
    }

    return true;
  }

  @Override
  public void afterViewChange(ViewChangeEvent event)
  {

  }

  public boolean canReportBugs()
  {
    if (toolbar != null)
    {
      return toolbar.canReportBugs();
    }
    return false;
  }

  public void reportBug()
  {
    toolbar.reportBug();
  }

  public void reportBug(Throwable cause)
  {
    toolbar.reportBug(cause);
  }

  @Override
  public void error(com.vaadin.server.ErrorEvent event)
  {
    log.error("Unknown error in some component: " + event.getThrowable().
      getLocalizedMessage(),
      event.getThrowable());
    // get the source throwable (thus the one that triggered the error)
    Throwable source = event.getThrowable();
    if (source != null)
    {
      while (source.getCause() != null)
      {
        source = source.getCause();
      }
      ExceptionDialog.show(source);
    }
  }

  


  @Override
  protected void addCustomUIPlugins(PluginManager pluginManager)
  {
    super.addCustomUIPlugins(pluginManager);
    pluginManager.addPluginsFrom(new ClassURI(TigerQueryBuilderPlugin.class).
      toURI());
    pluginManager.addPluginsFrom(new ClassURI(FlatQueryBuilderPlugin.class).
      toURI());
  }

  /**
   * Get a cached version of the {@link CorpusConfig} for a corpus.
   *
   * @param corpus
   * @return
   */
  public CorpusConfig getCorpusConfigWithCache(String corpus)
  {
    CorpusConfig config = new CorpusConfig();
    if (corpusConfigCache != null)
    {
      config = corpusConfigCache.getIfPresent(corpus);
      if (config == null)
      {
        if (corpus.equals(DEFAULT_CONFIG))
        {
          config = Helper.getDefaultCorpusConfig();
        }
        else
        {
          config = Helper.getCorpusConfig(corpus);
        }

        corpusConfigCache.put(corpus, config);
      }
    }

    return config;
  }

  public void clearCorpusConfigCache()
  {
    if (corpusConfigCache != null)
    {
      corpusConfigCache.invalidateAll();
    }
  }


  public QueryController getQueryController()
  {
    return queryController;
  }

  public SearchView getSearchView()
  {
    return searchView;
  }

  public QueryUIState getQueryState()
  {
    return queryState;
  }

}
