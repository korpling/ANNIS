/*
 * Copyright 2015 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.SelectionEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import annis.gui.objects.PagedResultQuery;
import annis.libgui.Helper;
import annis.libgui.PluginSystem;
import annis.libgui.visualizers.FilteringVisualizerPlugin;
import annis.libgui.visualizers.VisualizerPlugin;
import annis.resolver.ResolverEntry;
import annis.service.objects.Match;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ShareSingleMatchGenerator extends Window implements
  SelectionEvent.SelectionListener
{
  private final static Logger log = LoggerFactory.getLogger(ShareSingleMatchGenerator.class);
  
  private final VerticalLayout layout;
  private final Grid visSelector;
  private final VerticalLayout generatedLinks;
  
  private final Property<String> directURL;
  private final Property<String> iframeCode;
  private final BrowserFrame preview;
  
  private final  TextArea txtDirectURL;
  private final TextArea txtIFrameCode;
  
  private final BeanItemContainer<ResolverEntry> visContainer;
  
  private final Match match;
  private final PagedResultQuery query;
  private final String segmentation;
  private final PluginSystem ps;
  
  public ShareSingleMatchGenerator(List<ResolverEntry> visualizers, 
    Match match,
    PagedResultQuery query,
    String segmentation,
    PluginSystem ps)
  { 
    this.match = match;
    this.query = query;
    this.segmentation = segmentation;
    this.ps = ps;
    
    setResizeLazy(true);
    
    directURL = new ObjectProperty<>("");
    iframeCode = new ObjectProperty<>("");
    
    visContainer = new BeanItemContainer<>(ResolverEntry.class);
    visContainer.addAll(visualizers);
    
    txtDirectURL = new TextArea(directURL);
    txtDirectURL.setCaption("Link for publications");
    txtDirectURL.setWidth("100%");
    txtDirectURL.setHeight("-1px");
    txtDirectURL.addStyleName(ValoTheme.TEXTFIELD_LARGE);
    txtDirectURL.addStyleName("shared-text");
    txtDirectURL.setWordwrap(true);
    txtDirectURL.setReadOnly(true);
    
    txtIFrameCode = new TextArea(iframeCode);
    txtIFrameCode.setCaption("Code for embedding visualization into web page");
    txtIFrameCode.setWidth("100%");
    txtIFrameCode.setHeight("-1px");
    txtIFrameCode.addStyleName(ValoTheme.TEXTFIELD_LARGE);
    txtIFrameCode.addStyleName("shared-text");
    txtIFrameCode.setWordwrap(true);
    txtIFrameCode.setReadOnly(true);
    
    preview = new BrowserFrame();
    preview.setCaption("Preview");
    preview.addStyleName("shared-text");
    preview.setSizeFull();
    
    generatedLinks = new VerticalLayout(txtDirectURL, txtIFrameCode, preview);
    generatedLinks.setComponentAlignment(txtDirectURL, Alignment.TOP_LEFT);
    generatedLinks.setComponentAlignment(txtIFrameCode, Alignment.TOP_LEFT);
    generatedLinks.setExpandRatio(preview, 1.0f);
    
    visSelector = new Grid(visContainer);
    visSelector.setCaption("Select visualization");
    visSelector.setHeight("100%");
    visSelector.setColumns("displayName");
    visSelector.setSelectionMode(Grid.SelectionMode.SINGLE);
    visSelector.addSelectionListener(ShareSingleMatchGenerator.this);
    visSelector.select(visContainer.getIdByIndex(0));
    visSelector.setWidth("300px");
    visSelector.getColumn("displayName").setSortable(false);
    
    generatedLinks.setSizeFull();
    
    Label infoText = new Label(
        "<p style=\"font-size: 18px\" >"
        + "<strong>Share your match:</strong>&nbsp;"
        + "1.&nbsp;Choose the visualization to share. 2.&nbsp;Copy the generated link or code. "
        + "3.&nbsp;Share this link with your peers or include the code in your website. "
        + "</p>",
      ContentMode.HTML);
    
    
    HorizontalLayout hLayout = new HorizontalLayout(visSelector, generatedLinks);
    hLayout.setSizeFull();
    hLayout.setSpacing(true);
    hLayout.setExpandRatio(generatedLinks, 1.0f);
    
    Button btClose = new Button("Close");
    btClose.setSizeUndefined();
    btClose.addClickListener(new Button.ClickListener()
    {

      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        getUI().removeWindow(ShareSingleMatchGenerator.this);
      }
    });
    
    layout = new VerticalLayout(infoText, hLayout, btClose);
    layout.setSizeFull();
    layout.setExpandRatio(hLayout, 1.0f);
    layout.setComponentAlignment(btClose, Alignment.MIDDLE_CENTER);
    
    setContent(layout);
  }
  
  
  private URI generatorURLForVisualizer(ResolverEntry entry)
  {
    String appContext = Helper.getContext();
    URI appURI = UI.getCurrent().getPage().getLocation();
    UriBuilder result = UriBuilder.fromUri(appURI)
      .replacePath(appContext)
      .path("embeddedvis")
      .path(Helper.encodeJersey(entry.getVisType()))
      .fragment("");
    if(entry.getNamespace() != null)
    {
      result = result.queryParam("embedded_ns", 
        Helper.encodeJersey(entry.getNamespace()));
    }
    // test if the request was made from a sub-instance
    String nonContextPath = appURI.getPath().substring(appContext.length());
    if(!nonContextPath.isEmpty())
    {
      if(nonContextPath.startsWith("/"))
      {
        nonContextPath = nonContextPath.substring(1);
      }
      result = result.queryParam(EmbeddedVisUI.KEY_INSTANCE, nonContextPath);
    }
    
    UriBuilder serviceURL =
      UriBuilder.fromUri(Helper.getAnnisWebResource().path(
      "query").getURI());
    
    VisualizerPlugin visPlugin = ps.getVisualizer(entry.getVisType());
    if(visPlugin != null && visPlugin.isUsingText())
    {
      // generate a service URL that gets the whole document
      URI firstID = match.getSaltIDs().get(0);
      String pathAsString = firstID.getRawPath();
      List<String> path = Splitter.on('/').omitEmptyStrings().trimResults().splitToList(pathAsString);
      String corpusName = path.get(0);
      String documentName = path.get(path.size()-1);
      try
      {
        corpusName = URLDecoder.decode(corpusName, "UTF-8");
        documentName =URLDecoder.decode(documentName, "UTF-8"); ;
      }
      catch(UnsupportedEncodingException ex)
      {
        log.warn("Could not decode URL", ex);
      }
      
      // apply any node annotation filters if possible
      if(visPlugin instanceof FilteringVisualizerPlugin)
      {
        List<String> visAnnos = ((FilteringVisualizerPlugin) visPlugin).getFilteredNodeAnnotationNames(
          corpusName, documentName, entry.getMappings());
        if(visAnnos != null)
        {
          Set<String> annos = new HashSet<>(visAnnos);
          // always add the matched node annotation as well
          for (String matchedAnno : match.getAnnos())
          {
            if(!matchedAnno.isEmpty())
            {
              annos.add(matchedAnno);
            }
          }
          serviceURL = serviceURL.queryParam("filternodeanno", Joiner.on(",").
            join(annos));
        }
      }
      
      serviceURL = serviceURL.path("graph")
        .path(Helper.encodePath(corpusName))
        .path(Helper.encodePath(documentName));
      
      // add the original match so the embedded visualizer can add it
      // (since we use the graph query it will not be included in the Salt XMI itself)
      result = result
        .queryParam(EmbeddedVisUI.KEY_MATCH, Helper.encodeJersey(match.toString()));

    }
    else
    {
      // default to the subgraph URL for this specific match
      serviceURL = serviceURL.path("search").path("subgraph")
        .queryParam("match", Helper.encodeJersey(match.toString()))
        .queryParam("left", query.getLeftContext())
        .queryParam("right", query.getRightContext());
      
      if(query.getSegmentation() != null)
      {
        serviceURL = serviceURL.queryParam("segmentation", query.getSegmentation());
      }
      
    }
    // add the URL where to fetch the graph from
    result = result.queryParam(EmbeddedVisUI.KEY_SALT, 
      Helper.encodeQueryParam(serviceURL.build().toASCIIString()));
    
    // add the current view as "return back" parameter
    result = result.queryParam(EmbeddedVisUI.KEY_SEARCH_INTERFACE,
      appURI.toASCIIString());
    
    if (segmentation != null)
    {
      result = result.queryParam(EmbeddedVisUI.KEY_BASE_TEXT, segmentation);
    }
    
    // add all mappings as parameter
    for(String key : entry.getMappings().stringPropertyNames())
    {
      if(!key.startsWith(EmbeddedVisUI.KEY_PREFIX))
      {
        String value = Helper.encodeJersey(entry.getMappings().getProperty(key));
        result = result.queryParam(key, value);
      }
    }
    
    return result.build();
  }


  @Override
  public void select(SelectionEvent event)
  {
    Set<Object> selected = event.getSelected();
    if(selected.isEmpty())
    {
      generatedLinks.setVisible(false);
    }
    else
    {
      generatedLinks.setVisible(true);
      
      URI url = generatorURLForVisualizer((ResolverEntry) selected.iterator().next());
      String shortURL = Helper.shortenURL(url);
      directURL.setValue(shortURL);
      iframeCode.setValue("<iframe height=\"300px\" width=\"100%\" src=\"" + shortURL + "\"></iframe>");
      preview.setSource(new ExternalResource(shortURL));
    }
  }
  
  
}
