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

import static annis.model.AnnisConstants.ANNIS_NS;
import static annis.model.AnnisConstants.FEAT_MATCHEDNODE;

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
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.SFeature;

import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.escape.Escaper;
import com.google.common.eventbus.EventBus;
import com.google.common.net.UrlEscapers;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.ui.UI;

import annis.CommonHelper;
import annis.exceptions.AnnisCorpusAccessException;
import annis.exceptions.AnnisQLSemanticsException;
import annis.exceptions.AnnisQLSyntaxException;
import annis.libgui.Helper;
import annis.libgui.exporter.ExporterPlugin;
import annis.model.Annotation;
import annis.service.objects.AnnisAttribute;
import annis.service.objects.CorpusConfig;
import annis.service.objects.Match;
import annis.service.objects.MatchGroup;
import annis.service.objects.QueryLanguage;
import annis.service.objects.SubgraphFilter;

public abstract class GeneralTextExporter implements ExporterPlugin, Serializable
{
  private final static Escaper urlPathEscape = UrlEscapers.urlPathSegmentEscaper();
  
  
  @Override
  public Exception convertText(String queryAnnisQL, QueryLanguage queryLanguage, int contextLeft, int contextRight,
    Set<String> corpora, List<String> keys, String argsAsString, boolean alignmc, 
    WebResource annisResource, Writer out, EventBus eventBus, Map<String, CorpusConfig> corpusConfigs, UI ui)
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
        .queryParam("query-language", queryLanguage.name())
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

            Stopwatch stopwatch = Stopwatch.createUnstarted();
            stopwatch.start();
            SaltProject p = res.post(SaltProject.class, currentMatches);
            stopwatch.stop();

            // dynamically adjust the number of items to fetch if single subgraph
            // export was fast enough
            if(stopwatch.elapsed(TimeUnit.MILLISECONDS) < 500 && stepSize < 50)
            {
              stepSize += 10;
            }

            convertText(p, 
              keys, args, out, offset-currentMatches.getMatches().size(), ui);

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
          convertText(p,
            keys, args, out, offset - currentMatches.getMatches().size() - 1, ui);
        }
        offset = 0;
        
      }
      
      out.append("\n");
      out.append("\n");
      out.append("finished");
      
      return null;

    }
    catch (AnnisQLSemanticsException | AnnisQLSyntaxException 
      | AnnisCorpusAccessException | UniformInterfaceException| IOException ex)
    {
      return ex;
    }
  }

  public void convertText(SaltProject queryResult, List<String> keys,
    Map<String, String> args, Writer out, int offset, UI ui) throws IOException
  {
    Map<String, Map<String, Annotation>> metadataCache = new HashMap<>();
    
    List<String> metaKeys = new LinkedList<>();
    if(args.containsKey("metakeys"))
    {
      Iterable<String> it = 
        Splitter.on(",").trimResults().split(args.get("metakeys"));
      for(String s : it)
      {
        metaKeys.add(s);
      }
    }
    
    int counter = 0;
    
    for (SCorpusGraph corpusGraph : queryResult.getCorpusGraphs())
    {
      for (SDocument doc : corpusGraph.getDocuments())
      {
        SDocumentGraph graph = doc.getDocumentGraph();
        
        counter++;
        out.append((counter + offset) + ". ");
        List<SToken> tok = graph.getSortedTokenByText();

        for (SToken annisNode : tok)
        {
           SFeature featMatched = annisNode.getFeature(ANNIS_NS, FEAT_MATCHEDNODE);
           Long match = featMatched == null ? null : featMatched.
                    getValue_SNUMERIC();
            
          if (match != null)
          {
            out.append("[");
            out.append(graph.getText(annisNode));
            out.append("]");
          }
          else
          {
            out.append(graph.getText(annisNode));
          }

          out.append(" ");

        }
        out.append("\n");

        if(!metaKeys.isEmpty())
        {
          String[] path = CommonHelper.getCorpusPath(corpusGraph, doc).toArray(new String[0]);
          appendMetaData(out, metaKeys, path[path.length-1], path[0], metadataCache, ui);
        }
        out.append("\n");

      }
    }

  }

  public void appendMetaData(Writer out, 
    List<String> metaKeys,
    String toplevelCorpus, String documentName,
    Map<String, Map<String, Annotation>> metadataCache, UI ui)
    throws IOException
  {
    Map<String, Annotation> metaData = new HashMap<>();
    if(metadataCache.containsKey(toplevelCorpus + ":" + documentName))
    {
      metaData = metadataCache.get(toplevelCorpus + ":" + documentName);
    }
    else
    {
      List<Annotation> asList = Helper.getMetaData(toplevelCorpus, documentName,  ui);
      for(Annotation anno : asList)
      {
        metaData.put(anno.getQualifiedName(), anno);
        metaData.put(anno.getName(), anno);
      }
      metadataCache.put(toplevelCorpus + ":" + documentName, metaData);
    }
    
    for(String key : metaKeys)
    {
      Annotation anno = metaData.get(key);
      if(anno != null)
      {
        out.append("\tmeta::" + key + "\t" + anno.getValue()).append("\n");
      }
    }
  }
  
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
  
  @Override
    public boolean needsContext() {
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
