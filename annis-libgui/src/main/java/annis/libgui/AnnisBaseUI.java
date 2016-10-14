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
package annis.libgui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.google.common.base.Charsets;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.common.eventbus.EventBus;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.annotations.Theme;
import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.server.ClassResource;
import com.vaadin.server.Page;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

import annis.VersionInfo;
import annis.libgui.exporter.ExporterPlugin;
import annis.libgui.visualizers.VisualizerPlugin;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.PluginManagerUtil;
/**
 * Basic UI functionality.
 * 
 * This class allows to out source some common tasks like initialization of 
 * the logging framework or the plugin loading to this base class.
 */
@Theme("annis")
public class AnnisBaseUI extends UI implements PluginSystem, Serializable
{
  
  static
  {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
  }
  
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(
    AnnisBaseUI.class);

  public final static String USER_KEY = "annis.gui.AnnisBaseUI:USER_KEY";
  public final static String USER_LOGIN_ERROR = "annis.gui.AnnisBaseUI:USER_LOGIN_ERROR";
  public final static String CONTEXT_PATH = "annis.gui.AnnisBaseUI:CONTEXT_PATH";
  public final static String WEBSERVICEURL_KEY = "annis.gui.AnnisBaseUI:WEBSERVICEURL_KEY";

  public final static String CITATION_KEY = "annis.gui.AnnisBaseUI:CITATION_KEY";
  
  public final static Resource PINGUIN_IMAGE = new ClassResource("/annis/libgui/penguins.png");

  private transient PluginManager pluginManager;
  
  private transient ClassToInstanceMap<ExporterPlugin> exporterRegistryCache;
  
  private static final Map<String, VisualizerPlugin> visualizerRegistry =
    Collections.synchronizedMap(new HashMap<String, VisualizerPlugin>());

  private static final Map<String, Date> resourceAddedDate =
    Collections.synchronizedMap(new HashMap<String, Date>());

  private transient ObjectMapper jsonMapper;
  
  private TreeSet<String> alreadyAddedCSS = new TreeSet<String>();
  
  private transient EventBus loginDataLostBus;
  
  @Override
  protected void init(VaadinRequest request)
  {  
    
    initLogging();
    
    // store the webservice URL property explicitly in the session in order to 
    // access it from the "external" servlets
    getSession().getSession().setAttribute(WEBSERVICEURL_KEY,
      getSession().getAttribute(Helper.KEY_WEB_SERVICE_URL));

    getSession().setAttribute(CONTEXT_PATH, request.getContextPath());
    alreadyAddedCSS.clear();
    
    initPlugins();
    
    checkIfRemoteLoggedIn(request);
    getSession().addRequestHandler(new RemoteUserRequestHandler());
    
  }

