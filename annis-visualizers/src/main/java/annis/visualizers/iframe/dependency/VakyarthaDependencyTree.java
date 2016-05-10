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
package annis.visualizers.iframe.dependency;

import static annis.model.AnnisConstants.ANNIS_NS;
import static annis.model.AnnisConstants.FEAT_RELANNIS_NODE;

import java.io.IOException;
import java.io.Writer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SPointingRelation;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.util.DataSourceSequence;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import annis.libgui.MatchedNodeColors;
import annis.libgui.visualizers.VisualizerInput;
import annis.model.RelannisNodeFeature;
import annis.visualizers.iframe.WriterVisualizer;
import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 * Provides a dependence visualization based on vakyartha visualization,
 * which was developed by Kim Gerdes.
 * 
 * Requires SVG enabled browser. <br />
 *
 * Originally this visualization was token based. Now Vakyartha extended to
 * visualize dependence between any nodes. This must be configured in the
 * resolver_vis_map table. <br />
 * 
 * Mappings: <br />
 * If a annotation key is specified with <b>node_key:key</b>, all
 * nodes which carry this annotation are searched for pointing relations and
 * instead of the token span the annotation value is used.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 * @author Benjamin Weißenfels<b.pixeldrama@gmail.com>
 * @author Kim Gerdes
 */
@PluginImplementation
public class VakyarthaDependencyTree extends WriterVisualizer
{

  private static final org.slf4j.Logger log = LoggerFactory.
    getLogger(VakyarthaDependencyTree.class);

  /**
   * If this mapping is not set in the resolver_vis_map table this visualization
   * is only based on the token level.
   */
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
    this.mappings = input.getMappings();

