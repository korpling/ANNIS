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

import annis.gui.Helper;
import annis.gui.PluginSystem;
import annis.gui.visualizers.IFrameVisualizer;
import annis.gui.visualizers.VisualizerInput;
import annis.gui.visualizers.VisualizerPlugin;
import annis.gui.visualizers.component.KWICPanel;
import annis.gui.widgets.AutoHeightIFrame;
import annis.resolver.ResolverEntry;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.terminal.ApplicationResource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.ChameleonTheme;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author thomas, Benjamin Weißenfels
 *
 */
public class VisualizerPanel extends Panel implements Button.ClickListener
{

  private final Logger log = LoggerFactory.getLogger(VisualizerPanel.class);
  public static final ThemeResource ICON_COLLAPSE = new ThemeResource(
    "icon-collapse.gif");
  public static final ThemeResource ICON_EXPAND = new ThemeResource(
    "icon-expand.gif");
  private ApplicationResource resource = null;
  private Component iframe;
  private SDocument result;
  private PluginSystem ps;
  private ResolverEntry entry;
  private Random rand = new Random();
  private Map<SNode, Long> markedAndCovered;
  private List<SNode> token;
  private Map<String, String> markersExact;
  private Map<String, String> markersCovered;
  private Button btEntry;
  private KWICPanel kwicPanel;
  private List<String> mediaIDs;
  private String htmlID;
  private CustomLayout visContainer;
  private VisualizerPlugin compVis;
  private Set<String> visibleTokenAnnos;
  private STextualDS text;
  private SingleResultPanel parentPanel;
  private String segmentationName;
  private List<VisualizerPanel> mediaVisualizer;

  /**
   * This Constructor should be used for {@link ComponentVisualizerPlugin}
   * Visualizer.
   *
   */
  public VisualizerPanel(String visType, SDocument result, List<SNode> token,
    Set<String> visibleTokenAnnos, Map<SNode, Long> markedAndCovered,
    STextualDS text, List<String> mediaIDs,
    List<VisualizerPanel> mediaVisualizer, String htmlID,
    SingleResultPanel parent, String segmentationName, PluginSystem ps,
    CustomLayout visContainer)
  {

    compVis = ps.getVisualizer(visType);

    this.result = result;
    this.token = token;
    this.visibleTokenAnnos = visibleTokenAnnos;
    this.markedAndCovered = markedAndCovered;
    this.text = text;
    this.parentPanel = parent;
    this.segmentationName = segmentationName;
    this.mediaVisualizer = mediaVisualizer;
    this.mediaIDs = mediaIDs;
    // TODO use this also for ComponentVisualizer, or lookup a native mediaplayer
    this.htmlID = htmlID;

    this.visContainer = visContainer;

    this.addStyleName(ChameleonTheme.PANEL_BORDERLESS);
    this.setWidth("100%");
    this.setContent(this.visContainer);
  }

  public VisualizerPanel(final ResolverEntry entry, SDocument result,
    PluginSystem ps, Map<String, String> markersExact,
    Map<String, String> markersCovered, CustomLayout costumLayout,
    List<String> mediaIDs, String htmlID)
  {
    this.result = result;
    this.ps = ps;
    this.entry = entry;
    this.markersExact = markersExact;
    this.markersCovered = markersCovered;
    this.visContainer = costumLayout;
    this.mediaIDs = mediaIDs;
    this.htmlID = htmlID;


    setContent(this.visContainer);

    this.setWidth("100%");
    this.setHeight("-1px");

    addStyleName(ChameleonTheme.PANEL_BORDERLESS);

    btEntry = new Button(entry.getDisplayName());
    btEntry.setIcon(ICON_EXPAND);
    btEntry.setStyleName(ChameleonTheme.BUTTON_BORDERLESS + " "
      + ChameleonTheme.BUTTON_SMALL);
    btEntry.addListener((Button.ClickListener) this);
    visContainer.addComponent(btEntry, "btEntry");
  }

  @Override
  public void attach()
  {
    VisualizerInput visInput;
    Component vis;

    /**
     * check if this is a ComponentVisualizer.
     */
    if (compVis == null)
    {
      return;
    }

    visInput = createInput();
    visInput.setResult(result);
    visInput.setToken(token);
    visInput.setVisibleTokenAnnos(visibleTokenAnnos);
    visInput.setText(text);
    visInput.setSingleResultPanelRef(parentPanel);
    visInput.setSegmentationName(segmentationName);
    visInput.setMediaIDs(mediaIDs);
    visInput.setMediaVisualizer(mediaVisualizer);

    vis = this.compVis.createComponent(visInput);
    visContainer.addComponent(vis, "compVis");
  }

