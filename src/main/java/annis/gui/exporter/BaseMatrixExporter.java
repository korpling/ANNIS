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

import annis.gui.graphml.DocumentGraphMapper;
import annis.libgui.Helper;
import annis.libgui.exporter.ExporterPlugin;
import annis.service.objects.Match;
import annis.service.objects.SubgraphFilter;
import com.google.common.base.Joiner;
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
import java.util.stream.Stream;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.pool.sizeof.annotations.IgnoreSizeOf;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.api.CorporaApi;
import org.corpus_tools.annis.api.SearchApi;
import org.corpus_tools.annis.api.model.Annotation;
import org.corpus_tools.annis.api.model.CorpusConfiguration;
import org.corpus_tools.annis.api.model.FindQuery;
import org.corpus_tools.annis.api.model.QueryAttributeDescription;
import org.corpus_tools.annis.api.model.QueryLanguage;
import org.corpus_tools.annis.api.model.SubgraphWithContext;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SaltProject;
import org.eclipse.emf.common.util.URI;

/**
 * An abstract base class for exporters that use Salt subgraphs to some kind of matrix output
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */

@IgnoreSizeOf
public abstract class BaseMatrixExporter implements ExporterPlugin, Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 787797500368376816L;


  /**
   * Iterates over all matches (modelled as corpus graphs) and calls
   * {@link #convertText(de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph, java.util.List, java.util.Map, int, java.io.Writer) }
   * for the single document graph.
   * 
   * @param p
   * @param args
   * @param alignmc
   * @param offset
   * @param out
   */
  private void convertSaltProject(SaltProject p, Map<String, String> args, boolean alignmc,
      int offset, Writer out, Integer nodeCount, UI ui)
      throws IOException {
    int recordNumber = offset;
    if (p != null && p.getCorpusGraphs() != null) {

      for (SCorpusGraph corpusGraph : p.getCorpusGraphs()) {
        if (corpusGraph.getDocuments() != null) {
          for (SDocument doc : corpusGraph.getDocuments()) {
            // invokes the createAdjacencyMatrix method, if nodeCount != null or
            // outputText
            // otherwise
            if (nodeCount != null) {
              createAdjacencyMatrix(doc.getDocumentGraph(), args, recordNumber++, nodeCount);
            } else {
              outputText(doc.getDocumentGraph(), alignmc, recordNumber++, out, ui);
            }

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
    CacheManager cacheManager = CacheManager.create();

    try {
      Cache cache = cacheManager.getCache("saltProjectsCache");

      if (keys == null || keys.isEmpty()) {
        // auto set
        keys = new LinkedList<>();
        keys.add("tok");
        List<Annotation> attributes = new LinkedList<>();

        CorporaApi api = new CorporaApi(Helper.getClient(ui));
        for (String corpus : corpora) {
          attributes.addAll(api.nodeAnnotations(corpus, false, false));
        }

        for (Annotation a : attributes) {
          if (a.getKey().getName() != null) {
            keys.add(a.getKey().getName());
          }
        }
      }

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
      CorporaApi corporaApi = new CorporaApi(Helper.getClient(ui));


      // 1. Get all the matches as Salt ID
      FindQuery query = new FindQuery();
      query.setCorpora(new LinkedList<String>(corpora));
      query.setQueryLanguage(queryLanguage);
      query.setQuery(queryAnnisQL);
      File matches = searchApi.find(query);

      // Get the node count for the query by parsing it
      List<QueryAttributeDescription> nodeDescriptions =
          searchApi.nodeDescriptions(queryAnnisQL, queryLanguage);
      Integer nodeCount = nodeDescriptions.size();

      final AtomicInteger offset = new AtomicInteger();
      final AtomicInteger pCounter = new AtomicInteger();
      Map<Integer, Integer> offsets = new HashMap<Integer, Integer>();

      Optional<Exception> ex = Optional.empty();
      try (Stream<String> lines = Files.lines(matches.toPath(), StandardCharsets.UTF_8)) {
        ex = lines.map((currentLine) -> {
          // 2. iterate over all matches and get the sub-graph for a group of matches
          Match match = Match.parseFromString(currentLine);

          if (!match.getSaltIDs().isEmpty()) {
            List<String> corpusPath = Helper.getCorpusPath(match.getSaltIDs().get(0));

            SubgraphWithContext subgraphQuery = new SubgraphWithContext();
            subgraphQuery.setLeft(contextLeft);
            subgraphQuery.setRight(contextRight);
            subgraphQuery.setNodeIds(match.getSaltIDs());

            if (args.containsKey("segmentation")) {
              subgraphQuery.setSegmentation(args.get("segmentation"));
            }

            final SaltProject p = SaltFactory.createSaltProject();
            SCorpusGraph cg = p.createCorpusGraph();
            URI docURI = URI.createURI("salt:/" + Joiner.on('/').join(corpusPath));
            SDocument doc = cg.createDocument(docURI);

            try {
              File graphML = corporaApi.subgraphForNodes(corpusPath.get(0), subgraphQuery);

              SDocumentGraph docGraph = DocumentGraphMapper.map(graphML);
              doc.setDocumentGraph(docGraph);
              Helper.addMatchToDocumentGraph(match, doc);

              int currentOffset = offset.getAndIncrement();
              int currentPCounter = pCounter.getAndIncrement();
              convertSaltProject(p, args, alignmc, currentOffset, out, nodeCount, ui);

              offsets.put(currentPCounter, currentOffset);
              cache.put(new Element(currentPCounter, p));


              if (eventBus != null && (currentOffset + 1) % 100 == 0) {
                eventBus.post(currentOffset + 1);
              }

              if (Thread.interrupted()) {
                Exception result = new InterruptedException("Exporter job was interrupted");
                return result;
              }
            } catch (Exception e) {
              return e;
            }
          }
          return null;
        }).filter((result) -> result != null).findAny();
      }


      if (ex.isPresent()) {
        return ex.get();
      }

      // build the list of ordered match numbers (ordering by occurrence in text)
      getOrderedMatchNumbers();

      @SuppressWarnings("unchecked")
      List<Integer> cacheKeys = cache.getKeys();
      List<Integer> listOfKeys = new ArrayList<Integer>();

      for (Integer key : cacheKeys) {
        listOfKeys.add(key);
      }

      Collections.sort(listOfKeys);

      for (Integer key : listOfKeys) {
        SaltProject p = (SaltProject) cache.get(key).getObjectValue();
        convertSaltProject(p, args, alignmc, offsets.get(key), out, null, ui);
      }

      out.append(System.lineSeparator());

      return null;

    } catch (ApiException | IOException | CacheException | IllegalStateException
        | ClassCastException ex) {
      return ex;
    } finally {
      cacheManager.removalAll();
      cacheManager.shutdown();
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
