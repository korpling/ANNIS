package org.corpus_tools.annis.gui.util;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.corpus_tools.annis.gui.objects.AnnisConstants;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SDominanceRelation;
import org.corpus_tools.salt.common.SOrderRelation;
import org.corpus_tools.salt.common.SPointingRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.SStructuredNode;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STimeline;
import org.corpus_tools.salt.common.STimelineRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.GraphTraverseHandler;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SGraph.GRAPH_TRAVERSE_TYPE;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;

/**
 * Allows to reconstruct a proper {@link SDocumentGraph} with an {@link STimeline} and several
 * {@link SToken} connected by {@link SOrderRelation} from of a virtual tokenization.
 * 
 * @author Thomas Krause <thomaskrause@posteo.de>
 *
 */
public class TimelineReconstructor {

  private final class SpanToTokenTraverser implements GraphTraverseHandler {
    @Override
    public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode,
        SRelation relation, SNode fromNode, long order) {
      if (relation instanceof SOrderRelation && currNode instanceof SSpan) {
        String orderName = ((SOrderRelation) relation).getType();
        if (fromNode != null) {
          // add a space to the text
          StringBuilder t = textDataByName.get(orderName);
          if (t != null) {
            t.append(" ");
          }
        }
        convertSpanToToken((SSpan) currNode, orderName);
      }
    }

