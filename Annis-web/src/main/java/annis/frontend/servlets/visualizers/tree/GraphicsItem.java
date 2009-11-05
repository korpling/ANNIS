/**
 * 
 */
package annis.frontend.servlets.visualizers.tree;

import java.awt.geom.Rectangle2D;

public interface GraphicsItem {
	void setZValue(int zValue);
	void setParentItem(GraphicsItem parent);
	Rectangle2D getBounds();
	
}