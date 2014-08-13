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
import annis.model.AnnisNode;
import annis.service.ifaces.AnnisResult;
import annis.service.ifaces.AnnisResultSet;
import annis.service.objects.AnnisAttribute;
import annis.service.objects.Match;
import annis.service.objects.MatchGroup;
import annis.service.objects.SubgraphFilter;
import annis.utils.LegacyGraphConverter;
import com.google.common.base.Stopwatch;
import com.google.common.eventbus.EventBus;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.Writer;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

public abstract class GeneralTextExporter implements Exporter, Serializable
{
  
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(GeneralTextExporter.class);

  @Override
  public void convertText(String queryAnnisQL, int contextLeft, int contextRight,
    Set<String> corpora, String keysAsString, String argsAsString,
    WebResource annisResource, Writer out, EventBus eventBus)
  {
    try
    {
      // int count = service.getCount(corpusIdList, queryAnnisQL);
      
      LinkedList<String> keys = new LinkedList<>();

      if (keysAsString == null || keysAsString.isEmpty())
      {
        // auto set
        keys.add("tok");
        List<AnnisAttribute> attributes = new LinkedList<>();
        
        for(String corpus : corpora)
        {
          attributes.addAll(
            annisResource.path("corpora")
              .path(URLEncoder.encode(corpus, "UTF-8"))
              .path("annotations")
              .queryParam("fetchvalues", "false")
              .queryParam("onlymostfrequentvalues", "false")
              .get(new AnnisAttributeListType())
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

      Map<String, String> args = new HashMap<>();
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

      int stepSize = 10;
      
      // 1. Get all the matches as Salt ID
      InputStream matchStream = annisResource.path("search/find/")
        .queryParam("q", queryAnnisQL)
        .queryParam("corpora", StringUtils.join(corpora, ","))
        .accept(MediaType.TEXT_PLAIN_TYPE)
        .get(InputStream.class);
      
      BufferedReader inReader = new BufferedReader(new InputStreamReader(
        matchStream, "UTF-8"));
      try
      {
        WebResource subgraphRes = annisResource.path("search/subgraph");
        MatchGroup currentMatches = new MatchGroup();
        String currentLine;
        int offset=0;
        // 2. iterate over all matches and get the sub-graph for a group of matches
        while((currentLine = inReader.readLine()) != null)
        {
          
          if(Thread.currentThread().isInterrupted())
          {
            // return from loop and abort export
            break;
          }
          
          Match match = Match.parseFromString(currentLine);

          currentMatches.getMatches().add(match);

          if(currentMatches.getMatches().size() >= stepSize)
          {
            WebResource res = subgraphRes
              .queryParam("left", "" + contextLeft)
              .queryParam("right","" + contextRight);

            SubgraphFilter filter = getSubgraphFilter();
            if(filter != null)
            {
              res = res.queryParam("filter", filter.name());
            }
            // TODO: segmentation?

            Stopwatch stopwatch = new Stopwatch();
            stopwatch.start();
            SaltProject p = res.post(SaltProject.class, currentMatches);
            stopwatch.stop();

            // dynamically adjust the number of items to fetch if single subgraph
            // export was fast enough
            if(stopwatch.elapsed(TimeUnit.MILLISECONDS) < 500 && stepSize < 50)
            {
              stepSize += 10;
            }

            convertText(LegacyGraphConverter.convertToResultSet(p), 
              keys, args, out, offset-currentMatches.getMatches().size());

            currentMatches.getMatches().clear();

            if(eventBus != null)
            {
              eventBus.post(Integer.valueOf(offset+1));
            }
          }
          offset++;
        } // end for each line
        
        // query the left over matches
        if (!currentMatches.getMatches().isEmpty())
        {
          WebResource res = subgraphRes
            .queryParam("left", "" + contextLeft)
            .queryParam("right", "" + contextRight);

          SubgraphFilter filter = getSubgraphFilter();
          if (filter != null)
          {
            res = res.queryParam("filter", filter.name());
          }
        // TODO: segmentation?

          SaltProject p = res.post(SaltProject.class, currentMatches);
          convertText(LegacyGraphConverter.convertToResultSet(p),
            keys, args, out, offset - currentMatches.getMatches().size() - 1);
        }
        offset = 0;
        
      }
      finally
      {
        inReader.close();
      }
      
      out.append("\n");
      out.append("\n");
      out.append("finished");

    }
    catch (AnnisQLSemanticsException | AnnisQLSyntaxException 
      | AnnisCorpusAccessException | RemoteException  ex)
    {
      log.error(
        null, ex);
    }
    catch (IOException ex)
    {
      log.error(
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

  @Override
  public boolean isCancelable()
  {
    return true;
  }
  
  
  
  public abstract SubgraphFilter getSubgraphFilter();

  private static class AnnisAttributeListType extends GenericType<List<AnnisAttribute>>
  {

    public AnnisAttributeListType()
    {
    }
  }
}
