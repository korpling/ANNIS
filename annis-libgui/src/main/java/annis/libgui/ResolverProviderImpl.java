/*
 * Copyright 2013 SFB 632.
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
package annis.libgui;

import annis.resolver.ResolverEntry;
import annis.resolver.SingleResolverRequest;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class ResolverProviderImpl implements ResolverProvider, Serializable
{

  private final static Logger log = LoggerFactory.getLogger(
    ResolverProviderImpl.class);

  private Map<HashSet<SingleResolverRequest>, List<ResolverEntry>> cacheResolver;

  private final static Escaper urlPathEscape = UrlEscapers.
    urlPathSegmentEscaper();

  public ResolverProviderImpl(
    Map<HashSet<SingleResolverRequest>, List<ResolverEntry>> cacheResolver)
  {
    this.cacheResolver = cacheResolver;
  }

  @Override
  public ResolverEntry[] getResolverEntries(SDocument doc)
  {
    HashSet<ResolverEntry> visSet = new HashSet<ResolverEntry>();

    // create a request for resolver entries
    HashSet<SingleResolverRequest> resolverRequests
      = new HashSet<SingleResolverRequest>();

    Set<String> nodeLayers = new HashSet<String>();

    for (SNode n : doc.getDocumentGraph().getNodes())
    {
      for (SLayer layer : n.getLayers())
      {
        nodeLayers.add(layer.getName());
      }
    }

    Set<String> edgeLayers = new HashSet<String>();
    for (SRelation<SNode, SNode> e : doc.getDocumentGraph().getRelations())
    {
      for (SLayer layer : e.getLayers())
      {
        try
        {
          edgeLayers.add(layer.getName());
        }
        catch (NullPointerException ex)
        {
          log.warn(
            "NullPointerException when using Salt, was trying to get layer name",
            ex);
        }
      }
    }

    for (String ns : nodeLayers)
    {
      resolverRequests.add(new SingleResolverRequest(doc.getGraph().
        getRoots().get(0).getName(), ns, ResolverEntry.ElementType.node));
    }
    for (String ns : edgeLayers)
    {
      resolverRequests.add(new SingleResolverRequest(doc.getGraph().
        getRoots().get(0).getName(), ns, ResolverEntry.ElementType.edge));
    }

    // query with this resolver request and make sure it is unique
    if (cacheResolver.containsKey(resolverRequests))
    {
      visSet.addAll(cacheResolver.get(resolverRequests));
    }
    else
    {
      List<ResolverEntry> resolverList = new LinkedList<ResolverEntry>();

      WebResource resResolver = Helper.getAnnisWebResource()
        .path("query").path("resolver");
      for (SingleResolverRequest r : resolverRequests)
      {
        List<ResolverEntry> tmp;

        String corpusName = urlPathEscape.escape(r.getCorpusName());
        String namespace = r.getNamespace();
        String type = r.getType() == null ? null : r.getType().toString();
        if (corpusName != null && namespace != null && type != null)
        {
          WebResource res = resResolver.path(corpusName).path(namespace).
            path(type);
          try
          {
            tmp = res.get(new ResolverEntryListType());
            resolverList.addAll(tmp);
          }
          catch (UniformInterfaceException | ClientHandlerException ex)
          {
            if(!AnnisBaseUI.handleCommonError(ex, "query resolver entries"))
            {
              log.error("could not query resolver entries: "
                + res.toString(), ex);
            }
          }
        }

      }
      visSet.addAll(resolverList);
      cacheResolver.put(resolverRequests, resolverList);
    }
    // sort everything
    ResolverEntry[] visArray = visSet.toArray(new ResolverEntry[visSet.size()]);
    Arrays.sort(visArray, new ResolverEntryComparator());
    return visArray;
  }

  private static class ResolverEntryListType extends GenericType<List<ResolverEntry>>
  {

    public ResolverEntryListType()
    {
    }
  }

  private static class ResolverEntryComparator implements
    Comparator<ResolverEntry>, Serializable
  {

    public ResolverEntryComparator()
    {
    }

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
  }
}
