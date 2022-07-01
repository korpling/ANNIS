/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
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
package org.corpus_tools.annis.gui.docbrowser;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.api.CorporaApi;
import org.corpus_tools.annis.api.model.AnnotationComponentType;
import org.corpus_tools.annis.api.model.QueryLanguage;
import org.corpus_tools.annis.api.model.VisualizerRule;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.Background;
import org.corpus_tools.annis.gui.CommonUI;
import org.corpus_tools.annis.gui.Helper;
import org.corpus_tools.annis.gui.components.ExceptionDialog;
import org.corpus_tools.annis.gui.graphml.DocumentGraphMapper;
import org.corpus_tools.annis.gui.objects.RawTextWrapper;
import org.corpus_tools.annis.gui.visualizers.FilteringVisualizerPlugin;
import org.corpus_tools.annis.gui.visualizers.VisualizerInput;
import org.corpus_tools.annis.gui.visualizers.VisualizerPlugin;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SaltProject;
import org.eclipse.emf.common.util.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a global controller for the doc browser feature.
 *
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
public class DocBrowserController implements Serializable {

  private class DocVisualizerFetcher implements Runnable {

    VisualizerRule config;

    final String corpus;
    final String corpusId;


    private List<String> docPathDecoded;
    private List<String> docPathRaw;

    final Button btn;

    private final String canonicalTitle;

    private final String type;

    private final Panel visHolder;

    private VisualizerInput input;


    public DocVisualizerFetcher(String corpus, String corpusId, List<String> docPathDecoded,
        List<String> docPathRaw,
        String canonicalTitle, String type, Panel visHolder, VisualizerRule config, Button btn) {
      this.corpus = corpus;
      this.corpusId = corpusId;
      this.docPathDecoded = docPathDecoded;
      this.docPathRaw = docPathRaw;
      this.btn = btn;
      this.config = config;
      this.canonicalTitle = canonicalTitle;
      this.type = type;
      this.visHolder = visHolder;
    }

    @Override
    public void run() {
      input = null;

      final boolean createVis = !initiatedVis.containsKey(canonicalTitle);

      Optional<VisualizerPlugin> visualizer = ui.getVisualizerPlugins().stream()
          .filter(p -> Objects.equal(p.getShortName(), type)).findAny();

      List<String> nodeAnnoFilter = null;
      if (visualizer.isPresent() && visualizer.get() instanceof FilteringVisualizerPlugin) {
        nodeAnnoFilter =
            ((FilteringVisualizerPlugin) visualizer.get()).getFilteredNodeAnnotationNames(corpus,
                corpusId, docPathDecoded.get(docPathDecoded.size() - 1), config.getMappings(), ui);
      } else if (visualizer.isPresent() && visualizer.get().isUsingRawText()) {
        nodeAnnoFilter = new LinkedList<>();
      }

      // check if a visualization is already initiated
      {
        if (createVis && visualizer.isPresent()) {
          // fetch the salt project - so long part
          input = createInput(corpus, docPathDecoded, docPathRaw, config, nodeAnnoFilter,
              visualizer.get().isUsingRawText(), ui);
        }
      }

      // after initializing the visualizer update the gui
      ui.access(() -> {

        btn.setEnabled(true);

        if (createVis && input != null && visualizer.isPresent()) {
          // create and format visualizer

          Component vis1 = visualizer.get().createComponent(input, null);
          vis1.addStyleName(Helper.CORPUS_FONT_FORCE);
          vis1.setPrimaryStyleName("docviewer");
          vis1.setCaption(canonicalTitle);
          vis1.setWidth(100, Unit.PERCENTAGE);
          vis1.setHeight(-1, Unit.PIXELS);

          // update visualizer memory cache
          initiatedVis.put(canonicalTitle, vis1);
        }

        Component vis2 = initiatedVis.get(canonicalTitle);
        visHolder.setContent(vis2);

      });
    }

  }

  /**
   * 
   */
  private static final long serialVersionUID = 7035834674160143771L;

  private final static Logger log = LoggerFactory.getLogger(DocBrowserController.class);

  private static final Resource EYE_ICON = VaadinIcons.EYE;

  private static final Resource DOC_ICON = VaadinIcons.FILE_TEXT_O;