  private VisualizerInput createInput()
  {
    VisualizerInput input = new VisualizerInput();
    input.setAnnisRemoteServiceURL(getApplication().getProperty(
      "AnnisRemoteService.URL"));
    input.setContextPath(Helper.getContext(getApplication()));
    input.setDotPath(getApplication().getProperty("DotPath"));
    input.setId("" + rand.nextLong());

    input.setMarkableExactMap(markersExact);
    input.setMarkableMap(markersCovered);
    input.setMediaIDs(mediaIDs);
    input.setMarkedAndCovered(markedAndCovered);

    if (entry != null)
    {
      input.setMappings(entry.getMappings());
      input.setNamespace(entry.getNamespace());
      String template = Helper.getContext(getApplication())
        + "/Resource/" + entry.getVisType() + "/%s";
      input.setResourcePathTemplate(template);
    }

    return input;
  }

  public ApplicationResource createResource(
    final ByteArrayOutputStream byteStream,
    String mimeType)
  {

    StreamResource r;

    r = new StreamResource(new StreamResource.StreamSource()
    {
      @Override
      public InputStream getStream()
      {
        return new ByteArrayInputStream(byteStream.toByteArray());
      }
    }, entry.getVisType() + "_" + Math.abs(rand.nextLong()), getApplication());
    r.setMIMEType(mimeType);

    return r;
  }

  private SaltProject getText(String toplevelCorpusName, String documentName)
  {
    SaltProject text = null;
    try
    {
      toplevelCorpusName = URLEncoder.encode(toplevelCorpusName, "UTF-8");
      documentName = URLEncoder.encode(documentName, "UTF-8");
      WebResource annisResource = Helper.getAnnisWebResource(getApplication());
      text = annisResource.path("graphs").path(toplevelCorpusName).path(
        documentName).get(SaltProject.class);
    }
    catch (Exception e)
    {
      log.error("General remote service exception", e);
    }
    return text;
  }

  @Override
  public void detach()
  {
    super.detach();

    if (resource != null)
    {
      getApplication().removeResource(resource);
    }
  }

  @Override
  public void buttonClick(ClickEvent event)
  {
    toggleVisualizer(true);
  }

  /**
   * Opens and closes visualizer.
   *
   * @param collapse when collapse is false, the Visualizer would never be
   * closed
   */
  public void toggleVisualizer(boolean collapse)
  {
    if (resource != null && collapse)
    {
      getApplication().removeResource(resource);
    }

    if (btEntry.getIcon() == ICON_EXPAND)
    {
      // expand
      if (iframe == null)
      {

        VisualizerPlugin vis = ps.getVisualizer(entry.getVisType());

        if (vis == null)
        {
          entry.setVisType(PluginSystem.DEFAULT_VISUALIZER);
          vis = ps.getVisualizer(entry.getVisType());
        }

        VisualizerInput input = createInput();
        IFrameVisualizer iframeVis;

        //TODO print error message
        if (vis instanceof IFrameVisualizer)
        {
          iframeVis = (IFrameVisualizer) vis;

          if (iframeVis.isUsingText()
            && result.getSDocumentGraph().getSNodes().size() > 0)
          {
            SaltProject p = getText(result.getSCorpusGraph().getSRootCorpus().
              get(0).getSName(), result.getSName());

            input.setDocument(p.getSCorpusGraphs().get(0).getSDocuments().get(0));

          }
          else
          {
            input.setDocument(result);
          }

          input.setVisPanel(this);
          iframe = iframeVis.createComponent(input);
          visContainer.addComponent(iframe, "iframe");
        }

      }

      btEntry.setIcon(ICON_COLLAPSE);
      iframe.setVisible(true);
    }
    else if (btEntry.getIcon() == ICON_COLLAPSE && collapse)
    {
      // collapse
      if (iframe != null)
      {
        iframe.setVisible(false);
        stopMediaVisualizers();
      }
      btEntry.setIcon(ICON_EXPAND);
    }
  }

  public void setKwicPanel(KWICPanel kwicPanel)
  {
    this.kwicPanel = kwicPanel;
  }

  public void startMediaVisFromKWIC()
  {
    if (kwicPanel != null)
    {
      kwicPanel.startMediaVisualizers();
      // set back to null, otherwise the movie will stop
      kwicPanel = null;
    }
  }

  private void stopMediaVisualizers()
  {
    String stopCommand = ""
      + "document.getElementById(\"" + this.htmlID + "\")"
      + ".getElementsByTagName(\"iframe\")[0].contentWindow.stop()";
    getWindow().executeJavaScript(stopCommand);

  }
}
