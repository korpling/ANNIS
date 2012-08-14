/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.resultview;

import annis.service.objects.Match;
import annis.gui.Helper;
import annis.security.AnnisUser;
import annis.security.IllegalCorpusAccessException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.Application;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class AnnisResultQuery implements Serializable
{
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(AnnisResultQuery.class);

  private Set<String> corpora;
  private String aql;
  private Application app;

  public AnnisResultQuery(Set<String> corpora, String aql, Application app)
  {
    this.corpora = corpora;
    this.aql = aql;
    this.app = app;
  }

  public List<Match> loadBeans(int startIndex, int count, AnnisUser user) throws
    IllegalCorpusAccessException
  {
    // check corpus selection by logged in user

    Set<String> filteredCorpora = new TreeSet<String>(corpora);
    if (user != null)
    {
      filteredCorpora.retainAll(user.getCorpusNameList());
    }

    if (filteredCorpora.size() != corpora.size())
    {
      throw new IllegalCorpusAccessException("illegal corpus access");
    }

    List<Match> result = new LinkedList<Match>();
    if (app != null)
    {
      WebResource annisResource = Helper.getAnnisWebResource(app);
      try
      {
        annisResource = annisResource.path("search").path("find")
          .queryParam("q", aql)
          .queryParam("limit", "" + count)         
          .queryParam("corpora", StringUtils.join(corpora, ","));

       result = annisResource.get(new GenericType<List<Match>>() {});
      }
      catch (UniformInterfaceException ex)
      {
        log.error(
          ex.getResponse().getEntity(String.class), ex);
      }
    }
    return result;
  }
  
  
}
