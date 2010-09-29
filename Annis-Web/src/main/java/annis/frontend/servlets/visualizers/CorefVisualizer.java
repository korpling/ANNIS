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
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.service.ifaces.AnnisResult;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thomas
 * @author Christian
 */
public class CorefVisualizer extends WriterVisualizer
{

  long globalIndex;
  List<TReferent> ReferentList;
  List<TComponent> Komponent;
  HashMap<Long,List<Long>> ComponentOfToken, TokensOfNode; //ReferentOfToken
  HashMap<Long,HashMap<Long, Integer>> ReferentOfToken; // the Long ist the Referend, the Integer means: { 0=incoming P-Edge, 1=outgoing P-Edge, 2=both}

  List<Long> visitedNodes;
  LinkedList<TComponenttype> Componenttype; //used to save which Node (with outgoing "P"-Edge) gelongs to which Component
  private HashMap <Integer, Integer> colorlist;

  class TComponenttype{
      String Type;
      List<Long> NodeList;
      TComponenttype() { Type="";NodeList=new LinkedList<Long>(); }
  }
  class TComponent{
      List<Long> TokenList;
      String Type;
      TComponent(){ TokenList = new LinkedList<Long>();Type="";  }
      TComponent(List<Long> ll, String t){ TokenList = ll;Type=t;  }
  }
  class TReferent{
      long Node;
      Set<Annotation> Annotations;
      String Type;
      long Component;
      TReferent(){ Node=-1;Component=-1;Type="";Annotations = new HashSet<Annotation>(); }
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
      writer.append("<link href=\"" + getContextPath() + "/javascript/extjs/resources/css/ext-all.css\" rel=\"stylesheet\" type=\"text/css\" >");//new
      writer.append("<script type=\"text/javascript\" src=\"" + getContextPath() + "/javascript/annis/visualizer/CorefVisualizer.js\"></script>");

      writer.append("</head>");
      writer.append("<body>");

      //get Info
      globalIndex = 0;
      int toolTipMaxLineCount = 1;
      TokensOfNode = new HashMap<Long,List<Long>>();//LinkedList<List<Long>>();
      ReferentList = new LinkedList<TReferent>();
      Komponent = new LinkedList<TComponent>();
      ReferentOfToken = new HashMap<Long,HashMap<Long, Integer>>();
      ComponentOfToken = new HashMap<Long,List<Long>>();
      Componenttype = new LinkedList<TComponenttype>();
      AnnisResult anResult = getResult(); if (anResult==null) { writer.append("An Error occured: Could not get Result (Result == null)</body>");return;}
      AnnotationGraph anGraph = anResult.getGraph(); if (anGraph==null) { writer.append("An Error occured: Could not get Graph of Result (Graph == null)</body>");return;}
      List<Edge> edgeList = anGraph.getEdges(); if (edgeList==null) return;
      for (Edge e : edgeList) if (e != null && e.getName()!=null && e.getEdgeType()==Edge.EdgeType.POINTING_RELATION && e.getSource() != null && e.getDestination() != null) {
          visitedNodes = new LinkedList<Long>();
          //got Type for this?
          boolean gotIt = false;
          int Componentnr;
          for (Componentnr=0;Componentnr<Componenttype.size(); Componentnr++){
              if (Componenttype.get(Componentnr)!=null && Componenttype.get(Componentnr).Type!=null && Componenttype.get(Componentnr).NodeList!=null &&
                      Componenttype.get(Componentnr).Type.equals(e.getName()) && Componenttype.get(Componentnr).NodeList.contains(e.getSource().getId())) {
                  gotIt=true;break;
              }
          }
          TComponent currentComponent;
          TComponenttype currentComponenttype;
          if (gotIt){
              currentComponent = Komponent.get(Componentnr);
              currentComponenttype = Componenttype.get(Componentnr);
          }else{
              currentComponenttype = new TComponenttype();
              currentComponenttype.Type=e.getName();
              Componenttype.add(currentComponenttype);
              Componentnr=Komponent.size();//Componenttype.size();
              currentComponent=new TComponent();
              currentComponent.Type=e.getName();
              currentComponent.TokenList = new LinkedList<Long>();
              Komponent.add(currentComponent);
              currentComponenttype.NodeList.add(e.getSource().getId());
          }
          TReferent Ref = new TReferent();
          Ref.Annotations=e.getAnnotations();
          Ref.Component=Componentnr;
          Ref.Node=e.getSource().getId();
          Ref.Type=e.getName();
          ReferentList.add(Ref);
          //Double Referent

          List<Long> currentTokens = getAllTokens(e.getSource(),e.getName(),currentComponenttype, true, Componentnr);

          setReferent(e.getDestination(), globalIndex,0);//neu
          setReferent(e.getSource(), globalIndex,1);//neu

          //e.getDestination().getId()
          for (Long l : currentTokens){
              if (!currentComponent.TokenList.contains(l)) currentComponent.TokenList.add(l);
          }

          globalIndex++;
      }

