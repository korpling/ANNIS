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

import annis.visualizers.component.tree.GraphicsBackend.Alignment;
import annis.visualizers.component.tree.Shape;
import annis.visualizers.component.tree.backends.staticimg.Java2dBackend.Java2dFont;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.font.GlyphMetrics;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class LabelItem extends AbstractImageGraphicsItem {
	private final java.awt.Font awtFont;
	private final String label;
	private final Point2D pos;
	private final Color color;
	private final Rectangle2D rect;
	private Shape shape;
	
	public LabelItem(String label_, Point2D pos_, Java2dFont font, Color color_, Alignment alignment, Shape shape_) {
		this.color = color_;
		this.label = label_;
		this.awtFont = font.getAwtFont();
		
		GlyphMetrics gm = awtFont.createGlyphVector(Java2dBackend.FRC, label.substring(0, 1)).getGlyphMetrics(0);
		
		Rectangle2D size = font.extents(label);

		double text_x = pos_.getX() - size.getWidth() * alignment.getXAlign();
		double text_y = pos_.getY() + shape_.getInternalYOffset(label_, font, alignment);
		double rect_y = pos_.getY() + size.getHeight() * alignment.getYAlign() - (font.getAscent() + shape_.getYPadding());
		
		rect = new Rectangle2D.Double(
				text_x + gm.getLSB() - shape_.getXPadding(),
				rect_y, 
				size.getWidth() + 2 * shape_.getXPadding(), 
				font.getLineHeight() + 2 * shape_.getYPadding());
		this.pos = new Point2D.Double(text_x -  rect.getX(), text_y - rect.getY());
		this.shape = shape_;
	}
	
	@Override
	public Rectangle2D getBounds() {
		return rect;
	}
	
	@Override
	public void draw(Graphics2D canvas) {
		if (shape instanceof Shape.Ellipse) {
			canvas.setStroke(shape.getPenStyle());
			canvas.setColor(shape.getFillColor());
			canvas.fillOval((int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight());
			canvas.setColor(shape.getStrokeColor());
			canvas.drawOval((int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight());
		} else if (shape instanceof Shape.Rectangle) {
			canvas.setStroke(shape.getPenStyle());
			canvas.setColor(shape.getFillColor());
			canvas.fillRect((int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight());
			canvas.setColor(shape.getStrokeColor());
			canvas.drawRect((int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight());
		}
		canvas.setFont(awtFont);
		canvas.setColor(color);
		canvas.drawString(this.label, (float)(this.pos.getX() + this.rect.getX()), (float)(this.pos.getY() + this.rect.getY()));
	}
}
