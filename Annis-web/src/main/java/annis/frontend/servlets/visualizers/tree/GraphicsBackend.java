package annis.frontend.servlets.visualizers.tree;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;


public interface GraphicsBackend<T extends GraphicsItem> {
	class Alignment {
		public static final Alignment NONE  = new Alignment(0, 0);
		public static final Alignment CENTERED = new Alignment(0.5, 0.5);
		
		private final double x;
		private final double y;
		
		public Alignment(double x, double y) {
			super();
			this.x = x;
			this.y = y;
		}

		public double getXAlign() {
			return x;
		}
		
		public double getYAlign() {
			return y;
		}
	}
	
	interface Font {
		Rectangle2D extents(String string);
		double getLineHeight();
		public double getAscent();
	}
	
	/**
	 * Returns a backend font object.
	 * 
	 * @param family The font family
	 * @param pointSize The point size
	 * @param style The style constant, as given by the constants in {@link java.awt.Font}.
	 * @return a backend font object.
	 */
	Font getFont(String family, int pointSize, int style);
	
	/**
	 * Creates a new grouping item. Grouping items do not have any content and are just used to 
	 * bundle together other items.
	 * 
	 * @return a new grouping item.
	 */
	T group();
	T makeLabel(String label, Point2D pos, Font font, Color color, Alignment centered, Shape shape);
	T makeLines(Collection<Line2D> lines, Color color);
}
