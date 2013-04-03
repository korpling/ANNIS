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

import annis.CommonHelper;
import annis.libgui.MatchedNodeColors;
import annis.libgui.visualizers.VisualizerInput;
import com.hp.gagawa.java.DocumentType;
import com.hp.gagawa.java.Node;
import com.hp.gagawa.java.elements.Body;
import com.hp.gagawa.java.elements.Doctype;
import com.hp.gagawa.java.elements.Head;
import com.hp.gagawa.java.elements.Html;
import com.hp.gagawa.java.elements.Link;
import com.hp.gagawa.java.elements.Script;
import com.hp.gagawa.java.elements.Table;
import com.hp.gagawa.java.elements.Td;
import com.hp.gagawa.java.elements.Text;
import com.hp.gagawa.java.elements.Tr;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import java.io.IOException;
import java.io.Writer;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SPointingRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SStructuredNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Edge;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDataSourceSequence;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.slf4j.LoggerFactory;

/**
 * A view of the entire text of a document, possibly with interactive 
 * coreference links. 
 * <p>
 * It is possible to use this visualization to view entire texts
 * even if you do not have coreference annotations.
 * </p>
 * <p>
 * <strong>Mappings</strong>:<br/>
 * <ul>
 *   <li>hide_empty: if set to "true" only texts that have annotations are shown</li>
 * </ul>
 * </p>
 * <p>
 * <b>Implementation notes</b>: This code relies heavily on HTML-tables and 
 * has some logic that is difficult to 
 * understand. A GWT-based rewrite would be a good alternative.
 * </p>
 * @author Thomas Krause
 * @author Christian Schulz-Hanke
 */
@PluginImplementation
public class CorefVisualizer extends WriterVisualizer
{
  
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(CorefVisualizer.class);
  
  long globalIndex;
  List<TReferent> referentList;
  List<TComponent> komponent;
  HashMap<String, List<Long>> componentOfToken;
  HashMap<String, List<String>> tokensOfNode; //ReferentOfToken
  HashMap<String, HashMap<Long, Integer>> referentOfToken; // the Long ist the Referend, the Integer means: { 0=incoming P-Edge, 1=outgoing P-Edge, 2=both(not used anymore)}
  List<String> visitedNodes;
  LinkedList<TComponenttype> componenttype; //used to save which Node (with outgoing "P"-Edge) gelongs to which component
  private HashMap<Integer, Integer> colorlist;

  static class TComponenttype
  {

    String type;
    List<String> nodeList;

    TComponenttype()
    {
      type = "";
      nodeList = new LinkedList<String>();
    }
  }

  static class TComponent
  {

    List<String> tokenList;
    String type;

    TComponent()
    {
      tokenList = new LinkedList<String>();
      type = "";
    }

    TComponent(List<String> ll, String t)
    {
      tokenList = ll;
      type = t;
    }
  }

  static class TReferent
  {

    Set<SAnnotation> annotations;
    long component;

