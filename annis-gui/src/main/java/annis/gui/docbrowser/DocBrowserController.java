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
  private transient final Map<String, TabSheet.Tab> initedDocBrowsers;

  // cache for already initiated visualizations, the key is the doc name
  private transient Map<String, Component> initiatedVis;

  public DocBrowserController(SearchUI ui)
  {
    this.ui = ui;
    this.initedDocBrowsers = new HashMap<String, TabSheet.Tab>();
    this.initiatedVis = new HashMap<String, Component>();
  }

  public void openDocVis(String corpus, String doc)
  {
    String canonicalTitle = "doc view: " + corpus + " > " + doc;
    if (initiatedVis.containsKey(canonicalTitle))
    {
      TabSheet.Tab addTab = ui.getTabSheet().addTab(initiatedVis.get(
        canonicalTitle), canonicalTitle);
      ui.getTabSheet().setSelectedTab(addTab);
      addTab.setClosable(true);
    }
    else
    {
      VisualizerPlugin visualizer = ((PluginSystem) ui).getVisualizer(
        "grid_tree");
      Component vis = visualizer.createComponent(createInput(corpus, doc), null);
      TabSheet.Tab visTab = ui.getTabSheet().addTab(vis, canonicalTitle);
      visTab.setClosable(true);
      ui.getTabSheet().setSelectedTab(vis);
      initiatedVis.put(canonicalTitle, vis);
    }
  }

  public void openDocBrowser(String corpus)
  {
    if (!initedDocBrowsers.containsKey(corpus))
    {
      TabSheet.Tab tab = DocBrowserPanel.initDocBrowserPanel(ui, corpus);
      initedDocBrowsers.put(corpus, tab);
      tab.getComponent().addDetachListener(new DetachDocBrowserListener(corpus));
    }
    else
    {
      ui.getTabSheet().setSelectedTab(initedDocBrowsers.get(corpus));
    }
  }

  private class DetachDocBrowserListener implements
    ClientConnector.DetachListener
  {

    String corpus;

    public DetachDocBrowserListener(String corpus)
    {
      this.corpus = corpus;
    }

    @Override
    public void detach(ClientConnector.DetachEvent event)
    {
      initedDocBrowsers.remove(corpus);
    }
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
