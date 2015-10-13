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
import com.sun.jersey.api.client.WebResource;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class EmbedVisualizationGenerator extends Panel implements Property.ValueChangeListener
{
  private final HorizontalLayout layout;
  private final ListSelect visSelector;
  private final Accordion generatorSelector;
  
  private final TextArea txtURL;
  private final Property<String> urlProperty;
  
  private final BeanItemContainer<ResolverEntry> visContainer;
  
  private final Match match;
  private final PagedResultQuery query;
  private final PluginSystem ps;
  
  public EmbedVisualizationGenerator(List<ResolverEntry> visualizers, 
    Match match,
    PagedResultQuery query,
    PluginSystem ps)
  { 
    this.match = match;
    this.query = query;
    this.ps = ps;
    
    urlProperty = new ObjectProperty<>("");
    
    visContainer = new BeanItemContainer<>(ResolverEntry.class);
    visContainer.addAll(visualizers);
    
    txtURL = new TextArea(urlProperty);
    txtURL.setSizeFull();
    txtURL.addStyleName(ValoTheme.TEXTFIELD_LARGE);
    txtURL.addStyleName("citation");
    txtURL.setWordwrap(true);
    txtURL.setReadOnly(true);
    
    visSelector = new ListSelect("Select visualization");
    visSelector.setHeight("100%");
    visSelector.setContainerDataSource(visContainer);
    visSelector.setItemCaptionPropertyId("displayName");
    visSelector.setNullSelectionAllowed(false);
    visSelector.addValueChangeListener(this);
    
    generatorSelector = new Accordion();
    generatorSelector.addTab(txtURL, "Link");
    generatorSelector.addTab(new Label("Test"), "Webpage");
    generatorSelector.addTab(new Label("Test"), "Preview");
    
    generatorSelector.setSizeFull();
    
    layout = new HorizontalLayout(visSelector, generatorSelector);
    layout.setSizeFull();
    layout.setSpacing(true);
    layout.setExpandRatio(generatorSelector, 1.0f);
    
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
        List<String> annos = ((FilteringVisualizerPlugin) visPlugin).getFilteredNodeAnnotationNames(
          corpusName, documentName, entry.getMappings());
        if(annos != null)
        {
          serviceURL = serviceURL.queryParam("filternodeanno", Joiner.on(",").join(annos));
        }
      }
      
      serviceURL = serviceURL.path("graph")
        .path(corpusName)
        .path(documentName);
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
      Helper.encodeJersey(serviceURL.build().toASCIIString()));
    
    // add the current view as "return back" parameter
    result = result.queryParam(EmbeddedVisUI.KEY_SEARCH_INTERFACE,
      appURI.toASCIIString());
    
    // add all mappings as parameter
    for(String key : entry.getMappings().stringPropertyNames())
    {
      if(!key.startsWith(EmbeddedVisUI.KEY_PREFIX))
      {
        String value = Helper.encodeJersey(entry.getMappings().getProperty(key));
        result = result.queryParam(key, value);
      }
    }
    
    return result.build().toASCIIString();
  }

  @Override
  public void valueChange(Property.ValueChangeEvent event)
  {
    String url = generatorURLForVisualizer((ResolverEntry) event.getProperty().getValue());
    urlProperty.setValue(url);
  }
}
