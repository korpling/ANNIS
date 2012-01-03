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

import annis.gui.Helper;
import annis.security.AnnisUser;
import annis.security.IllegalCorpusAccessException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.Application;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thomas
 */
public class AnnisResultQuery implements Serializable
{

  private Set<String> corpora;
  private String aql;
  private Application app;
  private int contextLeft, contextRight;

  public AnnisResultQuery(Set<String> corpora, String aql, int contextLeft,
    int contextRight, Application app)
  {
    this.corpora = corpora;
    this.aql = aql;
    this.contextLeft = contextLeft;
    this.contextRight = contextRight;
  }

  public SaltProject loadBeans(int startIndex, int count, AnnisUser user) throws
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

    SaltProject result = SaltFactory.eINSTANCE.createSaltProject();
    if (app != null)
    {
      WebResource annisResource = Helper.getAnnisWebResource(app);
      try
      {
        result = annisResource.path("search").path("annotate").queryParam("q",
          aql).queryParam("limit", "" + count).queryParam("offset", ""
          + startIndex).queryParam("left", "" + contextLeft).queryParam("right", ""
          + contextRight).get(SaltProject.class);
      }
      catch (UniformInterfaceException ex)
      {
        Logger.getLogger(AnnisResultQuery.class.getName()).log(Level.SEVERE,
          null, ex);
      }
    }
    return result;
  }
}
