/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.visualizers.component.tree.backends.staticimg;

import annis.visualizers.component.tree.GraphicsBackend;
import annis.visualizers.component.tree.Shape;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

public class Java2dBackend implements GraphicsBackend<AbstractImageGraphicsItem> {
	public static final FontRenderContext FRC = new FontRenderContext(new AffineTransform(), true, true);
	
	public static class Java2dFont implements GraphicsBackend.Font {
		
		private final java.awt.Font awtFont;

		public Java2dFont(String family, int pointSize, int style) {
			awtFont = new java.awt.Font(family, style, pointSize);
		}
		
		@Override
		public Rectangle2D extents(String string) {
			if (string.isEmpty()) {
				return new Rectangle2D.Double(0, 0, 0, 0); 
			} else {
				TextLayout tl = new TextLayout(string, awtFont, FRC);
				return tl.getBounds();
			}
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
	public AbstractImageGraphicsItem makeLines(final Collection<Line2D> lines, final Color color, final Stroke stroke) {
		return new AbstractImageGraphicsItem() {
			
			@Override
			public Rectangle2D getBounds() {
				return null;
			}
			
			@Override
			public void draw(Graphics2D canvas) {
				canvas.setColor(color);
				canvas.setStroke(stroke);
				for (Line2D l: lines) {
					canvas.draw(l);
				}
			}
		};
	}

	private double getRotationAngle(Point2D origin, Point2D target) {
		double l = Math.hypot(origin.getX() - target.getX(), origin.getY() - target.getY());
		double x = Math.acos((origin.getX() - target.getX()) * Math.signum(origin.getX() - target.getX()) / l);
		
		if (origin.getX() > target.getX()) {
			if (origin.getY() < target.getY()) {
				x = -x;
			}
			x += Math.PI;
		} else {
			if (origin.getY() > target.getY()) {
				x = -x;
			}
			
		}
		return x;
	}

	@Override
	public AbstractImageGraphicsItem arrow(final Point2D tip, Point2D fromPoint,
			Rectangle2D dimensions, final Color color) {
		
		final GeneralPath path = new GeneralPath();
		path.moveTo(0, 0);
		path.lineTo(dimensions.getHeight(), dimensions.getWidth() / 2);
		path.lineTo(dimensions.getHeight(), -dimensions.getWidth() / 2);
		path.closePath();
		final double angle = getRotationAngle(tip, fromPoint);

		return new AbstractImageGraphicsItem() {
			@Override
			public Rectangle2D getBounds() {
				return new Rectangle2D.Double(0, 0, 0, 0);
			}
			
			@Override
			public void draw(Graphics2D canvas) {
				AffineTransform t = canvas.getTransform();
				canvas.setColor(color);
				canvas.translate(tip.getX(), tip.getY());
				canvas.rotate(angle);
				canvas.fill(path);
				canvas.setTransform(t);
			}
		};
	}

	@Override
	public AbstractImageGraphicsItem cubicCurve(final CubicCurve2D curveData,
			final Stroke strokeStyle, final Color color) {
		return new AbstractImageGraphicsItem() {
			@Override
			public Rectangle2D getBounds() {
				return curveData.getBounds2D();
			}
			
			@Override
			public void draw(Graphics2D canvas) {
				canvas.setStroke(strokeStyle);
				canvas.setColor(color);
				canvas.draw(curveData);
			}
		};
	}
}