    @Override
    public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode,
        SRelation relation, SNode fromNode, long order) {
      // We don't need to handle this case, because we already do have all information
      // needed in nodedReached()
    }

    @Override
    public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
        SRelation relation, SNode currNode, long order) {
      return relation == null || relation instanceof SOrderRelation;
    }
  }

  private final SDocumentGraph graph;

  private final Map<String, STextualDS> textsByName = new HashMap<>();
  private final Map<String, StringBuilder> textDataByName = new HashMap<>();
  private final boolean virtualTokenizationFromNamespace;
  private final Multimap<SStructuredNode, Integer> spans2TimelinePos = HashMultimap.create();

  private final Set<SNode> nodesToDelete = new HashSet<>();

  private final Multimap<String, String> order2spanAnnos = HashMultimap.create();

  private final Set<String> matchIDs;
  private final Map<String, String> oldID2newID;
  private final Set<String> segmentations;

  private TimelineReconstructor(SDocumentGraph graph, boolean virtualTokenizationFromNamespace,
      Set<String> segmentations) {
    this.virtualTokenizationFromNamespace = virtualTokenizationFromNamespace;
    this.graph = graph;
    this.segmentations = segmentations;

    this.oldID2newID = new HashMap<>();
    this.matchIDs = new HashSet<>();
    getMatchedNodes();
  }

  private TimelineReconstructor(SDocumentGraph graph, Set<String> segmentations,
      Map<String, String> spanAnno2order) {
    this.virtualTokenizationFromNamespace = false;
    this.graph = graph;
    this.segmentations = segmentations;

    this.oldID2newID = new HashMap<>();
    this.matchIDs = new HashSet<>();
    getMatchedNodes();

    if (spanAnno2order != null) {
      for (Map.Entry<String, String> e : spanAnno2order.entrySet()) {
        order2spanAnnos.put(e.getValue(), e.getKey());
      }
    }

  }

  private void getMatchedNodes() {
    SFeature matchids = graph.getFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_MATCHEDIDS);
    if (matchids != null) {
      String[] ids = matchids.getValue_STEXT().split(",");
      this.matchIDs.addAll(Arrays.asList(ids));
    }
  }

  private void addTimeline() {

    STimeline timeline = graph.createTimeline();
    for (SToken virtualTok : graph.getSortedTokenByText()) {
      timeline.increasePointOfTime();
      // find all spans that are connected to this token and are part of an SOrderRelation
      for (SRelation<?, ?> inRel : virtualTok.getInRelations()) {
        if (inRel instanceof SSpanningRelation) {
          SSpanningRelation spanRel = (SSpanningRelation) inRel;
          SSpan overlappingSpan = spanRel.getSource();
          if (overlappingSpan != null) {
            spans2TimelinePos.put(overlappingSpan, timeline.getEnd());
          }
        }
      }
      nodesToDelete.add(virtualTok);
    }
  }

  private void createTokenFromSOrder() {
    nodesToDelete.add(graph.getTextualDSs().get(0));

    Map<String, SSpan> rootNodes = new HashMap<>();

    // Find all spans with a (token) span annotation and a matching segmentation annotation
    for (String seg : this.segmentations) {
      for (SSpan n : graph.getSpans()) {
        if (n.getFeature("annis::tok") != null
            && n.getAnnotations().stream().anyMatch(a -> seg.equals(a.getName()))) {
          // check if there is no incoming SOrderRelation of the segmentation type
          boolean isRoot = true;
          for (SRelation<?, ?> inRel : n.getInRelations()) {
            if (inRel instanceof SOrderRelation && seg.equals(((SOrderRelation) inRel).getType())) {
              isRoot = false;
              break;
            }
          }
          if (isRoot) {
            rootNodes.put(seg, n);
          }
        }
      }
    }

    // convert all root nodes to spans
    for (Map.Entry<String, SSpan> rootEntry : rootNodes.entrySet()) {
      SSpan root = rootEntry.getValue();
      String orderName = rootEntry.getKey();
      convertSpanToToken(root, orderName);
    }

    if (!rootNodes.isEmpty()) {
      // traverse through all SOrderRelations in order
      graph.traverse(new LinkedList<>(rootNodes.values()), GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST,
          "TimeReconstructSOrderRelations", new SpanToTokenTraverser());

    }
    // update the text of the TextualDSs
    for (Map.Entry<String, StringBuilder> textDataEntry : textDataByName.entrySet()) {
      STextualDS textDS = textsByName.get(textDataEntry.getKey());
      if (textDS != null) {
        textDS.setText(textDataEntry.getValue().toString());
      }
    }

  }

  private void convertSpanToToken(SSpan span, String orderName) {
    final Set<String> validSpanAnnos = new HashSet<>(order2spanAnnos.get(orderName));
    if (!nodesToDelete.contains(span)) {
      nodesToDelete.add(span);

      if (textsByName.get(orderName) == null) {
        STextualDS newText = graph.createTextualDS("");
        newText.setName(orderName);
        textsByName.put(orderName, newText);
        textDataByName.put(orderName, new StringBuilder());
      }
      STextualDS textDS = textsByName.get(orderName);
      StringBuilder textData = textDataByName.get(orderName);

      TreeSet<Integer> coveredIdx = new TreeSet<>(spans2TimelinePos.get(span));
      if (!coveredIdx.isEmpty()) {
        SAnnotation textValueAnno = getTextValueAnno(orderName, span);
        if (textValueAnno != null) {
          String textValue = textValueAnno.getValue_STEXT();

          int startTextIdx = textData.length();
          textData.append(textValue);
          int endTextIdx = textData.length();
          SToken newToken = graph.createToken(textDS, startTextIdx, endTextIdx);
          // keep track of changed ids for matches
          if (this.matchIDs.contains(span.getId())) {
            this.oldID2newID.put(span.getId(), newToken.getId());
          }

          moveLabels(span, newToken, orderName);

          STimelineRelation timeRel = SaltFactory.createSTimelineRelation();
          timeRel.setSource(newToken);
          timeRel.setTarget(graph.getTimeline());
          timeRel.setStart(coveredIdx.first());
          timeRel.setEnd(coveredIdx.last());

          graph.addRelation(timeRel);

          moveRelations(span, newToken, validSpanAnnos, orderName);
        }
      }
    }
  }

  private void moveLabels(SSpan span, SToken newToken, String orderName) {
    // move all features to the new token
    for (SFeature feat : span.getFeatures()) {
      if (!"salt".equals(feat.getNamespace())) {
        newToken.addFeature(feat);
      }
    }

    // move all annotations to the new token
    for (SAnnotation annot : span.getAnnotations()) {
      if (!"salt".equals(annot.getNamespace()) && !orderName.equals(annot.getName())) {
        newToken.addAnnotation(annot);
      }
    }
  }

  private SAnnotation getTextValueAnno(String orderName, SNode node) {
    SAnnotation result = null;

    Set<SAnnotation> annos = node.getAnnotations();
    if (annos != null) {
      for (SAnnotation a : annos) {
        if (a.getName().equals(orderName)) {
          result = a;
          break;
        }
      }
    }

    return result;
  }

  private void moveRelations(SSpan oldSpan, SToken newToken, Set<String> validSpanAnnos,
      String orderName) {
    @SuppressWarnings("rawtypes")
    final List<SRelation> inRels = new LinkedList<>(oldSpan.getInRelations());
    @SuppressWarnings("rawtypes")
    final List<SRelation> outRels = new LinkedList<>(oldSpan.getOutRelations());

    final List<SToken> coveredByOldSpan = new LinkedList<>();

    for (SRelation<?, ?> rel : outRels) {
      if (rel instanceof SPointingRelation) {
        SPointingRelation pointingRel = (SPointingRelation) rel;
        pointingRel.setSource(newToken);
      } else if (rel instanceof SSpanningRelation) {
        coveredByOldSpan.add(((SSpanningRelation) rel).getTarget());
      }
    }

    for (SRelation<?, ?> rel : inRels) {
      if (rel instanceof SPointingRelation) {
        SPointingRelation pointingRel = (SPointingRelation) rel;
        pointingRel.setTarget(newToken);
      } else if (rel instanceof SDominanceRelation) {
        SDominanceRelation domRel = (SDominanceRelation) rel;
        domRel.setTarget(newToken);
      }
    }

    // find the connected spans and connect them with the new token instead
    for (SToken tok : coveredByOldSpan) {
      if (tok.getInRelations() != null) {
        for (SRelation<?, ?> rel : tok.getInRelations()) {
          if (rel instanceof SSpanningRelation) {
            boolean valid = false;
            SSpan spanToMap = ((SSpanningRelation) rel).getSource();
            if (virtualTokenizationFromNamespace) {
              for (SAnnotation anno : spanToMap.getAnnotations()) {
                if (anno.getNamespace() != null && anno.getNamespace().equals(orderName)) {
                  valid = true;
                  break;
                }
              }
            } else {
              for (String validAnno : validSpanAnnos) {
                if (spanToMap.getAnnotation(validAnno) != null) {
                  valid = true;
                  break;
                }
              }
            }

            if (valid) {
              graph.createRelation(spanToMap, newToken, SALT_TYPE.SSPANNING_RELATION, null);
            }

          }
        }
      }
    }

  }


  private void cleanup() {
    for (SNode node : nodesToDelete) {
      graph.removeNode(node);
    }
    // update the feature matchedIDs
    SFeature matchids = graph.getFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_MATCHEDIDS);
    if (matchids != null) {
      String[] ids = matchids.getValue_STEXT().split(",");
      for (int i = 0; i < ids.length; i++) {
        if (this.oldID2newID.containsKey(ids[i]))
          ids[i] = this.oldID2newID.get(ids[i]);
      }
      matchids.setValue(Joiner.on(',').join(ids));
    }
  }



  /**
   * Removes the virtual tokenization from a {@link SDocumentGraph} and replaces it with an
   * {@link STimeline} and multiple {@link STextualDS}.
   * 
   * This alters the original graph.
   * 
   * @param graph The graph to add the timeline to
   * @param segmentations A set of known segmentations
   * @param spanAnno2order A mapping from annotation names to the corresponding segmentation name
   */
  public static void removeVirtualTokenization(SDocumentGraph graph, Set<String> segmentations,
      Map<String, String> spanAnno2order) {
    if (graph.getTimeline() != null) {
      // do nothing if the graph does not contain any virtual tokenization
      return;
    }

    TimelineReconstructor reconstructor =
        new TimelineReconstructor(graph, segmentations, spanAnno2order);
    reconstructor.addTimeline();
    reconstructor.createTokenFromSOrder();

    reconstructor.cleanup();

  }

  /**
   * Removes the virtual tokenization from a {@link SDocumentGraph} and replaces it with an
   * {@link STimeline} and multiple {@link STextualDS}.
   * 
   * This alters the original graph.
   * 
   * @param graph The graph to add the timeline to
   * @param segmentations A set of known segmentations
   */
  public static void removeVirtualTokenizationUsingNamespace(SDocumentGraph graph,
      Set<String> segmentations) {
    if (graph.getTimeline() != null) {
      // do nothing if the graph does not contain any virtual tokenization
      return;
    }

    TimelineReconstructor reconstructor = new TimelineReconstructor(graph, true, segmentations);
    reconstructor.addTimeline();
    reconstructor.createTokenFromSOrder();

    reconstructor.cleanup();

  }
}
