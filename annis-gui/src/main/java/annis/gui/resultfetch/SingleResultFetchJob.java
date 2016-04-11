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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.corpus_tools.salt.common.SaltProject;

import com.sun.jersey.api.client.WebResource;

import annis.gui.objects.PagedResultQuery;
import annis.libgui.Helper;
import annis.service.objects.Match;
import annis.service.objects.MatchGroup;
import annis.service.objects.SubgraphFilter;

/**
 * Fetches a result which contains only one subgraph. This single query always
 * follows a normal ResultFetchJob and so it is assuming that there already
 * exists a list of matches. That is the reason for not needing to execute the
 * find command and hopefully this query is bit faster.
 *
 * @see ResultFetchJob
 * @see LegacyQueryController
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class SingleResultFetchJob extends AbstractResultFetchJob implements
  Callable<SaltProject>
{

  private final Match match;

  private final PagedResultQuery query;

  public SingleResultFetchJob(Match match, PagedResultQuery query)
  {
    this.match = match;
    this.query = query;
  }

  @Override
  public SaltProject call() throws Exception
  {
    WebResource subgraphRes
      = Helper.getAnnisWebResource().path("query/search/subgraph");

    if (Thread.interrupted())
    {
      return null;
    }

    List<Match> subList = new LinkedList<>();
    subList.add(match);
    SaltProject p = executeQuery(subgraphRes,
      new MatchGroup(subList),
      query.getLeftContext(), query.getRightContext(),
      query.getSegmentation(), SubgraphFilter.all);

    return p;
    
  }
  
}
