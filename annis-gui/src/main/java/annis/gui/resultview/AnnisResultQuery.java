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

import annis.service.AnnisService;
import annis.service.ifaces.AnnisResult;
import annis.service.ifaces.AnnisResultSet;
import annis.service.objects.AnnisResultSetImpl;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thomas
 */
public class AnnisResultQuery implements Serializable
{
  private List<Long> corpora;
  private String aql;
  private AnnisService service;
  private int contextLeft, contextRight;
  
  public AnnisResultQuery(List<Long> corpora, String aql, int contextLeft, 
    int contextRight, AnnisService service)
  {
    this.corpora = corpora;
    this.aql = aql;
    this.service = service;
    this.contextLeft = contextLeft;
    this.contextRight = contextRight;
  }

  public AnnisResultSet loadBeans(int startIndex, int count)
  { 
    AnnisResultSet result = new AnnisResultSetImpl();
    if(service != null)
    {
      try
      {
        result = service.getResultSet(corpora, aql, count, startIndex, contextLeft, contextRight);
      }
      catch(RemoteException ex)
      {
        Logger.getLogger(AnnisResultQuery.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    return result;
  }
  
}
