package annis.frontend.servlets.visualizers.tree;

import java.awt.Color;
import java.awt.Stroke;

import annis.frontend.servlets.visualizers.tree.GraphicsBackend.Font;
import annis.model.AnnisNode;
import annis.model.Edge;

public interface TreeElementStyler {
	
	Font getFont(AnnisNode n);
	Font getFont(Edge e);
	
	Color getTextBrush(AnnisNode n);
	Color getTextBrush(Edge n);
	
	Color getEdgeColor(Edge n);
	Stroke getStroke(Edge n);
	
	Shape getShape(AnnisNode n);
	Shape getShape(Edge e);
	
	
	int getLabelPadding();
	int getHeightStep();
	int getTokenSpacing();
	int getVEdgeOverlapThreshold();
}
