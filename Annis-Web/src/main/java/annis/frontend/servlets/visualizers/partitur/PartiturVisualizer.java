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
package annis.frontend.servlets.visualizers.partitur;

import annis.frontend.servlets.visualizers.WriterVisualizer;
import annis.service.ifaces.AnnisToken;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringEscapeUtils;

/**
 *
 * @author thomas
 */
public class PartiturVisualizer extends WriterVisualizer
{

  public enum ElementType
  {

    begin,
    end,
    middle,
    single,
    noEvent
  }

  @Override
  public void writeOutput(Writer writer)
  {
    try
    {
      // get partitur
      PartiturParser partitur = new PartiturParser(getResult().getGraph(), getNamespace());

      // check right to left
      boolean isRTL = checkRTL(getResult().getTokenList());

      List<String> tierNames = new LinkedList<String>(partitur.getKnownTiers());
      Collections.sort(tierNames);

      writer.append("<html><head>");
      writer.append("<link href=\"" + getContextPath() + "/css/visualizer/partitur.css\" rel=\"stylesheet\" type=\"text/css\" >");
      writer.append("<link href=\"" + getContextPath() + "/javascript/extjs/resources/css/ext-all.css\" rel=\"stylesheet\" type=\"text/css\" >");
      writer.append("<script type=\"text/javascript\" src=\"" + getContextPath() + "/javascript/extjs/adapter/ext/ext-base.js\"></script>");
      writer.append("<script type=\"text/javascript\" src=\"" + getContextPath() + "/javascript/extjs/ext-all.js\"></script>");

      writer.append("<script>\nvar levelNames = [");
      int i = 0;
      for (String levelName : tierNames)
      {
        writer.append((i++ > 0 ? ", " : "") + "\"" + levelName + "\"");
      }
      writer.append("];\n</script>");
      writer.append("<script type=\"text/javascript\" src=\"" + getContextPath() + "/javascript/annis/visualizer/PartiturVisualizer2.js\"></script>");

      writer.append("</head>");
      writer.append("<body>\n");

      writer.append("<div id=\"toolbar\"></div>");
      writer.append("<div id=\"partiture\" style=\"position: absolute; top: 30px; left: 0px;\">");

      if (isRTL)
      {
        writer.append("<table class=\"partitur_table\" dir=\"rtl\">\n");
      }
      else
      {
        writer.append("<table class=\"partitur_table\" >\n");
      }

     for(int iterator=0;iterator<partitur.getNameslist().size();iterator++){

          String tier = (String)partitur.getNameslist().toArray()[iterator];
          List<Long> indexlist = new LinkedList<Long>();

          for(int iterator2=0; iterator2<partitur.getResultlist().size();iterator2++){
                for (PartiturParser.ResultElement strr: partitur.getResultlist().get(iterator2)){
                    if (strr.getName().equals(tier) && !indexlist.contains(strr.getId())) {indexlist.add(strr.getId());}
                }
          }

          List<Long> currentarray = new LinkedList<Long>(); //Saves annotation-ids of the current row

          while (!indexlist.isEmpty()){ //Create Rows until all Annotations fit in
            List<Long> currentdontuselist = new LinkedList<Long>(); //Lists all Annotations that should not be added to the current row
            writer.append("<tr class=\"level_"+tier+"\"><th>" + tier +  "</th>"); //new row
            currentarray.clear();
            for (int iterator3=0; iterator3<partitur.getResultlist().size();iterator3++) {currentarray.add(Long.MIN_VALUE);} //Long.MIN_VALUE == empty
            for(int iterator3=0; iterator3<partitur.getResultlist().size();iterator3++){ //for each Token
                    for (PartiturParser.ResultElement annotationelement: partitur.getResultlist().get(iterator3)){ // for each Annotation annotationelement of that Token
                        if (indexlist.contains(annotationelement.getId()) && !currentdontuselist.contains(annotationelement.getId())){
                            boolean neu=false; //Should the Annotation be added?
                            if (currentarray.get(iterator3).equals(Long.MIN_VALUE)) {
                                indexlist.remove(annotationelement.getId());
                                currentarray.set(iterator3, annotationelement.getId());
                                neu=true;
                            }
                            //get all other annotationelement.id (earlier Ids => dontuselist)
                            for(int iterator4=0; iterator4<partitur.getResultlist().size();iterator4++){
                                for (PartiturParser.ResultElement strr2: partitur.getResultlist().get(iterator4)){
                                    if (strr2.getId()==annotationelement.getId() && neu) //{
                                        if (currentarray.get(iterator4)==Long.MIN_VALUE) currentarray.set(iterator4, annotationelement.getId());
                                        if (iterator4<=iterator3 && !currentdontuselist.contains(strr2.getId())) {
                                            currentdontuselist.add(strr2.getId());
                                        }
                                }
                            }
                            //break; //Not needed?
                        }
                    }
                }
         

            //Write Row
            int length=1;
            for(int iterator5=0; iterator5<currentarray.size();iterator5+=length){
                StringBuffer tokenIdsArray = new StringBuffer();
                StringBuffer eventIdsArray = new StringBuffer();
                boolean unused = true;
                length=1;
                if (currentarray.get(iterator5)==Long.MIN_VALUE){ //empty entry
                     writer.append("<td></td>");
                }else{
                    PartiturParser.ResultElement element=null;
                    HashSet<Integer> common = new HashSet<Integer>();
                    boolean found=false;
                    for(int iterator6=0; iterator6<partitur.getResultlist().size();iterator6++){
                                for (PartiturParser.ResultElement strr: partitur.getResultlist().get(iterator6)){
                                    if (strr.getId()==currentarray.get(iterator5)) {
                                        if (!found) element=strr;
                                        if (!common.contains(iterator6)) common.add(iterator6);
                                        found=true;
                                        if (unused) {
                                            tokenIdsArray.append("" + strr.getId() + "_" + iterator6);
                                            eventIdsArray.append(tier + "_" + strr.getId() + "_" + iterator6);
                                            unused=false;
                                        } else{
                                            tokenIdsArray.append("," + strr.getId() + "_" + iterator6);
                                            eventIdsArray.append(","+tier + "_" + strr.getId() + "_" + iterator6);
                                        }
                                    }
                                }
                    }
                    for (int iterator7=iterator5+1; iterator7<currentarray.size();iterator7++)
                        if (common.contains(iterator7)) {
                            length++;
                        }
                        else break;

                    for (int iterator8=0; iterator8<currentarray.size();iterator8++)
                        if (common.contains(iterator8)) {
                            Long id = ((PartiturParser.Token)partitur.getToken().toArray()[iterator8]).getId();
                            if (unused) {
                                tokenIdsArray.append("" + id);
                                eventIdsArray.append(tier + "_" + id);
                                unused=false;
                            } else{
                                tokenIdsArray.append("," + id);
                                eventIdsArray.append(","+tier + "_" + id);
                            }
                        }
                    if (found)
                         writer.append("<td class=\"single_event\" "
                            + "id=\"event_" + tier + "_" + element.getId() + "_" + iterator5 + "\" "
                            + "style=\"color:black;\" "
                            + "colspan=" + length + " "
                            + "annis:tokenIds=\"" + tokenIdsArray + "\" "
                            + "annis:eventIds=\"" + eventIdsArray + "\" "
                            + "ext:qtip=\"" + partitur.namespaceForTier(tier) + ":" + tier + " = " + StringEscapeUtils.escapeXml(element.getValue()) + "\"  " //tier =tier, event.getValue()= element.name
                            + "onMouseOver=\"toggleAnnotation(this, true);\" "
                            + "onMouseOut=\"toggleAnnotation(this, false);\""
                             + ">" + element.getValue() + "</td>");
                    else writer.append("<td class=\"single_event\" >error</td>");
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

        if (getMarkableMap().containsKey("" + token.getId()))
        {
          color = getMarkableMap().get("" + token.getId());
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
      Logger.getLogger(PartiturVisualizer.class.getName()).log(Level.SEVERE, null, ex);
      try
      {
        writer.append("<html><body>Error occured</body></html>");
      }
      catch (IOException ex1)
      {
        Logger.getLogger(PartiturVisualizer.class.getName()).log(Level.SEVERE, null, ex1);
      }
    }
  }

  private ElementType getTypeForToken(PartiturParser.Token token, String tier)
  {
    // get token before and after
    PartiturParser.Token beforeToken = token.getBefore();
    PartiturParser.Token afterToken = token.getAfter();

    PartiturParser.Event event = token.getTier2Event().get(tier);

    if (event != null)
    {
      PartiturParser.Event beforeEvent =
        beforeToken == null ? null : beforeToken.getTier2Event().get(tier);
      PartiturParser.Event afterEvent =
        afterToken == null ? null : afterToken.getTier2Event().get(tier);

      boolean left = false;
      boolean right = false;
      // check if the events left and right have the same
      // id (and are the same event)
      if (beforeEvent != null && beforeEvent.getId() == event.getId())
      {
        left = true;
      }
      if (afterEvent != null && afterEvent.getId() == event.getId())
      {
        right = true;
      }

      if (left && right)
      {
        return ElementType.middle;
      }
      else if (left)
      {
        return ElementType.end;
      }
      else if (right)
      {
        return ElementType.begin;
      }
      else
      {
        return ElementType.single;
      }
    }

    return ElementType.noEvent;
  }

  private boolean checkRTL(List<AnnisToken> tokenList)
  {
    boolean result = false;
    Iterator<AnnisToken> itToken = tokenList.listIterator();
    while(itToken.hasNext() && !result)
    {
      AnnisToken tok = itToken.next();
      String tokText = tok.getText();
      for(int i=0; i < tokText.length(); i++)
      {
        char cc = tokText.charAt(i);
        // hebrew extended and basic, arabic basic and extendend
        if (cc >= 1425 && cc <= 1785)
        {
          result = true;
        }
        // alphabetic presentations forms (hebrwew) to arabic presentation forms A
        else if (cc >= 64286 && cc <= 65019)
        {
          result = true;
        }
        // arabic presentation forms B
        else if (cc >= 65136 && cc <= 65276)
        {
          result = true;
        }

        if(result)
        {
          break;
        }
      }
    }
    
    return result;
  }
}



