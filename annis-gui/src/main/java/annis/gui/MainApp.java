/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.gui;

import annis.gui.tutorial.TutorialPanel;
import annis.gui.controlpanel.ControlPanel;
import annis.gui.resultview.ResultViewPanel;
import annis.gui.servlets.ResourceServlet;
import annis.gui.visualizers.CorefVisualizer;
import annis.gui.visualizers.ExternalFileVisualizer;
import annis.gui.visualizers.OldPartiturVisualizer;
import annis.gui.visualizers.PaulaTextVisualizer;
import annis.gui.visualizers.PaulaVisualizer;
import annis.gui.visualizers.VisualizerPlugin;
import annis.gui.visualizers.dependency.ProielDependecyTree;
import annis.gui.visualizers.dependency.ProielRegularDependencyTree;
import annis.gui.visualizers.dependency.VakyarthaDependencyTree;
import annis.gui.visualizers.graph.DotGraphVisualizer;
import annis.gui.visualizers.gridtree.GridTreeVisualizer;
import annis.gui.visualizers.partitur.PartiturVisualizer;
import annis.gui.visualizers.tree.TigerTreeVisualizer;
import com.vaadin.Application;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.PluginManagerUtil;
import net.xeoh.plugins.base.util.uri.ClassURI;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class MainApp extends Application implements PluginSystem
{

  private Window window;
  private ControlPanel control;
  private TutorialPanel tutorial;
  private ResultViewPanel resultView;
  private TabSheet mainTab;
  private PluginManager pluginManager;
  private static final Map<String, VisualizerPlugin> visualizerRegistry =
    Collections.synchronizedMap(new HashMap<String, VisualizerPlugin>());
  private static final Map<String, Date> resourceAddedDate =
    Collections.synchronizedMap(new HashMap<String, Date>());
  
  @Override
  public void init()
  {
    initPlugins();
    
    setTheme("annis-theme");
    
    window = new Window("Annis² Corpus Search");
    setMainWindow(window);
    
    window.getContent().setSizeFull();
    ((VerticalLayout)window.getContent()).setMargin(false);
    
    MenuBar menu = new MenuBar();
    MenuItem helpMenuItem = menu.addItem("Help", null);
    helpMenuItem.addItem("Tutorial", new MenuBar.Command() {

      @Override
      public void menuSelected(MenuItem selectedItem)
      {
        mainTab.setSelectedTab(tutorial);
      }
    });
    helpMenuItem.addItem("About", new MenuBar.Command() {

      @Override
      public void menuSelected(MenuItem selectedItem)
      {
        window.showNotification("The is a prototype to tests vaadins capabilities in regards to the need of ANNIS", Window.Notification.TYPE_HUMANIZED_MESSAGE);
      }
    });
    helpMenuItem.addItem("Test", new MenuBar.Command() {

      @Override
      public void menuSelected(MenuItem selectedItem)
      {
        Window w = new Window("Test", new TestPanel());
        w.setModal(true);
        window.addWindow(w);
      }
    });
    
    window.addComponent(menu);
    menu.setWidth(100f, Layout.UNITS_PERCENTAGE);
    
    HorizontalLayout hLayout = new HorizontalLayout();
    hLayout.setSizeFull();
    window.addComponent(hLayout);
    ((VerticalLayout) window.getContent()).setExpandRatio(hLayout, 1.0f);

    control = new ControlPanel(this);
    control.setWidth(30f, Layout.UNITS_EM);
    control.setHeight(100f, Layout.UNITS_PERCENTAGE);
    hLayout.addComponent(control);
    
    tutorial = new TutorialPanel();
    
    
    mainTab = new TabSheet();
    mainTab.setSizeFull();
    mainTab.addTab(tutorial, "Tutorial", null);
    
    hLayout.addComponent(mainTab);
    hLayout.setExpandRatio(mainTab, 1.0f);
  }
  

  public void showQueryResult(String aql, Set<Long> corpora, int contextLeft, 
    int contextRight, int pageSize)
  {  
    // remove old result from view
    if(resultView != null)
    {
      mainTab.removeComponent(resultView);
    }
    resultView = new ResultViewPanel(aql, corpora, contextLeft, contextRight,
      pageSize, this);
    mainTab.addTab(resultView, "Query Result", null);
    mainTab.setSelectedTab(resultView);
  }
  
  public void updateQueryCount(int count)
  {
    if(resultView != null && count >= 0)
    {
      resultView.setCount(count);
    }
  }
  
  private void initPlugins()
  {
    Logger log = Logger.getLogger(MainApp.class.getName());
    
    
    log.info("Adding plugins");
    pluginManager = PluginManagerFactory.createPluginManager();
    
    // TODO: package core plugins as extra project/jar and load them as jar
    // add our core plugins by hand
    pluginManager.addPluginsFrom(new ClassURI(CorefVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(DotGraphVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(ExternalFileVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(GridTreeVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(OldPartiturVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(PartiturVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(PaulaTextVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(PaulaVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(ProielDependecyTree.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(ProielRegularDependencyTree.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(ResourceServlet.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(TigerTreeVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(VakyarthaDependencyTree.class).toURI());
    
    // TODO: classpath is very large and it takes too much time
    /*
    URI classpath = ClassURI.CLASSPATH("annis.**");
    if(classpath != null)
    {
      pluginManager.addPluginsFrom(classpath);
      log.info("added plugins from classpath");
    }
     */

    File baseDir = this.getContext().getBaseDirectory();
    File basicPlugins = new File(baseDir, "plugins");
    if(basicPlugins.isDirectory())
    {
      pluginManager.addPluginsFrom(basicPlugins.toURI());
      log.log(Level.INFO, "added plugins from {0}", basicPlugins.getPath());
    }


    String globalPlugins = System.getenv("ANNIS_PLUGINS");
    if(globalPlugins != null)
    {
      pluginManager.addPluginsFrom(new File(globalPlugins).toURI());
      log.log(Level.INFO, "added plugins from {0}", globalPlugins);
    }
    
    StringBuilder listOfPlugins = new StringBuilder();
    listOfPlugins.append("loaded plugins:\n");
    PluginManagerUtil util = new PluginManagerUtil(pluginManager);
    for(Plugin p : util.getPlugins())
    {
      listOfPlugins.append(p.getClass().getName()).append("\n");
    }
    log.info(listOfPlugins.toString());
    
    Collection<VisualizerPlugin> visualizers = util.getPlugins(VisualizerPlugin.class);
    for(VisualizerPlugin vis : visualizers)
    {
      visualizerRegistry.put(vis.getShortName(), vis);
      resourceAddedDate.put(vis.getShortName(), new Date());
    }
  }

  @Override
  public void close()
  {
    if(pluginManager != null)
    {
      pluginManager.shutdown();
    }
    
    super.close();
  }

  @Override
  public PluginManager getPluginManager()
  {
    return pluginManager;
  }

  @Override
  public VisualizerPlugin getVisualizer(String shortName)
  {
    VisualizerPlugin result = visualizerRegistry.get(shortName);
    if(result == null)
    {
      result = visualizerRegistry.get("grid");
    }
    return result;
  }

  
}