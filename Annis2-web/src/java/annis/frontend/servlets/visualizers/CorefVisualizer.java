/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annis.frontend.servlets.visualizers;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;

/**
 *
 * @author thomas
 */
public class CorefVisualizer extends Visualizer
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

      writer.append("<script type=\"text/javascript\" src=\"" + contextPath + "/javascript/extjs/adapter/ext/ext-base.js\"></script>");
      writer.append("<script type=\"text/javascript\" src=\"" + contextPath + "/javascript/extjs/ext-all.js\"></script>");

      writer.append("<link href=\"" + contextPath + "/css/visualizer/coref.css\" rel=\"stylesheet\" type=\"text/css\" >");
      writer.append("<script type=\"text/javascript\" src=\"" + contextPath + "/javascript/annis/visualizer/CorefVisualizer.js\"></script>");

      writer.append("</head>");
      writer.append("<body>");

      // from source to pointing relation
      dst2PR = new HashMap<Long, List<PR>>();
      src2PR = new HashMap<Long, List<PR>>();
      span2tok = new HashMap<Long, HashSet<Long>>();
      id2Text = new HashMap<Long, String>();

      tokenList = new LinkedList<Long>();

      Iterator<Element> itElements = getPaulaJDOM().getDescendants(new ElementFilter());

      while(itElements.hasNext())
      {
        Element e = itElements.next();

        if("_rel".equals(e.getName()))
        {
          handleRel(e);
        }
        else if("tok".equals(e.getName()))
        {
          handleTok(e);
        }
        else
        {
          // collect span->tok
          Iterator<Element> itTokChild = e.getDescendants(new ElementFilter("tok"));
          if(itTokChild.hasNext())
          {
            Element tok = itTokChild.next();
            if(e.getAttribute("_id") != null && tok.getAttribute("_id") != null)
            {
              long spanID = Long.parseLong(e.getAttributeValue("_id"));
              long tokID = Long.parseLong(tok.getAttributeValue("_id"));
              if(span2tok.get(spanID) == null)
              {
                span2tok.put(spanID, new HashSet<Long>());
              }

              span2tok.get(spanID).add(tokID);
            }
          }
        }
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

  private void handleRel(Element e)
  {
    Namespace annisNS = Namespace.getNamespace("annis", "annis");

    Attribute srcAtt = e.getAttribute("_src");
    Attribute dstAtt = e.getAttribute("_dst");

    Attribute typeAtt = e.getAttribute("type", annisNS);
    if(srcAtt != null && dstAtt != null &&
      typeAtt != null && "p".equals(typeAtt.getValue()))
    {
      Attribute subtypeAtt = e.getAttribute("subtype", annisNS);
      if(subtypeAtt != null)
      {
        try
        {
          PR pr = new PR(dstAtt.getLongValue(), srcAtt.getLongValue(), subtypeAtt.getValue());

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
        catch(DataConversionException ex)
        {
          Logger.getLogger(CorefVisualizer.class.getName()).log(Level.SEVERE, null, ex);
        }

      }
    }

  }

  private void handleTok(Element e)
  {
    long elementID = Long.parseLong(e.getAttributeValue("_id"));
    tokenList.add(elementID);

    if(span2tok.get(elementID) == null)
    {
      span2tok.put(elementID, new HashSet<Long>());
    }
    span2tok.get(elementID).add(elementID);

    id2Text.put(elementID, e.getTextNormalize());

  }

  private void writeoutToken(long id, Writer writer) throws IOException
  {

    String style = "";
    if(markableMap.containsKey("" + id))
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


    writer.append("<span id=\"tok_" + id + "\" " 
      + "style=\"" + style
      + "\" onmouseover=\"" + onmouseover
      + "\" onmouseout=\"" + onmouseout
      + "\" onclick=\"" + onclick
      + "\" annis:pr_left=\"" + left + "\" annis:pr_right=\"" + right + "\" >");
    writer.append(id2Text.get(id));
    writer.append(" ");
    writer.append("</span>");
  }

  private class PR
  {

    public long dst;
    public long src;
    public String name;

    public PR(long dst, long src, String name)
    {
      this.dst = dst;
      this.src = src;
      this.name = name;
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
      if(this.name != other.name && (this.name == null || !this.name.equals(other.name)))
      {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode()
    {
      int hash = 5;
      hash = 17 * hash + (int) (this.dst ^ (this.dst >>> 32));
      hash = 17 * hash + (int) (this.src ^ (this.src >>> 32));
      hash = 17 * hash + (this.name != null ? this.name.hashCode() : 0);
      return hash;
    }
  }
}
