/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin
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
package annis.libgui;

import com.google.common.base.Charsets;
import com.google.common.eventbus.EventBus;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.vaadin.annotations.Theme;
import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.server.ClassResource;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
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
import org.corpus_tools.annis.ApiException;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Basic UI functionality.
 * 
 * This class allows to out source some common tasks like initialization of the logging framework or
 * the plugin loading to this base class.
 */
@Theme("annis")
public class AnnisBaseUI extends UI implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1953089184783346987L;

  static {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
  }

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(AnnisBaseUI.class);
  public final static String USER_KEY = "annis.gui.AnnisBaseUI:USER_KEY";

  public final static String CONTEXT_PATH = "annis.gui.AnnisBaseUI:CONTEXT_PATH";

  public final static String CITATION_KEY = "annis.gui.AnnisBaseUI:CITATION_KEY";

  public final static Resource PINGUIN_IMAGE = new ClassResource("/annis/libgui/penguins.png");


  /**
   * Given an configuration file name (might include directory) this function returns all locations
   * for this file in the "ANNIS configuration system".
   *
   * The files in the result list do not necessarily exist.
   *
   * These locations are the - base installation: WEB-INF/conf/ folder of the deployment. - global
   * configuration: $ANNIS_CFG environment variable value or /etc/annis/ if not set - user
   * configuration: ~/.annis/
   * 
   * @param configFile The file path of the configuration file relative to the base config folder.
   * @return list of files or directories in the order in which they should be processed (most
   *         important is last)
   */
  public static List<File> getAllConfigLocations(String configFile) {
    LinkedList<File> locations = new LinkedList<File>();

    // first load everything from the base application
    locations.add(
        new File(VaadinService.getCurrent().getBaseDirectory(), "/WEB-INF/conf/" + configFile));

    // next everything from the global config
    // When ANNIS_CFG environment variable is set use this value or default to
    // "/etc/annis/
    String globalConfigDir = System.getenv("ANNIS_CFG");
    if (globalConfigDir == null) {
      globalConfigDir = "/etc/annis";
    }
    locations.add(new File(globalConfigDir + "/" + configFile));

    // the final and most specific user configuration is in the users home directory
    locations.add(new File(System.getProperty("user.home") + "/.annis/" + configFile));

    return locations;
  }

  /**
   * Handle common errors like database/service connection problems and display a unified error
   * message.
   * 
   * This will not log the exception, only display information to the user.
   * 
   * @param ex exception to handle
   * @return True if error was handled, false otherwise.
   */
  public static boolean handleCommonError(Throwable ex, String action) {
    if (ex != null) {
      Throwable rootCause = ex;
      while (rootCause.getCause() != null) {
        rootCause = rootCause.getCause();
      }

      if (rootCause instanceof ApiException) {
        ApiException apiEx = (ApiException) rootCause;

        if (apiEx.getCode() == 503) {
          // database connection error
          Notification n = new Notification(
              "Can't execute " + (action == null ? "" : "\"" + action + "\"")
                  + " action because database server is not responding.<br/>"
                  + "There might be too many users using this service right now.",
              Notification.Type.WARNING_MESSAGE);
          n.setDescription(
              "<p><strong>Please try again later.</strong> If the error persists inform the administrator of this server.</p>"
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
    return false;
  }


  private transient ObjectMapper jsonMapper;

  private TreeSet<String> alreadyAddedCSS = new TreeSet<String>();

  private transient EventBus loginDataLostBus;


  @Override
  public void attach() {

    super.attach();
    alreadyAddedCSS.clear();
  }


  public ObjectMapper getJsonMapper() {
    if (jsonMapper == null) {
      jsonMapper = new ObjectMapper();
      // configure json object mapper
      AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
      jsonMapper.setAnnotationIntrospector(introspector);
      // the json should be human readable
      jsonMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
      jsonMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    return jsonMapper;
  }

  public EventBus getLoginDataLostBus() {
    if (loginDataLostBus == null) {
      loginDataLostBus = new EventBus();
    }
    return loginDataLostBus;
  }


  @Override
  protected void init(VaadinRequest request) {
    getSession().setAttribute(CONTEXT_PATH, request.getContextPath());
    alreadyAddedCSS.clear();
  }

  /**
   * Inject CSS into the UI. This function will not add multiple style-elements if the exact CSS
   * string was already added.
   * 
   * @param cssContent CSS as string
   */
  public void injectUniqueCSS(String cssContent) {
    injectUniqueCSS(cssContent, null);
  }

  /**
   * Inject CSS into the UI. This function will not add multiple style-elements if the exact CSS
   * string was already added.
   * 
   * @param cssContent CSS as string
   * @param wrapperClass Name of the wrapper class (a CSS class that is applied to a parent element)
   */
  public void injectUniqueCSS(String cssContent, String wrapperClass) {
    if (alreadyAddedCSS == null) {
      alreadyAddedCSS = new TreeSet<String>();
    }

    if (wrapperClass != null) {
      cssContent = wrapCSS(cssContent, wrapperClass);
    }

    String hashForCssContent = Hashing.md5().hashString(cssContent, Charsets.UTF_8).toString();
    if (!alreadyAddedCSS.contains(hashForCssContent)) {
      this.getPage().getStyles().add(cssContent);
      alreadyAddedCSS.add(hashForCssContent);
    }
  }

  protected Map<String, InstanceConfig> loadInstanceConfig() {
    TreeMap<String, InstanceConfig> result = new TreeMap<>();


    // get a list of all directories that contain instance informations
    List<File> locations = getAllConfigLocations("instances");
    for (File root : locations) {
      if (root.isDirectory()) {
        // get all sub-files ending on ".json"
        File[] instanceFiles = root.listFiles((FilenameFilter) new SuffixFileFilter(".json"));
        if (instanceFiles != null) {
          for (File i : instanceFiles) {
            if (i.isFile() && i.canRead()) {
              try {
                InstanceConfig config = getJsonMapper().readValue(i, InstanceConfig.class);
                String name = StringUtils.removeEnd(i.getName(), ".json");
                config.setInstanceName(name);
                result.put(name, config);
              } catch (IOException ex) {
                log.warn("could not parse instance config: " + ex.getMessage());
              }
            }
          }
        }
      }
    }

    // always provide a default instance
    if (!result.containsKey("default")) {
      InstanceConfig cfgDefault = new InstanceConfig();
      cfgDefault.setInstanceDisplayName("ANNIS");
      result.put("default", cfgDefault);
    }

    return result;
  }

  private String wrapCSS(String cssContent, String wrapperClass) {
    try {

      String wrappedContent =
          wrapperClass == null ? cssContent : "." + wrapperClass + "{\n" + cssContent + "\n}";

      File tmpFile = File.createTempFile("annis-stylesheet", ".scss");
      Files.write(wrappedContent, tmpFile, Charsets.UTF_8);
      ScssStylesheet styleSheet = ScssStylesheet.get(tmpFile.getCanonicalPath());
      styleSheet.compile();

      return styleSheet.printState();

    } catch (IOException ex) {
      log.error("IOException when compiling wrapped CSS", ex);
    } catch (Exception ex) {
      log.error("Could not compile wrapped CSS", ex);
    }
    return null;
  }


}
