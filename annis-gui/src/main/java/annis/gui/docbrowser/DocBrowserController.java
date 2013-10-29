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
import annis.service.objects.RawTextWrapper;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.server.ClientConnector;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a global controller for the doc browser feature.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class DocBrowserController implements Serializable
{

  private final Logger log = LoggerFactory.getLogger(DocBrowserController.class);

  // holds the complete state of the gui
  private final SearchUI ui;

  // track the already initiated doc browsers
  private final Map<String, Component> initedDocBrowsers;

  // cache for already initiated visualizations, the key is the doc name
  private final Map<String, Component> initiatedVis;

  // keep track of already visible doc visualizer, so it easy to switch to them.
  private final Map<String, Panel> visibleVisHolder;

  private static final ThemeResource EYE_ICON = new ThemeResource("eye.png");

  private static final ThemeResource DOC_ICON = new ThemeResource(
    "document_ico.png");

  public DocBrowserController(SearchUI ui)
  {
    this.ui = ui;
    this.initedDocBrowsers = new HashMap<String, Component>();
    this.initiatedVis = new HashMap<String, Component>();
    this.visibleVisHolder = new HashMap<String, Panel>();
  }

  public void openDocVis(String corpus, String doc, JSONSerializable config,
    Button btn)
  {
    try
    {
      final String type = config.getString("type");
      final String canonicalTitle = corpus + " > " + doc + " - " + "Visualizer: " + type;
      final String tabCaption = StringUtils.substring(canonicalTitle, 0, 15) + "...";

      if (visibleVisHolder.containsKey(canonicalTitle))
      {
        Panel visHolder = visibleVisHolder.get(canonicalTitle);
        ui.getTabSheet().setSelectedTab(visHolder);
        return;
      }

      Panel visHolder = new Panel();
      visHolder.setSizeFull();
      visHolder.addDetachListener(new ClientConnector.DetachListener()
      {
        @Override
        public void detach(ClientConnector.DetachEvent event)
        {
          visibleVisHolder.remove(canonicalTitle);
        }
      });

      // first set loading indicator
      ProgressBar progressBar = new ProgressBar(1.0f);
      progressBar.setIndeterminate(true);
      progressBar.setSizeFull();
      visHolder.setContent(progressBar);

      Tab visTab = ui.getTabSheet().addTab(visHolder, tabCaption);
      visTab.setDescription(canonicalTitle);
      visTab.setIcon(EYE_ICON);
      visTab.setClosable(true);
      ui.getTabSheet().setSelectedTab(visTab);

      // register visible visHolder
      this.visibleVisHolder.put(canonicalTitle, visHolder);

      new DocVisualizerFetcher(corpus, doc, canonicalTitle, type, visHolder,
        config, btn).
        start();
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

  /**
   * Creates the input. It only takes the salt project or the raw text from the
   * text table, never both, since the increase the performance for large texts.
   *
   * @param corpus the name of the toplevel corpus
   * @param docName the name of the document
   * @param config the visualizer configuration
   * @param isUsingRawText indicates, whether the text from text table is taken,
   * or if the salt project is traversed.
   * @return a {@link VisualizerInput} input, which is usable for rendering the
   * whole document.
   */
  private VisualizerInput createInput(String corpus, String docName,
    JSONSerializable config, boolean isUsingRawText)
  {
    VisualizerInput input = new VisualizerInput();

    try
    {
      if (isUsingRawText)
      {
        WebResource w = Helper.getAnnisWebResource();
        w = w.path("query").path("rawtext").path(corpus).path(docName);
        RawTextWrapper rawTextWrapper = w.get(RawTextWrapper.class);
        input.setRawText(rawTextWrapper);
      }
      else
      {
        // get the whole document wrapped in a salt project
        SaltProject txt = null;

        String topLevelCorpusName = URLEncoder.encode(corpus, "UTF-8");
        docName = URLEncoder.encode(docName, "UTF-8");
        WebResource annisResource = Helper.getAnnisWebResource();
        txt = annisResource.path("query").path("graphs").
          path(topLevelCorpusName).
          path(docName).get(SaltProject.class);

        if (txt != null)
        {
          SDocument sDoc = txt.getSCorpusGraphs().get(0).getSDocuments().get(0);
          input.setResult(sDoc);
        }
      }
    }
    catch (RuntimeException e)
    {
      log.error("General remote service exception", e);
    }
    catch (Exception e)
    {
      log.error("General remote service exception", e);
    }

    // set mappings and namespaces. some visualizer do not survive without   
    input.setMappings(parseMappings(config));
    input.setNamespace(getNamespace(config));

    return input;
  }

  private Properties parseMappings(JSONSerializable config)
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

  private String getNamespace(JSONSerializable config)
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

  private class DocVisualizerFetcher extends Thread
  {

    JSONSerializable config;

    String corpus;

    String doc;

    final Button btn;

    private final String canonicalTitle;

    private final String type;

    private final Panel visHolder;

    public DocVisualizerFetcher(String corpus, String doc, String canonicalTitle,
      String type,
      Panel visHolder,
      JSONSerializable config,
      Button btn)
    {
      this.corpus = corpus;
      this.doc = doc;
      this.btn = btn;
      this.config = config;
      this.canonicalTitle = canonicalTitle;
      this.type = type;
      this.visHolder = visHolder;
    }

    @Override
    public void run()
    {
      // check if a visualization is already initiated
      {
        if (!initiatedVis.containsKey(canonicalTitle))
        {
          VisualizerPlugin visualizer = ((PluginSystem) ui).
            getVisualizer(type);

          // fetch the salt project - so long part
          VisualizerInput input = createInput(corpus, doc, config, visualizer.
            isUsingRawText());

          // create and format visualizer
          Component vis = visualizer.createComponent(input, null);
          vis.addStyleName("corpus-font-force");
          vis.setPrimaryStyleName("docviewer");
          vis.setCaption(canonicalTitle);
          vis.setWidth(100, Unit.PERCENTAGE);
          vis.setHeight(-1, Unit.PIXELS);

          // update visualizer memory cache
          initiatedVis.put(canonicalTitle, vis);
        }
      }

      // after initializing the visualizer update the gui
      ui.access(new Runnable()
      {
        @Override
        public void run()
        {

          Component vis = initiatedVis.get(canonicalTitle);
          visHolder.setContent(vis);

          btn.setEnabled(true);
          ui.push();
        }
      });
    }
  }
}
