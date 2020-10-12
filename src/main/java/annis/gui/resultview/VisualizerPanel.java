/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.resultview;

import annis.gui.AnnisUI;
import annis.gui.components.ExceptionDialog;
import annis.gui.graphml.DocumentGraphMapper;
import annis.libgui.AnnisBaseUI;
import annis.libgui.Background;
import annis.libgui.Helper;
import annis.libgui.VisualizationToggle;
import annis.libgui.media.MediaController;
import annis.libgui.media.MediaPlayer;
import annis.libgui.media.PDFViewer;
import annis.libgui.visualizers.FilteringVisualizerPlugin;
import annis.libgui.visualizers.VisualizerInput;
import annis.libgui.visualizers.VisualizerPlugin;
import annis.service.objects.Match;
import annis.service.objects.RawTextWrapper;
import annis.visualizers.LoadableVisualizer;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.v7.ui.themes.ChameleonTheme;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.stream.XMLStreamException;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.api.CorporaApi;
import org.corpus_tools.annis.api.SearchApi;
import org.corpus_tools.annis.api.model.AnnotationComponentType;
import org.corpus_tools.annis.api.model.QueryLanguage;
import org.corpus_tools.annis.api.model.VisualizerRule;
import org.corpus_tools.annis.api.model.VisualizerRule.VisibilityEnum;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.SNode;
import org.eclipse.emf.common.util.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controls the visibility of visualizer plugins and provides some control methods for the media
 * visualizers.
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 *
 */

