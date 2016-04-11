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

import annis.libgui.MatchedNodeColors;
import annis.libgui.visualizers.VisualizerInput;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.Edge;
import annis.visualizers.component.AbstractDotVisualizer;
import java.util.HashSet;
import java.util.Set;
import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 * Visualizes unordered vertical tree of dependent tokens.
 * 
 * Requires GraphViz
 * @author thomas
 */
@PluginImplementation
public class ProielDependecyTree extends AbstractDotVisualizer
{

  @Override
  public String getShortName()
  {
    return "hierarchical_dependency";
  }
  

  private VisualizerInput input;
  private StringBuilder dot;
  private Set<String> alreadyWrittenEdge;

  @Override
  public void createDotContent(VisualizerInput input, StringBuilder sb)
  {
    this.input = input;
    this.dot = sb;
    alreadyWrittenEdge = new HashSet<String>();

    w("digraph G {\n");
    w("charset=\"UTF-8\";\n");
    w("graph [truecolor bgcolor=\"#ff000000\"];\n");

    writeAllNodes();
    writeAllEdges();

    w("}\n");

  }

  private void writeAllNodes()
  {

    for (AnnisNode n : input.getResult().getGraph().getNodes())
    {
      boolean isDepNode = false;
      String word = null;
      for (Annotation anno : n.getNodeAnnotations())
      {
        if ("tiger".equals(anno.getNamespace()) && "word".equals(anno.getName()))
        {
          isDepNode = true;
          word = anno.getValue();
          break;
        }
      }

      if (isDepNode)
      {
        writeNode(n, word);
      }

    }
  }

  private void writeAllEdges()
  {
    for (Edge e : input.getResult().getGraph().getEdges())
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

  private void writeEdge(Edge e)
  {
    AnnisNode srcNode = e.getSource();
    AnnisNode destNode = e.getDestination();

    if (e.getName() == null || srcNode == null || destNode == null)
    {
      return;
    }

    String srcId = "" + srcNode.getId();
    String destId = "" + destNode.getId();

    // get the edge annotation
    StringBuilder sbAnno = new StringBuilder();
    for (Annotation anno : e.getAnnotations())
    {
      if ("func".equals(anno.getName()))
      {
        if ("--".equals(anno.getValue()))
        {
          return;
        }
        sbAnno.append(anno.getValue());
      }
      break;
    }

    String style = null;
    if ("secedge".equals(e.getName()))
    {
      style = "color=blue, fontcolor=black, style=dashed";
    }
    else
    {
      style = "color=orange, fontcolor=black";
    }
    String edgeString = srcId + " -> " + destId
      + "[" + style + " label=\"" + sbAnno.toString() + "\"]";

    if (!alreadyWrittenEdge.contains(edgeString))
    {
      w("  " + edgeString);
      alreadyWrittenEdge.add(edgeString);
    }
  }

  private void writeNode(AnnisNode n, String word)
  {
    String shape = "box";
    String id = "" + n.getId();
    String fillcolor = "#ffffff";
    String fontcolor = "black";
    String style = "filled";
    String label = "";

    // get pos annotation
    String posAnno = "";

    for (Annotation anno : n.getNodeAnnotations())
    {
      if ("tiger".equals(anno.getNamespace()) && "pos".equals(anno.getName())
        && anno.getValue() != null)
      {
        posAnno = anno.getValue();
      }
    }

    if (isEmptyNode(word))
    {
      if (isRootNode(n))
      {
        shape = "circle";
      }
      else
      {
        if(posAnno.length() > 0)
        {
          // decide which pos to use
          switch (posAnno.charAt(0))
          {
            case 'V':
              shape = "circle";
              label = posAnno;
              break;
            case 'C':
              shape = "diamond";
              label = posAnno;
              break;
            case 'P':
              shape = "hexagon";
              label = posAnno;
              break;
            default:
              shape = "circle";
              label = posAnno;
              break;
          }
        }
      }
    }
    else
    {
      // is coordinator?
      if("C-".equals(posAnno))
      {
        shape = "diamond";
      }
      label = word;
    }

    // check coloring
    String matchColorAsString = input.getMarkableExactMap().get(Long.toString(n.getId()));
    if (matchColorAsString == null)
    {
      // check if there mighte be a matching token that is directly belonging to
      // this "fake" token node
      AnnisNode token = getCorrespondingRealToken(n);
      if (token != null)
      {
        matchColorAsString = input.getMarkableExactMap().get(Long.toString(token.getId()));
      }
    }

    if (matchColorAsString != null)
    {
      MatchedNodeColors matchColor = MatchedNodeColors.valueOf(matchColorAsString);
      fillcolor = matchColor.getHTMLColor();
    }

    // write out the node
    w(id);
    w(" [");
    wAtt("fontcolor", fontcolor);
    wAtt("shape", shape);
    wAtt("fillcolor", fillcolor);
    wAtt("style", style);
    wAtt("label", label);
    w("];\n");

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

  private boolean isRootNode(AnnisNode node)
  {
    boolean result = true;

    for (Edge e : node.getIncomingEdges())
    {
      if (e.getSource() != null)
      {
        result = false;
        break;
      }
    }

    return result;
  }

  private boolean isEmptyNode(String word)
  {
    if (word == null || "".equals(word) || "--".equals(word))
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  private void w(String s)
  {
    dot.append(s);
  }

  private void wAtt(String key, String value)
  {
    w(key);
    w("=\"");
    w(value);
    w("\"");
  }
}
