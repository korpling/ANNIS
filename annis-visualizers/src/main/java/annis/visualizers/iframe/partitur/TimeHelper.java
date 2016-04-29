/*
 * Copyright 2012 SFB 632.
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
package annis.visualizers.iframe.partitur;

import annis.model.AnnisNode;
import annis.model.Annotation;
import java.util.List;

/**
 *
 * @author benjamin
 */
public class TimeHelper
{

  private List<AnnisNode> token;

  // only for testing
  public TimeHelper()
  {
  }

  public TimeHelper(List<AnnisNode> token)
  {
    this.token = token;
  }

  public String getStartTime(String time)
  {
    return getTimePosition(time, true);
  }

  public String getEndTime(String time)
  {
    return getTimePosition(time, false);
  }

  /**
   * Split a time annotation s.ms-(s.ms)? Whether the flag first is set to true, 
   * we return the first value, otherwise we did try to return the second. The 
   * end time don't have to be annotated, in this case it returns "undefined". 
   * Without a defined start time the result is an empty String "undefined". If 
   * there is no time anno, it returns undefined
   * 
   * @return "undefined", when time parameter is undefined
   */
  private String getTimePosition(String time, boolean first)
  {

    if (time == null)
    {
      return "undefined";
    }

    String[] splittedTimeAnno = time.split("-");
    if (splittedTimeAnno.length > 1)
    {
      if (first)
      {
        return splittedTimeAnno[0].equals("") ? "undefined"
          : splittedTimeAnno[0];
      }
      else
      {
        return splittedTimeAnno[1].equals("") ? "undefined"
          : splittedTimeAnno[1];
      }
    }

    if (first)
    {
      return splittedTimeAnno[0].equals("") ? "undefined" : splittedTimeAnno[0];
    }

    // if we want the end time, return undefined.
    return "undefined";
  }

  String getTimeAnnotation(AnnisNode node)
  {
    for (Annotation anno : node.getNodeAnnotations())
    {
      if (anno.getName().equals("time"))
      {
        return anno.getValue();
      }
    }
    return "";
  }

  public String getTimeAnno(AnnisNode leftNode, AnnisNode rightNode)
  {
    String startTime = getStartTime(getTimeAnnotation(leftNode));
    String endTime = getEndTime(getTimeAnnotation(rightNode));

    // when no start time is given, we do not have to add something
    if ("undefined".equals(startTime))
    {
      return "";
    }

    // search for an end time in the result set, so we have the chance to stop 
    // the media    
    if ("undefined".equals(endTime))
    {
      endTime = getNextEndTime(rightNode);
    }

    return ("time=\"" + startTime + "-" + endTime + "\"");
  }

  private String getNextEndTime(AnnisNode rightNode)
  {
    int offset = getOffset(rightNode);
    String time = null;

    for (long i = offset + 1; i < token.size(); i++)
    {
      for (Annotation anno : token.get((int) i).getNodeAnnotations())
      {
        if ("time".equals(anno.getName()))
        {
          time = anno.getValue();
          break;
        }
      }

      String startTime = getStartTime(time);
      String endTime = getEndTime(time);

      if (startTime != null && !"undefined".equals(startTime))
      {
        return startTime;
      }

      if (endTime != null && !"undefined".equals(endTime))
      {
        return endTime;
      }
    }
    return "undefined";
  }

  private int getOffset(AnnisNode rightNode)
  {
    for (int i = 0; i < token.size(); i++)
    {
      if (rightNode == token.get(i))
      {
        return i;
      }
    }

    return 0;
  }
}