public class VisualizerPanel extends CssLayout
    implements Button.ClickListener, VisualizationToggle {
  private class BackgroundJob implements Runnable {

    private final Future<Component> future;
    private final LoadableVisualizer.Callback callback;

    public BackgroundJob(Future<Component> future, LoadableVisualizer.Callback callback) {
      this.future = future;
      this.callback = callback;
    }

    @Override
    public void run() {

      Throwable exception = null;
      try {
        final Component result = future.get(60, TimeUnit.SECONDS);

        ui.accessSynchronously(() -> {
          vis = result;
          updateGUIAfterLoadingVisualizer(callback);
        });
      } catch (InterruptedException ex) {
        log.error(null, ex);
        exception = ex;
        // Restore interrupted state
        Thread.currentThread().interrupt();
      } catch (ExecutionException ex) {
        log.error(null, ex);
        exception = ex;
      } catch (TimeoutException ex) {
        future.cancel(true);
        log.error("Could create visualizer "
            + visPlugin.map(p -> p.getShortName()).orElse("<unknown>") + " in 60 seconds: Timeout",
            ex);
        exception = ex;
      }

      if (exception != null) {
        final Throwable finalException = exception;
        ui.accessSynchronously(() -> Notification.show(
            "Error when creating visualizer "
                + visPlugin.map(p -> p.getShortName()).orElse("<unknown>"),
            finalException.toString(), Notification.Type.WARNING_MESSAGE));
      }

    }
  }

  public static class ByteArrayOutputStreamSource implements StreamResource.StreamSource {

    /**
     * 
     */
    private static final long serialVersionUID = 814822953760083712L;

    private static final Logger log = LoggerFactory.getLogger(ByteArrayOutputStreamSource.class);

    private transient ByteArrayOutputStream byteStream;

    public ByteArrayOutputStreamSource(ByteArrayOutputStream byteStream) {
      this.byteStream = byteStream;
    }

    @Override
    public InputStream getStream() {
      if (byteStream == null) {
        log.error("byte stream was null");
        return null;
      }
      return new ByteArrayInputStream(byteStream.toByteArray());
    }
  }

  public static final long serialVersionUID = 2L;

  public static final Resource ICON_COLLAPSE = FontAwesome.MINUS_SQUARE_O;

  public static final Resource ICON_EXPAND = FontAwesome.PLUS_SQUARE_O;

  private final Logger log = LoggerFactory.getLogger(VisualizerPanel.class);

  private List<String> path;

  private Component vis;

  private SDocument result;

  private VisualizerRule visRule;

  private Map<SNode, Long> markedAndCovered;

  private Button btEntry;

  private String htmlID;

  private String resultID;

  private final int visId;

  private Optional<VisualizerPlugin> visPlugin = Optional.empty();

  private Set<String> visibleTokenAnnos;

  private String segmentationName;

  private ProgressBar progress;

  private AnnisUI ui;

  private VisualizerContextChanger visCtxChanger;


  private final Pattern validQNamePattern =
      Pattern.compile("([a-zA-Z_%][a-zA-Z0-9_\\-%]*:)?[a-zA-Z_%][a-zA-Z0-9_\\-%]*");

  /**
   * This Constructor should be used for {@link ComponentVisualizerPlugin} Visualizer.
   *
   */
  public VisualizerPanel(final VisualizerRule visRule, int visId, SDocument result, Match match,
      Set<String> visibleTokenAnnos, Map<SNode, Long> markedAndCovered, String htmlID,
      String resultID, VisualizerContextChanger parent, String segmentationName, AnnisUI ui)
      throws IOException {
    this.ui = ui;
    this.visRule = visRule;
    this.visId = visId;

    this.visCtxChanger = parent;

    this.result = result;
    if (!match.getSaltIDs().isEmpty()) {
      this.path = Helper.getCorpusPath(match.getSaltIDs().get(0));
    } else {
      this.path = new LinkedList<>();
    }
    this.visibleTokenAnnos = visibleTokenAnnos;
    this.markedAndCovered = markedAndCovered;
    this.segmentationName = segmentationName;
    this.htmlID = htmlID;
    this.resultID = resultID;

    this.progress = new ProgressBar();
    this.progress.setIndeterminate(true);
    this.progress.setVisible(false);
    this.progress.setEnabled(false);

    this.addStyleName(ChameleonTheme.PANEL_BORDERLESS);
    this.setWidth("100%");

  }

  @Override
  public void attach() {
    super.attach();

    if (visRule != null) {
      visPlugin = ui.getVisualizerPlugins().stream()
          .filter(plugin -> Objects.equal(plugin.getShortName(), visRule.getVisType())).findAny();
      if (!visPlugin.isPresent()) {
        // fallback to default visualizer if original vis type was not found
        visRule.setVisType(VisualizerPlugin.DEFAULT_VISUALIZER);
        visPlugin = ui.getVisualizerPlugins().stream()
            .filter(plugin -> Objects.equal(plugin.getShortName(), visRule.getVisType())).findAny();
      }

      if (visRule.getVisibility() == VisibilityEnum.HIDDEN) {
        // build button for visualizer
        btEntry = new Button(visRule.getDisplayName());
        btEntry.setIcon(ICON_EXPAND);
        btEntry.setStyleName(ChameleonTheme.BUTTON_BORDERLESS + " " + ChameleonTheme.BUTTON_SMALL);
        btEntry.addClickListener(this);
        btEntry.setDisableOnClick(true);

        addComponent(btEntry);
        addComponent(progress);
      } else {

        if (visRule.getVisibility() == VisibilityEnum.VISIBLE
            || visRule.getVisibility() == VisibilityEnum.PRELOADED) {
          // build button for visualizer
          btEntry = new Button(visRule.getDisplayName());
          btEntry.setIcon(ICON_COLLAPSE);
          btEntry
              .setStyleName(ChameleonTheme.BUTTON_BORDERLESS + " " + ChameleonTheme.BUTTON_SMALL);
          btEntry.addClickListener(this);
          addComponent(btEntry);
        }

        addComponent(progress);

        // create the visualizer and calc input
        try {
          vis = createComponent(UI.getCurrent());
          if (vis != null) {
            vis.setVisible(true);
            addComponent(vis);
          }
        } catch (Exception ex) {
          Notification.show(
              "Could not create visualizer "
                  + visPlugin.map(p -> p.getShortName()).orElse("<unknown>"),
              ex.toString(), Notification.Type.TRAY_NOTIFICATION);
          log.error("Could not create visualizer "
              + visPlugin.map(p -> p.getShortName()).orElse("<unknown>"), ex);
        }

        if (btEntry != null && visRule.getVisibility() == VisibilityEnum.PRELOADED) {
          btEntry.setIcon(ICON_EXPAND);
          if (vis != null) {
            vis.setVisible(false);
          }
        }

      }

    } // end if entry not null
  }

  @Override
  public void buttonClick(ClickEvent event) {

    boolean isVisible = !visualizerIsVisible();

    // register new state by the parent SingleResultPanel, so the state will be
    // still available, after a reload
    visCtxChanger.registerVisibilityStatus(visId, isVisible);

    // start the toogle process.
    toggleVisualizer(isVisible, null);
  }

  private Component createComponent(UI ui) {
    if (!visPlugin.isPresent()) {
      return null;
    }

    final VisualizerInput input = createInput();

    Component c = visPlugin.get().createComponent(input, this);
    if (c == null) {
      return c;
    }
    c.setVisible(false);
    c.addStyleName(Helper.CORPUS_FONT);
    c.addStyleName("vis-content");

    return c;
  }

  private VisualizerInput createInput() {
    VisualizerInput input = new VisualizerInput();
    input.setUI(ui);
    input.setContextPath(ui.getServletContext().getContextPath());
    input.setId(resultID);

    input.setMarkedAndCovered(markedAndCovered);

    input.setResult(result);
    input.setVisibleTokenAnnos(visibleTokenAnnos);
    input.setSegmentationName(segmentationName);
    if (ui.getInstanceConfig() != null && ui.getInstanceConfig().getFont() != null) {
      input.setFont(ui.getInstanceConfig().getFont());
    }

    if (visRule != null) {
      input.setMappings(visRule.getMappings());
      input.setNamespace(visRule.getLayer());
      String template =
          ui.getServletContext().getContextPath() + "/Resource/" + visRule.getVisType() + "/%s";
      input.setResourcePathTemplate(template);
    }

    // getting the whole document, when plugin is using text
    if (visPlugin.isPresent() && visPlugin.get().isUsingText() && result != null
        && result.getDocumentGraph().getNodes().size() > 0) {
      List<String> nodeAnnoFilter = null;
      if (visPlugin.get() instanceof FilteringVisualizerPlugin) {
        nodeAnnoFilter =
            ((FilteringVisualizerPlugin) visPlugin.get()).getFilteredNodeAnnotationNames(
                path.get(0), Joiner.on('/').join(path), input.getMappings(), ui);
      }
      SaltProject p = getDocument(nodeAnnoFilter, ui);

      SDocument wholeDocument = null;
      if (p != null && p.getCorpusGraphs() != null && !p.getCorpusGraphs().isEmpty()
          && p.getCorpusGraphs().get(0).getDocuments() != null
          && !p.getCorpusGraphs().get(0).getDocuments().isEmpty()) {
        wholeDocument = p.getCorpusGraphs().get(0).getDocuments().get(0);

      }
      input.setDocument(wholeDocument);
    } else {
      input.setDocument(result);
    }

    // getting the raw text, when the visualizer wants to have it
    if (visPlugin.isPresent() && visPlugin.get().isUsingRawText()) {
      input.setRawText(getRawText(path.get(0), path, ui));
    }

    return input;
  }


  private static RawTextWrapper getRawText(String corpusName, List<String> docPath, UI ui) {
    RawTextWrapper result = null;
    SearchApi api = new SearchApi(Helper.getClient(ui));
    try {

      StringBuilder aql = new StringBuilder();
      aql.append("(t#tok | t#annis:ignored-tok) & doc#annis:node_name=/");
      aql.append(Helper.AQL_REGEX_VALUE_ESCAPER.escape(Joiner.on('/').join(docPath)));
      aql.append("/ & #t @* doc");



      result = new RawTextWrapper();
      File graphML = api.subgraphForQuery(corpusName, aql.toString(), QueryLanguage.AQL,
          AnnotationComponentType.ORDERING);
      SDocumentGraph graph = DocumentGraphMapper.map(graphML);
      // Reconstruct the text from the token values
      List<String> texts = new ArrayList<>();
      for (STextualDS ds : graph.getTextualDSs()) {
        texts.add(ds.getData());
      }

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

  private SaltProject getDocument(List<String> nodeAnnoFilter, UI ui) {

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
        aql.append(Helper.AQL_REGEX_VALUE_ESCAPER.escape(Joiner.on('/').join(path)));
        aql.append("/");
      } else {
        aql.append("(a#tok");
        for (String nodeAnno : nodeAnnoFilter) {
          aql.append(" | a#");
          aql.append(nodeAnno);
        }
        aql.append(") & d#annis:node_name=/");
        aql.append(Helper.AQL_REGEX_VALUE_ESCAPER.escape(Joiner.on('/').join(path)));
        aql.append("/ & #a @* #d");
      }


      File graphML = api.subgraphForQuery(path.get(0), aql.toString(), QueryLanguage.AQL, null);
      try {
        final SaltProject p = SaltFactory.createSaltProject();
        SCorpusGraph cg = p.createCorpusGraph();
        URI docURI = URI.createURI("salt:/" + Joiner.on('/').join(path));
        SDocument doc = cg.createDocument(docURI);
        SDocumentGraph docGraph = DocumentGraphMapper.map(graphML);
        doc.setDocumentGraph(docGraph);

        return p;
      } catch (XMLStreamException | IOException ex) {
        log.error("Could not map GraphML to Salt", ex);
        ui.access(() -> ExceptionDialog.show(ex, "Could not map GraphML to Salt", ui));
      }
    } catch (ApiException e) {
      log.error("General remote service exception", e);
    }
    return null;
  }

  public String getHtmlID() {
    return htmlID;
  }

  protected SDocument getResult() {
    return result;
  }

  public String getVisualizerShortName() {
    if (visPlugin.isPresent()) {
      return visPlugin.get().getShortName();
    }

    else {
      return null;
    }
  }

  private void loadVisualizer(final LoadableVisualizer.Callback callback) {
    if (visPlugin.isPresent()) {
      btEntry.setIcon(ICON_COLLAPSE);
      progress.setIndeterminate(true);
      progress.setVisible(true);
      progress.setEnabled(true);
      progress.setDescription("Loading visualizer" + visPlugin.get().getShortName());

      ExecutorService execService = Executors.newSingleThreadExecutor();

      final Future<Component> future = execService.submit(() -> {
        // only create component if not already created
        if (vis == null) {
          return createComponent(ui);
        } else {
          return vis;
        }
      });

      // run the actual code to load the visualizer
      Background.run(new BackgroundJob(future, callback));

    } // end if create input was needed

  } // end loadVisualizer

  public void setSegmentationLayer(String segmentationName, Map<SNode, Long> markedAndCovered) {
    this.segmentationName = segmentationName;
    this.markedAndCovered = markedAndCovered;

    if (visPlugin.isPresent() && vis != null) {
      visPlugin.get().setSegmentationLayer(vis, segmentationName, markedAndCovered);
    }
  }

  public void setVisibleTokenAnnosVisible(SortedSet<String> annos) {
    this.visibleTokenAnnos = annos;
    if (visPlugin.isPresent() && vis != null) {
      visPlugin.get().setVisibleTokenAnnosVisible(vis, annos);
    }
  }

  @Override
  public void toggleVisualizer(boolean visible, LoadableVisualizer.Callback callback) {
    if (visible) {
      loadVisualizer(callback);
    } else {
      // hide
      btEntry.setEnabled(true);

      if (vis != null) {
        vis.setVisible(false);
        if (vis instanceof MediaPlayer) {
          removeComponent(vis);
        }

      }

      btEntry.setIcon(ICON_EXPAND);

    }
  }

  private void updateGUIAfterLoadingVisualizer(LoadableVisualizer.Callback callback) {
    if (callback != null && vis instanceof LoadableVisualizer) {
      LoadableVisualizer loadableVis = (LoadableVisualizer) vis;
      if (loadableVis.isLoaded()) {
        // direct call callback since the visualizer is already ready
        callback.visualizerLoaded(loadableVis);
      } else {
        loadableVis.clearCallbacks();
        // add listener when player was fully loaded
        loadableVis.addOnLoadCallBack(callback);
      }
    }

    progress.setEnabled(false);
    progress.setVisible(false);

    if (vis != null) {
      btEntry.setEnabled(true);
      vis.setVisible(true);
      if (vis instanceof PDFViewer) {
        ((PDFViewer) vis).openPDFPage("-1");
      }
      if (vis instanceof MediaPlayer) {
        // if this is a media player visualizer, close all other media players
        // since some browsers (e.g. Chrome) have problems if there are multiple
        // audio/video elements on one page
        MediaController mediaController =
            VaadinSession.getCurrent().getAttribute(MediaController.class);
        mediaController.closeOtherPlayers((MediaPlayer) vis);

      }
      // add if not already added
      if (getComponentIndex(vis) < 0) {
        addComponent(vis);
      }
    }
  }

  @Override
  public boolean visualizerIsVisible() {
    return vis != null && vis.isVisible();
  }

}
