package annis.frontend.servlets.visualizers.tree.backends.staticimg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import annis.frontend.servlets.visualizers.tree.GraphicsBackend;
import annis.frontend.servlets.visualizers.tree.Shape;

public class Java2dBackend implements GraphicsBackend<AbstractImageGraphicsItem> {
	public static final FontRenderContext FRC = new FontRenderContext(new AffineTransform(), true, true);
	
	public static class Java2dFont implements GraphicsBackend.Font {
		
		private final java.awt.Font awtFont;

		public Java2dFont(String family, int pointSize, int style) {
			awtFont = new java.awt.Font(family, style, pointSize);
		}
		
		@Override
		public Rectangle2D extents(String string) {
			TextLayout tl = new TextLayout(string, awtFont, FRC);
			return tl.getBounds();
		}
		
		public java.awt.Font getAwtFont() {
			return awtFont;
		}

		@Override
		public double getLineHeight() {
			return lineMetrics().getHeight();
		}

		@Override
		public double getAscent() {
			return lineMetrics().getAscent();
		}

		private LineMetrics lineMetrics() {
			return awtFont.getLineMetrics("XgÜ", FRC);
		}
	};
	

	@Override
	public AbstractImageGraphicsItem group() {
		return new GraphicsItemGroup();
	}

	@Override
	public AbstractImageGraphicsItem makeLabel(String label, Point2D pos, Font font, Color color, Alignment alignment, Shape shape) {
		return new LabelItem(label, pos, (Java2dFont) font, color, alignment, shape);
	}

	@Override
	public Font getFont(String family, int pointSize, int style) {
		return new Java2dFont(family, pointSize, style);
	}

	@Override
	public AbstractImageGraphicsItem makeLines(final Collection<Line2D> lines, final Color color) {
		return new AbstractImageGraphicsItem() {
			
			@Override
			public Rectangle2D getBounds() {
				return null;
			}
			
			@Override
			public void draw(Graphics2D canvas) {
				canvas.setColor(color);
				canvas.setStroke(new BasicStroke(2));
				for (Line2D l: lines) {
					canvas.draw(l);
				}
			}
		};
	}
}
