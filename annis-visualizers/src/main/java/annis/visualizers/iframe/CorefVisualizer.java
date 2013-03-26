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
package annis.visualizers.iframe;

import annis.libgui.visualizers.VisualizerInput;
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
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

/**
 * A view of the entire text of a document, possibly with interactive 
 * coreference links. 
 * It is possible to use this visualization to view entire texts
 * even if you do not have coreference annotations.
 * 
 * @author Thomas Krause
 * @author Christian Schulz-Hanke
 */
@PluginImplementation
public class CorefVisualizer extends WriterVisualizer
{
  
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(CorefVisualizer.class);

  long globalIndex;
  List<TReferent> ReferentList;
  List<TComponent> Komponent;
  HashMap<Long, List<Long>> ComponentOfToken, TokensOfNode; //ReferentOfToken
  HashMap<Long, HashMap<Long, Integer>> ReferentOfToken; // the Long ist the Referend, the Integer means: { 0=incoming P-Edge, 1=outgoing P-Edge, 2=both(not used anymore)}
  List<Long> visitedNodes;
  LinkedList<TComponenttype> Componenttype; //used to save which Node (with outgoing "P"-Edge) gelongs to which Component
  private HashMap<Integer, Integer> colorlist;

  static class TComponenttype
  {

    String Type;
    List<Long> NodeList;

    TComponenttype()
    {
      Type = "";
      NodeList = new LinkedList<Long>();
    }
  }

  static class TComponent
  {

    List<Long> TokenList;
    String Type;

    TComponent()
    {
      TokenList = new LinkedList<Long>();
      Type = "";
    }

    TComponent(List<Long> ll, String t)
    {
      TokenList = ll;
      Type = t;
    }
  }

  static class TReferent
  {

    Set<Annotation> Annotations;
    long Component;

    TReferent()
    {
      Component = -1;
      Annotations = new HashSet<Annotation>();
    }
  }

  @Override
  public String getShortName()
  {
    return "discourse";
  }

  @Override
  public boolean isUsingText()
  {
    return true;
  }
  
  

