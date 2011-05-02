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
			writer.append(findRoot("cat"));
			writer.append("</table>\n");
			writer.append("</body></html>");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String findRoot(String anno) {

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
			Set<AnnisNode> tokens = new HashSet<AnnisNode>();
			getTokens(n, tokens);

			// print result
			String rootAnnotation = getAnnoValue(n, anno);
			htmlTableRow(sb, result, tokens, rootAnnotation);
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
	private void getTokens(AnnisNode n, Set<AnnisNode> nodes) {
		Set<Edge> edges = n.getOutgoingEdges();

		for (Edge e : edges) {
			AnnisNode x;
			if ((x = e.getDestination()).isToken())
				nodes.add(x);
			else
				getTokens(x, nodes);
		}
	}

	/**
	 * Build a html-table-row.
	 * 
	 * @param sb
	 *            the html-code, where the row is embedded
	 * @param result List of all markable nodes
	 * @param s set of dominated nodes
	 * @param rootAnnotation node, which dominated all members of s
	 */
	private void htmlTableRow(StringBuffer sb, List<AnnisNode> result,
			Set<AnnisNode> s, String rootAnnotation) {

		int colspan = 0;

		sb.append("<tr>\n");
		sb.append("<th>" + rootAnnotation + "</th>");

		for (AnnisNode n : result) {

			if (s.contains(n) && colspan >= 0) {
				colspan++;
			}

			if (!s.contains(n) && colspan == 0)
				sb.append("<td> </td>");

			if (!s.contains(n) && colspan > 0) {

				// build table-cell for span
				sb.append("<td colspan=\"" + colspan
						+ "\" class=\"gridtree-result\">" + rootAnnotation
						+ "</td>");

				// build table-cell for current node
				sb.append("<td> </td>");
				colspan = 0;
			}
		}

		// build tag, if a span is not closed
		if (colspan > 0)
			sb.append("<td colspan=\"" + colspan
					+ "\" class=\"gridtree-result\">" + rootAnnotation
					+ "</td>");

		sb.append("</tr>\n");
	}

	/**
	 * Build a simple html-row
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
