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
  private HashMap <Integer, Integer> colorlist;
  private HashMap<Long,Long> pointermap;
  private List<Long> tokenList;

  private List<Pointerinfo> pointertoken;
  private List<Pointerinfo> tokenpointer;
  class Pointerinfo{
      public Long id;
      public List<Long> list;
      Pointerinfo(){id=Long.MIN_VALUE; list =new LinkedList<Long>();}
      Pointerinfo(Long i, List<Long> l){id=i; list=l;}
  }

  //neu
  private int maxcount = 0;

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

      pointertoken = new LinkedList<Pointerinfo>();
      tokenpointer = new LinkedList<Pointerinfo>();
      colorlist = new HashMap<Integer, Integer>();

      //build list of linked tokens
      /*for(AnnisNode n : getResult().getGraph().getNodes()) if (!n.isToken()) {
          Pointerinfo info = new Pointerinfo();
          info.id=n.getId();
          for (Edge e : n.getOutgoingEdges()){
              if (e.getDestination().isToken() && e.getEdgeType()==Edge.EdgeType.POINTING_RELATION){ //TODO: non-direkt edges?
                  info.list.add(e.getDestination().getId());
                  if (!tokenpointer.isEmpty()) for (Pointerinfo i : tokenpointer) {
                      if (i.id==e.getDestination().getId()){
                          if (!i.list.contains(n.getId())) i.list.add(n.getId());
                      }
                  } else {
                      List<Long> ll = new LinkedList<Long>();
                      ll.add(n.getId());
                      tokenpointer.add(new Pointerinfo(e.getDestination().getId(), ll));
                  }
              }
          }
          if (!info.list.isEmpty()) pointertoken.add(info);
      }*/
      long edgenumber=1;

      if (getResult()==null || getResult().getGraph() == null || getResult().getGraph().getEdges() == null){
          writer.append("No Result </body></html>");return;
      }

      pointermap = new HashMap<Long, Long>();
      for (Edge e : getResult().getGraph().getEdges()) if (e.getEdgeType()==Edge.EdgeType.POINTING_RELATION &&
      e.getSource() != null && e.getDestination() != null) {
            if (pointermap.isEmpty() || !(pointermap.containsKey(e.getSource().getId()) && pointermap.containsKey(e.getDestination().getId()))){
            Pointerinfo info = new Pointerinfo();
            info.id=edgenumber;
            List<Long> liste = getAllTokensFromEdge(e,edgenumber);
                for (Long l : liste){
                    info.list.add(l);
                    if (!tokenpointer.isEmpty()) {
                        boolean gotit=false;
                        for (Pointerinfo i : tokenpointer) {
                            if (i.id.equals(l)){
                                gotit=true;
                                if (!i.list.contains(edgenumber)) {
                                    i.list.add(edgenumber);
                                }
                                break;
                            }
                        }
                        if (!gotit) {
                            List<Long> ll = new LinkedList<Long>();
                            ll.add(edgenumber);
                            tokenpointer.add(new Pointerinfo(l, ll));
                        }
                    } else {
                        List<Long> ll = new LinkedList<Long>();
                        ll.add(edgenumber);
                        tokenpointer.add(new Pointerinfo(l, ll));
                    }
                }
                pointertoken.add(info);
                edgenumber++;
            } 
      }
      
      //test
      /*writer.append("<td>Test{</td>");
      for (Pointerinfo pi : pointertoken){
          writer.append("<td>|"+pi.id+": ");
          for (Long l : pi.list){
              writer.append(l+", ");
          }
          writer.append(".</td>");
      }
      writer.append("<td>}Test//</td>");
      writer.append("<td>Test{</td>");
      for (Pointerinfo pi : tokenpointer){//pointertoken){
          writer.append("<td>|"+pi.id+": ");
          for (Long l : pi.list){
              writer.append(l+", ");
          }
          writer.append(".</td>");
      }
      writer.append("<td>}Test</td>");//*/

      //write
      List<List<Long>> currentpointer=new LinkedList<List<Long>>(), nextpointer=new LinkedList<List<Long>>();
      //for(AnnisToken tok : getResult().getTokenList())
      int maxlinkcount=0;
      int textcount=0;
      /*long tokid=0;
      int writtencount=0;
      String left = "", oldright = "";
      boolean opened = false;*/

      // [ tok ] method
      /*for(AnnisToken tok : getResult().getTokenList()) {
         tokid = tok.getId();
         if (writtencount>50){
             writtencount=0;
             writer.flush();
         }
         int currentlinkcount=0;
         nextpointer = getAllRelatedTokenIds(tokid); //getall Pointing components

         //Closing ] for ending pointing-relations
         for (int i=currentpointer.size()-1;i>=0;i--){
             List<Long> ll = currentpointer.get(i);
             boolean found = false;
             long value = ll.get(0);
             for (List<Long> ll2 : nextpointer){
                if (ll2.get(0).equals(value)){
                    found=true;break;
                }
             }
             if (!found) {
                String right="", left2="";
                int first=2;
                for (long l : ll) {
                    if (first==2) {
                        first--;
                        right=""+l;
                    } else if (first==1) {
                        left2 += "" + l;first--;
                        } else left2 += ","+l;
                    }
                writer.append("<span id=\"tok_" + tokid + "_"+right+"\" " + "style=\"cursor:pointer;color:gray;\" onmouseover=\""
                        + "\" onmouseout=\"" + "\" onclick=\"togglePRAuto(this);\" annis:pr_left=\""
                        +left2+  "\" annis:pr_right=\""
                        +right+ "\" > ] </span>");
                opened = false;
                writtencount++;
             }
         }

         //Opening [ for new pointing-relations
         for (List<Long> ll : nextpointer){
             boolean found = false;
             long value = ll.get(0);
             for (List<Long> ll2 : currentpointer){
                if (ll2.get(0).equals(value)){
                    found=true;break;
                }
             }
             if (!found) {
                oldright="";
                left="";
                int first=2;
                for (long l : ll) {
                    if (first==2) {
                        first--;
                        oldright=""+l;
                    } else if (first==1) {
                        left += "" + l;first--;
                        } else left += ","+l;
                    }
                writer.append("<span id=\"tok_" + tokid + "_"+oldright+"\" " + " style=\"cursor:pointer;color:gray;\" onmouseover=\""
                        + "\" onmouseout=\"" + "\" onclick=\"togglePRAuto(this);\" annis:pr_left=\""
                        +left+  "\" annis:pr_right=\""
                        +oldright+ "\" > [ </span>");
                opened=true;
                writtencount++;
             }
         }
         currentpointer=nextpointer;

         String onclick="", style = "";
         if(getMarkableMap().containsKey("" + tokid))
            {
            style += "color:red; ";
            }

          boolean underline=false;
          if (!opened) left = "";
          String right = "";boolean rightfirst=true;
          for (List<Long> pi : currentpointer)
          {
              if (!pi.isEmpty()) {
                style += "text-decoration:underline;cursor:pointer;";
                underline=true;
                onclick = "togglePRAuto(this);";
                int first=2;
                for (long l : pi) {
                    if (first==2) {
                        first--;
                        if (rightfirst) {right+=""+l;rightfirst=false;}
                        else right+=","+l;
                    } else if (!opened)
                        if (first==1) {
                            left += "" + l;first--;
                            } else left += ","+l;
                    }
                break;
              }
          }

          if (opened) writer.append("<span id=\"tok_" + tokid + "\" " + "style=\"" + style + "\" onmouseover=\"" + "\" onmouseout=\"" + "\" onclick=\"" + onclick + "\" annis:pr_left=\"" + left + "\" annis:pr_right=\"" + oldright + "\" >");
          else writer.append("<span id=\"tok_" + tokid + "\" " + "style=\"" + style + "\" onmouseover=\"" + "\" onmouseout=\"" + "\" onclick=\"" + onclick + "\" annis:pr_left=\"" + left + "\" annis:pr_right=\"" + right + "\" >");
          writer.append(tok.getText());
          writer.append(" ");
          writer.append("</span>");
          writtencount++;
      }

      for (int i=currentpointer.size()-1;i>=0;i--){
             List<Long> ll = currentpointer.get(i);
             long value = ll.get(0);
                String right="",left2="";
                int first=2;
                for (long l : ll) {
                    if (first==2) {
                        first--;
                        right=""+l;
                    } else if (first==1) {
                        left2 += "" + l;first--;
                        } else left2 += ","+l;
                    }
                writer.append("<span id=\"tok_" + tokid + "_"+right+"\" " + "style=\"cursor:pointer;color:gray;\" onmouseover=\""
                        + "\" onmouseout=\"" + "\" onclick=\"togglePRAuto(this);\" annis:pr_left=\""
                        +left2+  "\" annis:pr_right=\""
                        +right+ "\" > ] </span>");
         }//*/

      //underline method
      //for(AnnisNode n : getResult().getGraph().getNodes()) if (n.isToken()) {
      List<AnnisToken> tokenlist = new LinkedList<AnnisToken>();
      tokenlist.addAll(getResult().getTokenList());
      for(AnnisToken tok : tokenlist) {
         int currentlinkcount=0;
         //writer.append("<td>"+tok.getText()+"</td>");
         writer.flush();
         nextpointer = getAllRelatedTokenIds(tok.getId()); //getall Pointing components

         
         if (currentpointer.isEmpty() || nextpointer.isEmpty()) {
             currentpointer=nextpointer;
         } else {
            List<Boolean> checklist = new LinkedList<Boolean>();
            for (int i=0; i<currentpointer.size(); i++){ checklist.add(false);}
            List<List<Long>> remaininglist = new LinkedList<List<Long>>();

            //get useable Elements from current
            for (List<Long> ll : nextpointer){
                boolean remains = true;
                if (!ll.isEmpty())
                for(List<Long> ll2 : currentpointer){
                    if (!ll2.isEmpty())
                    if (ll2.toArray()[0].equals(ll.toArray()[0])){
                        checklist.set(currentpointer.indexOf(ll2), true);
                        remains = false;
                    }
                }
                if (remains) {
                    remaininglist.add(ll);
                    //writer.append("<td>|"+tok.getText()+" Add:"+ll+"|</td>");
                }
            }
            
            //writer.append("<td>|check: "+checklist+"</td>");

            //cleanup
            int insert=0;
            if (!checklist.isEmpty())
            for (int i=0;i<checklist.size();i++) if (!checklist.get(i)){
                if (!remaininglist.isEmpty() && insert<remaininglist.size()) {
                    currentpointer.set(i,remaininglist.get(insert++));
                }else{
                    currentpointer.set(i, new LinkedList<Long>());
                }
            }
            if (!remaininglist.isEmpty()){
                for (List<Long> ll : remaininglist){
                    if (insert>0) insert--;
                    else currentpointer.add(ll);
                }
            }

         }
     
         //writer.append("<td>|"+tok.getText()+", "+currentpointer+"|</td>");
         
         //writeoutToken(tok, writer);
         String onclick="", style = "";
         if(getMarkableMap().containsKey("" + tok.getId()))
            {
            style += "color:red; ";
            }

          boolean underline=false;
          if (!currentpointer.isEmpty()) for (List<Long> pi : currentpointer)
          {
              if (!pi.isEmpty()) {
                style += "cursor:pointer;";
                underline=true;
                onclick = "togglePRAuto(this);";
                break;
              }
          }

            writer.append("<table border=\"0\" style=\"float:left; font-size:11px; border-collapse: collapse\" cellspacing=\"0\" cellpadding=\"0\">");
            if (underline) {
                boolean firstone=true;
                if (!currentpointer.isEmpty()) for (List<Long> pi : currentpointer){
                     String left = "", right = "";
                     int first=2;
                     if (!pi.isEmpty()) for (long l : pi) {
                         if (first==2) {first--;right+=""+l;} else
                            if (first==1) {
                                left += "" + l;first--;
                            } else left += ","+l;
                     }
                    //right=left;
                    if (firstone) {
                        //String help="";
                        //for (List<Long> di: currentpointer) help+=","+di.toArray()[0];
                        writer.append("<tr><td id=\"tok_"
                        + tok.getId() + "\" " + " style=\""
                        + style + "\" onclick=\""
                        + onclick + "\" annis:pr_left=\""
                        + left + "\" annis:pr_right=\""
                        + right + "\" > &nbsp;" + tok.getText() + "&nbsp; </td></tr>");
                        writer.append("<tr><td><table border=\"0\" width=\"100%\" style=\"border-collapse: collapse \">");
                        //writer.append("<tr><td height=\"n: "+maxlinkcount*5+"px\"></td></tr>");
                        //if (++textcount>20) {textcount=0;writer.flush();}
                        firstone=false;
                    }
                    currentlinkcount++;
                    //while we've got underlines
                    if (pi.isEmpty()) {
                        writer.append("<tr><td height=\"5px\"></td></tr>");
                    }else{
                        int color = 0;
                        if (colorlist.containsKey(pi.get(0).intValue())) { color = colorlist.get(pi.get(0).intValue());
                        }  else {
                            color = getNewColor(pi.get(0).intValue());
                            colorlist.put(pi.get(0).intValue(), color);
                        }
                        if (color>16777215) color =16777215;
                        writer.append("<tr><td height=\"3px\" width=\"100%\" "
                        //+"id=\"tok_" + tok.getId() + "_"+currentlinkcount + "\" "
                        + " style=\"" + style + "\" onclick=\""
                        + onclick +"\" annis:pr_left=\""
                        + left + "\"annis:pr_right=\""
                        + right + "\" style=\"border-top:1\" " + "BGCOLOR=\""+
                       Integer.toHexString(color) //6710937) // "#666699"
                       + "\"></td></tr>");
                        writer.append("<tr><td height=\"2px\"></td></tr>");
                    }
                    //if (++textcount>20) {textcount=0;writer.flush();}

                    //writer.append("<tr><td height=\"3px\" width=\"100%\" style=\"border-top:1\" BGCOLOR=\"#112299\" /></tr>");
                    //writer.append("<tr><td height=\"2px\"></td></tr>");
                }
                if (currentlinkcount>maxlinkcount) maxlinkcount=currentlinkcount;
                else {
                    if (currentlinkcount<maxlinkcount) writer.append("<tr><td height=\"n: "+(maxlinkcount-currentlinkcount)*5+"px\"></td></tr>");
                }
                writer.append("</table></td></tr>");
            } else {
                writer.append("<tr><td id=\"tok_"
                + tok.getId() + "\" " + " style=\""
                + style + "\" onclick=\""
                + onclick + "\" > &nbsp;" + tok.getText() + "&nbsp; </td></tr>");
                if (maxlinkcount>0){
                    writer.append("<tr><td><table border=\"0\" width=\"100%\" style=\"border-collapse: collapse \">");
                    writer.append("<tr><td height=\"n: "+maxlinkcount*5+"px\"></td></tr>");
                    writer.append("</table></td></tr>");
                }
                //if (++textcount>20) {textcount=0;writer.flush();}
            }
            writer.append("</table>");//*/
            //if (++textcount>20) {textcount=0;writer.flush();}
      }//*/
      //writer.append("Real End");
      writer.append("</body></html>");
    }
    catch(IOException ex)
    {
      Logger.getLogger(CorefVisualizer.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  /**@Override
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
  }*/

  private void recursiveSpan2Tok(AnnisNode n, long tokenID)
  {
    for(Edge e : n.getIncomingEdges())
    {
      if(e.getEdgeType() == Edge.EdgeType.COVERAGE || e.getEdgeType() == Edge.EdgeType.DOMINANCE)
      {
        AnnisNode pre = e.getSource();

        if(pre != null)
        {
          if(span2tok.get(pre.getId()) == null)
          {
            span2tok.put(pre.getId(), new HashSet<Long>());
          }

          if(!span2tok.get(pre.getId()).contains(tokenID))
          {
            span2tok.get(pre.getId()).add(tokenID);
            recursiveSpan2Tok(pre, tokenID);
          }
        }
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

    boolean underline=false;
    if(counterL > 0 || counterR > 0)
    {
      //style += "text-decoration:underline;cursor:pointer;";
      style += "cursor:pointer;";
      underline=true;
      onmouseover = "";
      onmouseout = "";
      onclick = "togglePRAuto(this);";
    }

    int currentcount=0;

    //writer.append("<span id=\"tok_" + id + "\" " + "style=\"" + style + "\" onmouseover=\"" + onmouseover + "\" onmouseout=\"" + onmouseout + "\" onclick=\"" + onclick + "\" annis:pr_left=\"" + left + "\" annis:pr_right=\"" + right + "\" >");
    //writer.append(id2Text.get(id));
    //writer.append(" ");
    //writer.append("</span>");
    writer.append("<table border=\"0\" style=\"float:left; font-size:11px; border-collapse: collapse \" cellspacing=\"0\" cellpadding=\"0\">");
    writer.append("<tr><td id=\"tok_" 
            + id + "\" " + " style=\""
            + style + "\" onmouseover=\"" 
            + onmouseover + "\" onmouseout=\"" 
            + onmouseout + "\" onclick=\"" 
            + onclick + "\" annis:pr_left=\"" 
            + left + "\" annis:pr_right=\"" 
            + right + "\" > &nbsp;" + id2Text.get(id) + "&nbsp; </td></tr>");
    if (underline) {
        currentcount++;
        writer.append("<tr><td><table border=\"0\" width=\"100%\" style=\"border-collapse: collapse \">");
        //while we've got underlines
        writer.append("<tr><td height=\"3px\" width=\"100%\" style=\"border-top:1\" BGCOLOR=\"#FF6699\" /></tr>");
        writer.append("<tr><td height=\"2px\"></td></tr>");
            writer.append("<tr><td height=\"3px\" width=\"100%\" style=\"border-top:1\" BGCOLOR=\"#112299\" /></tr>");
            writer.append("<tr><td height=\"2px\"></td></tr>");
            currentcount++;
      writer.append("</table></td></tr>");
      if (currentcount>maxcount) maxcount=currentcount;
    } else {
        writer.append("<tr><td><table border=\"0\" width=\"100%\" style=\"border-collapse: collapse \">");
        writer.append("<tr><td height=\""+maxcount*5+"px\"></td></tr>");
        writer.append("</table></td></tr>");
    }
    writer.append("</table>");

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

  private List<List<Long>> getAllRelatedTokenIds(long id){
      List<List<Long>> result = new LinkedList<List<Long>>();
      for (Pointerinfo pi : tokenpointer) if (pi.id.equals(id)) {
          for (long l : pi.list){
              List<Long> newlist = new LinkedList<Long>();
              newlist.add(l); //First value = Pointer id
              //newlist.add(pi.id);
              for (Pointerinfo di : pointertoken) if (!di.list.isEmpty() && di.id.equals(l)){//(pi.list.contains(di.id)){
                newlist.addAll(di.list);
                break;
              }
              result.add(newlist);
          }
          break;
      }
      return result;
  }

  private List<Long> getAllTokensFromEdge(Edge e, long edgenumber){
      List<Long> result = new LinkedList<Long>();
      AnnisNode a = e.getDestination();
      AnnisNode b = e.getSource();
      if ((a != null && pointermap.containsKey(a.getId())) && (b != null && pointermap.containsKey(b.getId())) ) return result;
      if (a != null && !pointermap.containsKey(a.getId())) {
        pointermap.put(e.getDestination().getId(), edgenumber);
      if (a.isToken()) {
          result.add(a.getId());
      }else{
          for (Edge ed : a.getOutgoingEdges()) {
            if (ed.getEdgeType()==Edge.EdgeType.COVERAGE){
                List<Long> resultb = getAllTokenChilds(ed.getDestination());
                for (Long l : resultb){
                    if(!result.contains(l)) result.add(l);
                }
            } else if (ed.getEdgeType()==Edge.EdgeType.POINTING_RELATION && !ed.equals(e)) {
                List<Long> resultb=null;
                //if (ed.getDestination().getId() != a.getId()) {if (ed.getDestination() != null) resultb = getAllTokensFromEdgeDest(ed,edgenumber);}
                //else {if (ed.getSource() != null) resultb = getAllTokensFromEdgeSource(ed,edgenumber);}
                resultb = getAllTokensFromEdge(ed,edgenumber);
                if (resultb != null)
                    for (Long l : resultb){
                        if(!result.contains(l)) result.add(l);
                    }
            }
        }
        for (Edge ed : a.getIncomingEdges()) {
            if (ed.getEdgeType()==Edge.EdgeType.POINTING_RELATION && !ed.equals(e)) {
                List<Long> resultb = null;
                //if (ed.getDestination().getId() != a.getId()) {if (ed.getDestination() != null) resultb = getAllTokensFromEdgeDest(ed,edgenumber);}
                //else {if (ed.getSource() != null) resultb = getAllTokensFromEdgeSource(ed,edgenumber);}
                resultb = getAllTokensFromEdge(ed,edgenumber);
                if (resultb != null)
                for (Long l : resultb){
                    if(!result.contains(l)) result.add(l);
                }
            }
        }
      }
      }
      if (b != null && !pointermap.containsKey(b.getId())) {
        pointermap.put(e.getSource().getId(), edgenumber);
      if (b.isToken()) {
          if(!result.contains(b.getId())) result.add(b.getId());
      }else{
          for (Edge ed : b.getOutgoingEdges()) {
              if (ed.getEdgeType()==Edge.EdgeType.COVERAGE) {
                List<Long> resultb = getAllTokenChilds(ed.getDestination());
                for (Long l : resultb){
                    if(!result.contains(l)) result.add(l);
                }
              } else if (ed.getEdgeType()==Edge.EdgeType.POINTING_RELATION && !ed.equals(e)) {
                List<Long> resultb=null;
                //if (ed.getDestination().getId()!=b.getId()) {if (ed.getDestination() != null) resultb = getAllTokensFromEdgeDest(ed,edgenumber);}
                //else {if (ed.getDestination() != null) resultb = getAllTokensFromEdgeSource(ed,edgenumber);}
                resultb = getAllTokensFromEdge(ed,edgenumber);
                if (resultb != null)
                for (Long l : resultb){
                    if(!result.contains(l)) result.add(l);
                }
            }
          }
          for (Edge ed : b.getIncomingEdges()) {
              if (ed.getEdgeType()==Edge.EdgeType.POINTING_RELATION && !ed.equals(e)) {
                List<Long> resultb=null;
                //if (ed.getDestination().getId()!=b.getId()) {if (ed.getDestination() != null) resultb = getAllTokensFromEdgeDest(ed,edgenumber);}
                //else {if (ed.getSource() != null) resultb = getAllTokensFromEdgeSource(ed,edgenumber);}
                resultb = getAllTokensFromEdge(ed,edgenumber);
                if (resultb != null)
                for (Long l : resultb){
                    if(!result.contains(l)) result.add(l);
                }
            }
          }
      }
      }
      return result;
  }

  /**private List<Long> getAllTokensFromEdgeDest(Edge e, long edgenumber){
      List<Long> result = new LinkedList<Long>();
      AnnisNode a = e.getDestination();
      if (a == null || pointermap.containsKey(e.getSource().getId())) return result;
      pointermap.put(e.getDestination().getId(), edgenumber);
      if (a.isToken()) {
          result.add(a.getId());
      }else{
          for (Edge ed : a.getOutgoingEdges()) {
            if (ed.getEdgeType()==Edge.EdgeType.COVERAGE){
                List<Long> resultb = getAllTokenChilds(ed.getDestination());
                for (Long l : resultb){
                    if(!result.contains(l)) result.add(l);
                }
            } else if (ed.getEdgeType()==Edge.EdgeType.POINTING_RELATION && !ed.equals(e)) {
                List<Long> resultb= null;
                if (ed.getDestination().getId() != a.getId()) {if (ed.getDestination() != null) resultb = getAllTokensFromEdgeDest(ed,edgenumber);}
                else {if (ed.getSource() != null) resultb = getAllTokensFromEdgeSource(ed,edgenumber);}
                if (resultb != null)
                for (Long l : resultb){
                    if(!result.contains(l)) result.add(l);
                }
            }
        }
        for (Edge ed : a.getIncomingEdges()) {
            if (ed.getEdgeType()==Edge.EdgeType.POINTING_RELATION && !ed.equals(e)) {
                List<Long> resultb=null;
                if (ed.getDestination().getId() != a.getId()) {if (ed.getDestination() != null) resultb = getAllTokensFromEdgeDest(ed,edgenumber);}
                else {if (ed.getSource() != null) resultb = getAllTokensFromEdgeSource(ed,edgenumber);}
                if (resultb != null)
                for (Long l : resultb){
                    if(!result.contains(l)) result.add(l);
                }
            }
        }
      }
      return result;
  }//*/

  /**private List<Long> getAllTokensFromEdgeSource(Edge e, long edgenumber){
      List<Long> result = new LinkedList<Long>();
      AnnisNode b = e.getSource();
      if (b == null || pointermap.containsKey(e.getSource().getId())) return result;
      pointermap.put(e.getSource().getId(), edgenumber);
      if (b.isToken()) {
          if(!result.contains(b.getId())) result.add(b.getId());
      }else{
          for (Edge ed : b.getOutgoingEdges()) {
              if (ed.getEdgeType()==Edge.EdgeType.COVERAGE) {
                List<Long> resultb = getAllTokenChilds(ed.getDestination());
                for (Long l : resultb){
                    if(!result.contains(l)) result.add(l);
                }
              } else if (ed.getEdgeType()==Edge.EdgeType.POINTING_RELATION && !ed.equals(e)) {
                List<Long> resultb=null;
                if (ed.getDestination().getId() != b.getId()) {if (ed.getDestination() != null) resultb = getAllTokensFromEdgeDest(ed,edgenumber);}
                else {if (ed.getSource() != null) resultb = getAllTokensFromEdgeSource(ed,edgenumber);}
                if (resultb != null)
                for (Long l : resultb){
                    if(!result.contains(l)) result.add(l);
                }
            }
          }
          for (Edge ed : b.getIncomingEdges()) {
              if (ed.getEdgeType()==Edge.EdgeType.POINTING_RELATION && !ed.equals(e)) {
                List<Long> resultb=null;
                if (ed.getDestination().getId()!=b.getId()) {if (ed.getDestination() != null) resultb = getAllTokensFromEdgeDest(ed,edgenumber);}
                else {if (ed.getDestination() != null) resultb = getAllTokensFromEdgeSource(ed,edgenumber);}
                if (resultb != null)
                for (Long l : resultb){
                    if(!result.contains(l)) result.add(l);
                }
            }
          }
      }
      return result;
  }//*/

   private List<Long> getAllTokenChilds(AnnisNode a){
      List<Long> result = new LinkedList<Long>();
      if (a.isToken()) {
          result.add(a.getId());
      }else{
          for (Edge ed : a.getOutgoingEdges()) if (ed.getEdgeType()==Edge.EdgeType.COVERAGE) {
              List<Long> resultb = getAllTokenChilds(ed.getDestination());
              for (Long l : resultb){
                if(!result.contains(l)) result.add(l);
              }
          }
      }
      return result;
  }


private int getNewColor(int i){
  int r = (((i)*224) % 255);
  int g = (((i + 197)*1034345) % 255);
  int b = (((i + 23)*74353) % 255);

  //  too dark or too bright?
  if(((r+b+g) / 3) < 100 )
  {
    r = 255 - r;
    g = 255 - g;
    b = 255 - b;
  }
  else if(((r+b+g) / 3) > 192 )
  {
    r = 1*(r / 2);
    g = 1*(g / 2);
    b = 1*(b / 2);
  }

  if(r == 255 && g == 255 && b == 255)
  {
    r = 255;
    g = 255;
    b = 0;
  }

  return (r*65536+g*256+b);
}

}
