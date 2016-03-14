/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis;

import annis.dao.QueryDao;
import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import annis.service.objects.Match;
import annis.service.objects.MatchGroup;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class GraphHelper  {

  
  /**
   * This is a helper function to make it easier to create a correct query data object
   * from a {@link MatchGroup} for a {@link AnnisDao#graph(annis.ql.parser.QueryData)  } query.
   * 
   * @param matchGroup
   * @return 
   */
  public static QueryData createQueryData(MatchGroup matchGroup, QueryDao annisDao)
  {
    QueryData queryData = new QueryData();

    Set<String> corpusNames = new TreeSet<>();

    for(Match m : matchGroup.getMatches())
    {
      // collect list of used corpora and created pseudo QueryNodes for each URI
      List<QueryNode> pseudoNodes = new ArrayList<>(m.getSaltIDs().size());
      for (java.net.URI u : m.getSaltIDs())
      {
        pseudoNodes.add(new QueryNode());
        corpusNames.add(CommonHelper.getCorpusPath(u).get(0));
      }
      queryData.addAlternative(pseudoNodes);
    }
    List<Long> corpusIDs = annisDao.mapCorpusNamesToIds(new LinkedList<>(
      corpusNames));

    queryData.setCorpusList(corpusIDs);

    queryData.addExtension(matchGroup);
    return queryData;
  }
  
}