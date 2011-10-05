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
package annis.pluginsystem;

import annis.frontend.servlets.ResourceServlet;
import annis.frontend.servlets.VisualizerServlet;
import annis.frontend.servlets.visualizers.CorefVisualizer;
import annis.frontend.servlets.visualizers.ExternalFileVisualizer;
import annis.frontend.servlets.visualizers.OldPartiturVisualizer;
import annis.frontend.servlets.visualizers.PaulaTextVisualizer;
import annis.frontend.servlets.visualizers.PaulaVisualizer;
import annis.frontend.servlets.visualizers.dependency.ProielDependecyTree;
import annis.frontend.servlets.visualizers.dependency.ProielRegularDependencyTree;
import annis.frontend.servlets.visualizers.dependency.VakyarthaDependencyTree;
import annis.frontend.servlets.visualizers.graph.DotGraphVisualizer;
import annis.frontend.servlets.visualizers.gridtree.GridTreeVisualizer;
import annis.frontend.servlets.visualizers.media.audio.AudioVisualizer;
import annis.frontend.servlets.visualizers.partitur.PartiturVisualizer;
import annis.frontend.servlets.visualizers.tree.TigerTreeVisualizer;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.PluginManagerUtil;
import net.xeoh.plugins.base.util.uri.ClassURI;

/**
 * Loads plugins from several places on webapp startup.
 * 
 * @author Thomas Krause <krause@informatik.hu-berlin.de>
 */
public class StartStopListener implements ServletContextListener
{
  
  private PluginManager pluginManager;

  @Override
  public void contextInitialized(ServletContextEvent sce)
  {
    Logger log = Logger.getLogger(StartStopListener.class.getName());
    
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
    pluginManager.addPluginsFrom(new ClassURI(VisualizerServlet.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(AudioVisualizer.class).toURI());
    
    // TODO: classpath is very large and it takes too much time
    /*
    URI classpath = ClassURI.CLASSPATH("annis.**");
    if(classpath != null)
    {
      pluginManager.addPluginsFrom(classpath);
      log.info("added plugins from classpath");
    }
     */
    try
    {
      URL basicPlugins = sce.getServletContext().getResource("plugins");
      if(basicPlugins != null)
      {
        pluginManager.addPluginsFrom(basicPlugins.toURI());
        log.log(Level.INFO, "added plugins from {0}", basicPlugins.getPath());
      }
    }
    catch(MalformedURLException ex)
    {
      Logger.getLogger(StartStopListener.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch(URISyntaxException ex)
    {
      Logger.getLogger(StartStopListener.class.getName()).log(Level.SEVERE, null, ex);
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
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce)
  {
    if(pluginManager != null)
    {
      pluginManager.shutdown();
    }
  }
}