  /**
   * writes Output for the CorefVisualizer
   * @param writer writer to write with
   */
  @Override
  public void writeOutput(VisualizerInput input, Writer w)
  {
    try
    {
      println("<html>", w);
      println("<head>", w);
      
      LinkedList<String> fonts = new LinkedList<String>();
      if(input.getFont() != null)
      {
        fonts.add(input.getFont().getName());
        println("<link href=\"" 
        + input.getFont().getUrl()
        + "\" rel=\"stylesheet\" type=\"text/css\" >", w);
      }
      fonts.add("serif");

      println("<link href=\"" 
        + input.getResourcePath("coref/jquery.tooltip.css")
        +"\" rel=\"stylesheet\" type=\"text/css\" >", w);
      
      println("<script type=\"text/javascript\" src=\"" 
        + input.getResourcePath("coref/jquery-1.6.2.min.js")
        +"\"></script>", w);
      println("<script type=\"text/javascript\" src=\"" 
        + input.getResourcePath("coref/jquery.tooltip.min.js") 
        +"\"></script>", w);
      
      println("<link href=\"" 
        + input.getResourcePath("coref/coref.css")
        + "\" rel=\"stylesheet\" type=\"text/css\" >", w);
      println("<script type=\"text/javascript\" src=\"" 
        + input.getResourcePath("coref/CorefVisualizer.js")
        + "\"></script>", w);

      println("</head>", w);
     
      println("<body style=\"font-family: '" + StringUtils.join(fonts, "', '") + "';\" >", w);
      
      //get Info
      globalIndex = 0;
      int toolTipMaxLineCount = 1;
      TokensOfNode = new HashMap<Long, List<Long>>();
      ReferentList = new LinkedList<TReferent>();
      Komponent = new LinkedList<TComponent>();
      ReferentOfToken = new HashMap<Long, HashMap<Long, Integer>>();
      ComponentOfToken = new HashMap<Long, List<Long>>();
      Componenttype = new LinkedList<TComponenttype>();
      AnnisResult anResult = input.getResult();
      
      AnnotationGraph anGraph = anResult.getGraph();
      if (anGraph == null)
      {
        println("An Error occured: Could not get Graph of Result (Graph == null)</body>", w);
        return;
      }
      List<Edge> edgeList = anGraph.getEdges();
      if (edgeList == null)
      {
        return;
      }

      for (Edge e : edgeList)
      {
        if (includeEdge(e, input.getNamespace()))
        {
          visitedNodes = new LinkedList<Long>();
          //got Type for this?
          boolean gotIt = false;
          int Componentnr;
          for (Componentnr = 0; Componentnr < Componenttype.size(); Componentnr++)
          {
            if (Componenttype.get(Componentnr) != null && Componenttype.get(Componentnr).Type != null && Componenttype.get(Componentnr).NodeList != null
              && Componenttype.get(Componentnr).Type.equals(e.getName()) && Componenttype.get(Componentnr).NodeList.contains(e.getSource().getId()))
            {
              gotIt = true;
              break;
            }
          }
          TComponent currentComponent;
          TComponenttype currentComponenttype;
          if (gotIt)
          {
            currentComponent = Komponent.get(Componentnr);
            currentComponenttype = Componenttype.get(Componentnr);
          }
          else
          {
            currentComponenttype = new TComponenttype();
            currentComponenttype.Type = e.getName();
            Componenttype.add(currentComponenttype);
            Componentnr = Komponent.size();
            currentComponent = new TComponent();
            currentComponent.Type = e.getName();
            currentComponent.TokenList = new LinkedList<Long>();
            Komponent.add(currentComponent);
            currentComponenttype.NodeList.add(e.getSource().getId());
          }
          TReferent Ref = new TReferent();
          Ref.Annotations = e.getAnnotations();
          Ref.Component = Componentnr;
          ReferentList.add(Ref);

          List<Long> currentTokens = getAllTokens(e.getSource(), e.getName(), 
            currentComponenttype, Componentnr, input.getNamespace());

          setReferent(e.getDestination(), globalIndex, 0);//neu
          setReferent(e.getSource(), globalIndex, 1);//neu

          for (Long l : currentTokens)
          {
            if (!currentComponent.TokenList.contains(l))
            {
              currentComponent.TokenList.add(l);
            }
          }

          globalIndex++;
        }
      }

      colorlist = new HashMap<Integer, Integer>();

      //write Output
      List<Long> prevpositions, listpositions;
      List<Long> finalpositions = null;
      int maxlinkcount = 0;
      Long lastId = null, currentId = null;
      for (AnnisToken tok : input.getResult().getTokenList())
      {

        prevpositions = finalpositions;
        if (prevpositions != null && prevpositions.size() < 1)
        {
          prevpositions = null;
        }
        lastId = currentId;
        currentId = tok.getId();
        listpositions = ComponentOfToken.get(currentId);
        List<Boolean> checklist = null;

        if (prevpositions == null && listpositions != null)
        {
          finalpositions = listpositions;
        }
        else if (listpositions == null)
        {
          finalpositions = new LinkedList<Long>();
        }
        else
        {
          checklist = new LinkedList<Boolean>();
          for (int i = 0; i < prevpositions.size(); i++)
          {
            if (listpositions.contains(prevpositions.get(i)))
            {
              checklist.add(true);
            }
            else
            {
              checklist.add(false);
            }
          }
          List<Long> remains = new LinkedList<Long>();
          for (int i = 0; i < listpositions.size(); i++)
          {
            if (!prevpositions.contains(listpositions.get(i)))
            {
              remains.add(listpositions.get(i));
            }
          }

          int minsize = checklist.size() + remains.size();
          int number = 0;
          finalpositions = new LinkedList<Long>();
          for (int i = 0; i < minsize; i++)
          {
            if (checklist.size() > i && checklist.get(i).booleanValue())
            {
              finalpositions.add(prevpositions.get(i));
            }
            else
            {
              if (remains.size() > number)
              {
                Long ll = remains.get(number);
                finalpositions.add(ll);
                number++;
                minsize--;
              }
              else
              {
                finalpositions.add(Long.MIN_VALUE);
              }
            }
          }
        }

        String onclick = "", style = "";
        if (input.getMarkableMap().containsKey("" + tok.getId()))
        {
          style += "color:red; ";
        }

        boolean underline = false;
        if (!finalpositions.isEmpty())
        {
          style += "cursor:pointer;";
          underline = true;
          onclick = "togglePRAuto(this);";
        }

        println("<table border=\"0\" style=\"float:left; font-size:11px; border-collapse: collapse\" cellspacing=\"0\" cellpadding=\"0\">", w);
        int currentlinkcount = 0;
        if (underline)
        {
          boolean firstone = true;
          int index = -1;
          String tooltip = "";
          if (!finalpositions.isEmpty())
          {
            for (Long currentPositionComponent : finalpositions)
            {
              index++;
              String left = "", right = "";
              List<Long> pi;
              TComponent currentWriteComponent = null;// == pir
              String currentType = "";
              if (!currentPositionComponent.equals(Long.MIN_VALUE) && Komponent.size() > currentPositionComponent)
              {
                currentWriteComponent = Komponent.get((int) (long) currentPositionComponent);
                pi = currentWriteComponent.TokenList;
                currentType = currentWriteComponent.Type;
                left = ListToString(pi);
                right = "" + currentPositionComponent + 1;
              }
              String Annotations = getAnnotations(tok.getId(), currentPositionComponent);
              if (firstone)
              {
                firstone = false;
                if (currentWriteComponent == null)
                {
                  String left2 = "", right2 = "";
                  List<Long> pi2;
                  long pr = 0;
                  TComponent currentWriteComponent2 = null;// == pir
                  String currentType2 = "";
                  String Annotations2 = "";
                  for (Long currentPositionComponent2 : finalpositions)
                  {
                    if (!currentPositionComponent2.equals(Long.MIN_VALUE) && Komponent.size() > currentPositionComponent2)
                    {
                      currentWriteComponent2 = Komponent.get((int) (long) currentPositionComponent2);
                      pi2 = currentWriteComponent2.TokenList;
                      currentType2 = currentWriteComponent2.Type;
                      left2 = ListToString(pi2);
                      right2 = "" + currentPositionComponent2 + 1;
                      Annotations2 = getAnnotations(tok.getId(), currentPositionComponent2);
                      pr = currentPositionComponent2;
                      break;
                    }
                  }
                  tooltip = "title=\" - <b>Component</b>: " + (pr + 1) + ", <b>Type</b>: " + currentType2 + Annotations2 + "\"";
                  if (tooltip.length() / 40 + 1 > toolTipMaxLineCount)
                  {
                    toolTipMaxLineCount = tooltip.length() / 40 + 1;
                  }
                  println("<tr><td nowrap id=\"tok_"
                    + tok.getId() + "\" " + tooltip + " style=\""
                    + style + "\" onclick=\""
                    + onclick + "\" annis:pr_left=\""
                    + left2 + "\" annis:pr_right=\""
                    + right2 + "\" > &nbsp;" + tok.getText() + "&nbsp; </td></tr>", w);
                }
                else
                {//easier
                  tooltip = "title=\" - <b>Component</b>: " + (currentPositionComponent + 1) + ", <b>Type</b>: " + currentType + Annotations + "\"";
                  if (tooltip.length() / 40 + 1 > toolTipMaxLineCount)
                  {
                    toolTipMaxLineCount = tooltip.length() / 40 + 1;
                  }
                  println("<tr><td nowrap id=\"tok_"
                    + tok.getId() + "\" " + tooltip + " style=\""
                    + style + "\" onclick=\""
                    + onclick + "\" annis:pr_left=\""
                    + left + "\" annis:pr_right=\""
                    + right + "\" > &nbsp;" + tok.getText() + "&nbsp; </td></tr>", w);
                }
              }
              currentlinkcount++;
              //while we've got underlines
              if (currentPositionComponent.equals(Long.MIN_VALUE))
              {
                println("<tr><td height=\"5px\"></td></tr>", w);
              }
              else
              {
                int color = 0;
                if (colorlist.containsKey((int) (long) currentPositionComponent))
                {
                  color = colorlist.get((int) (long) currentPositionComponent);
                }
                else
                {
                  color = getNewColor((int) (long) currentPositionComponent);
                  colorlist.put((int) (long) currentPositionComponent, color);
                }
                if (color > 16777215)
                {
                  color = 16777215;
                }

                String addition = ";border-style: solid; border-width: 0px 0px 0px 2px; border-color: white ";
                if (lastId != null && currentId != null && checklist != null && checklist.size() > index && checklist.get(index).booleanValue() == true)
                {
                  if (connectionOf(lastId, currentId, currentPositionComponent))
                  {
                    addition = "";
                  }
                }

                tooltip = "title=\" - <b>Component</b>: " + (currentPositionComponent + 1) + ", <b>Type</b>: " + currentType + Annotations + "\"";
                if (tooltip.length() / 40 + 1 > toolTipMaxLineCount)
                {
                  toolTipMaxLineCount = tooltip.length() / 40 + 1;
                }

                println("<tr><td><table border=\"0\" width=\"100%\" style=\"border-collapse: collapse \">", w);//
                println("<tr><td height=\"3px\" width=\"100%\" "
                  + " style=\"" + style + addition + "\" onclick=\""
                  + onclick + "\" annis:pr_left=\""
                  + left + "\"annis:pr_right=\""
                  + right + "\" " + tooltip + "BGCOLOR=\""
                  + Integer.toHexString(color) + "\"></td></tr>", w);
                println("<tr><td height=\"2px\"></td></tr>", w);
                println("</table></td></tr>", w);//
              }
            }
          }
          if (currentlinkcount > maxlinkcount)
          {
            maxlinkcount = currentlinkcount;
          }
          else
          {
            if (currentlinkcount < maxlinkcount)
            {
              println("<tr><td height=\"" + (maxlinkcount - currentlinkcount) * 5 + "px\"></td></tr>", w);
            }
          }
          println("</table></td></tr>", w);
        }
        else
        {
          println("<tr><td id=\"tok_"
            + tok.getId() + "\" " + " style=\""
            + style + "\" onclick=\""
            + onclick + "\" > &nbsp;" + tok.getText() + "&nbsp; </td></tr>", w);
          if (maxlinkcount > 0)
          {
            println("<tr><td><table border=\"0\" width=\"100%\" style=\"border-collapse: collapse \">", w);
            println("<tr><td height=\"" + maxlinkcount * 5 + "px\"></td></tr>", w);
            println("</table></td></tr>", w);
          }
        }
        println("</table>", w);
      }
      println("<table border=\"0\" style=\"float:left; font-size:11px; border-collapse: collapse\" cellspacing=\"0\" cellpadding=\"0\">", w);
      println("<tr><td><table border=\"0\" width=\"100%\" style=\"border-collapse: collapse \">", w);
      if (toolTipMaxLineCount > 10)
      {
        toolTipMaxLineCount = 10;
      }
      println("<tr><td height=\"n: " + (toolTipMaxLineCount * 15 + 15) + "px\"></td></tr>", w);
      println("</table></td></tr>", w);

      println("</body></html>", w);
    }
    catch (IOException ex)
    {
      log.error(null, ex);
    }
  }

