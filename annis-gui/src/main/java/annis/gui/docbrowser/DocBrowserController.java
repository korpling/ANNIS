/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.gui.docbrowser;

import annis.gui.SearchUI;
import annis.libgui.Helper;
import annis.libgui.PluginSystem;
import annis.libgui.visualizers.VisualizerInput;
import annis.libgui.visualizers.VisualizerPlugin;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a global controller for the doc browser feature.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class DocBrowserController implements Serializable
{

  private Logger log = LoggerFactory.getLogger(DocBrowserController.class);

  // holds the complete state of the gui
  private final SearchUI ui;

  // track the already initiated doc browsers
  private final Map<String, Component> initedDocBrowsers;

  // cache for already initiated visualizations, the key is the doc name
  private final Map<String, Component> initiatedVis;

  private static final ThemeResource EYE_ICON = new ThemeResource("eye.png");

  private static final ThemeResource DOC_ICON = new ThemeResource(
    "document_ico.png");

  public DocBrowserController(SearchUI ui)
  {
    this.ui = ui;
    this.initedDocBrowsers = new HashMap<String, Component>();
    this.initiatedVis = new HashMap<String, Component>();
  }

  public void openDocVis(final String corpus, final String doc,
    final JSONObject config)
  {
    try
    {
      final String type = config.getString("type");
      final String canonicalTitle = corpus + " > " + doc + " - " + "Visualizer: " + type;
      final String tabCaption = StringUtils.substring(canonicalTitle, 0, 15) + "...";

      ui.access(new Runnable()
      {
        @Override
        public void run()
        {
          // check if a visualization is already initiated
          if (!initiatedVis.containsKey(canonicalTitle))
          {

            VisualizerPlugin visualizer = ((PluginSystem) ui).
              getVisualizer(type);
            VisualizerInput input = createInput(corpus, doc, config);
            Component vis = visualizer.createComponent(input, null);
            initiatedVis.put(canonicalTitle, vis);
            vis.setCaption(canonicalTitle);
            vis.setPrimaryStyleName("docviewer");
          }

          Component vis = initiatedVis.get(canonicalTitle);
          Panel visHolder = new Panel();
          visHolder.setContent(vis);
          visHolder.setSizeFull();
          vis.setSizeUndefined();
          TabSheet.Tab visTab = ui.getTabSheet().addTab(visHolder, tabCaption);
          visTab.setIcon(EYE_ICON);
          visTab.setClosable(true);
          ui.getTabSheet().setSelectedTab(vis);


          ui.push();
        }
      });
    }
    catch (JSONException ex)
    {
      log.error("problems with reading document visualizer config", ex);
    }
  }

  public void openDocBrowser(String corpus)
  {
    // if not already init, do it now
    if (!initedDocBrowsers.containsKey(corpus))
    {
      DocBrowserPanel docBrowser = DocBrowserPanel.initDocBrowserPanel(
        ui, corpus);
      initedDocBrowsers.put(corpus, docBrowser);
    }

    // init tab and put to front
    TabSheet.Tab tab = ui.getTabSheet().addTab(initedDocBrowsers.get(corpus),
      corpus);
    tab.setIcon(DOC_ICON);
    tab.setClosable(true);
    ui.getTabSheet().setSelectedTab(tab);
  }

  private VisualizerInput createInput(String corpus, String docName,
    JSONObject config)
  {
    VisualizerInput input = new VisualizerInput();

    // get the whole document wrapped in a salt project
    SaltProject txt = null;
    try
    {
      String topLevelCorpusName = URLEncoder.encode(corpus, "UTF-8");
      docName = URLEncoder.encode(docName, "UTF-8");
      WebResource annisResource = Helper.getAnnisWebResource();
      txt = annisResource.path("query").path("graphs").path(topLevelCorpusName).
        path(docName).get(SaltProject.class);
    }
    catch (RuntimeException e)
    {
      log.error("General remote service exception", e);
    }
    catch (Exception e)
    {
      log.error("General remote service exception", e);
    }

    if (txt != null)
    {
      SDocument sDoc = txt.getSCorpusGraphs().get(0).getSDocuments().get(0);
      input.setResult(sDoc);
    }

    // set mappings and namespaces. some visualizer do not survive without   
    input.setMappings(parseMappings(config));
    input.setNamespace(getNamespace(config));

    return input;
  }

  private Properties parseMappings(JSONObject config)
  {
    Properties mappings = new Properties();
    String mappingsAsString = null;

    try
    {
      mappingsAsString = config.getString("mappings");
    }
    catch (JSONException ex)
    {
      log.debug("no mappings defined", ex);
    }

    if (mappingsAsString != null)
    {
      // split the entrys
      String[] entries = mappingsAsString.split(";");
      for (String e : entries)
      {
        // split key-value
        String[] keyvalue = e.split(":", 2);
        if (keyvalue.length == 2)
        {
          mappings.put(keyvalue[0].trim(), keyvalue[1].trim());
        }
      }
    }

    return mappings;
  }

  private String getNamespace(JSONObject config)
  {
    String namespace = null;
    try
    {
      if (config.has("namespace"))
      {
        namespace = config.getString("namespace");
      }
    }
    catch (JSONException ex)
    {
      log.error("no namespace retrieved for doc visualizer", ex);
    }

    return namespace;
  }
}
