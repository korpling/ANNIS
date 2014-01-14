/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
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

import static annis.gui.ResultFetchJob.log;
import annis.gui.model.PagedResultQuery;
import annis.gui.paging.PagingComponent;
import annis.gui.resultview.ResultViewPanel;
import annis.gui.resultview.SingleResultPanel;
import annis.libgui.Helper;
import annis.service.objects.Match;
import annis.service.objects.SubgraphQuery;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class SingleResultFetchJob extends ResultFetchJob
{

  private SingleResultPanel singleResultPanel;

  public SingleResultFetchJob(PagedResultQuery query,
    ResultViewPanel resultPanel, SearchUI ui,
    SingleResultPanel singleResultPanel)
  {
    super(query, resultPanel, ui);
    this.singleResultPanel = singleResultPanel;
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

      // get the matches
      result = futureMatches.get(60, TimeUnit.SECONDS);

      if (Thread.interrupted())
      {
        return;
      }

      List<Match> subList = new LinkedList<Match>();
      subList.add(result.get(0)); //only one result is possible
      SubgraphQuery subgraphQuery = prepareQuery(subList);
      final SaltProject p = executeQuery(subgraphRes, subgraphQuery);

      singleResultPanel.updateResult(p);

      if (Thread.interrupted())
      {
        return;
      }
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

}
