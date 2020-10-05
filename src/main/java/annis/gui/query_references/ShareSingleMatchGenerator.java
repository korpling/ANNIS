/*
 * Copyright 2015 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.query_references;

import annis.gui.AnnisUI;
import annis.gui.CommonUI;
import annis.gui.EmbeddedVisUI;
import annis.libgui.Helper;
import annis.libgui.visualizers.VisualizerPlugin;
import annis.model.PagedResultQuery;
import annis.service.objects.Match;
import com.google.common.base.Objects;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.data.util.ObjectProperty;
import com.vaadin.v7.event.SelectionEvent;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.Grid;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.TextArea;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.ws.rs.core.UriBuilder;
import org.corpus_tools.annis.api.model.VisualizerRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class ShareSingleMatchGenerator extends Window implements SelectionEvent.SelectionListener {
  /**
   * 
   */
  private static final long serialVersionUID = -8303684875649384972L;

  private final static Logger log = LoggerFactory.getLogger(ShareSingleMatchGenerator.class);

  private final VerticalLayout layout;
  private final Grid visSelector;
  private final VerticalLayout generatedLinks;

  private final Property<String> directURL;
  private final Property<String> iframeCode;
  private final BrowserFrame preview;

  private final TextArea txtDirectURL;
  private final TextArea txtIFrameCode;

  private final BeanItemContainer<VisualizerRule> visContainer;

  private final Match match;
  private final PagedResultQuery query;
  private final String baseText;

  private final List<VisualizerPlugin> visualizerPlugins;

  private final CommonUI ui;

  public ShareSingleMatchGenerator(CommonUI ui, List<VisualizerRule> visualizers, Match match,
      PagedResultQuery query, String baseText, List<VisualizerPlugin> visualizerPlugins) {
    this.ui = ui;
    this.match = match;
    this.query = query;
    this.baseText = baseText;
    this.visualizerPlugins = visualizerPlugins;


    setResizeLazy(true);

    directURL = new ObjectProperty<>("");
    iframeCode = new ObjectProperty<>("");

    visContainer = new BeanItemContainer<>(VisualizerRule.class);
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
    generatedLinks.setSpacing(false);

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

    Label infoText =
        new Label("<p style=\"font-size: 18px\" >" + "<strong>Share your match:</strong>&nbsp;"
            + "1.&nbsp;Choose the visualization to share. 2.&nbsp;Copy the generated link or code. "
            + "3.&nbsp;Share this link with your peers or include the code in your website. "
            + "</p>", ContentMode.HTML);

    HorizontalLayout hLayout = new HorizontalLayout(visSelector, generatedLinks);
    hLayout.setSizeFull();
    hLayout.setSpacing(false);
    hLayout.setExpandRatio(generatedLinks, 1.0f);

    Button btClose = new Button("Close");
    btClose.setSizeUndefined();
    btClose.addClickListener(event -> getUI().removeWindow(ShareSingleMatchGenerator.this));

    layout = new VerticalLayout(infoText, hLayout, btClose);
    layout.setSizeFull();
    layout.setExpandRatio(hLayout, 1.0f);
    layout.setSpacing(false);
    layout.setComponentAlignment(btClose, Alignment.MIDDLE_CENTER);

    setContent(layout);
  }

  private URI generatorURLForVisualizer(VisualizerRule entry) {
    String appContext = ui.getServletContext().getContextPath();
    URI appURI = ui.getPage().getLocation();
    UriBuilder result = UriBuilder.fromUri(appURI).replacePath(appContext).path("embeddedvis")
        .path(Helper.encodeJersey(entry.getVisType())).fragment("");
    if (entry.getLayer() != null) {
      result = result.queryParam("embedded_ns", Helper.encodeJersey(entry.getLayer()));
    }
    // test if the request was made from a sub-instance
    String nonContextPath = appURI.getPath().substring(appContext.length());
    if (!nonContextPath.isEmpty()) {
      if (nonContextPath.startsWith("/")) {
        nonContextPath = nonContextPath.substring(1);
      }
      result = result.queryParam(EmbeddedVisUI.KEY_INSTANCE, nonContextPath);
    }

    Optional<VisualizerPlugin> visPlugin = visualizerPlugins.stream()
        .filter(vis -> Objects.equal(vis.getShortName(), entry.getVisType())).findAny();
    
    // Add the matched node IDs
    result = result.queryParam(EmbeddedVisUI.KEY_MATCH, Helper.encodeJersey(match.toString()));
    if (visPlugin.isPresent() && visPlugin.get().isUsingText()) {
      // Tell the embedded visualizer to extract the fulltext for the whole match
      result = result.queryParam(EmbeddedVisUI.KEY_FULLTEXT, "true");
    }
    // Add left, right and segmentation context information
    result = result.queryParam(EmbeddedVisUI.KEY_LEFT, query.getLeftContext());
    result = result.queryParam(EmbeddedVisUI.KEY_RIGHT, query.getRightContext());
    if(query.getSegmentation() != null) {
      result = result.queryParam(EmbeddedVisUI.KEY_SEGMENTATION, query.getSegmentation());
    }

    // add the current view as "return back" parameter
    result = result.queryParam(EmbeddedVisUI.KEY_SEARCH_INTERFACE, appURI.toASCIIString());

    if (baseText != null) {
      result = result.queryParam(EmbeddedVisUI.KEY_BASE_TEXT, baseText);
    }

    // add all mappings as parameter
    for (Map.Entry<String, String> e : entry.getMappings().entrySet()) {
      if (!e.getKey().startsWith(EmbeddedVisUI.KEY_PREFIX)) {
        String value = Helper.encodeJersey(e.getValue());
        result = result.queryParam(e.getKey(), value);
      }
    }
  


    return result.build();
  }

  @Override
  public void select(SelectionEvent event) {
    Set<Object> selected = event.getSelected();
    if (ui instanceof AnnisUI && !selected.isEmpty()) {
      AnnisUI annisUI = (AnnisUI) ui;
      generatedLinks.setVisible(true);

      URI url = generatorURLForVisualizer((VisualizerRule) selected.iterator().next());
      String shortURL = annisUI.getUrlShortener().shortenURL(url, annisUI);
      directURL.setValue(shortURL);
      iframeCode
          .setValue("<iframe height=\"300px\" width=\"100%\" src=\"" + shortURL + "\"></iframe>");
      preview.setSource(new ExternalResource(shortURL));

    } else {
      generatedLinks.setVisible(false);
    }
  }

}
