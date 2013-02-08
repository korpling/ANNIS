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

import annis.gui.media.MediaController;
import annis.gui.media.MediaControllerHolder;
import annis.gui.media.impl.MediaControllerFactoryImpl;
import annis.gui.querybuilder.TigerQueryBuilderPlugin;
import annis.gui.servlets.ResourceServlet;
import annis.gui.visualizers.VisualizerPlugin;
import annis.gui.visualizers.iframe.CorefVisualizer;
import annis.gui.visualizers.iframe.dependency.ProielDependecyTree;
import annis.gui.visualizers.iframe.dependency.ProielRegularDependencyTree;
import annis.gui.visualizers.iframe.dependency.VakyarthaDependencyTree;
import annis.gui.visualizers.iframe.graph.DebugVisualizer;
import annis.gui.visualizers.iframe.graph.DotGraphVisualizer;
import annis.gui.visualizers.iframe.gridtree.GridTreeVisualizer;
import annis.gui.visualizers.component.AudioVisualizer;
import annis.gui.visualizers.component.KWICPanel;
import annis.gui.visualizers.component.VideoVisualizer;
import annis.gui.visualizers.component.grid.GridVisualizer;
import annis.gui.visualizers.component.rst.RST;
import annis.gui.visualizers.component.rst.RSTFull;
import annis.gui.visualizers.iframe.partitur.PartiturVisualizer;
import annis.gui.visualizers.iframe.tree.TigerTreeVisualizer;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.vaadin.annotations.Theme;
import com.vaadin.server.ClassResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.UI;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.PluginManagerUtil;
import net.xeoh.plugins.base.util.uri.ClassURI;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Basic UI functionality.
 * 
 * This class allows to out source some common tasks like initialization of 
 * the logging framework or the plugin loading to this base class.
 */