    TReferent()
    {
      component = -1;
      annotations = new HashSet<SAnnotation>();
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
    // root html element 
    Html html = new Html();
    Head head = new Head();
    Body body = new Body();
    
    html.removeXmlns();
    html.appendChild(head);
    html.appendChild(body);
    
    try
    {
      LinkedList<String> fonts = new LinkedList<String>();
      if(input.getFont() != null)
      {
        Link linkFont = new Link();
        linkFont.setHref(input.getFont().getUrl());
        head.appendChild(linkFont);
        fonts.add(input.getFont().getName());
      }
      fonts.add("serif");

      Link linkTooltip = new Link();
      linkTooltip.setHref(input.getResourcePath("coref/jquery.tooltip.css"));
      linkTooltip.setRel("stylesheet");
      linkTooltip.setType("text/css");
      head.appendChild(linkTooltip);
      
      Script scriptJquery = new Script("text/javascript");
      scriptJquery.setSrc(input.getResourcePath("coref/jquery-1.6.2.min.js"));
      head.appendChild(scriptJquery);
      
      Script scriptTooltip = new Script("text/javascript");
      scriptTooltip.setSrc(input.getResourcePath("coref/jquery.tooltip.min.js"));
      head.appendChild(scriptTooltip);
      
      Link linkCoref = new Link();
      linkCoref.setHref(input.getResourcePath("coref/coref.css"));
      linkCoref.setRel("stylesheet");
      linkCoref.setType("text/css");
      head.appendChild(linkCoref);
      
      Script scriptCoref = new Script("text/javascript");
      scriptCoref.setSrc(input.getResourcePath("coref/CorefVisualizer.js"));
      head.appendChild(scriptCoref);
     
      body.setStyle("font-family: '" + StringUtils.join(fonts, "', '") + "';");
      
      //get Info
      globalIndex = 0;
      tokensOfNode = new HashMap<String, List<String>>();
      referentList = new LinkedList<TReferent>();
      komponent = new LinkedList<TComponent>();
      referentOfToken = new HashMap<String, HashMap<Long, Integer>>();
      componentOfToken = new HashMap<String, List<Long>>();
      componenttype = new LinkedList<TComponenttype>();
      SDocument saltDoc = input.getDocument();
     
      SDocumentGraph saltGraph = saltDoc.getSDocumentGraph();
      if (saltGraph == null)
      {
        body.setText("An Error occured: Could not get Graph of Result (Graph == null).");
        return;
      }
      List<SRelation> edgeList = saltGraph.getSRelations();
      if (edgeList == null)
      {
        return;
      }

      for (SRelation rawRel : edgeList)
      {
        if (includeEdge(rawRel, input.getNamespace()))
        {
          SPointingRelation rel = (SPointingRelation) rawRel;
          
          String relType = componentNameForRelation(rel);
          
          visitedNodes = new LinkedList<String>();
          //got type for this?
          boolean gotIt = false;
          int componentnr;
          for (componentnr = 0; componentnr < componenttype.size(); componentnr++)
          {
            if (componenttype.get(componentnr) != null && componenttype.get(componentnr).type != null 
              && componenttype.get(componentnr).nodeList != null
              && componenttype.get(componentnr).type.equals(relType) 
              && componenttype.get(componentnr).nodeList.contains(rel.getSStructuredSource().getSId()))
            {
              gotIt = true;
              break;
            }
          }
          TComponent currentComponent;
          TComponenttype currentComponenttype;
          if (gotIt)
          {
            currentComponent = komponent.get(componentnr);
            currentComponenttype = componenttype.get(componentnr);
          }
          else
          {
            currentComponenttype = new TComponenttype();
            currentComponenttype.type = relType;
            componenttype.add(currentComponenttype);
            componentnr = komponent.size();
            currentComponent = new TComponent();
            currentComponent.type = relType;
            currentComponent.tokenList = new LinkedList<String>();
            komponent.add(currentComponent);
            currentComponenttype.nodeList.add(rel.getSStructuredSource().getSId());
          }
          TReferent ref = new TReferent();
          ref.annotations = new HashSet<SAnnotation>();
          ref.annotations.addAll(rel.getSAnnotations());
          ref.component = componentnr;
          referentList.add(ref);

          List<String> currentTokens = getAllTokens(rel.getSStructuredSource(), componentNameForRelation(rel), 
            currentComponenttype, componentnr, input.getNamespace());

          setReferent(rel.getSStructuredTarget(), globalIndex, 0);//neu
          setReferent(rel.getSStructuredSource(), globalIndex, 1);//neu

          for (String s : currentTokens)
          {
            if (!currentComponent.tokenList.contains(s))
            {
              currentComponent.tokenList.add(s);
            }
          }

          globalIndex++;
        }
      }

      colorlist = new HashMap<Integer, Integer>();

      // A list containing all the generated HTML elements, one list entry
      // for each text.
      List<List<Node>> nodesPerText = new LinkedList<List<Node>>();
      
      // write output for each text separatly
      EList<STextualDS> texts = saltGraph.getSTextualDSs();
      if(texts != null && !texts.isEmpty())
      {
        
        for(STextualDS t : texts)
        {
          SDataSourceSequence sequence= SaltFactory.eINSTANCE.createSDataSourceSequence();
          sequence.setSSequentialDS(t);
          sequence.setSStart(0);
          sequence.setSEnd((t.getSText()!= null) ? t.getSText().length():0);
          EList<SToken> token = saltGraph.getSTokensBySequence(sequence);

          if(token != null)
          {
            boolean validText = true;
            if(Boolean.parseBoolean(input.getMappings().getProperty("hide_empty", "false")))
            {
              validText = false;
              // check if the text contains any matching annotations
              for(SToken tok : token)
              {
                /* 
                 * The token is only added to this map if an valid edge
                 * (according to the resolver trigger) conntected to 
                 * this token was found.
                 */
                if(referentOfToken.get(tok.getSId()) != null 
                  && !referentOfToken.get(tok.getSId()).isEmpty())
                {
                  validText = true;
                  break;
                }
              }
            }
            
            if(validText)
            {
              List<Node> nodes = outputSingleText(token, input);
              nodesPerText.add(nodes);
            }
          }
        } // end for each STexutalDS
        
        /* 
         * Append the generated output to the body, wrap in table if necessary. 
         */
        
        // present all texts as columns side by side if using multiple texts
        Table tableTexts = new Table();
        Tr trTextRow = new Tr();
        trTextRow.setCSSClass("textRow");

        // only append wrapper table if we have multiple texts
        if(nodesPerText.size() > 1)
        {
          body.appendChild(tableTexts);
          tableTexts.appendChild(trTextRow);
        }
        for(List<Node> nodes : nodesPerText)
        {
           // multi-text mode?
          if(nodesPerText.size() > 1)
          {
            Td tdSingleText = new Td();
            trTextRow.appendChild(tdSingleText);
            tdSingleText.setCSSClass("text");
            tdSingleText.appendChild(nodes);
          }
          else
          {
            body.appendChild(nodes);
          }
        }
        
      }
      else
      {
        Text errorTxt = new Text("Could not find any texts for the " 
          + input.getNamespace() + " node namespace (layer).");
        body.appendChild(errorTxt);
      }
      
      
      
      // write HTML4 transitional doctype
      w.append(new Doctype(DocumentType.HTMLTransitional).write());
      // append the html tree
      w.append(html.write());
      
    }
    catch (IOException ex)
    {
      log.error(null, ex);
    }
  }
  
