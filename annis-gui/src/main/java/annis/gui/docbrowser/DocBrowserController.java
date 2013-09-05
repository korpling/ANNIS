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
import com.vaadin.server.ClientConnector;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.compiler.CodeGenerator;

/**
 * Represents a global controller for the doc browser feature.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class DocBrowserController implements Serializable
{

  private Logger log = LoggerFactory.getLogger(DocBrowserController.class);

  // holds the complete state of the gui
  private transient final SearchUI ui;

  // track the already initiated doc browsers
  private transient final Map<String, Component> initedDocBrowsers;

  // cache for already initiated visualizations, the key is the doc name
  private transient Map<String, Component> initiatedVis;

  private static final ThemeResource EYE_ICON = new ThemeResource("eye.png");

  private static final ThemeResource DOC_ICON = new ThemeResource(
    "document_ico.png");

  public DocBrowserController(SearchUI ui)
  {
    this.ui = ui;
    this.initedDocBrowsers = new HashMap<String, Component>();
    this.initiatedVis = new HashMap<String, Component>();
  }

  public void openDocVis(String corpus, String doc, String visType)
  {
    String canonicalTitle = "doc view: " + corpus + " > " + doc;

    // check if a visualization is already initiated
    if (!initiatedVis.containsKey(canonicalTitle))
    {

      VisualizerPlugin visualizer = ((PluginSystem) ui).getVisualizer(visType);
      Component vis = visualizer.createComponent(createInput(corpus, doc), null);
      initiatedVis.put(canonicalTitle, vis);
    }

    Component vis = initiatedVis.get(canonicalTitle);
    TabSheet.Tab visTab = ui.getTabSheet().addTab(vis, doc);
    visTab.setIcon(EYE_ICON);
    visTab.setClosable(true);
    ui.getTabSheet().setSelectedTab(vis);
  }

  public void openDocBrowser(String corpus)
  {
    // if not already init, do it now
    if (!initedDocBrowsers.containsKey(corpus))
    {
      DocBrowserPanel browseTbl = DocBrowserPanel.
        initDocBrowserPanel(ui, corpus);
      initedDocBrowsers.put(corpus, browseTbl);
    }

    // init tab and put to front
    TabSheet.Tab tab = ui.getTabSheet().addTab(initedDocBrowsers.get(corpus),
      corpus);
    tab.setIcon(DOC_ICON);
    tab.setClosable(true);
    ui.getTabSheet().setSelectedTab(tab);
  }

  private VisualizerInput createInput(String corpus, String docName)
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

    // set empty mapping for avoiding errors with pure written visualizers
    input.setMappings(new Properties());
    return input;
  }
}
