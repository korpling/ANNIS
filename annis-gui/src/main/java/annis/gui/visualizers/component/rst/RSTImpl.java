/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.gui.visualizers.component.rst;

import annis.gui.MatchedNodeColors;
import annis.gui.visualizers.VisualizerInput;
import annis.gui.widgets.JITWrapper;
import com.vaadin.ui.Panel;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Edge;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.GRAPH_TRAVERSE_TYPE;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.resources.dot.Salt2DOT;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDataSourceSequence;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SStructure;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STYPE_NAME;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SGraphTraverseHandler;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Stack;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Visualizer Plugin for RST-Visualization.
 *
 * This class is used for generating for Generating a JSON-object which is
 * passed to the JITWrapper.
 *
 * Particularity: The pointing relations are provided as dominance edges from
 * salt which are typed as "edge" and carry a couple of annotation values, but
 * not "span" and "multinuc".
 *
 * The RST-Data-Model contains sentences in nodes with annotation value segment.
 * The segments are descends of nodes with annotation value group and the
 * relations names are span or multiunc.
 *
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class RSTImpl extends Panel implements SGraphTraverseHandler
{

  private final JITWrapper jit;
  private final Logger log = LoggerFactory.getLogger(RSTImpl.class);
  private Stack<JSONObject> st = new Stack<JSONObject>();
  // result of transform operation salt -> json
  private JSONObject result;
  private final String ANNOTATION_NAME = "cat";
  final String[] ANNOTATION_VALUES =
  {
    "span", "multinuc"
  };
  private final String DANGEROUS_RELATION = "edge";
  private final static String TYPE = "type";
  /**
   * Create a unique id, for every RSTImpl instance, for building an unique html
   * id, in the DOM.
   */
  private static int count = 0;
  private final String visId;
  private SDocumentGraph graph;
  private Map<SNode, Long> markedAndCovered;

  private String transformSaltToJSON(VisualizerInput visInput)
  {
    graph = visInput.getDocument().getSDocumentGraph();
    markedAndCovered = visInput.getMarkedAndCovered();
    EList<SNode> nodes = graph.getSRoots();
    EList<SNode> rootSNodes = new BasicEList<SNode>();


    if (nodes != null)
    {
      for (SNode node : nodes)
      {
        for (SAnnotation anno : node.getSAnnotations())
        {
          log.debug("anno name {}, anno value {}", anno.getName(), anno.getValue());

          if (ANNOTATION_NAME.equals(anno.getName()))
          {
            rootSNodes.add(node);
            log.debug("find root {} with {}", anno, ANNOTATION_NAME);
            break;
          }
        }
      }
    }

    Salt2DOT s2d = new Salt2DOT();
    s2d.salt2Dot(graph, URI.createFileURI("/tmp/graph_" + graph.getSName() + ".dot"));

    if (rootSNodes.size() > 0)
    {
      graph.traverse(rootSNodes, GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST, "jsonBuild", this);
    }
    else
    {
      log.debug("does not find an annotation which matched {}", ANNOTATION_NAME);
      graph.traverse(nodes, GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST, "jsonBuild", this);
    }

    log.debug("result json string: {}", result);

    try
    {

      String path = "/tmp/" + "json.js";
      FileOutputStream out = new FileOutputStream(path);
      out.write(("var json = " + result).toString().getBytes("UTF-8"));
      out.close();
    }
    catch (Exception ex)
    {
      log.error("writing json failed", ex);
    }
//    getWindow().executeJavaScript("console.log(" + result.toString() + ");");
    return result.toString();
  }

  public RSTImpl(VisualizerInput visInput)
  {

    //build id and increase count for every instance
    visId = "rst_" + count;
    count++;

    // get the jit wrapper;
    jit = new JITWrapper();
    this.addComponent(jit);

    // send the json to the widget
    jit.setVisData(transformSaltToJSON(visInput));
    jit.requestRepaint();
  }

  private JSONObject createJsonEntry(SNode currNode)
  {
    JSONObject jsonData = new JSONObject();
    StringBuilder sb = new StringBuilder();
    EList<SToken> token = new BasicEList<SToken>();
    EList<Edge> edges;

    if (currNode instanceof SStructure)
    {

      edges = currNode.getSGraph().getOutEdges(currNode.getSId());

      // get all tokens directly dominated tokens and build a string
      for (Edge e : edges)
      {
        SRelation sedge;

        if (e instanceof SRelation)
        {
          sedge = (SRelation) e;
        }
        else
        {
          log.error("wrong type of edge for {}", e);
          continue;
        }

        /**
         * Check if the SRelation points at a SToken and in this case check if,
         * only follow the edge with sType for avoiding double entries of SToken
         * in the token list
         */
        if (sedge.getSTarget() instanceof SToken
          && sedge.getSTypes() != null
          && sedge.getSTypes().size() > 0)
        {
          token.add((SToken) sedge.getSTarget());
        }
      }

      // build strings
      for (int i = 0; i < token.size(); i++)
      {
        SToken tok = token.get(i);
        String text = getText(tok);
        String color = getHTMLColor(tok);

        if (color != null)
        {
          sb.append("<span style=\"color : ").append(color).append(";\">");
        }
        else
        {
          sb.append("<span>");
        }

        if (i < token.size() - 1)
        {
          sb.append(text).append(" ");
        }
        else
        {
          sb.append(text);
        }

        sb.append("</span>");
      }
    }

    try
    {
      // build unique id, cause is used for an unique html element id.
      jsonData.put("id", getUniqueStringId(currNode));
      jsonData.put("name", currNode.getSName());

      // additional data for labelling edges and rendering sentences
      JSONObject data = new JSONObject();
      JSONArray edgesJSON = getIncomingEdgeTypeAnnotation(currNode);

      // since we have found some tokens, it must be a sentence in RST.
      if (token.size() > 0)
      {

        data.put("sentence", sb.toString());

        String color = null;
        if (markedAndCovered != null
          && markedAndCovered.containsKey(token.get(0))
          && (color = getHTMLColor(token.get(0))) != null)
        {
          data.put("color", color);
        }
      }


      if (edgesJSON != null)
      {
        data.put("edges", edgesJSON);
      }

      jsonData.put("data", data);

    }
    catch (JSONException ex)
    {
      log.error("problems create entry for {}", currNode, ex);
    }

    return jsonData;
  }

  private JSONObject appendChild(JSONObject root, JSONObject node)
  {
    try
    {
      root.append("children", node);
    }
    catch (JSONException ex)
    {
      log.error("cannot append {}", node, ex);
    }

    return node;
  }

  @Override
  public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode, SRelation sRelation, SNode fromNode, long order)
  {

    st.push(createJsonEntry(currNode));
  }

  @Override
  public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode, SRelation edge, SNode fromNode, long order)
  {
    assert st.size() > 0;

    if (st.size() == 1)
    {
      result = st.pop();
    }
    else
    {
      JSONObject node = st.pop();
      appendChild(st.peek(), node);
    }
  }

  @Override
  public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType,
    String traversalId, SRelation incomingEdge, SNode currNode, long order)
  {
    EList<String> sTypes;


    //entry case 
    if (incomingEdge == null)
    {
      return true;
    }

    // token data structures are not needed
    if (currNode instanceof SToken)
    {
      return false;
    }

    /**
     * check whether the edge has an sType or not, because there are always two
     * edges in the example rst corpus
     */
    if ((sTypes = incomingEdge.getSTypes()) != null && sTypes.size() > 0)
    {
      /**
       * the pointing relations are modelled as dominance relations with type
       * "edge" and do not carry the annotation "span" or "multinuc", so we will
       * have to exclude the "point relation" here
       */
      if (sTypes.size() == 1
        && DANGEROUS_RELATION.equals(sTypes.get(0))
        && this.detectWrongAnnotaton(incomingEdge))
      {
        return false;
      }
      else
      {
        return true;
      }
    }
    else
    {
      return false;
    }
  }

  /**
   * Gets the overlapping token as string from a node, which is direct dominated
   * by this node.
   *
   * @param currNode
   * @return is null, if there is no relation to a token, or there is more then
   * one STEXT is overlapped by this node
   */
  private String getText(SToken currNode)
  {
    EList<STYPE_NAME> relationTypes = new BasicEList<STYPE_NAME>();
    relationTypes.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);
    EList<SDataSourceSequence> sSequences = currNode.getSDocumentGraph().
      getOverlappedDSSequences(currNode, relationTypes);

    log.debug("sSequences {}", sSequences.toString());

    // only support one text for spanns
    if (sSequences == null || sSequences.size() != 1)
    {
      log.error("rst supports only one text and only text level");
      return null;
    }

    /**
     * Check if it is a text data structure. As described in the salt manual in
     * chapter "5.8 More specific nodes and relations" the start and end point
     * of a range of token is stored in superordinate node of type SSequentialDS
     */
    if (sSequences.get(0).getSSequentialDS() instanceof STextualDS)
    {
      STextualDS text = ((STextualDS) sSequences.get(0).getSSequentialDS());
      int start = sSequences.get(0).getSStart();
      int end = sSequences.get(0).getSEnd();
      return text.getSText().substring(start, end);
    }

    // something fundamentally goes wrong
    log.error("{} instead of {}",
      sSequences.get(0).getSSequentialDS().getClass().getName(),
      STextualDS.class.getName());

    return null;
  }

  /**
   * Returns false if the edge contains an annotation with value
   * {@link RSTImpl#EDGE_TYPE_POINTING_REL}.
   */
  private boolean detectWrongAnnotaton(SRelation edge)
  {
    EList<SAnnotation> annos = edge.getSAnnotations();

    for (SAnnotation anno : annos)
    {
      for (String value : ANNOTATION_VALUES)
      {
        if (value.equals(anno.getValueString()))
        {
          return false;
        }
      }
    }
    return true;
  }

  private JSONArray getIncomingEdgeTypeAnnotation(SNode node) throws JSONException
  {
    EList<Edge> edges = node.getSGraph().getInEdges(node.getId());
    EList<String> sTypes;
    EList<SAnnotation> annos;
    JSONArray edgeData = new JSONArray();

    for (Edge edge : edges)
    {


      if (edge instanceof SRelation)
      {
        sTypes = ((SRelation) edge).getSTypes();


        if (sTypes != null && sTypes.size() > 0 && isPointingRelation(edge))
        {
          JSONObject jsonEdge = new JSONObject();
          edgeData.put(jsonEdge);
          // asume that only one sType is defined
          jsonEdge.put("sType", sTypes.get(0));
          jsonEdge.put("from", getUniqueStringId(node));

          if (((SRelation) edge).getTarget() instanceof SNode)
          {
            jsonEdge.put("to",
              getUniqueStringId((SNode) ((SRelation) edge).getSource()));
          }
          else
          {
            throw new JSONException("could not cast to SNode");
          }

          annos = ((SRelation) edge).getSAnnotations();

          if (annos != null)
          {
            for (SAnnotation anno : annos)
            {
              jsonEdge.append("annotation", anno.getSValueSTEXT());
            }
          }
        }
      }
    }

    return edgeData;
  }

  /**
   * Build a unique HTML id.
   */
  private String getUniqueStringId(SNode node)
  {
    return visId + "_" + node.getSName();
  }

  /**
   * The salt graph does not contain pointing relations, so we have to check, if
   * the edge annotation contains at least one of this
   * {@link RSTImpl#ANNOTATION_VALUES}.
   *
   */
  private boolean isPointingRelation(Edge edge)
  {
    EList<SAnnotation> annos;
    if (edge instanceof SRelation)
    {
      annos = ((SRelation) edge).getSAnnotations();

      if (annos != null)
      {
        for (SAnnotation anno : annos)
        {
          for (String span : ANNOTATION_VALUES)
          {
            if (span.equals(anno.getSValueSTEXT()))
            {
              return false;
            }
          }
        }
      }
    }
    return true;
  }

  /**
   * Checks, if a specific token is marked as matching token and returns a HTML
   * color string.
   *
   * @return is null when token is not marked
   */
  private String getHTMLColor(SToken token)
  {

    if (!markedAndCovered.containsKey(token))
    {
      return null;
    }

    /**
     * Since the range in markedAndCovered is from 1 up to 8, we have to
     * decrease the value, for matching the colors in KWIC.
     */
    int color = (int) (long) markedAndCovered.get(token);
    color = Math.min(color > 0 ? color - 1 : color,
      MatchedNodeColors.values().length - 1);

    return MatchedNodeColors.values()[color].getHTMLColor();
  }
}
