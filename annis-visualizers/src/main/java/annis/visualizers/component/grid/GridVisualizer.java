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
package annis.visualizers.component.grid;

import annis.CommonHelper;
import annis.libgui.VisualizationToggle;
import annis.libgui.media.MediaController;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import annis.gui.widgets.grid.AnnotationGrid;
import annis.gui.widgets.grid.GridEvent;
import annis.gui.widgets.grid.Row;
import annis.libgui.media.PDFController;
import static annis.model.AnnisConstants.*;
import annis.model.RelannisNodeFeature;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Edge;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SFeature;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.eclipse.emf.common.util.EList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Visualizes annotations of a spans.
 *
 *
 * Mappings: <br/>
 * <ul>
 * <li>
 * It is possible to specify the order of annotation layers in each grid. Use
 * <b>annos: anno_name1, anno_name2, anno_name3</b> to specify the order or
 * annotation layers. If <b>anno:</b> is used, additional annotation layers not
 * present in the list will not be visualized. If mappings is left empty, layers
 * will be ordered alphabetically.
 * </li>
 * <li>
 * <b>tok_anno:true</b> can be used to also display the annotations of the
 * token.
 * </li>
 * <li>
 * <b>hide_tok:true</b> switches the line with the token value off.
 * </li>
 * </ul>
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@PluginImplementation
public class GridVisualizer extends AbstractVisualizer<GridVisualizer.GridVisualizerComponent> {

    private static final Logger log = LoggerFactory.
            getLogger(GridVisualizer.class);

    @Override
    public String getShortName() {
        return "grid";
    }

    @Override
    public GridVisualizerComponent createComponent(VisualizerInput visInput,
            VisualizationToggle visToggle) {
        MediaController mediaController = VaadinSession.getCurrent().getAttribute(
                MediaController.class);
        PDFController pdfController = VaadinSession.getCurrent().getAttribute(
                PDFController.class);
        GridVisualizerComponent component = null;
        try {
            component = new GridVisualizerComponent(visInput,
                    mediaController, pdfController);
        } catch (Exception ex) {
            log.error("create {} failed", GridVisualizerComponent.class.getName(), ex);
        }
        return component;
    }

    public static class GridVisualizerComponent extends Panel {

        private static final org.slf4j.Logger log = LoggerFactory.getLogger(
                GridVisualizerComponent.class);
        public static final String MAPPING_ANNOS_KEY = "annos";
        public static final String MAPPING_ANNO_REGEX_KEY = "anno_regex";
        public static final String MAPPING_HIDE_TOK_KEY = "hide_tok";
        public static final String MAPPING_TOK_ANNOS_KEY = "tok_anno";
        private AnnotationGrid grid;
        private transient VisualizerInput input;
        private VerticalLayout layout;

        public enum ElementType {

            begin,
            end,
            middle,
            single,
            noEvent
        }

