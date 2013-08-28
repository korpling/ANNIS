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
import annis.service.objects.SaltURIGroup;
import annis.service.objects.SaltURIGroupSet;
import annis.service.objects.SubgraphQuery;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.ui.UI;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A thread that queries for the matches, fetches the the subgraph for the
 * matches and updates the GUI at certain points.
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
class ResultFetchThread extends Thread
{

  private static final Logger log = LoggerFactory.getLogger(
    ResultFetchThread.class);
  private ResultViewPanel resultPanel;

  private Future<List<Match>> futureMatches;

  private AsyncWebResource res;

  private PagedResultQuery query;

  private SearchUI ui;

  ResultFetchThread(PagedResultQuery query, ResultViewPanel resultPanel, SearchUI ui)
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

  public void abort()
  {
    if (futureMatches != null && !futureMatches.isDone())
    {
      futureMatches.cancel(true);
    }
    interrupt();
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

    SaltURIGroupSet saltURIs = new SaltURIGroupSet();

    ListIterator<Match> it = matchesToPrepare.listIterator();
    int i = 0;
    while (it.hasNext())
    {
      Match m = it.next();
      SaltURIGroup urisForMatch = new SaltURIGroup();

      for (String s : m.getSaltIDs())
      {
        try
        {
          urisForMatch.getUris().add(new URI(s));
        }
        catch (URISyntaxException ex)
        {
          log.error(null, ex);
        }
      }
      saltURIs.getGroups().put(++i, urisForMatch);
    }

    subgraphQuery.setMatches(saltURIs);
    return subgraphQuery;
  }

  
  @Override
  public void run()
  {
    WebResource subgraphRes =
      Helper.getAnnisWebResource().path("query/search/subgraph");

    List<Match> result = null;
  
    try
    {
      if (isInterrupted())
      {
        return;
      }
      ui.access(new Runnable()
      {
        @Override
        public void run()
        {
          resultPanel.showMatchSearchInProgress(query);
          ui.push();
        }
      });

      // get the matches
      result = futureMatches.get(60, TimeUnit.SECONDS);

      // get the subgraph for each match
      if (result.isEmpty())
      {
        if (isInterrupted())
        {
          return;
        }
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
        if (isInterrupted())
          {
            return;
          }
        ui.access(new Runnable()
        {
          @Override
          public void run()
          {
            resultPanel.showSubgraphSearchInProgress(query, 0.0f);
            ui.push();
          }
        });
        
        final int totalResultSize = result.size();
        int current = 0;
        
         
        for (Match m : result)
        {
          if (isInterrupted())
          {
            return;
          }
          
          List<Match> subList = new LinkedList<Match>();
          subList.add(m);
          SubgraphQuery subgraphQuery = prepareQuery(subList);
          final SaltProject p = executeQuery(subgraphRes, subgraphQuery);
          
          final float progress = (float) current / (float) totalResultSize;
          
          if (isInterrupted())
          {
            return;
          }
          ui.access(new Runnable()
            {
              @Override
              public void run()
              {
                resultPanel.showSubgraphSearchInProgress(query, progress);
                resultPanel.addQueryResult(query, p);
                ui.push();
              }
            });
          
          current++;
        }
      } // end if no results

      if (isInterrupted())
      {
        return;
      }
      ui.access(new Runnable()
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
      ui.access(new Runnable()
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
          }
        }
      });
    }
    finally
    {
      if (isInterrupted())
      {
        return;
      }
      ui.access(new Runnable()
      {
        @Override
        public void run()
        {
          ui.push();
        }
      });
    }

  }

  private static class MatchListType extends GenericType<List<Match>>
  {

    public MatchListType()
    {
    }
  }
}
