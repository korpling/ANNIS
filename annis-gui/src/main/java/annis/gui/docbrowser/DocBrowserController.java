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
package annis.gui.docbrowser;

import annis.CommonHelper;
import annis.gui.AnnisUI;
import annis.gui.components.ExceptionDialog;
import annis.gui.graphml.DocumentGraphMapper;
import annis.libgui.AnnisBaseUI;
import annis.libgui.Background;
import annis.libgui.Helper;
import annis.libgui.visualizers.FilteringVisualizerPlugin;
import annis.libgui.visualizers.VisualizerInput;
import annis.libgui.visualizers.VisualizerPlugin;
import annis.service.objects.RawTextWrapper;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.api.CorporaApi;
import org.corpus_tools.annis.api.SearchApi;
import org.corpus_tools.annis.api.model.AnnotationComponentType;
import org.corpus_tools.annis.api.model.QueryLanguage;
import org.corpus_tools.annis.api.model.VisualizerRule;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.STextualDS;
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

    String corpus;

    List<String> docPath;

    final Button btn;

    private final String canonicalTitle;

    private final String type;

    private final Panel visHolder;

    private VisualizerInput input;


    public DocVisualizerFetcher(String corpus, List<String> docPath, String canonicalTitle,
        String type,
        Panel visHolder, VisualizerRule config, Button btn, final UI ui) {
      this.corpus = corpus;
      this.docPath = docPath;
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

      Optional<VisualizerPlugin> visualizer = ui.getVisualizerPlugins()
          .stream()
          .filter(p -> Objects.equal(p.getShortName(), type)).findAny();

      List<String> nodeAnnoFilter = null;
      if (visualizer.isPresent() && visualizer.get() instanceof FilteringVisualizerPlugin) {
        nodeAnnoFilter = ((FilteringVisualizerPlugin) visualizer.get())
            .getFilteredNodeAnnotationNames(corpus, docPath.get(docPath.size() - 1),
                config.getMappings(), ui);
      } else if (visualizer.isPresent() && visualizer.get().isUsingRawText()) {
        nodeAnnoFilter = new LinkedList<>();
        nodeAnnoFilter.add("annis:tok");
      }

      // check if a visualization is already initiated
      {
        if (createVis && visualizer.isPresent()) {
          // fetch the salt project - so long part
          input = createInput(corpus, docPath, config, nodeAnnoFilter, ui);
          if (visualizer.isPresent() && visualizer.get().isUsingRawText()) {
            input.setRawText(getRawText(corpus, Joiner.on('/').join(docPath), ui));
          }
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

  private static final Resource EYE_ICON = FontAwesome.EYE;

  private static final Resource DOC_ICON = FontAwesome.FILE_TEXT_O;

  private final static Escaper urlPathEscape = UrlEscapers.urlPathSegmentEscaper();


  private static final Pattern validQNamePattern =
      Pattern.compile("([a-zA-Z_%][a-zA-Z0-9_\\-%]*:)?[a-zA-Z_%][a-zA-Z0-9_\\-%]*");

  /**
   * Creates the input. It only takes the salt project or the raw text from the text table, never
   * both, since the increase the performance for large texts.
   *
   * @param corpus the name of the toplevel corpus
   * @param docPath the path of the document
   * @param config the visualizer configuration
   * @param nodeAnnoFilter A list of node annotation names for filtering the nodes or null if no
   *        filtering should be applied.
   * @return a {@link VisualizerInput} input, which is usable for rendering the whole document.
   */
  public static VisualizerInput createInput(String corpus, List<String> docPath,
      VisualizerRule config, List<String> nodeAnnoFilter, UI ui) {
    VisualizerInput input = new VisualizerInput();

    // set mappings and namespaces. some visualizer do not survive without
    input.setMappings(config.getMappings());
    input.setNamespace(config.getLayer());
    input.setUI(ui);


    // get the whole document wrapped in a salt project
    try {
      CorporaApi api = new CorporaApi(Helper.getClient(ui));

      // Build a query that includes all (possible filtered by name) node of the document
      boolean fallbackToAll = false;
      if (nodeAnnoFilter == null || nodeAnnoFilter.isEmpty()) {
        fallbackToAll = true;
      } else {
        nodeAnnoFilter = nodeAnnoFilter.stream()
            .map((anno_name) -> anno_name.replaceFirst("::", ":")).collect(Collectors.toList());
        for (String nodeAnno : nodeAnnoFilter) {
          if (!validQNamePattern.matcher(nodeAnno).matches()) {
            // If we can't produce a valid query for this annotation name fallback
            // to retrieve all annotations.
            fallbackToAll = true;
            break;
          }
        }
      }

      StringBuilder aql = new StringBuilder();
      if (fallbackToAll) {
        aql.append("node @* annis:node_name=/");
        aql.append(Helper.AQL_REGEX_VALUE_ESCAPER.escape(Joiner.on('/').join(docPath)));
        aql.append("/");
      } else {
        aql.append("(a#tok");
        for (String nodeAnno : nodeAnnoFilter) {
          aql.append(" | a#");
          aql.append(nodeAnno);
        }
        aql.append(") & d#annis:node_name=/");
        aql.append(Helper.AQL_REGEX_VALUE_ESCAPER.escape(Joiner.on('/').join(docPath)));
        aql.append("/ & #a @* #d");
      }


      String graphML =
          api.subgraphForQuery(docPath.get(0), aql.toString(), QueryLanguage.AQL, null);
      try {
        final SaltProject p = SaltFactory.createSaltProject();
        SCorpusGraph cg = p.createCorpusGraph();
        URI docURI = URI.createURI("salt:/" + Joiner.on('/').join(docPath));
        SDocument doc = cg.createDocument(docURI);
        SDocumentGraph docGraph = DocumentGraphMapper.map(new StringReader(graphML));
        doc.setDocumentGraph(docGraph);

        SDocument sDoc = p.getCorpusGraphs().get(0).getDocuments().get(0);
        input.setResult(sDoc);
      } catch (XMLStreamException | IOException ex) {
        log.error("Could not map GraphML to Salt", ex);
        ui.access(() -> ExceptionDialog.show(ex, "Could not map GraphML to Salt", ui));
      }
    } catch (ApiException e) {
      log.error("General remote service exception", e);
    }

    return input;
  }

  private static RawTextWrapper getRawText(String corpusName, String documentName, UI ui) {
    RawTextWrapper result = null;
    SearchApi api = new SearchApi(Helper.getClient(ui));
    try {

      String graphML = api.subgraphForQuery(corpusName, "tok | annis:node_type=\"ignored-tok\"",
          QueryLanguage.AQL, AnnotationComponentType.ORDERING);

      SDocumentGraph graph = DocumentGraphMapper.map(new StringReader(graphML), true);
      // Reconstruct the text from the token values
      List<String> texts = new ArrayList<>();
      for (STextualDS ds : graph.getTextualDSs()) {
        texts.add(ds.getData());
      }
      result = new RawTextWrapper();
      result.setTexts(texts);
    }

    catch (ApiException | XMLStreamException | IOException ex) {
      if (!AnnisBaseUI.handleCommonError(ex, "retrieve raw text")) {
        Notification.show("can not retrieve raw text", ex.getLocalizedMessage(),
            Notification.Type.WARNING_MESSAGE);
      }
    }

    return result;
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
    if (!initedDocBrowsers.containsKey(corpus)) {
      DocBrowserPanel docBrowser = DocBrowserPanel.initDocBrowserPanel(ui, corpus);
      initedDocBrowsers.put(corpus, docBrowser);
    }

    // init tab and put to front
    TabSheet.Tab tab =
        ui.getSearchView().getTabSheet().addTab(initedDocBrowsers.get(corpus), corpus);
    tab.setIcon(DOC_ICON);
    tab.setClosable(true);
    ui.getSearchView().getTabSheet().setSelectedTab(tab);
  }

  public void openDocVis(String corpus, String docId, VisualizerRule visConfig, Button btn) {

    List<String> path = CommonHelper.getCorpusPath(docId);
    
    final String canonicalTitle =
        Joiner.on(" > ").join(path) + " - " + "Visualizer: " + visConfig.getDisplayName();
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

    Background.run(new DocVisualizerFetcher(corpus, path, canonicalTitle, visConfig.getVisType(),
        visHolder, visConfig, btn, ui));
  }
}
