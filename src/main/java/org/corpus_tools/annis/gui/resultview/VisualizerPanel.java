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
package org.corpus_tools.annis.gui.resultview;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.xml.stream.XMLStreamException;
import org.corpus_tools.annis.api.CorporaApi;
import org.corpus_tools.annis.api.model.QueryLanguage;
import org.corpus_tools.annis.api.model.VisualizerRule;
import org.corpus_tools.annis.api.model.VisualizerRule.VisibilityEnum;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.Background;
import org.corpus_tools.annis.gui.Helper;
import org.corpus_tools.annis.gui.VisualizationToggle;
import org.corpus_tools.annis.gui.components.ExceptionDialog;
import org.corpus_tools.annis.gui.graphml.DocumentGraphMapper;
import org.corpus_tools.annis.gui.media.MediaController;
import org.corpus_tools.annis.gui.media.MediaPlayer;
import org.corpus_tools.annis.gui.media.PDFViewer;
import org.corpus_tools.annis.gui.objects.Match;
import org.corpus_tools.annis.gui.objects.RawTextWrapper;
import org.corpus_tools.annis.gui.visualizers.FilteringVisualizerPlugin;
import org.corpus_tools.annis.gui.visualizers.LoadableVisualizer;
import org.corpus_tools.annis.gui.visualizers.VisualizerInput;
import org.corpus_tools.annis.gui.visualizers.VisualizerPlugin;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.SNode;
import org.eclipse.emf.common.util.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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
        final Component createdComponent = future.get(120, TimeUnit.SECONDS);

        ui.access(() -> {
          vis = createdComponent;
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
        log.error("Could create visualizer {} in 2 minutes: Timeout",
            visPlugin == null ? UNKNOWN : visPlugin.getShortName(), ex);
        exception = ex;
      }

      if (exception != null) {
        final Throwable finalException = exception;
        ui.access(() -> Notification.show(
            "Error when creating visualizer "
                + (visPlugin == null ? UNKNOWN : visPlugin.getShortName()),
            finalException.toString(), Notification.Type.WARNING_MESSAGE));
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

  }

  public static final long serialVersionUID = 2L;

  public static final Resource ICON_COLLAPSE = VaadinIcons.MINUS_SQUARE_LEFT_O;

  public static final Resource ICON_EXPAND = VaadinIcons.PLUS_SQUARE_LEFT_O;

  private final Logger log = LoggerFactory.getLogger(VisualizerPanel.class);

  private List<String> documentPathRaw;
  private List<String> documentPathDecoded;

  private Component vis;

  private SDocument result;

  private VisualizerRule visRule;

  private Map<SNode, Long> markedAndCovered;

  private Button btEntry;

  private String htmlID;

  private String resultID;

  private final int visId;

  private VisualizerPlugin visPlugin = null;

  private Set<String> visibleTokenAnnos;

  private String segmentationName;

  private ProgressBar progress;

  private AnnisUI ui;

  private VisualizerContextChanger visCtxChanger;

  private static final String UNKNOWN = "<unknown>";


  /**
   * This Constructor should be used for {@link ComponentVisualizerPlugin} Visualizer.
   *
   */
  public VisualizerPanel(final VisualizerRule visRule, int visId, SDocument result, Match match,
      Set<String> visibleTokenAnnos, Map<SNode, Long> markedAndCovered, String htmlID,
      String resultID, VisualizerContextChanger parent, String segmentationName, AnnisUI ui) {
    this.ui = ui;
    this.visRule = visRule;
    this.visId = visId;

    this.visCtxChanger = parent;

    this.result = result;
    if (!match.getSaltIDs().isEmpty()) {
      this.documentPathRaw = Helper.getCorpusPath(match.getSaltIDs().get(0), false);
      this.documentPathDecoded = Helper.getCorpusPath(match.getSaltIDs().get(0), true);
    } else {
      this.documentPathRaw = new LinkedList<>();
      this.documentPathDecoded = new LinkedList<>();
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

    this.addStyleName(ValoTheme.PANEL_BORDERLESS);
    this.setWidth("100%");

  }

  @Override
  public void attach() {
    super.attach();

    if (visRule != null) {
      visPlugin = ui.getVisualizerPlugins().stream()
          .filter(plugin -> Objects.equal(plugin.getShortName(), visRule.getVisType())).findAny()
          .orElse(null);
      if (visPlugin == null) {
        // fallback to default visualizer if original vis type was not found
        visRule.setVisType(VisualizerPlugin.DEFAULT_VISUALIZER);
        visPlugin = ui.getVisualizerPlugins().stream()
            .filter(plugin -> Objects.equal(plugin.getShortName(), visRule.getVisType())).findAny()
            .orElse(null);
      }

      if (visRule.getVisibility() == VisibilityEnum.HIDDEN) {
        // build button for visualizer
        btEntry = new Button(visRule.getDisplayName());
        btEntry.setIcon(ICON_EXPAND);
        btEntry.setStyleName(ValoTheme.BUTTON_BORDERLESS + " " + ValoTheme.BUTTON_SMALL);
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
          btEntry.setStyleName(ValoTheme.BUTTON_BORDERLESS + " " + ValoTheme.BUTTON_SMALL);
          btEntry.addClickListener(this);
          addComponent(btEntry);
        }

        addComponent(progress);

        // create the visualizer and calc input
        try {
          vis = createComponent();
          if (vis != null) {
            vis.setVisible(true);
            addComponent(vis);
          }
        } catch (Exception ex) {
          Notification.show("Could not create visualizer " + getVisualizerShortNameDebug(),
              ex.toString(), Notification.Type.TRAY_NOTIFICATION);
          log.error("Could not create visualizer {}", getVisualizerShortNameDebug(), ex);
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

  private Component createComponent() {
    if (visPlugin == null) {
      return null;
    }

    final VisualizerInput input = createInput();

    Component c = visPlugin.createComponent(input, this);
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
    if (visPlugin != null && (visPlugin.isUsingText() || visPlugin.isUsingRawText())
        && result != null
        && !result.getDocumentGraph().getNodes().isEmpty()) {
      List<String> nodeAnnoFilter = null;
      if (visPlugin instanceof FilteringVisualizerPlugin) {

        nodeAnnoFilter = ((FilteringVisualizerPlugin) visPlugin).getFilteredNodeAnnotationNames(
            documentPathDecoded.get(0), documentPathRaw.get(0), input.getMappings(),
            ui);
      }
      SaltProject p = getDocument(nodeAnnoFilter, visPlugin.isUsingRawText(), ui);

      if (p != null && p.getCorpusGraphs() != null && !p.getCorpusGraphs().isEmpty()
          && p.getCorpusGraphs().get(0).getDocuments() != null
          && !p.getCorpusGraphs().get(0).getDocuments().isEmpty()) {
        SDocument wholeDocument = p.getCorpusGraphs().get(0).getDocuments().get(0);
        input.setDocument(wholeDocument);
        input.setRawText(new RawTextWrapper(wholeDocument.getDocumentGraph()));
      }
    } else {
      input.setDocument(result);
    }
    return input;
  }


  private SaltProject getDocument(List<String> nodeAnnoFilter, boolean useRawText, UI ui) {

    try {
      CorporaApi api = new CorporaApi(Helper.getClient(ui));
      // Reconstruct the document node name from the raw path of the match
      String documentNodeName = Joiner.on('/').join(documentPathRaw);
      String aql = Helper.buildDocumentQuery(documentNodeName, nodeAnnoFilter, useRawText);

      File graphML =
          api.subgraphForQuery(documentPathDecoded.get(0), aql, QueryLanguage.AQL, null).block();
      try {
        final SaltProject p = SaltFactory.createSaltProject();
        SCorpusGraph cg = p.createCorpusGraph();
        URI docURI = URI.createURI("salt:/" + Joiner.on('/').join(documentPathRaw));
        SDocument doc = cg.createDocument(docURI);
        SDocumentGraph docGraph = DocumentGraphMapper.map(graphML);
        doc.setDocumentGraph(docGraph);

        return p;
      } catch (XMLStreamException | IOException ex) {
        log.error("Could not map GraphML to Salt", ex);
        ui.access(() -> ExceptionDialog.show(ex, "Could not map GraphML to Salt", ui));
      }
    } catch (WebClientResponseException e) {
      log.error("General remote service exception", e);
    }
    return null;
  }

  public String getHtmlID() {
    return htmlID;
  }

  public String getVisualizerShortName() {
    return visPlugin == null ? null : visPlugin.getShortName();
  }

  private String getVisualizerShortNameDebug() {
    return visPlugin == null ? UNKNOWN : visPlugin.getShortName();
  }



  private void loadVisualizer(final LoadableVisualizer.Callback callback) {
    if (visPlugin != null) {
      btEntry.setIcon(ICON_COLLAPSE);
      progress.setIndeterminate(true);
      progress.setVisible(true);
      progress.setEnabled(true);
      progress.setDescription("Loading visualizer" + visPlugin.getShortName());

      ExecutorService execService = Executors.newSingleThreadExecutor();

      final Future<Component> future = execService.submit(() -> {
        // only create component if not already created
        if (vis == null) {
          return createComponent();
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

    if (visPlugin != null && vis != null) {
      visPlugin.setSegmentationLayer(vis, segmentationName, markedAndCovered);
    }
  }

  public void setVisibleTokenAnnosVisible(SortedSet<String> annos) {
    this.visibleTokenAnnos = annos;
    if (visPlugin != null && vis != null) {
      visPlugin.setVisibleTokenAnnosVisible(vis, annos);
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


  @Override
  public boolean visualizerIsVisible() {
    return vis != null && vis.isVisible();
  }

}
