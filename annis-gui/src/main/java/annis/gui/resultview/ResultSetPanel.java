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
import annis.gui.PluginSystem;
import annis.resolver.ResolverEntry;
import annis.resolver.ResolverEntry.ElementType;
import annis.resolver.SingleResolverRequest;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.ui.VerticalLayout;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thomas
 */
public class ResultSetPanel extends VerticalLayout implements ResolverProvider
{

  private Map<HashSet<SingleResolverRequest>, List<ResolverEntry>> cacheResolver;
  public static final String FILESYSTEM_CACHE_RESULT =
    "ResultSetPanel_FILESYSTEM_CACHE_RESULT";
  public List<SingleResultPanel> resultPanelList;

  public ResultSetPanel(SaltProject p, int start, PluginSystem ps,
    Set<String> visibleTokenAnnos, String segmentationName)
  {
    resultPanelList = new LinkedList<SingleResultPanel>();
    cacheResolver =
      Collections.synchronizedMap(
      new HashMap<HashSet<SingleResolverRequest>, List<ResolverEntry>>());

    setWidth("100%");
    setHeight("-1px");

    addStyleName("result-view");

    int i = start;
    for (SCorpusGraph corpusGraph : p.getSCorpusGraphs())
    {
      for (SDocument doc : corpusGraph.getSDocuments())
      {
        SingleResultPanel panel = new SingleResultPanel(doc, i, this, ps,
          visibleTokenAnnos, segmentationName);
        addComponent(panel);
        resultPanelList.add(panel);
        i++;
      }
    }
  }

  @Override
  public ResolverEntry[] getResolverEntries(SDocument doc)
  {
    HashSet<ResolverEntry> visSet = new HashSet<ResolverEntry>();

    // create a request for resolver entries
    HashSet<SingleResolverRequest> resolverRequests =
      new HashSet<SingleResolverRequest>();

    Set<String> nodeLayers = new HashSet<String>();
    
    for (SNode n : doc.getSDocumentGraph().getSNodes())
    {
      for (SLayer layer : n.getSLayers())
      {
        nodeLayers.add(layer.getSName());
      }
    }

    Set<String> edgeLayers = new HashSet<String>();
    for (SRelation e : doc.getSDocumentGraph().getSRelations())
    {
      for (SLayer layer : e.getSLayers())
      {
        try
        {
          edgeLayers.add(layer.getSName());
        }
        catch (NullPointerException ex)
        {
          Logger.getLogger(ResultSetPanel.class.getName()).log(Level.WARNING,
            "NullPointerException when using Salt, was trying to get layer name",
            ex);
        }
      }
    }

    for (String ns : nodeLayers)
    {
      resolverRequests.add(new SingleResolverRequest(doc.getSCorpusGraph().
        getSRootCorpus().get(0).getSName(), ns,
        ElementType.node));
    }
    for (String ns : edgeLayers)
    {
      resolverRequests.add(new SingleResolverRequest(doc.getSCorpusGraph().
        getSRootCorpus().get(0).getSName(), ns,
        ElementType.edge));
    }

    // query with this resolver request and make sure it is unique
    if (cacheResolver.containsKey(resolverRequests))
    {
      visSet.addAll(cacheResolver.get(resolverRequests));
    }
    else
    {
      List<ResolverEntry> resolverList = new LinkedList<ResolverEntry>();

      WebResource resResolver = Helper.getAnnisWebResource(getApplication()).
        path("resolver");

      for (SingleResolverRequest r : resolverRequests)
      {
        List<ResolverEntry> tmp;
        try
        {
          String corpusName = URLEncoder.encode(r.getCorpusName(), "UTF-8");
          String namespace = r.getNamespace();
          String type = r.getType() == null ? null : r.getType().toString();
          if(corpusName != null && namespace != null && type != null)
          {
            WebResource res = resResolver.path(corpusName).path(namespace).path(type);
            try
            {
              tmp = res.get(new GenericType<List<ResolverEntry>>(){});
              resolverList.addAll(tmp);
            }
            catch(Exception ex)
            {
               Logger.getLogger(ResultSetPanel.class.getName())
            .log(Level.SEVERE, "could not query resolver entries: " 
                 + res.toString(), ex);
            }
          }
        }
        catch (Exception ex)
        {
          Logger.getLogger(ResultSetPanel.class.getName())
            .log(Level.SEVERE, null, ex);
        }
      }
      visSet.addAll(resolverList);
      cacheResolver.put(resolverRequests, resolverList);
    }
    // sort everything
    ResolverEntry[] visArray = visSet.toArray(new ResolverEntry[0]);
    Arrays.sort(visArray, new Comparator<ResolverEntry>()
    {

      @Override
      public int compare(ResolverEntry o1, ResolverEntry o2)
      {
        if (o1.getOrder() < o2.getOrder())
        {
          return -1;
        }
        else if (o1.getOrder() > o2.getOrder())
        {
          return 1;
        }
        else
        {
          return 0;
        }
      }
    });
    return visArray;
  }
  
  public void setSegmentationLayer(String segmentationLayer)
  {
    for(SingleResultPanel p : resultPanelList)
    {
      p.setSegmentationLayer(segmentationLayer);
    }
  }

  public void setVisibleTokenAnnosVisible(Set<String> annos)
  {
    for (SingleResultPanel p : resultPanelList)
    {
      p.setVisibleTokenAnnosVisible(annos);
    }
  }
}
