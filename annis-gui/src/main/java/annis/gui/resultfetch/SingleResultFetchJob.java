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
package annis.gui.resultfetch;

import annis.gui.SearchUI;
import static annis.gui.resultfetch.ResultFetchJob.log;
import annis.gui.objects.PagedResultQuery;
import annis.gui.resultview.ResultViewPanel;
import annis.gui.resultview.VisualizerContextChanger;
import annis.libgui.Helper;
import annis.service.objects.Match;
import annis.service.objects.MatchGroup;
import annis.service.objects.SubgraphFilter;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import java.util.LinkedList;
import java.util.List;

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
public class SingleResultFetchJob extends AbstractResultFetchJob implements
  Runnable
{

  private VisualizerContextChanger visContextChanger;

  private Match match;

  private PagedResultQuery query;

  public SingleResultFetchJob(Match match, PagedResultQuery query,
    VisualizerContextChanger visContextChanger)
  {
    this.match = match;
    this.query = query;
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

    List<Match> subList = new LinkedList<>();
    subList.add(match);
    SaltProject p = executeQuery(subgraphRes,
      new MatchGroup(subList),
      query.getContextLeft(), query.getContextRight(),
      query.getSegmentation(), SubgraphFilter.all);

    visContextChanger.updateResult(p, query);

    if (Thread.interrupted())
    {
      return;
    }
  }
}
