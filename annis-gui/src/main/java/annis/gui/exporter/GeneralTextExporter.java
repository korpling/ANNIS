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
import annis.gui.Helper;
import annis.gui.resultview.AnnisResultQuery;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import annis.model.AnnisNode;
import annis.service.AnnisService;
import annis.service.objects.AnnisAttribute;
import annis.service.objects.AnnisCorpus;
import annis.service.ifaces.AnnisResult;
import annis.service.ifaces.AnnisResultSet;
import annis.utils.LegacyGraphConverter;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

public class GeneralTextExporter implements Exporter, Serializable
{

  @Override
  public void convertText(String queryAnnisQL, int contextLeft, int contextRight,
    Map<String, AnnisCorpus> corpora, String keysAsString, String argsAsString,
    AnnisService service, WebResource annisResource, Writer out)
  {
    try
    {
      // int count = service.getCount(corpusIdList, queryAnnisQL);
      SaltProject queryResult = null;

      LinkedList<String> keys = new LinkedList<String>();

      if (keysAsString == null)
      {
        // auto set
        keys.add("tok");
        List<AnnisAttribute> attributes = new LinkedList<AnnisAttribute>();
        
        for(String corpus : corpora.keySet())
        {
          attributes.addAll(
            annisResource.path("corpora").path(corpus).path("annotations")
              .queryParam("fetchvalues", "false")
              .queryParam("onlymostfrequentvalues", "false")
              .get(new GenericType<List<AnnisAttribute>>() {})
          );
        }
        
        for (AnnisAttribute a : attributes)
        {
          if (a.getName() != null)
          {
            String[] namespaceAndName = a.getName().split(":", 2);
            if (namespaceAndName.length > 1)
            {
              keys.add(namespaceAndName[1]);
            }
            else
            {
              keys.add(namespaceAndName[0]);
            }
          }
        }
      }
      else
      {
        // manually specified
        String[] keysSplitted = keysAsString.split("\\,");
        for (String k : keysSplitted)
        {
          keys.add(k.trim());
        }
      }

      Map<String, String> args = new HashMap<String, String>();
      for (String s : argsAsString.split("&"))
      {
        String[] splitted = s.split("=", 2);
        String key = splitted[0];
        String val = "";
        if (splitted.length > 1)
        {
          val = splitted[1];
        }
        args.put(key, val);
      }

      int offset = 0;
      while (offset == 0 || (queryResult != null
        && queryResult.getSCorpusGraphs().size() > 0))
      {

        try
        {
          queryResult = annisResource.path("search").path("annotate")
            .queryParam("q", queryAnnisQL)
            .queryParam("limit", "" + 50)
            .queryParam("offset", "" + offset)
            .queryParam("left", "" + contextLeft)
            .queryParam("right", "" + contextRight)
            .queryParam("corpora", StringUtils.join(corpora.keySet(), ","))
            .get(SaltProject.class);
        }
        catch (UniformInterfaceException ex)
        {
          Logger.getLogger(AnnisResultQuery.class.getName()).log(Level.SEVERE,
            ex.getResponse().getEntity(String.class), ex);
        }


        convertText(LegacyGraphConverter.convertToResultSet(queryResult), keys,
          args, out, offset);

        out.flush();
        offset = offset + 50;

      }

      out.append("\n");
      out.append("\n");
      out.append("finished");

    }
    catch (AnnisQLSemanticsException ex)
    {
      Logger.getLogger(GeneralTextExporter.class.getName()).log(Level.SEVERE,
        null, ex);
    }
    catch (AnnisQLSyntaxException ex)
    {
      Logger.getLogger(GeneralTextExporter.class.getName()).log(Level.SEVERE,
        null, ex);
    }
    catch (AnnisCorpusAccessException ex)
    {
      Logger.getLogger(GeneralTextExporter.class.getName()).log(Level.SEVERE,
        null, ex);
    }
    catch (RemoteException ex)
    {
      Logger.getLogger(GeneralTextExporter.class.getName()).log(Level.SEVERE,
        null, ex);
    }
    catch (IOException ex)
    {
      Logger.getLogger(GeneralTextExporter.class.getName()).log(Level.SEVERE,
        null, ex);
    }
  }

  public void convertText(AnnisResultSet queryResult, LinkedList<String> keys,
    Map<String, String> args, Writer out, int offset) throws IOException
  {
    int counter = 0;
    for (AnnisResult annisResult : queryResult)
    {
      Set<Long> matchedNodeIds = annisResult.getGraph().getMatchedNodeIds();

      counter++;
      out.append((counter + offset) + ". ");
      List<AnnisNode> tok = annisResult.getGraph().getTokens();

      for (AnnisNode annisNode : tok)
      {
        Long tokID = annisNode.getId();
        if (matchedNodeIds.contains(tokID))
        {
          out.append("[");
          out.append(annisNode.getSpannedText());
          out.append("]");
        }
        else
        {
          out.append(annisNode.getSpannedText());
        }

        //for (Annotation annotation : annisNode.getNodeAnnotations()){
        //      out.append("/"+annotation.getValue());
        //}

        out.append(" ");

      }
      out.append("\n");
    }
  }
}
