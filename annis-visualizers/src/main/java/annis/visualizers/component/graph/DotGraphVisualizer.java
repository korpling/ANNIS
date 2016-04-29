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
package annis.visualizers.component.graph;

import annis.libgui.MatchedNodeColors;
import annis.libgui.visualizers.VisualizerInput;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.Edge;
import annis.visualizers.component.AbstractDotVisualizer;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 * A debug visualization of the annotation graph.
 * 
 * Mappings:<br/>
 * Use <b>all_ns:true</b> to visualize the entire annotation graph. 
 * Specifying e.g. <b>node_ns:tiger</b> or <b>edge_ns:tiger</b> instead 
 * causes only nodes and edges of the namespace <b>tiger</b> to be visualized 
 * (i.e. only a subgraph of all annotations).
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@PluginImplementation
public class DotGraphVisualizer extends AbstractDotVisualizer
{
  
  private VisualizerInput input;
  private StringBuilder dot;
  private boolean displayAllNamespaces = false;
  private String requiredNodeNS;
  private String requiredEdgeNS;

  @Override
  public String getShortName()
  {
    return "dot_vis";
  }

  
  
  @Override
  public void createDotContent(VisualizerInput input, StringBuilder sb)
  {    
    this.input = input;
    
    // initialization
    displayAllNamespaces = Boolean.parseBoolean(input.getMappings().getProperty("all_ns", "false"));
    requiredNodeNS = input.getMappings().getProperty("node_ns", input.getNamespace());
    requiredEdgeNS = input.getMappings().getProperty("edge_ns", input.getNamespace());

    if(requiredEdgeNS == null && requiredNodeNS == null)
    {
      displayAllNamespaces = true;
    }

    dot = sb;

    // do the real work
    internalCreateDot();
  }

  private void internalCreateDot()
  {
    w("digraph G {\n");
    w("\tnode [shape=box];\n");
     // node definitions
    List<AnnisNode> token = new LinkedList<AnnisNode>();
    for (AnnisNode n : input.getResult().getGraph().getNodes())
    {
      if(n.isToken())
      {
        token.add(n);
      }
      else
      {
        if(testNode(n))
        {
          writeNode(n);
        }
      }
    }
    // Token are in a subgraph
    w("\t{\n"
      + "\trank=max;\n");
    for(AnnisNode tok : token)
    {
      w("\t");
      writeNode(tok);
    }
    writeInvisibleTokenEdges(token);
    w("\t}\n");

    for (Edge e : input.getResult().getGraph().getEdges())
    {
      if(e != null && testEdge(e))
      {
        writeEdge(e);
      }
    }
    
    w("}");
  }

  private void w(String s)
  {
    dot.append(s);
  }
  private void w(long l)
  {
    dot.append(l);
  }
  

  private boolean testNode(AnnisNode node)
  {
    if(displayAllNamespaces)
    {
      return true;
    }
    
    if(requiredNodeNS == null)
    {
      return false;
    }
      
    for(Annotation anno : node.getNodeAnnotations())
    {
      if(requiredNodeNS.equals(anno.getNamespace()))
      {
        return true;
      }
    }
    
    for(Annotation anno : node.getEdgeAnnotations())
    {
      if(requiredNodeNS.equals(anno.getNamespace()))
      {
        return false;
      }
    }
    
    return false;
  }
  
  private void writeNode(AnnisNode node)
  {

    w("\t");
    w(node.getId());
    // attributes
    w(" [ ");
    // output label
    w("label=\"");
    appendLabel(node);
    appendNodeAnnotations(node);
    w("\", ");

    // background color
    w("style=filled, ");
    w("fillcolor=\"");
    String colorAsString = input.getMarkableExactMap().get(Long.toString(node.getId()));
    if (colorAsString != null)
    {
      MatchedNodeColors color = MatchedNodeColors.valueOf(colorAsString);
      w(color.getHTMLColor());
    }
    else
    {
      w("#ffffff");
    }
    w("\" ");
    // "footer"
    w("];\n");
  }

  private void writeInvisibleTokenEdges(List<AnnisNode> token)
  {
    Collections.sort(token, new Comparator<AnnisNode>() {

      @Override
      public int compare(AnnisNode o1, AnnisNode o2)
      {
        return o1.getTokenIndex().compareTo(o2.getTokenIndex());
      }

    });
    AnnisNode lastTok = null;
    for(AnnisNode tok : token)
    {
      if(lastTok != null)
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

  private void appendLabel(AnnisNode node)
  {
    if (node.isToken())
    {
      w(node.getSpannedText().replace("\"", "\\\""));
    }
    else
    {
      w(node.getQualifiedName());
    }
    w("\\n");
  }

  private void appendNodeAnnotations(AnnisNode node)
  {
    for(Annotation anno : node.getNodeAnnotations())
    {
      if(displayAllNamespaces || requiredNodeNS.equals(anno.getNamespace()))
      {
        w("\\n");

        w(anno.getQualifiedName());
        w("=");
        w(anno.getValue().replace("\"", "\\\""));
      }
    }
  }

  private boolean testEdge(Edge edge)
  {
    if(displayAllNamespaces)
    {
      return true;
    }

    if(requiredEdgeNS == null)
    {
      return false;
    }

    for(Annotation anno : edge.getAnnotations())
    {
      if(requiredEdgeNS.equals(anno.getNamespace()))
      {
        return true;
      }
    }

    return false;
  }
  private void writeEdge(Edge edge)
  {
    // from -> to
    w("\t");
    w("" + (edge.getSource() == null ? "null" : edge.getSource().getId()));
    w(" -> ");
    w("" + (edge.getDestination() == null ? "null" : edge.getDestination().getId()));
    // attributes
    w(" [");
    if(edge.getEdgeType() == Edge.EdgeType.POINTING_RELATION)
    {
      w(" style=dashed color=green ");
    }
    else if(edge.getEdgeType() == Edge.EdgeType.COVERAGE)
    {
      w(" style=dotted color=orange ");
    }
    // label
    w("label=\"");
    w(edge.getNamespace());
    w(".");
    w(edge.getName());
    w("\\n");
    Iterator<Annotation> itAnno = edge.getAnnotations().iterator();
    while(itAnno.hasNext())
    {
      Annotation anno = itAnno.next();
      w(anno.getQualifiedName());
      w("=");
      w(anno.getValue());
      if(itAnno.hasNext())
      {
        w("\\n");
      }
    }
    w("\"");
    w("];\n");
  }
}
