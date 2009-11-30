package annis.frontend.servlets.visualizers.tree;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import annis.frontend.servlets.visualizers.Visualizer;
import annis.frontend.servlets.visualizers.tree.backends.staticimg.AbstractImageGraphicsItem;
import annis.frontend.servlets.visualizers.tree.backends.staticimg.Java2dBackend;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import annis.service.ifaces.AnnisResult;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;



public class TigerTreeVisualizer extends Visualizer {
	private static final String VISUALIZER_NAMESPACE = "tiger";
	private static final int SIDE_MARGIN = 20;
	private static final int TOP_MARGIN = 40;
	private static final int TREE_DISTANCE = 40;
	
	public static class DefaultStyler implements TreeElementStyler {
		public static final int LABEL_PADDING = 2;
		public static final int HEIGHT_STEP = 40;
		public static final int TOKEN_SPACING = 15;
		public static final int VEDGE_OVERLAP_THRESHOLD = 20;
		
		private final Java2dBackend backend;

		public DefaultStyler(Java2dBackend backend_) {
			backend = backend_;
		}

		public int getLabelPadding() {
			return LABEL_PADDING;
		}
		
		public GraphicsBackend.Font getFont(AnnisNode n) {
			if (n.isToken()) {
				return backend.getFont(Font.SANS_SERIF, 12, java.awt.Font.PLAIN);
			} else {
				return backend.getFont(Font.SANS_SERIF, 15, java.awt.Font.BOLD);
			}
			
		}
		
		public GraphicsBackend.Font getFont(Edge e) {
			return backend.getFont(Font.SANS_SERIF, 10, java.awt.Font.PLAIN);
		}

		@Override
		public Shape getShape(AnnisNode n) {
			if (n.isToken()) {
				return new Shape.Invisible(getLabelPadding());
			} else {
				return new Shape.Ellipse(Color.BLACK, Color.WHITE, new BasicStroke(1), getLabelPadding());
			}
		}

		@Override
		public Shape getShape(Edge e) {
			return new Shape.Rectangle(new Color(0.4f, 0.4f, 0.4f), Color.WHITE, new BasicStroke(1), getLabelPadding());
		}

		@Override
		public Color getTextBrush(AnnisNode n) {
			return Color.BLACK;
		}

		@Override
		public Color getTextBrush(Edge n) {
			return Color.BLACK;
		}

		@Override
		public int getHeightStep() {
			return HEIGHT_STEP;
		}

		@Override
		public Color getEdgePen(Edge n) {
			return new Color(0.3f, 0.3f, 0.3f);
		}

		@Override
		public int getTokenSpacing() {
			return TOKEN_SPACING;
		}

		@Override
		public int getVEdgeOverlapThreshold() {
			return VEDGE_OVERLAP_THRESHOLD;
		}
	}
	
	private static class DefaultLabeler implements TreeElementLabeler {

		@Override
		public String getLabel(AnnisNode n) {
			if (n.isToken()) 
      {
        String spannedText = n.getSpannedText();
        if(spannedText == null || "".equals(spannedText))
        {
          spannedText = " ";
        }
				return spannedText;
			} 
      else
      {
				return extractAnnotation(n.getNodeAnnotations(), VISUALIZER_NAMESPACE, "cat");
			}
		}

		@Override
		public String getLabel(Edge e) {
			return extractAnnotation(e.getAnnotations(), VISUALIZER_NAMESPACE, "func");
		}

		private String extractAnnotation(Set<Annotation> annotations, String namespace, String featureName) {
			for (Annotation a: annotations) {
				if (a.getNamespace().equals(namespace) && a.getName().equals(featureName)) {
					return a.getValue();
				}
			}
			return "--";
		}
	}

	private boolean isRootNode(AnnisNode n) {
		if (!n.getNamespace().equals(getNamespace())) {
			return false;
		}
		for (Edge e: n.getIncomingEdges()) {
			if (isDominanceEdge(e) && e.getSource() != null) {
				return false;
			}
		}
		return true;
	}

	private boolean isDominanceEdge(Edge e) {
		String name = e.getName();
		return e.getEdgeType() == Edge.EdgeType.DOMINANCE && name != null && name.equals("edge");
	}
	
