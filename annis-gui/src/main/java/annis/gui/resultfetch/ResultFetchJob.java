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
package annis.gui.resultfetch;

import annis.CommonHelper;
import annis.gui.AnnisUI;
import annis.gui.ServiceHelper;
import annis.gui.paging.PagingComponent;
import annis.gui.resultview.ResultViewPanel;
import annis.libgui.Helper;
import annis.model.AqlParseError;
import annis.model.PagedResultQuery;
import annis.service.objects.Match;
import annis.service.objects.MatchGroup;
import annis.service.objects.QueryLanguage;
import annis.service.objects.SubgraphFilter;
import okhttp3.Call;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.api.CorporaApi;
import org.corpus_tools.annis.api.SearchApi;
import org.corpus_tools.annis.api.model.FindQuery;
import org.corpus_tools.annis.api.model.SubgraphWithContext;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SaltProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A thread that queries for the matches, fetches the the subgraph for the matches and updates the
 * GUI at certain points.
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class ResultFetchJob extends AbstractResultFetchJob implements Runnable {

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

        SearchApi search = new SearchApi(ServiceHelper.getClient(ui));
        CorporaApi corpora = new CorporaApi(ServiceHelper.getClient(ui));

        // holds the ids of the matches.
        MatchGroup result;

        try {
            if (Thread.interrupted()) {
                return;
            }

            // set the the progress bar, for given the user some information about the
            // loading process
            ui.accessSynchronously(
                    () -> resultPanel.showMatchSearchInProgress(query.getSegmentation()));

            // get the matches
            FindQuery q = new FindQuery();
            q.setCorpora(new LinkedList<>(query.getCorpora()));
            q.setQuery(query.getQuery());
            q.setOffset((int) query.getOffset());
            q.setLimit(query.getLimit());
            if (query.getQueryLanguage() == QueryLanguage.AQL_QUIRKS_V3) {
                q.setQueryLanguage(org.corpus_tools.annis.api.model.QueryLanguage.AQLQUIRKSV3);
            } else {
                q.setQueryLanguage(org.corpus_tools.annis.api.model.QueryLanguage.AQL);
            }
            result = MatchGroup.parseString(search.find(q));

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

                // prepare fetching subgraphs

                final BlockingQueue<SaltProject> queue =
                        new ArrayBlockingQueue<>(result.getMatches().size());
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
                    arg.setNodeIds(m.getSaltIDs().stream().map(id -> id.toString())
                            .collect(Collectors.toList()));
                    List<String> corpusPath = CommonHelper.getCorpusPath(m.getSaltIDs().get(0));

                    if (!corpusPath.isEmpty()) {
                        String graphML = corpora.subgraphForNodes(corpusPath.get(0), arg);
                        // TODO create Salt from GraphML
                        final SaltProject p = SaltFactory.createSaltProject();
                        queue.put(p);
                        log.debug("added match {} to queue", current + 1);
                    }

                    if (current == 0) {
                        ui.access(() -> resultPanel.setQueryResultQueue(queue, query, matchList));
                    }

                    if (Thread.interrupted()) {
                        return;
                    }

                    current++;
                }
            } // end if no results

        } catch (InterruptedException ex) {
            // just return
        } catch (final ApiException root) {
            ui.accessSynchronously(() -> {
                if (resultPanel != null && resultPanel.getPaging() != null) {
                    PagingComponent paging = resultPanel.getPaging();
                    Throwable cause = root.getCause();

                    if (cause instanceof UniformInterfaceException) {
                        UniformInterfaceException ex = (UniformInterfaceException) cause;
                        if (ex.getResponse().getStatus() == 400) {
                            List<AqlParseError> errors = ex.getResponse()
                                    .getEntity(new GenericType<List<AqlParseError>>() {});
                            String errMsg = Joiner.on(" | ").join(errors);

                            paging.setInfo("parsing error: " + errMsg);
                        } else if (ex.getResponse().getStatus() == 504) {
                            paging.setInfo("Timeout: query execution took too long");
                        } else if (ex.getResponse().getStatus() == 403) {
                            paging.setInfo("Not authorized to query this corpus.");
                        } else {
                            paging.setInfo("unknown error: " + ex);
                        }
                    } else {
                        log.error("Unexcepted ExecutionException cause", root);
                    }

                    resultPanel.showFinishedSubgraphSearch();

                }
            });
        } // end catch
    }
}
