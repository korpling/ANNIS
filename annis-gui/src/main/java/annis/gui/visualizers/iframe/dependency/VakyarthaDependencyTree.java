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
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Edge;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDataSourceSequence;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SPointingRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STYPE_NAME;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SFeature;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import java.io.IOException;
import java.io.Writer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
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
 * @author Kim Gerdes
 */
@PluginImplementation
public class VakyarthaDependencyTree extends WriterVisualizer
{

  private static final org.slf4j.Logger log = LoggerFactory.
    getLogger(VakyarthaDependencyTree.class);

  private Writer theWriter;

  private VisualizerInput input;

  private final String MAPPING_NODE_KEY = "node_key";

  private Properties mappings;

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
    this.mappings = input.getMappings();

    printHTMLOutput();
  }

  public void printHTMLOutput()
  {
    SDocumentGraph sDocumentGraph = input.getSResult().getSDocumentGraph();

    /**
     * Try to create a sorted map of nodes. The left annis feature token index
     * is used for sorting the nodes. It is possible the different nodes has the
     * same left token index, but the probability of this is small and it seem's
     * not to make much sense to visualize this. Mabye we should use the node
     * id.
     */
    Map<SNode, Integer> selectedNodes = new TreeMap<SNode, Integer>(new Comparator<SNode>()
    {
      private int getIdx(SNode snode)
      {
        if (snode instanceof SToken)
        {
          SFeature sF = snode.getSFeature(ANNIS_NS, FEAT_TOKENINDEX);
          return sF != null ? (int) (long) sF.getSValueSNUMERIC() : -1;
        }
        else
        {
          SFeature sF = snode.getSFeature(ANNIS_NS, FEAT_LEFTTOKEN);
          return sF != null ? (int) (long) sF.getSValueSNUMERIC() : -1;
        }
      }

      @Override
      public int compare(SNode o1, SNode o2)
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
      if (selectNode(n))
      {
        SFeature sFeature = n.getSFeature(ANNIS_NS, FEAT_TOKENINDEX);
        int tokenIdx = sFeature != null ? (int) (long) sFeature.
          getSValueSNUMERIC() : -1;
        selectedNodes.put(n, tokenIdx);
      }
    }

    Map<SNode, Integer> node2Int = new HashMap<SNode, Integer>();
    int count = 0;
    for (SNode tok : selectedNodes.keySet())
    {
      node2Int.put(tok, count++);
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
      for (SNode tok : selectedNodes.keySet())
      {
        JSONObject o = new JSONObject();
        o.put("t", getText(tok));

        JSONObject govs = new JSONObject();
        EList<Edge> sEdges = tok.getSGraph().getInEdges(tok.getSId());

        for (Edge e : sEdges)
        {
          if (!(e instanceof SPointingRelation) || !(e.getSource() instanceof SNode))
          {
            continue;
          }

          SPointingRelation sRelation = (SPointingRelation) e;
          SNode source = (SNode) sRelation.getSource();

          String label = "";
          for (SAnnotation anno : sRelation.getSAnnotations())
          {
            label = anno.getSValueSTEXT();
            break;
          }

          if (sRelation.getSource() == null
            || !node2Int.containsKey(source))
          {
            govs.put("root", label);
          }
          else
          {
            govs.put(String.valueOf(node2Int.get(source)), label);
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

  private String getText(SNode node)
  {
    SDocumentGraph sDocumentGraph = input.getSResult().getSDocumentGraph();
    EList<STYPE_NAME> textRelations = new BasicEList<STYPE_NAME>();
    textRelations.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);
    EList<SDataSourceSequence> sequences = sDocumentGraph.
      getOverlappedDSSequences(node, textRelations);

    if (sequences != null && sequences.size() > 0)
    {
      return ((STextualDS) sequences.get(0).getSSequentialDS()).getSText().
        substring(sequences.get(0).getSStart(), sequences.get(0).getSEnd());
    }

    return "";
  }

  /**
   * If the {@link VakyarthaDependencyTree#MAPPING_NODE_KEY} is set, then the
   * value of this mapping is used for selecting the SNode. If the mapping is
   * not set, it falls back to the default behaviour and only SToken are are
   * selected.
   */
  private boolean selectNode(SNode n)
  {
    String annoKey = null;

    if (mappings.containsKey(MAPPING_NODE_KEY))
    {
      annoKey = mappings.getProperty(MAPPING_NODE_KEY);
    }

    /**
     * Default behaviour, when mapping is not set correctly or is not set at
     * all.
     */
    if (annoKey == null)
    {
      if (n instanceof SToken)
      {
        return true;
      }
      else
      {
        return false;
      }
    }


    // if mapping is set, we check, if the node carries the mapped annotation key
    EList<SAnnotation> annos = n.getSAnnotations();
    for (SAnnotation a : annos)
    {
      if (annoKey.equals(a.getName()))
      {
        return true;
      }
    }

    return false;
  }
}
