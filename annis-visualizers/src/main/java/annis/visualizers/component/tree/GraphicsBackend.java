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
package annis.visualizers.component.tree;

import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.CubicCurve2D;
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
	
	/**
	 * Creates a new label item.
	 * 
	 * @param label The label string.
	 * @param pos The label position.
	 * @param font Font to be used for rendering.
	 * @param color The text color. 
	 * @param alignment Alignment of the label string with regard to <code>pos</code>.
	 * @param shape The shape surrounding the label string (border, background color)
	 * @return a new label item
	 */
	T makeLabel(String label, Point2D pos, Font font, Color color, Alignment alignment, Shape shape);
	T makeLines(Collection<Line2D> lines, Color color, Stroke strokeStyle);

	/**
	 * Creates a new item that draws a cubic curve.
	 * 
	 * @param curveData The curve data.
	 * @param strokeStyle The stroking style for the curve.
	 * @param color The stroke color. 
	 * @return a new graphics item.
	 */
	T cubicCurve(CubicCurve2D curveData, Stroke strokeStyle, Color color);

	/**
	 * Creates a new arrow item.
	 * 
	 * @param tip The point of the arrow's tip.
	 * @param fromDirection The point that specifies the direction from which the arrow is pointing.
	 * @param dimensions The dimensions (width, height) of the arrow.
	 * @param fillColor The fill color.
	 * 
	 * @return a new graphics item.
	 */
	T arrow(Point2D tip, Point2D fromDirection, Rectangle2D dimensions, Color fillColor);
}
