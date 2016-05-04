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
package annis.libgui.media;


import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SGraph;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.util.SaltUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for getting time annotations on {@link SSpan} from
 * the covered token.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class TimeHelper
{
  
  private final static Logger log = LoggerFactory.getLogger(TimeHelper.class);

  /**
   * Get start and end time from the overlapped {@link SToken}. The minimum
   * start time and maximum end times are choosen.
   * @param node The span to start from.
   * @return A double array, will have length of 0 if no time annotations where found,
   *         one element of only start elements where found, 2 elements if both
   *         start and end time are found.
   */
  public static double[] getOverlappedTime(SNode node)
  {
    SGraph graph = node.getGraph();

    final List<Double> startTimes = new LinkedList<>();
    final List<Double> endTimes = new  LinkedList<>();
    
    List<SToken> token = new LinkedList<>();
    if(node instanceof SToken)
    {
      token.add((SToken) node);
    }
    else
    {
      List<SRelation<SNode,SNode>> outRelations = graph.getOutRelations(node.getId());
      if (outRelations != null)
      {
        for (SRelation<? extends SNode, ? extends SNode> e : outRelations)
        {
          if (e instanceof SSpanningRelation)
          {
            SToken tok = ((SSpanningRelation) e).getTarget();
            token.add(tok);
          }
        }
      } // end for each out relations
    }

    for (SToken tok : token)
    {

      SAnnotation anno = tok.getAnnotation(SaltUtil.createQName("annis", "time"));
      if (anno != null
        && anno.getValue_STEXT() != null
        && !anno.getValue_STEXT().isEmpty() 
        && !anno.getValue_STEXT().matches("\\-[0-9]*(\\.[0-9]*)?"))
      {
        try
        {
          String[] split = anno.getValue_STEXT().split("-");
          if (split.length == 1)
          {
            startTimes.add(Double.parseDouble(split[0]));
          }
          if (split.length == 2)
          {
            startTimes.add(Double.parseDouble(split[0]));
            endTimes.add(Double.parseDouble(split[1]));
          }
        }
        catch(NumberFormatException ex) {
          log.debug("Invalid time annotation", ex);
        }
      }


    } // end for each token
    
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
