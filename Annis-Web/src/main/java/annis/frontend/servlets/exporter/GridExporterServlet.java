/*
 * Copyright 2010 Collaborative Research Centre SFB 632
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
import annis.service.ifaces.AnnisAttribute;
import annis.service.ifaces.AnnisAttributeSet;
import annis.service.ifaces.AnnisResult;
import annis.service.ifaces.AnnisResultSet;
import java.util.List;

public class GridExporterServlet extends GeneralTextExporter
{

  @Override
  public void convertText(AnnisResultSet queryResult, List<String> keys, HttpServletResponse response, int offset) throws IOException
  {
    
    

    int counter = 0;
    for (AnnisResult annisResult : queryResult)
    {

      counter++;
      response.getWriter().append((counter+offset) + ". ");
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

          int count = 0;
          long tokenOffset = annisResult.getGraph().getTokens().get(0).getTokenIndex() -1;
          for (AnnisNode resolveNode : annisResult.getGraph().getNodes())
          {

            for (Annotation resolveAnnotation : resolveNode.getNodeAnnotations())
            {

              if (resolveAnnotation.getName().equals(k))
              {
                if (count == 0)
                {
                  response.getWriter().append("\t " + k + "\t ");
                  count++;
                }
                response.getWriter().append(resolveAnnotation.getValue() );
                long leftIndex = Math.max(1, resolveNode.getLeftToken() - tokenOffset);
                long rightIndex = resolveNode.getRightToken() - tokenOffset;
                response.getWriter().append("[" + leftIndex
                  + "-" + rightIndex + "]");
                response.getWriter().append(" ");
              }
            }
          }
          if (count != 0)
          {
            response.getWriter().append("\n");
          }
        }
      }

      response.getWriter().append("\n\n");
    }
  }
}
