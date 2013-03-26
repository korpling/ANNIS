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

import annis.visualizers.component.tree.GraphicsBackend.Alignment;
import annis.visualizers.component.tree.GraphicsBackend.Font;
import java.awt.Color;
import java.awt.Stroke;

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