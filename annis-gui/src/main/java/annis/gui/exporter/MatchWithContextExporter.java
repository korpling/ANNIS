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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SDominanceRelation;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.GraphTraverseHandler;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SGraph.GRAPH_TRAVERSE_TYPE;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;

import com.google.common.base.Joiner;

import annis.model.AnnisConstants;
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
  
  private static class IsDominatedByMatch implements GraphTraverseHandler
  {
    
    boolean result = false;

    @Override
    public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode,
        SRelation<SNode, SNode> relation, SNode fromNode, long order)
    {
      
    }

    @Override
    public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode,
        SRelation<SNode, SNode> relation, SNode fromNode, long order)
    {
      SFeature matchedAnno = currNode.getFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_MATCHEDNODE);
      if(matchedAnno != null)
      {
        this.result = true;
      }
    }

    @Override
    public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
        SRelation relation, SNode currNode, long order)
    {
      if(this.result)
      {
        // don't traverse any further if matched node was found 
        return false;
      }
      else
      {
        // only iterate over text-coverage relations
        return 
            relation == null
            || relation instanceof SDominanceRelation 
            || relation instanceof SSpanningRelation;
      }
    }
    
  }

  
  @Override
  public void convertText(SDocumentGraph graph, List<String> annoKeys,
    Map<String, String> args, int matchNumber, Writer out)
    throws IOException
  {
    if(graph != null)
    {
      List<SToken> orderedToken = graph.getSortedTokenByText();
      if(orderedToken != null)
      {
        ListIterator<SToken> it = orderedToken.listIterator();
        boolean lastTokenWasMatched = false;
        while(it.hasNext())
        {
          SToken tok = it.next();
          
          if(it.hasPrevious())
          {
            char seperator = ' '; // default to space as seperator
            
            List<SNode> root = new LinkedList<>();
            root.add(tok);
            IsDominatedByMatch traverser = new IsDominatedByMatch();
            graph.traverse(root, GRAPH_TRAVERSE_TYPE.BOTTOM_UP_DEPTH_FIRST, "IsDominatedByMatch", traverser);
            if(traverser.result)
            {
              // is dominated by a matched node, thus use tab to seperate the non-matches from the matches
              if(!lastTokenWasMatched)
              {
                seperator = '\t';
              }
              lastTokenWasMatched = true;
            }
            else if(lastTokenWasMatched)
            {
              // also mark the end of a match with the tab
              seperator = '\t';
              lastTokenWasMatched = false;
            }
            out.append(seperator);
          } // end if has previous
          
          // append the actual token
          out.append(graph.getText(tok));
          
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
