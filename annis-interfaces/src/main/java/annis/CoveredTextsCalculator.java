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

import de.hu_berlin.u.saltnpepper.salt.common.SDocumentGraph;
import de.hu_berlin.u.saltnpepper.salt.common.SDominanceRelation;
import de.hu_berlin.u.saltnpepper.salt.common.SSpanningRelation;
import de.hu_berlin.u.saltnpepper.salt.common.STextualDS;
import de.hu_berlin.u.saltnpepper.salt.common.STextualRelation;
import de.hu_berlin.u.saltnpepper.salt.core.GraphTraverseHandler;
import de.hu_berlin.u.saltnpepper.salt.core.SGraph.GRAPH_TRAVERSE_TYPE;
import de.hu_berlin.u.saltnpepper.salt.core.SNode;
import de.hu_berlin.u.saltnpepper.salt.core.SRelation;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
