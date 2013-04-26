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

import annis.gui.widgets.grid.GridEvent;
import annis.gui.widgets.grid.Row;
import annis.libgui.media.PageHelper;
import annis.libgui.media.TimeHelper;
import annis.libgui.visualizers.VisualizerInput;
import static annis.model.AnnisConstants.ANNIS_NS;
import static annis.model.AnnisConstants.FEAT_LEFTTOKEN;
import static annis.model.AnnisConstants.FEAT_MATCHEDNODE;
import static annis.model.AnnisConstants.FEAT_RIGHTTOKEN;
import static annis.model.AnnisConstants.FEAT_TOKENINDEX;
import static annis.visualizers.component.grid.GridVisualizer.GridVisualizerComponent.MAPPING_ANNOS_KEY;
import static annis.visualizers.component.grid.GridVisualizer.GridVisualizerComponent.MAPPING_ANNO_REGEX_KEY;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Edge;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpanningRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SFeature;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class EventExtractor {

  private static Logger log = LoggerFactory.getLogger(EventExtractor.class);

  /**
   * Converts Salt document graph to rows.
   *
   * @param graph
   * @param annotationNames
   * @param startTokenIndex token index of the first token in the match
   * @param endTokenIndex token index of the last token in the match
   * @return
   */
  public static LinkedHashMap<String, ArrayList<Row>> parseSalt(
          SDocumentGraph graph,
          List<String> annotationNames, long startTokenIndex, long endTokenIndex) {
    // only look at annotations which were defined by the user
    LinkedHashMap<String, ArrayList<Row>> rowsByAnnotation =
            new LinkedHashMap<String, ArrayList<Row>>();

    for (String anno : annotationNames) {
      rowsByAnnotation.put(anno, new ArrayList<Row>());
    }

    int eventCounter = 0;

    PageHelper pageNumberHelper = new PageHelper(graph);

    for (SSpan span : graph.getSSpans()) {
      // calculate the left and right values of a span
      // TODO: howto get these numbers with Salt?
      long leftLong = span.getSFeature(ANNIS_NS, FEAT_LEFTTOKEN).
              getSValueSNUMERIC();
      long rightLong = span.getSFeature(ANNIS_NS, FEAT_RIGHTTOKEN).
              getSValueSNUMERIC();

      leftLong = clip(leftLong, startTokenIndex, endTokenIndex);
      rightLong = clip(rightLong, startTokenIndex, endTokenIndex);

      int left = (int) (leftLong - startTokenIndex);
      int right = (int) (rightLong - startTokenIndex);

      for (SAnnotation anno : span.getSAnnotations()) {
        ArrayList<Row> rows = rowsByAnnotation.get(anno.getQName());
        if (rows == null) {
          // try again with only the name
          rows = rowsByAnnotation.get(anno.getSName());
        }
        if (rows != null) {
          // only do something if the annotation was defined before

          // 1. give each annotation of each span an own row
          Row r = new Row();

          String id = "event_" + eventCounter++;
          GridEvent event = new GridEvent(id, left, right,
                  anno.getSValueSTEXT());

          // check if the span is a matched node
          SFeature featMatched = span.getSFeature(ANNIS_NS, FEAT_MATCHEDNODE);
          Long match = featMatched == null ? null : featMatched.
                  getSValueSNUMERIC();
          event.setMatch(match);

          // calculate overlapped SToken
          EList<Edge> outEdges = graph.getOutEdges(span.getSId());
          if (outEdges != null) {
            for (Edge e : outEdges) {
              if (e instanceof SSpanningRelation) {
                SSpanningRelation spanRel = (SSpanningRelation) e;

                SToken tok = spanRel.getSToken();
                event.getCoveredIDs().add(tok.getSId());

                // get the STextualDS of this token and add it to the event
                EList<Edge> tokenOutEdges = graph.getOutEdges(tok.getSId());
                if (tokenOutEdges != null) {
                  for (Edge tokEdge : tokenOutEdges) {
                    if (tokEdge instanceof STextualRelation) {
                      event.setTextID(((STextualRelation) tokEdge).
                              getSTextualDS().getSId());
                      break;
                    }
                  }
                }
              }
            }
          }


          // try to get time annotations
          double[] startEndTime = TimeHelper.getOverlappedTime(span);
          if (startEndTime.length == 1) {
            event.setStartTime(startEndTime[0]);
          } else if (startEndTime.length == 2) {
            event.setStartTime(startEndTime[0]);
            event.setEndTime(startEndTime[1]);
          }

          r.addEvent(event);
          rows.add(r);

          String page = pageNumberHelper.getPageFromAnnotation(span);
          if (page != null) {
            event.setPage(page);
          }
        }
      } // end for each annotation of span
    } // end for each span

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

    // 4. split up events if they have gaps
    for (Map.Entry<String, ArrayList<Row>> e : rowsByAnnotation.entrySet()) {
      for (Row r : e.getValue()) {
        splitRowsOnGaps(r, graph, startTokenIndex, endTokenIndex);
      }
    }
    return rowsByAnnotation;
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
      return new LinkedList<String>();
    }

    SDocumentGraph graph = input.getDocument().getSDocumentGraph();

    Set<String> annoPool = getAnnotationLevelSet(graph, input.getNamespace(),
            type);
    List<String> annos = new LinkedList<String>(annoPool);

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

          LinkedList<String> matchingAnnos = new LinkedList<String>();
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
    Set<String> result = new TreeSet<String>();

    if (graph != null) {
      EList<? extends SNode> nodes;
      // catch most common cases directly
      if (SSpan.class == type) {
        nodes = graph.getSSpans();
      } else if (SToken.class == type) {
        nodes = graph.getSTokens();
      } else {
        nodes = graph.getSNodes();
      }
      if (nodes != null) {
        for (SNode n : nodes) {
          if (type.isAssignableFrom(n.getClass())) {
            for (SLayer layer : n.getSLayers()) {
              if (namespace == null || namespace.equals(layer.getSName())) {
                for (SAnnotation anno : n.getSAnnotations()) {
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
          long startTokenIndex, long endTokenIndex) {
    ListIterator<GridEvent> itEvents = row.getEvents().listIterator();
    while (itEvents.hasNext()) {
      GridEvent event = itEvents.next();

      int lastTokenIndex = Integer.MIN_VALUE;

      // sort the coveredIDs
      LinkedList<String> sortedCoveredToken = new LinkedList<String>(event.
              getCoveredIDs());
      Collections.sort(sortedCoveredToken, new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
          SNode node1 = graph.getSNode(o1);
          SNode node2 = graph.getSNode(o2);

          if (node1 == node2) {
            return 0;
          }
          if (node1 == null) {
            return -1;
          }
          if (node2 == null) {
            return +1;
          }

          long tokenIndex1 = node1.getSFeature(ANNIS_NS, FEAT_TOKENINDEX).
                  getSValueSNUMERIC();
          long tokenIndex2 = node2.getSFeature(ANNIS_NS, FEAT_TOKENINDEX).
                  getSValueSNUMERIC();

          return ((Long) (tokenIndex1)).compareTo(tokenIndex2);
        }
      });

      // first calculate all gaps
      List<GridEvent> gaps = new LinkedList<GridEvent>();
      for (String id : sortedCoveredToken) {

        SNode node = graph.getSNode(id);
        long tokenIndexRaw = node.getSFeature(ANNIS_NS, FEAT_TOKENINDEX).
                getSValueSNUMERIC();

        tokenIndexRaw = clip(tokenIndexRaw, startTokenIndex, endTokenIndex);

        int tokenIndex = (int) (tokenIndexRaw - startTokenIndex);
        int diff = tokenIndex - lastTokenIndex;

        if (lastTokenIndex >= 0 && diff > 1) {
          // we detected a gap
          GridEvent gap = new GridEvent(event.getId() + "_gap",
                  lastTokenIndex + 1, tokenIndex - 1, "");
          gap.setGap(true);
          gaps.add(gap);
        }

        lastTokenIndex = tokenIndex;
      } // end for each covered token id

      for (GridEvent gap : gaps) {
        // remember the old right value
        int oldRight = event.getRight();

        // shorten last event
        event.setRight(gap.getLeft() - 1);

        // insert the real gap
        itEvents.add(gap);

        // insert a new event node that covers the rest of the event
        GridEvent after = new GridEvent(event.getId() + "_after",
                gap.getRight() + 1, oldRight, event.getValue());
        after.getCoveredIDs().addAll(event.getCoveredIDs());
        itEvents.add(after);
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
