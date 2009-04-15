package annis.frontend.servlets.visualizers;

import java.io.IOException;
import java.io.Writer;

public class PaulaVisualizer extends Visualizer {

	@Override
	public void writeOutput(Writer writer) {
		try {
			writer.append("<html><head><style> body { font-family: verdana, arial; font-size: 10px; } </style><body>");
			writer.append(this.paula.replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>").replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;") + "</body></html>");		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