	public List<DirectedGraph<AnnisNode, Edge>> getSyntaxGraphs(AnnotationGraph ag) {
		List<DirectedGraph<AnnisNode, Edge>> resultGraphs = new ArrayList<DirectedGraph<AnnisNode, Edge>>();
		for (AnnisNode n: ag.getNodes()) {
			if (isRootNode(n)) {
				resultGraphs.add(extractGraph(ag, n));
			}
		}
		return resultGraphs;
	}
	
	private DirectedGraph<AnnisNode, Edge> extractGraph(AnnotationGraph ag,
			AnnisNode n) {
		DirectedGraph<AnnisNode, Edge> graph = new DirectedSparseGraph<AnnisNode, Edge>();
		copyNode(graph, n);
		return graph;
	}
	
	private void copyNode(DirectedGraph<AnnisNode, Edge> graph, AnnisNode n) {
		graph.addVertex(n);
		for (Edge e: n.getOutgoingEdges()) {
			if (includeEdge(e)) {
				copyNode(graph, e.getDestination());
				graph.addEdge(e, n, e.getDestination());
			}
		}
	}

	private boolean includeEdge(Edge e) {
		return isDominanceEdge(e) && e.getNamespace() != null;
	}

	@Override
	public void writeOutput(OutputStream outstream) {
		AnnisResult result = getResult();
		List<AbstractImageGraphicsItem> layouts = new LinkedList<AbstractImageGraphicsItem>();
		
		Java2dBackend backend = new Java2dBackend();
		DefaultLabeler labeler = new DefaultLabeler();
		DefaultStyler styler = new DefaultStyler(backend);
		
		double width = 0;
		double maxheight = 0;
		
		for (DirectedGraph<AnnisNode, Edge> g: getSyntaxGraphs(result.getGraph())) {
			ConstituentLayouter<AbstractImageGraphicsItem> cl = new ConstituentLayouter<AbstractImageGraphicsItem>(
					g, backend, labeler, styler);
			AbstractImageGraphicsItem item = cl.createLayout(
					new LayoutOptions(VerticalOrientation.TOP_ROOT, HorizontalOrientation.LEFT_TO_RIGHT));

			Rectangle2D treeSize = item.getBounds();
			
			maxheight = Math.max(maxheight, treeSize.getHeight());
			width += treeSize.getWidth();
			layouts.add(item);
		}
		
		BufferedImage image = new BufferedImage(
				(int)(width + (layouts.size() - 1) * TREE_DISTANCE + 2 * SIDE_MARGIN),
				(int)(maxheight + 2 * TOP_MARGIN), BufferedImage.TYPE_INT_ARGB);
		Graphics2D canvas = createCanvas(image);
		double xOffset = SIDE_MARGIN;
		for (AbstractImageGraphicsItem item: layouts) {
			AffineTransform t = canvas.getTransform();
			Rectangle2D bounds = item.getBounds();
			canvas.translate(xOffset, TOP_MARGIN + maxheight - bounds.getHeight()); 
			renderTree(item, canvas);
			xOffset += bounds.getWidth() + TREE_DISTANCE; 
			canvas.setTransform(t);
		}
		
		try {
			ImageIO.write(image, "png", outstream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void renderTree(AbstractImageGraphicsItem item, Graphics2D canvas) {
		List<AbstractImageGraphicsItem> allItems = new ArrayList<AbstractImageGraphicsItem>();
		
		allItems.addAll(item.getChildren());
		
		Collections.sort(allItems, new Comparator<AbstractImageGraphicsItem>() {
			@Override
			public int compare(AbstractImageGraphicsItem o1,
					AbstractImageGraphicsItem o2) {
				return o1.getZValue() - o2.getZValue();
			}
		});
		
		for (AbstractImageGraphicsItem c: allItems) {
			c.draw(canvas);
		}
	}

	private Graphics2D createCanvas(BufferedImage image) {
		Graphics2D canvas = (Graphics2D)image.getGraphics();
		canvas.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		canvas.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		canvas.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		return canvas;
	}
	
	@Override
	public String getContentType() {
		return "image/png";
	}
	
	@Override
	public String getCharacterEncoding() {
		return "ISO-8859-1";
	}

}