@Theme("annis-theme")
public class AnnisBaseUI extends UI implements PluginSystem, Serializable,
  MediaControllerHolder
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(
    AnnisBaseUI.class);

  public final static String USER_KEY = "annis.gui.AnnisBaseUI:USER_KEY";
  public final static String WEBSERVICEURL_KEY = "annis.gui.AnnisBaseUI:WEBSERVICEURL_KEY";

  public final static String CITATION_KEY = "annis.gui.AnnisBaseUI:CITATION_KEY";

  private transient PluginManager pluginManager;
  
  private static final Map<String, VisualizerPlugin> visualizerRegistry =
    Collections.synchronizedMap(new HashMap<String, VisualizerPlugin>());

  private static final Map<String, Date> resourceAddedDate =
    Collections.synchronizedMap(new HashMap<String, Date>());

  private Properties versionProperties;

  private transient MediaController mediaController;

  private transient ObjectMapper jsonMapper;

  @Override
  protected void init(VaadinRequest request)
  {
    // load some additional properties from our ANNIS configuration
    loadApplicationProperties("annis-gui.properties");
    
    // store the webservice URL property explicitly in the session in order to 
    // access it from the "external" servlets
    getSession().getSession().setAttribute(WEBSERVICEURL_KEY, 
      getSession().getAttribute(Helper.KEY_WEB_SERVICE_URL));
    
    
    initLogging();

    // get version of ANNIS
    ClassResource res = new ClassResource(AnnisBaseUI.class, "version.properties");
    versionProperties = new Properties();
    try
    {
      versionProperties.load(res.getStream().getStream());
      getSession().setAttribute("annis-version", getVersion());
    }
    catch (Exception ex)
    {
      log.error(null, ex);
    }
    
    initPlugins();
  }

  /**
   * Given an configuration file name (might include directory) this function
   * returns all locations for this file in the "ANNIS configuration system".
   *
   * The files in the result list do not necessarily exist.
   *
   * These locations are the
   * - base installation: WEB-INF/conf/ folder of the deployment.
   * - global configuration: $ANNIS_CFG environment variable value or /etc/annis/ if not set
   * - user configuration: ~/.annis/
   * @param configFile The file path of the configuration file relative to the base config folder.
   * @return list of files or directories in the order in which they should be processed (most important is last)
   */
  protected List<File> getAllConfigLocations(String configFile)
  {
    LinkedList<File> locations = new LinkedList<File>();

    // first load everything from the base application
    locations.add(new File(VaadinService.getCurrent().getBaseDirectory(), 
      "/WEB-INF/conf/" + configFile));

    // next everything from the global config
    // When ANNIS_CFG environment variable is set use this value or default to
    // "/etc/annis/
    String globalConfigDir = System.getenv("ANNIS_CFG");
    if (globalConfigDir == null)
    {
      globalConfigDir = "/etc/annis";
    }
    locations.add(new File(globalConfigDir + "/" + configFile));

    // the final and most specific user configuration is in the users home directory
    locations.add(new File(
      System.getProperty("user.home") + "/.annis/" + configFile));

    return locations;
  }

  protected void loadApplicationProperties(String configFile)
  {

    List<File> locations = getAllConfigLocations(configFile);

    // load properties in the right order
    for(File f : locations)
    {
      loadPropertyFile(f);
    }
  }

  protected Map<String, InstanceConfig> loadInstanceConfig()
  {
    TreeMap<String, InstanceConfig> result = new TreeMap<String, InstanceConfig>();


    // get a list of all directories that contain instance informations
    List<File> locations = getAllConfigLocations("instances");
    for(File root : locations)
    {
      if(root.isDirectory())
      {
        // get all sub-files ending on ".json"
        File[] instanceFiles =
          root.listFiles((FilenameFilter) new SuffixFileFilter(".json"));
        for(File i : instanceFiles)
        {
          if(i.isFile() && i.canRead())
          {
            try
            {
              InstanceConfig config = getJsonMapper().readValue(i, InstanceConfig.class);
              String name = StringUtils.removeEnd(i.getName(), ".json");
              config.setInstanceName(name);
              result.put(name, config);
            }
            catch (IOException ex)
            {
              log.warn("could not parsing instance config: " + ex.getMessage());
            }
          }
        }
      }
    }

    // always provide a default instance
    if(!result.containsKey("default"))
    {
      InstanceConfig cfgDefault = new InstanceConfig();
      cfgDefault.setInstanceDisplayName("ANNIS");
      result.put("default", cfgDefault);
    }

    return result;
  }

  private void loadPropertyFile(File f)
  {
   if(f.canRead() && f.isFile())
    {
      FileInputStream fis = null;
      try
      {
        fis = new FileInputStream(f);
        Properties p = new Properties();
        p.load(fis);
        
        // copy all properties to the session
        for(String name : p.stringPropertyNames())
        {
          getSession().setAttribute(name, p.getProperty(name));
        }
        
      }
      catch(IOException ex)
      {

      }
      finally
      {
        if(fis != null)
        {
          try
          {
            fis.close();
          }
          catch(IOException ex)
          {
            log.error("could not close stream", ex);
          }
        }
      }
    }
  }

  protected void initLogging()
  {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();

    try
    {
      ClassResource res = new ClassResource(AnnisBaseUI.class, "logback.xml");

      if (res != null)
      {
        LoggerContext context = (LoggerContext) LoggerFactory.
          getILoggerFactory();
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(context);
        context.reset();
        
        context.putProperty("webappHome",
          VaadinService.getCurrent().getBaseDirectory().getAbsolutePath());

        // load config file
        jc.doConfigure(res.getStream().getStream());
      }
    }
    catch (JoranException ex)
    {
      log.error("init logging failed", ex);
    }

  }

  public String getBuildRevision()
  {
    String result = versionProperties.getProperty("build_revision", "");
    return result;
  }

  public String getVersion()
  {
    String rev = getBuildRevision();
    Date date = getBuildDate();
    StringBuilder result = new StringBuilder();

    result.append(getVersionNumber());
    if (!"".equals(rev) || date != null)
    {
      result.append(" (");

      boolean added = false;
      if (!"".equals(rev))
      {
        result.append("rev. ");
        result.append(rev);
        added = true;
      }
      if (date != null)
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
    catch (Exception ex)
    {
      log.debug(null, ex);
    }
    return result;
  }

  private void initPlugins()
  {

    log.info("Adding plugins");
    pluginManager = PluginManagerFactory.createPluginManager();

    // TODO: package core plugins as extra project/jar and load them as jar
    // add our core plugins by hand
    pluginManager.addPluginsFrom(new ClassURI(CorefVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(DotGraphVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(DebugVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(GridTreeVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(GridVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(PartiturVisualizer.class).toURI());
    pluginManager.
      addPluginsFrom(new ClassURI(ProielDependecyTree.class).toURI());
    pluginManager.
      addPluginsFrom(new ClassURI(ProielRegularDependencyTree.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(ResourceServlet.class).toURI());
    pluginManager.
      addPluginsFrom(new ClassURI(TigerTreeVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(VakyarthaDependencyTree.class).
      toURI());
    pluginManager.addPluginsFrom(new ClassURI(AudioVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(VideoVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(KWICPanel.class).toURI());

    pluginManager.addPluginsFrom(new ClassURI(TigerQueryBuilderPlugin.class).toURI());

    pluginManager.addPluginsFrom(new ClassURI(MediaControllerFactoryImpl.class).toURI());

    pluginManager.addPluginsFrom(new ClassURI(RST.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(RSTFull.class).toURI());

    File baseDir = VaadinService.getCurrent().getBaseDirectory();
    File basicPlugins = new File(baseDir, "plugins");
    if (basicPlugins.isDirectory())
    {
      pluginManager.addPluginsFrom(basicPlugins.toURI());
      log.info("added plugins from {}", basicPlugins.getPath());
    }


    String globalPlugins = System.getenv("ANNIS_PLUGINS");
    if (globalPlugins != null)
    {
      pluginManager.addPluginsFrom(new File(globalPlugins).toURI());
      log.info("added plugins from {}", globalPlugins);
    }

    StringBuilder listOfPlugins = new StringBuilder();
    listOfPlugins.append("loaded plugins:\n");
    PluginManagerUtil util = new PluginManagerUtil(pluginManager);
    for (Plugin p : util.getPlugins())
    {
      listOfPlugins.append(p.getClass().getName()).append("\n");
    }
    log.info(listOfPlugins.toString());

    Collection<VisualizerPlugin> visualizers = util.getPlugins(
      VisualizerPlugin.class);
    for (VisualizerPlugin vis : visualizers)
    {
      visualizerRegistry.put(vis.getShortName(), vis);
      resourceAddedDate.put(vis.getShortName(), new Date());
    }
  }

  @Override
  public void close()
  {
    if (pluginManager != null)
    {
      pluginManager.shutdown();
    }

    super.close();
  }


  @Override
  public PluginManager getPluginManager()
  {
    if (pluginManager == null)
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


  // TODO: move this to SearchGUI (vaadin7)
  
//  @Override
//  public void onRequestStart(HttpServletRequest request,
//    HttpServletResponse response)
//  {
//    String origURI = request.getRequestURI();
//    String parameters = origURI.replaceAll(".*?/Cite(/)?", "");
//    if (!"".equals(parameters) && !origURI.equals(parameters))
//    {
//      try
//      {
//        String decoded = URLDecoder.decode(parameters, "UTF-8");
//        for(SearchWindow w : searchWindows)
//        {
//          w.evaluateCitation(decoded);
//        }
//        try
//        {
//          response.sendRedirect(getURL().toString());
//        }
//        catch (IOException ex)
//        {
//          log.error(null, ex);
//        }
//      }
//      catch (UnsupportedEncodingException ex)
//      {
//        log.error(null, ex);
//      }
//
//
//    }
//  }


  @Override
  public MediaController getMediaController()
  {
    return mediaController;
  }

  @Override
  public void setMediaController(MediaController mediaController)
  {
    this.mediaController = mediaController;
  }

  public ObjectMapper getJsonMapper()
  {
    if(jsonMapper == null)
    {
      jsonMapper = new ObjectMapper();
      // configure json object mapper
      AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
      jsonMapper.setAnnotationIntrospector(introspector);
      // the json should be human readable
      jsonMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT,
        true);
    }
    return jsonMapper;
  }
}