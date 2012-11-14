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
import annis.model.AnnisConstants;
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
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SFeature;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SGraphTraverseHandler;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SProcessingAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import java.io.FileOutputStream;
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

  // implements the AbstractComponent and talks to the VJITWrapperWidget
  private final JITWrapper jit;

  // traversing stack for build the json tree
  private Stack<JSONObject> st = new Stack<JSONObject>();

  // result of transform operation salt -> json
  private JSONObject result;

  // Default annotation namespace
  private final String ANNOTATION_NAMESPACE = "default_ns";

  // filter root nodes with this annotation key
  private final String ANNOTATION_KEY = "cat";

  // used for filtering dominating edges and detecting pointing relations
  final String[] ANNOTATION_VALUES =
  {
    "span", "multinuc"
  };

  /**
   * Used for filtering edges which should not be visualized. The target of the
   * edge is added to the json tree, but the sources contains an entry
   * "invisibleRelations:target_id".
   */
  private String INVISIBLE_RELATION = "type";

  // sType for the pointing relation
  private final String POINTING_RELATION = "edge";

  /**
   * Create a unique id, for every RSTImpl instance, for building an unique html
   * id, in the DOM.
   */
  private static int count = 0;

  // unique id for every instance of RSTImpl
  private final String visId;

  // result graph
  private SDocumentGraph graph;

  // all marked tokens of the result graph
  private Map<SNode, Long> markedAndCovered;

  // namespace for SProcessingAnnotation sentence index
  static private final String SENTENCE_INDEX = "sentence_index";

  static private final String SENTENCE_LEFT = "sentence_left";

  static private final String SENTENCE_RIGHT = "sentence_right";

  /**
   * Sorted list of all SStructures which overlapped a sentence. It's used for
   * mapping the sentence to a number by the order of the SStructures in the
   * list.
   */
  private TreeSet<SStructure> sentences = new TreeSet<SStructure>(
    new Comparator<SStructure>()
    {
      private int getStartPosition(SStructure s)
      {
        EList<Edge> out = s.getSGraph().getOutEdges(s.getSId());

        for (Edge e : out)
        {
          if (e instanceof SRelation
            && ((SRelation) e).getTarget() instanceof SToken)
          {
            SToken tok = ((SToken) ((SRelation) e).getTarget());
            SFeature sf = tok.getSFeature(
              AnnisConstants.ANNIS_NS + "::" + AnnisConstants.FEAT_LEFTTOKEN);
            return Integer.parseInt(sf.getSValueSTEXT());
          }
        }

        SFeature sf = s.getSFeature(
          AnnisConstants.ANNIS_NS + "::" + AnnisConstants.FEAT_LEFTTOKEN);
        return Integer.parseInt(sf.getSValueSTEXT());
      }

      @Override
      public int compare(SStructure t1, SStructure t2)
      {
        int t1Idx = getStartPosition(t1);
        int t2Idx = getStartPosition(t2);

        if (t1Idx < t2Idx)
        {
          return -1;
        }

        if (t1Idx == t2Idx)
        {
          return 0;
        }
        else
        {
          return 1;
        }
      }
    });

  private final Logger log = LoggerFactory.getLogger(RSTImpl.class);

  public RSTImpl(VisualizerInput visInput)
  {

    // build id and increase count for every instance
    visId = "rst_" + count;
    count++;

    jit = new JITWrapper();
    this.addComponent(jit);

    // send the json to the widget
    jit.setVisData(transformSaltToJSON(visInput));
    jit.requestRepaint();
  }

  private String transformSaltToJSON(VisualizerInput visInput)
  {
    graph = visInput.getDocument().getSDocumentGraph();
    markedAndCovered = visInput.getMarkedAndCovered();
    EList<SNode> nodes = graph.getSRoots();
    EList<SNode> rootSNodes = new BasicEList<SNode>();


    Salt2DOT s2d = new Salt2DOT();
    s2d.salt2Dot(graph, URI.createFileURI(
      "/tmp/graph_" + graph.getSName() + ".dot"));

    if (nodes != null)
    {
      for (SNode node : nodes)
      {
        for (SAnnotation anno : node.getSAnnotations())
        {
          log.debug("anno name {}, anno value {}", anno.getName(), anno.
            getValue());

          if (ANNOTATION_KEY.equals(anno.getName()))
          {
            rootSNodes.add(node);
            log.debug("find root {} with {}", anno, ANNOTATION_KEY);
            break;
          }
        }
      }
    }

    if (rootSNodes.size() > 0)
    {
      graph.traverse(rootSNodes, GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST,
        "getSentences", new SGraphTraverseHandler()
      {
        @Override
        public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType,
          String traversalId, SNode currNode, SRelation sRelation,
          SNode fromNode, long order)
        {
        }

        @Override
        public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType,
          String traversalId, SNode currNode, SRelation edge, SNode fromNode,
          long order)
        {
          if (currNode instanceof SStructure && isSegment(currNode))
          {
            sentences.add((SStructure) currNode);
          }
        }

        @Override
        public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType,
          String traversalId, SRelation edge, SNode currNode, long order)
        {

          // entry case
          if (edge == null)
          {
            return true;
          }

          // token are not needed
          if (currNode instanceof SToken)
          {
            return false;
          }

          return checkIncomingEdge(edge);
        }
      });

      //decorate segments with sentence number
      int i = 0;
      for (SStructure sentence : sentences)
      {
        sentence.createSProcessingAnnotation(
          SENTENCE_INDEX, SENTENCE_INDEX, Integer.toString(i));
        i++;
      }

      graph.traverse(rootSNodes, GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST,
        "jsonBuild", this);
    }
    else
    {
      log.debug("does not find an annotation which matched {}",
        ANNOTATION_KEY);
      graph.traverse(nodes, GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST,
        "jsonBuild", this);
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

    return result.toString();
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
      jsonData.put("id", getUniStrId(currNode));
      jsonData.put("name", currNode.getSName());

      /**
       * additional data oject for edge labels and rendering sentences
       */
      JSONObject data = new JSONObject();
      JSONArray edgesJSON = getIncomingEdgeTypeAnnotation(currNode);
      JSONArray invisRel = getInvisibleRelatedNodes(currNode);

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

      if (invisRel != null)
      {
        data.put("invisibleRelations", invisRel);
      }

      if (currNode instanceof SStructure && isSegment(currNode))
      {

        SProcessingAnnotation sentence_idx = currNode.
          getSProcessingAnnotation(SENTENCE_INDEX + "::" + SENTENCE_INDEX);
        int index = sentence_idx == null ? -1 : Integer.parseInt(sentence_idx.
          getValueString());

        data.put(SENTENCE_LEFT, index);
        data.put(SENTENCE_RIGHT, index);
      }

      jsonData.put("data", data);
    }
    catch (JSONException ex)
    {
      log.error("problems create entry for {}", currNode, ex);
    }

    return jsonData;
  }

  private JSONObject appendChild(JSONObject root, JSONObject node,
    SNode currSnode)
  {
    try
    {
      boolean isAppendedToParent = false;
      EList<Edge> in = currSnode.getSGraph().getInEdges(currSnode.getSId());
      if (in != null)
      {
        SRelation sEdge;

        for (Edge e : in)
        {
          if (e instanceof SRelation
            && ((SRelation) e).getSource() instanceof SNode
            && isSegment(((SNode) ((SRelation) e).getSource()))
            && !isSegment(currSnode))
          {
            JSONObject tmp = st.pop();

            st.peek().append("children", node);
            setSentenceSpan(node, st.peek());
            sortChildren(st.peek());
            st.peek().getJSONObject("data").append("invisibleRelations",
              getUniStrId(currSnode));

            st.push(tmp);
            isAppendedToParent = true;
            break;
          }
        }
      }

      if (!isAppendedToParent)
      {
        root.append("children", node);
        setSentenceSpan(node, root);
        sortChildren(root);
      }

    }
    catch (JSONException ex)
    {
      log.error("cannot append {}", node, ex);
    }

    return node;
  }

  @Override
  public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType,
    String traversalId,
    SNode currNode, SRelation sRelation, SNode fromNode, long order)
  {
    st.push(createJsonEntry(currNode));
  }

  @Override
  public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
    SNode currNode, SRelation edge, SNode fromNode, long order)
  {
    assert st.size() > 0;

    if (st.size() == 1)
    {
      result = st.pop();
    }
    else
    {
      JSONObject jsonNode = st.pop();
      appendChild(st.peek(), jsonNode, currNode);
    }
  }

  @Override
  public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType,
    String traversalId, SRelation incomingEdge, SNode currNode, long order)
  {


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

    return checkIncomingEdge(incomingEdge);
  }

  /**
   * Gets the overlapping token as string from a node, which are direct
   * dominated by this node.
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

  private JSONArray getInvisibleRelatedNodes(SNode node)
  {
    JSONArray nodeIds = new JSONArray();
    EList<Edge> out = node.getSGraph().getOutEdges(node.getSId());

    /**
     * notice the node which are targets of edges which contains an annotation
     * with the key "type"
     */
    if (out != null)
    {
      for (Edge edge : out)
      {
        if (!(edge instanceof SRelation))
        {
          continue;
        }

        if (hasAnnoKey(((SRelation) edge), INVISIBLE_RELATION)
          && ((SRelation) edge).getTarget() instanceof SNode)
        {
          nodeIds.put(getUniStrId(((SNode) ((SRelation) edge).getTarget())));
        }
      }

    }
    return nodeIds;

  }

  private JSONArray getIncomingEdgeTypeAnnotation(SNode node) throws JSONException
  {
    EList<Edge> in = node.getSGraph().getInEdges(node.getId());
    EList<String> sTypes;
    EList<SAnnotation> annos;
    JSONArray edgeData = new JSONArray();


    // check if there is a pointing relation
    if (in != null)
    {
      for (Edge edge : in)
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
            jsonEdge.put("from", getUniStrId(node));

            if (((SRelation) edge).getTarget() instanceof SNode)
            {
              jsonEdge.put("to",
                getUniStrId((SNode) ((SRelation) edge).getSource()));
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
    }

    return edgeData;
  }

  /**
   * Build a unique HTML id.
   */
  private String getUniStrId(SNode node)
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

  /**
   * Returns false, if the incoming edge does not contain any sType or if it has
   * an sType "edge" and not the annotation "span" and "multinuc".
   */
  private boolean checkIncomingEdge(SRelation incomingEdge)
  {

    EList<String> sTypes;

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
        && POINTING_RELATION.equals(sTypes.get(0))
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
   * Checks, if there exists an SRelation which targets a SToken.
   */
  private boolean isSegment(SNode currNode)
  {

    EList<Edge> edges = currNode.getSGraph().getOutEdges(currNode.getSId());

    if (edges != null && edges.size() > 0)
    {
      for (Edge edge : edges)
      {
        if (edge.getTarget() instanceof SToken)
        {
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
  private void setSentenceSpan(JSONObject cNode, JSONObject parent)
  {
    try
    {
      JSONObject data = cNode.getJSONObject("data");

      int leftPosC = data.getInt(SENTENCE_LEFT);
      int rightPosC = data.getInt(SENTENCE_RIGHT);

      data = parent.getJSONObject("data");

      if (data.has(SENTENCE_LEFT))
      {
        data.put(SENTENCE_LEFT, Math.min(leftPosC, data.getInt(SENTENCE_LEFT)));
      }
      else
      {
        data.put(SENTENCE_LEFT, leftPosC);
      }

      if (data.has(SENTENCE_RIGHT))
      {
        data.put(SENTENCE_RIGHT,
          Math.max(rightPosC, data.getInt(SENTENCE_RIGHT)));
      }
      else
      {
        data.put(SENTENCE_RIGHT, rightPosC);
      }
    }
    catch (JSONException ex)
    {
      log.debug("error while setting left and right position for sentences", ex);
    }
  }

  /**
   * Sorts the children of root by the the sentence indizes. Since the sentence
   * indizes are calculated with the token indizes, some sentences have no
   * sentences indizes, because sometimes token nodes are out of context.
   *
   * A kind of insertion sort would be better than the used mergesort.
   *
   * And it is a pity that the {@link JSONArray} has no interface to sort the
   * underlying {@link Array}.
   *
   */
  private void sortChildren(JSONObject root) throws JSONException
  {
    JSONArray children = root.getJSONArray("children");
    List<JSONObject> childrenSorted = new ArrayList<JSONObject>(children.
      length());

    for (int i = 0; i < children.length(); i++)
    {
      childrenSorted.add(children.getJSONObject(i));
    }

    Collections.sort(childrenSorted, new Comparator<Object>()
    {
      @Override
      public int compare(Object o1, Object o2)
      {
        int o1IdxLeft = 0;
        int o1IdxRight = 0;
        int o2IdxLeft = 0;
        int o2IdxRight = 0;
        try
        {
          o1IdxLeft = ((JSONObject) o1).getJSONObject("data").getInt(
            SENTENCE_LEFT);
          o1IdxRight = ((JSONObject) o1).getJSONObject("data").getInt(
            SENTENCE_RIGHT);
          o2IdxLeft = ((JSONObject) o2).getJSONObject("data").getInt(
            SENTENCE_LEFT);
          o2IdxRight = ((JSONObject) o2).getJSONObject("data").getInt(
            SENTENCE_RIGHT);
        }
        catch (JSONException ex)
        {
          log.error("Could not compare sentence indizes.", ex);
        }

        if (o1IdxLeft + o1IdxRight > o2IdxLeft + o2IdxRight)
        {
          return 1;
        }

        if (o1IdxLeft + o1IdxRight == o2IdxLeft + o2IdxRight)
        {
          return 0;
        }
        else
        {
          return -1;
        }
      }
    });

    children = new JSONArray(childrenSorted);
    root.put("children", children);
  }

  private boolean hasAnnoKey(SRelation edge, String type)
  {
    EList<SAnnotation> annos = edge.getSAnnotations();
    for (SAnnotation anno : annos)
    {
      if (edge.getSTypes() != null && type.equals(anno.getSName()))
      {
        for (String annoValue : ANNOTATION_VALUES)
        {
          if (annoValue.equals(anno.getSValueSTEXT()))
          {
            return true;
          }
        }
      }
    }

    return false;
  }
}
