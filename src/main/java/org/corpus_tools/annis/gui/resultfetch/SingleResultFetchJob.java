/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
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
package org.corpus_tools.annis.gui.resultfetch;

import com.google.common.base.Joiner;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.Callable;
import org.corpus_tools.annis.api.model.SubgraphWithContext;
import org.corpus_tools.annis.gui.CommonUI;
import org.corpus_tools.annis.gui.Helper;
import org.corpus_tools.annis.gui.graphml.DocumentGraphMapper;
import org.corpus_tools.annis.gui.objects.Match;
import org.corpus_tools.annis.gui.objects.PagedResultQuery;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SaltProject;
import org.eclipse.emf.common.util.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

/**
 * Fetches a result which contains only one subgraph. This single query always follows a normal
 * ResultFetchJob and so it is assuming that there already exists a list of matches. That is the
 * reason for not needing to execute the find command and hopefully this query is bit faster.
 *
 * @see ResultFetchJob
 * @see LegacyQueryController
 *
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
public class SingleResultFetchJob implements Callable<SaltProject>
{

  private static final Logger log = LoggerFactory.getLogger(SingleResultFetchJob.class);

  private final Match match;

  private final PagedResultQuery query;

  private final CommonUI ui;

  public SingleResultFetchJob(Match match, PagedResultQuery query, CommonUI ui) {
    this.match = match;
    this.query = query;
    this.ui = ui;
  }

  @Override
  public SaltProject call() throws Exception {
    WebClient client = ui.getWebClient();

    if (Thread.interrupted()) {
      return null;
    }

    
    SubgraphWithContext subgraphQuery = new SubgraphWithContext();
    subgraphQuery.setNodeIds(match.getSaltIDs());
    subgraphQuery.setLeft(query.getLeftContext());
    subgraphQuery.setRight(query.getRightContext());
    subgraphQuery.setSegmentation(query.getSegmentation());

    List<String> corpusPath = Helper.getCorpusPath(match.getSaltIDs().get(0));
    final SaltProject p = SaltFactory.createSaltProject();
    SCorpusGraph cg = p.createCorpusGraph();

    if (!corpusPath.isEmpty()) {
      File graphML = File.createTempFile("annis-subgraph", ".salt");
      Flux<DataBuffer> response = client.post()
          .uri("/corpora/{corpus}/subgraph", corpusPath.get(0))
          .accept(MediaType.APPLICATION_XML).bodyValue(subgraphQuery).retrieve()
          .bodyToFlux(DataBuffer.class);
      DataBufferUtils.write(response, graphML.toPath()).block();
      URI docURI = URI.createURI("salt:/" + Joiner.on('/').join(corpusPath));
      SDocument doc = cg.createDocument(docURI);
      SDocumentGraph docGraph = DocumentGraphMapper.map(graphML);
      if (Files.deleteIfExists(graphML.toPath())) {
        log.debug("Could not delete temporary SaltXML file {} because it does not exist.",
            graphML.getPath());
      }
      doc.setDocumentGraph(docGraph);
      Helper.addMatchToDocumentGraph(match, doc.getDocumentGraph());
    }

    return p;

  }

}
