/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632
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
package org.corpus_tools.annis.gui.exporter;

import com.google.common.eventbus.EventBus;
import com.vaadin.ui.UI;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.corpus_tools.annis.api.model.AnnotationComponentType;
import org.corpus_tools.annis.api.model.CorpusConfiguration;
import org.corpus_tools.annis.api.model.FindQuery;
import org.corpus_tools.annis.api.model.QueryAttributeDescription;
import org.corpus_tools.annis.api.model.QueryLanguage;
import org.corpus_tools.annis.gui.CommonUI;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SaltProject;
import org.hibernate.cache.CacheException;
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
 * An abstract base class for exporters that use Salt subgraphs to some kind of matrix output
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public abstract class BaseMatrixExporter implements ExporterPlugin, Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 787797500368376816L;

  private static final Logger log = LoggerFactory.getLogger(BaseMatrixExporter.class);

  /**
   * Iterates over all matches (modelled as corpus graphs) and executes the first pass
   * ({@link #createAdjacencyMatrix(SDocumentGraph, Map, int, int)} on the results.
   * 
   * @param p
   * @param args
   * @param offset
   */
  private void processFirstPass(SaltProject p, Map<String, String> args, int offset, long nodeCount)
      throws IOException {
    int recordNumber = offset;
    if (p != null && p.getCorpusGraphs() != null) {

      for (SCorpusGraph corpusGraph : p.getCorpusGraphs()) {
        if (corpusGraph.getDocuments() != null) {
          for (SDocument doc : corpusGraph.getDocuments()) {
            createAdjacencyMatrix(doc.getDocumentGraph(), args, recordNumber++, nodeCount);
          }
        }
      }
    }
  }

  private static boolean segmentationNameIsValid(Collection<String> corpora, String segmentation,
      CommonUI ui) {
    WebClient client = ui.getWebClient();
    for (String corpus : corpora) {
      try {
        Long matchingSegmentations = client.get()
            .uri(ub -> ub.path("/corpora/{corpus}/components")
                .queryParam("type", AnnotationComponentType.ORDERING.getValue())
                .queryParam("name", segmentation).build(corpus))
            .retrieve().bodyToFlux(org.corpus_tools.annis.api.model.Component.class).count()
            .block();
        if (matchingSegmentations == null || matchingSegmentations == 0) {
          return false;
        }
      } catch (WebClientResponseException ex) {
        if (ex.getStatusCode() == HttpStatus.FORBIDDEN) {
          log.debug("Did not have access rights to query segmentation names for corpus", ex);
        } else {
          log.warn("Could not query segmentation names for corpus", ex);
        }
      }
    }
    return true;
  }

  @Override
  public Exception convertText(String queryAnnisQL, QueryLanguage queryLanguage, int contextLeft,
      int contextRight, Set<String> corpora, List<String> keys, String argsAsString,
      boolean alignmc, Writer out, EventBus eventBus,
      Map<String, CorpusConfiguration> corpusConfigs, CommonUI ui) {



    Map<String, String> args = new HashMap<>();
    for (String s : argsAsString.split("&|;")) {
      String[] splitted = s.split("=", 2);
      String key = splitted[0];
      String val = "";
      if (splitted.length > 1) {
        val = splitted[1];
      }
      args.put(key, val);
    }

    WebClient client = ui.getWebClient();

    // Do some validity checks of the arguments, like a segmentation must exist on all selected
    // corpora
    String segmentation = args.get(ExportHelper.SEGMENTATION_KEY);
    if (segmentation != null && !segmentationNameIsValid(corpora, segmentation, ui)) {
        return new IllegalArgumentException("The 'segmentation' parameter is set to '"
            + segmentation
            + "' but this segmentation does not exist in all corpora.");
    }

    // 1. Get all the matches as Salt ID
    FindQuery query = new FindQuery();
    query.setCorpora(new LinkedList<>(corpora));
    query.setQueryLanguage(queryLanguage);
    query.setQuery(queryAnnisQL);
    try {

      Flux<DataBuffer> response = ui.getWebClient().post().uri("/search/find")
          .contentType(MediaType.APPLICATION_JSON).bodyValue(query).accept(MediaType.TEXT_PLAIN)
          .retrieve().bodyToFlux(DataBuffer.class);

      File matches = File.createTempFile("annis-result", ".txt");
      DataBufferUtils.write(response, matches.toPath()).block();

      // Get the node count for the query by parsing it
      Long nodeCount = client.get()
          .uri(ub -> ub.path("/search/node-descriptions").queryParam("query", queryAnnisQL)
              .queryParam("query_language", queryLanguage).build())
          .retrieve().bodyToFlux(QueryAttributeDescription.class).count().block();

      List<Integer> listOfKeys = new ArrayList<>();

      Collections.sort(listOfKeys);

      // First pass: iterate over all matches and get the sub-graph for them
      int progress = 0;
      try (LineIterator lines = FileUtils.lineIterator(matches, StandardCharsets.UTF_8.name())) {
        int recordNumber = 0;
        while (lines.hasNext()) {
          String currentLine = lines.nextLine();
          Optional<SaltProject> p = ExportHelper.getSubgraphForMatch(currentLine, ui.getWebClient(),
              contextLeft, contextRight, args, corpusConfigs);
          if (p.isPresent()) {
            processFirstPass(p.get(), args, recordNumber++, nodeCount);
          }
          progress++;

          if (eventBus != null && progress % 100 == 0) {
            eventBus.post(progress / 2);
          }

          if (Thread.interrupted()) {
            return new InterruptedException("Exporter job was interrupted");
          }
        }
      }

      // build the list of ordered match numbers (ordering by occurrence in text)
      getOrderedMatchNumbers();



      // Execute the second pass on all Salt projects
      int recordNumber = 0;
      try (LineIterator lines = FileUtils.lineIterator(matches, StandardCharsets.UTF_8.name())) {
        while (lines.hasNext()) {
          String currentLine = lines.nextLine();
          Optional<SaltProject> p = ExportHelper.getSubgraphForMatch(currentLine, ui.getWebClient(),
              contextLeft, contextRight, args, corpusConfigs);
          if (p.isPresent()) {
            for (SCorpusGraph cg : p.get().getCorpusGraphs()) {
              for (SDocument doc : cg.getDocuments()) {
                outputText(doc.getDocumentGraph(), args, alignmc, recordNumber++, out, ui);
                progress++;
                if (eventBus != null && progress % 100 == 0) {
                  eventBus.post(progress / 2);
                }
              }
            }
          }

          if (Thread.interrupted()) {
            return new InterruptedException("Exporter job was interrupted");
          }
        }
      }

      out.append("\n");

      Files.deleteIfExists(matches.toPath());

      return null;

    } catch (WebClientResponseException | IOException | CacheException | IllegalStateException
        | ClassCastException | XMLStreamException ex) {
      return ex;
    }

  }

  public abstract void createAdjacencyMatrix(SDocumentGraph graph, Map<String, String> args,
      int recordNumber, long nodeCount) throws IOException;

  /**
   * Specifies the ending of export file.
   */
  @Override
  public String getFileEnding() {
    return "txt";
  }

  public abstract void getOrderedMatchNumbers();

  /**
   * Indicates, whether the export can be cancelled or not.
   */
  @Override
  public boolean isCancelable() {
    return true;
  }

  @Override
  public boolean needsContext() {
    return true;
  }


  public abstract void outputText(SDocumentGraph graph, Map<String, String> args, boolean alignmc,
      int recordNumber, Writer out, UI ui) throws IOException;
}
