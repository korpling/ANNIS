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
package annis.visualizers.component.grid;

import annis.CommonHelper;
import annis.gui.widgets.grid.GridEvent;
import annis.gui.widgets.grid.Row;
import annis.libgui.Helper;
import annis.libgui.PDFPageHelper;
import annis.libgui.media.PDFController;
import annis.libgui.media.TimeHelper;
import annis.libgui.visualizers.VisualizerInput;
import static annis.model.AnnisConstants.ANNIS_NS;
import static annis.model.AnnisConstants.FEAT_MATCHEDANNOS;
import static annis.model.AnnisConstants.FEAT_MATCHEDNODE;
import static annis.model.AnnisConstants.FEAT_RELANNIS_NODE;
import annis.model.RelannisNodeFeature;
import static annis.visualizers.component.grid.GridComponent.MAPPING_ANNOS_KEY;
import static annis.visualizers.component.grid.GridComponent.MAPPING_ANNO_REGEX_KEY;
import com.google.common.base.Splitter;
import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class EventExtractor {

  private static final Logger log = LoggerFactory.getLogger(EventExtractor.class);

  /**
   * Converts Salt document graph to rows.
   *
   * @param input
   * @param showSpanAnnos
   * @param showTokenAnnos
   * @param mediaLayer  A set of all annotation layers which should be treated as special media layer.
   * @param annotationNames
   * @param replaceValueWithMediaIcon If true the actual value is removed and an icon for playing the media file is shown instead.
   * @param startTokenIndex token index of the first token in the match
   * @param endTokenIndex token index of the last token in the match
   * @param pdfController makes status of all pdfviewer available for the
   * events.
   * @param text If non-null only include annotations for nodes of the specified text.
   * @return
   */
  public static LinkedHashMap<String, ArrayList<Row>> parseSalt(
          VisualizerInput input, boolean showSpanAnnos, boolean showTokenAnnos,
          List<String> annotationNames, 
          Set<String> mediaLayer, boolean replaceValueWithMediaIcon,
          long startTokenIndex, long endTokenIndex,
          PDFController pdfController, STextualDS text) 
  {

    SDocumentGraph graph = input.getDocument().getDocumentGraph();

    // only look at annotations which were defined by the user
    LinkedHashMap<String, ArrayList<Row>> rowsByAnnotation =
            new LinkedHashMap<>();

    for (String anno : annotationNames) {
      rowsByAnnotation.put(anno, new ArrayList<Row>());
    }

    AtomicInteger eventCounter = new AtomicInteger();

    PDFPageHelper pageNumberHelper = new PDFPageHelper(input);

    if(showSpanAnnos)
    {
      for (SSpan span : graph.getSpans())
      {
        if(text == null || text == CommonHelper.getTextualDSForNode(span, graph))
        {
          addAnnotationsForNode(span, graph, startTokenIndex, endTokenIndex,
            pdfController, pageNumberHelper, eventCounter, rowsByAnnotation,
            true, mediaLayer, replaceValueWithMediaIcon);
        }
      } // end for each span
    }
    
    if(showTokenAnnos)
    {
      for(SToken tok : graph.getTokens())
      {
        if(text == null || text == CommonHelper.getTextualDSForNode(tok, graph))
        {
          addAnnotationsForNode(tok, graph, startTokenIndex, endTokenIndex,
            pdfController, pageNumberHelper, eventCounter, rowsByAnnotation, false,
            mediaLayer, replaceValueWithMediaIcon);
        }
      }
    }

    // 2. merge rows when possible
    for (Map.Entry<String, ArrayList<Row>> e : rowsByAnnotation.entrySet()) {
      mergeAllRowsIfPossible(e.getValue());
    }

    // 3. sort events on one row by left token index
    for (Map.Entry<String, ArrayList<Row>> e : rowsByAnnotation.entrySet()) {
      for (Row r : e.getValue()) {
        sortEventsByTokenIndex(r);
      }
    }
    
    // 4. split up events if they cover islands
    for (Map.Entry<String, ArrayList<Row>> e : rowsByAnnotation.entrySet()) {
      for (Row r : e.getValue()) {
        splitRowsOnIslands(r, graph, text, startTokenIndex, endTokenIndex);
      }
    }

    // 5. split up events if they have gaps
    for (Map.Entry<String, ArrayList<Row>> e : rowsByAnnotation.entrySet()) {
      for (Row r : e.getValue()) {
        splitRowsOnGaps(r, graph, startTokenIndex, endTokenIndex);
      }
    }
    
    return rowsByAnnotation;
  }
  
  public static void removeEmptySpace(LinkedHashMap<String, 
    ArrayList<Row>> rowsByAnnotation, Row tokenRow)
  {
    List<Range<Integer>> gaps = new LinkedList<>();

    BitSet totalOccupancyGrid = new BitSet();
    for(Map.Entry<String, ArrayList<Row>> layer : rowsByAnnotation.entrySet())
    {
      for(Row r : layer.getValue())
      {
        totalOccupancyGrid.or(r.getOccupancyGridCopy());
      }
    }
    // We always include the token row in the occupancy grid since it is not
    // a gap. Otherwise empty token would trigger gaps if the token list
    // is included in the visualizer output.
    // See https://github.com/korpling/ANNIS/issues/281 for the corresponding
    // bug report.
    if(tokenRow != null)
    {
      totalOccupancyGrid.or(tokenRow.getOccupancyGridCopy());
    }
    
    
    // The Range class can give us the next bit that is not set. Use this
    // to detect gaps. A gap starts from the next non-set bit and goes to
    // the next set bit.
    Range<Integer> gap = Range.closed(-1, totalOccupancyGrid.nextSetBit(0));
    while(true)
    {
      int gapStart = totalOccupancyGrid.nextClearBit(gap.upperEndpoint()+1);
      int gapEnd = totalOccupancyGrid.nextSetBit(gapStart);
      if(gapEnd <= 0)
      {
        break;
      }
      gap = Range.closed(gapStart, gapEnd-1);
      gaps.add(gap);
      
      
    }
    
    int gapID =0;
    int totalOffset = 0;
    for(Range<Integer> gRaw : gaps)
    {
      // adjust the space range itself
      Range<Integer> g = 
        Range.closed(gRaw.lowerEndpoint() - totalOffset, gRaw.upperEndpoint() - totalOffset);
      int offset = g.upperEndpoint() - g.lowerEndpoint();
      totalOffset += offset;
      
      for(Entry<String, ArrayList<Row>> rowEntry : rowsByAnnotation.entrySet())
      {
        ArrayList<Row> rows = rowEntry.getValue();
        for(Row r : rows)
        {
          List<GridEvent> eventsCopy = new LinkedList<>(r.getEvents());
          for(GridEvent e : eventsCopy)
          {
            if(e.getLeft() >= g.upperEndpoint())
            {
              
              
              r.removeEvent(e);
              e.setLeft(e.getLeft() - offset);
              e.setRight(e.getRight() - offset);
              r.addEvent(e);
            }
          }
          
          // add a special space event
          String spaceCaption ="";
          if("tok".equalsIgnoreCase(rowEntry.getKey()))
          {
            spaceCaption = "(...)";
          }
          GridEvent spaceEvent = new GridEvent("gap-" + gapID, g.lowerEndpoint(), g.lowerEndpoint(), spaceCaption);
          spaceEvent.setSpace(true);
          r.addEvent(spaceEvent);
          gapID++; 
       }
      }
    }
  }
  private static void addAnnotationsForNode(SNode node,
    SDocumentGraph graph,
    long startTokenIndex, long endTokenIndex,
    PDFController pdfController, PDFPageHelper pageNumberHelper,
    AtomicInteger eventCounter,
    LinkedHashMap<String, ArrayList<Row>> rowsByAnnotation,
    boolean addMatch,
    Set<String> mediaLayer, boolean replaceValueWithMediaIcon)
  {

    List<String> matchedAnnos = new ArrayList<>();
    SFeature featMatchedAnnos = graph.getFeature(ANNIS_NS, FEAT_MATCHEDANNOS);
    if(featMatchedAnnos != null)
    {
      matchedAnnos = Splitter.on(',').trimResults()
        .splitToList(featMatchedAnnos.getValue_STEXT());
    }
    // check if the span is a matched node
    SFeature featMatched = node.getFeature(ANNIS_NS, FEAT_MATCHEDNODE);
    Long matchRaw = featMatched == null ? null : featMatched.
      getValue_SNUMERIC();
    
    String matchedQualifiedAnnoName = "";
    if(matchRaw != null && matchRaw <= matchedAnnos.size())
    {
      matchedQualifiedAnnoName = matchedAnnos.get((int) ((long) matchRaw)-1);
    }
    

    // calculate the left and right values of a span
    // TODO: howto get these numbers with Salt?
    RelannisNodeFeature feat = (RelannisNodeFeature) node.
      getFeature(ANNIS_NS, FEAT_RELANNIS_NODE).getValue();

    long leftLong = feat.getLeftToken();
    long rightLong = feat.getRightToken();

    leftLong = clip(leftLong, startTokenIndex, endTokenIndex);
    rightLong = clip(rightLong, startTokenIndex, endTokenIndex);

    int left = (int) (leftLong - startTokenIndex);
    int right = (int) (rightLong - startTokenIndex);

    for (SAnnotation anno : node.getAnnotations())
    {
      ArrayList<Row> rows = rowsByAnnotation.get(anno.getQName());
      if (rows == null)
      {
        // try again with only the name
        rows = rowsByAnnotation.get(anno.getName());
      }
      if (rows != null)
      {
        // only do something if the annotation was defined before

        // 1. give each annotation of each span an own row
        Row r = new Row();

        String id = "event_" + eventCounter.incrementAndGet();
        GridEvent event = new GridEvent(id, left, right,
          anno.getValue_STEXT());
        event.setTooltip(Helper.getQualifiedName(anno));

        if(addMatch && matchRaw != null)
        {
          long match = matchRaw;
          
          if(matchedQualifiedAnnoName.isEmpty())
          {
            // always set the match when there is no matched annotation at all
            event.setMatch(match);
          }
          // check if the annotation also matches
          else if(matchedQualifiedAnnoName.equals(anno.getQName()))
          {
            event.setMatch(match);
          }

        }
        if(node instanceof SSpan)
        {
          // calculate overlapped SToken
          
          List<? extends SRelation<? extends SNode, ? extends SNode>> outEdges = graph.getOutRelations(node.getId());
          if (outEdges != null)
          {
            for (SRelation<? extends SNode, ? extends SNode> e : outEdges)
            {
              if (e instanceof SSpanningRelation)
              {
                SSpanningRelation spanRel = (SSpanningRelation) e;

                SToken tok = spanRel.getTarget();
                event.getCoveredIDs().add(tok.getId());

                // get the STextualDS of this token and add it to the event
                String textID = getTextID(tok, graph);
                if(textID != null)
                {
                  event.setTextID(textID);
                }
              }
            }
          } // end if span has out edges
        }
        else if(node instanceof SToken)
        {
          event.getCoveredIDs().add(node.getId());
          // get the STextualDS of this token and add it to the event
          String textID = getTextID((SToken) node, graph);
          if(textID != null)
          {
            event.setTextID(textID);
          }
        }


        // try to get time annotations
        if(mediaLayer == null || mediaLayer.contains(anno.getQName()))
        {
          
          double[] startEndTime = TimeHelper.getOverlappedTime(node);
          if (startEndTime.length == 1)
          {
            if (replaceValueWithMediaIcon)
            {
              event.setValue(" ");
              event.setTooltip("play excerpt " + event.getStartTime());
            }
            event.setStartTime(startEndTime[0]);
          }
          else if (startEndTime.length == 2)
          {
            event.setStartTime(startEndTime[0]);
            event.setEndTime(startEndTime[1]);
            if (replaceValueWithMediaIcon)
            {
              event.setValue(" ");
              event.setTooltip("play excerpt " + event.getStartTime() + "-"
                + event.getEndTime());
            }
          }
          
        }

        r.addEvent(event);
        rows.add(r);

        if (pdfController != null &&
          pdfController.sizeOfRegisterdPDFViewer() > 0)
        {
          String page = pageNumberHelper.getPageFromAnnotation(node);
          if (page != null)
          {
            event.setPage(page);
          }
        }
      }
    } // end for each annotation of span
  }

  private static String getTextID(SToken tok, SDocumentGraph graph)
  {
    List<? extends SRelation<? extends SNode, ? extends SNode>> tokenOutEdges = graph.getOutRelations(tok.getId());
    if (tokenOutEdges != null)
    {
      for (SRelation<? extends SNode, ? extends SNode> tokEdge : tokenOutEdges)
      {
        if (tokEdge instanceof STextualRelation)
        {
          return ((STextualRelation) tokEdge).
            getTarget().getId();
        }
      }
    }
    return null;
  }

  /**
   * Returns the annotations to display according to the mappings configuration.
   *
   * This will check the "annos" and "annos_regex" paramters for determining.
   * the annotations to display. It also iterates over all nodes of the graph
   * matching the type.
   *
   * @param input The input for the visualizer.
   * @param type Which type of nodes to include
   * @return
   */
  public static List<String> computeDisplayAnnotations(VisualizerInput input,
          Class<? extends SNode> type) {
    if (input == null) {
      return new LinkedList<>();
    }

    SDocumentGraph graph = input.getDocument().getDocumentGraph();

    Set<String> annoPool = SToken.class.isAssignableFrom(type) ?
      getAnnotationLevelSet(graph, null, type)
      : getAnnotationLevelSet(graph, input.getNamespace(), type);
    List<String> annos = new LinkedList<>(annoPool);

    String annosConfiguration = input.getMappings().getProperty(
            MAPPING_ANNOS_KEY);
    if (annosConfiguration != null && annosConfiguration.trim().length() > 0) {
      String[] split = annosConfiguration.split(",");
      annos.clear();
      for (String s : split) {
        s = s.trim();
        // is regular expression?
        if (s.startsWith("/") && s.endsWith("/")) {
          // go over all remaining items in our pool of all annotations and
          // check if they match
          Pattern regex = Pattern.compile(StringUtils.strip(s, "/"));

          LinkedList<String> matchingAnnos = new LinkedList<>();
          for (String a : annoPool) {
            if (regex.matcher(a).matches()) {
              matchingAnnos.add(a);
            }
          }

          annos.addAll(matchingAnnos);
          annoPool.removeAll(matchingAnnos);

        } else {
          annos.add(s);
          annoPool.remove(s);
        }
      }
    }

    // filter already found annotation names by regular expression
    // if this was given as mapping
    String regexFilterRaw = input.getMappings().getProperty(
            MAPPING_ANNO_REGEX_KEY);
    if (regexFilterRaw != null) {
      try {
        Pattern regexFilter = Pattern.compile(regexFilterRaw);
        ListIterator<String> itAnnos = annos.listIterator();
        while (itAnnos.hasNext()) {
          String a = itAnnos.next();
          // remove entry if not matching
          if (!regexFilter.matcher(a).matches()) {
            itAnnos.remove();
          }
        }
      } catch (PatternSyntaxException ex) {
        log.
                warn("invalid regular expression in mapping for grid visualizer",
                ex);
      }
    }
    return annos;
  }
  
  /**
   * Returns the annotations to which should be displayed together with their namespace.
   *
   * This will check the "show_ns" paramter for determining.
   * the annotations to display. It also iterates over all nodes of the graph
   * matching the type.
   *
   * @param input The input for the visualizer.
   * @param types Which types of nodes to include
   * @return
   */
  public static Set<String> computeDisplayedNamespace(VisualizerInput input,
          List<Class<? extends SNode>> types) {
    if (input == null) {
      return new HashSet<>();
    }    

    String showNamespaceConfig = input.getMappings().getProperty(
            GridComponent.MAPPING_SHOW_NAMESPACE);
    
    if (showNamespaceConfig != null)
    {
      
      SDocumentGraph graph = input.getDocument().getDocumentGraph();

      Set<String> annoPool = new LinkedHashSet<>();
      for(Class<? extends SNode> t : types)
      {
        annoPool.addAll(SToken.class.isAssignableFrom(t)
        ? getAnnotationLevelSet(graph, null, t)
        : getAnnotationLevelSet(graph, input.getNamespace(), t));
      }
      
      
      if ("true".equalsIgnoreCase(showNamespaceConfig))
      {
        // all annotations should be displayed with a namespace
        return annoPool;
      }
      else if("false".equalsIgnoreCase(showNamespaceConfig))
      {
        return new LinkedHashSet<>();
      }
      else
      {
        Set<String> annos = new LinkedHashSet<>();
        
        List<String> defs = Splitter.on(',').omitEmptyStrings()
          .trimResults().splitToList(showNamespaceConfig);
        for (String s : defs)
        {
          // is regular expression?
          if (s.startsWith("/") && s.endsWith("/"))
          {
            // go over all remaining items in our pool of all annotations and
            // check if they match
            Pattern regex = Pattern.compile(StringUtils.strip(s, "/"));

            LinkedList<String> matchingAnnos = new LinkedList<>();
            for (String a : annoPool)
            {
              if (regex.matcher(a).matches())
              {
                matchingAnnos.add(a);
              }
            }

            annos.addAll(matchingAnnos);
            annoPool.removeAll(matchingAnnos);

          }
          else
          {
            annos.add(s);
            annoPool.remove(s);
          }
        }
        
        return annos;
      }
    }
    
    return new LinkedHashSet<>();
  }

  /**
   * Get the qualified name of all annotations belonging to spans having a
   * specific namespace.
   *
   * @param graph The graph.
   * @param namespace The namespace of the node (not the annotation) to search
   * for.
   * @param type Which type of nodes to include
   * @return
   *
   */
  private static Set<String> getAnnotationLevelSet(SDocumentGraph graph,
          String namespace, Class<? extends SNode> type) {
    Set<String> result = new TreeSet<>();

    if (graph != null) {
      List<? extends SNode> nodes;
      // catch most common cases directly
      if (SSpan.class == type) {
        nodes = graph.getSpans();
      } else if (SToken.class == type) {
        nodes = graph.getTokens();
      } else {
        nodes = graph.getNodes();
      }
      if (nodes != null) {
        for (SNode n : nodes) {
          if (type.isAssignableFrom(n.getClass())) {
            for (SLayer layer : n.getLayers()) {
              if (namespace == null || namespace.equals(layer.getName())) {
                for (SAnnotation anno : n.getAnnotations()) {
                  result.add(anno.getQName());
                }
                // we got all annotations of this node, jump to next node
                break;
              } // end if namespace equals layer name
            } // end for each layer
          }
        } // end for each node
      }
    }

    return result;
  }

  /**
   * Merges the rows. This function uses a heuristical approach that guarantiess
   * to merge all rows into one if there is no conflict at all. If there are
   * conflicts the heuristic will be best-efford but with linear runtime (given
   * a a number of rows).
   *
   * @param rows Will be altered, if no conflicts occcured this wil have only
   * one element.
   */
  private static void mergeAllRowsIfPossible(ArrayList<Row> rows) {
    // use fixed seed in order to get consistent results (with random properties)
    Random rand = new Random(5711l);
    int tries = 0;
    // this should be enough to be quite sure we don't miss any optimalization
    // possibility
    final int maxTries = rows.size() * 2;

    // do this loop until we successfully merged everything into one row
    // or we give up until too much tries
    while (rows.size() > 1 && tries < maxTries) {
      // choose two random entries
      int oneIdx = rand.nextInt(rows.size());
      int secondIdx = rand.nextInt(rows.size());
      if (oneIdx == secondIdx) {
        // try again if we choose the same rows by accident
        continue;
      }

      Row one = rows.get(oneIdx);
      Row second = rows.get(secondIdx);

      if (one.merge(second)) {
        // remove the second one since it is merged into the first
        rows.remove(secondIdx);

        // success: reset counter
        tries = 0;
      } else {
        // increase counter to avoid endless loops if no improvement is possible
        tries++;
      }
    }
  }

  /**
   * Sort events of a row. The sorting is depending on the left value of the
   * event
   *
   * @param row
   */
  private static void sortEventsByTokenIndex(Row row) {
    Collections.sort(row.getEvents(), new Comparator<GridEvent>() {
      @Override
      public int compare(GridEvent o1, GridEvent o2) {
        if (o1 == o2) {
          return 0;
        }
        if (o1 == null) {
          return -1;
        }
        if (o2 == null) {
          return +1;
        }

        return ((Integer) o1.getLeft()).compareTo(o2.getLeft());
      }
    });
  }
  
  /**
   * Splits events of a row if they overlap an island.  Islands are areas between
   * the token which are included in the result.
   *
   * @param row
   * @param graph
   * @param text
   * @param startTokenIndex token index of the first token in the match
   * @param endTokenIndex token index of the last token in the match
   */
  private static void splitRowsOnIslands(Row row, 
    final SDocumentGraph graph,
    STextualDS text,
    long startTokenIndex, long endTokenIndex)
  {
    
    BitSet tokenCoverage = new BitSet();
    // get the sorted token
    List<SToken> sortedTokenList = graph.getSortedTokenByText();
    // add all token belonging to the right text to the bit set
    ListIterator<SToken> itToken = sortedTokenList.listIterator();
    while (itToken.hasNext())
    {
      SToken t = itToken.next();
      if (text == null || text == CommonHelper.getTextualDSForNode(t, graph))
      {
        RelannisNodeFeature feat = (RelannisNodeFeature) t.getFeature(
          ANNIS_NS,
          FEAT_RELANNIS_NODE).getValue();
        long tokenIndexRaw = feat.getTokenIndex();

        tokenIndexRaw = clip(tokenIndexRaw, startTokenIndex, endTokenIndex);
        int tokenIndex = (int) (tokenIndexRaw - startTokenIndex);
        tokenCoverage.set(tokenIndex);
      }
    }

    ListIterator<GridEvent> itEvents = row.getEvents().listIterator();
    while (itEvents.hasNext())
    {
      GridEvent event = itEvents.next();
      BitSet eventBitSet = new BitSet();
      eventBitSet.set(event.getLeft(), event.getRight()+1);
      
      // restrict event bitset on the locations where token are present
      eventBitSet.and(tokenCoverage);
      
      // if there is is any 0 bit before the right border there is a break in the event
      // and we need to split it
      if(eventBitSet.nextClearBit(event.getLeft()) <= event.getRight())
      {
        // remove the original event
        row.removeEvent(itEvents);
        
        // The event bitset now marks all the locations which the event should
        // cover.
        // Make a list of new events for each connected range in the bitset
        int subElement = 0;
        int offset = eventBitSet.nextSetBit(0);
        while(offset >= 0)
        {
          int end = eventBitSet.nextClearBit(offset)-1;
          if(offset < end)
          {
            GridEvent newEvent = new GridEvent(event);
            newEvent.setId(event.getId() + "_islandsplit_" +  subElement++);
            newEvent.setLeft(offset);
            newEvent.setRight(end);
            row.addEvent(itEvents, newEvent);
          }
          offset = eventBitSet.nextSetBit(end+1);
        }
      } // end if we need to split

    }
  }

  /**
   * Splits events of a row if they contain a gap. Gaps are found using the
   * token index (provided as ANNIS specific {@link SFeature}. Inserted events
   * have a special style to mark them as gaps.
   *
   * @param row
   * @param graph
   * @param startTokenIndex token index of the first token in the match
   * @param endTokenIndex token index of the last token in the match
   */
  private static void splitRowsOnGaps(Row row, final SDocumentGraph graph,
    long startTokenIndex, long endTokenIndex)
  {
    ListIterator<GridEvent> itEvents = row.getEvents().listIterator();
    while (itEvents.hasNext())
    {
      GridEvent event = itEvents.next();

      int lastTokenIndex = -1;

      // sort the coveredIDs
      LinkedList<String> sortedCoveredToken = new LinkedList<>(event.
        getCoveredIDs());
      Collections.sort(sortedCoveredToken, new Comparator<String>()
      {
        @Override
        public int compare(String o1, String o2)
        {
          SNode node1 = graph.getNode(o1);
          SNode node2 = graph.getNode(o2);

          if (node1 == node2)
          {
            return 0;
          }
          if (node1 == null)
          {
            return -1;
          }
          if (node2 == null)
          {
            return +1;
          }

          RelannisNodeFeature feat1 = (RelannisNodeFeature) node1.getFeature(
            ANNIS_NS,
            FEAT_RELANNIS_NODE).getValue();
          RelannisNodeFeature feat2 = (RelannisNodeFeature) node2.getFeature(
            ANNIS_NS,
            FEAT_RELANNIS_NODE).getValue();

          long tokenIndex1 = feat1.getTokenIndex();
          long tokenIndex2 = feat2.getTokenIndex();

          return ((Long) (tokenIndex1)).compareTo(tokenIndex2);
        }
      });

      // first calculate all gaps
      List<GridEvent> gaps = new LinkedList<>();
      for (String id : sortedCoveredToken)
      {

        SNode node = graph.getNode(id);
        RelannisNodeFeature feat = (RelannisNodeFeature) node.getFeature(
          ANNIS_NS,
          FEAT_RELANNIS_NODE).getValue();
        long tokenIndexRaw = feat.getTokenIndex();

        tokenIndexRaw = clip(tokenIndexRaw, startTokenIndex, endTokenIndex);

        int tokenIndex = (int) (tokenIndexRaw - startTokenIndex);
        
        // sanity check
        if(tokenIndex >= event.getLeft() && tokenIndex <= event.getRight())
        {
          int diff = tokenIndex - lastTokenIndex;

          if (lastTokenIndex >= 0 && diff > 1)
          {
            // we detected a gap
            GridEvent gap = new GridEvent(event.getId() + "_gap_" + gaps.size(),
              lastTokenIndex + 1, tokenIndex - 1, "");
            gap.setGap(true);
            gaps.add(gap);
          }

          lastTokenIndex = tokenIndex;
        }
        else
        {
          // reset gap search when discovered there were token we use for 
          // hightlighting but do not actually cover
          lastTokenIndex = -1;
        }
      } // end for each covered token id

      ListIterator<GridEvent> itGaps = gaps.listIterator();
      // remember the old right value
      int oldRight = event.getRight();
      
      int gapNr = 0;
      while(itGaps.hasNext())
      {
        GridEvent gap = itGaps.next();
      
        if(gapNr == 0)
        {
          // shorten original event
          event.setRight(gap.getLeft() - 1);
        }
        
        // insert the real gap
        itEvents.add(gap);

        int rightBorder = oldRight;
        if(itGaps.hasNext())
        {
          // don't use the old event right border since the gap should only go until
          // the next event
          GridEvent nextGap = itGaps.next();
          itGaps.previous();

          rightBorder = nextGap.getLeft()-1;

        }
        // insert a new event node that covers the rest of the event
        GridEvent after = new GridEvent(event);
          
        after.setId(event.getId() + "_after_" + gapNr);
        after.setLeft(gap.getRight() + 1);
        after.setRight(rightBorder);
        
        itEvents.add(after);
        gapNr++;
      }

    }
  }

  private static long clip(long value, long min, long max) {
    if (value > max) {
      return max;
    } else if (value < min) {
      return min;
    } else {
      return value;
    }

  }
}
