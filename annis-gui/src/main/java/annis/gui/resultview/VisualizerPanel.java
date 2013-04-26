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

import annis.libgui.Helper;
import annis.libgui.InstanceConfig;
import annis.libgui.PluginSystem;
import annis.libgui.VisualizationToggle;
import annis.libgui.media.MediaPlayer;
import annis.libgui.media.PDFViewer;
import annis.libgui.visualizers.VisualizerInput;
import annis.libgui.visualizers.VisualizerPlugin;
import annis.resolver.ResolverEntry;
import annis.visualizers.LoadableVisualizer;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.themes.ChameleonTheme;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import static annis.model.AnnisConstants.*;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SFeature;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SGraph;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controls the visibility of visualizer plugins and provides some control
 * methods for the media visualizers.
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 *
 */
public class VisualizerPanel extends VerticalLayout
  implements Button.ClickListener, VisualizationToggle
{

  private final Logger log = LoggerFactory.getLogger(VisualizerPanel.class);

  public static final ThemeResource ICON_COLLAPSE = new ThemeResource(
    "icon-collapse.gif");

  public static final ThemeResource ICON_EXPAND = new ThemeResource(
    "icon-expand.gif");

  private Component vis;

  private transient SDocument result;

  private transient PluginSystem ps;

  private ResolverEntry entry;

  private Random rand = new Random();

  private transient Map<SNode, Long> markedAndCovered;

  private transient List<SToken> token;

  private Map<String, String> markersExact;

  private Map<String, String> markersCovered;

  private Button btEntry;

  private String htmlID;

  private String resultID;

  private transient VisualizerPlugin visPlugin;

  private Set<String> visibleTokenAnnos;

  private String segmentationName;

  private final String PERMANENT = "permanent";

  private final String ISVISIBLE = "visible";

  private final String HIDDEN = "hidden";

  private final String PRELOADED = "preloaded";

  private ProgressIndicator progress;

  private InstanceConfig instanceConfig;

  /**
   * This Constructor should be used for {@link ComponentVisualizerPlugin}
   * Visualizer.
   *
   */
  public VisualizerPanel(
    final ResolverEntry entry,
    SDocument result,
    List<SToken> token,
    Set<String> visibleTokenAnnos,
    Map<SNode, Long> markedAndCovered,
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


    this.result = result;
    this.token = token;
    this.visibleTokenAnnos = visibleTokenAnnos;
    this.markedAndCovered = markedAndCovered;
    this.segmentationName = segmentationName;
    this.htmlID = htmlID;
    this.resultID = resultID;

    this.progress = new ProgressIndicator();

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


        if (PRELOADED.equalsIgnoreCase(entry.getVisibility()))
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

    return c;
  }

  private VisualizerInput createInput()
  {
    VisualizerInput input = new VisualizerInput();
    input.setAnnisWebServiceURL((String) VaadinSession.getCurrent().
      getAttribute(
      "AnnisWebService.URL"));
    input.setContextPath(Helper.getContext());
    input.
      setDotPath((String) VaadinSession.getCurrent().getAttribute("DotPath"));

    input.setId(resultID);

    input.setMarkableExactMap(markersExact);
    input.setMarkableMap(markersCovered);
    input.setMarkedAndCovered(markedAndCovered);

    input.setResult(result);
    input.setToken(token);
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
      SaltProject p = getDocument(result.getSCorpusGraph().getSRootCorpus().
        get(0).getSName(), result.getSName());

      SDocument wholeDocument = p.getSCorpusGraphs().get(0).
        getSDocuments().get(0);

      input.setMarkedAndCovered(rebuildMarkedAndConvered(markedAndCovered,
        input.
        getDocument(), wholeDocument));
      input.setDocument(wholeDocument);
    }
    else
    {
      input.setDocument(result);
    }

    return input;
  }

  public void setVisibleTokenAnnosVisible(Set<String> annos)
  {
    this.visibleTokenAnnos = annos;
    if (visPlugin != null && vis != null)
    {
      visPlugin.setVisibleTokenAnnosVisible(vis, annos);
    }
  }

  public void setSegmentationLayer(String segmentationName,
    Map<SNode, Long> markedAndCovered)
  {
    this.segmentationName = segmentationName;
    this.markedAndCovered = markedAndCovered;

    if (visPlugin != null && vis != null)
    {
      visPlugin.setSegmentationLayer(vis, segmentationName, markedAndCovered);
    }
  }

  private SaltProject getDocument(String toplevelCorpusName, String documentName)
  {
    SaltProject txt = null;
    try
    {
      toplevelCorpusName = URLEncoder.encode(toplevelCorpusName, "UTF-8");
      documentName = URLEncoder.encode(documentName, "UTF-8");
      WebResource annisResource = Helper.getAnnisWebResource();
      txt = annisResource.path("query").path("graphs").path(toplevelCorpusName).
        path(
        documentName).get(SaltProject.class);
    }
    catch (RuntimeException e)
    {
      log.error("General remote service exception", e);
    }
    catch (Exception e)
    {
      log.error("General remote service exception", e);
    }
    return txt;
  }

  @Override
  public void buttonClick(ClickEvent event)
  {
    toggleVisualizer(!visualizerIsVisible(), null);
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
      Executor exec = Executors.newSingleThreadExecutor();
      FutureTask<Component> future = new FutureTask<Component>(
        new LoadComponentTask())
      {
        @Override
        public void run()
        {
          VaadinSession session = VaadinSession.getCurrent();
          try
          {
            super.run();
            // wait maximum 60 seconds
            vis = get(60, TimeUnit.SECONDS);
            session.lock();
            try
            {
              if (callback != null && vis instanceof LoadableVisualizer)
              {
                LoadableVisualizer loadableVis = (LoadableVisualizer) vis;
                if (loadableVis.isLoaded())
                {
                  // direct call callback since the visualizer is already ready
                  if (vis instanceof LoadableVisualizer)
                  {
                    callback.visualizerLoaded((LoadableVisualizer) vis);
                  }
                }
                else
                {
                  loadableVis.clearCallbacks();
                  // add listener when player was fully loaded
                  loadableVis.addOnLoadCallBack(callback);
                }
              }

              if (getComponentIndex(progress) > -1)
              {
                removeComponent(progress);
              }

              if (vis != null)
              {
                btEntry.setEnabled(true);
                vis.setVisible(true);
                addComponent(vis);
              }
            }
            finally
            {
              session.unlock();
            }
          }
          catch (InterruptedException ex)
          {
            log.error("Visualizer creation interrupted " + visPlugin.
              getShortName(), ex);
          }
          catch (ExecutionException ex)
          {
            log.error("Exception when creating visualizer " + visPlugin.
              getShortName(), ex);
          }
          catch (TimeoutException ex)
          {
            log.
              error(
              "Could create visualizer " + visPlugin.getShortName() + " in 60 seconds: Timeout",
              ex);
            session.lock();
            try
            {
              Notification.show(
                "Could not create visualizer " + visPlugin.getShortName(),
                ex.toString(),
                Notification.Type.WARNING_MESSAGE);
            }
            finally
            {
              session.unlock();
            }
            cancel(true);
          }
        }
      };
      exec.execute(future);

      btEntry.setIcon(ICON_COLLAPSE);
      progress.setIndeterminate(true);
      progress.setVisible(true);
      progress.setEnabled(true);
      progress.setPollingInterval(250);
      progress.setDescription("Loading visualizer" + visPlugin.getShortName());
      addComponent(progress);
    }
    // end if create input was needed

  } // end loadVisualizer

  @Override
  public void toggleVisualizer(boolean visible,
    LoadableVisualizer.Callback callback)
  {
    if (visible)
    {
      if (vis != null && vis instanceof PDFViewer)
      {
        vis.setVisible(true);
        //TODO this is dangerous, because openPDF ist calling toggleVisualizer again
        ((PDFViewer) vis).openPDF("-1");
        btEntry.setIcon(ICON_COLLAPSE);
      }
      else
      {
        loadVisualizer(callback);
      }
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
   * Rebuild the map of marked and covered matches with new object references.
   * If a visualizer uses the whole document, the {@link VisualizerInput} gets a
   * new result object, with new SNode objects, so we have to update these
   * references.
   *
   * @param markedAndCovered the original map calculated with the partial
   * document graph
   * @param document the partial document or subgraph
   * @param wholeDocucment the new complete document
   * @return a new map, with updated object/node references. The salt ids of the
   * node objects remains the same.
   */
  private Map<SNode, Long> rebuildMarkedAndConvered(
    Map<SNode, Long> markedAndCovered,
    SDocument document, SDocument wholeDocument)
  {
    Map<SNode, Long> newMarkedAndCovered = new HashMap<SNode, Long>();
    SGraph wholeSGraph = wholeDocument.getSDocumentGraph();
    SNode wholeNode;

    for (Entry<SNode, Long> e : markedAndCovered.entrySet())
    {
      wholeNode = wholeSGraph.getSNode(e.getKey().getSId());
      newMarkedAndCovered.put(wholeNode, e.getValue());

      // copy the annis features, which are not set by the annis service
      copyAnnisFeature(e.getKey(), wholeNode, ANNIS_NS, FEAT_MATCHEDNODE, false);
    }

    // copy the annis features, which are not set by the annis service
    copyAnnisFeature(document, wholeDocument, ANNIS_NS, FEAT_MATCHEDIDS, true);
    return newMarkedAndCovered;
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
}
