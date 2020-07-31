/*
 * Copyright 2013 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package annis.libgui;

import annis.resolver.SingleResolverRequest;
import com.vaadin.ui.UI;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.corpus_tools.annis.api.model.CorpusConfiguration;
import org.corpus_tools.annis.api.model.VisualizerRule;
import org.corpus_tools.annis.api.model.VisualizerRule.ElementEnum;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
public class ResolverProviderImpl implements ResolverProvider, Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 5170826828759716453L;

  private final static Logger log = LoggerFactory.getLogger(ResolverProviderImpl.class);

  private Map<HashSet<SingleResolverRequest>, LinkedHashSet<VisualizerRule>> cacheResolver;

  public ResolverProviderImpl(
      Map<HashSet<SingleResolverRequest>, LinkedHashSet<VisualizerRule>> cacheResolver) {
    this.cacheResolver = cacheResolver;
  }

  @Override
  public List<VisualizerRule> getResolverEntries(SDocument doc, UI ui) {


    // create a request for resolver entries
    HashSet<SingleResolverRequest> resolverRequests = new HashSet<>();

    Set<String> nodeLayers = new HashSet<String>();
    Set<String> edgeLayers = new HashSet<String>();

    if (doc != null && doc.getDocumentGraph() != null) {
      for (SNode n : doc.getDocumentGraph().getNodes()) {
        for (SLayer layer : n.getLayers()) {
          nodeLayers.add(layer.getName());
        }
      }

      for (SRelation<SNode, SNode> e : doc.getDocumentGraph().getRelations()) {
        for (SLayer layer : e.getLayers()) {
          try {
            edgeLayers.add(layer.getName());
          } catch (NullPointerException ex) {
            log.warn("NullPointerException when using Salt, was trying to get layer name", ex);
          }
        }
      }

    }

    String corpusName = doc.getGraph().getRoots().get(0).getName();

    for (String ns : nodeLayers) {
      resolverRequests.add(new SingleResolverRequest(corpusName, ns, ElementEnum.NODE));
    }
    for (String ns : edgeLayers) {
      resolverRequests.add(new SingleResolverRequest(corpusName, ns, ElementEnum.EDGE));
    }
    LinkedHashSet<VisualizerRule> matchingRules = new LinkedHashSet<>();
    // query with this resolver request and make sure it is unique
    if (cacheResolver.containsKey(resolverRequests)) {
      matchingRules.addAll(cacheResolver.get(resolverRequests));
    } else {

      // Get all rules for the corpus in the corpus graph
      CorpusConfiguration corpusConfig = Helper.getCorpusConfig(corpusName, ui);;

      if (corpusConfig != null && corpusConfig.getVisualizers() != null) {
        for (VisualizerRule visRule : corpusConfig.getVisualizers()) {
          for (SingleResolverRequest r : resolverRequests) {
            if (visRule.getMappings() == null) {
              visRule.setMappings(new LinkedHashMap<>());
            }
            if (visRule.getElement() != null && !visRule.getElement().equals(r.getType())) {
              continue;
            }
            if (visRule.getLayer() != null && !visRule.getLayer().equals(r.getNamespace())) {
              continue;
            }
            matchingRules.add(visRule);
          }
          cacheResolver.put(resolverRequests, matchingRules);
        }
      }
    }

    return new LinkedList<>(matchingRules);
  }
}
