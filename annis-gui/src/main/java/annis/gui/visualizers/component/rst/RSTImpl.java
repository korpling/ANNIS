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
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.GRAPH_TRAVERSE_TYPE;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SGraphTraverseHandler;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import java.util.Stack;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
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

  private String transformSaltToJSON(VisualizerInput visInput)
  {
    SDocumentGraph graph = visInput.getDocument().getSDocumentGraph();
    EList<SNode> nodes = graph.getSRoots();
    EList<SNode> rootSNodes = new BasicEList<SNode>();
    final String ANNOTATION = "cat";

    if (nodes != null)
    {
      for (SNode node : nodes)
      {
        for (SAnnotation anno : node.getSAnnotations())
        {
          log.debug("anno name {}, anno value {}", anno.getName(), anno.getValue());
          
          if (ANNOTATION.equals(anno.getName()))
          {
            rootSNodes.add(node);
            log.debug("find root {} with {}", anno, ANNOTATION);
            break;
          }
        }
      }
    }
    
    if (rootSNodes.size() > 0)
    {
      graph.traverse(rootSNodes, GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST, "jsonBuild", this);
    }
    else
    {
      log.debug("does not find an annotation which matched {}", ANNOTATION);
      graph.traverse(nodes, GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST, "jsonBuild", this);
    }

    log.debug("result json string: {}", result);
    return result.toString();
  }

  public RSTImpl(VisualizerInput visInput)
  {
    jit = new JITWrapper();
    this.addComponent(jit);

    jit.setVisData(transformSaltToJSON(visInput));
    jit.requestRepaint();

  }

  private JSONObject createJsonEntry(SNode current)
  {
    JSONObject jsonData = new JSONObject();

    try
    {
      jsonData.put("id", current.getSName());
      jsonData.put("name", current.getSName());
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
  public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SRelation edge, SNode currNode, long order)
  {
    return true;
  }
}
