/*
 *  Copyright 2010 thomas.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package annis.frontend.servlets.visualizers.dependency;

import annis.frontend.servlets.MatchedNodeColors;
import annis.frontend.servlets.visualizers.AbstractDotVisualizer;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.Edge;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author thomas
 */
public class RegularDependencyTree extends AbstractDotVisualizer
{

  private StringBuilder dot;
  private List<AnnisNode> realToken;
  private List<AnnisNode> pseudoToken;
  private Set<String> alreadyWrittenEdge;
  private Random rand = new Random();

  @Override
  public void createDotContent(StringBuilder sb)
  {
    dot = sb;


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

    for (AnnisNode n : getResult().getGraph().getTokens())
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
    for (AnnisNode n : getResult().getGraph().getNodes())
    {
      if (!n.isToken())
      {
        boolean isDepNode = false;
        String word = null;
        for (Annotation anno : n.getNodeAnnotations())
        {
          if ("tiger".equals(anno.getNamespace()) && "word".equals(anno.getName()))
          {
            isDepNode = true;
            word= anno.getValue();
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
    for (Edge e : getResult().getGraph().getEdges())
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

    String colorAsString = getMarkableExactMap().get(Long.toString(n.getId()));
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
    String colorAsString = getMarkableExactMap().get(Long.toString(n.getId()));
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
      String srcId = srcNode == null ? "root" : "" + srcNode.getId();
      String destId = destNode == null ? "root" : "" + destNode.getId();

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
