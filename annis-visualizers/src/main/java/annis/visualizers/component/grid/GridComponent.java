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

import annis.CommonHelper;
import annis.gui.widgets.grid.AnnotationGrid;
import annis.gui.widgets.grid.GridEvent;
import annis.gui.widgets.grid.Row;
import annis.libgui.media.MediaController;
import annis.libgui.media.PDFController;
import annis.libgui.visualizers.VisualizerInput;
import annis.model.AnnisConstants;
import annis.model.RelannisNodeFeature;
import com.google.common.base.Preconditions;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import org.eclipse.emf.common.util.EList;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class GridComponent extends Panel
{

  private static final org.slf4j.Logger log
    = LoggerFactory.getLogger(GridComponent.class);
  public static final String MAPPING_ANNOS_KEY = "annos";
  public static final String MAPPING_ANNO_REGEX_KEY = "anno_regex";
  public static final String MAPPING_HIDE_TOK_KEY = "hide_tok";
  public static final String MAPPING_TOK_ANNOS_KEY = "tok_anno";
  public static final String MAPPING_ESCAPE_HTML = "escape_html";
  private AnnotationGrid grid;
  private final transient VisualizerInput input;
  private final transient MediaController mediaController;
  private final transient PDFController pdfController;
  private final VerticalLayout layout;
  private Set<String> manuallySelectedTokenAnnos;
  private String segmentationName;
  private transient STextualDS enforcedText;
  
  public enum ElementType
  {

    begin, end, middle, single, noEvent
  }

  public GridComponent(VisualizerInput input, MediaController mediaController,
    PDFController pdfController, boolean forceToken, STextualDS enforcedText)
  {
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
    
    if (input != null)
    {
      this.manuallySelectedTokenAnnos = input.getVisibleTokenAnnos();
      this.segmentationName = forceToken ? null :  input.getSegmentationName();
      
      EList<STextualDS> texts
        = input.getDocument().getSDocumentGraph().getSTextualDSs();
      if (texts != null && texts.size() > 0)
      {
        if (CommonHelper.containsRTLText(texts.get(0).getSText()))
        {
          addStyleName("rtl");
        }
      }

      createAnnotationGrid();
    } // end if input not null
    

  }


  private void createAnnotationGrid()
  {
    String resultID = input.getId();
    grid = new AnnotationGrid(mediaController, pdfController, resultID);
    grid.addStyleName(getMainStyle());
    grid.addStyleName("corpus-font-force");
    grid.setEscapeHTML(Boolean.parseBoolean(input.getMappings().
      getProperty(MAPPING_ESCAPE_HTML, "true")));
    layout.addComponent(grid);
    SDocumentGraph graph = input.getDocument().getSDocumentGraph();
    
    List<SNode> tokens = CommonHelper.getSortedSegmentationNodes(segmentationName,
      graph);
    Preconditions.checkArgument(!tokens.isEmpty(), "Token list must be non-empty");
    RelannisNodeFeature featTokStart
      = (RelannisNodeFeature) tokens.get(0).
      getSFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_RELANNIS_NODE).
      getValue();
    long startIndex = featTokStart.getTokenIndex();
    RelannisNodeFeature featTokEnd
      = (RelannisNodeFeature) tokens.get(tokens.size() - 1).
      getSFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_RELANNIS_NODE).
      getValue();
    long endIndex = featTokEnd.getTokenIndex();
    
    LinkedHashMap<String, ArrayList<Row>> rowsByAnnotation = 
      computeAnnotationRows(startIndex, endIndex);
    
    // add tokens as row
    AtomicInteger tokenOffsetForText = new AtomicInteger(-1);
    ArrayList<Row> tokenRowList = computeTokenRow(tokens, graph,
      rowsByAnnotation, startIndex, tokenOffsetForText);
    if (isHidingToken() == false)
    {
      
      if(isTokenFirst())
      {
        // copy original list but add token row at the beginning
        LinkedHashMap<String, ArrayList<Row>> newList = new LinkedHashMap<String, ArrayList<Row>>();
        
        newList.put("tok", tokenRowList);
        newList.putAll(rowsByAnnotation);
        rowsByAnnotation = newList;
        
      }
      else
      {
        // just add the token row to the end of the list
        rowsByAnnotation.put("tok", tokenRowList);
      }
    }
    
    EventExtractor.removeEmptySpace(rowsByAnnotation);
    
    grid.setRowsByAnnotation(rowsByAnnotation);
    grid.setTokenIndexOffset(tokenOffsetForText.get());
  }
  
  private ArrayList<Row> computeTokenRow(List<SNode> tokens, 
    SDocumentGraph graph, LinkedHashMap<String, ArrayList<Row>> rowsByAnnotation,
    long startIndex, AtomicInteger tokenOffsetForText)
  {
    /* we will only add tokens of one texts which is mentioned by any
      included annotation. */
    Set<String> validTextIDs = new HashSet<String>();
    
    if(enforcedText == null)
    {
      Iterator<ArrayList<Row>> itAllRows = rowsByAnnotation.values().iterator();
      while (itAllRows.hasNext())
      {
        ArrayList<Row> rowsForAnnotation = itAllRows.next();
        for (Row r : rowsForAnnotation)
        {
          validTextIDs.addAll(r.getTextIDs());
        }
      }
      /**
       * we want to show all token if no valid text was found and we have only one
       * text and the first one if there are more than one text.
       */
      EList<STextualDS> allTexts = graph.getSTextualDSs();
      if (validTextIDs.isEmpty() && allTexts != null && (allTexts.size() == 1
        || allTexts.size() == 2))
      {
        validTextIDs.add(allTexts.get(0).getSId());
      }
    }
    else
    {
      validTextIDs.add(enforcedText.getSId());
    }
    
    Row tokenRow = new Row();
    for (SNode t : tokens)
    {
      // get the Salt ID of the STextualDS of this token
      STextualDS tokenText = CommonHelper.getTextualDSForNode(t, graph);

      // only add token if text ID matches the valid one
      if (tokenText != null && validTextIDs.contains(tokenText.getSId()))
      {
        RelannisNodeFeature feat
          = (RelannisNodeFeature) t.getSFeature(AnnisConstants.ANNIS_NS,
            AnnisConstants.FEAT_RELANNIS_NODE).getValue();
        long idxLeft = feat.getLeftToken() - startIndex;
        long idxRight = feat.getRightToken() - startIndex;
        if (tokenOffsetForText.get() < 0)
        {
          // set the token offset by assuming the first idx must be zero
          tokenOffsetForText.set(Math.abs((int) idxLeft));
        }
        String text = extractTextForToken(t, segmentationName);
        GridEvent event
          = new GridEvent(t.getSId(), (int) idxLeft, (int) idxRight, text);
        event.setTextID(tokenText.getSId());
        // check if the token is a matched node
        Long match = markCoveredTokens(input.getMarkedAndCovered(), t);
        event.setMatch(match);
        tokenRow.addEvent(event);
      }
    } // end token row
    ArrayList<Row> tokenRowList = new ArrayList<Row>();
    tokenRowList.add(tokenRow);
    
    return tokenRowList;
  }
  
  private String extractTextForToken(SNode t, String segmentation)
  {
    if(t instanceof SToken)
    {
      return CommonHelper.getSpannedText((SToken) t);
    }
    else if(segmentation != null)
    {
      for(SAnnotation anno : t.getSAnnotations())
      {
        if(anno.getSName().equals(segmentation))
        {
          return anno.getSValueSTEXT();
        }
      }
    }
    return "";
  }
  
  private LinkedHashMap<String, ArrayList<Row>> computeAnnotationRows(
    long startIndex, long endIndex)
  {
    List<String> annos = new LinkedList<String>();
    
    boolean showSpanAnnotations = isShowingSpanAnnotations();
    if(showSpanAnnotations)
    {
      annos.addAll(EventExtractor.computeDisplayAnnotations(input, SSpan.class));
    }
    
    boolean showTokenAnnotations = isShowingTokenAnnotations();
    
    if (showTokenAnnotations)
    {
      List<String> tokenAnnos
        = EventExtractor.computeDisplayAnnotations(input, SToken.class);
      if(manuallySelectedTokenAnnos != null)
      {
        tokenAnnos.retainAll(manuallySelectedTokenAnnos);
      }
      annos.addAll(tokenAnnos);
    }
    
     // search for media annotations
    Set<String> mediaAnnotations = null;
    
    if(isFilteringMediaLayer())
    {
      mediaAnnotations = new HashSet<String>();
      Pattern patternMedia = Pattern.compile("(annis::)?time");
      for (String qname : annos)
      {
        if(patternMedia.matcher(qname).matches())
        {
          mediaAnnotations.add(qname);
        }
      }
    }
    
    LinkedHashMap<String, ArrayList<Row>> rowsByAnnotation
      = EventExtractor.parseSalt(input, showSpanAnnotations, 
        showTokenAnnotations, annos, mediaAnnotations, isUnsettingValueForMedia(),
        (int) startIndex, (int) endIndex, pdfController, enforcedText);
    
    return rowsByAnnotation;
  }
  
  public void setVisibleTokenAnnos(Set<String> annos)
  {
    this.manuallySelectedTokenAnnos = annos;
    // complete recreation of the grid
    layout.removeComponent(grid);
    createAnnotationGrid();
    
  }
  
  public void setSegmentationLayer(String segmentationName, 
    Map<String, Long> markedAndCovered)
  {
    this.segmentationName = segmentationName;
    this.input.setMarkedAndCovered(markedAndCovered);
    // complete recreation of the grid
    layout.removeComponent(grid);
    createAnnotationGrid();
  }
  
  protected boolean isShowingTokenAnnotations()
  {
    return Boolean.parseBoolean(input.getMappings().
        getProperty(MAPPING_TOK_ANNOS_KEY));
  }
  
  protected boolean isShowingSpanAnnotations()
  {
    return true;
  }
  
  protected boolean isHidingToken()
  {
    return Boolean.parseBoolean(input.getMappings().
      getProperty(MAPPING_HIDE_TOK_KEY, "false"));
    
  }
  
  protected boolean isTokenFirst()
  {
    return false;
  }
  
  protected boolean isFilteringMediaLayer()
  {
    return false;
  }
  
  protected boolean isUnsettingValueForMedia()
  {
    return false;
  }
  
  protected String getMainStyle()
  {
    return "partitur_table";
  }
  
  /**
   * Checks if a token is covered by a matched node but not a match by it self.
   *
   * @param markedAndCovered A mapping from node to a matched number. The node
   * must not matched directly, but covered by a matched node.
   * @param tok the checked token.
   * @return Returns null, if token is not covered neither marked.
   */
  private Long markCoveredTokens(Map<SNode, Long> markedAndCovered, SNode tok)
  {
    RelannisNodeFeature f = RelannisNodeFeature.extract(tok);
    if (markedAndCovered.containsKey(tok) && f != null && f.getMatchedNode()
      == null)
    {
      return markedAndCovered.get(tok);
    }
    return f != null ? f.getMatchedNode() : null;
  }

  public VisualizerInput getInput()
  {
    return input;
  }

  public AnnotationGrid getGrid()
  {
    return grid;
  }
  
  

} // end GridVisualizerComponent
