package org.corpus_tools.annis.gui.visualizers.component.grid;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import java.util.Arrays;
import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.STimeOverlappingRelation;
import org.corpus_tools.salt.common.STimeline;
import org.corpus_tools.salt.common.STimelineRelation;
import org.corpus_tools.salt.core.GraphTraverseHandler;
import org.corpus_tools.salt.core.SGraph.GRAPH_TRAVERSE_TYPE;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;

public class TimelineSpanCollector implements GraphTraverseHandler {



  public static Range<Integer> getRange(SDocumentGraph graph, SNode node) {
    STimeline timeline = graph.getTimeline();
    Preconditions.checkNotNull(timeline);

    TimelineSpanCollector collector = new TimelineSpanCollector();
    graph.traverse(Arrays.asList(node), GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST,
        "TimelineSpanCollector", collector);

    OptionalInt start =
        collector.collectedRanges.stream().mapToInt((range) -> range.lowerEndpoint()).min();
    OptionalInt end =
        collector.collectedRanges.stream().mapToInt((range) -> range.upperEndpoint()).max();

    if (start.isPresent() && end.isPresent()) {
      return Range.closed(start.getAsInt(), end.getAsInt());
    } else {
      // Use the whole timeline as a fallback
      return Range.closed(timeline.getStart(), timeline.getEnd() - 1);
    }
  }

  public static Set<Range<Integer>> getAllRanges(SDocumentGraph graph, SNode node) {
    STimeline timeline = graph.getTimeline();
    Preconditions.checkNotNull(timeline);

    TimelineSpanCollector collector = new TimelineSpanCollector();
    graph.traverse(Arrays.asList(node), GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST,
        "TimelineSpanCollector", collector);

    return collector.collectedRanges;

  }

  private final HashSet<Range<Integer>> collectedRanges;

  private TimelineSpanCollector() {
    collectedRanges = new HashSet<>();
  }

  @Override
  public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode,
      @SuppressWarnings("rawtypes") SRelation relation, SNode fromNode, long order) {
    if (relation instanceof STimelineRelation) {
      STimelineRelation timelineRelation = (STimelineRelation) relation;
      this.collectedRanges
          .add(Range.closed(timelineRelation.getStart(), timelineRelation.getEnd() - 1));
    }

  }

  @Override
  public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode,
      SRelation<SNode, SNode> relation, SNode fromNode, long order) {

  }

  @Override
  public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
      SRelation<SNode, SNode> relation, SNode currNode, long order) {
    if (relation == null) {
      return true;
    } else if (relation instanceof STimeOverlappingRelation) {
      return true;
    }
    return false;
  }

}
