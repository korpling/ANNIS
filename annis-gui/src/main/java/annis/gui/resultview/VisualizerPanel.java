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

import annis.service.AnnisService;
import annis.service.ifaces.AnnisResult;
import com.vaadin.addon.chameleon.ChameleonTheme;
import com.vaadin.terminal.ApplicationResource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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

  public static final ThemeResource ICON_COLLAPSE = new ThemeResource("icon-collapse.gif");
  public static final ThemeResource ICON_EXPAND = new ThemeResource("icon-expand.gif");
  private ApplicationResource resource = null;
  private AutoHeightIFrame iframe;
  private AnnisResult result;
  private PluginSystem ps;
  private ResolverEntry entry;
  private Random rand = new Random();
  private Map<String, String> markersExact;
  private Map<String, String> markersCovered;
  private Button btEntry;

  public VisualizerPanel(final ResolverEntry entry, AnnisResult result,
    PluginSystem ps, Map<String, String> markersExact, Map<String, String> markersCovered)
  {
    this.result = result;
    this.ps = ps;
    this.entry = entry;
    this.markersExact = markersExact;
    this.markersCovered = markersCovered;
    
    this.setWidth("100%");
    this.setHeight("-1px");
    
    addStyleName(ChameleonTheme.PANEL_BORDERLESS);
    
    VerticalLayout layout = (VerticalLayout) getContent();
    layout.setMargin(false);
    layout.setSpacing(false);
    layout.setWidth("100%");
    layout.setHeight("-1px");
    
    btEntry = new Button(entry.getDisplayName());
    btEntry.setIcon(ICON_EXPAND);
    btEntry.setStyleName(ChameleonTheme.BUTTON_BORDERLESS + " " + ChameleonTheme.BUTTON_SMALL);
    btEntry.addListener((Button.ClickListener) this);
    addComponent(btEntry);
  }
  

  private VisualizerInput createInput()
  {
    VisualizerInput input = new VisualizerInput();
    input.setAnnisRemoteServiceURL(getApplication().getProperty("AnnisRemoteService.URL"));
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

    return input;
  }

  private ApplicationResource createResource(final ByteArrayOutputStream byteStream, 
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

  private AnnisResult getText(long textId)
  {
    AnnisResult text = null;
    try
    {
      AnnisService service = Helper.getService(getApplication(), getWindow());
      text = service.getAnnisResult(textId);
    }
    catch(Exception e)
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

    if(resource != null)
    {
      getApplication().removeResource(resource);
    }
  }

  @Override
  public void buttonClick(ClickEvent event)
  {

    if(resource != null)
    {
      getApplication().removeResource(resource);
    }
      
    if(btEntry.getIcon() == ICON_EXPAND)
    {
      // expand
      if(iframe == null)
      {
        
        VisualizerPlugin vis = ps.getVisualizer(entry.getVisType());
        if(vis == null)
        {
          entry.setVisType(PluginSystem.DEFAULT_VISUALIZER);
          vis = ps.getVisualizer(entry.getVisType());
        }
        VisualizerInput input = createInput();
        if(vis.isUsingText() && result.getGraph().getNodes().size() > 0)
        {
          input.setResult(getText(result.getGraph().getNodes().get(0).getTextId()));
        }
        else
        {
          input.setResult(result);
        }

        
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
          vis.writeOutput(input, outStream);
       
        resource = createResource(outStream, vis.getContentType());
        String url = getApplication().getRelativeLocation(resource);
        iframe = new AutoHeightIFrame(url == null ? "/error.html" : url);

        addComponent(iframe);
      }

      btEntry.setIcon(ICON_COLLAPSE);
      iframe.setVisible(true);
    }
    else if(btEntry.getIcon() == ICON_COLLAPSE)
    {
      // collapse
      if(iframe != null)
      {
        iframe.setVisible(false);
      }
      btEntry.setIcon(ICON_EXPAND);
    }
  }
}
