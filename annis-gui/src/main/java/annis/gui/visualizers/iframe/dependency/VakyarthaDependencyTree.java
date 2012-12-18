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
package annis.gui.visualizers.iframe.dependency;

import annis.gui.MatchedNodeColors;
import annis.gui.visualizers.VisualizerInput;
import annis.gui.visualizers.iframe.WriterVisualizer;
import static annis.model.AnnisConstants.*;
import annis.service.ifaces.AnnisResult;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Edge;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDataSourceSequence;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SPointingRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STYPE_NAME;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SFeature;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import java.io.IOException;
import java.io.Writer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <krause@informatik.hu-berlin.de>
 * @author Benjamin Weißenfels<b.pixeldrama@gmail.com>
 */
@PluginImplementation
public class VakyarthaDependencyTree extends WriterVisualizer
{

  private static final org.slf4j.Logger log = LoggerFactory.
    getLogger(VakyarthaDependencyTree.class);

  private Writer theWriter;

  private VisualizerInput input;

  @Override
  public String getShortName()
  {
    return "arch_dependency";
  }

  @Override
  public void writeOutput(VisualizerInput input, Writer writer)
  {
    theWriter = writer;
    this.input = input;

    printHTMLOutput();
  }

  public void printHTMLOutput()
  {
    SDocumentGraph sDocumentGraph = input.getSResult().getSDocumentGraph();
    Map<SToken, Integer> sToken = new TreeMap<SToken, Integer>(new Comparator<SToken>()
    {
      private int getIdx(SToken tok)
      {
        SFeature sF = tok.getSFeature(ANNIS_NS, FEAT_TOKENINDEX);
        return sF != null ? (int) (long) sF.getSValueSNUMERIC() : -1;
      }

      @Override
      public int compare(SToken o1, SToken o2)
      {
        int tok1 = getIdx(o1);
        int tok2 = getIdx(o2);

        if (tok1 < tok2)
        {
          return -1;
        }

        if (tok1 == tok2)
        {
          return 0;
        }
        else
        {
          return 1;
        }

      }
    });

    for (SNode n : sDocumentGraph.getSNodes())
    {
      if (n instanceof SToken)
      {
        SFeature sFeature = n.getSFeature(ANNIS_NS, FEAT_TOKENINDEX);
        int tokenIdx = sFeature != null ? (int) (long) sFeature.
          getSValueSNUMERIC() : -1;
        sToken.put((SToken) n, tokenIdx);
      }
    }

    Map<SToken, Integer> tok2Int = new HashMap<SToken, Integer>();
    int count = 0;
    for (SToken tok : sToken.keySet())
    {
      tok2Int.put(tok, count++);
    }

    try
    {
      println("<html>");
      println("<head>");

      println(
        "<script type=\"text/javascript\" src=\""
        + input.getResourcePath("vakyartha/jquery-1.4.2.min.js") + "\"></script>");
      println("<script type=\"text/javascript\" src=\""
        + input.getResourcePath("vakyartha/raphael.js") + "\"></script>");
      println(
        "<script type=\"text/javascript\" src=\""
        + input.getResourcePath("vakyartha/vakyarthaDependency.js") + "\"></script>");

      // output the data for the javascript
      println("<script type=\"text/javascript\">");
      println("fcolors={};");
      println("shownfeatures=[\"t\"];");
      println("tokens=new Object();");


      count = 0;
      for (SToken tok : sToken.keySet())
      {
        JSONObject o = new JSONObject();
        o.put("t", getText(tok));

        JSONObject govs = new JSONObject();
        EList<Edge> sEdges = tok.getSGraph().getInEdges(tok.getSId());

        for (Edge e : sEdges)
        {
          if (!(e instanceof SRelation) || !(e.getSource() instanceof SToken))
          {
            continue;
          }

          SRelation sRelation = (SRelation) e;
          SToken sTokSource = (SToken) sRelation.getSource();

          if (sRelation instanceof SPointingRelation)
          {
            String label = "";
            for (SAnnotation anno : sRelation.getSAnnotations())
            {
              label = anno.getSValueSTEXT();
              break;
            }

            if (sRelation.getSource() == null
              || !tok2Int.containsKey(sTokSource))
            {
              govs.put("root", label);
            }
            else
            {
              govs.put(String.valueOf(tok2Int.get(sTokSource)), label);
            }
          }
        }

        o.put("govs", govs);
        JSONObject attris = new JSONObject();

        JSONObject tAttris = new JSONObject();
        String tokenColor = "black";
        if (input.getMarkedAndCovered().containsKey(tok))
        {
          int colorNumber = ((int) (long) input.getMarkedAndCovered().get(tok)) - 1;
          tokenColor = MatchedNodeColors.values()[colorNumber].getHTMLColor();
        }
        tAttris.put("fill", tokenColor);
        tAttris.put("font", "11px Arial,Tahoma,Helvetica,Sans-Serif");

        attris.put("t", tAttris);
        o.put("attris", attris);

        theWriter.append("tokens[").append("" + count++).append("]=");
        theWriter.append(o.toString().replaceAll("\n", " "));
        theWriter.append(";\n");
      }

      println("</script>");

      println("</head>");
      println("<body>");

      // the div to render the javascript to
      println(
        "<div id=\"holder\" style=\"background:white; position:relative;\"> </div>");

      println("</body>");
      println("</html>");
    }
    catch (JSONException ex)
    {
      log.error(null, ex);
    }
    catch (IOException ex)
    {
      log.error(null, ex);
    }
  }

  private void println(String s) throws IOException
  {
    println(s, 0);
  }

  private void println(String s, int indent) throws IOException
  {
    for (int i = 0; i < indent; i++)
    {
      theWriter.append("\t");
    }
    theWriter.append(s);
    theWriter.append("\n");
  }

  private String getText(SToken tok)
  {
    SDocumentGraph sDocumentGraph = input.getSResult().getSDocumentGraph();
    EList<STYPE_NAME> textRelations = new BasicEList<STYPE_NAME>();
    textRelations.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);
    EList<SDataSourceSequence> sequences = sDocumentGraph.
      getOverlappedDSSequences(tok, textRelations);

    if (sequences != null && sequences.size() > 0)
    {
      return ((STextualDS) sequences.get(0).getSSequentialDS()).getSText().
        substring(sequences.get(0).getSStart(), sequences.get(0).getSEnd());
    }

    return "qurar";
  }
}
