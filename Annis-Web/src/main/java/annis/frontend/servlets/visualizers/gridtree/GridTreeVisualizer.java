package annis.frontend.servlets.visualizers.gridtree;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aspectj.tools.ant.taskdefs.Ajdoc.Link;

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.model.Edge;

import annis.frontend.servlets.visualizers.WriterVisualizer;

/**
 * 
 * @author benjamin
 * 
 */

public class GridTreeVisualizer extends WriterVisualizer {

	/**
	 * This helper-class saves the span from a specific Node. The span is
	 * represented as tokenIndex from the most and the most right Token of the
	 * root.
	 * 
	 * @author benjamin
	 * 
	 */
	private class Span {

		Long left;
		Long right;
		AnnisNode root;

		/**
		 * left and right should be initiate with null, when root is not a
		 * token.
		 * 
		 * @param root
		 * @param r
		 *            must be a sorted List of the result
		 */
		public Span(AnnisNode root) {
			this.root = root;
		}
	}

	@Override
	public void writeOutput(Writer writer) {
		try {
			writer.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
			writer.append("<link href=\""
					+ getContextPath()
					+ "/css/visualizer/partitur.css\" rel=\"stylesheet\" type=\"text/css\" >");
			writer.append("<link href=\""
					+ getContextPath()
					+ "/css/visualizer/gridtree.css\" rel=\"stylesheet\" type=\"text/css\" >");
			writer.append("<body>");
			writer.append("<table class=\"grid-tree partitur_table\">\n");
			writer.append(findAnnotation("cat"));
			writer.append("</table>\n");
			writer.append("</body></html>");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String findAnnotation(String anno) {

		AnnotationGraph graph = getResult().getGraph();
		List<AnnisNode> nodes = graph.getNodes();
		List<AnnisNode> result = graph.getTokens();
		Set<AnnisNode> roots = new HashSet<AnnisNode>();

		for (AnnisNode n : nodes)
			if (hasAnno(n, anno))
				roots.add(n);

		StringBuffer sb = new StringBuffer();

		for (AnnisNode n : roots) {

			// catch the result
			Span span = new Span(n);
			getTokens(span);

			// print result
			String rootAnnotation = getAnnoValue(n, anno);
			htmlTableRow(sb, result, span, rootAnnotation);
		}

		htmlTableRow(sb, result);

		return sb.toString();
	};

	/**
	 * Returns the annotation of a {@link AnnisNode}
	 * 
	 * @param n
	 * @param anno
	 * @return null, if the annotation not exists.
	 */
	private String getAnnoValue(AnnisNode n, String anno) {

		for (Annotation a : n.getNodeAnnotations()) {
			if (a.getName().equals(anno))
				return a.getName() + " : " + a.getValue();
		}

		return " ";
	}

	/**
	 * Returns true when the node is annotated with the string. It is not
	 * sensitive to namespaces.
	 * 
	 * @param n
	 *            the node to check
	 * @param annotation
	 *            String to check, without namespaces
	 * @return
	 */
	private boolean hasAnno(AnnisNode n, String annotation) {

		Set<Annotation> annos = n.getNodeAnnotations();

		for (Annotation x : annos)
			if (x.getName().equals(annotation))
				return true;

		return false;
	}

	/**
	 * Steps from the root recursive through all children nodes to find the
	 * tokens.
	 * 
	 * @param n
	 *            is the root
	 * @param nodes
	 *            the references of the tokens
	 */
	private void getTokens(Span n) {
		Set<Edge> edges = n.root.getOutgoingEdges();

		for (Edge e : edges) {

			AnnisNode x;

			if ((x = e.getDestination()).isToken()) {

				Long tokenIndex = x.getTokenIndex();

				if (n.left == null)
					n.left = tokenIndex;
				else
					n.left = Math.min(n.left, tokenIndex);

				if (n.right == null)
					n.right = tokenIndex;
				else
					n.right = Math.max(n.right, tokenIndex);

			}

			else {
				n.root = e.getDestination();
				getTokens(n);
			}
		}
	}

	/**
	 * Build a html-table-row.
	 * 
	 * @param sb
	 *            the html-code, where the row is embedded
	 * @param result
	 *            List of all results, the list must be sorted by the
	 *            token-index
	 * @param s
	 *            Span object with left and right limit
	 * @param rootAnnotation
	 *            node, which dominated all members of s
	 */
	private void htmlTableRow(StringBuffer sb, List<AnnisNode> result, Span s,
			String rootAnnotation) {

		sb.append("<tr>\n");
		sb.append("<th>" + rootAnnotation + "</th>");

		// fill with empty cell
		for (long i = result.get(0).getTokenIndex(); i < s.left; i++) {
			sb.append("<td> </td>");
		}

		// build table-cell for span
		sb.append("<td colspan=\"" + (Math.abs(s.right - s.left) + 1)
				+ "\" class=\"gridtree-result\">" + rootAnnotation + "</td>");

		// fill with empty cells
		long lastTokenIndex = result.get(result.size() - 1).getTokenIndex();
		for (long i = s.right; i < lastTokenIndex; i++) {
			sb.append("<td> </td>");
		}
		sb.append("</tr>\n");
	}

	/**
	 * Build a simple html-row
	 * 
	 * @param sb
	 * @param result
	 */
	private void htmlTableRow(StringBuffer sb, List<AnnisNode> result) {

		sb.append("<tr>\n");
		sb.append("<th> tok </th>");

		for (AnnisNode n : result) {
			sb.append("<td>" + n.getSpannedText() + "</td>");
		}

		sb.append("</tr>\n");
	}
}
