/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corpus_tools.annis.gui.visualizers.component.tree;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.util.Pair;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.corpus_tools.annis.gui.visualizers.VisualizerInput;
import org.corpus_tools.annis.gui.visualizers.component.tree.GraphicsBackend.Alignment;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.util.DataSourceSequence;


public class ConstituentLayouter<T extends GraphicsItem> {
  private class TreeLayoutData {
    private double baseline;
    private double ntStart;

    private T parentItem;

    private final Map<SNode, Double> positions;
    private final VerticalOrientation orientation;
    private final List<Line2D> lines = new ArrayList<Line2D>();
    private final OrderedNodeList nodeList = new OrderedNodeList(styler.getVEdgeOverlapThreshold());
    private final Map<SNode, Rectangle2D> rectangles = new HashMap<>();

    public TreeLayoutData(VerticalOrientation orientation_, Map<SNode, Double> positions_) {
      positions = positions_;
      orientation = orientation_;
    }

    public void addEdge(Point2D from, Point2D to) {
      getLines().add(new Line2D.Double(from, to));
    }

    public void addNodeRect(SNode node, Rectangle2D nodeRect) {
      rectangles.put(node, nodeRect);
    }

    public Point2D getDominanceConnector(SNode node, Rectangle2D bounds) {
      if (AnnisGraphTools.isTerminal(node, input)) {
        return new Point2D.Double(bounds.getCenterX(),
            (orientation == VerticalOrientation.TOP_ROOT) ? bounds.getMinY() : bounds.getMaxY());
      } else {
        return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
      }
    }

    public List<Line2D> getLines() {
      return lines;
    }

    public OrderedNodeList getNodeList() {
      return nodeList;
    }

    public double getNtStart() {
      return ntStart;
    }

    public VerticalOrientation getOrientation() {
      return orientation;
    }

    public T getParentItem() {
      return parentItem;
    }

    public Rectangle2D getRect(SNode source) {
      return rectangles.get(source);
    }

    public Point2D getTokenPosition(SNode terminal) {
      return new Point2D.Double(positions.get(terminal), baseline);
    }

    public double getYPosition(SNode node) {
      return ntStart - orientation.value * dataMap.get(node).getHeight() * styler.getHeightStep();
    }

    public void setBaseline(double baseline) {
      this.baseline = baseline;
    }

    public void setNtStart(double ntStart) {
      this.ntStart = ntStart;
    }

    public void setParentItem(T parentItem) {
      this.parentItem = parentItem;
    }
  }

  private static final SToken TOKEN_NODE = SaltFactory.createSToken();

  private final AnnisGraphTools GRAPH_TOOLS;


  private final DirectedGraph<SNode, SRelation> graph;
  private final SNode root;
  private final TreeElementLabeler labeler;
  private final GraphicsBackend<T> backend;
  private final Map<SNode, NodeStructureData> dataMap;
  private final TreeElementStyler styler;
  private final VisualizerInput input;

  public ConstituentLayouter(DirectedGraph<SNode, SRelation> graph_, GraphicsBackend<T> backend_,
      TreeElementLabeler labeler_, TreeElementStyler styler_, VisualizerInput input_,
      AnnisGraphTools graphTools) {
    this.backend = backend_;
    this.labeler = labeler_;
    this.graph = graph_;
    this.styler = styler_;
    this.input = input_;
    GRAPH_TOOLS = graphTools;
    root = findRoot();

    dataMap = new HashMap<>();
    fillHeightMap(root, 0, null);
    adaptNodeHeights();
  }

