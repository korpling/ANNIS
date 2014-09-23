/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.resultview;

import annis.CommonHelper;
import annis.libgui.Helper;
import annis.libgui.InstanceConfig;
import annis.libgui.PluginSystem;
import annis.libgui.PollControl;
import annis.libgui.VisualizationToggle;
import annis.libgui.media.MediaController;
import annis.libgui.media.MediaPlayer;
import annis.libgui.media.PDFViewer;
import annis.libgui.visualizers.FilteringVisualizerPlugin;
import annis.libgui.visualizers.VisualizerInput;
import annis.libgui.visualizers.VisualizerPlugin;
import annis.resolver.ResolverEntry;
import annis.visualizers.LoadableVisualizer;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.themes.ChameleonTheme;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import static annis.model.AnnisConstants.*;
import com.google.common.base.Joiner;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SFeature;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SGraph;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controls the visibility of visualizer plugins and provides some control
 * methods for the media visualizers.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 *
 */
public class VisualizerPanel extends CssLayout
  implements Button.ClickListener, VisualizationToggle
{
  public static final long serialVersionUID = 2L;

  private final Logger log = LoggerFactory.getLogger(VisualizerPanel.class);

  public static final Resource ICON_COLLAPSE = FontAwesome.MINUS_SQUARE_O;

  public static final Resource ICON_EXPAND = FontAwesome.PLUS_SQUARE_O;

  private String corpusName;

  private String documentName;

  private Component vis;

  private SDocument result;

  private PluginSystem ps;

  private ResolverEntry entry;

  private Map<String, Long> markedAndCovered;

  private Map<String, String> markersExact;

  private Map<String, String> markersCovered;

  private Button btEntry;

  private String htmlID;

  private String resultID;

  private VisualizerPlugin visPlugin;

  private Set<String> visibleTokenAnnos;

  private String segmentationName;

  private final String PERMANENT = "permanent";

  private final String ISVISIBLE = "visible";

  private final String HIDDEN = "hidden";

  private final String PRELOADED = "preloaded";

  private ProgressBar progress;

  private InstanceConfig instanceConfig;

  private VisualizerContextChanger visCtxChanger;

  /**
   * This Constructor should be used for {@link ComponentVisualizerPlugin}
   * Visualizer.
   *
   */
  public VisualizerPanel(
    final ResolverEntry entry,
    SDocument result,
    String corpusName,
    String documentName,
    Set<String> visibleTokenAnnos,
    Map<String, Long> markedAndCovered,
    @Deprecated Map<String, String> markedAndCoveredMap,
    @Deprecated Map<String, String> markedExactMap,
    String htmlID,
    String resultID,
    SingleResultPanel parent,
    String segmentationName,
    PluginSystem ps,
    InstanceConfig instanceConfig) throws IOException
  {

    this.ps = ps;
    this.instanceConfig = instanceConfig;
    this.entry = entry;
    this.markersExact = markedExactMap;
    this.markersCovered = markedAndCoveredMap;

    this.visCtxChanger = parent;

    this.result = result;
    this.corpusName = corpusName;
    this.documentName = documentName;
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

    if (entry != null && ps != null)
    {
      visPlugin = ps.getVisualizer(entry.getVisType());
      if (visPlugin == null)
      {
        // fallback to default visualizer if original vis type was not found
        entry.setVisType(PluginSystem.DEFAULT_VISUALIZER);
        visPlugin = ps.getVisualizer(entry.getVisType());
      }

      if (HIDDEN.equalsIgnoreCase(entry.getVisibility()))
      {
        // build button for visualizer
        btEntry = new Button(entry.getDisplayName());
        btEntry.setIcon(ICON_EXPAND);
        btEntry.setStyleName(ChameleonTheme.BUTTON_BORDERLESS + " "
          + ChameleonTheme.BUTTON_SMALL);
        btEntry.addClickListener((Button.ClickListener) this);
        btEntry.setDisableOnClick(true);
        
        addComponent(btEntry);
        addComponent(progress);
      }
      else
      {

        if (ISVISIBLE.equalsIgnoreCase(entry.getVisibility())
          || PRELOADED.equalsIgnoreCase(entry.getVisibility()))
        {
          // build button for visualizer
          btEntry = new Button(entry.getDisplayName());
          btEntry.setIcon(ICON_COLLAPSE);
          btEntry.setStyleName(ChameleonTheme.BUTTON_BORDERLESS + " "
            + ChameleonTheme.BUTTON_SMALL);
          btEntry.addClickListener((Button.ClickListener) this);
          addComponent(btEntry);
        }
        
        addComponent(progress);

        // create the visualizer and calc input
        try
        {
          vis = createComponent();
          if (vis != null)
          {
            vis.setVisible(true);
            addComponent(vis);
          }
        }
        catch (Exception ex)
        {
          Notification.show(
            "Could not create visualizer " + visPlugin.getShortName(),
            ex.toString(),
            Notification.Type.TRAY_NOTIFICATION);
          log.error("Could not create visualizer " + visPlugin.getShortName(),
            ex);
        }

        if (btEntry != null && PRELOADED.equalsIgnoreCase(entry.getVisibility()))
        {
          btEntry.setIcon(ICON_EXPAND);
          if (vis != null)
          {
            vis.setVisible(false);
          }
        }

      }

    } // end if entry not null

  }
  
  private void writeObject(ObjectOutputStream out) throws IOException
  {
    out.defaultWriteObject();
    
    CommonHelper.writeSDocument(result, out);
  }
  
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    in.defaultReadObject();
    
   this.result = CommonHelper.readSDocument(in);
  }
  
  private List<SToken> createTokenList(List<String> tokenIDs, SDocumentGraph graph)
  {
    if(tokenIDs == null || graph == null)
    {
      return new LinkedList<>();
    }
    ArrayList<SToken> r = new ArrayList<>(tokenIDs.size());
    for(String t : tokenIDs)
    {
      SNode n = graph.getSNode(t);
      if(n instanceof SToken)
      {
        r.add((SToken) n);
      }
    }
    return r;
  }

  private Component createComponent()
  {
    if (visPlugin == null)
    {
      return null;
    }

    final VisualizerInput input = createInput();

    Component c = visPlugin.createComponent(input, this);
    c.setVisible(false);
    c.addStyleName("corpus-font");
    c.addStyleName("vis-content");

    return c;
  }

  private VisualizerInput createInput()
  {
    VisualizerInput input = new VisualizerInput();
    input.setAnnisWebServiceURL((String) VaadinSession.getCurrent().
      getAttribute("AnnisWebService.URL"));
    input.setContextPath(Helper.getContext());
    input.
      setDotPath((String) VaadinSession.getCurrent().getAttribute("DotPath"));

    input.setId(resultID);

    input.setMarkableExactMap(markersExact);
    input.setMarkableMap(markersCovered);
    input.setMarkedAndCovered(markedAndCovered);

    input.setResult(result);
    input.setVisibleTokenAnnos(visibleTokenAnnos);
    input.setSegmentationName(segmentationName);
    if (instanceConfig != null && instanceConfig.getFont() != null)
    {
      input.setFont(instanceConfig.getFont());
    }

    if (entry != null)
    {
      input.setMappings(entry.getMappings());
      input.setNamespace(entry.getNamespace());
      String template = Helper.getContext()
        + "/Resource/" + entry.getVisType() + "/%s";
      input.setResourcePathTemplate(template);
    }

    // getting the whole document, when plugin is using text
    if (visPlugin != null
      && visPlugin.isUsingText()
      && result != null
      && result.getSDocumentGraph().getSNodes().size() > 0)
    {
      List<String> nodeAnnoFilter = null;
      if(visPlugin instanceof FilteringVisualizerPlugin)
      {
        nodeAnnoFilter = ((FilteringVisualizerPlugin) visPlugin).getFilteredNodeAnnotationNames(
          corpusName, documentName, input.getMappings());
      }
      SaltProject p = getDocument(result.getSCorpusGraph().getSRootCorpus().
        get(0).getSName(), result.getSName(), nodeAnnoFilter);

      SDocument wholeDocument = p.getSCorpusGraphs().get(0).getSDocuments()
        .get(0);

      input.setDocument(wholeDocument);
    }
    else
    {
      input.setDocument(result);
    }

    // getting the raw text, when the visualizer wants to have it
    if (visPlugin != null && visPlugin.isUsingRawText())
    {
      input.setRawText(Helper.getRawText(corpusName, documentName));
    }

    return input;
  }

  public void setVisibleTokenAnnosVisible(SortedSet<String> annos)
  {
    this.visibleTokenAnnos = annos;
    if (visPlugin != null && vis != null)
    {
      visPlugin.setVisibleTokenAnnosVisible(vis, annos);
    }
  }

  public void setSegmentationLayer(String segmentationName,
    Map<String, Long> markedAndCovered)
  {
    this.segmentationName = segmentationName;
    this.markedAndCovered = markedAndCovered;
    
    if (visPlugin != null && vis != null)
    {
      visPlugin.setSegmentationLayer(vis, segmentationName, markedAndCovered);
    }
  }

  private SaltProject getDocument(String toplevelCorpusName, String documentName,
    List<String> nodeAnnoFilter)
  {
    SaltProject txt = null;
    try
    {
      toplevelCorpusName = URLEncoder.encode(toplevelCorpusName, "UTF-8");
      documentName = URLEncoder.encode(documentName, "UTF-8");
      WebResource res = Helper.getAnnisWebResource().path("query").path("graph").path(toplevelCorpusName).
        path(documentName);
      if(nodeAnnoFilter != null)
      {
        res = res.queryParam("filternodeanno", Joiner.on(",").join(nodeAnnoFilter));
      }
      txt = res.get(SaltProject.class);
    }
    catch (ClientHandlerException | UniformInterfaceException | UnsupportedEncodingException e)
    {
      log.error("General remote service exception", e);
    }
    return txt;
  }

  @Override
  public void buttonClick(ClickEvent event)
  {

    boolean isVisible = !visualizerIsVisible();

    // register new state by the parent SingleResultPanel, so the state will be
    // still available, after a reload
    visCtxChanger.registerVisibilityStatus(entry.getId(), isVisible);

    // start the toogle process.
    toggleVisualizer(isVisible, null);
  }

  @Override
  public boolean visualizerIsVisible()
  {
    if (vis == null || !vis.isVisible())
    {
      return false;
    }
    return true;
  }

  private void loadVisualizer(final LoadableVisualizer.Callback callback)
  {
    if (visPlugin != null)
    {
      btEntry.setIcon(ICON_COLLAPSE);
      progress.setIndeterminate(true);
      progress.setVisible(true);
      progress.setEnabled(true);
      progress.setDescription("Loading visualizer" + visPlugin.getShortName());
      
      ExecutorService execService = Executors.newSingleThreadExecutor();

      final Future<Component> future = execService.submit(
        new LoadComponentTask());
      
      // run the actual code to load the visualizer
      PollControl.runInBackground(500, 150, null,
        new BackgroundJob(future, callback, UI.getCurrent()));

    } // end if create input was needed

  } // end loadVisualizer

  private void updateGUIAfterLoadingVisualizer(
    LoadableVisualizer.Callback callback)
  {
    if (callback != null && vis instanceof LoadableVisualizer)
    {
      LoadableVisualizer loadableVis = (LoadableVisualizer) vis;
      if (loadableVis.isLoaded())
      {
        // direct call callback since the visualizer is already ready
        callback.visualizerLoaded(loadableVis);
      }
      else
      {
        loadableVis.clearCallbacks();
        // add listener when player was fully loaded
        loadableVis.addOnLoadCallBack(callback);
      }
    }

    progress.setEnabled(false);
    progress.setVisible(false);

    if (vis != null)
    {
      btEntry.setEnabled(true);
      vis.setVisible(true);
      if (vis instanceof PDFViewer)
      {
        ((PDFViewer) vis).openPDFPage("-1");
      }
      if (vis instanceof MediaPlayer)
      {
        // if this is a media player visualizer, close all other media players
        // since some browsers (e.g. Chrome) have problems if there are multiple
        // audio/video elements on one page
        MediaController mediaController = VaadinSession.getCurrent().
          getAttribute(
            MediaController.class);
        mediaController.closeOtherPlayers((MediaPlayer) vis);

      }
      // add if not already added
      if (getComponentIndex(vis) < 0)
      {
        addComponent(vis);
      }
    }
  }

  @Override
  public void toggleVisualizer(boolean visible,
    LoadableVisualizer.Callback callback)
  {
    if (visible)
    {
      loadVisualizer(callback);
    }
    else
    {
      // hide
      btEntry.setEnabled(true);

      if (vis != null)
      {
        vis.setVisible(false);
        if (vis instanceof MediaPlayer)
        {
          removeComponent(vis);
        }

      }

      btEntry.setIcon(ICON_EXPAND);

    }
  }

  public String getHtmlID()
  {
    return htmlID;
  }

  /**
   * Since there is a bug in the annis-service some ANNIS Features are not set
   * when the whole document is requested, we have to copy it manually from the
   * old nodes
   *
   * @param source orignal node
   * @param target node which is missing the annis feature
   * @param featureNameSpace namespace of the feature
   * @param featureName name of the feature
   * @param copyIfExists If true the feature is copied even if it already exists
   * on target node.
   */
  private void copyAnnisFeature(SNode source, SNode target,
    String featureNameSpace, String featureName, boolean copyIfExists)
  {
    SFeature sfeature;

    if ((sfeature = source.getSFeature(featureNameSpace, featureName)) != null)
    {
      if (target.getSFeature(featureNameSpace, featureName) == null)
      {
        target.createSFeature(sfeature.getNamespace(), sfeature.getName(),
          sfeature.getSValueSTEXT());
        log.debug("copy SFeature {} value {}", sfeature.getQName(), sfeature.
          getValueString());
      }
      else if (copyIfExists)
      {
        SFeature targetFeature = target.getSFeature(featureNameSpace,
          featureName);
        targetFeature.setValue(sfeature.getValue());

        log.debug("overwriting SFeature {} value {}", sfeature.getQName(),
          sfeature.
          getValueString());
      }
    }
  }

  private class BackgroundJob implements Runnable
  {

    private final Future<Component> future;
    private final LoadableVisualizer.Callback callback;
    private final UI ui;

    public BackgroundJob(
      Future<Component> future, LoadableVisualizer.Callback callback, UI ui)
    {
      this.future = future;
      this.callback = callback;
      this.ui = ui;
    }

    @Override
    public void run()
    {

      Throwable exception = null;
      try
      {
        final Component result = future.get(60, TimeUnit.SECONDS);

        ui.accessSynchronously(new Runnable()
        {
          @Override
          public void run()
          {
            vis = result;
            updateGUIAfterLoadingVisualizer(callback);
          }
        });
      }
      catch (InterruptedException ex)
      {
        log.error(null, ex);
        exception = ex;
      }
      catch (ExecutionException ex)
      {
        log.error(null, ex);
        exception = ex;
      }
      catch (TimeoutException ex)
      {
        future.cancel(true);
        log.error(
          "Could create visualizer " + visPlugin.getShortName()
          + " in 60 seconds: Timeout",
          ex);
        exception = ex;
      }

      if (exception != null)
      {
        final Throwable finalException = exception;
        ui.accessSynchronously(new Runnable()
        {
          @Override
          public void run()
          {
            Notification.show(
              "Error when creating visualizer " + visPlugin.getShortName(),
              finalException.toString(),
              Notification.Type.WARNING_MESSAGE);
          }
        });
      }

    }
  }

  public class LoadComponentTask implements Callable<Component>
  {

    @Override
    public Component call() throws Exception
    {
      // only create component if not already created
      if (vis == null)
      {
        return createComponent();
      }
      else
      {
        return vis;
      }
    }
  }

  public static class ByteArrayOutputStreamSource implements
    StreamResource.StreamSource
  {

    private static final Logger log = LoggerFactory.
      getLogger(ByteArrayOutputStreamSource.class);

    private transient ByteArrayOutputStream byteStream;

    public ByteArrayOutputStreamSource(ByteArrayOutputStream byteStream)
    {
      this.byteStream = byteStream;
    }

    @Override
    public InputStream getStream()
    {
      if (byteStream == null)
      {
        log.error("byte stream was null");
        return null;
      }
      return new ByteArrayInputStream(byteStream.toByteArray());
    }
  }

  public String getVisualizerShortName()
  {
    if (visPlugin != null)
    {
      return visPlugin.getShortName();
    }

    else
    {
      return null;
    }
  }

  protected SDocument getResult()
  {
    return result;
  }
  
  
}
