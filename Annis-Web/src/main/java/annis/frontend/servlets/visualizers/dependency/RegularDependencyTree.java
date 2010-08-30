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

import annis.exceptions.AnnisException;
import annis.frontend.servlets.visualizers.AbstractDotVisualizer;
import annis.model.AnnisNode;
import annis.model.Edge;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections15.map.HashedMap;

/**
 *
 * @author thomas
 */
public class RegularDependencyTree extends AbstractDotVisualizer
{

  private StringBuilder dot;
  private Map<Vector2,AnnisNode> token;
  private Set<String> alreadyWrittenEdge;

  private enum NodeType 
  {
    empty,coordinator,other
  }

  @Override
  public void createDotContent(StringBuilder sb)
  {
    dot = sb;

    w("digraph G {\n");
    w("  charset=\"UTF-8\";\n");
    w("  graph [truecolor bgcolor=\"#ff000000\"];\n");
    w("  root [fontcolor=\"black\",label=\"\",shape=\"circle\"];\n");

    token = new HashMap<Vector2, AnnisNode>();
    alreadyWrittenEdge = new HashSet<String>();

    for(AnnisNode n : getResult().getGraph().getNodes())
    {
      if(n.isToken())
      {
        token.put(new Vector2(n.getLeftToken(), n.getRightToken()), n);
        writeNode(n);
      }
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
    w("  " + n.getId() + "[shape=box, label=\"" + n.getSpannedText() + "\"]");    
  }

  private void writeEdge(Edge e)
  {
    AnnisNode srcNode = getRealNode(e.getSource());
    AnnisNode destNode = getRealNode(e.getDestination());
    
    String srcId = srcNode == null ? "root" : "" + srcNode.getId();
    String destId = destNode == null ? "root" : "" + destNode.getId();

    String edgeString = srcId + " -> " + destId
      + "[shape=box, label=\"" + "nolabel" + "\"]";

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