  public void adaptNodeHeights() {
    /*
     * Adapts node heights to prevent overlapping horizontal edges with discontinuous nodes.
     * 
     * To avoid clashes, the `step` attribute of nodes is increased. Moved-up nodes will be
     * rechecked at the next level.
     * 
     * Algorithm outline ================= 1. Retrieve all nonterminals from `tree` 2. If all
     * nonterminals are continuous, stop here 3. Set `level` to 1 4. Get all nonterminals
     * `level_nodes` whose height equals `level` 5. Compare each node `a` from `level_nodes` to each
     * other node `b`
     * 
     * if the terminals of one node are completely inside another node, move up the enclosing node
     * if the left/rightmost direct terminal children (not corners!) of `a` and `b` overlap, move up
     * the node with less direct children
     * 
     * 6. Increase `level` 7. Continue with 4 until there is a level for which no nodes are found.
     */
    List<NodeStructureData> allNonterminals = new ArrayList<NodeStructureData>();
    boolean allContinuous = true;
    for (SNode n : this.graph.getVertices()) {
      if (!AnnisGraphTools.isTerminal(n, input)) {
        allNonterminals.add(dataMap.get(n));
        allContinuous &= dataMap.get(n).isContinuous();
      }
    }
    if (allContinuous) {
      return;
    }
    for (int level = 1;; level++) {
      List<NodeStructureData> levelNodes = new ArrayList<NodeStructureData>();
      for (NodeStructureData n : allNonterminals) {
        if (n.getHeight() == level) {
          levelNodes.add(n);
        }
      }
      if (levelNodes.isEmpty()) {
        return;
      }
      Collections.sort(levelNodes, (o1, o2) -> {
        int o1k = o1.isContinuous() ? 1 : 0;
        int o2k = o2.isContinuous() ? 1 : 0;

        return o1k - o2k;
      });
      int d = findFirstContinuous(levelNodes);
      /*
       * d is either the index of the first continuous node, or levelNodes.size(), if there are only
       * discontinuous nodes. In any case, each combination of 2 nodes with at least one
       * discontinuous node is checked exactly once.
       */
      for (int i = 0; i < d; i++) {
        NodeStructureData iNode = levelNodes.get(i);

        for (int j = i + 1; j < levelNodes.size(); j++) {
          NodeStructureData jNode = levelNodes.get(j);
          if (iNode.getHeight() != jNode.getHeight()) {
            continue;
          }
          if (jNode.isContinuous()) {
            if (iNode.encloses(jNode)) {
              iNode.increaseStep();
              break;
            }
          } else {
            bubbleNode(iNode, jNode);
          }
        }
      }
    }
  }

  private void addSecEdges(TreeLayoutData treeLayout, LayoutOptions options) {
    for (SRelation<? extends SNode, ? extends SNode> e : graph.getEdges()) {
      if (!GRAPH_TOOLS.hasEdgeSubtype(e, GRAPH_TOOLS.getSecEdgeSubType())) {
        continue;
      }
      Rectangle2D sourceRect = treeLayout.getRect(e.getSource());
      Rectangle2D targetRect = treeLayout.getRect(e.getTarget());

      CubicCurve2D curveData = secedgeCurve(treeLayout.getOrientation(), sourceRect, targetRect);
      T secedgeElem =
          backend.cubicCurve(curveData, styler.getStroke(e, input), styler.getEdgeColor(e, input));
      secedgeElem.setZValue(-2);

      T arrowElem = backend.arrow(curveData.getP1(), curveData.getCtrlP1(),
          new Rectangle2D.Double(0, 0, 8, 8), styler.getEdgeColor(e, input));
      arrowElem.setZValue(-1);
      arrowElem.setParentItem(secedgeElem);

      Point2D labelPos = evaluate(curveData, 0.8);

      T label = backend.makeLabel(labeler.getLabel(e, input), labelPos, styler.getFont(e),
          styler.getTextBrush(e), Alignment.CENTERED, styler.getShape(e, input));
      label.setParentItem(secedgeElem);
      secedgeElem.setParentItem(treeLayout.getParentItem());

    }
  }

  private Point2D addTerminalNode(SNode terminal, TreeLayoutData treeLayout) {
    GraphicsItem label = backend.makeLabel(labeler.getLabel(terminal, input),
        treeLayout.getTokenPosition(terminal), styler.getFont(terminal, input),
        styler.getTextBrush(terminal, input), Alignment.NONE, styler.getShape(terminal, input));
    label.setParentItem(treeLayout.getParentItem());
    treeLayout.addNodeRect(terminal, label.getBounds());
    return treeLayout.getDominanceConnector(terminal, label.getBounds());
  }

