package annis.frontend.servlets.visualizers.gridtree;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.model.Edge;

import annis.frontend.servlets.visualizers.WriterVisualizer;

public class GridTreeVisualizer extends WriterVisualizer {

	@Override
	public void writeOutput(Writer writer) {
		try {
			writer.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");

			writer.append("<link href=\""
					+ getContextPath()
					+ "/css/visualizer/partitur.css\" rel=\"stylesheet\" type=\"text/css\" >");
			writer.append("<body>");
			writer.append("<table class=\"partitur_table\" dir=\"rtl\">\n");
			writer.append(this.test());
			writer.append("</table>");
			writer.append("</body></html>");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String test() {

		List<AnnisNode> l = getResult().getGraph().getTokens();
		StringBuffer sb = new StringBuffer();

		sb.append("<tr>");
		for (int i = 0; i < l.size(); i++) {
			sb.append("<td>" + l.get(i).getSpannedText() + "</td>");
		}		
		sb.append("</tr>");
		
		sb.append("<tr>");		
		for (int i = 0; i < l.size(); i++) {
			Set <Annotation> setanno = l.get(i).getNodeAnnotations();
			Annotation [] anno = new Annotation[setanno.size()];
			setanno.toArray(anno);
			sb.append("<td>" + anno[anno.length-1].getValue() + "</td>");
		}
		sb.append("</tr>");

		return sb.toString();
	}

}
