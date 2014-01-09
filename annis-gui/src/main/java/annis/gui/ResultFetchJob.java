/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.gui;

import annis.gui.model.PagedResultQuery;
import annis.gui.paging.PagingComponent;
import annis.gui.resultview.ResultViewPanel;
import annis.libgui.Helper;
import annis.service.objects.Match;
import annis.service.objects.MatchGroup;
import annis.service.objects.SubgraphQuery;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A thread that queries for the matches, fetches the the subgraph for the
 * matches and updates the GUI at certain points.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
class ResultFetchJob implements Runnable
{

  private static final Logger log = LoggerFactory.getLogger(
    ResultFetchJob.class);

  private ResultViewPanel resultPanel;

  private Future<List<Match>> futureMatches;

  private AsyncWebResource res;

  private PagedResultQuery query;

  private SearchUI ui;

  public ResultFetchJob(PagedResultQuery query, ResultViewPanel resultPanel,
    SearchUI ui)
  {
    this.resultPanel = resultPanel;
    this.query = query;
    this.ui = ui;

    res = Helper.getAnnisAsyncWebResource();

    futureMatches = res.path("query").path("search").path("find")
      .queryParam("q", query.getQuery())
      .queryParam("offset", "" + query.getOffset())
      .queryParam("limit", "" + query.getLimit())
      .queryParam("corpora", StringUtils.join(query.getCorpora(), ","))
      .accept(MediaType.APPLICATION_XML_TYPE)
      .get(new MatchListType());

  }

  private SaltProject executeQuery(WebResource subgraphRes,
    SubgraphQuery query)
  {
    SaltProject p = null;
    try
    {
      p = subgraphRes.post(SaltProject.class, query);
    }
    catch (UniformInterfaceException ex)
    {
      log.error(ex.getMessage(), ex);
    }

    return p;
  }

  private SubgraphQuery prepareQuery(List<Match> matchesToPrepare)
  {
    SubgraphQuery subgraphQuery = new SubgraphQuery();

    subgraphQuery.setLeft(query.getContextLeft());
    subgraphQuery.setRight(query.getContextRight());
    if (query.getSegmentation() != null)
    {
      subgraphQuery.setSegmentationLayer(query.getSegmentation());
    }

    MatchGroup saltURIs = new MatchGroup();

    ListIterator<Match> it = matchesToPrepare.listIterator();
    int i = 0;
    while (it.hasNext())
    {
      Match m = it.next();
      saltURIs.getMatches().put(++i, m);
    }

    subgraphQuery.setMatches(saltURIs);
    return subgraphQuery;
  }

  @Override
  public void run()
  {
    WebResource subgraphRes
      = Helper.getAnnisWebResource().path("query/search/subgraph");

    // holds the ids of the matches.
    List<Match> result;

    try
    {
      if (Thread.interrupted())
      {
        return;
      }

      // set the the progress bar, for given the user some information about the loading process
      ui.accessSynchronously(new Runnable()
      {
        @Override
        public void run()
        {
          resultPanel.showMatchSearchInProgress(query);
        }
      });

      // get the matches
      result = futureMatches.get(60, TimeUnit.SECONDS);

      // get the subgraph for each match, when the result is not empty
      if (result.isEmpty())
      {

        // check if thread was interrupted
        if (Thread.interrupted())
        {
          return;
        }

        // nothing found, so inform the user about this.
        ui.access(new Runnable()
        {
          @Override
          public void run()
          {
            resultPanel.showNoResult();
          }
        });
      }
      else
      {
        if (Thread.interrupted())
        {
          return;
        }

        // since annis found something, inform the user that subgraphs are created
        ui.accessSynchronously(new Runnable()
        {
          @Override
          public void run()
          {
            resultPanel.showSubgraphSearchInProgress(query, 0.0f);
          }
        });

        // prepare fetching subgraphs
        final int totalResultSize = result.size();
        final BlockingQueue<SaltProject> queue = new ArrayBlockingQueue<SaltProject>(
          3);
        int current = 0;

        for (Match m : result)
        {
          if (Thread.interrupted())
          {
            return;
          }

          List<Match> subList = new LinkedList<Match>();
          subList.add(m);
          SubgraphQuery subgraphQuery = prepareQuery(subList);
          final SaltProject p = executeQuery(subgraphRes, subgraphQuery);

          queue.put(p);

          if (current == 0)
          {
            ui.accessSynchronously(new Runnable()
            {
              @Override
              public void run()
              {
                resultPanel.setQueryResultQueue(queue, query, totalResultSize);
              }
            });
          }

          if (Thread.interrupted())
          {
            return;
          }

          current++;
        }
      } // end if no results

      if (Thread.interrupted())
      {
        return;
      }
      ui.accessSynchronously(new Runnable()
      {
        @Override
        public void run()
        {
          resultPanel.showFinishedSubgraphSearch();
        }
      });
    }
    catch (TimeoutException ex)
    {
      log.info(null, ex);
    }
    catch (InterruptedException ex)
    {
      log.warn(null, ex);
    }
    catch (final ExecutionException root)
    {
      ui.accessSynchronously(new Runnable()
      {
        @Override
        public void run()
        {
          if (resultPanel != null && resultPanel.getPaging() != null)
          {
            PagingComponent paging = resultPanel.getPaging();
            Throwable cause = root.getCause();
            if (cause instanceof UniformInterfaceException)
            {
              UniformInterfaceException ex = (UniformInterfaceException) cause;
              if (ex.getResponse().getStatus() == 400)
              {
                paging.setInfo("parsing error: " + ex.getResponse().
                  getEntity(String.class));
              }
              else if (ex.getResponse().getStatus() == 504)
              {
                paging.setInfo("Timeout: query exeuction took too long");
              }
              else
              {
                paging.setInfo("unknown error: " + ex);
              }
            }
            else
            {
              log.error("Unexcepted ExecutionException cause",
                root);
            }

            resultPanel.showFinishedSubgraphSearch();

          }
        }
      });
    }
    finally
    {
      if (Thread.interrupted())
      {
        return;
      }
    }

  }

  private static class MatchListType extends GenericType<List<Match>>
  {

    public MatchListType()
    {
    }
  }
}
