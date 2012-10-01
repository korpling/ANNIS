/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.media.impl;

import de.hu_berlin.german.korpling.saltnpepper.salt.graph.GRAPH_TRAVERSE_TYPE;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SGraphTraverseHandler;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class TimeHelper
{

  public static double[] getOverlappedTime(SNode node)
  {
    SGraph graph = node.getSGraph();

    EList<SNode> startNodes = new BasicEList<SNode>();
    startNodes.add(node);
    
    final List<Double> startTimes = new LinkedList<Double>();
    final List<Double> endTimes = new  LinkedList<Double>();
    
    graph.traverse(startNodes, GRAPH_TRAVERSE_TYPE.TOP_DOWN_BREADTH_FIRST, "timehelpertraverse", new SGraphTraverseHandler()
    {
      @Override
      public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode, SRelation sRelation, SNode fromNode, long order)
      {
        SAnnotation anno = currNode.getSAnnotation("annis::time");
        if(anno != null)
        {
          String[] split = anno.getSValueSTEXT().split("-");
          if(split.length == 1)
          {
            startTimes.add(Double.parseDouble(split[0]));
          }
          if(split.length == 2)
          {
            endTimes.add(Double.parseDouble(split[1]));
          }
        }
      }

      @Override
      public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode, SRelation edge, SNode fromNode, long order)
      {
      }

      @Override
      public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SRelation edge, SNode currNode, long order)
      {
        if(currNode != null)
        {
          SAnnotation anno = currNode.getSAnnotation("annis::time");
          if(anno != null)
          {
            return false;
          }
        }
        return true;
      }
    });
    
    if(startTimes.size() > 0 && endTimes.size() > 0)
    {
      return new double[] 
      {
        Collections.min(startTimes), 
        Collections.max(endTimes)
      };
    }
    else if(startTimes.size() > 0)
    {
      return new double[] 
      {
        Collections.min(startTimes)
      };
    }
    

    return new double[0];
  }
}
