/*
 *  Copyright 2010 Collaborative Research Centre SFB 632.
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
package annis.frontend.servlets.visualizers.graph;

import annis.frontend.servlets.MatchedNodeColors;
import annis.frontend.servlets.visualizers.WriterVisualizer;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.Edge;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Thomas Krause
 */
public class DotGraphVisualizer extends WriterVisualizer
{

  private String outputFormat = "png";
  private int scale = 50;
  private StringBuilder dot;
  private boolean displayAllNamespaces = false;
  private String requiredNodeNS;
  private String requiredEdgeNS;

  @Override
  public void writeOutput(Writer writer)
  {
    displayAllNamespaces = Boolean.parseBoolean(getMappings().getProperty("all_ns", "false"));
    requiredNodeNS = getMappings().getProperty("node_ns", getNamespace());
    requiredEdgeNS = getMappings().getProperty("edge_ns", getNamespace());

    if(requiredEdgeNS == null && requiredNodeNS == null)
    {
      displayAllNamespaces = true;
    }

    dot = new StringBuilder();

    try
    {
      String cmd = getMappings().getProperty("dotpath", "dot") + " -s" + scale + ".0 -T" + outputFormat;
      Runtime runTime = Runtime.getRuntime();
      Process p = runTime.exec(cmd);
      OutputStreamWriter stdin = new OutputStreamWriter(p.getOutputStream(), "UTF-8");

      createDOT();
      stdin.append(dot);
      stdin.flush();

      p.getOutputStream().close();
      int chr;
      InputStream stdout = p.getInputStream();
      StringBuilder outMessage = new StringBuilder();
      while ((chr = stdout.read()) != -1)
      {
        writer.write(chr);
        outMessage.append((char) chr);
      }

      StringBuilder errorMessage = new StringBuilder();
      InputStream stderr = p.getErrorStream();
      while ((chr = stderr.read()) != -1)
      {
        errorMessage.append((char) chr);
      }

      p.destroy();
      writer.flush();

      if (!"".equals(errorMessage.toString()))
      {
        Logger.getLogger(DotGraphVisualizer.class.getName()).log(
          Level.SEVERE,
          "Could not execute dot graph-layouter.\ncommand line:\n{0}\n\nstderr:\n{1}\n\nstdin:\n{2}",
          new Object[]
          {
            cmd, errorMessage.toString(), dot.toString()
          });
      }

    }
    catch (IOException ex)
    {
      Logger.getLogger(DotGraphVisualizer.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

  private void createDOT()
  {
    dot.append("digraph G {\n");
    dot.append("\tnode [shape=box];\n");
     // node definitions
    List<AnnisNode> token = new LinkedList<AnnisNode>();
    for (AnnisNode n : getResult().getGraph().getNodes())
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
    dot.append("\t{\n"
      + "\trank=max;\n");
    for(AnnisNode tok : token)
    {
      dot.append("\t");
      writeNode(tok);
    }
    writeInvisibleTokenEdges(token);
    dot.append("\t}\n");

    for (Edge e : getResult().getGraph().getEdges())
    {
      if(testEdge(e))
      {
        writeEdge(e);
      }
    }
    
    dot.append("}");
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

    dot.append("\t");
    dot.append(node.getId());
    // attributes
    dot.append(" [ ");
    // output label
    dot.append("label=\"");
    appendLabel(node);
    appendNodeAnnotations(node);
    dot.append("\", ");

    // background color
    dot.append("style=filled, ");
    dot.append("fillcolor=\"");
    String colorAsString = getMarkableExactMap().get(Long.toString(node.getId()));
    if (colorAsString != null)
    {
      MatchedNodeColors color = MatchedNodeColors.valueOf(colorAsString);
      dot.append(color.getHTMLColor());
    }
    else
    {
      dot.append("#ffffff");
    }
    dot.append("\" ");
    // "footer"
    dot.append("];\n");
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
        dot.append("\t\t");
        dot.append(lastTok.getId());
        dot.append(" -> ");
        dot.append(tok.getId());
        dot.append(" [style=invis];\n");
      }
      lastTok = tok;
    }
  }

  private void appendLabel(AnnisNode node)
  {
    if (node.isToken())
    {
      dot.append(node.getSpannedText());
    }
    else
    {
      dot.append(node.getName());
    }
    dot.append("\\n");
  }

  private void appendNodeAnnotations(AnnisNode node)
  {
    for(Annotation anno : node.getNodeAnnotations())
    {
      if(displayAllNamespaces || requiredNodeNS.equals(anno.getNamespace()))
      {
        dot.append("\\n");

        dot.append(anno.getQualifiedName());
        dot.append("=");
        dot.append(anno.getValue());
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
    dot.append("\t");
    dot.append(edge.getSource() == null ? null : edge.getSource().getId());
    dot.append(" -> ");
    dot.append(edge.getDestination() == null ? null : edge.getDestination().getId());
    // attributes
    dot.append(" [");
    // label
    dot.append("label=\"");
    dot.append(edge.getNamespace());
    dot.append(".");
    dot.append(edge.getName());
    dot.append("\\n");
    Iterator<Annotation> itAnno = edge.getAnnotations().iterator();
    while(itAnno.hasNext())
    {
      Annotation anno = itAnno.next();
      dot.append(anno.getQualifiedName());
      dot.append("=");
      dot.append(anno.getValue());
      if(itAnno.hasNext())
      {
        dot.append("\\n");
      }
    }
    dot.append("\"");
    dot.append("];\n");
    // TODO
  }

  @Override
  public String getContentType()
  {
    return "image/png";
  }

  @Override
  public String getCharacterEncoding()
  {
    return "ISO-8859-1";
  }
}
