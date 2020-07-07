/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package annis.visualizers.component.rst;

import annis.CommonHelper;
import annis.gui.components.CssRenderInfo;
import annis.gui.widgets.JITWrapper;
import annis.gui.widgets.gwt.client.ui.VJITWrapper;
import annis.libgui.MatchedNodeColors;
import annis.libgui.visualizers.VisualizerInput;
import com.vaadin.ui.Panel;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.UUID;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SStructure;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.GraphTraverseHandler;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SGraph.GRAPH_TRAVERSE_TYPE;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SProcessingAnnotation;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.util.DataSourceSequence;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Visualizer Plugin for RST-Visualization.
 *
 * This Visualization transforms the salt graph to a json object, which is sent to the
 * {@link VJITWrapper}.
 *
 * A node with an incoming rst edge will be moved one level up, so that it becomes a sibling of its
 * original parent. This is done, because in typical rst visualizations these nodes are drawn as
 * siblings of their parent, so they are in one horizontal line with their parent, but they are
 * actually modeled as children, which is confusing, when we want to render these nodes. The json,
 * which is generated, looks nearly like this:
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
 * The example above shows the two nodes, which are connected by a rst edge. They are on the same
 * level in the json tree. The *natural* tree would have looked like this:
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
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
public class RSTImpl extends Panel implements GraphTraverseHandler {

  /**
   * 
   */
  private static final long serialVersionUID = 1355629687872757529L;

  // namespace for SProcessingAnnotation sentence index
  static private final String SENTENCE_INDEX = "sentence_index";

  static private final String SENTENCE_LEFT = "sentence_left";

  static private final String SENTENCE_RIGHT = "sentence_right";

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
   * Create a unique id, for every RSTImpl instance, for building an unique html id, in the DOM.
   */
  private final UUID uniqueID = UUID.randomUUID();

  // unique id for every instance of RSTImpl
  private final String visId;

  // result graph
  private SDocumentGraph graph;

  // contains all nodes which are marked as matches and child nodes of matches
  private final Map<SNode, Long> markedAndCovered;

  private Map<String, String> mappings;

  private String namespace;

  private final Map<SToken, Integer> token2index = new HashMap<>();

  private final Logger log = LoggerFactory.getLogger(RSTImpl.class);

  /**
   * Sorted list of all SStructures which overlapped a sentence. It's used for mapping the sentence
   * to a number by the order of the SStructures in the list.
   */
  private final TreeSet<SStructure> sentences;

  public RSTImpl(VisualizerInput visInput) {

    markedAndCovered = visInput.getMarkedAndCovered();

    mappings = visInput.getMappings();

    namespace = visInput.getNamespace();

    visId = "rst_" + uniqueID.toString();

    int tokIdx = 0;
    for (SToken tok : visInput.getDocument().getDocumentGraph().getSortedTokenByText()) {
      token2index.put(tok, tokIdx++);
    }

    this.sentences = new TreeSet<SStructure>(new Comparator<SStructure>() {
      @Override
      public int compare(SStructure t1, SStructure t2) {
        List<SToken> overlappedT1 = t1.getGraph().getOverlappedTokens(t1);
        List<SToken> overlappedT2 = t2.getGraph().getOverlappedTokens(t2);


        int byStart =
            Integer.compare(getStartPosition(overlappedT1), getStartPosition(overlappedT2));
        if (byStart == 0) {
          // use end value (which means shorter sentences come first)
          int byEnd = Integer.compare(getEndPosition(overlappedT1), getEndPosition(overlappedT2));
          if (byEnd == 0) {
            // resort to ID as a last way to compare two sentences
            return t1.getId().compareTo(t2.getId());
          } else {
            return byEnd;
          }
        } else {
          return byStart;
        }
      }

      private int getEndPosition(Collection<SToken> overlappedToken) {

        int pos = Integer.MIN_VALUE;
        for (SToken tok : overlappedToken) {
          pos = Math.max(pos, token2index.get(tok));
        }

        return pos;
      }

      private int getStartPosition(Collection<SToken> overlappedToken) {

        int pos = Integer.MAX_VALUE;
        for (SToken tok : overlappedToken) {
          pos = Math.min(pos, token2index.get(tok));
        }

        return pos;
      }
    });

    jit = new JITWrapper();
    jit.setWidth("100%");
    jit.setHeight("-1px");
    setContent(jit);

    // send the json to the widget
    jit.setVisData(transformSaltToJSON(visInput));
    jit.setProperties(visInput.getMappings());
    jit.requestRepaint();

    addScrollbar();

  }

  public void addExtension(CssRenderInfo renderInfo) {
    super.addExtension(renderInfo);
  }

