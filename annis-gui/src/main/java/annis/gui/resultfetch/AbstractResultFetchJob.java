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

import static annis.gui.resultfetch.ResultFetchJob.log;
import annis.service.objects.MatchGroup;
import annis.service.objects.SubgraphFilter;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import org.corpus_tools.salt.common.SaltProject;

/**
 * Asks for salt graphs for a given {@link MatchGroup}.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public abstract class AbstractResultFetchJob
{

  /**
   * Asks for salt graphs with a given list of matches {@link MatchGroup}.
   *
   * @param subgraphRes Handles the REST connection with the annis service.
   * @param matches A list of matches. This implies, that the "find" method of
   * the annis-service was executed before.
   * @param left The left context. 
   * @param right The right context.
   * @param segmentation restrict results to this segmentations
   * @param filter 
   * @return
   */
  final protected SaltProject executeQuery(WebResource subgraphRes,
    MatchGroup matches, int left, int right, String segmentation,
    SubgraphFilter filter)
  {
    SaltProject p = null;
    WebResource res = subgraphRes.queryParam("left", "" + left).queryParam(
      "right", "" + right);
    try
    {
      if (segmentation != null)
      {
        res = res.queryParam("segmentation", segmentation);
      }
      if (filter != null)
      {
        res = res.queryParam("filter", filter.name());
      }
      p = res.post(SaltProject.class, matches);
    }
    catch (UniformInterfaceException ex)
    {
      log.error(ex.getMessage(), ex);
    }

    return p;
  }
}
