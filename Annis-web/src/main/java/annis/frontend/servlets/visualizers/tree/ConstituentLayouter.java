package annis.frontend.servlets.visualizers.tree;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import annis.frontend.servlets.visualizers.tree.GraphicsBackend.Alignment;
import annis.model.AnnisNode;
import annis.model.Edge;
import edu.uci.ics.jung.graph.DirectedGraph;
	

public class ConstituentLayouter<T extends GraphicsItem> {
	private class TreeLayoutData {
		private double baseline;
		private double ntStart;
		
		private T parentItem;
		
		private final Map<AnnisNode, Double> positions;
		private final VerticalOrientation orientation;
		private final List<Line2D> lines = new ArrayList<Line2D>(); 
		private final OrderedNodeList nodeList = new OrderedNodeList(styler.getVEdgeOverlapThreshold());
		
		public void setBaseline(double baseline) {
			this.baseline = baseline;
		}

		public TreeLayoutData(VerticalOrientation orientation_, Map<AnnisNode, Double> positions_) {
			positions = positions_;
			orientation = orientation_;
		}

		public double getYPosition(AnnisNode node) {
			return ntStart - orientation.value * dataMap.get(node).getHeight() * styler.getHeightStep();
		}
		
		public Point2D getTokenPosition(AnnisNode terminal) {
			return new Point2D.Double(positions.get(terminal), baseline);
		}

		public void addEdge(Point2D from, Point2D to) {
			getLines().add(new Line2D.Double(from, to));
		}
		
		public Point2D getDominanceConnector(AnnisNode node, Rectangle2D bounds) {
			if (node.isToken()) {
				return new Point2D.Double(bounds.getCenterX(), (orientation == VerticalOrientation.TOP_ROOT) ? bounds.getMinY() : bounds.getMaxY()); 
			} else {
				return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
			}
		}

		public void setParentItem(T parentItem) {
			this.parentItem = parentItem;
		}

		public T getParentItem() {
			return parentItem;
		}

		public void setNtStart(double ntStart) {
			this.ntStart = ntStart;
		}

		public double getNtStart() {
			return ntStart;
		}

		public List<Line2D> getLines() {
			return lines;
		}
		
		public OrderedNodeList getNodeList() {
			return nodeList;
		}
	}
	
	private static final AnnisNode TOKEN_NODE = new AnnisNode();

	static {
		TOKEN_NODE.setToken(true);
	}
	
	private final DirectedGraph<AnnisNode,Edge> graph;
	private final AnnisNode root;
	private final TreeElementLabeler labeler;
	private final GraphicsBackend<T> backend;
	private final Map<AnnisNode, NodeStructureData> dataMap;
	private final TreeElementStyler styler;
	
	public ConstituentLayouter(DirectedGraph<AnnisNode, Edge> graph_, GraphicsBackend<T> backend_, TreeElementLabeler labeler_, TreeElementStyler styler_) {
		this.backend = backend_;
		this.labeler = labeler_;
		this.graph = graph_;
		this.styler = styler_;
		root = findRoot();
		
		dataMap = new HashMap<AnnisNode, NodeStructureData>();
		fillHeightMap(root, 0, null);
		adaptNodeHeights();
	}

