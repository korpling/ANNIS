/*
 * Copyright 2017 SFB 632.
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
package annis.utils;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SDominanceRelation;
import org.corpus_tools.salt.common.STextOverlappingRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.GraphTraverseHandler;
import org.corpus_tools.salt.core.SGraph;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class SubgraphExtractor implements GraphTraverseHandler
{
  
  private final List<SNode> reachableNodes = new LinkedList<>();
  
  public static SDocumentGraph extract(SDocumentGraph old, Set<URI> contextToken)
  {
   
    if(contextToken != null && old != null)
    {
      List<SNode> startNodes = new LinkedList<>();
      for(URI id : contextToken)
      {
        SNode tok = old.getNode(id.getFragment());
        if(tok != null && tok instanceof SToken)
        {
          startNodes.add(tok);
        }
      }
      SubgraphExtractor extractor = new SubgraphExtractor();
      old.traverse(startNodes, SGraph.GRAPH_TRAVERSE_TYPE.BOTTOM_UP_DEPTH_FIRST, "SubgraphExtractor", extractor);
      
      // create a new graph and add all original nodes, this will alter the old graph
      SDocumentGraph result = SaltFactory.createSDocumentGraph();
      for(SNode n : extractor.reachableNodes)
      {
        result.addNode(n);
      }
      return result;
      
    }
    
    return null;
  }

  @Override
  public void nodeReached(SGraph.GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode, SRelation<SNode, SNode> relation, SNode fromNode, long order)
  {
    reachableNodes.add(currNode);
  }

  @Override
  public void nodeLeft(SGraph.GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode, SRelation<SNode, SNode> relation, SNode fromNode, long order)
  {
  }

  @Override
  public boolean checkConstraint(SGraph.GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SRelation<SNode, SNode> relation, SNode currNode, long order)
  {
    return relation == null || relation instanceof STextOverlappingRelation;
  }
  
}
