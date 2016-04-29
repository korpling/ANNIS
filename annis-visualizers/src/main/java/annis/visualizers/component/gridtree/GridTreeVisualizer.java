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

import static annis.CommonHelper.getSpannedText;
import static annis.model.AnnisConstants.ANNIS_NS;
import static annis.model.AnnisConstants.FEAT_MATCHEDNODE;
import static annis.model.AnnisConstants.FEAT_RELANNIS_NODE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.GraphTraverseHandler;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SGraph.GRAPH_TRAVERSE_TYPE;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;

import com.vaadin.ui.Panel;

import annis.gui.widgets.grid.AnnotationGrid;
import annis.gui.widgets.grid.GridEvent;
import annis.gui.widgets.grid.Row;
import annis.libgui.Helper;
import annis.libgui.VisualizationToggle;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import annis.model.RelannisNodeFeature;
import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 * A grid visualizing hierarchical tree annotations as ordered grid layers.
 *
 * Note that all layers represent the same annotation name at different
 * hierarchical depths, marked level:0,1,2,... etc. on the left
 *
 * Known Bug: the visualizer does not handle crossing edges. Equal annotation
 * names which cover the same range or a subset of nodes and have the same
 * hierarchical depths are not rendered correctly by grid_tree.
 * https://github.com/korpling/ANNIS/issues/14
 *
 * <h2>Mappings:</h2>
 * <ul>
 * <li>Specify the name of the annotation to be visualized in the grid with
 * <b>node_key:name</b>. Note that all grid levels visualize the same annotation
 * name at different hierarchical depths.</li>
 * <li>Specify the name of the base row with the <b>tok_key:name</b>. This is
 * useful, if you want to use a specific annotation layer for instead of the
 * always existing tok layer. E.g. the falko corpora often contain the ZH
 * (Zielhypothesen)-layers.</li>
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmailcom>
 *
 */
@PluginImplementation
public class GridTreeVisualizer extends AbstractVisualizer<Panel> {

    @Override
    public String getShortName() {
        return "grid_tree";
    }

    @Override
    public Panel createComponent(VisualizerInput visInput,
            VisualizationToggle visToggle) {
        return new GridTreePanel(visInput, visToggle);
    }

    private static class GridTreePanel extends Panel {

        private VisualizerInput input;
        private SDocumentGraph graph;

        public GridTreePanel(VisualizerInput visInput, VisualizationToggle visToggle) {

            // nothing to render if no input is there
            if (visInput == null) {
                return;
            }

            // save the input for helper methods
            this.input = visInput;

            // save the graph for convenience access
            graph = input.getSResult().getDocumentGraph();

            // init an empty grid
            AnnotationGrid grid = new AnnotationGrid(input.getId(), getTokKey());

            // set config for escaping html tags
            String escapeHTML = visInput.getMappings().getProperty("escape_html", "true");
            grid.setEscapeHTML(Boolean.parseBoolean(escapeHTML));

            // get all roots for having a start point for the traversal
            List<SNode> roots = graph.getRoots();

            /**
             * This abstract representation is used by the AnnotationGrid for
             * creating the html table at the end. The should be sorted by the
             * row height, which is represented as the a string value of the row
             * integer. The would only work up to 10 rows.
             */
            final Map<String, ArrayList<Row>> table = new TreeMap<String, ArrayList<Row>>();

            /**
             * Get a list of sorted token for retrieving the token index of the
             * most left token and fetch the token index of the first token, so
             * we have can calculate the offset of each token index. Also the
             * token index of the last token is fetched.
             */
            List<SToken> sortedToken = graph.getSortedTokenByText();
            int startIdx = -1;
            int endIdx = -1;

            if (sortedToken != null && sortedToken.get(0) != null) {
                RelannisNodeFeature f = (RelannisNodeFeature) sortedToken.get(0).
                        getFeature(ANNIS_NS, FEAT_RELANNIS_NODE).getValue();
                startIdx = (int) f.getTokenIndex();

                f = (RelannisNodeFeature) sortedToken.get(sortedToken.size() - 1).
                        getFeature(ANNIS_NS, FEAT_RELANNIS_NODE).getValue();
                endIdx = (int) f.getTokenIndex();
            }

            // init the traversal
            GraphTraverseHandler traverse = new Traverse(startIdx, endIdx,
                    getNodeKey(), input.getNamespace(), table);

            // TODO build the grid tree above the token/annotation level
            graph.traverse(roots, GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST,
                    "gridtree", traverse);

            // add the last row, TODO extend to arbitrary nodes not only token level
            ArrayList<Row> baseRows = createBaseRows();

            /**
             * Add the last row. For placing it to the bottom of the table, we
             * need to get the string representation of the last index.
             */
            table.put(getTokKey(), baseRows);

            addCoveredIDs(getTokKey(), table);

            // finally put the table into the rendering class
            grid.setRowsByAnnotation(table);

            grid.setTokenIndexOffset(startIdx);

            // add the annotation grid to the gui
            setContent(grid);

            // add some css formatting
            grid.addStyleName("partitur_table");
            grid.addStyleName(Helper.CORPUS_FONT_FORCE);

        }