  private void bubbleNode(NodeStructureData iNode, NodeStructureData jNode) {
    if (iNode.getLeftCorner() < jNode.getLeftCorner()
        && jNode.getLeftCorner() < iNode.getRightCorner()) {
      if (jNode.getRightCorner() < iNode.getRightCorner()) {
        iNode.increaseStep();
      } else if (jNode.getLeftmostImmediate() < iNode.getRightmostImmediate()) {
        NodeStructureData x = (iNode.getArity() < jNode.getArity()) ? iNode : jNode;
        x.increaseStep();
      }
    } else if (jNode.getLeftCorner() < iNode.getLeftCorner()
        && jNode.getLeftCorner() < jNode.getRightCorner()) {
      if (iNode.getRightCorner() < jNode.getRightCorner()) {
        jNode.increaseStep();
      } else if (iNode.getLeftmostImmediate() < jNode.getRightmostImmediate()) {
        NodeStructureData x = (iNode.getArity() < jNode.getArity()) ? iNode : jNode;
        x.increaseStep();
      }
    }
  }

  private Point2D calculateNodePosition(final SNode current, TreeLayoutData treeLayout,
      LayoutOptions options) {
    double y = treeLayout.getYPosition(current);

    List<Double> childPositions = new ArrayList<Double>();
    for (SRelation e : getOutgoingEdges(current)) {
      SNode child = graph.getOpposite(current, e);
      Point2D childPos;

      if (AnnisGraphTools.isTerminal(child, input)) {
        childPos = addTerminalNode(child, treeLayout);
      } else {
        childPos = calculateNodePosition(child, treeLayout, options);
      }
      childPositions.add(childPos.getX());

      NodeStructureData childData = dataMap.get(child);

      if (childData.canHaveVerticalOverlap()) {
        treeLayout.getNodeList().addVerticalEdgePosition(childData, childPos);
      }
      treeLayout.addEdge(new Point2D.Double(childPos.getX(), y), childPos);

      GraphicsItem label = backend.makeLabel(labeler.getLabel(e, input),
          new Point2D.Double(childPos.getX(),
              y + treeLayout.orientation.value * styler.getHeightStep() * 0.5),
          styler.getFont(e), styler.getTextBrush(e), Alignment.CENTERED, styler.getShape(e, input));

      label.setZValue(10);
      label.setParentItem(treeLayout.parentItem);
    }

    double xCenter = treeLayout.getNodeList().findBestPosition(dataMap.get(current),
        Collections.min(childPositions), Collections.max(childPositions));

    GraphicsItem label = backend.makeLabel(labeler.getLabel(current, input),
        new Point2D.Double(xCenter, y), styler.getFont(current, input),
        styler.getTextBrush(current, input), Alignment.CENTERED, styler.getShape(current, input));
    treeLayout.addNodeRect(current, label.getBounds());

    label.setZValue(11);
    label.setParentItem(treeLayout.getParentItem());
    treeLayout.addEdge(new Point2D.Double(Collections.min(childPositions), y),
        new Point2D.Double(Collections.max(childPositions), y));
    return treeLayout.getDominanceConnector(current, label.getBounds());
  }

  private Map<SNode, Double> computeTokenPositions(LayoutOptions options, int padding) {
    Map<SNode, Double> positions = new HashMap<>();
    double x = 0;
    boolean first = true;

    List<SNode> leaves = getTokens(options);
    if (leaves.isEmpty()) {
      throw new IllegalStateException("No terminal nodes found");
    }
    GraphicsBackend.Font tokenFont = styler.getFont(leaves.get(0), input);

    for (SNode token : leaves) {
      if (first) {
        first = false;
      } else {
        x += styler.getTokenSpacing();
      }
      positions.put(token, x);
      x += 2 * padding + tokenFont.extents(labeler.getLabel(token, input)).getWidth();
    }
    return positions;
  }

