package annis.frontend.servlets.visualizers.tree.backends.staticimg;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class GraphicsItemGroup extends AbstractImageGraphicsItem {

	@Override
	public Rectangle2D getBounds() {
		Rectangle2D r = new Rectangle2D.Double();
		for (AbstractImageGraphicsItem c: getChildren()) {
			Rectangle2D childBounds = c.getBounds();
			if (childBounds != null) {
				r.add(childBounds);
			}
		}
		return r;
	}

	@Override
	public void draw(Graphics2D canvas) {
	}
	
}
