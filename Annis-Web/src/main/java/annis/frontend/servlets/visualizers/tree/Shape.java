/**
 * 
 */
package annis.frontend.servlets.visualizers.tree;

import java.awt.Color;
import java.awt.Stroke;

import annis.frontend.servlets.visualizers.tree.GraphicsBackend.Alignment;
import annis.frontend.servlets.visualizers.tree.GraphicsBackend.Font;

public interface Shape {
	double getXPadding();
	double getYPadding();
	
	double getInternalYOffset(String label, GraphicsBackend.Font font, GraphicsBackend.Alignment alignment);
	
	Color getStrokeColor();
	Color getFillColor();
	Stroke getPenStyle();
	
	abstract class AbstractShape implements Shape {
		private final double xPadding;
		private final double yPadding;
		private final Color stroke;
		private final Color fill;
		private final Stroke penStyle;

		public AbstractShape(double xPadding_, double yPadding_, Color stroke_,
				Color fill_, Stroke penStyle_) {
			this.xPadding = xPadding_;
			this.yPadding = yPadding_;
			this.stroke = stroke_;
			this.fill = fill_;
			this.penStyle = penStyle_;
		}


		@Override
		public double getXPadding() {
			return xPadding;
		}
		
		@Override
		public double getYPadding() {
			return yPadding;
		}

		public Color getStrokeColor() {
			return stroke;
		}

		public Color getFillColor() {
			return fill;
		}

		public Stroke getPenStyle() {
			return penStyle;
		}

		@Override
		public double getInternalYOffset(String label, Font font,
				Alignment alignment) {
			return font.extents(label).getHeight() * alignment.getYAlign();
		}
	}

	class Invisible extends AbstractShape {
		public Invisible(double padding) {
			super(padding, padding, null, null, null);
		}
	}
	
	class Rectangle extends AbstractShape {
		public Rectangle (Color stroke, Color fill, Stroke penStyle, double padding) {
			super(padding, padding, stroke, fill, penStyle);
		}
		
	}
	
	class Ellipse extends AbstractShape {
		public Ellipse (Color stroke, Color fill, Stroke penStyle, double padding) {
			super(padding + 4, padding, stroke, fill, penStyle);
		}

		@Override
		public double getInternalYOffset(String label, Font font,
				Alignment alignment) {
			return (font.getLineHeight() - font.getAscent() * alignment.getYAlign()) * alignment.getYAlign(); 
		}
	}
}