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

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.Edge;
import annis.resolver.ResolverEntry;
import annis.resolver.ResolverEntry.ElementType;
import annis.resolver.SingleResolverRequest;
import annis.service.AnnisService;
import annis.service.ifaces.AnnisResult;
import annis.service.ifaces.AnnisResultSet;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author thomas
 */
public class ResultSetPanel extends Panel implements ResolverProvider
{
  private HashMap<HashSet<SingleResolverRequest>, List<ResolverEntry>> cacheResolver;
  
  public static final String FILESYSTEM_CACHE_RESULT = "ResultSetPanel_FILESYSTEM_CACHE_RESULT";
  
  public ResultSetPanel(AnnisResultSet resultSet, int start)
  {
    cacheResolver = new HashMap<HashSet<SingleResolverRequest>, List<ResolverEntry>>();
    
    setWidth("100%");
    setHeight("-1px");
    
    ((VerticalLayout) getContent()).setWidth("100%");
    ((VerticalLayout) getContent()).setHeight("-1px");
    
    int i=start; 
    for(AnnisResult r : resultSet)
    {
      SingleResultPanel panel = new SingleResultPanel(r, i, this);
      addComponent(panel);
      i++;
    }
  }

  
  @Override
  public ResolverEntry[] getResolverEntries(AnnisResult result, AnnisService service) throws RemoteException
  {
    HashSet<ResolverEntry> visSet = new HashSet<ResolverEntry>();
    
    long corpusIdFromFirstNode = result.getGraph().getNodes().get(0).getCorpus();

    // create a request for resolver entries
    HashSet<SingleResolverRequest> resolverRequests = new HashSet<SingleResolverRequest>();

    Set<String> nodeNamespaces = new HashSet<String>();
    for(AnnisNode node : result.getGraph().getNodes())
    {
      nodeNamespaces.add(node.getNamespace());
      for(Annotation annotation : node.getNodeAnnotations())
      {
        nodeNamespaces.add(annotation.getNamespace());
      }
    }
    Set<String> edgeNamespaces = new HashSet<String>();
    for(Edge e : result.getGraph().getEdges())
    {
      edgeNamespaces.add(e.getNamespace());
      for(Annotation annotation : e.getAnnotations())
      {
        edgeNamespaces.add(annotation.getNamespace());
      }
    }
    for(String ns : nodeNamespaces)
    {
      resolverRequests.add(new SingleResolverRequest(corpusIdFromFirstNode, ns, ElementType.node));
    }
    for(String ns : edgeNamespaces)
    {
      resolverRequests.add(new SingleResolverRequest(corpusIdFromFirstNode, ns, ElementType.edge));
    }

    // query with this resolver request and make sure it is unique
    if(cacheResolver.containsKey(resolverRequests))
    {
      visSet.addAll(cacheResolver.get(resolverRequests));
    }
    else
    {
      List<ResolverEntry> resolverList =
        service.getResolverEntries(resolverRequests.toArray(new SingleResolverRequest[0]));
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
        if(o1.getOrder() < o2.getOrder())
        {
          return -1;
        }
        else if(o1.getOrder() > o2.getOrder())
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
  
}
