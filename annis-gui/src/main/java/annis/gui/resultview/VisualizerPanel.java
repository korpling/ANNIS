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
import annis.gui.visualizers.VisualizerInput;
import annis.gui.visualizers.VisualizerPlugin;
import annis.gui.widgets.AutoHeightIFrame;
import annis.resolver.ResolverEntry;

import com.sun.jersey.api.client.WebResource;
import com.vaadin.ui.themes.ChameleonTheme;
import com.vaadin.terminal.ApplicationResource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Panel;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author thomas
 *
 */
public class VisualizerPanel extends Panel implements Button.ClickListener
{

  public static final ThemeResource ICON_COLLAPSE = new ThemeResource(
    "icon-collapse.gif");
  public static final ThemeResource ICON_EXPAND = new ThemeResource(
    "icon-expand.gif");
  private ApplicationResource resource = null;
  private AutoHeightIFrame iframe;
  private SDocument result;
  private PluginSystem ps;
  private ResolverEntry entry;
  private Random rand = new Random();
  private Map<String, String> markersExact;
  private Map<String, String> markersCovered;
  private Button btEntry;
  private KWICPanel kwicPanel;
  private List<String> mediaIDs;
  private String htmlID;
  public CustomLayout customLayout;

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
    this.customLayout = costumLayout;
    this.mediaIDs = mediaIDs;
    this.htmlID = htmlID;

    setContent(this.customLayout);

    this.setWidth("100%");
    this.setHeight("-1px");

    addStyleName(ChameleonTheme.PANEL_BORDERLESS);

    btEntry = new Button(entry.getDisplayName());
    btEntry.setIcon(ICON_EXPAND);
    btEntry.setStyleName(ChameleonTheme.BUTTON_BORDERLESS + " "
      + ChameleonTheme.BUTTON_SMALL);
    btEntry.addListener((Button.ClickListener) this);
    customLayout.addComponent(btEntry, "btEntry");
  }

  private VisualizerInput createInput()
  {
    VisualizerInput input = new VisualizerInput();
    input.setAnnisRemoteServiceURL(getApplication().getProperty(
      "AnnisRemoteService.URL"));
    input.setContextPath(Helper.getContext(getApplication()));
    input.setDotPath(getApplication().getProperty("DotPath"));
    input.setId("" + rand.nextLong());
    input.setMappings(entry.getMappings());
    input.setMarkableExactMap(markersExact);
    input.setMarkableMap(markersCovered);
    input.setNamespace(entry.getNamespace());
    String template = Helper.getContext(getApplication())
      + "/Resource/" + entry.getVisType() + "/%s";
    input.setResourcePathTemplate(template);
    input.setMediaIDs(mediaIDs);
    return input;
  }

  private ApplicationResource createResource(
    final ByteArrayOutputStream byteStream,
    String mimeType)
  {

    StreamResource r = null;

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
      WebResource annisResource = Helper.getAnnisWebResource(getApplication());
      text = annisResource.path("graphs").path(toplevelCorpusName).path(
        documentName).get(SaltProject.class);
    }
    catch (Exception e)
    {
      Logger.getLogger(VisualizerPanel.class.getName()).log(Level.SEVERE,
        "General remote service exception", e);
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
    openVisualizer(true);
  }

  public void openVisualizer(Boolean collapse)
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
        if (vis.isUsingText() && result.getSDocumentGraph().getSNodes().size()
          > 0)
        {

          SaltProject p = getText(result.getSCorpusGraph().getSRootCorpus().
            get(0).getSName(), result.getSName());

          input.setDocument(p.getSCorpusGraphs().get(0).getSDocuments().get(0));

        }
        else
        {

          input.setDocument(result);

        }


        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        vis.writeOutput(input, outStream);

        resource = createResource(outStream, vis.getContentType());
        String url = getApplication().getRelativeLocation(resource);
        iframe = new AutoHeightIFrame(url == null ? "/error.html" : url, this);


        customLayout.addComponent(iframe, "iframe");
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
