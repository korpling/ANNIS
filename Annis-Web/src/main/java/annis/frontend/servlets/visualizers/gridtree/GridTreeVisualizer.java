package annis.frontend.servlets.visualizers.gridtree;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
			writer.append("<body>");
			writer.append("<table class=\"partitur_table\">\n");
			writer.append(findRoot());
			writer.append("</table>\n");
			writer.append("</body></html>");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String findRoot() {

		AnnotationGraph graph = getResult().getGraph();
		List<AnnisNode> nodes = graph.getNodes();
		List<AnnisNode> result = graph.getTokens();
		Set<AnnisNode> roots = new HashSet<AnnisNode>();

		for (AnnisNode n : nodes)
			if (hasAnno(n, "cat"))
				roots.add(n);

		StringBuffer sb = new StringBuffer();

		for (AnnisNode n : roots) {

			// catch the result
			Set<AnnisNode> tokens = new HashSet<AnnisNode>();
			getTokens(n, tokens);

			for (AnnisNode tok : tokens)
				tok.setMarker("db0505");

			// print result
			sb.append("<tr>\n");
			sb.append("<td>" + getAnnoValue(n, "cat") + "</td>");
			HTMLTableCell(sb, result, tokens);
			sb.append("</tr>\n");
		}

		return sb.toString();
	};

	private String getAnnoValue(AnnisNode n, String anno) {

		for (Annotation a : n.getNodeAnnotations()) {
			if (a.getName().equals(anno))
				return a.getName() + " : " + a.getValue();
		}

		return "this Annotation does not exist";
	}

	private boolean hasAnno(AnnisNode n, String annotation) {

		Set<Annotation> annos = n.getNodeAnnotations();

		for (Iterator<Annotation> it = annos.iterator(); it.hasNext();)
			if (it.next().getName().equals(annotation))
				return true;

		return false;
	}

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

	private void HTMLTableCell(StringBuffer sb, List<AnnisNode> result,
			Set<AnnisNode> ts) {
		for (AnnisNode n : result) {
			if (ts.contains(n))
				sb.append("<td>" + n.getSpannedText() + "</td>");
		}
	}
}
