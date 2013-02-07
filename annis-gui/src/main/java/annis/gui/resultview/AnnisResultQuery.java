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
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.Application;
import org.apache.commons.lang3.StringUtils;
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

  public AnnisResultQuery(Set<String> corpora, String aql)
  {
    this.corpora = corpora;
    this.aql = aql;
  }

  public List<Match> loadBeans(int startIndex, int count, AnnisUser user)
  {
    
    List<Match> result = new LinkedList<Match>();

    WebResource annisResource = Helper.getAnnisWebResource();
    try
    {
      annisResource = annisResource.path("query").path("search").path("find")
        .queryParam("q", aql)
        .queryParam("offset", "" + startIndex)
        .queryParam("limit", "" + count)         
        .queryParam("corpora", StringUtils.join(corpora, ","));

      result = annisResource.get(new GenericType<List<Match>>() {});
    }
    catch (UniformInterfaceException ex)
    {
      log.error(
        ex.getResponse().getEntity(String.class), ex);
    }
    catch (ClientHandlerException ex)
    {
      log.error("could not execute REST call to query matches", ex);
    }

    return result;
  }
  
  
}
