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
package org.corpus_tools.annis.gui;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.base.Charsets;
import com.google.common.eventbus.EventBus;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.vaadin.annotations.Theme;
import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.server.ClassResource;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.UI;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringUtils;
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
  public static final Resource PINGUIN_IMAGE =
      new ClassResource("/org.corpus_tools.annis/libgui/penguins.png");


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

  private TreeSet<String> alreadyAddedCSS = new TreeSet<String>();

  private transient EventBus loginDataLostBus;


  @Override
  public void attach() {

    super.attach();
    alreadyAddedCSS.clear();
  }


  public EventBus getLoginDataLostBus() {
    if (loginDataLostBus == null) {
      loginDataLostBus = new EventBus();
    }
    return loginDataLostBus;
  }


  @Override
  protected void init(VaadinRequest request) {
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
    this.access(() -> {
      if (alreadyAddedCSS == null) {
        alreadyAddedCSS = new TreeSet<String>();
      }

      final String wrappedCssContent =
          wrapperClass == null ? cssContent : wrapCSS(cssContent, wrapperClass);


      if (wrappedCssContent != null) {
        String hashForCssContent =
            Hashing.md5().hashString(wrappedCssContent, StandardCharsets.UTF_8).toString();

        if (!alreadyAddedCSS.contains(hashForCssContent)) {

          alreadyAddedCSS.add(hashForCssContent);
          this.getPage().getStyles().add(wrappedCssContent);
        }
      }
    });
  }

  protected Map<String, InstanceConfig> loadInstanceConfig() {
    TreeMap<String, InstanceConfig> result = new TreeMap<>();

    JsonMapper mapper = new JsonMapper();
    mapper.registerModule(new JaxbAnnotationModule());
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

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
                InstanceConfig config = mapper.readValue(i, InstanceConfig.class);
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
