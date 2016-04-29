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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SaltProject;

import com.google.common.base.Joiner;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.server.ClientConnector;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import annis.gui.AnnisUI;
import annis.libgui.Background;
import annis.libgui.Helper;
import annis.libgui.PluginSystem;
import annis.libgui.visualizers.FilteringVisualizerPlugin;
import annis.libgui.visualizers.VisualizerInput;
import annis.libgui.visualizers.VisualizerPlugin;
import annis.service.objects.CorpusConfig;
import annis.service.objects.RawTextWrapper;
import annis.service.objects.Visualizer;

/**
 * Represents a global controller for the doc browser feature.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class DocBrowserController implements Serializable
{

  // holds the complete state of the gui
  private final AnnisUI ui;

  // track the already initiated doc browsers
  private final Map<String, Component> initedDocBrowsers;

  // cache for already initiated visualizations, the key is the doc name
  private final Map<String, Component> initiatedVis;

  // keep track of already visible doc visualizer, so it easy to switch to them.
  private final Map<String, Panel> visibleVisHolder;

  private static final Resource EYE_ICON = FontAwesome.EYE;

  private static final Resource DOC_ICON = FontAwesome.FILE_TEXT_O;
  
  private final static Escaper urlPathEscape = UrlEscapers.urlPathSegmentEscaper();

  public DocBrowserController(AnnisUI ui)
  {
    this.ui = ui;
    this.initedDocBrowsers = new HashMap<>();
    this.initiatedVis = new HashMap<>();
    this.visibleVisHolder = new HashMap<>();
  }

  public void openDocVis(String corpus, String doc, Visualizer visConfig, Button btn)
  {

    final String canonicalTitle = corpus + " > " + doc + " - " + "Visualizer: " + visConfig.
      getDisplayName();
    final String tabCaption = StringUtils.substring(canonicalTitle, 0, 15) + "...";

    if (visibleVisHolder.containsKey(canonicalTitle))
    {
      Panel visHolder = visibleVisHolder.get(canonicalTitle);
      ui.getSearchView().getTabSheet().setSelectedTab(visHolder);
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
    VerticalLayout layoutProgress = new VerticalLayout(progressBar);
    layoutProgress.setSizeFull();
    layoutProgress.setComponentAlignment(progressBar, Alignment.MIDDLE_CENTER);
    
    visHolder.setContent(layoutProgress);
    
    Tab visTab = ui.getSearchView().getTabSheet().addTab(visHolder, tabCaption);
    visTab.setDescription(canonicalTitle);
    visTab.setIcon(EYE_ICON);
    visTab.setClosable(true);
    ui.getSearchView().getTabSheet().setSelectedTab(visTab);

    // register visible visHolder
    this.visibleVisHolder.put(canonicalTitle, visHolder);

    Background.run(new DocVisualizerFetcher(corpus, doc, canonicalTitle,
        visConfig.getType(), visHolder, visConfig, btn, UI.getCurrent())
    );
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
    TabSheet.Tab tab = ui.getSearchView().getTabSheet().addTab(initedDocBrowsers.get(corpus),
      corpus);
    tab.setIcon(DOC_ICON);
    tab.setClosable(true);
    ui.getSearchView().getTabSheet().setSelectedTab(tab);
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
   * @param nodeAnnoFilter A list of node annotation names for filtering the nodes or null if no filtering should be applied.
   * @return a {@link VisualizerInput} input, which is usable for rendering the
   * whole document.
   */
  public static VisualizerInput createInput(String corpus, String docName,
    Visualizer config, boolean isUsingRawText, List<String> nodeAnnoFilter)
  {
    VisualizerInput input = new VisualizerInput();

    // set mappings and namespaces. some visualizer do not survive without   
    input.setMappings(parseMappings(config));
    input.setNamespace(config.getNamespace());
    
    String encodedToplevelCorpus = urlPathEscape.escape(corpus);
    String encodedDocument = urlPathEscape.escape(docName);
    if (isUsingRawText)
    {
      WebResource w = Helper.getAnnisWebResource();
      w = w.path("query").path("rawtext")
        .path(encodedToplevelCorpus).path(encodedDocument);
      RawTextWrapper rawTextWrapper = w.get(RawTextWrapper.class);
      input.setRawText(rawTextWrapper);
    }
    else 
    {
      // get the whole document wrapped in a salt project
      SaltProject txt = null;

      WebResource res = Helper.getAnnisWebResource()
        .path("query").path("graph").
        path(encodedToplevelCorpus).
        path(encodedDocument);

      if(nodeAnnoFilter != null)
      {
        res = res.queryParam("filternodeanno", Joiner.on(",").join(nodeAnnoFilter));
      }

      txt = res.get(SaltProject.class);

      if (txt != null)
      {
        SDocument sDoc = txt.getCorpusGraphs().get(0).getDocuments().get(0);
        input.setResult(sDoc);
      }
    }


    return input;
  }

  private static Properties parseMappings(Visualizer config)
  {
    Properties mappings = new Properties();


    if (config.getMappings() != null)
    {
      // split the entrys
      String[] entries = config.getMappings().split(";");
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

  private class DocVisualizerFetcher implements Runnable
  {

    Visualizer config;

    String corpus;

    String doc;

    final Button btn;

    private final String canonicalTitle;

    private final String type;

    private final Panel visHolder;
    
    private VisualizerInput input;

    public DocVisualizerFetcher(String corpus, String doc, String canonicalTitle,
      String type,
      Panel visHolder,
      Visualizer config,
      Button btn,
      final UI ui)
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
      input = null;
      
      final boolean createVis = !initiatedVis.containsKey(canonicalTitle);
      
      final VisualizerPlugin visualizer = ((PluginSystem) ui).
              getVisualizer(type);
      
      List<String> nodeAnnoFilter = null;
      if(visualizer instanceof FilteringVisualizerPlugin)
      {
        nodeAnnoFilter = ((FilteringVisualizerPlugin) visualizer)
          .getFilteredNodeAnnotationNames(corpus, doc, parseMappings(config));
      }
      
      // check if a visualization is already initiated
      {
        if (createVis)
        {
          // fetch the salt project - so long part
          input = createInput(corpus, doc, config, visualizer.
            isUsingRawText(), nodeAnnoFilter);

        }
      }
     
      // after initializing the visualizer update the gui
      ui.access(new Runnable()
      {
        @Override
        public void run()
        {
          
          btn.setEnabled(true);

          if (createVis && input != null)
          {
            // create and format visualizer
            
            Component vis = visualizer.createComponent(input, null);
            vis.addStyleName(Helper.CORPUS_FONT_FORCE);
            vis.setPrimaryStyleName("docviewer");
            vis.setCaption(canonicalTitle);
            vis.setWidth(100, Unit.PERCENTAGE);
            vis.setHeight(-1, Unit.PIXELS);

            // update visualizer memory cache
            initiatedVis.put(canonicalTitle, vis);
          }

          Component vis = initiatedVis.get(canonicalTitle);
          visHolder.setContent(vis);

        }
      });
    }
  }

  public boolean docsAvailable(String id)
  {
    if (ui != null)
    {
      CorpusConfig corpusConfig = ui.getCorpusConfigWithCache(id);

      if (corpusConfig != null)
      {
        if (corpusConfig.containsKey("browse-documents"))
        {
          return Boolean.
            parseBoolean(corpusConfig.getConfig("browse-documents"));
        }

        // get the default config
        else
        {
          corpusConfig = ui.getCorpusConfigWithCache(Helper.DEFAULT_CONFIG);
          boolean browseDocuments = Boolean.parseBoolean(
            corpusConfig.getConfig("browse-documents", "true"));
          return browseDocuments;
        }
      }
    }

    return true;
  }
}
