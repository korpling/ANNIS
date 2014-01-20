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
import annis.gui.resultview.ResultViewPanel;
import annis.gui.resultview.VisualizerContextChanger;
import annis.libgui.Helper;
import annis.service.objects.Match;
import annis.service.objects.SaltURIGroup;
import annis.service.objects.SaltURIGroupSet;
import annis.service.objects.SubgraphQuery;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Fetches a result which contains only one subgraph. This single query always
 * follows a normal ResultFetchJob and so it is assuming that there already
 * exists a list of matches. That is the reason for not needing to execute the
 * find command and hopefully this query is bit faster.
 *
 * @see ResultFetchJob
 * @see QueryController
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class SingleResultFetchJob implements Runnable
{

  private VisualizerContextChanger visContextChanger;

  private Match match;

  private PagedResultQuery query;

  private ResultViewPanel resultPanel;

  private SearchUI ui;

  public SingleResultFetchJob(Match match, PagedResultQuery query, SearchUI ui,
    ResultViewPanel resultPanel, VisualizerContextChanger visContextChanger)
  {
    this.match = match;
    this.query = query;
    this.ui = ui;
    this.resultPanel = resultPanel;
    this.visContextChanger = visContextChanger;
  }

  @Override
  public void run()
  {
    WebResource subgraphRes
      = Helper.getAnnisWebResource().path("query/search/subgraph");

    if (Thread.interrupted())
    {
      return;
    }

    if (Thread.interrupted())
    {
      return;
    }

    SubgraphQuery subgraphQuery = prepareQuery();
    final SaltProject p = executeQuery(subgraphRes, subgraphQuery);

    visContextChanger.updateResult(p, query);

    if (Thread.interrupted())
    {
      return;
    }
  }

  public SubgraphQuery prepareQuery()
  {
    SubgraphQuery subgraphQuery = new SubgraphQuery();

    subgraphQuery.setLeft(query.getContextLeft());
    subgraphQuery.setRight(query.getContextRight());
    if (query.getSegmentation() != null)
    {
      subgraphQuery.setSegmentationLayer(query.getSegmentation());
    }

    SaltURIGroupSet saltURIs = new SaltURIGroupSet();

    SaltURIGroup urisForMatch = new SaltURIGroup();

    for (String s : match.getSaltIDs())
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

    saltURIs.getGroups().put(1, urisForMatch);
    subgraphQuery.setMatches(saltURIs);
    return subgraphQuery;
  }

  final protected SaltProject executeQuery(WebResource subgraphRes,
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
}
