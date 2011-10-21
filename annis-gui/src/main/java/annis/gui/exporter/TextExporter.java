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
package annis.gui.exporter;

import java.io.IOException;

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.service.ifaces.AnnisResult;
import annis.service.ifaces.AnnisResultSet;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TextExporter extends GeneralTextExporter
{

  @Override
  public void convertText(AnnisResultSet queryResult, LinkedList<String> keys, 
    Map<String,String> args, Writer out, int offset) throws IOException
  {
    int counter = 0;
    for (AnnisResult annisResult : queryResult)
    {
      Set<Long> matchedNodeIds = annisResult.getGraph().getMatchedNodeIds();

      counter++;
      out.append((counter+offset) + ". ");
      List<AnnisNode> tok = annisResult.getGraph().getTokens();

      for (AnnisNode annisNode : tok)
      {
        Long tokID = annisNode.getId();
        if (matchedNodeIds.contains(tokID))
        {
          out.append("[");
          out.append(annisNode.getSpannedText());
          out.append("]");
        }
        else
        {
          out.append(annisNode.getSpannedText());
        }

        for (Annotation annotation : annisNode.getNodeAnnotations())
        {
          out.append("/" + annotation.getValue());
        }

        out.append(" ");

      }
      out.append("\n");
    }
  }
}
