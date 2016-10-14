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
package annis.gui.resultfetch;

import annis.gui.AnnisUI;
import annis.gui.objects.PagedResultQuery;
import annis.gui.paging.PagingComponent;
import annis.gui.resultview.ResultViewPanel;
import annis.libgui.Helper;
import annis.model.AqlParseError;
import annis.service.objects.Match;
import annis.service.objects.MatchGroup;
import annis.service.objects.SubgraphFilter;
import com.google.common.base.Joiner;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.salt.common.SaltProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A thread that queries for the matches, fetches the the subgraph for the
 * matches and updates the GUI at certain points.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ResultFetchJob extends AbstractResultFetchJob implements Runnable
{

  protected static final Logger log = LoggerFactory.getLogger(
    ResultFetchJob.class);

  protected ResultViewPanel resultPanel;

  private final Future<MatchGroup> futureMatches;

  protected AsyncWebResource res;

  protected PagedResultQuery query;

  protected AnnisUI ui;

  public ResultFetchJob(PagedResultQuery query,
    ResultViewPanel resultPanel,
    AnnisUI ui)
  {
    this.resultPanel = resultPanel;
    this.query = query;
    this.ui = ui;
    
    res = Helper.getAnnisAsyncWebResource();
    
    futureMatches = res.path("query").path("search").path("find")
      .queryParam("q", Helper.encodeJersey(query.getQuery()))
      .queryParam("offset", "" + query.getOffset())
      .queryParam("limit", "" + query.getLimit())
      .queryParam("corpora", Helper.encodeJersey(StringUtils.join(query.getCorpora(), ",")))
      .queryParam("order", query.getOrder().toString())
      .accept(MediaType.APPLICATION_XML_TYPE)
      .get(MatchGroup.class);

  }

  @Override
  public void run()
  {
    WebResource subgraphRes
      = Helper.getAnnisWebResource().path("query/search/subgraph");

    // holds the ids of the matches.
    MatchGroup result;

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
      result = futureMatches.get();
      
      // get the subgraph for each match, when the result is not empty
      if (result.getMatches().isEmpty())
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
        ui.access(new Runnable()
        {
          @Override
          public void run()
          {
            resultPanel.showSubgraphSearchInProgress(query, 0.0f);
          }
        });

        // prepare fetching subgraphs
       
        final BlockingQueue<SaltProject> queue = new ArrayBlockingQueue<>(
          result.getMatches().size());
        int current = 0;        
        final ArrayList<Match> matchList = new ArrayList<>(result.getMatches());

        for (Match m : matchList)
        {
          if (Thread.interrupted())
          {
            return;
          }

          List<Match> subList = new LinkedList<>();
          subList.add(m);
          final SaltProject p = executeQuery(subgraphRes, 
            new MatchGroup(subList), 
            query.getLeftContext(), query.getRightContext(),
            query.getSegmentation(), SubgraphFilter.all);

          queue.put(p);
          log.debug("added match {} to queue", current+1);

          
          if (current == 0)
          {
            ui.access(new Runnable()
            {
              @Override
              public void run()
              {
                resultPanel.setQueryResultQueue(queue, query, matchList);
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

    }
    catch (InterruptedException ex)
    {
      // just return
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
                List<AqlParseError> errors
                  = ex.getResponse().getEntity(
                    new GenericType<List<AqlParseError>>()
                    {
                    });
                String errMsg = Joiner.on(" | ").join(errors);

                paging.setInfo("parsing error: " + errMsg);
              }
              else if (ex.getResponse().getStatus() == 504)
              {
                paging.setInfo("Timeout: query execution took too long");
              }
              else if(ex.getResponse().getStatus() == 403)
              {
                paging.setInfo("Not authorized to query this corpus.");
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
    } // end catch
  }
}
