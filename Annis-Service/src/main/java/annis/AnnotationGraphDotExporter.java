package annis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import annis.model.AnnisNode;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import annis.model.Edge.EdgeType;

public class AnnotationGraphDotExporter {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private String path;
	
	public void writeDotFile(AnnotationGraph graph) {
		
		String filename = StringUtils.join(graph.getMatchedNodeIds(), "-");
		
		File dotFile = new File(path, filename + ".dot");
		PrintWriter w = null;
		try {
			w = new PrintWriter(new FileWriter(dotFile));
		} catch (IOException e) {
			log.warn("Couldn't write " + dotFile.getAbsolutePath());
		}
		
		w.println("digraph NAME {");

		w.println("\tordering = out;");

		// FIXME: suchergebnisse markieren
		// FIXME: root knoten auf eine ebene		
		w.print("\t{ rank = same; ");
		for (AnnisNode token : graph.getTokens()) {
			w.print(token.getName() + "; ");
		}
		w.println("}");
		for (AnnisNode token : graph.getTokens()) {
			w.println("\t\"" + token.getSpannedText() + "\" [shape=none];");
			w.println("\t" + token.getName() + "->" + "\"" + token.getSpannedText() + "\" [style=dotted];");
		}
		
		for (AnnisNode dst : graph.getNodes()) {
			
			if (graph.getMatchedNodeIds().contains(dst.getId())) {
				w.println("\t" + dst.getName() + "[style=filled,fillcolor=yellow]");
			}
			
			Set<AnnisNode> skipUnnamedDominance = new HashSet<AnnisNode>();;
			
			for (Edge edge : dst.getIncomingEdges()) {
				if (edge.getEdgeType() == EdgeType.DOMINANCE && edge.getName() != null)
					skipUnnamedDominance.add(edge.getSource());
			}
			
			for (Edge edge : dst.getIncomingEdges()) {
				// don't print the unnamed dominance edge if there are named ones 
				String name = edge.getName();
				EdgeType type = edge.getEdgeType();
				if (type == EdgeType.DOMINANCE && name == null && skipUnnamedDominance.contains(edge.getSource()))
					continue;
				
				AnnisNode src = edge.getSource(); 
				
				if (src != null) {
					w.print("\t" + src.getName() + " -> " + dst.getName());
					List<String> formats = new ArrayList<String>();
					if (name != null)
						formats.add("label=\"" + name + "\"");
					if (type == EdgeType.COVERAGE)
						formats.add("color=green");
					if (type == EdgeType.POINTING_RELATION)
						formats.add("color=blue");
					if ( ! formats.isEmpty() )
						w.print(" [" + StringUtils.join(formats, ",") + "] ");
					w.println(";");
				}
			}
		}
		
		w.println("}");

		w.close();
		
		log.debug("DOT file written to " + dotFile.getAbsolutePath());
	}
	
	///// Getter / Setter

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
}