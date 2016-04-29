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
package annis.visualizers.component.dependency;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import annis.libgui.MatchedNodeColors;
import annis.libgui.visualizers.VisualizerInput;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.Edge;
import annis.visualizers.component.AbstractDotVisualizer;
import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 * Visualizes arrow based dependency visualization for corpora 
 * with dependencies between non terminal nodes.
 * 
 * Requires GraphViz.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@PluginImplementation
public class ProielRegularDependencyTree extends AbstractDotVisualizer
{

  private VisualizerInput input;
  private StringBuilder dot;
  private List<AnnisNode> realToken;
  private List<AnnisNode> pseudoToken;
  private Set<String> alreadyWrittenEdge;

  @Override
  public String getShortName()
  {
    return "ordered_dependency";
  }

  
  
  @Override
  public void createDotContent(VisualizerInput input, StringBuilder sb)
  {
    this.dot = sb;
    this.input = input;

    w("digraph G {\n");
    w("charset=\"UTF-8\";\n");
    w("graph [truecolor bgcolor=\"#ff000000\"];\n");
    w("node [shape=none];\n");

    realToken = new LinkedList<AnnisNode>();
    pseudoToken = new LinkedList<AnnisNode>();
    alreadyWrittenEdge = new HashSet<String>();

    writeAllRealToken();
    writeAllPseudoToken();
    writeAllTokenConnections();
    writeAllDepEdges();

    w("}");
  }
  
  private void writeAllRealToken()
  {
    // Token are in a subgraph
    w("  {\n \trank=max;\n");

    for (AnnisNode n : input.getResult().getGraph().getTokens())
    {
      realToken.add(n);
      writeToken(n);
    }
    writeInvisibleTokenEdges(realToken);
    w("  }\n");
  }
  
  private void writeAllPseudoToken()
  {
    // write out pseudo token nodes
    for (AnnisNode n : input.getResult().getGraph().getNodes())
    {
      if (!n.isToken())
      {
        boolean isDepNode = false;
        for (Annotation anno : n.getNodeAnnotations())
        {
          if ("tiger".equals(anno.getNamespace()) && "word".equals(anno.getName()))
          {
            isDepNode = true;
            break;
          }
        }

        if (isDepNode)
        {
          writeNode(n);
          pseudoToken.add(n);
        }
      }
    }
  }
  
  private void writeAllDepEdges()
  {
    for (Edge e : input.getResult().getGraph().getEdges())
    {
      if(e.getDestination() != null && !e.getDestination().isToken())
      {
        boolean isDepEdge = false;
        for (Annotation anno : e.getAnnotations())
        {
          if ("tiger".equals(anno.getNamespace()) && "func".equals(anno.getName()))
          {
            isDepEdge = true;
            break;
          }
        }
        if (isDepEdge)
        {
          writeEdge(e);
        }
      }
    }
  }

  private void writeAllTokenConnections()
  {
    for (AnnisNode tok : pseudoToken)
    {

      AnnisNode realTok = getCorrespondingRealToken(tok);
      if(realTok != null)
      {
        w("" + tok.getId() + " -> " + realTok.getId() + "[label=\"\", color=lightgrey, arrowhead=none]");
      }
    }
  }


  private AnnisNode getCorrespondingRealToken(AnnisNode n)
  {
    if (n == null)
    {
      return null;
    }

    for (Edge e : n.getOutgoingEdges())
    {
      if (e.getDestination() != null && e.getDestination().isToken())
      {
        for (Annotation anno : e.getAnnotations())
        {
          if ("tiger".equals(anno.getNamespace()) && "func".equals(anno.getName())
            && "--".equals(anno.getValue()))
          {
            return e.getDestination();
          }
        }
      }
    }

    return null;
  }

  private void writeInvisibleTokenEdges(List<AnnisNode> token)
  {
    Collections.sort(token, new Comparator<AnnisNode>()
    {

      @Override
      public int compare(AnnisNode o1, AnnisNode o2)
      {
        return o1.getTokenIndex().compareTo(o2.getTokenIndex());
      }
    });
    AnnisNode lastTok = null;
    for (AnnisNode tok : token)
    {
      if (lastTok != null)
      {
        w("\t\t");
        w(lastTok.getId());
        w(" -> ");
        w(tok.getId());
        w(" [style=invis];\n");
      }
      lastTok = tok;
    }
  }

  private void writeNode(AnnisNode n)
  {
    String color = "#000000";
    String shape = "point";

    String colorAsString = input.getMarkableExactMap().get(Long.toString(n.getId()));
    if (colorAsString != null)
    {
      MatchedNodeColors matchedColor = MatchedNodeColors.valueOf(colorAsString);
      color = matchedColor.getHTMLColor();
      shape = "circle";
    }

    w("  " + n.getId() + "[shape=\"" + shape + "\", label=\"\" ");
    // background color
    w("style=filled, ");
    w("fillcolor=\"");
    w(color);
    w("\" color=\"");
    w(color);
    w("\" ];\n");
  }

  private void writeToken(AnnisNode n)
  {
    w("  " + n.getId() + "[label=\"" + n.getSpannedText() + "\" ");
    // background color
    w("style=filled, ");
    w("fillcolor=\"");
    String colorAsString = input.getMarkableExactMap().get(Long.toString(n.getId()));
    if (colorAsString != null)
    {
      MatchedNodeColors color = MatchedNodeColors.valueOf(colorAsString);
      w(color.getHTMLColor());
    }
    else
    {
      w("#ffffff");
    }
    w("\" ];\n");
  }

  private void writeEdge(Edge e)
  {
    AnnisNode srcNode = e.getSource();
    AnnisNode destNode = e.getDestination();

    if(e.getName() == null || srcNode == null || destNode == null)
    {
      return;
    }
    else
    {
      String srcId = "" + srcNode.getId();
      String destId = "" + destNode.getId();

      // get the edge annotation
      StringBuilder sbAnno = new StringBuilder();
      boolean first = true;
      for (Annotation anno : e.getAnnotations())
      {
        if (!first)
        {
          sbAnno.append("\\n");
        }
        first = false;
        sbAnno.append(anno.getValue());
      }

      String style = "solid";
      if("secedge".equals(e.getName()))
      {
        style = "dashed";
      }

      String edgeString = srcId + " -> " + destId
        + "[shape=none, label=\"" + sbAnno.toString() + "\" style=\"" + style + "\"];\n";

      if (!alreadyWrittenEdge.contains(edgeString))
      {
        w(edgeString);
        alreadyWrittenEdge.add(edgeString);
      }
    }
  }

  private void w(String s)
  {
    dot.append(s);
  }

  private void w(long l)
  {
    dot.append(l);
  }
}
