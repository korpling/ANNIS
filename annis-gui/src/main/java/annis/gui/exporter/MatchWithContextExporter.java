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

import static annis.model.AnnisConstants.ANNIS_NS;
import static annis.model.AnnisConstants.FEAT_MATCHEDIDS;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SNode;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

import annis.service.objects.Match;
import annis.service.objects.SubgraphFilter;
import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 * An exporter that will take all token nodes and exports
 * them in a kind of grid.
 * This is useful for getting references of texts where the normal token based
 * text exporter doesn't work since there are multiple speakers or normalizations.
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@PluginImplementation
public class MatchWithContextExporter extends SaltBasedExporter
{

  
  @Override
  public void convertText(SDocumentGraph graph, List<String> annoKeys,
    Map<String, String> args, int matchNumber, Writer out)
    throws IOException
  {
    if(graph != null)
    {
      // get matched nodes
      SFeature featMatchedIDs = graph.getFeature(ANNIS_NS, FEAT_MATCHEDIDS);
      Match match = new Match();
      if (featMatchedIDs != null && featMatchedIDs.getValue_STEXT() != null)
      {    
         match = Match.parseFromString(featMatchedIDs.getValue_STEXT(), ',');
      }
      List<SNode> matchedNodes = new LinkedList<>();
      for(URI uri : match.getSaltIDs())
      {
        SNode n = graph.getNode(uri.toASCIIString());
        if(n != null)
        {
          matchedNodes.add(n);
        }
      }
      
      List<String> line = new LinkedList<>();
      for(SNode n : matchedNodes)
      {
        // TODO: get context left and right of node
        line.add(graph.getText(n));
      }
      out.append(Joiner.on('\t').join(line));
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