	private NodeStructureData fillHeightMap(AnnisNode node, int height, NodeStructureData parent) {
		if (node.isToken()) {
			NodeStructureData structureData = new NodeStructureData(parent);
			structureData.setChildHeight(0);
			structureData.setTokenArity(1);
			structureData.setLeftCorner(node.getTokenIndex().longValue());
			structureData.setRightCorner(node.getTokenIndex().longValue());
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
			for (AnnisNode n: graph.getSuccessors(node)) {
				NodeStructureData childData = fillHeightMap(n, height + 1, structureData); 
				maxH = Math.max(childData.getHeight(), maxH);
				leftCorner = Math.min(childData.getLeftCorner(), leftCorner);
				rightCorner = Math.max(childData.getRightCorner(), rightCorner);
				tokenArity += childData.getTokenArity();
				arity += 1;
				if (n.isToken()) {
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

	public void adaptNodeHeights() {
		/*
		Adapts node heights to prevent overlapping horizontal edges with discontinuous nodes.

		To avoid clashes, the `step` attribute of nodes is increased. Moved-up nodes will be
		rechecked at the next level.

		Algorithm outline
		=================
		1. Retrieve all nonterminals from `tree`
		2. If all nonterminals are continuous, stop here
		3. Set `level` to 1
		4. Get all nonterminals `level_nodes` whose height equals `level`
		5. Compare each node `a` from `level_nodes` to each other node `b`

		 * if the terminals of one node are completely inside another node, move up
   		   the enclosing node
		 * if the left/rightmost direct terminal children (not corners!) of `a` and `b` overlap,
   		   move up the node with less direct children

		6. Increase `level`
		7. Continue with 4 until there is a level for which no nodes are found.
		 */
		List<NodeStructureData> allNonterminals = new ArrayList<NodeStructureData>();
		boolean allContinuous = true;
		for (AnnisNode n: this.graph.getVertices()) {
			if (!n.isToken()) {
				allNonterminals.add(dataMap.get(n));
				allContinuous &= dataMap.get(n).isContinuous();
			}
		}
		if (allContinuous) {
			return;
		}
		for (int level = 1; ; level++) {
			List<NodeStructureData> levelNodes = new ArrayList<NodeStructureData>();
			for (NodeStructureData n: allNonterminals) {
				if (n.getHeight() == level) {
					levelNodes.add(n);
				}
			}
			if (levelNodes.isEmpty()) {
				return;
			}
			Collections.sort(levelNodes, new Comparator<NodeStructureData>() {
				@Override
				public int compare(NodeStructureData o1, NodeStructureData o2) {
					int o1k = o1.isContinuous() ? 1 : 0;
					int o2k = o2.isContinuous() ? 1 : 0;
					
					return o1k - o2k;
				}});
			int d = findFirstContinuous(levelNodes);
			/* d is either the index of the first continuous node,
		     * or len(level_nodes), if there are only discontinuous
		     * nodes.
		     * In any case, each combination of 2 nodes with at least
		     * one discontinuous node is checked exactly once. 
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

	private void bubbleNode(NodeStructureData iNode, NodeStructureData jNode) {
		if (iNode.getLeftCorner() < jNode.getLeftCorner() && jNode.getLeftCorner() < iNode.getRightCorner()) {
			if (jNode.getRightCorner() < iNode.getRightCorner()) {
				iNode.increaseStep();
			} else if (jNode.getLeftmostImmediate() < iNode.getRightmostImmediate()) {
				NodeStructureData x = (iNode.getArity() < jNode.getArity()) ? iNode : jNode;
				x.increaseStep();
			}
		} else if (jNode.getLeftCorner() < iNode.getLeftCorner() && jNode.getLeftCorner() < jNode.getRightCorner()) {
			if (iNode.getRightCorner() < jNode.getRightCorner()) {
				jNode.increaseStep();
			} else if (iNode.getLeftmostImmediate() < jNode.getRightmostImmediate()) {
				NodeStructureData x = (iNode.getArity() < jNode.getArity()) ? iNode : jNode;
				x.increaseStep();
			}
		}
	}

	private int findFirstContinuous(List<NodeStructureData> levelNodes) {
		for (int d = 0; d < levelNodes.size(); d++) {
			if (levelNodes.get(d).isContinuous()) {
				return d;
			}
		}
		return levelNodes.size();
	}

	private AnnisNode findRoot() {
		for (AnnisNode n: graph.getVertices()) {
			if (graph.getInEdges(n).isEmpty()) {
				return n;
			}
		}
		throw new RuntimeException();
	}

	private double computeTreeHeight() {
		return (dataMap.get(root).getHeight()) * styler.getHeightStep() + styler.getFont(TOKEN_NODE).getLineHeight() / 2;
	}
	
	private List<AnnisNode> getTokens(LayoutOptions options) {
		List<AnnisNode> tokens = new ArrayList<AnnisNode>();
		for (AnnisNode n: graph.getVertices()) {
			if (n.isToken()) {
				tokens.add(n);
			}
		}
		Collections.sort(tokens, options.getHorizontalOrientation().getComparator());					
		return tokens;
	}
	
	private Map<AnnisNode, Double> computeTokenPositions(LayoutOptions options, int padding) {
		Map<AnnisNode, Double> positions = new HashMap<AnnisNode, Double>();
		double x = 0;
		boolean first = true;
		
		List<AnnisNode> leaves = getTokens(options);
		GraphicsBackend.Font tokenFont = styler.getFont(leaves.get(0));
		
		for (AnnisNode token: leaves) {
			if (first) {
				first = false;
			} else {
				x += styler.getTokenSpacing();
			}
			positions.put(token, x);
			x += 2 * padding + tokenFont.extents(labeler.getLabel(token)).getWidth();
		}
		return positions;
	}
	
	public T createLayout(LayoutOptions options) {
		TreeLayoutData treeLayout = new TreeLayoutData(options.getOrientation(), computeTokenPositions(options, 5));
		
		treeLayout.setParentItem(backend.group());
		/* extra_count = self._labeler.extra_label_count(self._graph.id[0], NodeType.TERMINAL)
        tree_layout.extra_label_height = \
                   self._font(EXTRA_LABEL).height + 2 * TreeElements.LABEL_PADDING
		 */	
		if (options.getOrientation() == VerticalOrientation.TOP_ROOT) {
			treeLayout.setNtStart(computeTreeHeight());
			treeLayout.setBaseline(treeLayout.getNtStart() + styler.getFont(TOKEN_NODE).getLineHeight());
		} else {
			throw new RuntimeException();
			/*
            tree_layout.extra_start = 0
            tree_layout.baseline = extra_count * tree_layout.extra_label_height + self._font(NodeType.TERMINAL).height
            tree_layout.nt_start = tree_layout.baseline
			 */
		}
		calculateNodePosition(root, treeLayout, options);
		GraphicsItem edges = backend.makeLines(treeLayout.getLines(), styler.getEdgePen(null));
		edges.setZValue(-4);
		edges.setParentItem(treeLayout.getParentItem());
		//self._add_secedges(tree_layout, options)
		return treeLayout.getParentItem();
	}

	private Point2D calculateNodePosition(final AnnisNode current, TreeLayoutData treeLayout, LayoutOptions options) {
		double y = treeLayout.getYPosition(current);
		
		List<Edge> outEdges = new ArrayList<Edge>();
		outEdges.addAll(graph.getOutEdges(current));
		Collections.sort(outEdges, new Comparator<Edge>()  {
			@Override
			public int compare(Edge o1, Edge o2) {
				int h1 = dataMap.get(graph.getOpposite(current, o1)).getHeight();
				int h2 = dataMap.get(graph.getOpposite(current, o2)).getHeight();
				return h1 - h2;
			}
		});
        List<Double> childPositions = new ArrayList<Double>();
		
		for (Edge e: outEdges) {
			AnnisNode child = graph.getOpposite(current, e);
			Point2D childPos;
			if (child.isToken()) {
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
			
			String labelString = labeler.getLabel(e);
			
			GraphicsItem label = backend.makeLabel(
					labelString, new Point2D.Double(childPos.getX(), y + treeLayout.orientation.value * styler.getHeightStep() * 0.5), 
					styler.getFont(e), styler.getTextBrush(e), Alignment.CENTERED, styler.getShape(e));
			label.setZValue(10);
			label.setParentItem(treeLayout.parentItem);
		}
		
		double xCenter = treeLayout.getNodeList().findBestPosition(dataMap.get(current), 
				Collections.min(childPositions), 
				Collections.max(childPositions));

		GraphicsItem label = backend.makeLabel(
				labeler.getLabel(current), new Point2D.Double(xCenter, y), 
				styler.getFont(current), styler.getTextBrush(current), Alignment.CENTERED, styler.getShape(current));
		
		label.setZValue(11);
        label.setParentItem(treeLayout.getParentItem());
        treeLayout.addEdge(new Point2D.Double(Collections.min(childPositions), y), new Point2D.Double(Collections.max(childPositions), y));
		return treeLayout.getDominanceConnector(current, label.getBounds());
	}
	
/*
    def _add_secedges(self, tree_layout, options):
        for origin_id, target_id, edge in self._graph.edges_of(EdgeType.SECONDARY):
            origin_layout = self.layout_data(origin_id)
            target_layout = self.layout_data(target_id)

            if origin_layout.hidden or target_layout.hidden:
                continue

            curve = graphics.secedge_curve(tree_layout.orientation.value, origin_layout.rect, target_layout.rect)

            secedge = self._backend.cubic_curve(curve, TreeElements.SECEDGE_PEN)
            secedge.setZValue(-2)

            target_arrow = self._backend.arrow(
                curve[-1], curve[-2],
                graphics.Pen(graphics.Colors.INVISIBLE, 0, []),
                graphics.Brush(TreeElements.SECONDARY_EDGE_COLOR),
                graphics.Rectangle(0, 0, 8, 8))
            target_arrow.setZValue(-1)

            if options.draw_edge_labels:
                label_pos = graphics.bezier_point(curve, 0.8)
                shape, brush = self._label_style(edge)
                secedge_label = self._backend.label(
                    self._labeler.label(edge, corpus_id = origin_id[0]), label_pos,
                    self._font(edge.TYPE), shape, (0.5, 0.5), brush)
                secedge_label.setParentItem(secedge)

            target_arrow.setParentItem(secedge)
            secedge.setParentItem(tree_layout.parent_item)

 */

	private Point2D addTerminalNode(AnnisNode terminal, TreeLayoutData treeLayout) {
		GraphicsItem label = backend.makeLabel(
				labeler.getLabel(terminal), treeLayout.getTokenPosition(terminal), styler.getFont(terminal), 
				styler.getTextBrush(terminal), Alignment.NONE, styler.getShape(terminal));
		label.setParentItem(treeLayout.getParentItem());
		//
        //    for i, lstr in enumerate(self._labeler.extra_labels(token)[::tree_layout.orientation.value]):
        //        extra_lbox = self._backend.label(
        //            lstr,
        //            (lbox.rect.x_mid, tree_layout.extra_start + (i + 1) * tree_layout.extra_label_height),
        //            self._font(EXTRA_LABEL),
        //            TreeElements.ADD_TERMINAL_LABEL_SHAPE, (0.5, 0))
        //        extra_lbox.setParentItem(tree_layout.parent_item)
		//child_layout.orientation = tree_layout.orientation
		return treeLayout.getDominanceConnector(terminal, label.getBounds());
	}
}	