        /**
         * Sets the covered ids for gridtree spans.
         *
         * @param baseRowIdx the index of the row from which the salt ids are
         * extracted. Most of the time the index would be "tok".
         * @param table abstract representation of the table which is rendered
         * by {@link AnnotationGrid}
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

        private String getTokKey() {
            return input.getMappings().getProperty("tok_key", "tok");
        }

        private String getNodeKey() {
            return input.getMappings().getProperty("node_key", "cat");
        }

        private boolean hasAnno(SNode n) {
            Set<SAnnotation> annos = n.getAnnotations();
            if (annos != null) {
                for (SAnnotation a : annos) {
                    if (getTokKey().equals(a.getName())) {
                        return true;
                    }
                }
            }

            return false;
        }

        private ArrayList<Row> createBaseRows() {
            ArrayList<Row> baseRows = new ArrayList<Row>();
            Row baseRow = new Row();
            baseRows.add(baseRow);

            if (getTokKey().equals("tok")) {

                for (SToken t : graph.getSortedTokenByText()) {
                    RelannisNodeFeature f = (RelannisNodeFeature) t.getFeature(
                            ANNIS_NS, FEAT_RELANNIS_NODE).getValue();

                    int idx = (int) f.getTokenIndex();
                    baseRow.
                            addEvent(new GridEvent(t.getId(), idx, idx, getSpannedText(t)));
                }
            } else {

                List<SSpan> sSpans = graph.getSpans();
                if (sSpans != null) {
                    for (SNode n : sSpans) {
                        if (hasAnno(n)) {
                            RelannisNodeFeature f = (RelannisNodeFeature) n.getFeature(
                                    ANNIS_NS, FEAT_RELANNIS_NODE).getValue();

                            int leftIdx = (int) f.getLeftToken();
                            int rightIdx = (int) f.getRightToken();
                            baseRow.
                                    addEvent(new GridEvent(n.getId(), leftIdx, rightIdx,
                                    getAnnoText(n)));
                        }
                    }
                }
            }

            return baseRows;
        }

        private String getAnnoText(SNode n) {

            Set<SAnnotation> annos = n.getAnnotations();
            if (annos != null) {
                for (SAnnotation a : annos) {
                    if (getTokKey().equals(a.getName())) {
                        return a.getValue_STEXT();
                    }
                }
            }

            return "";
        }
    }

    private static class Traverse implements GraphTraverseHandler {

        /**
         * Tracks the depth of the traversal. Steps are counted, when the node
         * containes the defined annotation key. This value is later used for
         * the row index in the {@link AnnotationGrid}.
         */
        int depth = 0;
        // the token index of the last token
        int endIdx;
        // the token index of the most left token of the result set
        int startIdx;
        // defines the annotation key. Only nodes with that key are filtered.
        String annotationKey;
        Map<String, ArrayList<Row>> table;
        // tracks all nodes which was visited.
        Set<SNode> visited = new HashSet<SNode>();

        /**
         * Init a traverse handler for building a tree of topological fields.
         *
         * @param startIdx the most left token index
         * @param endIdx the most right index
         * @param nodeKey the annotation key. Only nodes which contain this key
         * will be taken into account
         * @param namespace the namespace which triggered this visualization
         * @param table the abstract representation of the table
         */
        private Traverse(int startIdx, int endIdx, String nodeKey, String namespace,
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
            SAnnotation anno = getAnno(currNode);

            if (anno != null) {

                String rIdx = String.valueOf(depth);

                if (!table.containsKey(rIdx)) {
                    ArrayList<Row> rows = new ArrayList<Row>();
                    rows.add(new Row());
                    table.put(rIdx, rows);
                }

                RelannisNodeFeature f = (RelannisNodeFeature) currNode.
                        getFeature(ANNIS_NS, FEAT_RELANNIS_NODE).getValue();

                // cut off the most left and right indexes
                int leftIdx = Math.max(((int) f.getLeftToken()), startIdx);
                int rightIdx = Math.min(((int) f.getRightToken()), endIdx);

                GridEvent e = new GridEvent(currNode.getId(), leftIdx, rightIdx, anno.
                        getValue_STEXT());

                // add match id
                SFeature featMatched = currNode.getFeature(ANNIS_NS, FEAT_MATCHEDNODE);
                Long match = featMatched == null ? null : featMatched.
                        getValue_SNUMERIC();
                e.setMatch(match);

                // set tooltip
                e.setTooltip(anno.getQName() + "=\"" + e.getValue() + "\"");

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

        private SAnnotation getAnno(SNode n) {
            Set<SAnnotation> annos = n.getAnnotations();
            if (annos != null) {
                for (SAnnotation a : annos) {
                    if (annotationKey.equals(a.getName())) {
                        return a;
                    }
                }
            }

            return null;
        }
    }
}
