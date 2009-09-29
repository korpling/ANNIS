/*
 * Copyright 2009 Collaborative Research Centre SFB 632 
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
package annis.frontend.servlets.visualizers;

import annis.model.AnnisNode;
import annis.model.Edge;
import annis.service.ifaces.AnnisToken;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thomas
 */
public class CorefVisualizer extends WriterVisualizer
{

  private HashMap<Long, List<PR>> dst2PR;
  private HashMap<Long, List<PR>> src2PR;
  private HashMap<Long, List<PR>> dst2PR_tok;
  private HashMap<Long, List<PR>> src2PR_tok;
  private HashMap<Long, HashSet<Long>> span2tok;
  private HashMap<Long, String> id2Text;
  private List<Long> tokenList;

  @Override
  public void writeOutput(Writer writer)
  {
    try
    {
      writer.append("<html>");
      writer.append("<head>");

      writer.append("<script type=\"text/javascript\" src=\"" + getContextPath() + "/javascript/extjs/adapter/ext/ext-base.js\"></script>");
      writer.append("<script type=\"text/javascript\" src=\"" + getContextPath() + "/javascript/extjs/ext-all.js\"></script>");

      writer.append("<link href=\"" + getContextPath() + "/css/visualizer/coref.css\" rel=\"stylesheet\" type=\"text/css\" >");
      writer.append("<script type=\"text/javascript\" src=\"" + getContextPath() + "/javascript/annis/visualizer/CorefVisualizer.js\"></script>");

      writer.append("</head>");
      writer.append("<body>");

      // from source to pointing relation
      dst2PR = new HashMap<Long, List<PR>>();
      src2PR = new HashMap<Long, List<PR>>();
      span2tok = new HashMap<Long, HashSet<Long>>();
      id2Text = new HashMap<Long, String>();

      tokenList = new LinkedList<Long>();

      // all token
      for(AnnisToken tok : getResult().getTokenList())
      {
        handleTok(tok);
      }

      // all edges
      for(Edge e : getResult().getGraph().getEdges())
      {
        handleRel(e);
      }

      // for all nodes collect span->tok
      for(AnnisNode n : getResult().getGraph().getTokens())
      {
        span2tok.put(n.getId(), new HashSet<Long>());
        span2tok.get(n.getId()).add(n.getId());
        recursiveSpan2Tok(n, n.getId());
      }

      dst2PR_tok = new HashMap<Long, List<PR>>();
      src2PR_tok = new HashMap<Long, List<PR>>();
      // add token ids if they are included in spans
      for(long srcKey : src2PR.keySet())
      {
        List<PR> spanList = src2PR.get(srcKey);
        for(long tokKey : span2tok.get(srcKey))
        {
          if(!src2PR_tok.containsKey(tokKey))
          {
            src2PR_tok.put(tokKey, new LinkedList<PR>());
          }
          src2PR_tok.get(tokKey).addAll(spanList);
        }
      }

      for(long dstKey : dst2PR.keySet())
      {
        List<PR> spanList = dst2PR.get(dstKey);
        for(long tokKey : span2tok.get(dstKey))
        {
          if(!dst2PR_tok.containsKey(tokKey))
          {
            dst2PR_tok.put(tokKey, new LinkedList<PR>());
          }
          dst2PR_tok.get(tokKey).addAll(spanList);
        }
      }

      // write out every token
      for(Long tok : tokenList)
      {
        writeoutToken(tok, writer);
      }

      writer.append("</body></html>");
    }
    catch(IOException ex)
    {
      Logger.getLogger(CorefVisualizer.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void recursiveSpan2Tok(AnnisNode n, long tokenID)
  {
    for(Edge e : n.getIncomingEdges())
    {
      AnnisNode pre = e.getSource();

      if(pre != null)
      {
        if(span2tok.get(pre.getId()) == null)
        {
          span2tok.put(pre.getId(), new HashSet<Long>());
        }

        span2tok.get(pre.getId()).add(tokenID);
        recursiveSpan2Tok(pre, tokenID);
      }
      
    }
  }

  private void handleRel(Edge e)
  {
    if(e.getEdgeType() == Edge.EdgeType.POINTING_RELATION &&
      e.getSource() != null && e.getDestination() != null)
    {

      PR pr = new PR(e.getDestination().getId(), e.getSource().getId());

      if(!dst2PR.containsKey(pr.dst))
      {
        dst2PR.put(pr.dst, new LinkedList<PR>());
      }
      dst2PR.get(pr.dst).add(pr);

      if(!src2PR.containsKey(pr.src))
      {
        src2PR.put(pr.src, new LinkedList<PR>());
      }
      src2PR.get(pr.src).add(pr);

    }
  }

  void handleTok(AnnisToken t)
  {
    long elementID = t.getId();
    tokenList.add(elementID);

    if(span2tok.get(elementID) == null)
    {
      span2tok.put(elementID, new HashSet<Long>());
    }
    span2tok.get(elementID).add(elementID);

    id2Text.put(elementID, t.getText());

  }

  private void writeoutToken(long id, Writer writer) throws IOException
  {

    String style = "";
    if(getMarkableMap().containsKey("" + id))
    {
      style += "color:red; ";
    }

    StringBuilder left = new StringBuilder();
    StringBuilder right = new StringBuilder();
    String onmouseover = "";
    String onmouseout = "";
    String onclick = "";

    long counterL = 0;
    List<PR> leftList = dst2PR_tok.get(id);
    if(leftList != null)
    {
      for(PR p : leftList)
      {
        HashSet<Long> leftToken = span2tok.get(p.src);
        for(long l : leftToken)
        {
          if(l != id)
          {
            if(counterL > 0)
            {
              left.append(",");
            }

            left.append(l);
            counterL++;
          }
        }
      }
    }

    long counterR = 0;
    List<PR> rightList = src2PR_tok.get(id);
    if(rightList != null)
    {
      for(PR p : rightList)
      {
        HashSet<Long> rightToken = span2tok.get(p.dst);
        for(long l : rightToken)
        {
          if(l != id)
          {
            if(counterR > 0)
            {
              right.append(",");
            }

            right.append(l);
            counterR++;
          }
        }
      }
    }
    if(counterL > 0 || counterR > 0)
    {
      style += "text-decoration:underline;cursor:pointer;";
      onmouseover = "";
      onmouseout = "";
      onclick = "togglePRAuto(this);";
    }


    writer.append("<span id=\"tok_" + id + "\" " + "style=\"" + style + "\" onmouseover=\"" + onmouseover + "\" onmouseout=\"" + onmouseout + "\" onclick=\"" + onclick + "\" annis:pr_left=\"" + left + "\" annis:pr_right=\"" + right + "\" >");
    writer.append(id2Text.get(id));
    writer.append(" ");
    writer.append("</span>");
  }

  private class PR
  {

    public long dst;
    public long src;

    public PR(long dst, long src)
    {
      this.dst = dst;
      this.src = src;
    }

    @Override
    public boolean equals(Object obj)
    {
      if(obj == null)
      {
        return false;
      }
      if(getClass() != obj.getClass())
      {
        return false;
      }
      final PR other = (PR) obj;
      if(this.dst != other.dst)
      {
        return false;
      }
      if(this.src != other.src)
      {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode()
    {
      int hash = 3;
      hash = 47 * hash + (int) (this.dst ^ (this.dst >>> 32));
      hash = 47 * hash + (int) (this.src ^ (this.src >>> 32));
      return hash;
    }
  }
}