  /**
   * collects all Tokens of the Component
   * @param a AnnisNode to start with
   * @param name String that determines which Component we search for
   * @param c Componenttype, that will include its Tokens
   * @param cnr Number of the Component
   * @return List of Tokens
   */
  private List<Long> getAllTokens(AnnisNode a, String name, TComponenttype c, long cnr, String namespace)
  {
    List<Long> result = null;
    if (!visitedNodes.contains(a.getId()))
    {
      result = new LinkedList<Long>();
      visitedNodes.add(a.getId());
      if (TokensOfNode.containsKey(a.getId()))
      {
        for (Long l : TokensOfNode.get(a.getId()))
        {
          result.add(l);
          if (ComponentOfToken.get(l) == null)
          {
            List<Long> newlist = new LinkedList<Long>();
            newlist.add(cnr);
            ComponentOfToken.put(l, newlist);
          }
          else
          {
            if (!ComponentOfToken.get(l).contains(cnr))
            {
              ComponentOfToken.get(l).add(cnr);
            }
          }
        }
      }
      else
      {
        result = searchTokens(a, cnr);
        if (result != null)
        {
          TokensOfNode.put(a.getId(), result);
        }
      }
      //get "P"-Edges!
      for (Edge e : a.getOutgoingEdges())
      {
        if (includeEdge(e, namespace) && name.equals(e.getName())
          && !visitedNodes.contains(e.getDestination().getId()))
        {
          c.NodeList.add(e.getDestination().getId());
          List<Long> Med = getAllTokens(e.getDestination(), name, c, cnr, namespace);
          for (Long l : Med)
          {
            if (!result.contains(l))
            {
              result.add(l);
            }
          }
        }
      }
      for (Edge e : a.getIncomingEdges())
      {
        if (includeEdge(e, namespace) && name.equals(e.getName())
          && !visitedNodes.contains(e.getSource().getId()))
        {
          c.NodeList.add(e.getSource().getId());
          List<Long> Med = getAllTokens(e.getSource(), name, c, cnr, namespace);
          for (Long l : Med)
          {
            if (!result.contains(l))
            {
              result.add(l);
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * adds a Referent for all Nodes dominated or covered by outgoing Edges of AnnisNode a
   * @param a the AnnisNode
   * @param index index of the Referent
   * @param value determines wheather the refered P-Edge is incoming (1) or outgoing (0)
   */
  private void setReferent(AnnisNode a, long index, int value)
  {
    if (a.isToken())
    {
      if (!ReferentOfToken.containsKey(a.getId()))
      {
        HashMap<Long, Integer> newlist = new HashMap<Long, Integer>();
        newlist.put(index, value);//globalindex?
        ReferentOfToken.put(a.getId(), newlist);
      }
      else
      {
        ReferentOfToken.get(a.getId()).put(globalIndex, value);
      }
    }
    else
    {
      for (Edge e : a.getOutgoingEdges())
      {
        if (e.getEdgeType() != Edge.EdgeType.POINTING_RELATION && e.getSource() != null && e.getDestination() != null)
        {
          setReferent(e.getDestination(), index, value);
        }
      }
    }
  }

  /**
   * Collects all Token dominated or covered by all outgoing Edges of AnnisNode a
   * @param a AnnisNode a
   * @param cnr ComponentNumber this tokens will be added for
   * @return List of Tokennumbers
   */
  private List<Long> searchTokens(AnnisNode a, long cnr)
  {
    List<Long> result = new LinkedList<Long>();
    if (a.isToken())
    {
      result.add(a.getId());
      if (ComponentOfToken.get(a.getId()) == null)
      {
        List<Long> newlist = new LinkedList<Long>();
        newlist.add(cnr);
        ComponentOfToken.put(a.getId(), newlist);
      }
      else
      {
        List<Long> newlist = ComponentOfToken.get(a.getId());
        if (!newlist.contains(cnr))
        {
          newlist.add(cnr);
        }
      }
    }
    else
    {
      for (Edge e : a.getOutgoingEdges())
      {
        if (e.getEdgeType() != Edge.EdgeType.POINTING_RELATION && e.getSource() != null && e.getDestination() != null)
        {
          List<Long> Med = searchTokens(e.getDestination(), cnr);
          for (Long l : Med)
          {
            if (!result.contains(l))
            {
              result.add(l);
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * Collects fitting annotations of an Token
   * @param id id of the given Token
   * @param component componentnumber of the line we need the annotations of
   * @return Annotations as a String
   */
  private String getAnnotations(Long id, long component)
  {
    String result = "";
    String incoming = "", outgoing = "";
    int nri = 1, nro = 1;

    if(ReferentOfToken.get(id) != null)
    {
      for (long l : ReferentOfToken.get(id).keySet())
      {
        if (ReferentList.get((int) (long) l) != null && ReferentList.get((int) (long) l).Component == component
          && ReferentList.get((int) (long) l).Annotations != null && ReferentList.get((int) (long) l).Annotations.size() > 0)
        {
          int num = ReferentOfToken.get(id).get(l);
          if (num == 0 || num == 2)
          {
            for (Annotation an : ReferentList.get((int) (long) l).Annotations)
            {
              if (nri == 1)
              {
                incoming = ", <b>incoming Annotations</b>: " + an.getName() + "=" + an.getValue();
                nri--;
              }
              else
              {
                incoming += ", " + an.getName() + "=" + an.getValue();
              }
            }
          }
          if (num == 1 || num == 2)
          {
            for (Annotation an : ReferentList.get((int) (long) l).Annotations)
            {
              if (nro == 1)
              {
                outgoing = ", <b>outgoing Annotations</b>: " + an.getName() + "=" + an.getValue();
                nro--; // remove l+"- "+
              }
              else
              {
                outgoing += ", " + an.getName() + "=" + an.getValue();
              }
            }
          }
        }
      }
    }
    if (nro < 1)
    {
      result += outgoing;
    }
    if (nri < 1)
    {
      result += incoming;
    }
    return result;
  }

  /**
   * Calculates wheather a line determinded by its Component should be discontinous
   * @param pre Id of the left token
   * @param now Id of the right token
   * @param currentComponent Number of the Component, number of variable "Komponent"
   * @return Should the line be continued?
   */
  private boolean connectionOf(long pre, long now, long currentComponent)
  {
    List<Long> prel = new LinkedList<Long>(), nowl = new LinkedList<Long>();
    if (pre != now && ReferentOfToken.get(pre) != null && ReferentOfToken.get(now) != null)
    {
      for (long l : ReferentOfToken.get(pre).keySet())
      {
        if (ReferentList.get((int) l) != null && ReferentList.get((int) l).Component == currentComponent
          && ReferentOfToken.get(pre).get(l).equals(0))
        {
          prel.add(l);
        }
      }
      for (long l : ReferentOfToken.get(now).keySet())
      {
        if (ReferentList.get((int) l) != null && ReferentList.get((int) l).Component == currentComponent
          && ReferentOfToken.get(now).get(l).equals(0))
        {
          nowl.add(l);
        }
      }
      for (long l : nowl)
      {
        if (prel.contains(l))
        {
          return true;
        }
      }
    }
    prel = new LinkedList<Long>();
    nowl = new LinkedList<Long>();
    if (pre != now && ReferentOfToken.get(pre) != null && ReferentOfToken.get(now) != null)
    {
      for (long l : ReferentOfToken.get(pre).keySet())
      {
        if (ReferentList.get((int) l) != null && ReferentList.get((int) l).Component == currentComponent
          && ReferentOfToken.get(pre).get(l).equals(1))
        {
          prel.add(l);
        }
      }
      for (long l : ReferentOfToken.get(now).keySet())
      {
        if (ReferentList.get((int) l) != null && ReferentList.get((int) l).Component == currentComponent
          && ReferentOfToken.get(now).get(l).equals(1))
        {
          nowl.add(l);
        }
      }
      for (long l : nowl)
      {
        if (prel.contains(l))
        {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Creates a proper String out of a List<Long>
   * @param ll List that should become a String
   * @return String
   */
  private String ListToString(List<Long> ll)
  {
    StringBuilder result = new StringBuilder();
    int i = 1;
    for (Long l : ll)
    {
      if (i == 1)
      {
        i = 0;
        result.append(l);
      }
      else
      {
        result.append(",");
        result.append(l);
      }
    }
    return result.toString();
  }

  /**
   * Returns a unique color-value for a given number
   * @param i identifer of an unique color
   * @return color-value
   */
  private int getNewColor(int i)
  {
    int r = (((i) * 224) % 255);
    int g = (((i + 197) * 1034345) % 255);
    int b = (((i + 23) * 74353) % 255);

    //  too dark or too bright?
    if (((r + b + g) / 3) < 100)
    {
      r = 255 - r;
      g = 255 - g;
      b = 255 - b;
    }
    else if (((r + b + g) / 3) > 192)
    {
      r = 1 * (r / 2);
      g = 1 * (g / 2);
      b = 1 * (b / 2);
    }

    if (r == 255 && g == 255 && b == 255)
    {
      r = 255;
      g = 255;
      b = 0;
    }

    return (r * 65536 + g * 256 + b);
  }

  private void println(String s, Writer writer) throws IOException
  {
    println(s, 0, writer);
  }

  private void println(String s, int indent, Writer writer) throws IOException
  {
    for(int i=0; i < indent; i++)
    {
      writer.append("\t");
    }
    writer.append(s);
    writer.append("\n");
  }

  private boolean includeEdge(Edge e, String namespace)
  {
    if (e != null && e.getName() != null
          && e.getEdgeType() == Edge.EdgeType.POINTING_RELATION && e.getSource() != null
          && e.getDestination() != null
          && e.getNamespace() != null 
          && e.getNamespace().equals(namespace))
    {
      return true;
    }
    else
    {
      return false;
    }
  }
}
