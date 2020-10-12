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
package annis.gui.exporter;

import annis.libgui.Helper;
import annis.libgui.exporter.ExporterPlugin;
import annis.service.objects.SubgraphFilter;
import com.google.common.eventbus.EventBus;
import com.vaadin.ui.UI;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.api.CorporaApi;
import org.corpus_tools.annis.api.SearchApi;
import org.corpus_tools.annis.api.model.CorpusConfiguration;
import org.corpus_tools.annis.api.model.FindQuery;
import org.corpus_tools.annis.api.model.QueryAttributeDescription;
import org.corpus_tools.annis.api.model.QueryLanguage;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SaltProject;
import org.eclipse.emf.common.util.URI;
import org.hibernate.cache.CacheException;
import org.slf4j.LoggerFactory;

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


  private static final org.slf4j.Logger log = LoggerFactory.getLogger(BaseMatrixExporter.class);
  
  /**
   * Iterates over all matches (modelled as corpus graphs) and executes the first pass
   * ({@link #createAdjacencyMatrix(SDocumentGraph, Map, int, int)} on the results.
   * 
   * @param p
   * @param args
   * @param offset
   */
  private void processFirstPass(SaltProject p, Map<String, String> args, int offset, int nodeCount)
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


  @Override
  public Exception convertText(String queryAnnisQL, QueryLanguage queryLanguage, int contextLeft,
      int contextRight, Set<String> corpora, List<String> keys, String argsAsString,
      boolean alignmc, Writer out, EventBus eventBus,
      Map<String, CorpusConfiguration> corpusConfigs, UI ui) {

    try {

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

      SearchApi searchApi = new SearchApi(Helper.getClient(ui));

      // 1. Get all the matches as Salt ID
      FindQuery query = new FindQuery();
      query.setCorpora(new LinkedList<>(corpora));
      query.setQueryLanguage(queryLanguage);
      query.setQuery(queryAnnisQL);
      File matches = searchApi.find(query);

      // Get the node count for the query by parsing it
      List<QueryAttributeDescription> nodeDescriptions =
          searchApi.nodeDescriptions(queryAnnisQL, queryLanguage);
      Integer nodeCount = nodeDescriptions.size();

      final AtomicInteger offset = new AtomicInteger();

      List<Integer> listOfKeys = new ArrayList<>();


      Collections.sort(listOfKeys);

      LinkedList<SDocument> serializedDocuments = new LinkedList<>();

      // 2. iterate over all matches and get the sub-graph for them
      CorporaApi corporaApi = new CorporaApi(Helper.getClient(ui));
      Optional<Exception> ex =
          Files.lines(matches.toPath(), StandardCharsets.UTF_8).map((currentLine) -> {
            try {
              Optional<SaltProject> p = ExportHelper.getSubgraphForMatch(currentLine, corporaApi,
                  contextLeft, contextRight, args);
              if (p.isPresent()) {
                int currentOffset = offset.getAndIncrement();
                processFirstPass(p.get(), args, currentOffset, nodeCount);

                // Serialize the salt project to a file for later use in the second pass
                SDocument doc = p.get().getCorpusGraphs().get(0).getDocuments().get(0);
                File tmpFile = File.createTempFile("annis-export-", ".salt");
                URI location = URI.createFileURI(tmpFile.getAbsolutePath());
                // Saving the document graph will set the document graph reference to null.
                // This is the desired effect, since we don't want to hold the graph in memory
                doc.saveDocumentGraph(location);
                serializedDocuments.add(doc);

                tmpFile.deleteOnExit();

                if (eventBus != null && (currentOffset + 1) % 100 == 0) {
                  eventBus.post(currentOffset + 1);
                }
              }

              if (Thread.interrupted()) {
                return new InterruptedException("Exporter job was interrupted");
              }
            } catch (Exception e) {
              return e;
            }
            return null;
          }).filter((result) -> result != null).findAny();

      if (ex.isPresent()) {
        return ex.get();
      }

      // build the list of ordered match numbers (ordering by occurrence in text)
      getOrderedMatchNumbers();

      // Execute the second pass on all Salt projects
      int recordNumber = 0;
      for (SDocument doc : serializedDocuments) {
        doc.loadDocumentGraph();
        
        outputText(doc.getDocumentGraph(), alignmc, recordNumber++, out, ui);

        URI location = doc.getDocumentGraphLocation();
        // Delete the temporary file
        File tmpFile = new File(location.toFileString());
        Files.deleteIfExists(tmpFile.toPath());
      }

      out.append(System.lineSeparator());

      return null;

    } catch (ApiException | IOException | CacheException | IllegalStateException
        | ClassCastException ex) {
      return ex;
    }

  }

  public abstract void createAdjacencyMatrix(SDocumentGraph graph, Map<String, String> args,
      int recordNumber, int nodeCount) throws IOException;

  /**
   * Specifies the ending of export file.
   */
  @Override
  public String getFileEnding() {
    return "txt";
  }

  public abstract void getOrderedMatchNumbers();

  public abstract SubgraphFilter getSubgraphFilter();

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

  public abstract void outputText(SDocumentGraph graph, boolean alignmc, int recordNumber,
      Writer out, UI ui) throws IOException;
}
