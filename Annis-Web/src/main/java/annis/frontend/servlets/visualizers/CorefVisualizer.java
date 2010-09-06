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

  //private HashMap<Long, List<PR>> dst2PR;
  //private HashMap<Long, List<PR>> src2PR;
  //private HashMap<Long, List<PR>> dst2PR_tok;
  //private HashMap<Long, List<PR>> src2PR_tok;
  //private HashMap<Long, HashSet<Long>> span2tok;
  //private HashMap<Long, String> id2Text;
  private HashMap <Integer, Integer> colorlist;
  private HashMap<Long,Long> pointermap;
  private List<TypedNodeList> typelist;
  //private List<Long> tokenList;

  private List<Pointerinfo> pointertoken;
  private List<Pointerinfo> tokenpointer;
  class Pointerinfo{
      public Long id;
      public List<Long> list;
      Pointerinfo(){id=Long.MIN_VALUE; list =new LinkedList<Long>();}
      Pointerinfo(Long i, List<Long> l){id=i; list=l;}
  }
  class TypedNodeList{
      public String name;
      public HashMap <Long,Long> list;
      TypedNodeList(){
          name="";
          list=new HashMap<Long,Long>();
      }
  }

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
      long edgenumber=1;

      if (getResult()==null || getResult().getGraph() == null || getResult().getGraph().getEdges() == null){
          writer.append(" Error : Result is null </body></html>");return;
      }

      //pointermap = new HashMap<Long, Long>();
      typelist = new LinkedList<TypedNodeList>();
      for (Edge e : getResult().getGraph().getEdges()) if (e.getEdgeType()==Edge.EdgeType.POINTING_RELATION && e.getSource() != null && e.getDestination() != null) {
            pointermap=null;
            String currenttype = e.getName();
            for (int i=0; i<typelist.size();i++){
                if (typelist.get(i).name.equals(currenttype)) {
                    pointermap=typelist.get(i).list;
                    break;
                }
            }
            if (pointermap==null) {
                TypedNodeList laa = new TypedNodeList();
                laa.name=e.getName();
                pointermap = new HashMap<Long, Long>();
                laa.list=pointermap;
                typelist.add(laa);
            }
            if (pointermap.isEmpty() || !(pointermap.containsKey(e.getSource().getId()) && pointermap.containsKey(e.getDestination().getId()))){
                Pointerinfo info = new Pointerinfo();
                info.id=edgenumber;
                List<Long> liste = getAllTokensFromEdge(e,edgenumber,currenttype);
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
      
      //write
      List<List<Long>> currentpointer=new LinkedList<List<Long>>(), nextpointer=new LinkedList<List<Long>>();
      int maxlinkcount=0;
     
      List<AnnisToken> tokenlist = new LinkedList<AnnisToken>();
      tokenlist.addAll(getResult().getTokenList());
      for(AnnisToken tok : tokenlist) {
         int currentlinkcount=0;
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
                }
            }

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
                        String left2 = "", right2 = "";
                        for (List<Long> pi2 : currentpointer){
                            left2 = ""; right2 = "";
                            int first2=2;
                            if (!pi2.isEmpty()) for (long l : pi2) {
                                if (first2==2) {first2--;right2+=""+l;} else
                                    if (first2==1) {
                                        left2 += "" + l;first2--;
                                    } else left2 += ","+l;
                                }
                            if (!right2.equals("")) break;
                        }
                        writer.append("<tr><td id=\"tok_"
                        + tok.getId() + "\" " + " style=\""
                        + style + "\" onclick=\""
                        + onclick + "\" annis:pr_left=\""
                        + left2 + "\" annis:pr_right=\""
                        + right2 + "\" > &nbsp;" + tok.getText() + "&nbsp; </td></tr>");
                        writer.append("<tr><td><table border=\"0\" width=\"100%\" style=\"border-collapse: collapse \">");
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
            }
            writer.append("</table>");
      }
      writer.append("</body></html>");
    }
    catch(IOException ex)
    {
      Logger.getLogger(CorefVisualizer.class.getName()).log(Level.SEVERE, null, ex);
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

private List<Long> getAllTokensFromEdge(Edge e, long edgenumber, String searchedtype){
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
            } else if (ed.getEdgeType()==Edge.EdgeType.POINTING_RELATION && !ed.equals(e) && ed.getName().equals(searchedtype)) {
                List<Long> resultb=null;
                //if (ed.getDestination().getId() != a.getId()) {if (ed.getDestination() != null) resultb = getAllTokensFromEdgeDest(ed,edgenumber);}
                //else {if (ed.getSource() != null) resultb = getAllTokensFromEdgeSource(ed,edgenumber);}
                resultb = getAllTokensFromEdge(ed,edgenumber,searchedtype);
                if (resultb != null)
                    for (Long l : resultb){
                        if(!result.contains(l)) result.add(l);
                    }
            }
        }
        for (Edge ed : a.getIncomingEdges()) {
            if (ed.getEdgeType()==Edge.EdgeType.POINTING_RELATION && !ed.equals(e) && ed.getName().equals(searchedtype)) {
                List<Long> resultb = null;
                //if (ed.getDestination().getId() != a.getId()) {if (ed.getDestination() != null) resultb = getAllTokensFromEdgeDest(ed,edgenumber);}
                //else {if (ed.getSource() != null) resultb = getAllTokensFromEdgeSource(ed,edgenumber);}
                resultb = getAllTokensFromEdge(ed,edgenumber,searchedtype);
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
              } else if (ed.getEdgeType()==Edge.EdgeType.POINTING_RELATION && !ed.equals(e) && ed.getName().equals(searchedtype)) {
                List<Long> resultb=null;
                //if (ed.getDestination().getId()!=b.getId()) {if (ed.getDestination() != null) resultb = getAllTokensFromEdgeDest(ed,edgenumber);}
                //else {if (ed.getDestination() != null) resultb = getAllTokensFromEdgeSource(ed,edgenumber);}
                resultb = getAllTokensFromEdge(ed,edgenumber,searchedtype);
                if (resultb != null)
                for (Long l : resultb){
                    if(!result.contains(l)) result.add(l);
                }
            }
          }
          for (Edge ed : b.getIncomingEdges()) {
              if (ed.getEdgeType()==Edge.EdgeType.POINTING_RELATION && !ed.equals(e) && ed.getName().equals(searchedtype)) {
                List<Long> resultb=null;
                //if (ed.getDestination().getId()!=b.getId()) {if (ed.getDestination() != null) resultb = getAllTokensFromEdgeDest(ed,edgenumber);}
                //else {if (ed.getSource() != null) resultb = getAllTokensFromEdgeSource(ed,edgenumber);}
                resultb = getAllTokensFromEdge(ed,edgenumber,searchedtype);
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