  private List<Node> outputSingleText(EList<SToken> token, VisualizerInput input)
    throws IOException
  {
    List<Node> result = new LinkedList<Node>();
    
    List<Long> prevpositions, listpositions;
    List<Long> finalpositions = null;
    int maxlinkcount = 0;
    String lastId, currentId = null;

    for (SToken tok : token)
    {

      prevpositions = finalpositions;
      if (prevpositions != null && prevpositions.size() < 1)
      {
        prevpositions = null;
      }
      lastId = currentId;
      currentId = tok.getId();
      listpositions = componentOfToken.get(currentId);
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
        for (int i = 0; prevpositions != null && i < prevpositions.size(); i++)
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
          if (prevpositions != null && !prevpositions.contains(listpositions.get(i)))
          {
            remains.add(listpositions.get(i));
          }
        }

        int minsize = checklist.size() + remains.size();
        int number = 0;
        finalpositions = new LinkedList<Long>();
        for (int i = 0; i < minsize; i++)
        {
          if (prevpositions != null && checklist.size() > i && checklist.get(i).booleanValue())
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

      String onclick = "";
      String style = "";
      if (input.getMarkedAndCovered().containsKey(tok))
      {
        MatchedNodeColors[] vals = MatchedNodeColors.values();
        long match = Math.min(input.getMarkedAndCovered().get(tok)-1, vals.length-1);

        style += ("color: " + vals[(int) match].getHTMLColor() + ";");
      }

      boolean underline = false;
      if (!finalpositions.isEmpty())
      {
        style += "cursor:pointer;";
        underline = true;
        onclick = "togglePRAuto(this);";
      }

      Table tableSingleTok = new Table();
      result.add(tableSingleTok);
      tableSingleTok.setCSSClass("token");
      
      int currentlinkcount = 0;
      if (underline)
      {
        boolean firstone = true;
        int index = -1;
        String tooltip;
        if (!finalpositions.isEmpty())
        {
          for (Long currentPositionComponent : finalpositions)
          {
            index++;
            String left = "", right = "";
            
            TComponent currentWriteComponent = null;// == pir
            String currentType = "";
            if (!currentPositionComponent.equals(Long.MIN_VALUE) && komponent.size() > currentPositionComponent)
            {
              currentWriteComponent = komponent.get((int) (long) currentPositionComponent);
              List<String> pi = currentWriteComponent.tokenList;
              List<String> preparedPi = new LinkedList<String>();
              for(String s : pi)
              {
                preparedPi.add(prepareID(s));
              }
              currentType = currentWriteComponent.type;
              left = StringUtils.join(preparedPi, ",");
              right = "" + currentPositionComponent + 1;
            }
            String annotations = getAnnotations(tok.getId(), currentPositionComponent);
            if (firstone)
            {
              firstone = false;
              if (currentWriteComponent == null)
              {
                String left2 = "", right2 = "";
                long pr = 0;
                TComponent currentWriteComponent2;// == pir
                String currentType2 = "";
                String annotations2 = "";
                for (Long currentPositionComponent2 : finalpositions)
                {
                  if (!currentPositionComponent2.equals(Long.MIN_VALUE) && komponent.size() > currentPositionComponent2)
                  {
                    currentWriteComponent2 = komponent.get((int) (long) currentPositionComponent2);
                    List<String> pi2 = currentWriteComponent2.tokenList;
                   
                    // prepare each single ID
                    List<String> preparedPi2 = new LinkedList<String>();
                    for(String s : pi2)
                    {
                      preparedPi2.add(prepareID(s));
                    }
                    currentType2 = currentWriteComponent2.type;
                    left2 = StringUtils.join(preparedPi2, ",");
                    right2 = "" + currentPositionComponent2 + 1;
                    annotations2 = getAnnotations(tok.getId(), currentPositionComponent2);
                    pr = currentPositionComponent2;
                    break;
                  }
                }
                tooltip = "&lt;b&gt;Component&lt;/b&gt;: " + (pr + 1) + ", &lt;b&gt;Type&lt;/b&gt;: " + currentType2 + annotations2;
                
                Tr trTok = new Tr();
                tableSingleTok.appendChild(trTok);
                
                Td tdTok = new Td();
                trTok.appendChild(tdTok);
                
                tdTok.setId("tok_" + prepareID(tok.getSId()));
                tdTok.setTitle(tooltip);
                tdTok.setStyle(style);
                tdTok.setAttribute("onclick", onclick);
                tdTok.setAttribute("annis:pr_left", left2);
                tdTok.setAttribute("annis:pr_right", right2);
                
                Text textTok = new Text("&nbsp;" + CommonHelper.getSpannedText(tok) + "&nbsp;");
                tdTok.appendChild(textTok);
              }
              else
              {//easier
                tooltip = "&lt;b&gt;Component&lt;/b&gt;: " + (currentPositionComponent + 1) + ", &lt;b&gt;Type&lt;/b&gt; " + currentType + annotations;
                
                Tr trTok = new Tr();
                tableSingleTok.appendChild(trTok);
                
                Td tdTok = new Td();
                trTok.appendChild(tdTok);
                tdTok.setId("tok_" + prepareID(tok.getSId()));
                tdTok.setTitle(tooltip);
                tdTok.setStyle(style);
                tdTok.setAttribute("onclick", onclick);
                tdTok.setAttribute("annis:pr_left", left);
                tdTok.setAttribute("annis:pr_right", right);
                
                Text textTok = new Text("&nbsp;" + CommonHelper.getSpannedText(tok) + "&nbsp;");
                tdTok.appendChild(textTok);
                
              }
            }
            currentlinkcount++;
            //while we've got underlines
            if (currentPositionComponent.equals(Long.MIN_VALUE))
            {
              Tr trBlank = new Tr();
              tableSingleTok.appendChild(trBlank);
              
              Td tdBlank = new Td();
              trBlank.appendChild(tdBlank);
              
              tdBlank.setCSSClass("blank");
            }
            else
            {
              int color;
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

              tooltip = "&lt;b&gt;Component&lt;/b&gt;: " + (currentPositionComponent + 1) + ", &lt;b&gt;Type&lt;/b&gt;: " + currentType + annotations;
              
              Tr trLineContainer = new Tr();
              tableSingleTok.appendChild(trLineContainer);
              
              Td tdLineContainer = new Td();
              trLineContainer.appendChild(tdLineContainer);
              
              Table tableLineContainer = new Table();
              tdLineContainer.appendChild(tableLineContainer);
              
              tableLineContainer.setCSSClass("linecontainer");
              
              Tr trLine = new Tr();
              tableLineContainer.appendChild(trLine);
              
              Td tdLine = new Td();
              trLine.appendChild(tdLine);
              
              tdLine.setCSSClass("line");
              tdLine.setStyle("background-color: #" + Integer.toHexString(color) + "; " + style + addition);
              tdLine.setAttribute("onclick", onclick);
              tdLine.setAttribute("annis:pr_left", left);
              tdLine.setAttribute("annis:pr_right", right);
              tdLine.setTitle(tooltip);

              Tr trSpace = new Tr();
              tableLineContainer.appendChild(trSpace);
              
              Td tdSpace = new Td();
              trSpace.appendChild(tdSpace);
              
              tdSpace.setCSSClass("space");
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
            Tr trSpace = new Tr();
            tableSingleTok.appendChild(trSpace);

            Td tdSpace = new Td();
            trSpace.appendChild(tdSpace);

            tdSpace.setStyle("height: " + (maxlinkcount - currentlinkcount) * 5 + "px;");
          }
        }
      }
      else
      {
        
        // print a token without lines
        Tr trTok = new Tr();
        tableSingleTok.appendChild(trTok);

        Td tdTok = new Td();
        trTok.appendChild(tdTok);
        
        tdTok.setId("tok_" + prepareID(tok.getSId()));
        tdTok.setStyle(style);
        
        Text textTok = new Text("&nbsp;" + CommonHelper.getSpannedText(tok) + "&nbsp;");
        tdTok.appendChild(textTok);
        
        if (maxlinkcount > 0)
        {
          Tr trSpace = new Tr();
          tableSingleTok.appendChild(trSpace);

          Td tdSpace = new Td();
          trSpace.appendChild(tdSpace);

          tdSpace.setStyle("height: " + maxlinkcount * 5 + "px;");
        }
      }
    } // end for each token
    
    return result;
  }

  /**
   * collects all Tokens of the component
   * @param n SStructuredNode to start with
   * @param name String that determines which component we search for
   * @param c componenttype, that will include its Tokens
   * @param cnr Number of the component
   * @return List of Tokens
   */
  private List<String> getAllTokens(SStructuredNode n, String name, TComponenttype c, long cnr, String namespace)
  {
    List<String> result = null;
    if (!visitedNodes.contains(n.getSId()))
    {
      result = new LinkedList<String>();
      visitedNodes.add(n.getSId());
      if (tokensOfNode.containsKey(n.getSId()))
      {
        for (String t : tokensOfNode.get(n.getSId()))
        {
          result.add(t);
          if (componentOfToken.get(t) == null)
          {
            List<Long> newlist = new LinkedList<Long>();
            newlist.add(cnr);
            componentOfToken.put(t, newlist);
          }
          else
          {
            if (!componentOfToken.get(t).contains(cnr))
            {
              componentOfToken.get(t).add(cnr);
            }
          }
        }
      }
      else
      {
        result = searchTokens(n, cnr);
        if (result != null)
        {
          tokensOfNode.put(n.getSId(), result);
        }
      }
      //get "P"-Edges!
      EList<Edge> outEdges = n.getSGraph().getOutEdges(n.getSId());
      if(outEdges != null)
      {
        for (Edge e : outEdges)
        {
          if(includeEdge(e, namespace))
          {
            SPointingRelation rel = (SPointingRelation) e;
            if (name.equals(componentNameForRelation(rel))
              && !visitedNodes.contains(rel.getSStructuredTarget().getSId()))
            {
              c.nodeList.add(rel.getSStructuredTarget().getSId());
              List<String> Med = getAllTokens(rel.getSStructuredTarget(), 
                name, c, cnr, namespace);
              for (String l : Med)
              {
                if (result != null && !result.contains(l))
                {
                  result.add(l);
                }
              }
            }
          }
        }
      }
      EList<Edge> inEdges = n.getSGraph().getInEdges(n.getSId());
      if(inEdges != null)
      {
        for (Edge e : inEdges)
        {
          if(includeEdge(e, namespace))
          {
            SPointingRelation rel = (SPointingRelation) e;
            if (name.equals(componentNameForRelation(rel))
              && !visitedNodes.contains(rel.getSStructuredSource().getSId()))
            {
              c.nodeList.add(rel.getSStructuredSource().getSId());
              List<String> Med = getAllTokens(rel.getSStructuredSource(), name, c, cnr, namespace);
              for (String s : Med)
              {
                if (result != null && !result.contains(s))
                {
                  result.add(s);
                }
              }
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * adds a Referent for all Nodes dominated or covered by outgoing Edges of AnnisNode a
   * @param n the Node
   * @param index index of the Referent
   * @param value determines wheather the refered P-Edge is incoming (1) or outgoing (0)
   */
  private void setReferent(
    de.hu_berlin.german.korpling.saltnpepper.salt.graph.Node n, long index, int value)
  {
    if (n instanceof SToken)
    {
      SToken tok = (SToken) n;
      if (!referentOfToken.containsKey(tok.getSId()))
      {
        HashMap<Long, Integer> newlist = new HashMap<Long, Integer>();
        newlist.put(index, value);//globalindex?
        referentOfToken.put(tok.getSId(), newlist);
      }
      else
      {
        referentOfToken.get(tok.getSId()).put(globalIndex, value);
      }
    }
    else
    {
      EList<Edge> outEdges = n.getGraph().getOutEdges(n.getId());
      if(outEdges != null)
      {
        for (Edge edge : outEdges)
        {
          if(!(edge instanceof SPointingRelation))
          {
            if (edge.getSource() != null && edge.getTarget() != null)
            {
              setReferent(edge.getTarget(), index, value);
            }
          }
        }
      }
    }
  }

  /**
   * Collects all Token dominated or covered by all outgoing Edges of AnnisNode a
   * @param n
   * @param cnr ComponentNumber this tokens will be added for
   * @return List of Tokennumbers
   */
  private List<String> searchTokens(SNode n, long cnr)
  {
    List<String> result = new LinkedList<String>();
    if (n instanceof SToken)
    {
      result.add(n.getSId());
      if (componentOfToken.get(n.getSId()) == null)
      {
        List<Long> newlist = new LinkedList<Long>();
        newlist.add(cnr);
        componentOfToken.put(n.getSId(), newlist);
      }
      else
      {
        List<Long> newlist = componentOfToken.get(n.getSId());
        if (!newlist.contains(cnr))
        {
          newlist.add(cnr);
        }
      }
    }
    else
    {
      EList<Edge> outgoing = n.getSGraph().getOutEdges(n.getSId());
      if(outgoing != null)
      {
        for (Edge e : outgoing)
        {
          if(!(e instanceof SPointingRelation) && e.getSource() instanceof SNode && e.getTarget() instanceof SNode)
          {
            List<String> Med = searchTokens((SNode) e.getTarget(), cnr);
            for (String s : Med)
            {
              if (!result.contains(s))
              {
                result.add(s);
              }
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
   * @return annotations as a String
   */
  private String getAnnotations(String id, long component)
  {
    String result = "";
    String incoming = "", outgoing = "";
    int nri = 1, nro = 1;

    if(referentOfToken.get(id) != null)
    {
      for (long l : referentOfToken.get(id).keySet())
      {
        if (referentList.get((int) l) != null && referentList.get((int) l).component == component
          && referentList.get((int) l).annotations != null && referentList.get((int) l).annotations.size() > 0)
        {
          int num = referentOfToken.get(id).get(l);
          if (num == 0 || num == 2)
          {
            for (SAnnotation an : referentList.get((int) l).annotations)
            {
              if (nri == 1)
              {
                incoming = ", &lt;b&gt;incoming Annotations&lt;/b&gt;: " + an.getName() + "=" + an.getValue();
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
            for (SAnnotation an : referentList.get((int) (long) l).annotations)
            {
              if (nro == 1)
              {
                outgoing = ", &lt;b&gt;outgoing Annotations&lt;/b&gt;: " + an.getSName() + "=" + an.getValueString();
                nro--; // remove l+"- "+
              }
              else
              {
                outgoing += ", " + an.getSName() + "=" + an.getValueString();
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
   * Calculates wheather a line determinded by its component should be discontinous
   * @param pre Id of the left token
   * @param now Id of the right token
   * @param currentComponent Number of the component, number of variable "komponent"
   * @return Should the line be continued?
   */
  private boolean connectionOf(String pre, String now, long currentComponent)
  {
    List<Long> prel = new LinkedList<Long>(), nowl = new LinkedList<Long>();
    if (!pre.equals(now) && referentOfToken.get(pre) != null && referentOfToken.get(now) != null)
    {
      for (long l : referentOfToken.get(pre).keySet())
      {
        if (referentList.get((int) l) != null && referentList.get((int) l).component == currentComponent
          && referentOfToken.get(pre).get(l).equals(0))
        {
          prel.add(l);
        }
      }
      for (long l : referentOfToken.get(now).keySet())
      {
        if (referentList.get((int) l) != null && referentList.get((int) l).component == currentComponent
          && referentOfToken.get(now).get(l).equals(0))
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
    if (!pre.equals(now) && referentOfToken.get(pre) != null && referentOfToken.get(now) != null)
    {
      for (long l : referentOfToken.get(pre).keySet())
      {
        if (referentList.get((int) l) != null && referentList.get((int) l).component == currentComponent
          && referentOfToken.get(pre).get(l).equals(1))
        {
          prel.add(l);
        }
      }
      for (long l : referentOfToken.get(now).keySet())
      {
        if (referentList.get((int) l) != null && referentList.get((int) l).component == currentComponent
          && referentOfToken.get(now).get(l).equals(1))
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

  private boolean includeEdge(Edge e, String namespace)
  {
    if(e instanceof SPointingRelation)
    {
      SPointingRelation rel = (SPointingRelation) e;
      if(componentNameForRelation(rel) != null && rel.getSSource() != null && rel.getSTarget() != null
        && rel.getSLayers() != null && namespace.equals(rel.getSLayers().get(0).getSName()))
      {
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * replaces all unwanted characters from the ID with "_"
   * @param orig
   * @return 
   */
  private String prepareID(String orig)
  {
    return DigestUtils.md5Hex(orig);
//    return StringUtils.replaceChars(orig, "#:/.", "____");
//    Matcher m = patternIrregualIDChar.matcher(orig);
//    return m.replaceAll("_");
  }
  
  private static String componentNameForRelation(SRelation rel)
  {
    return (rel.getSTypes() != null && rel.getSTypes().size() > 0)
      ? rel.getSTypes().get(0) : null;
  }
}
