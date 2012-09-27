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
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Graph;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.GraphTraverseHandler;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Node;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import java.util.LinkedList;
import java.util.Stack;
import org.eclipse.emf.common.util.EList;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class RSTImpl extends Panel implements GraphTraverseHandler
{

  String testJSON = "{"
    + "\"id\": \"node1\","
    + "\"name\" : \"node1\","
    + "\"data\": {},"
    + "\"children\": [{"
    + "\"id\": \"node2\","
    + "\"name\": \"node2\","
    + "\"data\": {},"
    + "\"children\":[]"
    + "},{"
    + "\"id\": \"node3\","
    + "\"name\": \"node3\","
    + "\"data\": {},"
    + "\"children\":[]"
    + "}"
    + "]"
    + "}";
  private final JITWrapper jit;
  private final Logger log = LoggerFactory.getLogger(RSTImpl.class);
  private Stack<JSONObject> st = new Stack<JSONObject>();
  // result of transform operation salt -> json
  private JSONObject result;

  private String transformSaltToJSON(VisualizerInput visInput)
  {
    Graph graph = visInput.getDocument().getGraph();
    EList<Node> nodes = graph.getRoots();

    graph.traverse(nodes, GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST,
      "generateJSON", this);

    
    log.info(result.toString());
    return result.toString();
  }

  public RSTImpl(VisualizerInput visInput)
  {
    jit = new JITWrapper();
    this.addComponent(jit);

    jit.setVisData(transformSaltToJSON(visInput));
    jit.requestRepaint();

  }

  @Override
  public void nodeReached(GRAPH_TRAVERSE_TYPE g, String string, Node current, Edge edge, Node fromNode, long l)
  {
    st.push(createJsonEntry(current));
    log.info("pushed {}", st.toString());
  }

  @Override
  public void nodeLeft(GRAPH_TRAVERSE_TYPE g, String string, Node current, Edge edge, Node fromNode, long l)
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
    
    log.info("poped {}", st.toString());
  }

  @Override
  public boolean checkConstraint(GRAPH_TRAVERSE_TYPE g, String string, Edge edge, Node node, long l)
  {
    // all the graph should be traverse.
    return true;
  }

  private JSONObject createJsonEntry(Node current)
  {
    JSONObject jsonData = new JSONObject();

    try
    {
      jsonData.put("id", current.getId());
      jsonData.put("name", current.getId());
      jsonData.put("data", "{}");
    }
    catch (JSONException ex)
    {
      log.error("problems create entry for {}", current, ex);
    }

    return jsonData;
  }

  private JSONObject appendChild(JSONObject root, JSONObject node)
  {
    try
    {
      root.put("children", node);
    }
    catch (JSONException ex)
    {
      log.error("cannot append {}", node, ex);
    }

    return node;
  }
}
