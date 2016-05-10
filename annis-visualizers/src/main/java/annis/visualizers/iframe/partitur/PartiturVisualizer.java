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
package annis.visualizers.iframe.partitur;

import annis.CommonHelper;
import annis.libgui.Helper;
import annis.libgui.visualizers.VisualizerInput;
import annis.model.AnnisNode;
import annis.service.ifaces.AnnisToken;
import annis.visualizers.iframe.WriterVisualizer;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@PluginImplementation
public class PartiturVisualizer extends WriterVisualizer
{
  
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(PartiturVisualizer.class);

  private List<AnnisNode> nodes;
  private List<AnnisNode> token;  

  @Override
  public String getShortName()
  {
    return "iframegrid";
  }

  @Override
  public void writeOutput(VisualizerInput input, Writer writer)
  {
    try
    {

      nodes = input.getResult().getGraph().getNodes();
      token = input.getResult().getGraph().getTokens();

      // get partitur
      PartiturParser partitur = new PartiturParser(input.getResult().getGraph(),
        input.getNamespace());

      // check right to left
      boolean isRTL = checkRTL(input.getResult().getTokenList());

      List<String> tierNames = new LinkedList<String>(partitur.getKnownTiers());
      Collections.sort(tierNames);
      
      // get keys that are allowed to select
      LinkedHashSet<String> keys = new LinkedHashSet<String>();
      String mapping = input.getMappings().getProperty("annos");
      if (mapping == null)
      {
        // default to the alphabetical order
        keys.addAll(partitur.getNameslist());
      }
      else
      {
        String[] splitted = mapping.split(",");
        for (int k = 0; k < splitted.length; k++)
        {
          String s = splitted[k].trim();
          if (partitur.getNameslist().contains(s))
          {
            keys.add(s);
          }
        }
      }

      writer.append(
        "<!DOCTYPE html><html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
      writer.append("<link href=\"" + input.getResourcePath("jbar.css")
        + "\" rel=\"stylesheet\" type=\"text/css\" >");
      writer.append("<link href=\""
        + input.getResourcePath("jquery.tooltip.css")
        + "\" rel=\"stylesheet\" type=\"text/css\" >");
      writer.append("<link href=\""
        + input.getResourcePath("jquery.noty.css")
        + "\" rel=\"stylesheet\" type=\"text/css\" >");
      writer.append("<link href=\"" + input.getResourcePath("partitur.css")
        + "\" rel=\"stylesheet\" type=\"text/css\" >");
      writer.append("<script src=\"" + input.getResourcePath(
        "jquery-1.7.1.min.js") + "\"></script>");
      writer.append("<script src=\"" + input.getResourcePath("jquery.jbar.js")
        + "\"></script>");
      writer.append("<script src=\"" + input.getResourcePath(
        "jquery.tooltip.min.js") + "\"></script>");
      writer.append("<script src=\"" + input.getResourcePath(
        "jquery.noty.js") + "\"></script>");
      writer.append("<script>");
      writer.append(convertToJavacSriptArray(new LinkedList<String>()));
      writer.append("\nvar levelNames = [");
      int i = 0;
      for (String levelName : tierNames)
      {
        if(keys.contains(levelName))
        {
          writer.append((i++ > 0 ? ", " : "") + "\"" + levelName + "\"");
        }
      }
      writer.append("];\n</script>");
      writer.append("<script type=\"text/javascript\" src=\""
        + input.getResourcePath("PartiturVisualizer.js")
        + "\"></script>");

      writer.append("</head>");
      writer.append("<body>\n");

      writer.append("<ul id=\"toolbar\"></ul>");
      writer.append("<div id=\"partiture\">");

      if (isRTL)
      {
        writer.append("<table class=\"partitur_table\" dir=\"rtl\">\n");
      }
      else
      {
        writer.append("<table class=\"partitur_table\")\">\n");
      }

      

      for (String tier : keys)
      {
        List<String> indexlist = new ArrayList<String>();

        for (List<PartiturParser.ResultElement> span : partitur.getResultlist())
        {
          for (PartiturParser.ResultElement strr : span)
          {
            if (strr.getName().equals(tier) && !indexlist.contains(strr.getId()))
            {
              indexlist.add(strr.getId());
            }
          }
        }

        String[] currentarray; //Saves annotation-ids of the current row

        while (!indexlist.isEmpty())
        { //Create Rows until all Annotations fit in
          List<String> currentdontuselist = new LinkedList<String>(); //Lists all Annotations that should not be added to the current row
          writer.append("<tr class=\"level_" + tier + "\"><th>" + tier + "</th>"); //new row

          currentarray = new String[partitur.getResultlist().size()];
          for (int iterator3 = 0; iterator3 < partitur.getResultlist().size();
            iterator3++)
          {
            currentarray[iterator3] = null;
          }

          int spanCounter = 0;
          for (List<PartiturParser.ResultElement> span :
            partitur.getResultlist())
          { //for each Token
            for (PartiturParser.ResultElement annotationelement : span)
            { // for each Annotation annotationelement of that Token
              if (indexlist.contains(annotationelement.getId())
                && !currentdontuselist.contains(annotationelement.getId()))
              {
                boolean neu = false; //Should the Annotation be added?
                if (currentarray[spanCounter] == null)
                {
                  indexlist.remove(annotationelement.getId());
                  currentarray[spanCounter] = annotationelement.getId();
                  neu = true;
                }
                //get all other annotationelement.id (earlier Ids => dontuselist)
                int span2Counter = 0;
                for (List<PartiturParser.ResultElement> span2 : partitur.
                  getResultlist())
                {
                  for (PartiturParser.ResultElement strr2 : span2)
                  {
                    if (strr2.getId().equals(annotationelement.getId()) && neu) //{
                    {
                      if (currentarray[span2Counter] == null)
                      {
                        currentarray[span2Counter] = annotationelement.getId();
                      }
                    }
                    if (span2Counter <= spanCounter && !currentdontuselist.
                      contains(strr2.getId()))
                    {
                      currentdontuselist.add(strr2.getId());
                    }
                  }
                  span2Counter++;
                }
                //break; //Not needed?
              }
            }
            spanCounter++;
          }


          //Write Row
          int length = 1;
          for (int iterator5 = 0; iterator5 < currentarray.length; iterator5 +=
              length)
          {
            StringBuffer tokenIdsArray = new StringBuffer();
            StringBuffer eventIdsArray = new StringBuffer();
            boolean unused = true;
            length = 1;
            if (currentarray[iterator5] == null)
            { //empty entry
              writer.append("<td></td>");
            }
            else
            {
              PartiturParser.ResultElement element = null;
              HashSet<Integer> common = new HashSet<Integer>();
              boolean found = false;
              int outputSpanCounter = 0;
              for (List<PartiturParser.ResultElement> outputSpan : partitur.
                getResultlist())
              {
                for (PartiturParser.ResultElement strr : outputSpan)
                {
                  if (strr.getId().equals(currentarray[iterator5]))
                  {
                    if (!found)
                    {
                      element = strr;
                    }
                    if (!common.contains(outputSpanCounter))
                    {
                      common.add(outputSpanCounter);
                    }
                    found = true;
                    if (unused)
                    {
                      tokenIdsArray.append("" + strr.getId() + "_"
                        + outputSpanCounter);
                      eventIdsArray.append(tier + "_" + strr.getId() + "_"
                        + outputSpanCounter);
                      unused = false;
                    }
                    else
                    {
                      tokenIdsArray.append("," + strr.getId() + "_"
                        + outputSpanCounter);
                      eventIdsArray.append("," + tier + "_" + strr.getId() + "_"
                        + outputSpanCounter);
                    }
                  }
                }
                outputSpanCounter++;
              }
              for (int iterator7 = iterator5 + 1; iterator7
                < currentarray.length; iterator7++)
              {
                if (common.contains(iterator7))
                {
                  length++;
                }
                else
                {
                  break;
                }
              }

              for (int iterator8 = 0; iterator8 < currentarray.length;
                iterator8++)
              {
                if (common.contains(iterator8))
                {
                  Long id = ((PartiturParser.Token) partitur.getToken().toArray()[iterator8]).
                    getId();
                  if (unused)
                  {
                    tokenIdsArray.append("" + id);
                    eventIdsArray.append(tier + "_" + id);
                    unused = false;
                  }
                  else
                  {
                    tokenIdsArray.append("," + id);
                    eventIdsArray.append("," + tier + "_" + id);
                  }
                }
              }

              String color = "black";
              if (input.getMarkableExactMap().containsKey(""
                + element.getNodeId()))
              {
                color =
                  input.getMarkableExactMap().get("" + element.getNodeId());
              }
              if (found)
              {
                writer.append("<td class=\"single_event\" "
                  + "id=\"event_" + tier + "_" + element.getId() + "_"
                  + iterator5 + "\" "
                  + "style=\"color:" + color + ";\" "
                  + "colspan=" + length + " "
                  + "annis:tokenIds=\"" + tokenIdsArray + "\" "
                  + "annis:eventIds=\"" + eventIdsArray + "\" "
                  + "title=\"" + partitur.namespaceForTier(tier) + ":" + tier
                  + " = " + StringEscapeUtils.escapeXml(element.getValue())
                  + "\"  " //tier =tier, event.getValue()= element.name
                  + "onMouseOver=\"toggleAnnotation(this, true);\" "
                  + "onMouseOut=\"toggleAnnotation(this, false);\" "
                  + addTimeAttribute(element.getNodeId()) 
                  + ">" + element.getValue() + "</td>");
              }
              else
              {
                writer.append("<td class=\"single_event\" >error</td>");
              }
            }
          }
          writer.append("</tr>");     //finish row
        }
      }

      // add token itself
      writer.append("<tr><th>tok</th>");


      for (PartiturParser.Token token : partitur.getToken())
      {
        String color = "black";

        if (input.getMarkableExactMap().containsKey("" + token.getId()))
        {
          color = input.getMarkableExactMap().get("" + token.getId());
        }
        writer.append("<td class=\"tok\" style=\"color:" + color + ";\" "
          + "id=\"token_" + token.getId() + "\" "
          + ">" + token.getValue() + "</td>");
      }
      writer.append("</tr>");


      writer.append("</table>\n");
      writer.append("</div>\n");
      writer.append("</body></html>");

    }
    catch (Exception ex)
    {
      log.error(
        null, ex);
      try
      {
        String annisLine = "";
        for (int i = 0; i < ex.getStackTrace().length; i++)
        {
          if (ex.getStackTrace()[i].getClassName().startsWith("annis."))
          {
            annisLine = ex.getStackTrace()[i].toString();
          }
        }

        writer.append("<html><body>Error occured ("
          + ex.getClass().getName()
          + "): " + ex.getLocalizedMessage() + "<br/>"
          + annisLine
          + "</body></html>");
      }
      catch (IOException ex1)
      {
        log.error(
          null, ex1);
      }
    }
  }


  private boolean checkRTL(List<AnnisToken> tokenList)
  {
    if(Helper.isRTLDisabled())
    {
      return false;
    }
    
    Iterator<AnnisToken> itToken = tokenList.listIterator();
    while (itToken.hasNext())
    {
      AnnisToken tok = itToken.next();
      String tokText = tok.getText();
      if (CommonHelper.containsRTLText(tokText))
      {
        return true;
      }
    }

    return false;
  }

  /**
   * We need to know, in which place of DOM the media visulizer are plugged in, 
   * so we could  call the seekAndPlay() function with the help of 
   * PartiturVisualizer.js
   * 
   * @param mediaIDs
   * @return a string which represents a javascript array
   */
  private String convertToJavacSriptArray(List<String> mediaIDs)
  {
    // in case there is no media visualizer do not build an array
    if (mediaIDs == null)
    {
      return "";
    }
    StringBuilder sb = new StringBuilder("\nvar mediaIDs = [ ");
    int size = mediaIDs.size();
    for (int i = 0; i < size; i++)
    {
      sb.append("\"");
      sb.append(mediaIDs.get(i));
      sb.append("\"");
      if (!(size - 1 - i == 0))
      {
        sb.append(", ");
      }
    }
    return sb.append(" ];\n").toString();
  }

  private String addTimeAttribute(long nodeId)
  {
    DetectHoles detectHoles = new DetectHoles(token);
    AnnisNode root = null;
    TimeHelper t = new TimeHelper(token);

    for (AnnisNode n : nodes)
    {
      if (n.getId() == nodeId)
      {
        root = n;
        break;
      }
    }

    // some calculations for index shifting
    AnnisNode leftNode = detectHoles.getLeftBorder(root);
    AnnisNode rightNode = detectHoles.getRightBorder(root);
    return t.getTimeAnno(leftNode, rightNode);    
  }
}
