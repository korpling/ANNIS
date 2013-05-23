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
package annis.visualizers.component.rst;

import annis.CommonHelper;
import annis.libgui.MatchedNodeColors;
import annis.gui.components.CssRenderInfo;
import annis.libgui.visualizers.VisualizerInput;
import annis.gui.widgets.JITWrapper;
import annis.gui.widgets.gwt.client.ui.VJITWrapper;
import static annis.model.AnnisConstants.*;
import com.vaadin.ui.Panel;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Edge;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.GRAPH_TRAVERSE_TYPE;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDataSourceSequence;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SStructure;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STYPE_NAME;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SFeature;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SGraphTraverseHandler;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SProcessingAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeSet;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Visualizer Plugin for RST-Visualization.
 *
 * This Visualization transforms the salt graph to a json object, which is sent
 * to the {@link VJITWrapper}.
 *
 * A node with an incoming rst edge will be moved one level up, so that it
 * becomes a sibling of its original parent. This is done, because in typical
 * rst visualizations these nodes are drawn as siblings of their parent, so they
 * are in one horizontal line with their parent, but they are actually modeled
 * as children, which is confusing, when we want to render these nodes. The
 * json, which is generated, looks nearly like this:
 *
 * <pre>
 * {
 *  "id" : "root"
 *  "children" : [
 *  {
 *    "id" : "rst_0_1",
 *    "name" : 1
 *    "edges" : [{"sType" : "rst", from: "rst_0_2", to : "rst_0_1"}
 *  },
 *  {
 *    "id" : "rst_0_2",
 *    "name" : "2
 *  }]
 * }
 * </pre>
 *
 * The example above shows the two nodes, which are connected by a rst edge.
 * They are on the same level in the json tree. The *natural* tree would have
 * looked like this:
 *
 * <pre>
 * {
 *  "id" : "root"
 *  "children" : [
 *  {
 *    "id" : "rst_0_1",
 *    "name" : 1
 *    "edges" : [{"sType" : "rst", from: "rst_0_2", to : "rst_0_1"},
 *    "children" : [{
 *      "id" : "rst_0_2",
 *      "name" : "2
 *    }]
 *  }]
 * }
 * </pre>
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class RSTImpl extends Panel implements SGraphTraverseHandler {

  // implements the AbstractComponent and talks to the VJITWrapperWidget
  private final JITWrapper jit;

  // traversing stack for build the json tree
  private Stack<JSONObject> st = new Stack<JSONObject>();

  // result of transform operation salt -> json
  private JSONObject result = new JSONObject();

  // filter root nodes with this annotation key
  private final String ANNOTATION_KEY = "cat";

  // sType for the rst relation
  private final String RST_RELATION = "rst";

  /**
   * Create a unique id, for every RSTImpl instance, for building an unique html
   * id, in the DOM.
   */
  private static int count = 0;

  // unique id for every instance of RSTImpl
  private final String visId;

  // result graph
  private SDocumentGraph graph;

  // namespace for SProcessingAnnotation sentence index
  static private final String SENTENCE_INDEX = "sentence_index";

  static private final String SENTENCE_LEFT = "sentence_left";

  static private final String SENTENCE_RIGHT = "sentence_right";

  // contains all nodes which are marked as matches and child nodes of matches
  private final Map<SNode, Long> markedAndCovered;

  /**
   * Sorted list of all SStructures which overlapped a sentence. It's used for
   * mapping the sentence to a number by the order of the SStructures in the
   * list.
   */
  private TreeSet<SStructure> sentences = new TreeSet<SStructure>(
          new Comparator<SStructure>() {
    private int getStartPosition(SStructure s) {
      EList<Edge> out = s.getSGraph().getOutEdges(s.getSId());

      for (Edge e : out) {
        if (e instanceof SRelation
                && ((SRelation) e).getTarget() instanceof SToken) {
          SToken tok = ((SToken) ((SRelation) e).getTarget());
          SFeature sf = tok.getSFeature(
                  ANNIS_NS + "::" + FEAT_LEFTTOKEN);
          return Integer.parseInt(sf.getSValueSTEXT());
        }
      }

      SFeature sf = s.getSFeature(
              ANNIS_NS + "::" + FEAT_LEFTTOKEN);
      return Integer.parseInt(sf.getSValueSTEXT());
    }

    @Override
    public int compare(SStructure t1, SStructure t2) {
      int t1Idx = getStartPosition(t1);
      int t2Idx = getStartPosition(t2);

      if (t1Idx < t2Idx) {
        return -1;
      }

      if (t1Idx == t2Idx) {
        return 0;
      } else {
        return 1;
      }
    }
  });

  private final Logger log = LoggerFactory.getLogger(RSTImpl.class);

  public RSTImpl(VisualizerInput visInput) {

    markedAndCovered = visInput.getMarkedAndCovered();

    /**
     * build id and increase count for every instance, so we receive an unique
     * id
     */
    visId = "rst_" + count;
    count++;

    jit = new JITWrapper();
    jit.setWidth("100%");
    jit.setHeight("-1px");
    setContent(jit);

    // send the json to the widget
    jit.setVisData(transformSaltToJSON(visInput));
    jit.setProperties(visInput.getMappings());
    jit.requestRepaint();

  }

  public void addExtension(CssRenderInfo renderInfo) {
    super.addExtension(renderInfo);
  }

  private void addScrollbar() {
    this.setWidth("100%");
    this.getContent().setSizeUndefined();
  }

  private String transformSaltToJSON(VisualizerInput visInput) {
    graph = visInput.getSResult().getSDocumentGraph();
    EList<SNode> rootSNodes = graph.getSRoots();
    EList<SNode> rstRoots = new BasicEList<SNode>();


    for (SNode sNode : rootSNodes) {
      if (CommonHelper.checkSLayer("rst", sNode)) {
        rstRoots.add(sNode);
      }
    }


    if (rootSNodes.size() > 0) {
      graph.traverse(rstRoots, GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST,
              "getSentences", new SGraphTraverseHandler() {
        @Override
        public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType,
                String traversalId, SNode currNode, SRelation sRelation,
                SNode fromNode, long order) {
          if (currNode instanceof SStructure && isSegment(currNode)) {
            sentences.add((SStructure) currNode);
          }
        }

        @Override
        public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType,
                String traversalId, SNode currNode, SRelation edge,
                SNode fromNode,
                long order) {
        }

        @Override
        public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType,
                String traversalId, SRelation edge, SNode currNode, long order) {

          // token are not needed
          if (currNode instanceof SToken) {
            return false;
          }

          return true;
        }
      });

      //decorate segments with sentence number
      int i = 0;
      for (SStructure sentence : sentences) {
        sentence.createSProcessingAnnotation(
                SENTENCE_INDEX, SENTENCE_INDEX, Integer.toString(i));
        i++;
      }

      graph.traverse(rootSNodes, GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST,
              "jsonBuild", this);
    } else {
      log.debug("does not find an annotation which matched {}",
              ANNOTATION_KEY);
      graph.traverse(rootSNodes, GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST,
              "jsonBuild", this);
    }

    return result.toString();
  }

  private JSONObject createJsonEntry(SNode currNode) {
    JSONObject jsonData = new JSONObject();
    StringBuilder sb = new StringBuilder();
    EList<SToken> token = new BasicEList<SToken>();
    EList<Edge> edges;

    if (currNode instanceof SStructure) {

      edges = currNode.getSGraph().getOutEdges(currNode.getSId());

      // get all tokens directly dominated tokens and build a string
      for (Edge e : edges) {
        SRelation sedge;

        if (e instanceof SRelation) {
          sedge = (SRelation) e;
        } else {
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
                && sedge.getSTypes().size() > 0) {
          token.add((SToken) sedge.getSTarget());
        }
      }

      // build strings
      for (int i = 0; i < token.size(); i++) {
        SToken tok = token.get(i);
        String text = getText(tok);
        String color = getHTMLColor(tok);

        if (color != null) {
          sb.append("<span style=\"color : ").append(color).append(";\">");
        } else {
          sb.append("<span>");
        }

        if (i < token.size() - 1) {
          sb.append(text).append(" ");
        } else {
          sb.append(text);
        }

        sb.append("</span>");
      }
    }

    try {
      // build unique id, cause is used for an unique html element id.
      jsonData.put("id", getUniStrId(currNode));
      jsonData.put("name", currNode.getSName());

      /**
       * additional data oject for edge labels and rendering sentences
       */
      JSONObject data = new JSONObject();
      JSONArray edgesJSON = getOutGoingEdgeTypeAnnotation(currNode);


      // since we have found some tokens, it must be a sentence in RST.
      if (token.size() > 0) {
        data.put("sentence", sb.toString());
      }


      if (edgesJSON != null) {
        data.put("edges", edgesJSON);
      }

      if (currNode instanceof SStructure && isSegment(currNode)) {
        SProcessingAnnotation sentence_idx = currNode.
                getSProcessingAnnotation(SENTENCE_INDEX + "::" + SENTENCE_INDEX);
        int index = sentence_idx == null ? -1 : Integer.parseInt(sentence_idx.
                getValueString());

        data.put(SENTENCE_LEFT, index);
        data.put(SENTENCE_RIGHT, index);
      }

      jsonData.put("data", data);
    } catch (JSONException ex) {
      log.error("problems create entry for {}", currNode, ex);
    }

    return jsonData;
  }

  private JSONObject appendChild(JSONObject root, JSONObject node,
          SNode currSnode) {
    try {
      // is set to true, when currNode is reached by an rst edge
      boolean isAppendedToParent = false;

      EList<Edge> in = currSnode.getSGraph().getInEdges(currSnode.getSId());

      if (in != null) {

        for (Edge e : in) {
          if (e instanceof SRelation && hasRSTType((SRelation) e)) {
            JSONObject tmp;


            if (st.size() > 1) {
              tmp = st.pop();
              st.peek().append("children", node);
              sortChildren(st.peek());
              st.push(tmp);
            } else {
              result.append("children", node);
            }

            setSentenceSpan(node, st.peek());
            isAppendedToParent = true;
            break;
          }
        }
      }

      if (!isAppendedToParent) {
        root.append("children", node);
        setSentenceSpan(node, root);
        sortChildren(root);
      }
    } catch (JSONException ex) {
      log.error("cannot append {}", node, ex);
    }

    return node;
  }

  @Override
  public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType,
          String traversalId,
          SNode currNode, SRelation sRelation, SNode fromNode, long order) {
    st.push(createJsonEntry(currNode));
  }

  @Override
  public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
          SNode currNode, SRelation edge, SNode fromNode, long order) {
    assert st.size() > 0;

    if (st.size() == 1) {
      try {
        result.append("children", st.pop());
        sortChildren(result);
      } catch (JSONException ex) {
        log.error("Problems with adding roots", ex);
      }
    } else {
      JSONObject jsonNode = st.pop();
      appendChild(st.peek(), jsonNode, currNode);
    }
  }

  @Override
  public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType,
          String traversalId, SRelation incomingEdge, SNode currNode, long order) {
    // token data structures are not needed
    if (currNode instanceof SToken) {
      return false;
    }

    return true;
  }

  /**
   * Gets the overlapping token as string from a node, which are direct
   * dominated by this node.
   *
   * @param currNode
   * @return is null, if there is no relation to a token, or there is more then
   * one STEXT is overlapped by this node
   */
  private String getText(SToken currNode) {
    EList<STYPE_NAME> relationTypes = new BasicEList<STYPE_NAME>();
    relationTypes.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);
    EList<SDataSourceSequence> sSequences = currNode.getSDocumentGraph().
            getOverlappedDSSequences(currNode, relationTypes);

    log.debug("sSequences {}", sSequences.toString());

    // only support one text for spanns
    if (sSequences == null || sSequences.size() != 1) {
      log.error("rst supports only one text and only text level");
      return null;
    }

    /**
     * Check if it is a text data structure. As described in the salt manual in
     * chapter "5.8 More specific nodes and relations" the start and end point
     * of a range of token is stored in superordinate node of type SSequentialDS
     */
    if (sSequences.get(0).getSSequentialDS() instanceof STextualDS) {
      STextualDS text = ((STextualDS) sSequences.get(0).getSSequentialDS());
      int start = sSequences.get(0).getSStart();
      int end = sSequences.get(0).getSEnd();
      return text.getSText().substring(start, end);


    }

    // something fundamentally goes wrong
    log.error("{} instead of {}",
            sSequences.get(0).getSSequentialDS().getClass().getName(),
            STextualDS.class
            .getName());


    return null;
  }

  private JSONArray getOutGoingEdgeTypeAnnotation(SNode node) throws
          JSONException {
    EList<Edge> out = node.getSGraph().getOutEdges(node.getId());
    EList<String> sTypes;
    EList<SAnnotation> annos;
    JSONArray edgeData = new JSONArray();


    // check if there is a pointing relation
    if (out == null) {
      return edgeData;
    }

    for (Edge edge : out) {
      if (!(edge instanceof SRelation) || edge.getTarget() instanceof SToken) {
        continue;
      }

      sTypes = ((SRelation) edge).getSTypes();


      if (sTypes != null && sTypes.size() > 0) {
        JSONObject jsonEdge = new JSONObject();
        edgeData.put(jsonEdge);

        jsonEdge.put("sType", sTypes.get(0));


        if (((SRelation) edge).getTarget() instanceof SNode) {
          /**
           * Invert the direction of the RST-edge.
           */
          if (getRSTType().equals(sTypes.get(0))) {
            jsonEdge.put("to", getUniStrId(node));
            jsonEdge.put("from",
                    getUniStrId((SNode) ((SRelation) edge).getTarget()));
          } else {
            jsonEdge.put("from", getUniStrId(node));
            jsonEdge.put("to",
                    getUniStrId((SNode) ((SRelation) edge).getTarget()));
          }
        } else {
          throw new JSONException("could not cast to SNode");
        }

        annos = ((SRelation) edge).getSAnnotations();

        if (annos != null) {
          for (SAnnotation anno : annos) {
            jsonEdge.append("annotation", anno.getSValueSTEXT());
          }
        }
      }
    }

    return edgeData;
  }

  /**
   * Build a unique HTML id.
   */
  private String getUniStrId(SNode node) {
    return visId + "_" + node.getSName();
  }

  /**
   * Checks, if a specific token is marked as matching token and returns a HTML
   * color string.
   *
   * @return is null when token is not marked
   */
  private String getHTMLColor(SToken token) {

    if (!markedAndCovered.containsKey(token)) {
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

  /**
   * Checks, if there exists an SRelation which targets a SToken.
   */
  private boolean isSegment(SNode currNode) {

    EList<Edge> edges = currNode.getSGraph().getOutEdges(currNode.getSId());

    if (edges != null && edges.size() > 0) {
      for (Edge edge : edges) {
        if (edge.getTarget() instanceof SToken) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Sets the sentence_left and sentence_right properties of the data object of
   * parent to the min/max of the currNode.
   */
  private void setSentenceSpan(JSONObject cNode, JSONObject parent) {
    try {
      JSONObject data = cNode.getJSONObject("data");

      int leftPosC = data.getInt(SENTENCE_LEFT);
      int rightPosC = data.getInt(SENTENCE_RIGHT);

      data = parent.getJSONObject("data");

      if (data.has(SENTENCE_LEFT)) {
        data.put(SENTENCE_LEFT, Math.min(leftPosC, data.getInt(SENTENCE_LEFT)));
      } else {
        data.put(SENTENCE_LEFT, leftPosC);
      }

      if (data.has(SENTENCE_RIGHT)) {
        data.put(SENTENCE_RIGHT,
                Math.max(rightPosC, data.getInt(SENTENCE_RIGHT)));
      } else {
        data.put(SENTENCE_RIGHT, rightPosC);
      }
    } catch (JSONException ex) {
      log.debug("error while setting left and right position for sentences", ex);
    }
  }

  /**
   * Sorts the children of root by the the sentence indizes. Since the sentence
   * indizes are based on the token indizes, some sentences have no sentences
   * indizes, because sometimes token nodes are out of context.
   *
   * A kind of insertion sort would be better than the used mergesort.
   *
   * And it is a pity that the {@link JSONArray} has no interface to sort the
   * underlying {@link Array}.
   *
   */
  private void sortChildren(JSONObject root) throws JSONException {
    JSONArray children = root.getJSONArray("children");
    List<JSONObject> childrenSorted = new ArrayList<JSONObject>(children.
            length());

    for (int i = 0; i < children.length(); i++) {
      childrenSorted.add(children.getJSONObject(i));
    }

    Collections.sort(childrenSorted, new Comparator<Object>() {
      @Override
      public int compare(Object o1, Object o2) {
        int o1IdxLeft = 0;
        int o1IdxRight = 0;
        int o2IdxLeft = 0;
        int o2IdxRight = 0;
        try {
          o1IdxLeft = ((JSONObject) o1).getJSONObject("data").getInt(
                  SENTENCE_LEFT);
          o1IdxRight = ((JSONObject) o1).getJSONObject("data").getInt(
                  SENTENCE_RIGHT);
          o2IdxLeft = ((JSONObject) o2).getJSONObject("data").getInt(
                  SENTENCE_LEFT);
          o2IdxRight = ((JSONObject) o2).getJSONObject("data").getInt(
                  SENTENCE_RIGHT);
        } catch (JSONException ex) {
          log.error("Could not compare sentence indizes.", ex);
        }

        if (o1IdxLeft + o1IdxRight > o2IdxLeft + o2IdxRight) {
          return 1;
        }

        if (o1IdxLeft + o1IdxRight == o2IdxLeft + o2IdxRight) {
          return 0;
        } else {
          return -1;
        }
      }
    });

    children = new JSONArray(childrenSorted);
    root.put("children", children);

    addScrollbar();

  }

  private boolean hasRSTType(SRelation e) {
    EList<String> sTypes = e.getSTypes();

    for (String sType : sTypes) {
      if (getRSTType().equalsIgnoreCase(sType)) {
        return true;
      }
    }
    return false;
  }

  private String getRSTType() {
    return RST_RELATION;
  }
}
