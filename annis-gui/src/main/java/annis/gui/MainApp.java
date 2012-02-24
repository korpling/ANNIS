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

import annis.gui.servlets.ResourceServlet;
import annis.gui.visualizers.CorefVisualizer;
import annis.gui.visualizers.OldPartiturVisualizer;
import annis.gui.visualizers.PaulaTextVisualizer;
import annis.gui.visualizers.PaulaVisualizer;
import annis.gui.visualizers.VisualizerPlugin;
import annis.gui.visualizers.dependency.ProielDependecyTree;
import annis.gui.visualizers.dependency.ProielRegularDependencyTree;
import annis.gui.visualizers.dependency.VakyarthaDependencyTree;
import annis.gui.visualizers.graph.DotGraphVisualizer;
import annis.gui.visualizers.gridtree.GridTreeVisualizer;
import annis.gui.visualizers.media.audio.AudioVisualizer;
import annis.gui.visualizers.media.video.VideoVisualizer;
import annis.gui.visualizers.partitur.PartiturVisualizer;
import annis.gui.visualizers.tree.TigerTreeVisualizer;
import annis.security.AnnisSecurityManager;
import annis.security.AnnisUser;
import com.vaadin.Application;
import com.vaadin.Application.UserChangeListener;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.PluginManagerUtil;
import net.xeoh.plugins.base.util.uri.ClassURI;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class MainApp extends Application implements PluginSystem,
  UserChangeListener, HttpServletRequestListener, Serializable
{

  public final static String USER_KEY = "annis.gui.MainApp:USER_KEY";
  public final static String CITATION_KEY = "annis.gui.MainApp:CITATION_KEY";
  private transient SearchWindow windowSearch;
  private transient PluginManager pluginManager;
  private static final Map<String, VisualizerPlugin> visualizerRegistry =
    Collections.synchronizedMap(new HashMap<String, VisualizerPlugin>());
  private static final Map<String, Date> resourceAddedDate =
    Collections.synchronizedMap(new HashMap<String, Date>());
  private Properties versionProperties;

  @Override
  public void init()
  {
    ClassResource res = new ClassResource("version.properties", this);
    versionProperties = new Properties();
    try
    {
      versionProperties.load(res.getStream().getStream());
    }
    catch(Exception ex)
    {
      Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
    }

    addListener((UserChangeListener) this);

    initPlugins();

    setTheme("annis-theme");
    
    initWindow(); 
  }

  public void initWindow()
  {
    windowSearch = new SearchWindow((PluginSystem) this);
    setMainWindow(windowSearch);
  }
  
  public SearchWindow getWindowSearch()
  {
    if(windowSearch == null)
    {
      initWindow();
    }
    return windowSearch;
  }

  public int getBuildRevision()
  {
    int result = -1;
    try
    {
      result = Integer.parseInt(versionProperties.getProperty("build_revision", "-1"));
    }
    catch(NumberFormatException ex)
    {
      Logger.getLogger(MainApp.class.getName()).log(Level.FINE, null, ex);
    }
    return result;
  }

  @Override
  public String getVersion()
  {
    int rev = getBuildRevision();
    Date date = getBuildDate();
    StringBuilder result = new StringBuilder();

    result.append(getVersionNumber());
    if(rev >= 0 || date != null)
    {
      result.append(" (");

      boolean added = false;
      if(rev >= 0)
      {
        result.append("rev. ");
        result.append(rev);
        added = true;
      }
      if(date != null)
      {
        result.append(added ? ", built " : "");

        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        result.append(d.format(date));
      }

      result.append(")");
    }

    return result.toString();

  }

  public String getVersionNumber()
  {
    return versionProperties.getProperty("version", "UNKNOWNVERSION");
  }

  public Date getBuildDate()
  {
    Date result = null;
    try
    {
      DateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
      result = format.parse(versionProperties.getProperty("build_date"));
    }
    catch(Exception ex)
    {
      Logger.getLogger(MainApp.class.getName()).log(Level.FINE, null, ex);
    }
    return result;
  }

  @Override
  public void setUser(Object user)
  {
    if(user == null || !(user instanceof AnnisUser))
    {
      try
      {
        user = getWindowSearch().getSecurityManager().login(AnnisSecurityManager.FALLBACK_USER,
          AnnisSecurityManager.FALLBACK_USER, true);
      }
      catch(Exception ex)
      {
        Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    super.setUser(user);

    getWindowSearch().updateUserInformation();
  }

  @Override
  public AnnisUser getUser()
  {
    Object u = super.getUser();
    return (AnnisUser) u;
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
    pluginManager.addPluginsFrom(new ClassURI(AudioVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(VideoVisualizer.class).toURI());

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
    if(pluginManager == null)
    {
      initPlugins();
    }
    return pluginManager;
  }

  @Override
  public VisualizerPlugin getVisualizer(String shortName)
  {
    return visualizerRegistry.get(shortName);
  }

  @Override
  public void applicationUserChanged(UserChangeEvent event)
  {
    HttpSession session = ((WebApplicationContext) getContext()).getHttpSession();
    session.setAttribute(USER_KEY, event.getNewUser());
  }

  public AnnisSecurityManager getSecurityManager()
  {
    return getWindowSearch().getSecurityManager();
  }

  @Override
  public void onRequestStart(HttpServletRequest request, HttpServletResponse response)
  {
    String origURI = request.getRequestURI();
    String parameters = origURI.replaceAll(".*?/Cite(/)?", "");
    if(!"".equals(parameters) && !origURI.equals(parameters))
    {
      try
      {
        String decoded = URLDecoder.decode(parameters, "UTF-8");
        getWindowSearch().evaluateCitation(decoded);
        try
        {
          response.sendRedirect(getURL().toString());
        }
        catch(IOException ex)
        {
          Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      catch(UnsupportedEncodingException ex)
      {
        Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
      }


    }
  }

  @Override
  public void onRequestEnd(HttpServletRequest request, HttpServletResponse response)
  {
  }
}