  /**
   * Creates the input. It only takes the salt project or the raw text from the text table, never
   * both, since the increase the performance for large texts.
   *
   * @param corpus the name of the toplevel corpus
   * @param docPathDecoded the path of the document with all path elements decoded from the URI
   *        * @param docPathRaw the path of the document with all path elements in its original
   *        unencoded form as given in the URI
   * @param config the visualizer configuration
   * @param nodeAnnoFilter A list of node annotation names for filtering the nodes or null if no
   *        filtering should be applied.
   * @param useRawText If true, only extract the original raw text
   * @return a {@link VisualizerInput} input, which is usable for rendering the whole document.
   */
  public static VisualizerInput createInput(String corpus, List<String> docPathDecoded,
      List<String> docPathRaw,
      VisualizerRule config, List<String> nodeAnnoFilter, boolean useRawText, CommonUI ui) {
    VisualizerInput input = new VisualizerInput();

    // set mappings and namespaces. some visualizer do not survive without
    input.setMappings(config.getMappings());
    input.setNamespace(config.getLayer());
    input.setUI(ui);
    input.setContextPath(ui.getServletContext().getContextPath());


    // get the whole document wrapped in a salt project
    try {
      CorporaApi api = new CorporaApi(Helper.getClient(ui));

      final SaltProject p = SaltFactory.createSaltProject();
      SCorpusGraph cg = p.createCorpusGraph();

      URI docURI = URI.createURI("salt:/" + Joiner.on('/').join(docPathDecoded));
      SDocument doc = cg.createDocument(docURI);

      // Build a query that includes all (possible filtered by name) node of the document
      String aql = Helper.buildDocumentQuery(docPathRaw, nodeAnnoFilter, useRawText);
      File graphML = api.subgraphForQuery(docPathDecoded.get(0), aql, QueryLanguage.AQL,
          useRawText ? AnnotationComponentType.ORDERING : null);

      SDocumentGraph docGraph = DocumentGraphMapper.map(graphML);
      doc.setDocumentGraph(docGraph);
      input.setResult(doc);
      if (useRawText) {
        input.setRawText(new RawTextWrapper(docGraph));
      }
    } catch (ApiException e) {
      log.error("General remote service exception", e);
    } catch (XMLStreamException | IOException ex) {
      log.error("Could not map GraphML to Salt", ex);
      ui.access(() -> ExceptionDialog.show(ex, "Could not map GraphML to Salt", ui));
    }

    return input;
  }


  // holds the complete state of the gui
  private final AnnisUI ui;

  // track the already initiated doc browsers
  private final Map<String, Component> initedDocBrowsers;

  // cache for already initiated visualizations, the key is the doc name
  private final Map<String, Component> initiatedVis;

  // keep track of already visible doc visualizer, so it easy to switch to them.
  private final Map<String, Panel> visibleVisHolder;

  public DocBrowserController(AnnisUI ui) {
    this.ui = ui;
    this.initedDocBrowsers = new HashMap<>();
    this.initiatedVis = new HashMap<>();
    this.visibleVisHolder = new HashMap<>();
  }

  public void openDocBrowser(String corpus) {
    // if not already init, do it now
    initedDocBrowsers.computeIfAbsent(corpus, k -> DocBrowserPanel.initDocBrowserPanel(ui, k));

    // init tab and put to front
    TabSheet.Tab tab =
        ui.getSearchView().getTabSheet().addTab(initedDocBrowsers.get(corpus), corpus);
    tab.setIcon(DOC_ICON);
    tab.setClosable(true);
    ui.getSearchView().getTabSheet().setSelectedTab(tab);
  }

  public void openDocVis(String corpus, String docId, VisualizerRule visConfig, Button btn) {

    List<String> pathDecoded = Helper.getCorpusPath(docId, true);

    final String canonicalTitle =
        Joiner.on(" > ").join(pathDecoded) + " - " + "Visualizer: " + visConfig.getDisplayName();
    final String tabCaption = StringUtils.substring(canonicalTitle, 0, 15) + "...";

    if (visibleVisHolder.containsKey(canonicalTitle)) {
      Panel visHolder = visibleVisHolder.get(canonicalTitle);
      ui.getSearchView().getTabSheet().setSelectedTab(visHolder);
      return;
    }

    Panel visHolder = new Panel();
    visHolder.setSizeFull();
    visHolder.addDetachListener(event -> visibleVisHolder.remove(canonicalTitle));

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

    List<String> pathRaw = Helper.getCorpusPath(docId, false);

    Background.run(new DocVisualizerFetcher(corpus, pathRaw.get(0), pathDecoded, pathRaw,
        canonicalTitle,
        visConfig.getVisType(), visHolder, visConfig, btn));
  }
}
