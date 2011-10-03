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
package annis.gui.exporter;

import annis.exceptions.AnnisCorpusAccessException;
import annis.exceptions.AnnisQLSemanticsException;
import annis.exceptions.AnnisQLSyntaxException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;


import annis.service.AnnisService;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class WekaExporter implements Exporter
{

  private static final long serialVersionUID = -8182635617256833563L;

  @Override
  public void convertText(String queryAnnisQL, int contextLeft, int contextRight, String corpusListAsString, String keysAsString, String argsAsString, AnnisService service, Writer out)
  {
    //this is a full result export
    List<Long> corpusIdList = new LinkedList<Long>();

    for(String corpusId : corpusListAsString.split(","))
    {
      try
      {
        corpusIdList.add(Long.parseLong(corpusId));
      }
      catch(NumberFormatException ex)
      {
        // ignore
      }
    }

    try
    {
      out.append(service.getWeka(corpusIdList, queryAnnisQL));
    }




    catch(AnnisQLSemanticsException ex)
    {
      Logger.getLogger(WekaExporter.class.getName()).log(Level.SEVERE, null, ex);
    }    catch(AnnisQLSyntaxException ex)
    {
      Logger.getLogger(WekaExporter.class.getName()).log(Level.SEVERE, null, ex);
    }    catch(AnnisCorpusAccessException ex)
    {
      Logger.getLogger(WekaExporter.class.getName()).log(Level.SEVERE, null, ex);
    }    catch(RemoteException ex)
    {
      Logger.getLogger(WekaExporter.class.getName()).log(Level.SEVERE, null, ex);
    }    catch(IOException ex)
    {
      Logger.getLogger(WekaExporter.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

}
