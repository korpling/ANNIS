/*
 * Copyright 2013 SFB 632.
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
package annis.visualizers.component.gridtree;

import annis.gui.widgets.grid.AnnotationGrid;
import annis.gui.widgets.grid.GridEvent;
import annis.gui.widgets.grid.Row;
import annis.libgui.VisualizationToggle;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import annis.model.RelannisNodeFeature;
import com.vaadin.ui.Panel;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.GRAPH_TRAVERSE_TYPE;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SGraphTraverseHandler;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.eclipse.emf.common.util.EList;
import static annis.model.AnnisConstants.*;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import java.util.TreeMap;
import static annis.CommonHelper.*;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SFeature;
import java.util.Map.Entry;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
@PluginImplementation
public class GridTreeVisualizer extends AbstractVisualizer<Panel> {

  @Override
  public String getShortName() {
    return "grid_tree2";
  }

  @Override
  public Panel createComponent(VisualizerInput visInput,
          VisualizationToggle visToggle) {
    return new GridTreePanel(visInput, visToggle);
  }

  private class GridTreePanel extends Panel {

    private VisualizerInput input;

    public GridTreePanel(VisualizerInput visInput, VisualizationToggle visToggle) {

      // nothing to render if no input is there
      if (visInput == null) {
        return;
      }

      // save the input for helper methods
      this.input = visInput;

      // save the graph for convenience access
      SDocumentGraph graph = input.getSResult().getSDocumentGraph();

      // init an empty grid
      AnnotationGrid grid = new AnnotationGrid(input.getId());

      // get all roots for having a start point for the traversal
      EList<SNode> roots = graph.getSRoots();

      /**
       * This abstract representation is used by the AnnotationGrid for creating
       * the html table at the end. The should be sorted by the row height,
       * which is represented as the a string value of the row integer. The
       * would only work up to 10 rows.
       */
      final Map<String, ArrayList<Row>> table = new TreeMap<String, ArrayList<Row>>();


      /**
       * Get a list of sorted token for retrieving the token index of the most
       * left token and fetch the token index of the first token, so we have can
       * calculate the offset of each token index. Also the token index of the
       * last token is fetched.
       */
      EList<SToken> sortedToken = graph.getSortedSTokenByText();
      int startIdx = -1;
      int endIdx = -1;

      if (sortedToken != null && sortedToken.get(0) != null) {
        RelannisNodeFeature f = (RelannisNodeFeature) sortedToken.get(0).
                getSFeature(ANNIS_NS, FEAT_RELANNIS_NODE).getValue();
        startIdx = (int) f.getTokenIndex();

        f = (RelannisNodeFeature) sortedToken.get(sortedToken.size() - 1).
                getSFeature(ANNIS_NS, FEAT_RELANNIS_NODE).getValue();
        endIdx = (int) f.getTokenIndex();
      }

      // init the traversal
      SGraphTraverseHandler traverse = new Traverse(startIdx, endIdx,
              getNodeKey(), table);

      // TODO build the grid tree above the token/annotation level
      graph.traverse(roots, GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST,
              "gridtree", traverse);

      // add the last row, TODO extend to arbitrary nodes not only token level
      ArrayList<Row> baseRows = new ArrayList<Row>();
      Row baseRow = new Row();
      baseRows.add(baseRow);
      for (SToken t : sortedToken) {
        RelannisNodeFeature f = (RelannisNodeFeature) t.getSFeature(
                ANNIS_NS, FEAT_RELANNIS_NODE).getValue();

        int idx = (int) f.getTokenIndex();
        baseRow.addEvent(new GridEvent(t.getId(), idx, idx, getSpannedText(t)));
      }

      /**
       * Add the last row. For placing it to the bottom of the table, we need to
       * get the string representation of the last index.
       */
      table.put("tok", baseRows);

      addCoveredIDs("tok", table);

      // finally put the table into the rendering class
      grid.setRowsByAnnotation(table);

      grid.setTokenIndexOffset(startIdx);

      // add the annotation grid to the gui
      setContent(grid);

      // add some css formatting
      grid.addStyleName("partitur_table");
      grid.addStyleName("corpus-font-force");

    }

    /**
     * Sets the covered ids for gridtree spans.
     *
     * @param baseRowIdx the index of the row from which the salt ids are
     * extracted. Most of the time the index would be "tok".
     * @param table abstract representation of the table which is rendered by
     * {@link AnnotationGrid}
     */
    private void addCoveredIDs(String baseRowIdx,
            Map<String, ArrayList<Row>> table) {

      if (!table.containsKey(baseRowIdx)) {
        throw new IllegalArgumentException("table index does not exist");
      }

      // get the base row. There should be only one
      Row baseRow = table.get(baseRowIdx).get(0);

      // iterate over all rows, except the row with the base index
      for (Entry<String, ArrayList<Row>> e : table.entrySet()) {

        // skip the base row
        if (e.getKey().equals(baseRowIdx)) {
          continue;
        }

        // find all base events which have a token index range with the span event
        Row row = table.get(e.getKey()).get(0);
        for (GridEvent event : row.getEvents()) {
          int leftIdx = event.getLeft();
          int rightIdx = event.getRight();

          for (GridEvent baseEvent : baseRow.getEvents()) {
            if (leftIdx <= baseEvent.getLeft()
                    && baseEvent.getRight() <= rightIdx) {
              event.getCoveredIDs().add(baseEvent.getId());
            }
          }
        }
      }
    }

    private String getNodeKey() {
      return input.getMappings().getProperty("node_key", "cat");
    }
  }

  private class Traverse implements SGraphTraverseHandler {

    /**
     * Tracks the depth of the traversal. Steps are counted, when the node
     * containes the defined annotation key. This value is later used for the
     * row index in the {@link AnnotationGrid}.
     */
    int depth = 0;

    // the token index of the last token
    int endIdx;

    // the token index of the most left token of the result set
    int startIdx;

    // defines the annotation key. Only nodes with that key are filtered.
    String annotationKey;

    Map<String, ArrayList<Row>> table;

    Set<SNode> visited = new HashSet<SNode>();

    /**
     * Init a traverse handler for building a tree of topological fields.
     *
     * @param startIdx the most left token index
     * @param endIdx the most right index
     * @param nodeKey the annotation key. Only nodes which contain this key will
     * be taken into account
     * @param table the abstract representation of the table
     */
    private Traverse(int startIdx, int endIdx, String nodeKey,
            Map<String, ArrayList<Row>> table) {
      this.startIdx = startIdx;
      this.endIdx = endIdx;
      this.annotationKey = nodeKey;
      this.table = table;
    }

    @Override
    public void nodeReached(GRAPH_TRAVERSE_TYPE g, String string,
            SNode currNode, SRelation edge, SNode fromNode, long l) {

      // retrieve the annotation by the node key
      String anno = getAnno(currNode);

      if (!anno.equals("")) {

        String rIdx = String.valueOf(depth);

        if (!table.containsKey(rIdx)) {
          ArrayList<Row> rows = new ArrayList<Row>();
          rows.add(new Row());
          table.put(rIdx, rows);
        }

        RelannisNodeFeature f = (RelannisNodeFeature) currNode.
                getSFeature(ANNIS_NS, FEAT_RELANNIS_NODE).getValue();

        // cut off the most left and right indexes
        int leftIdx = Math.max(((int) f.getLeftToken()), startIdx);
        int rightIdx = Math.min(((int) f.getRightToken()), endIdx);

        GridEvent e = new GridEvent(currNode.getId(), leftIdx, rightIdx, anno);

        // add match id
        SFeature featMatched = currNode.getSFeature(ANNIS_NS, FEAT_MATCHEDNODE);
        Long match = featMatched == null ? null : featMatched.
                getSValueSNUMERIC();
        e.setMatch(match);

        // always only one row for a gridtree
        table.get(rIdx).get(0).addEvent(e);

        // mark as visited
        visited.add(currNode);

        // increase the depth of the depth tree
        depth++;
      }
    }

    @Override
    public void nodeLeft(GRAPH_TRAVERSE_TYPE g, String string,
            SNode currNode, SRelation edge, SNode fromNode, long l) {
      assert depth >= 0;
      if (visited.contains(currNode)) {
        visited.remove(currNode);
        depth--;
      }
    }

    @Override
    public boolean checkConstraint(GRAPH_TRAVERSE_TYPE g, String string,
            SRelation sr, SNode snode, long l) {
      return true;
    }

    private String getAnno(SNode n) {
      EList<SAnnotation> annos = n.getSAnnotations();
      if (annos != null) {
        for (SAnnotation a : annos) {
          if (annotationKey.equals(a.getSName())) {
            return a.getSValueSTEXT();
          }
        }
      }

      return "";
    }
  }
}
