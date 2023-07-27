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

import static org.corpus_tools.annis.gui.objects.AnnisConstants.ANNIS_NS;
import static org.corpus_tools.annis.gui.objects.AnnisConstants.FEAT_MATCHEDNODE;

import com.google.common.base.Splitter;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.api.model.Annotation;
import org.corpus_tools.annis.api.model.CorpusConfiguration;
import org.corpus_tools.annis.api.model.FindQuery;
import org.corpus_tools.annis.api.model.QueryLanguage;
import org.corpus_tools.annis.gui.CommonUI;
import org.corpus_tools.annis.gui.Helper;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SMetaAnnotation;
import org.corpus_tools.salt.core.SNode;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

public abstract class GeneralTextExporter implements ExporterPlugin, Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1583693456515398514L;

  public void appendMetaData(Writer out, List<String> metaKeys, String toplevelCorpus,
      String documentName, Map<String, Map<String, SMetaAnnotation>> metadataCache, UI ui)
      throws IOException {
    Map<String, SMetaAnnotation> metaData = new HashMap<>();
    if (metadataCache.containsKey(toplevelCorpus + ":" + documentName)) {
      metaData = metadataCache.get(toplevelCorpus + ":" + documentName);
    } else {

      List<SMetaAnnotation> asList = new ArrayList<>();
      for (SNode n : Helper.getMetaData(toplevelCorpus, Optional.ofNullable(documentName), ui)
          .getNodes()) {
        asList.addAll(n.getMetaAnnotations());
      }
      for (SMetaAnnotation anno : asList) {
        metaData.put(anno.getQName(), anno);
        metaData.put(anno.getName(), anno);
      }
      metadataCache.put(toplevelCorpus + ":" + documentName, metaData);
    }

    for (String key : metaKeys) {
      SMetaAnnotation anno = metaData.get(key);
      if (anno != null) {
        out.append("\tmeta::" + key + "\t" + anno.getValue()).append("\n");
      }
    }
  }

  public void convertText(SaltProject queryResult, List<String> keys, Map<String, String> args,
      Writer out, int offset, UI ui) throws IOException {
    Map<String, Map<String, SMetaAnnotation>> metadataCache = new HashMap<>();

    List<String> metaKeys = new LinkedList<>();
    if (args.containsKey("metakeys")) {
      Iterable<String> it = Splitter.on(",").trimResults().split(args.get("metakeys"));
      for (String s : it) {
        metaKeys.add(s);
      }
    }

    int counter = 0;

    for (SCorpusGraph corpusGraph : queryResult.getCorpusGraphs()) {
      for (SDocument doc : corpusGraph.getDocuments()) {
        SDocumentGraph graph = doc.getDocumentGraph();

        counter++;
        out.append((counter + offset) + ". ");
        List<SToken> tok = graph.getSortedTokenByText();

        for (SToken annisNode : tok) {
          SFeature featMatched = annisNode.getFeature(ANNIS_NS, FEAT_MATCHEDNODE);
          Long match = featMatched == null ? null : featMatched.getValue_SNUMERIC();

          if (match != null) {
            out.append("[");
            out.append(graph.getText(annisNode));
            out.append("]");
          } else {
            out.append(graph.getText(annisNode));
          }

          out.append(" ");

        }
        out.append("\n");

        if (!metaKeys.isEmpty()) {
          String[] path = Helper.getCorpusPath(corpusGraph, doc).toArray(new String[0]);
          appendMetaData(out, metaKeys, path[path.length - 1], path[0], metadataCache, ui);
        }
        out.append("\n");

      }
    }

  }

  @Override
  public Exception convertText(String queryAnnisQL, QueryLanguage queryLanguage, int contextLeft,
      int contextRight, Set<String> corpora, List<String> keys, String argsAsString,
      boolean alignmc, Writer out, EventBus eventBus,
      Map<String, CorpusConfiguration> corpusConfigs, CommonUI ui) {
    try {

      if (keys == null || keys.isEmpty()) {
        keys = getAllAnnotationsAsExporterKey(corpora, ui.getWebClient());
      }

      final List<String> finalKeys = keys;

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


      // 1. Get all the matches as Salt ID
      FindQuery query = new FindQuery();
      query.setCorpora(new LinkedList<String>(corpora));
      query.setQueryLanguage(queryLanguage);
      query.setQuery(queryAnnisQL);

      final AtomicInteger offset = new AtomicInteger();
      Flux<DataBuffer> response =
          ui.getWebClient().post().uri("/search/find").contentType(MediaType.APPLICATION_JSON)
              .bodyValue(query).accept(MediaType.TEXT_PLAIN).retrieve()
              .bodyToFlux(DataBuffer.class);

      File matches = File.createTempFile("annis-result", ".txt");
      DataBufferUtils.write(response, matches.toPath()).block();
      // 2. iterate over all matches and get the sub-graph for them
      try (LineIterator lines = FileUtils.lineIterator(matches, StandardCharsets.UTF_8.name())) {
        while (lines.hasNext()) {
          String currentLine = lines.nextLine();
          Optional<SaltProject> p = ExportHelper.getSubgraphForMatch(currentLine, ui.getWebClient(),
              contextLeft, contextRight, args, corpusConfigs);
          if (p.isPresent()) {
            int currentOffset = offset.getAndIncrement();
            convertText(p.get(), finalKeys, args, out, currentOffset, ui);

            if (eventBus != null) {
              eventBus.post(currentOffset + 1);
            }
          }

          if (Thread.interrupted()) {
            return new InterruptedException("Exporter job was interrupted");
          }
        }
      }
      Files.deleteIfExists(matches.toPath());

      return null;

    } catch (WebClientResponseException | IOException | XMLStreamException ex) {
      return ex;
    }
  }

  @Override
  public String getFileEnding() {
    return "txt";
  }

  @Override
  public boolean isCancelable() {
    return true;
  }

  @Override
  public boolean needsContext() {
    return true;
  }

  /**
   * Queries all annotations for the given corpora.
   * 
   * @param corpora The corpora to query
   * @param api An API object used to perform the lookup
   * @return A list of annotation names.
   * @throws ApiException
   */
  protected List<String> getAllAnnotationsAsExporterKey(Collection<String> corpora,
      WebClient client) throws WebClientResponseException {
    LinkedList<String> keys = new LinkedList<>();
    keys.add("tok");
    List<Annotation> attributes = new LinkedList<>();
  
    for (String corpus : corpora) {
      Iterable<Annotation> allAnnotations = client.get()
          .uri(uriBuilder -> uriBuilder.path("/corpora/{corpus}/node-annotations")
              .queryParam("list_values", false).queryParam("only_most_frequent_values", false)
              .build(corpus))
          .retrieve().bodyToFlux(Annotation.class).toIterable();
      for (Annotation a : allAnnotations) {
        attributes.add(a);
      }
    }
  
    for (Annotation a : attributes) {
      if (a.getKey().getName() != null) {
        keys.add(a.getKey().getName());
      }
    }
    return keys;
  }
}
