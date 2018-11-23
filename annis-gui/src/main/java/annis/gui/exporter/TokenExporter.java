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

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.service.ifaces.AnnisResult;
import annis.service.ifaces.AnnisResultSet;
import annis.service.objects.SubgraphFilter;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import com.google.common.base.Splitter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@PluginImplementation
public class TokenExporter extends GeneralTextExporter
{

  @Override
  public void convertText(AnnisResultSet queryResult, List<String> keys, 
    Map<String,String> args, Writer out, int offset) throws IOException
  {
    
    Map<String, Map<String, Annotation>> metadataCache = new HashMap<>();
    
    List<String> metaKeys = new LinkedList<>();
    if(args.containsKey("metakeys"))
    {
      Iterable<String> it = 
        Splitter.on(",").trimResults().split(args.get("metakeys"));
      for(String s : it)
      {
        metaKeys.add(s);
      }
    }

    
    
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
      
      
      if(!metaKeys.isEmpty())
      {
        String[] path = annisResult.getPath();
        super.appendMetaData(out, metaKeys, path[path.length-1], annisResult.getDocumentName(), metadataCache);
      }
      out.append("\n");
    }
  }

  
  @Override
  public String getHelpMessage()
  {
    return "The Token Exporter exports the token covered by the matched nodes of every search result and "
        + "its context, one line per result. "
        + "Beside the text of the token it also contains all token annotations separated by \"/\"."
        + "<p>"
        + "<strong>This exporter does not work well with dialog data "
        + "(corpora that have more than one primary text). "
        + "Use the GridExporter instead.</strong>"
        + "</p>";
  }
  
  
  
  
  
  @Override
  public SubgraphFilter getSubgraphFilter()
  {
    return SubgraphFilter.token;
  }


@Override
public boolean isAlignable() 
	{
		return false;
	}
  
  
}