  private double computeTreeHeight() {
    return (dataMap.get(root).getHeight()) * styler.getHeightStep()
        + styler.getFont(TOKEN_NODE, input).getLineHeight() / 2;
  }

  public T createLayout(LayoutOptions options) {
    TreeLayoutData treeLayout =
        new TreeLayoutData(options.getOrientation(), computeTokenPositions(options, 5));

    treeLayout.setParentItem(backend.group());
    if (options.getOrientation() == VerticalOrientation.TOP_ROOT) {
      treeLayout.setNtStart(computeTreeHeight());
      treeLayout
          .setBaseline(treeLayout.getNtStart() + styler.getFont(TOKEN_NODE, input).getLineHeight());
    } else {
      treeLayout.setBaseline(styler.getFont(TOKEN_NODE, input).getLineHeight());
      treeLayout.setNtStart(styler.getFont(TOKEN_NODE, input).getLineHeight());
    }
    calculateNodePosition(root, treeLayout, options);
    SRelation e = getOutgoingEdges(root).get(0);
    GraphicsItem edges = backend.makeLines(treeLayout.getLines(), styler.getEdgeColor(e, input),
        styler.getStroke(e, input));
    edges.setZValue(-4);
    edges.setParentItem(treeLayout.getParentItem());
    addSecEdges(treeLayout, options);
    return treeLayout.getParentItem();
  }

  private Point2D evaluate(CubicCurve2D curveData, double t) {
    double u = 1 - t;
    return new Point2D.Double(
        curveData.getX1() * u * u * u + 3 * curveData.getCtrlX1() * t * u * u
            + 3 * curveData.getCtrlX2() * t * t * u + curveData.getX2() * t * t * t,
        curveData.getY1() * u * u * u + 3 * curveData.getCtrlY1() * t * u * u
            + 3 * curveData.getCtrlY2() * t * t * u + curveData.getY2() * t * t * t);
  }

  private NodeStructureData fillHeightMap(SNode node, int height, NodeStructureData parent) {
    if (AnnisGraphTools.isTerminal(node, input)) {

      DataSourceSequence seq = ((SDocumentGraph) node.getGraph())
          .getOverlappedDataSourceSequence(node, SALT_TYPE.STEXT_OVERLAPPING_RELATION).get(0);

      NodeStructureData structureData = new NodeStructureData(parent);
      structureData.setChildHeight(0);
      structureData.setTokenArity(1);
      structureData.setLeftCorner(seq.getStart().longValue());
      structureData.setRightCorner(seq.getStart().longValue());
      dataMap.put(node, structureData);
      return structureData;
    } else {
      int maxH = 0;
      long leftCorner = Integer.MAX_VALUE;
      long rightCorner = 0;
      boolean hasTokenChildren = false;
      long leftmostImmediate = Integer.MAX_VALUE;
      long rightmostImmediate = 0;
      int tokenArity = 0;
      int arity = 0;
      NodeStructureData structureData = new NodeStructureData(parent);
      for (SNode n : graph.getSuccessors(node)) {
        NodeStructureData childData = fillHeightMap(n, height + 1, structureData);
        maxH = Math.max(childData.getHeight(), maxH);
        leftCorner = Math.min(childData.getLeftCorner(), leftCorner);
        rightCorner = Math.max(childData.getRightCorner(), rightCorner);
        tokenArity += childData.getTokenArity();
        arity += 1;
        if (AnnisGraphTools.isTerminal(n, input)) {
          hasTokenChildren = true;
          leftmostImmediate = Math.min(leftmostImmediate, childData.getLeftCorner());
          rightmostImmediate = Math.max(rightmostImmediate, childData.getLeftCorner());
        }
      }
      structureData.setStep(1);
      structureData.setArity(arity);
      structureData.setTokenArity(tokenArity);
      structureData.setChildHeight(maxH);
      structureData.setLeftCorner(leftCorner);
      structureData.setRightCorner(rightCorner);
      structureData.setContinuous(tokenArity == rightCorner - leftCorner + 1);
      if (hasTokenChildren) {
        structureData.setLeftmostImmediate(leftmostImmediate);
        structureData.setRightmostImmediate(rightmostImmediate);
      }
      dataMap.put(node, structureData);
      return structureData;
    }
  }

