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
import com.google.common.base.Splitter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class OrfeoExporter extends GeneralTextExporter
{

  @Override
  public void convertText(AnnisResultSet queryResult, List<String> keys, 
    Map<String,String> args, Writer out, int offset) throws IOException
  {
    
    Map<String, Map<String, Annotation>> metadataCache = 
      new HashMap<>();


    boolean showNumbers = true;
    if (args.containsKey("numbers"))
    {
      String arg = args.get("numbers");
      if (arg.equalsIgnoreCase("false")
        || arg.equalsIgnoreCase("0")
        || arg.equalsIgnoreCase("off"))
      {
        showNumbers = false;
      }
    }
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

	String speakerChange = args.containsKey("c") ? args.get("c") : null;

    int counter = 0;
    for (AnnisResult annisResult : queryResult)
    {
      HashMap<String, TreeMap<Long, Span>> annos =
        new HashMap<>();

      counter++;
      out.append((counter + offset) + ". \t");

	  List<AnnisNode> tokenList = annisResult.getGraph().getTokens();
	  long tokenOffset = tokenList.get(0).getTokenIndex() - 1;
	  for (int i = annisResult.getPath().length - 1; i >= 0; i--)
	  {
		out.append(annisResult.getPath()[i]);
		if (i > 0)
		  out.append(" > ");
	  }
	  long lastTokenIndex = tokenList.get(tokenList.size() - 1).getTokenIndex();
	  out.append(" (tokens ").append((tokenOffset + 2) + "-" + (lastTokenIndex + 1)).append(")\n");

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
          out.append("\t" + k + "\t ");
		  if (annos.get("speaker") != null && speakerChange != null) {
			String prev = null;
			Iterator<Span> it = annos.get("speaker").values().iterator();
			for (AnnisNode annisNode : annisResult.getGraph().getTokens()) {
			  String speaker = it.next().getValue();
			  if (prev == null)
			  {
				prev = speaker;
			  }
			  else if (!prev.equals(speaker))
			  {
				out.append(speakerChange+" ");
				prev = speaker;
			  }
			  out.append(annisNode.getSpannedText() + " ");
			}
		  }
		  else
		  {
			for (AnnisNode annisNode : annisResult.getGraph().getTokens())
			{
			  out.append(annisNode.getSpannedText() + " ");
			}
		  }
		  out.append("\n");
		}
        else if ("pos".equalsIgnoreCase(k) || "lemma".equalsIgnoreCase(k))
        {
          if(annos.get(k) != null)
          {
            out.append("\t" + k + "\t ");
            for(Span s : annos.get(k).values())
            {

              out.append(s.getValue());

              if (showNumbers)
              {
                long leftIndex = Math.max(1, s.getStart() - tokenOffset);
                long rightIndex = s.getEnd() - tokenOffset;
                out.append("[" + leftIndex
                  + "-" + rightIndex + "]");
              }
              out.append(" ");

            }
            out.append("\n");
          }
        }
      }
      
      if(!metaKeys.isEmpty())
      {
        String[] path = annisResult.getPath();
        super.appendMetaData(out, metaKeys, path[path.length-1], annisResult.getDocumentName(), metadataCache);
      }
      out.append("\n\n");
    }
  }

  @Override
  public String getHelpMessage() {
	return "The Orfeo exporter is a modification of GridExporter with a few differences:<br/><br/>"
			+ "The path of the sample and the indices of the tokens in the context are included.<br/>"
			+ "Annotation layers other than POS and lemma are omitted.<br/>"
			+ "If speaker annotation is available (layer <em>speaker</em>) and the parameter <em>c</em>"
			+ " is defined, then the string in that parameter is used within the token list"
			+ " to indicate speaker changes.";
  }

  @Override
  public SubgraphFilter getSubgraphFilter()
  {
    return SubgraphFilter.all;
  }
  
  


  private static class Span
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