      colorlist = new HashMap<Integer,Integer>();

      //write Output
      List<Long> prevpositions, listpositions;
      List<Long> finalpositions = null;
      int maxlinkcount=0;
      Long lastId = null, currentId = null;
      for(AnnisToken tok : getResult().getTokenList()) {

          prevpositions = finalpositions;
          if (prevpositions!=null && prevpositions.size()<1) prevpositions=null;
          lastId = currentId;
          currentId = tok.getId();
          //listpositions = ReferentOfToken.get(currentId);
          listpositions = ComponentOfToken.get(currentId);
          List<Boolean> checklist = null;

          if (prevpositions==null && listpositions!=null) {
              finalpositions = listpositions;
         }else if (listpositions==null){
          finalpositions = new LinkedList<Long>();
         }else{
          checklist = new LinkedList<Boolean>();
          for (int i=0;i<prevpositions.size();i++) {
              if (listpositions.contains(prevpositions.get(i))) checklist.add(true); else checklist.add(false);
          }
          List<Long> remains = new LinkedList<Long>();
          for (int i=0;i<listpositions.size();i++) if (!prevpositions.contains(listpositions.get(i))) remains.add(listpositions.get(i));

          int minsize = checklist.size()+remains.size();
          int number = 0;
          finalpositions = new LinkedList<Long>();
          for (int i=0; i<minsize;i++){
              if (checklist.size()>i && checklist.get(i).booleanValue()) finalpositions.add(prevpositions.get(i));
              else {
                  if (remains.size()>number) { Long ll = remains.get(number);finalpositions.add(ll);number++;minsize--; }
                  else finalpositions.add(Long.MIN_VALUE);
              }
          }
         }

          //Write ///////////////////////////////////////////////////////
          String onclick="", style = "";
         if(getMarkableMap().containsKey("" + tok.getId()))
            {
            style += "color:red; ";
            }

          boolean underline=false;
          if (!finalpositions.isEmpty()) {
                style += "cursor:pointer;";
                underline=true;
                onclick = "togglePRAuto(this);";
          }

            writer.append("<table border=\"0\" style=\"float:left; font-size:11px; border-collapse: collapse\" cellspacing=\"0\" cellpadding=\"0\">");
            int currentlinkcount=0;
            if (underline) {
                boolean firstone=true;
                int index = -1;
                String tooltip = "";
                if (!finalpositions.isEmpty()) for (Long currentPositionComponent : finalpositions){
                     index++;
                     String left = "", right = "";
                     List<Long> pi;
                     //TReferent currentReferent=null;// not used
                     TComponent currentWriteComponent = null;// == pir
                     String currentType = "";
                     if (!currentPositionComponent.equals(Long.MIN_VALUE) && Komponent.size()>currentPositionComponent) {
                         currentWriteComponent = Komponent.get((int)(long)currentPositionComponent);
                         pi = currentWriteComponent.TokenList;
                         currentType = currentWriteComponent.Type;
                         left = ListToString(pi); right = ""+currentPositionComponent+1;
                     }
                    String Annotations = getAnnotations(tok.getId(), currentPositionComponent);
                    if (firstone) {
                      firstone=false;
                      if (currentWriteComponent==null){
                        //TReferent firstReferent=null;
                        String left2 = "", right2 = "";
                        List<Long> pi2;
                        long pr = 0;
                        TComponent currentWriteComponent2 = null;// == pir
                        String currentType2 = "";
                        String Annotations2 = "";
                        for (Long currentPositionComponent2 : finalpositions){
                            if (!currentPositionComponent2.equals(Long.MIN_VALUE) && Komponent.size()>currentPositionComponent2) {
                                currentWriteComponent2 = Komponent.get((int)(long)currentPositionComponent2);
                                pi2 = currentWriteComponent2.TokenList;
                                currentType2 = currentWriteComponent2.Type;
                                left2 = ListToString(pi2); right2 = ""+currentPositionComponent2+1;
                                Annotations2 = getAnnotations(tok.getId(),currentPositionComponent2);
                                pr = currentPositionComponent2;
                                break;
                            }
                        }
                        tooltip = "ext:qtip=\"<b>Component</b>: "+(pr+1)+", <b>Type</b>: "+currentType2+Annotations2+"\"";
                        if (tooltip.length()/40+1>toolTipMaxLineCount) toolTipMaxLineCount = tooltip.length()/40+1;
                        writer.append("<tr><td nowrap id=\"tok_"
                        + tok.getId() + "\" " + tooltip + " style=\""
                        + style + "\" onclick=\""
                        + onclick + "\" annis:pr_left=\""
                        + left2 + "\" annis:pr_right=\""
                        + right2 + "\" > &nbsp;" + tok.getText() + "&nbsp; </td></tr>");
                        //writer.append("<tr><td><table border=\"0\" width=\"100%\" style=\"border-collapse: collapse \">");
                      }else{//easier
                        tooltip = "ext:qtip=\"<b>Component</b>: "+(currentPositionComponent+1)+", <b>Type</b>: "+currentType+Annotations+"\"";
                        if (tooltip.length()/40+1>toolTipMaxLineCount) toolTipMaxLineCount = tooltip.length()/40+1;
                        writer.append("<tr><td nowrap id=\"tok_"
                        + tok.getId() + "\" " + tooltip + " style=\""
                        + style + "\" onclick=\""
                        + onclick + "\" annis:pr_left=\""
                        + left + "\" annis:pr_right=\""
                        + right + "\" > &nbsp;" + tok.getText() + "&nbsp; </td></tr>");
                        //writer.append("<tr><td><table border=\"0\" width=\"100%\" style=\"border-collapse: collapse \">");
                      }
                    }
                    currentlinkcount++;
                    //while we've got underlines
                    if (currentPositionComponent.equals(Long.MIN_VALUE)) {
                        writer.append("<tr><td height=\"5px\"></td></tr>");
                    }else{
                        int color = 0;
                        if (colorlist.containsKey((int)(long)currentPositionComponent)) { color = colorlist.get((int)(long)currentPositionComponent);
                        }  else {
                            color = getNewColor((int)(long)currentPositionComponent);
                            colorlist.put((int)(long)currentPositionComponent, color);
                        }
                        if (color>16777215) color =16777215;

                        String addition = ";border-style: solid; border-width: 0px 0px 0px 0px; border-color: white "; //"";
                        if (checklist!=null && checklist.size()>index && checklist.get(index).booleanValue()==true){
                            boolean connection =false;
                            if (lastId!=null && currentId!=null && !lastId.equals(currentId) && ReferentOfToken.get(lastId)!=null && ReferentOfToken.get(currentId)!=null)
                                for (long l : ReferentOfToken.get(lastId).keySet()) if (ReferentList.get((int)l) != null && ReferentOfToken.get(lastId).get(l).equals(1)
                                        && currentPositionComponent.equals(ReferentList.get((int)l).Component)
                                        && ReferentOfToken.get(currentId).containsKey(l) && ReferentOfToken.get(currentId).get(l).equals(1)){ connection=true; break; }
                            if (!connection) addition = ";border-style: solid; border-width: 0px 0px 0px 2px; border-color: white ";
                        }else  addition = ";border-style: solid; border-width: 0px 0px 0px 2px; border-color: white ";//2px

                        //String addition =  ";border-style: solid; border-width: 0px 0px 0px 2px; border-color: white ";
                        if (prevpositions!=null && prevpositions.contains(currentPositionComponent)) addition="";
                        tooltip = "ext:qtip=\"<b>Component</b>: "+(currentPositionComponent+1)+", <b>Type</b>: "+currentType+Annotations+"\"";
                        if (tooltip.length()/40+1>toolTipMaxLineCount) toolTipMaxLineCount = tooltip.length()/40+1;

                        writer.append("<tr><td><table border=\"0\" width=\"100%\" style=\"border-collapse: collapse \">");//
                        writer.append("<tr><td height=\"3px\" width=\"100%\" "
                        //+"id=\"tok_" + tok.getId() + "_"+currentlinkcount + "\" "
                        + " style=\"" + style + addition + "\" onclick=\""
                        + onclick +"\" annis:pr_left=\""
                        + left + "\"annis:pr_right=\""
                        + right + "\" "+ tooltip + "BGCOLOR=\""+
                       Integer.toHexString(color) + "\"></td></tr>");
                        writer.append("<tr><td height=\"2px\"></td></tr>");
                        writer.append("</table></td></tr>");//
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
      writer.append("<table border=\"0\" style=\"float:left; font-size:11px; border-collapse: collapse\" cellspacing=\"0\" cellpadding=\"0\">");
      writer.append("<tr><td><table border=\"0\" width=\"100%\" style=\"border-collapse: collapse \">");
      if (toolTipMaxLineCount>10) toolTipMaxLineCount=10;
      writer.append("<tr><td height=\"n: "+(toolTipMaxLineCount*15+15)+"px\"></td></tr>");
      writer.append("</table></td></tr>");

      writer.append("</body></html>");
    }
    catch(IOException ex)
    {
      Logger.getLogger(CorefVisualizer.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

//neu
 private List<Long> getAllTokens(AnnisNode a, String type, TComponenttype c,boolean b, long cnr){
     List<Long> result = null;
     if (!visitedNodes.contains(a.getId())){
         result = new LinkedList<Long>();
         visitedNodes.add(a.getId());
         if (TokensOfNode.containsKey(a.getId())){
             for (Long l : TokensOfNode.get(a.getId())){
                result.add(l);
                if (ComponentOfToken.get(l)==null){
                    List<Long> newlist = new LinkedList<Long>();
                    newlist.add(cnr);
                    ComponentOfToken.put(l, newlist);
                }else{
                    if (!ComponentOfToken.get(l).contains(cnr)) ComponentOfToken.get(l).add(cnr);
                }
                /*if (b) {if (ReferentOfToken.get(l)==null) {
                        HashMap<Long, Integer> newlist = new HashMap<Long, Integer>();
                        newlist.put(globalIndex, 1);
                        ReferentOfToken.put(l, newlist);
                    } else if (!ReferentOfToken.get(l).containsKey(globalIndex)){ ReferentOfToken.get(l).put(globalIndex, 1);
                    }// else if(ReferentOfToken.get(l).get(globalIndex).equals(0)) ReferentOfToken.get(l).put(globalIndex, 2);
             }*/
         }
         }else{
            result = searchTokens(a,b,cnr);
            if (result!=null){
                TokensOfNode.put(a.getId(), result);
                //for (Long l: result) if (!ReferentOfToken.get(a.getId()).contains(l)) ReferentOfToken.get(a.getId()).add(l);
             }
         }
         //get "P"-Edges!
         for (Edge e : a.getOutgoingEdges())if (e.getName()!=null && e.getEdgeType()==Edge.EdgeType.POINTING_RELATION && e.getSource() != null && e.getDestination() != null && !visitedNodes.contains(e.getDestination().getId())) {
             c.NodeList.add(e.getDestination().getId());
             List<Long> Med = getAllTokens(e.getDestination(), type, c, false, cnr);
             for (Long l : Med) if (!result.contains(l)) result.add(l);
         }
         for (Edge e : a.getIncomingEdges())if (e.getName()!=null && e.getEdgeType()==Edge.EdgeType.POINTING_RELATION && e.getSource() != null && e.getDestination() != null && !visitedNodes.contains(e.getSource().getId())) {
             c.NodeList.add(e.getSource().getId());
             List<Long> Med = getAllTokens(e.getSource(), type,c, false, cnr);
             for (Long l : Med) if (!result.contains(l)) result.add(l);
         }
     }
     return result;
 }

 private void setReferent(AnnisNode a, long index, int value){
     if (a.isToken()){
            if (!ReferentOfToken.containsKey(a.getId())) {
                        HashMap<Long, Integer> newlist = new HashMap<Long, Integer>();
                        newlist.put(globalIndex, value);
                        ReferentOfToken.put(a.getId(), newlist);
            } else { ReferentOfToken.get(a.getId()).put(globalIndex, value);}
     }else{
         for (Edge e : a.getOutgoingEdges()) if (e.getEdgeType()!=Edge.EdgeType.POINTING_RELATION && e.getSource() != null && e.getDestination() != null) {
             setReferent(e.getDestination(), index, value);
         }
     }
 }

 private List<Long> searchTokens(AnnisNode a,boolean b,long cnr){
     List<Long> result = new LinkedList<Long>();
     if (a.isToken()){
         result.add(a.getId());
         if (ComponentOfToken.get(a.getId())==null){
             List<Long> newlist = new LinkedList<Long>();
             newlist.add(cnr);
             ComponentOfToken.put(a.getId(), newlist);
         }else{
             List<Long> newlist = ComponentOfToken.get(a.getId());
             if (!newlist.contains(cnr)) newlist.add(cnr);
         }
         /*if (b){
                    if (ReferentOfToken.get(a.getId())==null) {
                        HashMap<Long, Integer> newlist = new HashMap<Long, Integer>();
                        newlist.put(globalIndex, 1);
                        ReferentOfToken.put(a.getId(), newlist);
                    } else if (!ReferentOfToken.get(a.getId()).containsKey(globalIndex)){ ReferentOfToken.get(a.getId()).put(globalIndex, 1);
                    }// else if (ReferentOfToken.get(a.getId()).get(globalIndex).equals(1)) ReferentOfToken.get(a.getId()).put(globalIndex, 2);
         }//*/
     }else{
         for (Edge e : a.getOutgoingEdges()) if (e.getEdgeType()!=Edge.EdgeType.POINTING_RELATION && e.getSource() != null && e.getDestination() != null) {
             List<Long> Med = searchTokens(e.getDestination(),b,cnr);
             for (Long l : Med) if (!result.contains(l)) result.add(l);
         }
     }
     return result;
 }

 private String getAnnotations(Long id, long component){
     String result = "";
     String incoming = "", outgoing = "";
     int nri = 1, nro = 1;//, referents = 0;
     for (long l :ReferentOfToken.get(id).keySet()){
         if (ReferentList.get((int)(long)l)!=null && ReferentList.get((int)(long)l).Component==component
                 && ReferentList.get((int)(long)l).Annotations != null && ReferentList.get((int)(long)l).Annotations.size()>0){
             //referents++;
             //if (referents>1) result += "[";
             int num = ReferentOfToken.get(id).get(l);
             if (num == 0 || num == 2) {
                for (Annotation an : ReferentList.get((int)(long)l).Annotations) {
                    if (nri == 1) { incoming = ", <b>incoming Annotations</b>: "+an.getName()+"="+an.getValue();nri--;
                    } else { incoming += ", "+an.getName()+"="+an.getValue();}}
             }
             if (num == 1 || num == 2) {
                for (Annotation an : ReferentList.get((int)(long)l).Annotations) {
                    if (nro == 1) { outgoing = ", <b>outgoing Annotations</b>: "+an.getName()+"="+an.getValue();nro--; // remove l+"- "+
                    } else { outgoing += ", "+an.getName()+"="+an.getValue();}}
             }
         }
     }
     if (nro<1) result+=outgoing;
     if (nri<1) result+=incoming;
     return result;
 }

 private String ListToString(List<Long> ll){
     String result = "";
     int i = 1;
     for (Long l : ll) {
         if (i == 1) { i=0;result+=""+l; }
         else result+=","+l;
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
