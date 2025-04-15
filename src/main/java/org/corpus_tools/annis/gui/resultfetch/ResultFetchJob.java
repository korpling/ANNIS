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
package org.corpus_tools.annis.gui.resultfetch;

import com.google.common.base.Joiner;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.stream.XMLStreamException;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.JSON;
import org.corpus_tools.annis.api.CorporaApi;
import org.corpus_tools.annis.api.SearchApi;
import org.corpus_tools.annis.api.model.BadRequestError;
import org.corpus_tools.annis.api.model.FindQuery;
import org.corpus_tools.annis.api.model.SubgraphWithContext;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.Helper;
import org.corpus_tools.annis.gui.components.ExceptionDialog;
import org.corpus_tools.annis.gui.components.codemirror.AqlCodeEditorState.ParseError;
import org.corpus_tools.annis.gui.graphml.DocumentGraphMapper;
import org.corpus_tools.annis.gui.objects.Match;
import org.corpus_tools.annis.gui.objects.MatchGroup;
import org.corpus_tools.annis.gui.objects.PagedResultQuery;
import org.corpus_tools.annis.gui.paging.PagingComponent;
import org.corpus_tools.annis.gui.resultview.ResultViewPanel;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SaltProject;
import org.eclipse.emf.common.util.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A thread that queries for the matches, fetches the the subgraph for the matches and updates the
 * GUI at certain points.
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class ResultFetchJob implements Runnable {

  protected static final Logger log = LoggerFactory.getLogger(ResultFetchJob.class);

  protected ResultViewPanel resultPanel;

  protected PagedResultQuery query;

  protected AnnisUI ui;

  public ResultFetchJob(PagedResultQuery query, ResultViewPanel resultPanel, AnnisUI ui) {
    this.resultPanel = resultPanel;
    this.query = query;
    this.ui = ui;
  }

  @Override
  public void run() {

    SearchApi search = new SearchApi(Helper.getClient(ui));
    CorporaApi corpora = new CorporaApi(Helper.getClient(ui));

    // holds the ids of the matches.
    MatchGroup result = new MatchGroup();

    try {
      if (Thread.interrupted()) {
        return;
      }

      // set the the progress bar, for given the user some information about the
      // loading process
      ui.access(() -> resultPanel.showMatchSearchInProgress(query.getSegmentation()));

      // get the matches
      FindQuery q = new FindQuery();
      q.setCorpora(new LinkedList<>(query.getCorpora()));
      q.setQuery(query.getQuery());
      q.setOffset((int) query.getOffset());
      q.setLimit(query.getLimit());
      q.setQueryLanguage(query.getApiQueryLanguage());
      q.setOrder(query.getOrder());
      File findResult = search.find(q);
      try (Stream<String> findResultLines =
          Files.lines(findResult.toPath(), StandardCharsets.UTF_8)) {
        findResultLines.forEachOrdered(line -> {
          Match m = Match.parseFromString(line);
          result.getMatches().add(m);
        });
      }
      // get the subgraph for each match, when the result is not empty
      if (result.getMatches().isEmpty()) {

        // check if thread was interrupted
        if (Thread.interrupted()) {
          return;
        }

        // nothing found, so inform the user about this.
        ui.access(() -> resultPanel.showNoResult());
      } else {
        if (Thread.interrupted()) {
          return;
        }

        // since graphANNIS found something, inform the user that subgraphs are created
        ui.access(() -> resultPanel.showSubgraphSearchInProgress(query, 0.0f));

        int current = 0;
        final ArrayList<Match> matchList = new ArrayList<>(result.getMatches());

        for (Match m : matchList) {
          if (Thread.interrupted()) {
            return;
          }

          SubgraphWithContext arg = new SubgraphWithContext();
          arg.setLeft(query.getLeftContext());
          arg.setRight(query.getRightContext());
          arg.setSegmentation(query.getSegmentation());
          arg.setNodeIds(m.getSaltIDs().stream().collect(Collectors.toList()));

          SaltProject p = createSaltFromMatch(m, arg, current, corpora);
          ui.access(() -> resultPanel.addQueryResult(query, p, matchList));

          if (Thread.interrupted()) {
            return;
          }

          current++;
        }
      } // end if no results

    } catch (final ApiException ex) {
      ui.access(() -> {
        if (resultPanel != null && resultPanel.getPaging() != null) {
          PagingComponent paging = resultPanel.getPaging();

          if (ex.getCode() == 400) {
            JSON json = new JSON();
            BadRequestError error = json.deserialize(ex.getResponseBody(), BadRequestError.class);

            String errMsg = "";
            if (error.getAqLSyntaxError() != null) {
              errMsg = new ParseError(error.getAqLSyntaxError()).message;
            }
            if (error.getAqLSemanticError() != null) {
              errMsg = new ParseError(error.getAqLSemanticError()).message;
            }

            paging.setInfo("parsing error: " + errMsg);
          } else if (ex.getCode() == 504) {
            paging.setInfo("Timeout: query execution took too long");
          } else if (ex.getCode() == 403) {
            paging.setInfo("Not authorized to query this corpus.");
          } else {
            ExceptionDialog.show(ex, "Backend error", ui);
          }
          resultPanel.showFinishedSubgraphSearch();

        }
      });
    } catch(IOException ex) {
      ui.access(() -> ExceptionDialog.show(ex, ui));
    }
  }

  private SaltProject createSaltFromMatch(Match m, SubgraphWithContext arg, int currentMatchNumber,
      CorporaApi api) throws ApiException {
    List<String> corpusPathRaw = Helper.getCorpusPath(m.getSaltIDs().get(0), false);
    List<String> corpusPathDecoded = Helper.getCorpusPath(m.getSaltIDs().get(0), true);
    final SaltProject p = SaltFactory.createSaltProject();

    if (!corpusPathRaw.isEmpty()) {
      File graphML = api.subgraphForNodes(corpusPathDecoded.get(0), arg);
      try {

        final SCorpusGraph cg = p.createCorpusGraph();
        URI docURI = URI.createURI("salt:/" + Joiner.on('/').join(corpusPathRaw));
        if (corpusPathRaw.size() > 1) {
          SDocument doc = cg.createDocument(docURI);
          SDocumentGraph docGraph = DocumentGraphMapper.map(graphML);
          if (Files.deleteIfExists(graphML.toPath())) {
            log.debug("Could not delete temporary SaltXML file {} because it does not exist.",
                graphML.getPath());
          }
          doc.setDocumentGraph(docGraph);
          Helper.addMatchToDocumentGraph(m, doc.getDocumentGraph());
        } else if (corpusPathRaw.size() == 1) {
          cg.createCorpus(null, corpusPathRaw.get(0));
        }
        log.debug("added match {} to queue", currentMatchNumber + 1);
      } catch (XMLStreamException | IOException ex) {
        log.error("Could not map GraphML to Salt", ex);
        ui.access(() -> ExceptionDialog.show(ex, "Could not map GraphML to Salt", ui));
      }
    }
    return p;
  }
}
