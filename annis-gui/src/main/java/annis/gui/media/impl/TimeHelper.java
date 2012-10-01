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

import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Edge;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpanningRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SGraph;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.emf.common.util.EList;

/**
 * Helper class for getting time annotations on {@link SSpan} from
 * the covered token.
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class TimeHelper
{

  /**
   * Get start and end time from the overlapped {@link SToken}. The minimum
   * start time and maximum end times are choosen.
   * @param node The span to start from.
   * @return A double array, will have length of 0 if no time annotations where found,
   *         one element of only start elements where found, 2 elements if both
   *         start and end time are found.
   */
  public static double[] getOverlappedTime(SSpan node)
  {
    SGraph graph = node.getSGraph();

    final List<Double> startTimes = new LinkedList<Double>();
    final List<Double> endTimes = new  LinkedList<Double>();
    
    EList<Edge> outEdges = graph.getOutEdges(node.getSId());
    if(outEdges != null)
    {
      for(Edge e : outEdges)
      {
        if(e instanceof SSpanningRelation)
        {
          SToken tok = ((SSpanningRelation) e).getSToken();
          
          SAnnotation anno = tok.getSAnnotation("annis::time");
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
      }
    } // end for each out edges
    
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
