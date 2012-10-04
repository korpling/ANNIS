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
import java.util.Stack;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
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
  final String ANNOTATION_NAME = "cat";
  final String ANNOTATION_VALUE = "group";
  final String ANNOTATION_NAMESPACE = "default_ns";
  private SDocumentGraph graph;

  private String transformSaltToJSON(VisualizerInput visInput)
  {
    graph = visInput.getDocument().getSDocumentGraph();
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

      String path = "/tmp/" + graph.getSName() + ".js";
      FileOutputStream out = new FileOutputStream(path);
      out.write(result.toString().getBytes("UTF-8"));
      out.close();
    }
    catch (Exception ex)
    {
      log.error("writing json failed", ex);
    }
    return result.toString();
  }

  public RSTImpl(VisualizerInput visInput)
  {
    jit = new JITWrapper();
    this.addComponent(jit);

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
      for (SToken tok : token)
      {
        String text = getText(tok);
        sb.append(text);
        log.debug("append: {}", text);
      }
    }

    try
    {
      jsonData.put("id", currNode.getSId());
      jsonData.put("name", token.size() == 0 ? "no token" : sb.toString());
      jsonData.put("data", "{}");
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
      return true;
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

    // check if it is a text data structure
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
}
