/*
 * Copyright 2014 SFB 632.
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

import static annis.model.AnnisConstants.ANNIS_NS;
import static annis.model.AnnisConstants.FEAT_MATCHEDNODE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SOrderRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.ui.themes.ChameleonTheme;

import annis.CommonHelper;
import annis.gui.widgets.grid.AnnotationGrid;
import annis.gui.widgets.grid.GridEvent;
import annis.gui.widgets.grid.Row;
import annis.libgui.Helper;
import annis.libgui.media.MediaController;
import annis.libgui.media.PDFController;
import annis.libgui.visualizers.VisualizerInput;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class GridComponent extends Panel {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(GridComponent.class);
    public static final String MAPPING_ANNOS_KEY = "annos";
    public static final String MAPPING_ANNO_REGEX_KEY = "anno_regex";
    public static final String MAPPING_HIDE_TOK_KEY = "hide_tok";
    public static final String MAPPING_TOK_ANNOS_KEY = "tok_anno";
    public static final String MAPPING_ESCAPE_HTML = "escape_html";
    public static final String MAPPING_SHOW_NAMESPACE = "show_ns";
    public static final String MAPPING_GRID_TEMPLATES = "templates";

    private AnnotationGrid grid;
    private final transient VisualizerInput input;
    private final transient MediaController mediaController;
    private final transient PDFController pdfController;
    private final VerticalLayout layout;
    private Set<String> manuallySelectedTokenAnnos;
    private String segmentationName;
    private final transient STextualDS enforcedText;
    private final Label lblEmptyToken;

    public enum ElementType {

        begin, end, middle, single, noEvent
    }

    public GridComponent(VisualizerInput input, MediaController mediaController, PDFController pdfController,
            boolean forceToken, STextualDS enforcedText) {
        this.input = input;
        this.mediaController = mediaController;
        this.pdfController = pdfController;
        this.enforcedText = enforcedText;

        setWidth("100%");
        setHeight("-1");
        layout = new VerticalLayout();
        setContent(layout);
        layout.setSizeUndefined();
        addStyleName(ChameleonTheme.PANEL_BORDERLESS);

        lblEmptyToken = new Label("(Empty token list, you may want to select another base text from the menu above.)");
        lblEmptyToken.setVisible(false);
        lblEmptyToken.addStyleName("empty_token_hint");
        layout.addComponent(lblEmptyToken);
        if (input != null) {
            this.manuallySelectedTokenAnnos = input.getVisibleTokenAnnos();
            this.segmentationName = forceToken ? null : input.getSegmentationName();

            List<STextualDS> texts = input.getDocument().getDocumentGraph().getTextualDSs();
            if (texts != null && texts.size() > 0 && !Helper.isRTLDisabled()) {
                if (CommonHelper.containsRTLText(texts.get(0).getText())) {
                    addStyleName("rtl");
                }
            }

            createAnnotationGrid();
        } // end if input not null

    }

    private boolean createAnnotationGrid() {
        String resultID = input.getId();
        grid = new AnnotationGrid(mediaController, pdfController, resultID);
        grid.addStyleName(getMainStyle());
        grid.addStyleName(Helper.CORPUS_FONT_FORCE);
        grid.setEscapeHTML(Boolean.parseBoolean(input.getMappings().getProperty(MAPPING_ESCAPE_HTML, "true")));
        LinkedList<Class<? extends SNode>> types = new LinkedList<>();
        if (isShowingSpanAnnotations()) {
            types.add(SSpan.class);
        }
        if (isShowingTokenAnnotations()) {
            types.add(SToken.class);
        }
        grid.setAnnosWithNamespace(EventExtractor.computeDisplayedNamespace(input, types));

        layout.addComponent(grid);
        SDocumentGraph graph = input.getDocument().getDocumentGraph();

        List<SNode> sortedSegmentationNodes = CommonHelper.getSortedSegmentationNodes(this.segmentationName, graph);

        Map<SToken, Integer> token2index = Helper.createToken2IndexMap(graph);
        Preconditions.checkArgument(!token2index.isEmpty(), "Token list must be non-empty");

        LinkedHashMap<String, ArrayList<Row>> rowsByAnnotation = computeAnnotationRows(token2index);

        // Get Mappings
        String gridTemplates = input.getMappings().getProperty(MAPPING_GRID_TEMPLATES, "");

        // Parse Mappings
        if (!gridTemplates.equals("")) {
            String[] split = gridTemplates.split("\\|\\|");
            for (String s : split) {
                // example of s: entity="person"==>:), or infstat==><b>%%value%%</b>
                String[] unit_split = s.split("==>");
                Set<Map.Entry<String, ArrayList<Row>>> set = rowsByAnnotation.entrySet();
                // Displaying elements of LinkedHashMap
                Iterator<Map.Entry<String, ArrayList<Row>>> iterator = set.iterator();
                while (iterator.hasNext()) {
                    // iterate over rows
                    Map.Entry<String, ArrayList<Row>> me = iterator.next();
                    String rowKey = (String) me.getKey();
                    ArrayList<Row> rowValue = (ArrayList<Row>) me.getValue();
                    for (Row rowValue1 : rowValue) {
                        ArrayList<GridEvent> rowEvents = rowValue1.getEvents();

                        if (unit_split[0].indexOf('=') < 0) {
                            // unit_split[0] is a single instruction, e.g., infstat
                            // check if the key of a row in rowsByAnnotation is unit_split[0]
                            // if it is, we need to change every value of this row, else we dont do anything
                            String rowName = rowKey.split("::")[1];
                            if (rowName.equals(unit_split[0])) {
                                // iterate over all values and replace the value with the unit_split[1]
                                for (GridEvent ev : rowEvents) {
                                    String origValue = ev.getValue();
                                    String newValue = unit_split[1].replaceAll("%%value%%", origValue);
                                    ev.setValue(newValue);
                                }
                            }
                        } else {
                            // its a instruction like entity='person'
                            // first break this split into entity and person
                            // check if rowKey is entity, then when iterating over events, check if value is
                            // person
                            String rowName = rowKey.split("::")[1];
                            String targetRow = unit_split[0].split("=")[0];
                            String targetValue = unit_split[0].split("=")[1].replaceAll("\"", "");
                            if (rowName.equals(targetRow)) {
                                // iterate over all values and replace the value with the unit_split[1]
                                for (GridEvent ev : rowEvents) {
                                    String origValue = ev.getValue();
                                    if (origValue.equals(targetValue)) {
                                        ev.setValue(unit_split[1]);
                                    }
                                    // String newValue = unit_split[1].replaceAll("%%value%%",origValue);

                                }

                            }
                        }
                    }
                }
            }
        }

        // add tokens as row
        Row tokenRow = computeTokenRow(sortedSegmentationNodes, graph, rowsByAnnotation, token2index);

        String tokenRowCaption = "tok";
        if (isHidingToken()) {

            // We have to add the invisible token row avoid issues with the layout
            // (see https://github.com/korpling/ANNIS/issues/524)
            // but we don't want the invisible token layer to override an actual "tok"
            // annotation layer (see https://github.com/korpling/ANNIS/issues/596)
            tokenRow.setStyle("invisible_token");
            tokenRowCaption = "";
            grid.setTokRowKey("");
        }

        if (isTokenFirst()) {
            // copy original list but add token row at the beginning
            LinkedHashMap<String, ArrayList<Row>> newList = new LinkedHashMap<>();

            newList.put(tokenRowCaption, Lists.newArrayList(tokenRow));
            newList.putAll(rowsByAnnotation);
            rowsByAnnotation = newList;

        } else {
            // just add the token row to the end of the list
            rowsByAnnotation.put(tokenRowCaption, Lists.newArrayList(tokenRow));
        }

        EventExtractor.removeEmptySpace(rowsByAnnotation, tokenRow);

        // check if the token row only contains empty values
        boolean tokenRowIsEmpty = true;
        for (GridEvent tokenEvent : tokenRow.getEvents()) {
            if (tokenEvent.getValue() != null && !tokenEvent.getValue().trim().isEmpty()) {
                tokenRowIsEmpty = false;
                break;
            }
        }
        if (!isHidingToken() && canShowEmptyTokenWarning()) {
            lblEmptyToken.setVisible(tokenRowIsEmpty);
        }
        grid.setRowsByAnnotation(rowsByAnnotation);

        return !tokenRowIsEmpty;
    }

    private boolean hasSegmentation(SNode node, String segmentation) {
        if (segmentation == null) {
            return node instanceof SToken;
        }

        for (SRelation<?, ?> outRel : node.getOutRelations()) {
            if (outRel instanceof SOrderRelation) {
                if (segmentation.equals(outRel.getType())) {
                    return true;
                }
            }
        }
        for (SRelation<?, ?> inRel : node.getInRelations()) {
            if (inRel instanceof SOrderRelation) {
                if (segmentation.equals(inRel.getType())) {
                    return true;
                }
            }
        }
        return false;
    }

    private Row computeTokenRow(List<SNode> tokens, SDocumentGraph graph,
            LinkedHashMap<String, ArrayList<Row>> rowsByAnnotation, Map<SToken, Integer> token2index) {
        /*
         * we will only add tokens of one texts which is mentioned by any included
         * annotation.
         */
        Set<String> validTextIDs = new HashSet<>();

        if (enforcedText == null) {
            Iterator<ArrayList<Row>> itAllRows = rowsByAnnotation.values().iterator();
            while (itAllRows.hasNext()) {
                ArrayList<Row> rowsForAnnotation = itAllRows.next();
                for (Row r : rowsForAnnotation) {
                    validTextIDs.addAll(r.getTextIDs());
                }
            }
            /**
             * we want to show all token if no valid text was found and we have only one
             * text and the first one if there are more than one text.
             */
            List<STextualDS> allTexts = graph.getTextualDSs();
            if (validTextIDs.isEmpty() && allTexts != null && (allTexts.size() == 1 || allTexts.size() == 2)) {
                validTextIDs.add(allTexts.get(0).getId());
            }
        } else {
            validTextIDs.add(enforcedText.getId());
        }

        Row tokenRow = new Row();
        for (SNode t : tokens) {
            // get the Salt ID of the STextualDS of this token
            STextualDS tokenText = CommonHelper.getTextualDSForNode(t, graph);

            // only add token if text ID matches the valid one
            if (tokenText != null && validTextIDs.contains(tokenText.getId())
                    && hasSegmentation(t, this.segmentationName)) {

                Range<Integer> coveredRange = Helper.getLeftRightSpan(t, graph, token2index);

                String text = extractTextForToken(t, segmentationName);
                GridEvent event = new GridEvent(t.getId(), coveredRange.lowerEndpoint(), coveredRange.upperEndpoint(),
                        text);
                event.setTextID(tokenText.getId());
                // check if the token is a matched node
                Long match = isCoveredTokenMarked() ? markCoveredTokens(input.getMarkedAndCovered(), t) : tokenMatch(t);
                event.setMatch(match);
                tokenRow.addEvent(event);
            }
        } // end token row

        return tokenRow;
    }
    
    private static String extractTextForToken(SNode t, String segmentation) {
        if (t instanceof SToken) {
            return CommonHelper.getSpannedText((SToken) t);
        } else if (segmentation != null) {
            for (SAnnotation anno : t.getAnnotations()) {
                if (anno.getName().equals(segmentation)) {
                    return anno.getValue_STEXT();
                }
            }
        }
        return "";
    }


    private LinkedHashMap<String, ArrayList<Row>> computeAnnotationRows(Map<SToken, Integer> token2index) {
        List<String> annos = new LinkedList<>();

        boolean showSpanAnnotations = isShowingSpanAnnotations();
        if (showSpanAnnotations) {
            annos.addAll(EventExtractor.computeDisplayAnnotations(input, SSpan.class));
        }

        boolean showTokenAnnotations = isShowingTokenAnnotations();

        if (showTokenAnnotations) {
            List<String> tokenAnnos = EventExtractor.computeDisplayAnnotations(input, SToken.class);
            if (manuallySelectedTokenAnnos != null) {
                tokenAnnos.retainAll(manuallySelectedTokenAnnos);
            }
            annos.addAll(tokenAnnos);
        }

        // search for media annotations
        Set<String> mediaAnnotations = null;

        if (isFilteringMediaLayer()) {
            mediaAnnotations = new HashSet<>();
            Pattern patternMedia = Pattern.compile("(annis::)?time");
            for (String qname : annos) {
                if (patternMedia.matcher(qname).matches()) {
                    mediaAnnotations.add(qname);
                }
            }
        }

        LinkedHashMap<String, ArrayList<Row>> rowsByAnnotation = EventExtractor.parseSalt(input, showSpanAnnotations,
                showTokenAnnotations, annos, mediaAnnotations, isAddingPlaybackRow(), token2index, pdfController,
                enforcedText);

        return rowsByAnnotation;
    }

    public void setVisibleTokenAnnos(Set<String> annos) {
        this.manuallySelectedTokenAnnos = annos;
        // complete recreation of the grid
        layout.removeComponent(grid);
        createAnnotationGrid();

    }

    public boolean setSegmentationLayer(String segmentationName, Map<SNode, Long> markedAndCovered) {
        this.segmentationName = segmentationName;
        this.input.setMarkedAndCovered(markedAndCovered);
        // complete recreation of the grid
        layout.removeComponent(grid);
        return createAnnotationGrid();
    }

    protected boolean isShowingTokenAnnotations() {
        return Boolean.parseBoolean(input.getMappings().getProperty(MAPPING_TOK_ANNOS_KEY));
    }

    protected boolean isShowingSpanAnnotations() {
        return true;
    }

    protected boolean isHidingToken() {
        return Boolean.parseBoolean(input.getMappings().getProperty(MAPPING_HIDE_TOK_KEY, "false"));

    }

    protected boolean isTokenFirst() {
        return false;
    }

    protected boolean isFilteringMediaLayer() {
        return false;
    }

    protected boolean isAddingPlaybackRow() {
        return false;
    }

    protected boolean canShowEmptyTokenWarning() {
        return false;
    }

    protected boolean isCoveredTokenMarked() {
        return false;
    }

    protected String getMainStyle() {
        return "partitur_table";
    }

    /**
     * Checks if a token is covered by a matched node but not a match by it self.
     *
     * @param markedAndCovered
     *                             A mapping from node to a matched number. The node
     *                             must not matched directly, but covered by a
     *                             matched node.
     * @param tok
     *                             the checked token.
     * @return Returns null, if token is not covered neither marked.
     */
    private Long markCoveredTokens(Map<SNode, Long> markedAndCovered, SNode tok) {
        SFeature featMatched = tok.getFeature(ANNIS_NS, FEAT_MATCHEDNODE);
        if (markedAndCovered.containsKey(tok) && featMatched == null) {
            return markedAndCovered.get(tok);
        }
        return featMatched != null ? featMatched.getValue_SNUMERIC() : null;
    }

    /**
     * Checks if a token is a marked match
     *
     * @param tok
     *                the checked token.
     * @return Returns null, if token is not marked.
     */
    private Long tokenMatch(SNode tok) {
        // check if the span is a matched node
        SFeature featMatched = tok.getFeature(ANNIS_NS, FEAT_MATCHEDNODE);
        Long matchRaw = featMatched == null ? null : featMatched.getValue_SNUMERIC();
        return matchRaw;
    }

    public VisualizerInput getInput() {
        return input;
    }

    public AnnotationGrid getGrid() {
        return grid;
    }

} // end GridVisualizerComponent