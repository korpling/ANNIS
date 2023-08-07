/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
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
package org.corpus_tools.annis.gui.docbrowser;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.data.util.filter.SimpleStringFilter;
import com.vaadin.v7.ui.themes.ChameleonTheme;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.corpus_tools.annis.api.model.AnnotationComponentType;
import org.corpus_tools.annis.api.model.QueryLanguage;
import org.corpus_tools.annis.api.model.VisualizerRule;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.Background;
import org.corpus_tools.annis.gui.components.ExceptionDialog;
import org.corpus_tools.annis.gui.graphml.CorpusGraphMapper;
import org.corpus_tools.annis.gui.objects.DocumentBrowserConfig;
import org.corpus_tools.annis.gui.objects.Visualizer;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

/**
 *
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
public class DocBrowserPanel extends Panel {

  private static final Logger log = LoggerFactory.getLogger(DocBrowserPanel.class);

  private class LoadingDocs implements Runnable {

    @Override
    public void run() {

      WebClient client = ui.getWebClient();

      try {
        File graphML = File.createTempFile("annis-subgraph", ".salt");
        Flux<DataBuffer> response = client.get()
            .uri(uriBuilder -> uriBuilder.path("/corpora/{corpus}/subgraph-for-query")
                .queryParam("query", "annis:node_type=\"corpus\"")
                .queryParam("query_language", QueryLanguage.AQL)
                .queryParam("component_type_filter", AnnotationComponentType.PARTOF)
                .build(corpus))
            .accept(MediaType.APPLICATION_XML).retrieve().bodyToFlux(DataBuffer.class);
        DataBufferUtils.write(response, graphML.toPath()).block();
        SCorpusGraph graph = CorpusGraphMapper.map(graphML);
        Files.deleteIfExists(graphML.toPath());
        List<SDocument> docs = graph.getDocuments();

        ui.access(() -> {
          table = DocBrowserTable.getDocBrowserTable(DocBrowserPanel.this);
          layout.removeComponent(progress);

          TextField txtFilter = new TextField();
          txtFilter.setWidth("100%");
          txtFilter.setPlaceholder("Filter documents by name");
          txtFilter.addValueChangeListener(event -> {
            if (table != null) {
              table.setContainerFilter(new SimpleStringFilter(DocBrowserTable.PROP_DOC_NAME,
                  event.getValue(), true, false));
            }
          });

          layout.addComponent(txtFilter);
          layout.addComponent(table);
          layout.setExpandRatio(table, 1.0f);

          table.setDocuments(docs);
        });

      } catch (WebClientResponseException | IOException | XMLStreamException ex) {
        ui.access(() -> {
           ExceptionDialog.show(ex, ui);
        });
      }


    }
  }

  /**
   * 
   */
  private static final long serialVersionUID = -1785316182826648719L;

  private final static Escaper urlPathEscape = UrlEscapers.urlPathSegmentEscaper();

  /**
   * Initiated the {@link DocBrowserPanel} and put the main tab navigation.
   *
   * @param ui The main application class of the gui.
   * @param corpus The corpus, for which the doc browser is initiated.
   * @return A new wrapper panel for a doc browser. Make sure, that this is not done several times.
   */
  public static DocBrowserPanel initDocBrowserPanel(AnnisUI ui, String corpus) {
    return new DocBrowserPanel(ui, corpus);
  }

  private final AnnisUI ui;

  private VerticalLayout layout;

  // the name of the corpus from which the documents are fetched
  private String corpus;

  private DocBrowserTable table;

  final ProgressBar progress;

  private DocBrowserPanel(AnnisUI ui, String corpus) {
    this.ui = ui;
    this.corpus = corpus;

    // init layout
    layout = new VerticalLayout();
    setContent(layout);
    layout.setSizeFull();
    layout.addStyleName(ChameleonTheme.PANEL_BORDERLESS);

    setSizeFull();

    progress = new ProgressBar();
    progress.setIndeterminate(true);
    progress.setSizeFull();

  }

  @Override
  public void attach() {
    super.attach();

    // start fetching table only if not done yet.
    if (table == null) {
      layout.addComponent(progress);
      layout.setComponentAlignment(progress, Alignment.MIDDLE_CENTER);
      Background.run(new LoadingDocs());
    }
  }

  public String getCorpus() {
    return corpus;
  }

  public DocumentBrowserConfig getDocBrowserConfig() {
    DocumentBrowserConfig defaultConfig = new DocumentBrowserConfig();
    VisualizerRule textVis = new VisualizerRule();
    textVis.setDisplayName("full text");
    textVis.setVisType("raw_text");
    defaultConfig.setVisualizers(Arrays.asList(new Visualizer(textVis)));
    WebClient client = ui.getWebClient();

    try {
      byte[] response = client.get()
          .uri("/corpora/{corpus}/files/{name}", getCorpus(),
              urlPathEscape.escape(getCorpus()) + "/document_browser.json")
          .retrieve().bodyToMono(byte[].class).block();
      try (ByteArrayInputStream is = new ByteArrayInputStream(response)) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        DocumentBrowserConfig config = mapper.readValue(is, DocumentBrowserConfig.class);
        return config;
      }
    } catch (WebClientResponseException ex) {
      if (ex.getStatusCode() != HttpStatus.NOT_FOUND) {
        ExceptionDialog.show(ex,
            "Could not get the document browser configuration file from the backend.", ui);
      }
    }
    catch (IOException ex) {
      ExceptionDialog.show(ex,
          "Could not get the document browser configuration file from the backend.", ui);
    }

    return defaultConfig;
  }

  public void openVis(String docId, VisualizerRule config, Button btn) {
    ui.getSearchView().getDocBrowserController().openDocVis(corpus, docId, config, btn);
  }

}
