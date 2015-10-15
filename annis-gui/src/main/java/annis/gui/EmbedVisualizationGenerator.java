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

import annis.gui.objects.PagedResultQuery;
import annis.libgui.Helper;
import annis.libgui.PluginSystem;
import annis.libgui.visualizers.FilteringVisualizerPlugin;
import annis.libgui.visualizers.VisualizerPlugin;
import annis.resolver.ResolverEntry;
import annis.service.objects.Match;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class EmbedVisualizationGenerator extends Panel implements Property.ValueChangeListener
{
  private final HorizontalLayout layout;
  private final ListSelect visSelector;
  private final VerticalLayout generatedLinks;
  
  private final Property<String> directURL;
  private final Property<String> iframeCode;
  private final BrowserFrame preview;
  
  private final BeanItemContainer<ResolverEntry> visContainer;
  
  private final Match match;
  private final PagedResultQuery query;
  private final String segmentation;
  private final PluginSystem ps;
  
  public EmbedVisualizationGenerator(List<ResolverEntry> visualizers, 
    Match match,
    PagedResultQuery query,
    String segmentation,
    PluginSystem ps)
  { 
    this.match = match;
    this.query = query;
    this.segmentation = segmentation;
    this.ps = ps;
    
    directURL = new ObjectProperty<>("");
    iframeCode = new ObjectProperty<>("");
    
    visContainer = new BeanItemContainer<>(ResolverEntry.class);
    visContainer.addAll(visualizers);
    
    TextArea txtDirectURL = new TextArea(directURL);
    txtDirectURL.setCaption("Link for publications");
    txtDirectURL.setWidth("100%");
    txtDirectURL.setHeight("-1px");
    txtDirectURL.addStyleName(ValoTheme.TEXTFIELD_LARGE);
    txtDirectURL.addStyleName("citation");
    txtDirectURL.setWordwrap(true);
    txtDirectURL.setReadOnly(true);
    
    TextArea txtIFrameCode = new TextArea(iframeCode);
    txtIFrameCode.setCaption("Code for embedding visualization into web page");
    txtIFrameCode.setWidth("100%");
    txtIFrameCode.setHeight("-1px");
    txtIFrameCode.addStyleName(ValoTheme.TEXTFIELD_LARGE);
    txtIFrameCode.addStyleName("citation");
    txtIFrameCode.setWordwrap(true);
    txtIFrameCode.setReadOnly(true);
    
    preview = new BrowserFrame();
    preview.setCaption("Preview");
    preview.addStyleName("citation");
    preview.setSizeFull();
    
    visSelector = new ListSelect("Select visualization");
    visSelector.setHeight("100%");
    visSelector.setContainerDataSource(visContainer);
    visSelector.setItemCaptionPropertyId("displayName");
    visSelector.setNullSelectionAllowed(false);
    visSelector.addValueChangeListener(this);
    visSelector.setValue(visContainer.getIdByIndex(0));
    
    generatedLinks = new VerticalLayout(txtDirectURL, txtIFrameCode, preview);
    generatedLinks.setComponentAlignment(txtDirectURL, Alignment.TOP_LEFT);
    generatedLinks.setComponentAlignment(txtIFrameCode, Alignment.TOP_LEFT);
    generatedLinks.setExpandRatio(preview, 1.0f);
    
    generatedLinks.setSizeFull();
    
    layout = new HorizontalLayout(visSelector, generatedLinks);
    layout.setSizeFull();
    layout.setSpacing(true);
    layout.setExpandRatio(generatedLinks, 1.0f);
    
    setContent(layout);
    setSizeFull();
  }
  
  private String generatorURLForVisualizer(ResolverEntry entry)
  {
    URI appURI = UI.getCurrent().getPage().getLocation();
    UriBuilder result = UriBuilder.fromUri(appURI)
      .path("embeddedvis")
      .path(Helper.encodeJersey(entry.getVisType()))
      .fragment("");
    if(entry.getNamespace() != null)
    {
      result = result.queryParam("embedded_ns", 
        Helper.encodeJersey(entry.getNamespace()));
    }
    
    UriBuilder serviceURL =
      UriBuilder.fromUri(Helper.getAnnisWebResource().path(
      "query").getURI());
    
    VisualizerPlugin visPlugin = ps.getVisualizer(entry.getVisType());
    if(visPlugin != null && visPlugin.isUsingText())
    {
      // generate a service URL that gets the whole document
      URI firstID = match.getSaltIDs().get(0);
      String pathAsString = firstID.getPath();
      List<String> path = Splitter.on('/').omitEmptyStrings().trimResults().splitToList(pathAsString);
      String corpusName = Helper.encodeJersey(path.get(0));
      String documentName = Helper.encodeJersey(path.get(path.size()-1));
      
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
        .path(corpusName)
        .path(documentName);
      
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
        String value = Helper.encodeQueryParam(entry.getMappings().getProperty(key));
        result = result.queryParam(key, value);
      }
    }
    
    return result.build().toASCIIString();
  }

  @Override
  public void valueChange(Property.ValueChangeEvent event)
  {
    String url = generatorURLForVisualizer((ResolverEntry) event.getProperty().getValue());
    directURL.setValue(url);
    iframeCode.setValue("<iframe width=\"100%\" height=\"300px\" src=\"" + url + "\"></iframe>");
    preview.setSource(new ExternalResource(url));
  }
}
