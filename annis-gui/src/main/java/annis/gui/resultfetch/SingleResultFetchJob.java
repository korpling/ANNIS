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
package annis.gui.resultfetch;

import annis.gui.graphml.DocumentGraphMapper;
import annis.libgui.Helper;
import annis.model.PagedResultQuery;
import annis.service.objects.Match;
import com.google.common.base.Joiner;
import com.vaadin.ui.UI;
import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import org.corpus_tools.annis.api.CorporaApi;
import org.corpus_tools.annis.api.model.SubgraphWithContext;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SaltProject;
import org.eclipse.emf.common.util.URI;

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

  private final Match match;

  private final PagedResultQuery query;

  private final UI ui;

  public SingleResultFetchJob(Match match, PagedResultQuery query, UI ui) {
    this.match = match;
    this.query = query;
    this.ui = ui;
  }

  @Override
  public SaltProject call() throws Exception {
    CorporaApi api = new CorporaApi(Helper.getClient(ui));

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
      File graphML = api.subgraphForNodes(corpusPath.get(0), subgraphQuery);
      URI docURI = URI.createURI("salt:/" + Joiner.on('/').join(corpusPath));
      SDocument doc = cg.createDocument(docURI);
      SDocumentGraph docGraph = DocumentGraphMapper.map(graphML);
      doc.setDocumentGraph(docGraph);
      Helper.addMatchToDocumentGraph(match, doc);
    }

    return p;

  }

}