  @Override
  public void attach()
  {
    
    super.attach();
    alreadyAddedCSS.clear();
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
  public static List<File> getAllConfigLocations(String configFile)
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

  protected Map<String, InstanceConfig> loadInstanceConfig()
  {
    TreeMap<String, InstanceConfig> result = new TreeMap<>();


    // get a list of all directories that contain instance informations
    List<File> locations = getAllConfigLocations("instances");
    for(File root : locations)
    {
      if(root.isDirectory())
      {
        // get all sub-files ending on ".json"
        File[] instanceFiles =
          root.listFiles((FilenameFilter) new SuffixFileFilter(".json"));
        if(instanceFiles != null)
        {
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
                log.warn("could not parse instance config: " + ex.getMessage());
              }
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


  protected final void initLogging()
  {
    try
    {
      
      List<File> logbackFiles = getAllConfigLocations("gui-logback.xml");
      
      InputStream inStream = null;
      if(!logbackFiles.isEmpty())
      {
        try
        {
          inStream = new FileInputStream(logbackFiles.get(logbackFiles.size()-1));
        }
        catch(FileNotFoundException ex)
        {
          // well no logging no error...
        }
      }
      if(inStream == null)
      {
        ClassResource res = new ClassResource(AnnisBaseUI.class, "logback.xml");
        inStream = res.getStream().getStream();
      }
      
      if (inStream != null)
      {
        LoggerContext context = (LoggerContext) LoggerFactory.
          getILoggerFactory();
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(context);
        context.reset();
        
        context.putProperty("webappHome",
          VaadinService.getCurrent().getBaseDirectory().getAbsolutePath());

        // load config file
        jc.doConfigure(inStream);
      }
    }
    catch (JoranException ex)
    {
      log.error("init logging failed", ex);
    }

  }

  
  /**
   * Override this method to append additional plugins to the internal {@link PluginManager}.
   * 
   * The default implementation is empty 
   * (thus you don't need to call {@code super.addCustomUIPlugins(...)}).
   * @param pluginManager 
   */
  protected void addCustomUIPlugins(PluginManager pluginManager)
  {
    // default: do nothing
  }

  private void initPlugins()
  {

    log.info("Adding plugins");
    pluginManager = PluginManagerFactory.createPluginManager();
    addCustomUIPlugins(pluginManager);

    File baseDir = VaadinService.getCurrent().getBaseDirectory();
    
    File builtin = new File(baseDir, "WEB-INF/lib/annis-visualizers-" 
      + VersionInfo.getReleaseName() + ".jar");
    if(builtin.canRead()) { 
      pluginManager.addPluginsFrom(builtin.toURI());
      log.info("added built-in plugins from  {}", builtin.getPath());
    } else {
      log.warn("could not find built-in plugin file {}", builtin.getPath());
    }
    File basicPlugins = new File(baseDir, "WEB-INF/plugins");
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
  
  private static void checkIfRemoteLoggedIn(VaadinRequest request)
  {
     // check if we are logged in using an external authentification mechanism
      // like Schibboleth
      String remoteUser = request.getRemoteUser();
      if(remoteUser != null)
      { 
        Helper.setUser(new AnnisUser(remoteUser, null, true));
      }
  }
  
  /**
   * Handle common errors like database/service connection problems and display a unified
   * error message.
   * 
   * This will not log the exception, only display information to the user.
   * 
   * @param ex
   * @return True if error was handled, false otherwise.
   */
  public static boolean handleCommonError(Throwable ex, String action)
  {
    if(ex != null)
    {
      Throwable rootCause = ex;
      while(rootCause.getCause() != null)
      {
        rootCause = rootCause.getCause();
      }

      if(rootCause instanceof UniformInterfaceException)
      {
        UniformInterfaceException uniEx = (UniformInterfaceException) rootCause;
        
        if(uniEx.getResponse() != null)
        {
          if(uniEx.getResponse().getStatus() == 503)
          {
            // database connection error
            Notification n = new Notification(
                "Can't execute " + (action == null ? "" : "\"" + action + "\"" ) 
                + " action because database server is not responding.<br/>"
                + "There might be too many users using this service right now.", 
                Notification.Type.WARNING_MESSAGE);
            n.setDescription("<p><strong>Please try again later.</strong> If the error persists inform the administrator of this server.</p>"
                + "<p>Click on this message to close it.</p>"
                + "<p style=\"font-size:9pt;color:gray;\">Pinguin picture by Polar Cruises [CC BY 2.0 (http://creativecommons.org/licenses/by/2.0)], via Wikimedia Commons</p>");
            n.setIcon(PINGUIN_IMAGE);
            n.setHtmlContentAllowed(true);
            n.setDelayMsec(15000);
           
            n.show(Page.getCurrent());
            
            return true;
          }
        }
       
      }
    }
    return false;
  }
  
  /**
   * Inject CSS into the UI. 
   * This function will not add multiple style-elements if the
   * exact CSS string was already added.
   * @param cssContent 
   */
  public void injectUniqueCSS(String cssContent)
  {
    injectUniqueCSS(cssContent, null);
  }
  
  /**
   * Inject CSS into the UI. 
   * This function will not add multiple style-elements if the
   * exact CSS string was already added.
   * @param cssContent 
   * @param wrapperClass Name of the wrapper class (a CSS class that is applied to a parent element)
   */
  public void injectUniqueCSS(String cssContent, String wrapperClass)
  {
    if(alreadyAddedCSS == null)
    {
      alreadyAddedCSS = new TreeSet<String>(); 
    }
    
    if(wrapperClass != null)
    {
      cssContent = wrapCSS(cssContent, wrapperClass);
    }
    
    String hashForCssContent = Hashing.md5().hashString(cssContent, Charsets.UTF_8).toString();
    if(!alreadyAddedCSS.contains(hashForCssContent))
    {
//      CSSInject cssInject = new CSSInject(UI.getCurrent());
//      cssInject.setStyles(cssContent);
      Page.getCurrent().getStyles().add(cssContent);
      alreadyAddedCSS.add(hashForCssContent);
    }
  }
  
  private String wrapCSS(String cssContent, String wrapperClass)
  {
    try
    {
      
      String wrappedContent
        = wrapperClass == null ? cssContent
        : "." + wrapperClass + "{\n"
        + cssContent
        + "\n}";
      
      File tmpFile = File.createTempFile("annis-stylesheet", ".scss");
      Files.write(wrappedContent, tmpFile, Charsets.UTF_8);
      ScssStylesheet styleSheet = ScssStylesheet.get(tmpFile.getCanonicalPath());
      styleSheet.compile();
      
      return styleSheet.printState();
      
    }
    catch (IOException ex)
    {
      log.error("IOException when compiling wrapped CSS", ex);
    }
    catch (Exception ex)
    {
      log.error("Could not compile wrapped CSS", ex);
    }
    return null;
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
  
  @Override
  public ExporterPlugin getExporter(Class<? extends ExporterPlugin> clazz)
  {
    if(exporterRegistryCache == null)
    {
      exporterRegistryCache = MutableClassToInstanceMap.create();
      PluginManagerUtil util = new PluginManagerUtil(getPluginManager());
      for (ExporterPlugin e : util.getPlugins(ExporterPlugin.class))
      {
        exporterRegistryCache.put(e.getClass(), e);
      }
    }
    return exporterRegistryCache.get(clazz);
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
      jsonMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES , false);
    }
    return jsonMapper;
  }
  
  private static class RemoteUserRequestHandler implements RequestHandler
  {

    @Override
    public boolean handleRequest(VaadinSession session, VaadinRequest request,
      VaadinResponse response) throws IOException
    {
      checkIfRemoteLoggedIn(request);
      // we never write any information in this handler
      return false;
    }  
  }

  public EventBus getLoginDataLostBus()
  {
    if(loginDataLostBus == null)
    {
      loginDataLostBus = new EventBus();
    }
    return loginDataLostBus;
  }
 
  
}