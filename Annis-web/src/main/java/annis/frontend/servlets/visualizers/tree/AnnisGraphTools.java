/**
 * 
 */
package annis.frontend.servlets.visualizers.tree;

import java.util.ArrayList;
import java.util.List;

import annis.model.AnnisNode;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

class AnnisGraphTools {
	public static final String PRIMEDGE_SUBTYPE = "edge";
	public static final String SECEDGE_SUBTYPE = "secedge";

	public List<DirectedGraph<AnnisNode, Edge>> getSyntaxGraphs(AnnotationGraph ag, String namespace) {
		List<DirectedGraph<AnnisNode, Edge>> resultGraphs = new ArrayList<DirectedGraph<AnnisNode, Edge>>();
		for (AnnisNode n: ag.getNodes()) {
			if (isRootNode(n, namespace)) {
				resultGraphs.add(extractGraph(ag, n));
			}
		}
		return resultGraphs;
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

	private boolean isRootNode(AnnisNode n, String namespace) {
		if (!n.getNamespace().equals(namespace)) {
			return false;
		}
		for (Edge e: n.getIncomingEdges()) {
			if (hasEdgeSubtype(e, AnnisGraphTools.PRIMEDGE_SUBTYPE) && e.getSource() != null) {
				return false;
			}
		}
		return true;
	}

	private DirectedGraph<AnnisNode, Edge> extractGraph(AnnotationGraph ag,
			AnnisNode n) {
		DirectedGraph<AnnisNode, Edge> graph = new DirectedSparseGraph<AnnisNode, Edge>();
		copyNode(graph, n);
		for (Edge e: ag.getEdges()) {
			if (hasEdgeSubtype(e, AnnisGraphTools.SECEDGE_SUBTYPE) && 
					graph.containsVertex(e.getDestination()) &&
					graph.containsVertex(e.getSource())) {
				graph.addEdge(e, e.getSource(), e.getDestination());
			}
		}
		return graph;
	}
	
	private boolean includeEdge(Edge e) {
		return hasEdgeSubtype(e, AnnisGraphTools.PRIMEDGE_SUBTYPE) && e.getNamespace() != null;
	}
	
	public static boolean hasEdgeSubtype(Edge e, String edgeSubtype) {
		String name = e.getName();
		return e.getEdgeType() == Edge.EdgeType.DOMINANCE && name != null && name.equals(edgeSubtype);
	}
	
	public static HorizontalOrientation detectLayoutDirection(AnnotationGraph ag) {
		int withHebrew = 0;
		for (AnnisNode token: ag.getTokens()) {
			if (isHebrewToken(token.getSpannedText())) {
				withHebrew += 1;
			}
		}
		return (withHebrew > ag.getTokens().size() / 3)
		        ? HorizontalOrientation.RIGHT_TO_LEFT
				: HorizontalOrientation.LEFT_TO_RIGHT;
	}

	private static boolean isHebrewToken(String text) {
		for (int i = 0; i < text.length(); ++i) {
			char c = text.charAt(i);
			if (c >= 0x0590 && c <= 0x05ff) {
				return true;
			}
		}
		return false;
	}
}