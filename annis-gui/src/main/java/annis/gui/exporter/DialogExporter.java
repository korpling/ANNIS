/*
 * Copyright 2015 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.exporter;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SToken;

import com.google.common.base.Strings;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

import annis.service.objects.SubgraphFilter;
import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 * An exporter that will take all token and segmentation nodes and exports
 * them in a kind of grid.
 * This is useful for getting references of texts where the normal token based
 * text exporter doesn't work since there are multiple speakers or normalizations.
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@PluginImplementation
public class DialogExporter extends SaltBasedExporter
{

  
  @Override
  public void convertText(SDocumentGraph graph, List<String> annoKeys,
    Map<String, String> args, int matchNumber, Writer out)
    throws IOException
  {
    if(graph != null)
    {
      List<SToken> token = graph.getSortedTokenByText();
      // a map which is used to determine how much space each column will need
      Map<Integer, Integer> columnToWidth = new HashMap<>();
      
      int maxCaptionWidth = 0;
      
      // row: annotation name, column: token index, value: the textual output
      Table<String, Integer, String> grid = TreeBasedTable.create();
      // add the token with the special empty name as row caption
      int i=0;
      for(SToken t : token)
      {
        String coveredText = graph.getText(t);
        grid.put("", i, coveredText);
        // a token will need at least its own number of characters as width
        columnToWidth.put(i, coveredText.length());
        i++;
      }
      
      // TODO: add the segmentation nodes to the output
      
      // actually output the grid
      for(Map.Entry<String, Map<Integer, String>> entry : grid.rowMap().entrySet())
      {
        String caption = entry.getKey();
        Map<Integer, String> row = entry.getValue();
        
        // add the caption
        out.append(Strings.padEnd(caption, maxCaptionWidth, ' '));
        // we separate the entries with tab
        if(maxCaptionWidth > 0)
        {
          out.append("\t");
        }
        
        // we know the row is ordered by the index and that each position is filled
        Iterator<Map.Entry<Integer, String>> itRowEntry = row.entrySet().iterator();
        while(itRowEntry.hasNext())
        {
          Map.Entry<Integer, String> rowEntry = itRowEntry.next();
          
          int index = rowEntry.getKey();
          int width = columnToWidth.get(index);
          out.append(Strings.padEnd(rowEntry.getValue(), width, ' '));
          
          if(itRowEntry.hasNext())
          {
            out.append("\t");
          }
        }
      }

    }
    
    out.append("\n");
  }

  

  @Override
  public SubgraphFilter getSubgraphFilter()
  {
    return SubgraphFilter.all;
  }
  
  @Override
  public String getHelpMessage()
  {
    return null;
  }
  
  
}
