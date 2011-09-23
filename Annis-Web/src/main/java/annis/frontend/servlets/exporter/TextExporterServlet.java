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
import annis.service.ifaces.AnnisAttributeSet;
import annis.service.ifaces.AnnisResult;
import annis.service.ifaces.AnnisResultSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TextExporterServlet extends GeneralTextExporter
{

  @Override
  public void convertText(AnnisResultSet queryResult, 
    List<String> keys, Map<String,String[]> httpArgs, HttpServletResponse response,
    int offset) throws IOException
  {
    int counter = 0;
    for (AnnisResult annisResult : queryResult)
    {
      Set<Long> matchedNodeIds = annisResult.getGraph().getMatchedNodeIds();

      counter++;
      response.getWriter().append((counter+offset) + ". ");
      List<AnnisNode> tok = annisResult.getGraph().getTokens();

      for (AnnisNode annisNode : tok)
      {
        Long tokID = annisNode.getId();
        if (matchedNodeIds.contains(tokID))
        {
          response.getWriter().append("[");
          response.getWriter().append(annisNode.getSpannedText());
          response.getWriter().append("]");
        }
        else
        {
          response.getWriter().append(annisNode.getSpannedText());
        }

        for (Annotation annotation : annisNode.getNodeAnnotations())
        {
          response.getWriter().append("/" + annotation.getValue());
        }

        response.getWriter().append(" ");

      }
      response.getWriter().append("\n");
    }
  }
}