    /**
     * Try to create a sorted map of nodes. The left annis feature token index
     * is used for sorting the nodes. It is possible the different nodes has the
     * same left token index, but the probability of this is small and it seem's
     * not to make much sense to visualize this. Mabye we should use the node
     * id.
     *
     * Contains only token, if mappings does not contain "node_key".
     */
    Map<SNode, Integer> selectedNodes = new TreeMap<SNode, Integer>(
      new Comparator<SNode>()
      {
        private int getIdx(SNode snode)
        {
          
          RelannisNodeFeature feat = 
            (RelannisNodeFeature) snode.getFeature(ANNIS_NS, FEAT_RELANNIS_NODE).getValue();
          
          if (snode instanceof SToken)
          {
            return feat != null ? (int) feat.getTokenIndex(): -1;
          }
          else
          {
            return feat != null ? (int) feat.getLeftToken() : -1;
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

    printHTMLOutput(input, writer, selectedNodes);
  }

  public void printHTMLOutput(VisualizerInput input, Writer writer,
    Map<SNode, Integer> selectedNodes)
  {
    SDocumentGraph sDocumentGraph = input.getSResult().getDocumentGraph();

    for (SNode n : sDocumentGraph.getNodes())
    {
      if (selectNode(n))
      {
        RelannisNodeFeature feat = 
          (RelannisNodeFeature) n.getFeature(ANNIS_NS, FEAT_RELANNIS_NODE).getValue();
        
        int tokenIdx = feat != null ? (int) feat.getTokenIndex() : -1;
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
      println("<html>", writer);
      println("<head>", writer);

      
      LinkedList<String> fontsText = new LinkedList<String>();
      LinkedList<String> fontsDep = new LinkedList<String>();
      if (input.getFont() != null)
      {
        fontsText.add(input.getFont().getName());
        fontsDep.add(input.getFont().getName());
        println("<link href=\""
          + input.getFont().getUrl()
          + "\" rel=\"stylesheet\" type=\"text/css\" >", writer);
      }
      fontsText.add("sans-serif");
      fontsDep.add("serif");
      
      println(
        "<script type=\"text/javascript\" src=\""
        + input.getResourcePath("vakyartha/jquery.js") + "\"></script>", 
        writer);
      println("<script type=\"text/javascript\" src=\""
        + input.getResourcePath("vakyartha/raphael-min.js") + "\"></script>", writer);
      println(
        "<script type=\"text/javascript\" src=\""
        + input.getResourcePath("vakyartha/vakyarthaDependency.js") + "\"></script>", 
        writer);

      // output the data for the javascript
      println("<script type=\"text/javascript\">", writer);
      println("fcolors={};", writer);
      println("shownfeatures=[\"t\"];", writer);
      println("tokens=new Object();", writer);

      count = 0;
      for (SNode node : selectedNodes.keySet())
      {
        JSONObject vakyarthaObject = new JSONObject();

        String completeAnnotation = getAnnotation(node);
        String annotationValue = completeAnnotation.replaceFirst(
          ".*=", "");
        String text = getText(node, input);

        // decide, if the visualization is token based.
        if (mappings.containsKey(MAPPING_NODE_KEY))
        {
          vakyarthaObject.put("t", annotationValue);
        }
        else
        {
          vakyarthaObject.put("t", text);
        }
        vakyarthaObject.put("annotation", annotationValue);
        vakyarthaObject.put("text", text);
        vakyarthaObject.put("tooltip", completeAnnotation);

        JSONObject govs = new JSONObject();
        List<SRelation<SNode,SNode>> sEdges = node.getGraph().getInRelations(node.getId());

        for (SRelation<? extends SNode,? extends SNode> e : sEdges)
        {
          if(e instanceof SPointingRelation)
          {
            SPointingRelation sRelation = (SPointingRelation) e;
            boolean includeEdge = true;


            // check layer
            if(input.getNamespace() != null)
            {
              // must be included in the layer in order to be included
              includeEdge = false;
              if(sRelation.getLayers() != null)
              {
                for(SLayer layer : sRelation.getLayers())
                {
                  if(input.getNamespace().equals(layer.getName()))
                  {
                    includeEdge = true;
                    break;
                  }
                }
              }
            }

            if(includeEdge)
            {
              SNode source = (SNode) sRelation.getSource();

              String label = "";
              for (SAnnotation anno : sRelation.getAnnotations())
              {
                label = anno.getValue_STEXT();
                break;
              }

              if (sRelation.getSource() != null && node2Int.containsKey(source))
              {
                govs.put(String.valueOf(node2Int.get(source)), label);
              }
            }
          } // end if pointing relation
        }

        vakyarthaObject.put("govs", govs);
        JSONObject attris = new JSONObject();

        JSONObject tAttris = new JSONObject();
        String tokenColor = "black";
        if (input.getMarkedAndCovered().containsKey(node))
        {
          tokenColor = MatchedNodeColors
                  .getHTMLColorByMatch(input.getMarkedAndCovered().get(node));
        }
        tAttris.put("fill", tokenColor);
        tAttris.put("font", "11px " + StringUtils.join(fontsText, ","));

        attris.put("t", tAttris);
        
        JSONObject depAttris = new JSONObject();
        depAttris.put("fill", "#999");
        depAttris.put("font-style", "italic");
        depAttris.put("font", "12px " + StringUtils.join(fontsDep, ","));
        attris.put("deptext", depAttris);
        vakyarthaObject.put("attris", attris);

        writer.append("tokens[").append("" + count++).append("]=");
        writer.append(vakyarthaObject.toString().replaceAll("\n", " "));
        writer.append(";\n");
      }

      println("</script>", writer);

      println("</head>", writer);
      println("<body id=\"holder\">", writer);

      // the div to render the javascript to
//      println(
//        "<div id=\"holder\"> </div>", 
//        writer);

      println("</body>", writer);
      println("</html>", writer);
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

  private void println(String s, Writer writer) throws IOException
  {
    println(s, 0, writer);
  }

  private void println(String s, int indent, Writer writer) throws IOException
  {
    for (int i = 0; i < indent; i++)
    {
      writer.append("\t");
    }
    writer.append(s);
    writer.append("\n");
  }

  /**
   * Provid the whole annotation ((String)
   * &lt;namespace&gt;::&lt;key&gt;=&lt;value&gt;) value with namespace and
   * annotation key, which was defined in the mappings with the Key
   * {@link VakyarthaDependencyTree#MAPPING_NODE_KEY}.
   *
   * @return Empty string, if the mapping is not defined.
   */
  private String getAnnotation(SNode node)
  {

    if (mappings.containsKey(MAPPING_NODE_KEY)
      && mappings.getProperty(MAPPING_NODE_KEY) != null)
    {

      Set<SAnnotation> annos = node.getAnnotations();
      SAnnotation anno = null;

      for (SAnnotation a : annos)
      {
        if (mappings.getProperty(MAPPING_NODE_KEY).equals(a.getName()))
        {
          anno = a;
          break;
        }
      }

      return anno != null ? anno.getQName() + "=" + anno.getValue_STEXT(): "";
    }

    return "";
  }

  /**
   * Get the text which is overlapped by the SNode.
   *
   * @return Empty string, if there are no token overlapped by the node.
   */
  private String getText(SNode node, VisualizerInput input)
  {
    SDocumentGraph sDocumentGraph = input.getSResult().getDocumentGraph();

    List<DataSourceSequence> sequences = sDocumentGraph.
      getOverlappedDataSourceSequence(node, SALT_TYPE.STEXT_OVERLAPPING_RELATION);

    if (sequences != null && sequences.size() > 0)
    {
      return ((STextualDS) sequences.get(0).getDataSource()).getText().
        substring(sequences.get(0).getStart().intValue(), sequences.get(0).getEnd().intValue());
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
    Set<SAnnotation> annos = n.getAnnotations();
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
