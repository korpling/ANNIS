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
import annis.service.objects.SaltURIGroup;
import annis.service.objects.SaltURIGroupSet;
import annis.service.objects.SubgraphFilter;
import annis.service.objects.SubgraphQuery;
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
import java.net.URI;
import java.net.URISyntaxException;
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
      
      LinkedList<String> keys = new LinkedList<String>();

      if (keysAsString == null)
      {
        // auto set
        keys.add("tok");
        List<AnnisAttribute> attributes = new LinkedList<AnnisAttribute>();
        
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

      int stepSize = 10;
      
      // 1. Get all the matches as Salt ID
      InputStream matchStream = annisResource.path("search/find/")
        .queryParam("q", queryAnnisQL)
        .queryParam("corpora", StringUtils.join(corpora, ","))
        .accept(MediaType.TEXT_PLAIN_TYPE)
        .get(InputStream.class);
      
      BufferedReader inReader = new BufferedReader(new InputStreamReader(
        matchStream, "UTF-8"));
      
      WebResource subgraphRes = annisResource.path("search/subgraph");
      SaltURIGroupSet saltURIs = new SaltURIGroupSet();
      String currentLine;
      int offset=0;
      // 2. iterate over all matches and get the sub-graph for a group of matches
      while((currentLine = inReader.readLine()) != null)
      {
        SaltURIGroup urisForMatch = new SaltURIGroup();
        
        for(String uri : currentLine.split(","))
        {
          try
          {
            urisForMatch.getUris().add(new URI(uri));
          }
          catch (URISyntaxException ex)
          {
            log.error(null, ex);
          }
        }
        saltURIs.getGroups().put(offset, urisForMatch);
        
        if(saltURIs.getGroups().size() >= stepSize)
        {
          SubgraphQuery subQuery = new SubgraphQuery();
          subQuery.setLeft(contextLeft);
          subQuery.setRight(contextRight);
          subQuery.setMatches(saltURIs);
          subQuery.setFilter(getSubgraphFilter());
          // TODO: segmentation?
          
          Stopwatch stopwatch = new Stopwatch();
          stopwatch.start();
          SaltProject p = subgraphRes.post(SaltProject.class, subQuery);
          stopwatch.stop();
          
          // dynamically adjust the number of items to fetch single subgraph
          // export was fast enough
          if(stopwatch.elapsed(TimeUnit.MILLISECONDS) < 500)
          {
            stepSize += 10;
          }
          
          convertText(LegacyGraphConverter.convertToResultSet(p), 
            keys, args, out, offset-saltURIs.getGroups().size());
          
          saltURIs.getGroups().clear();
          
          if(eventBus != null)
          {
            eventBus.post(new Integer(offset+1));
          }
        }
        offset++;
      }
      
      if(!saltURIs.getGroups().isEmpty())
      {
        SubgraphQuery subQuery = new SubgraphQuery();
          subQuery.setLeft(contextLeft);
          subQuery.setRight(contextRight);
          subQuery.setMatches(saltURIs);
          subQuery.setFilter(getSubgraphFilter());
          // TODO: segmentation?
          
        SaltProject p = subgraphRes.post(SaltProject.class, subQuery);
          convertText(LegacyGraphConverter.convertToResultSet(p), 
            keys, args, out, offset-saltURIs.getGroups().size()-1);
      }
      offset = 0;
      
      out.append("\n");
      out.append("\n");
      out.append("finished");

    }
    catch (AnnisQLSemanticsException ex)
    {
      log.error(
        null, ex);
    }
    catch (AnnisQLSyntaxException ex)
    {
      log.error(
        null, ex);
    }
    catch (AnnisCorpusAccessException ex)
    {
      log.error(
        null, ex);
    }
    catch (RemoteException ex)
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
  
  public abstract SubgraphFilter getSubgraphFilter();

  private static class AnnisAttributeListType extends GenericType<List<AnnisAttribute>>
  {

    public AnnisAttributeListType()
    {
    }
  }
}
