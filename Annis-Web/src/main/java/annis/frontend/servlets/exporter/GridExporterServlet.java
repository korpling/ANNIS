/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.frontend.servlets.exporter;

import java.io.IOException;
import javax.servlet.http.*;

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.service.ifaces.AnnisResult;
import annis.service.ifaces.AnnisResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GridExporterServlet extends GeneralTextExporter
{

  @Override
  public void convertText(AnnisResultSet queryResult, List<String> keys,
    Map<String, String[]> httpArgs, HttpServletResponse response, int offset) throws IOException
  {


    boolean showNumbers = true;
    if (httpArgs.containsKey("numbers"))
    {
      String[] arg = httpArgs.get("numbers");
      if (arg[0].equalsIgnoreCase("false")
        || arg[0].equalsIgnoreCase("0")
        || arg[0].equalsIgnoreCase("off"))
      {
        showNumbers = false;
      }
    }

    int counter = 0;
    for (AnnisResult annisResult : queryResult)
    {
      HashMap<String, TreeMap<Long, Span>> annos =
        new HashMap<String, TreeMap<Long, Span>>();

      counter++;
      response.getWriter().append((counter + offset) + ". ");

      long tokenOffset = annisResult.getGraph().getTokens().get(0).getTokenIndex() - 1;
      for (AnnisNode resolveNode : annisResult.getGraph().getNodes())
      {

        for (Annotation resolveAnnotation : resolveNode.getNodeAnnotations())
        {
          String k = resolveAnnotation.getName();
          if (annos.get(k) == null)
          {
            annos.put(k, new TreeMap<Long, Span>());
          }

          // create a separate span for every annotation
          annos.get(k).put(resolveNode.getLeftToken(), new Span(resolveNode.getLeftToken(), resolveNode.getRightToken(),
            resolveAnnotation.getValue()));
          
        }
      }

      for (String k : keys)
      {

        if ("tok".equals(k))
        {
          response.getWriter().append("\t " + k + "\t ");
          for (AnnisNode annisNode : annisResult.getGraph().getTokens())
          {
            response.getWriter().append(annisNode.getSpannedText() + " ");
          }
          response.getWriter().append("\n");
        }
        else
        {
          if(annos.get(k) != null)
          {
            response.getWriter().append("\t " + k + "\t ");
            for(Span s : annos.get(k).values())
            {

              response.getWriter().append(s.getValue());

              if (showNumbers)
              {
                long leftIndex = Math.max(1, s.getStart() - tokenOffset);
                long rightIndex = s.getEnd() - tokenOffset;
                response.getWriter().append("[" + leftIndex
                  + "-" + rightIndex + "]");
              }
              response.getWriter().append(" ");

            }
            response.getWriter().append("\n");
          }
        }
      }

      response.getWriter().append("\n\n");
    }
  }


  private class Span
  {

    private long start;
    private long end;
    private String value;

    public Span(long start, long end, String value)
    {
      this.start = start;
      this.end = end;
      this.value = value;
    }

    public long getStart()
    {
      return start;
    }

    public void setStart(long start)
    {
      this.start = start;
    }

    public long getEnd()
    {
      return end;
    }

    public void setEnd(long end)
    {
      this.end = end;
    }

    public String getValue()
    {
      return value;
    }

    public void setValue(String value)
    {
      this.value = value;
    }
  }
}