  private void addScrollbar() {
    this.setWidth("100%");
    this.getContent().setSizeUndefined();
  }

  private JSONObject appendChild(JSONObject root, JSONObject node, SNode currSnode) {
    try {
      // is set to true, when currNode is reached by an rst edge
      boolean isAppendedToParent = false;

      List<SRelation<SNode, SNode>> in = currSnode.getGraph().getInRelations(currSnode.getId());

      if (in != null) {

        for (SRelation<SNode, SNode> e : in) {
          if (hasRSTType(e)) {
            JSONObject tmp;

            if (st.size() > 1) {
              tmp = st.pop();
              getOrCreateArray(st.peek(), "children").put(node);
              sortChildren(st.peek());
              st.push(tmp);
            } else {
              getOrCreateArray(result, "children").put(node);
            }

            setSentenceSpan(node, st.peek());
            isAppendedToParent = true;
            break;
          }
        }
      }

      if (!isAppendedToParent) {
        getOrCreateArray(root, "children").put(node);
        setSentenceSpan(node, root);
        sortChildren(root);
      }
    } catch (JSONException ex) {
      log.error("cannot append {}", node, ex);
    }

    return node;
  }

  @Override
  public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
      SRelation incomingEdge, SNode currNode, long order) {
    // token data structures are not needed
    if (currNode instanceof SToken) {
      return false;
    } else if (CommonHelper.checkSLayer(namespace, currNode)) {
      return true;
    }

    return false;
  }

  private JSONObject createJsonEntry(SNode currNode) {
    JSONObject jsonData = new JSONObject();
    JSONObject data = new JSONObject();
    StringBuilder sb = new StringBuilder();
    // use a hash set so we don't get any duplicate entries
    LinkedHashSet<SToken> token = new LinkedHashSet<>();
    List<SRelation<SNode, SNode>> edges;

    if (currNode instanceof SStructure) {

      SDocumentGraph dgraph = ((SStructure) currNode).getGraph();
      edges = currNode.getGraph().getOutRelations(currNode.getId());
      List<SToken> overlappedToken = new LinkedList<>();
      // get all tokens directly dominated tokens and build a string
      for (SRelation<SNode, SNode> sedge : edges) {
        if (sedge.getTarget() instanceof SToken) {
          overlappedToken.add((SToken) sedge.getTarget());
        }
      }
      token.addAll(dgraph.getSortedTokenByText(overlappedToken));

      // build strings
      Iterator<SToken> tokIterator = token.iterator();
      while (tokIterator.hasNext()) {
        SToken tok = tokIterator.next();
        String text = getText(tok);
        String color = getHTMLColor(tok);

        if (color != null) {
          sb.append("<span class=\"rst-token\" style=\"color : ").append(color).append(";\">");
        } else {
          sb.append("<span class=\"rst-token\">");
        }

        if (tokIterator.hasNext()) {
          sb.append(text).append(" ");
        } else {
          sb.append(text);
        }

        sb.append("</span>");
      }

      // add signals
      JSONArray signals = new JSONArray();
      for (SRelation<SNode, SNode> relation : currNode.getInRelations()) {
        if (isSignalNode(relation.getSource())) {
          signals.put(jsonizeSignalNode(relation.getSource()));
        }
      }
      if (signals.length() > 0) {
        data.put("signals", signals);
      }
    }

    try {
      // build unique id, cause is used for an unique html element id.
      jsonData.put("id", getUniStrId(currNode));
      jsonData.put("name", currNode.getName());

      /**
       * additional data oject for edge labels and rendering sentences
       */
      JSONArray edgesJSON = getOutGoingEdgeTypeAnnotation(currNode);

      // since we have found some tokens, it must be a sentence in RST.
      if (token.size() > 0) {
        data.put("sentence", sb.toString());
      }

      if (edgesJSON != null) {
        data.put("edges", edgesJSON);
      }

      if (currNode instanceof SStructure && isSegment(currNode)) {
        SProcessingAnnotation sentence_idx =
            currNode.getProcessingAnnotation(SENTENCE_INDEX + "::" + SENTENCE_INDEX);
        int index = sentence_idx == null ? -1 : Integer.parseInt(sentence_idx.getValue_STEXT());

        data.put(SENTENCE_LEFT, index);
        data.put(SENTENCE_RIGHT, index);
      }

      jsonData.put("data", data);
    } catch (JSONException ex) {
      log.error("problems create entry for {}", currNode, ex);
    }

    return jsonData;
  }

  /**
   * Checks, if a specific token is marked as matching token and returns a HTML color string.
   *
   * @return is null when token is not marked
   */
  private String getHTMLColor(SToken token) {

    if (!markedAndCovered.containsKey(token)) {
      return null;
    }

    /**
     * Since the range in markedAndCovered is from 1 up to 8, we have to decrease the value, for
     * matching the colors in KWIC.
     */
    int color = (int) (long) markedAndCovered.get(token);
    color = Math.min(color > 0 ? color - 1 : color, MatchedNodeColors.values().length - 1);

    return MatchedNodeColors.values()[color].getHTMLColor();
  }

  private JSONArray getOrCreateArray(JSONObject parent, String key) throws JSONException {
    JSONArray array = parent.has(key) ? parent.getJSONArray(key) : null;
    if (array == null) {
      array = new JSONArray();
      parent.put(key, array);
    }
    return array;
  }

  private JSONArray getOutGoingEdgeTypeAnnotation(SNode node) throws JSONException {
    List<SRelation<SNode, SNode>> out = node.getGraph().getOutRelations(node.getId());
    String type;
    Set<SAnnotation> annos;
    JSONArray edgeData = new JSONArray();

    // check if there is a pointing relation
    if (out == null) {
      return edgeData;
    }

    for (SRelation<SNode, SNode> edge : out) {
      if (edge.getTarget() instanceof SToken) {
        continue;
      }

      type = ((SRelation) edge).getType();
      String sTypeAsString = "edge";
      if (type != null && !type.isEmpty()) {
        sTypeAsString = type;
      }

      JSONObject jsonEdge = new JSONObject();
      edgeData.put(jsonEdge);

      jsonEdge.put("sType", sTypeAsString);

      if (((SRelation) edge).getTarget() instanceof SNode) {
        /**
         * Invert the direction of the RST-edge.
         */
        if (getRSTType().equals(sTypeAsString)) {
          jsonEdge.put("to", getUniStrId(node));
          jsonEdge.put("from", getUniStrId((SNode) ((SRelation) edge).getTarget()));
        } else {
          jsonEdge.put("from", getUniStrId(node));
          jsonEdge.put("to", getUniStrId((SNode) ((SRelation) edge).getTarget()));
        }
      } else {
        throw new JSONException("could not cast to SNode");
      }

      annos = edge.getAnnotations();

      if (annos != null) {
        for (SAnnotation anno : annos) {
          getOrCreateArray(jsonEdge, "annotation").put(anno.getValue_STEXT());
        }
      }

    }

    return edgeData;
  }

  private String getRSTType() {
    return mappings.getOrDefault("edge", RST_RELATION);
  }

  /**
   * Gets the overlapping token as string from a node, which are direct dominated by this node.
   *
   * @param currNode
   * @return is null, if there is no relation to a token, or there is more then one STEXT is
   *         overlapped by this node
   */
  private String getText(SToken currNode) {

    List<DataSourceSequence> sSequences = currNode.getGraph()
        .getOverlappedDataSourceSequence(currNode, SALT_TYPE.STEXT_OVERLAPPING_RELATION);

    // only support one text for spanns
    if (sSequences == null || sSequences.size() != 1) {
      log.error("rst supports only one text and only text level");
      return null;
    }

    log.debug("sSequences {}", sSequences.toString());

    /**
     * Check if it is a text data structure. As described in the salt manual in chapter "5.8 More
     * specific nodes and relations" the start and end point of a range of token is stored in
     * superordinate node of type SSequentialDS
     */
    if (sSequences.get(0).getDataSource() instanceof STextualDS) {
      STextualDS text = ((STextualDS) sSequences.get(0).getDataSource());
      int start = sSequences.get(0).getStart().intValue();
      int end = sSequences.get(0).getEnd().intValue();
      return text.getText().substring(start, end);

    }

    // something fundamentally goes wrong
    log.error("{} instead of {}", sSequences.get(0).getDataSource().getClass().getName(),
        STextualDS.class.getName());

    return null;
  }

  /**
   * Build a unique HTML id.
   */
  private String getUniStrId(SNode node) {
    return visId + "_" + node.getId();
  }

  private boolean hasRSTType(SRelation e) {
    String type = e.getType();

    if (e.getTarget() instanceof SToken && e.getType() == null) {
      return true;
    } else if (type != null) {
      if (getRSTType().equalsIgnoreCase(type)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks, if there exists an SRelation which targets a SToken.
   */
  private boolean isSegment(SNode currNode) {

    List<SRelation<SNode, SNode>> edges = currNode.getGraph().getOutRelations(currNode.getId());

    if (edges != null && edges.size() > 0) {
      for (SRelation<SNode, SNode> edge : edges) {
        if (edge.getTarget() instanceof SToken) {
          return true;
        }
      }
    }

    return false;
  }

  private boolean isSignalNode(SNode sNode) {
    return sNode.getAnnotation("default_ns", "signal_type") != null;
  }

  private JSONObject jsonizeSignalNode(SNode node) {
    JSONObject signal = new JSONObject();
    signal.put("type", node.getAnnotation("default_ns", "signal_type").getValue());
    signal.put("subtype", node.getAnnotation("default_ns", "signal_subtype").getValue());
    JSONArray indexes = new JSONArray();
    SAnnotation indexesAnn = node.getAnnotation("default_ns", "signal_indexes");
    if (indexesAnn != null) {
      for (String index : ((String) indexesAnn.getValue()).split(" ")) {
        indexes.put(index);
      }
    }
    signal.put("indexes", indexes);
    return signal;
  }

  @Override
  public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode,
      SRelation edge, SNode fromNode, long order) {
    assert st.size() > 0;

    if (st.size() == 1) {
      try {
        getOrCreateArray(result, "children").put(st.pop());
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
  public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode,
      SRelation sRelation, SNode fromNode, long order) {

    st.push(createJsonEntry(currNode));

  }

  /**
   * Sets the sentence_left and sentence_right properties of the data object of parent to the
   * min/max of the currNode.
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
        data.put(SENTENCE_RIGHT, Math.max(rightPosC, data.getInt(SENTENCE_RIGHT)));
      } else {
        data.put(SENTENCE_RIGHT, rightPosC);
      }
    } catch (JSONException ex) {
      log.debug("error while setting left and right position for sentences", ex);
    }
  }

  /**
   * Sorts the children of root by the the sentence indizes. Since the sentence indizes are based on
   * the token indizes, some sentences have no sentences indizes, because sometimes token nodes are
   * out of context.
   *
   * A kind of insertion sort would be better than the used mergesort.
   *
   * And it is a pity that the {@link JSONArray} has no interface to sort the underlying
   * {@link Array}.
   *
   */
  private void sortChildren(JSONObject root) throws JSONException {
    JSONArray children = root.getJSONArray("children");
    List<JSONObject> childrenSorted = new ArrayList<JSONObject>(children.length());

    for (int i = 0; i < children.length(); i++) {
      childrenSorted.add(children.getJSONObject(i));
    }

    Collections.sort(childrenSorted, (o1, o2) -> {
      int o1IdxLeft = 0;
      int o1IdxRight = 0;
      int o2IdxLeft = 0;
      int o2IdxRight = 0;
      try {
        o1IdxLeft = ((JSONObject) o1).getJSONObject("data").getInt(SENTENCE_LEFT);
        o1IdxRight = ((JSONObject) o1).getJSONObject("data").getInt(SENTENCE_RIGHT);
        o2IdxLeft = ((JSONObject) o2).getJSONObject("data").getInt(SENTENCE_LEFT);
        o2IdxRight = ((JSONObject) o2).getJSONObject("data").getInt(SENTENCE_RIGHT);
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
    });

    children = new JSONArray(childrenSorted);
    root.put("children", children);
  }

  private String transformSaltToJSON(VisualizerInput visInput) {
    graph = visInput.getSResult().getDocumentGraph();
    List<SNode> rootSNodes = graph.getRoots();
    List<SNode> rstRoots = new ArrayList<SNode>();

    for (SNode sNode : rootSNodes) {
      if (CommonHelper.checkSLayer(namespace, sNode) && !isSignalNode(sNode)) {
        rstRoots.add(sNode);
      }
    }

    if (rootSNodes.size() > 0) {

      // collect all sentence and sort them.
      graph.traverse(rstRoots, GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST, "getSentences",
          new GraphTraverseHandler() {
            @Override
            public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
                SRelation edge, SNode currNode, long order) {

              // token are not needed
              if (currNode instanceof SToken) {
                return false;
              }

              return true;
            }

            @Override
            public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
                SNode currNode, SRelation edge, SNode fromNode, long order) {}

            @Override
            public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
                SNode currNode, SRelation sRelation, SNode fromNode, long order) {
              if (currNode instanceof SStructure && isSegment(currNode)
                  && !isSignalNode(currNode)) {
                boolean added = sentences.add((SStructure) currNode);
                if (!added) {
                  log.warn(
                      "Did not add node {} because another node with the same index already existed",
                      currNode.getId());
                }
              }
            }
          });

      // decorate segments with sentence number
      int i = 1;
      for (SStructure sentence : sentences) {
        sentence.createProcessingAnnotation(SENTENCE_INDEX, SENTENCE_INDEX, Integer.toString(i));
        i++;
      }

      graph.traverse(rstRoots, GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST, "jsonBuild", this);
    } else {
      log.debug("does not find an annotation which matched {}", ANNOTATION_KEY);
      graph.traverse(rstRoots, GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST, "jsonBuild", this);
    }

    return result.toString();
  }
}
