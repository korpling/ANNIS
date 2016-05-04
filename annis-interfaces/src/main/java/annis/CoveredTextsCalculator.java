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
package annis;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SDominanceRelation;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.core.GraphTraverseHandler;
import org.corpus_tools.salt.core.SGraph.GRAPH_TRAVERSE_TYPE;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;

/**
 * Traverses the Salt graph and gets the covered {@link STextualDS} for a list
 * of nodes. 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class CoveredTextsCalculator implements GraphTraverseHandler
{
  private Set<STextualDS> texts;

  public CoveredTextsCalculator(SDocumentGraph graph,
    List<SNode> startNodes)
  {
    texts = new LinkedHashSet<STextualDS>();
    if (startNodes.size() > 0)
    {
      graph.traverse(new ArrayList<SNode>(startNodes),
        GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST, "CoveredTextsCalculator",
        (GraphTraverseHandler) this, true);
    }
  }

  @Override
  public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
    SNode currNode, SRelation relation, SNode fromNode, long order)
  {
    if (currNode instanceof STextualDS)
    {
      texts.add((STextualDS) currNode);
    }
  }

  @Override
  public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
    SNode currNode, SRelation relation, SNode fromNode, long order)
  {
  }

  @Override
  public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType,
    String traversalId, SRelation relation, SNode currNode, long order)
  {
    if (relation == null || relation instanceof SDominanceRelation ||
      relation instanceof SSpanningRelation ||
      relation instanceof STextualRelation)
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  public Set<STextualDS> getCoveredTexts()
  {
    return texts;
  }
  
}
