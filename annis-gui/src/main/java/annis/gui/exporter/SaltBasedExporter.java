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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SaltProject;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.escape.Escaper;
import com.google.common.eventbus.EventBus;
import com.google.common.net.UrlEscapers;
import com.google.gwt.thirdparty.guava.common.base.Splitter;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import annis.CommonHelper;
import annis.TimelineReconstructor;
import annis.exceptions.AnnisCorpusAccessException;
import annis.exceptions.AnnisQLSemanticsException;
import annis.exceptions.AnnisQLSyntaxException;
import annis.libgui.Helper;
import annis.libgui.exporter.ExporterPlugin;
import annis.service.objects.AnnisAttribute;
import annis.service.objects.CorpusConfig;
import annis.service.objects.Match;
import annis.service.objects.MatchGroup;
import annis.service.objects.SubgraphFilter;

/**
 * An abstract base class for exporters that use Salt subgraphs to produce
 * some kind of textual output.
 * @author thomas
 */
public abstract class SaltBasedExporter implements ExporterPlugin, Serializable
{
  
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(SaltBasedExporter.class);

  private final static Escaper urlPathEscape = UrlEscapers.urlPathSegmentEscaper();
  
  @Override
  public Exception convertText(String queryAnnisQL, int contextLeft, int contextRight,
    Set<String> corpora, List<String> keys, String argsAsString,
    WebResource annisResource, Writer out, EventBus eventBus, Map<String, CorpusConfig> corpusConfigs)
  {
    try
    {
      
      // int count = service.getCount(corpusIdList, queryAnnisQL);
      
      if (keys == null || keys.isEmpty())
      {
        // auto set
        keys = new LinkedList<>();
        keys.add("tok");
        List<AnnisAttribute> attributes = new LinkedList<>();
        
        for(String corpus : corpora)
        {
          attributes.addAll(
            annisResource.path("corpora")
              .path(urlPathEscape.escape(corpus))
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

      Map<String, String> args = new HashMap<>();
      for (String s : argsAsString.split("&|;"))
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
        .queryParam("q", Helper.encodeJersey(queryAnnisQL))
        .queryParam("corpora", StringUtils.join(corpora, ","))
        .accept(MediaType.TEXT_PLAIN_TYPE)
        .get(InputStream.class);
      
     
      try(BufferedReader inReader = new BufferedReader(new InputStreamReader(
        matchStream, "UTF-8")))
      {
        WebResource subgraphRes = annisResource.path("search/subgraph");
        MatchGroup currentMatches = new MatchGroup();
        String currentLine;
        int offset=0;
        // 2. iterate over all matches and get the sub-graph for a group of matches
        while(!Thread.currentThread().isInterrupted() 
          && (currentLine = inReader.readLine()) != null)
        { 
          Match match = Match.parseFromString(currentLine);

          currentMatches.getMatches().add(match);

          if(currentMatches.getMatches().size() >= stepSize)
          {
            WebResource res = subgraphRes
              .queryParam("left", "" + contextLeft)
              .queryParam("right","" + contextRight);
            
            if(args.containsKey("segmentation"))
            {
              res = res.queryParam("segmentation", args.get("segmentation"));
            }

            SubgraphFilter filter = getSubgraphFilter();
            if(filter != null)
            {
              res = res.queryParam("filter", filter.name());
            }

            Stopwatch stopwatch = Stopwatch.createStarted();
            SaltProject p = res.post(SaltProject.class, currentMatches);
            stopwatch.stop();

            // dynamically adjust the number of items to fetch if single subgraph
            // export was fast enough
            if(stopwatch.elapsed(TimeUnit.MILLISECONDS) < 500 && stepSize < 50)
            {
              stepSize += 10;
            }

            convertSaltProject(p, keys, args, offset-currentMatches.getMatches().size(), corpusConfigs, out);

            currentMatches.getMatches().clear();

            if(eventBus != null)
            {
              eventBus.post(offset+1);
            }
          }
          offset++;
        } // end for each line
        
        if (Thread.interrupted())
        {
          return new InterruptedException("Exporter job was interrupted");
        }
        
        // query the left over matches
        if (!currentMatches.getMatches().isEmpty())
        {
          WebResource res = subgraphRes
            .queryParam("left", "" + contextLeft)
            .queryParam("right", "" + contextRight);
          if(args.containsKey("segmentation"))
          {
            res = res.queryParam("segmentation", args.get("segmentation"));
          }

          SubgraphFilter filter = getSubgraphFilter();
          if (filter != null)
          {
            res = res.queryParam("filter", filter.name());
          }

          SaltProject p = res.post(SaltProject.class, currentMatches);
          convertSaltProject(p, keys, args, offset - currentMatches.getMatches().size() - 1,
              corpusConfigs, out);
        }
        offset = 0;
        
      }
      
      out.append("\n");
      
      return null;

    }
    catch (AnnisQLSemanticsException | AnnisQLSyntaxException 
      | AnnisCorpusAccessException | UniformInterfaceException| IOException ex)
    {
      return ex;
    }
  }
  
  /**
   * Iterates over all matches (modelled as corpus graphs) and calls {@link #convertText(de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph, java.util.List, java.util.Map, int, java.io.Writer) } for
   * the single document graph.
   * @param p
   * @param annoKeys
   * @param args
   * @param offset
   * @param out 
   */
  private void convertSaltProject(SaltProject p, List<String> annoKeys, Map<String, String> args, int offset,
      Map<String, CorpusConfig> corpusConfigs, Writer out) throws IOException
  {
    int matchNumber = offset;
    if(p != null && p.getCorpusGraphs() != null)
    {
      
      Map<String, String> spanAnno2order = null;
      boolean virtualTokenizationFromNamespace = false;
      
      Set<String> corpusNames = CommonHelper.getToplevelCorpusNames(p);
      if(!corpusNames.isEmpty())
      {
        CorpusConfig config = corpusConfigs.get(corpusNames.iterator().next());
        if(config != null)
        {
          if("true".equalsIgnoreCase(config.getConfig("virtual_tokenization_from_namespace")))
          {
            virtualTokenizationFromNamespace = true;
          }
          else
          {
            String mappingRaw = config.getConfig("virtual_tokenization_mapping");
            if(mappingRaw != null)
            {
              spanAnno2order = new HashMap<>();
              for(String singleMapping : Splitter.on(',').split(mappingRaw))
              {
                List<String> mappingParts = Splitter.on('=').splitToList(singleMapping);
                if(mappingParts.size() >= 2)
                {
                  spanAnno2order.put(mappingParts.get(0), mappingParts.get(1));
                }
              }
            }
          }
         
        }
      }
      
      for(SCorpusGraph corpusGraph : p.getCorpusGraphs())
      {
        if(corpusGraph.getDocuments() != null)
        {
          for(SDocument doc : corpusGraph.getDocuments())
          {
            if(virtualTokenizationFromNamespace)
            {
              TimelineReconstructor.removeVirtualTokenizationUsingNamespace(doc.getDocumentGraph());
            }
            else if(spanAnno2order != null)
            {
              // there is a definition how to map the virtual tokenization to a real one
              TimelineReconstructor.removeVirtualTokenization(doc.getDocumentGraph(), spanAnno2order);
            }
            
            convertText(doc.getDocumentGraph(), annoKeys, args, matchNumber, out);
          }
        }
      }
      matchNumber++;
    }
  }

  public abstract void convertText(SDocumentGraph graph, List<String> annoKeys, Map<String, String> args, int matchNumber,
    Writer out) throws IOException;

  @Override
  public boolean isCancelable()
  {
    return true;
  }
  
  @Override
  public String getFileEnding()
  {
    return "txt";
  }
  
  
  public abstract SubgraphFilter getSubgraphFilter();

  private static class AnnisAttributeListType extends GenericType<List<AnnisAttribute>>
  {

    public AnnisAttributeListType()
    {
    }
  }
}
