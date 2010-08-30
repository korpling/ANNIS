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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author thomas
 */
public class RegularDependencyTree extends AbstractDotVisualizer
{

  private StringBuilder dot;
  private Map<Vector2,AnnisNode> token;
  private Set<String> alreadyWrittenEdge;
  private boolean flatToken;

  private enum NodeType 
  {
    empty,coordinator,other
  }

  @Override
  public void createDotContent(StringBuilder sb)
  {
    dot = sb;

    flatToken = Boolean.parseBoolean(getMappings().getProperty("flat", "false"));

    w("digraph G {\n");
    w("  charset=\"UTF-8\";\n");
    w("  graph [truecolor bgcolor=\"#ff000000\"];\n");
    w("  root [fontcolor=\"black\",label=\"\",shape=\"circle\"];\n");

    token = new HashMap<Vector2, AnnisNode>();
    alreadyWrittenEdge = new HashSet<String>();

    if(flatToken)
    {
      // Token are in a subgraph
      w("  {\n \trank=max;\n");
    }

    for(AnnisNode n : getResult().getGraph().getNodes())
    {
      if(n.isToken())
      {
        token.put(new Vector2(n.getLeftToken(), n.getRightToken()), n);
        writeNode(n);
      }
    }

    if(flatToken)
    {
      writeInvisibleTokenEdges(new LinkedList<AnnisNode>(token.values()));
      w("  }\n");
    }
    
    for(Edge e : getResult().getGraph().getEdges())
    {
      if(e.getEdgeType() == Edge.EdgeType.POINTING_RELATION)
      {
        writeEdge(e);
      }
    }

    w("}");
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

  /**
   * try to avoid some data oddity where there are dummy nodes instead of annotations
   * on the real token
   * @param n
   * @return
   */
  private AnnisNode getRealNode(AnnisNode n)
  {
    if(n == null)
    {
      return null;
    }
    else
    {
      if(n.isToken())
      {
        return n;
      }
      else
      {
        Vector2 v = new Vector2(n.getLeftToken(), n.getRightToken());
        AnnisNode realToken = token.get(v);
        if(realToken == null)
        {
          return n;
        }
        else
        {
          return realToken;
        }
      }
    }
  }

  private void writeNode(AnnisNode n)
  {    
    w("  " + n.getId() + "[shape=box, label=\"" + n.getSpannedText() + "\" ");
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
    AnnisNode srcNode = getRealNode(e.getSource());
    AnnisNode destNode = getRealNode(e.getDestination());
    
    String srcId = srcNode == null ? "root" : "" + srcNode.getId();
    String destId = destNode == null ? "root" : "" + destNode.getId();

    // get the edge annotation
    StringBuilder sbAnno = new StringBuilder();
    boolean first = true;
    for(Annotation anno : e.getAnnotations())
    {
      if(!first)
      {
        sbAnno.append("\\n");
      }
      first = false;
      sbAnno.append(anno.getName());
    }

    String edgeString = srcId + " -> " + destId
      + "[shape=box, label=\"" + sbAnno.toString() + "\"]";

    if(!alreadyWrittenEdge.contains(edgeString))
    {
      w("  " + edgeString);
      alreadyWrittenEdge.add(edgeString);
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

  private class Vector2
  {
    private long x;
    private long y;

    public Vector2(long x, long y)
    {
      this.x = x;
      this.y = y;
    }

    public long getX()
    {
      return x;
    }

    public void setX(long x)
    {
      this.x = x;
    }

    public long getY()
    {
      return y;
    }

    public void setY(long y)
    {
      this.y = y;
    }

    @Override
    public boolean equals(Object obj)
    {
      if (obj == null)
      {
        return false;
      }
      if (getClass() != obj.getClass())
      {
        return false;
      }
      final Vector2 other = (Vector2) obj;
      if (this.x != other.x)
      {
        return false;
      }
      if (this.y != other.y)
      {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode()
    {
      int hash = 7;
      hash = 83 * hash + (int) (this.x ^ (this.x >>> 32));
      hash = 83 * hash + (int) (this.y ^ (this.y >>> 32));
      return hash;
    }
    
    
    
    
  }

}