  private Pair<RectangleSide> findBestConnection(Rectangle2D sourceRect, Rectangle2D targetRect) {
    // Initialize with with "from bottom to top side" to never return null values
    Pair<RectangleSide> result = new Pair<>(RectangleSide.BOTTOM, RectangleSide.TOP);
    double minDist = Float.MAX_VALUE;
    for (RectangleSide orig : RectangleSide.values()) {
      for (RectangleSide target : RectangleSide.values()) {
        Point2D o = sideMidPoint(sourceRect, orig);
        Point2D t = sideMidPoint(targetRect, target);
        double dist = Math.hypot(o.getX() - t.getX(), o.getY() - t.getY());
        if (dist < minDist) {
          result = new Pair<RectangleSide>(orig, target);
          minDist = dist;
        }
      }
    }
    return result;
  }

  private int findFirstContinuous(List<NodeStructureData> levelNodes) {
    for (int d = 0; d < levelNodes.size(); d++) {
      if (levelNodes.get(d).isContinuous()) {
        return d;
      }
    }
    return levelNodes.size();
  }

  private SNode findRoot() {
    for (SNode n : graph.getVertices()) {
      if (graph.getInEdges(n).isEmpty()) {
        return n;
      }
    }
    // This state is impossible to reach given graphs that are created by the TigerTreeVisualizer.
    throw new RuntimeException("Cannot find a root for the graph.");
  }

  private List<SRelation> getOutgoingEdges(final SNode current) {
    List<SRelation> outEdges = new ArrayList<>();
    for (SRelation e : graph.getOutEdges(current)) {
      if (GRAPH_TOOLS.hasEdgeSubtype(e, GRAPH_TOOLS.getPrimEdgeSubType())) {
        outEdges.add(e);
      }
    }
    Collections.sort(outEdges, (o1, o2) -> {
      int h1 = dataMap.get(graph.getOpposite(current, o1)).getHeight();
      int h2 = dataMap.get(graph.getOpposite(current, o2)).getHeight();
      return h1 - h2;
    });
    return outEdges;
  }

  private List<SNode> getTokens(LayoutOptions options) {
    List<SNode> tokens = new ArrayList<>();
    for (SNode n : graph.getVertices()) {
      if (AnnisGraphTools.isTerminal(n, input)) {
        tokens.add(n);
      }
    }
    Collections.sort(tokens, options.getHorizontalOrientation().getComparator());
    return tokens;
  }

  private CubicCurve2D secedgeCurve(VerticalOrientation verticalOrientation, Rectangle2D sourceRect,
      Rectangle2D targetRect) {
    Pair<RectangleSide> sidePair = findBestConnection(sourceRect, targetRect);

    Point2D startPoint = sideMidPoint(sourceRect, sidePair.getFirst());
    Point2D endPoint = sideMidPoint(targetRect, sidePair.getSecond());

    double middleX = (startPoint.getX() + endPoint.getX()) / 2.0;
    double middleY = 50 * -verticalOrientation.value + (startPoint.getY() + endPoint.getY()) / 2;
    return new CubicCurve2D.Double(startPoint.getX(), startPoint.getY(), middleX, middleY, middleX,
        middleY, endPoint.getX(), endPoint.getY());
  }

  private Point2D sideMidPoint(Rectangle2D rect, RectangleSide side) {
    switch (side) {
      case TOP:
        return new Point2D.Double(rect.getCenterX(), rect.getMinY());
      case BOTTOM:
        return new Point2D.Double(rect.getCenterX(), rect.getMaxY());
      case LEFT:
        return new Point2D.Double(rect.getMinX(), rect.getCenterY());
      case RIGHT:
        return new Point2D.Double(rect.getMaxX(), rect.getCenterY());
      default:
        throw new RuntimeException();
    }
  }
}
