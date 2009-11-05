package annis.frontend.servlets.visualizers.tree;

import annis.model.AnnisNode;
import annis.model.Edge;

public interface TreeElementLabeler {
	String getLabel(AnnisNode n);
	String getLabel(Edge e);
}