        public GridVisualizerComponent(VisualizerInput input,
                MediaController mediaController, PDFController pdfController) {
            this.input = input;

            setWidth("100%");
            setHeight("-1");

            layout = new VerticalLayout();
            setContent(layout);
            layout.setSizeUndefined();
            addStyleName(ChameleonTheme.PANEL_BORDERLESS);

            EList<STextualDS> texts = input.getDocument().getSDocumentGraph().
                    getSTextualDSs();

            if (texts != null && texts.size() > 0) {
                if (CommonHelper.containsRTLText(texts.get(0).getSText())) {
                    addStyleName("rtl");
                }
            }

            if (input != null) {
                String resultID = input.getId();

                grid = new AnnotationGrid(mediaController, pdfController, resultID);
                grid.addStyleName("partitur_table");
                grid.addStyleName("corpus-font-force");
                grid.setEscapeHTML(Boolean.parseBoolean(input.getMappings().getProperty("escape_html", "true")));
                layout.addComponent(grid);

                SDocumentGraph graph = input.getDocument().getSDocumentGraph();

                boolean showTokenAnnotations =
                        Boolean.parseBoolean(input.getMappings().getProperty(MAPPING_TOK_ANNOS_KEY));

                List<String> annos = EventExtractor.computeDisplayAnnotations(input,
                        SSpan.class);
                if (showTokenAnnotations) {
                    List<String> tokenAnnos = EventExtractor.computeDisplayAnnotations(
                            input, SToken.class);
                    annos.addAll(tokenAnnos);
                }

                EList<SToken> tokens = graph.getSortedSTokenByText();

                RelannisNodeFeature featTokStart =
                        (RelannisNodeFeature) tokens.get(0).getSFeature(ANNIS_NS, FEAT_RELANNIS_NODE).getValue();
                long startIndex = featTokStart.getTokenIndex();

                RelannisNodeFeature featTokEnd =
                        (RelannisNodeFeature) tokens.get(tokens.size() - 1).getSFeature(ANNIS_NS, FEAT_RELANNIS_NODE).getValue();
                long endIndex = featTokEnd.getTokenIndex();

                LinkedHashMap<String, ArrayList<Row>> rowsByAnnotation =
                        EventExtractor.parseSalt(input, showTokenAnnotations, annos,
                        (int) startIndex,
                        (int) endIndex, pdfController);

                // we will only add tokens of one texts which is mentioned by any
                // included annotation.
                Set<String> validTextIDs = new HashSet<String>();
                Iterator<ArrayList<Row>> itAllRows = rowsByAnnotation.values().
                        iterator();
                while (itAllRows.hasNext()) {
                    ArrayList<Row> rowsForAnnotation = itAllRows.next();
                    for (Row r : rowsForAnnotation) {
                        validTextIDs.addAll(r.getTextIDs());
                    }
                }
                
                /**
                 * we want to show all token if no valid text was found and
                 * we have only one text and the first one if there are more
                 * than one text.
                 */
                EList<STextualDS> allTexts = graph.getSTextualDSs();
                if (validTextIDs.isEmpty() && allTexts != null &&
                        (allTexts.size() == 1 || allTexts.size() == 2)) {
                    validTextIDs.add(allTexts.get(0).getSId());
                }

                int tokenOffsetForText = -1;

                // add tokens as row
                Row tokenRow = new Row();
                for (SToken t : tokens) {
                    // get the Salt ID of the STextualDS of this token
                    String tokenTextID = null;
                    EList<Edge> tokenOutEdges = graph.getOutEdges(t.getSId());
                    if (tokenOutEdges != null) {
                        for (Edge tokEdge : tokenOutEdges) {
                            if (tokEdge instanceof STextualRelation) {
                                tokenTextID = ((STextualRelation) tokEdge).getSTextualDS().
                                        getSId();
                                break;
                            }
                        }
                    }

                    // only add token if text ID matches the valid one
                    if (tokenTextID != null && validTextIDs.contains(tokenTextID)) {
                        RelannisNodeFeature feat =
                                (RelannisNodeFeature) t.getSFeature(ANNIS_NS, FEAT_RELANNIS_NODE).getValue();
                        long idx = feat.getTokenIndex() - startIndex;

                        if (tokenOffsetForText < 0) {
                            // set the token offset by assuming the first idx must be zero
                            tokenOffsetForText = Math.abs((int) idx);
                        }

                        String text = CommonHelper.getSpannedText(t);

                        GridEvent event = new GridEvent(t.getSId(), (int) idx, (int) idx,
                                text);
                        event.setTextID(tokenTextID);

                        // check if the token is a matched node
                        Long match = markCoveredTokens(input.getMarkedAndCovered(), t);
                        event.setMatch(match);

                        tokenRow.addEvent(event);
                    }
                } // end token row

                ArrayList<Row> tokenRowList = new ArrayList<Row>();
                tokenRowList.add(tokenRow);

                if (Boolean.parseBoolean(
                        input.getMappings().getProperty(MAPPING_HIDE_TOK_KEY, "false")) == false) {
                    rowsByAnnotation.put("tok", tokenRowList);
                }

                grid.setRowsByAnnotation(rowsByAnnotation);
                grid.setTokenIndexOffset(tokenOffsetForText);
            } // end if input not null
        }

        /**
         * Checks if a token is covered by a matched node but not a match by it
         * self.
         *
         * @param markedAndCovered A mapping from node to a matched number. The
         * node must not matched directly, but covered by a matched node.
         * @param tok the checked token.
         * @return Returns null, if token is not covered neither marked.
         */
        private Long markCoveredTokens(Map<SNode, Long> markedAndCovered, SToken tok) {

            RelannisNodeFeature f = RelannisNodeFeature.extract(tok);
            if (markedAndCovered.containsKey(tok) && f != null
                    && f.getMatchedNode() == null) {
                return markedAndCovered.get(tok);
            }
            return f != null ? f.getMatchedNode() : null;
        }
    } // end GridVisualizerComponent
}
