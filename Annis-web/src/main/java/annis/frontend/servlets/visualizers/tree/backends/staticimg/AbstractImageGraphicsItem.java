package annis.frontend.servlets.visualizers.tree.backends.staticimg;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import annis.frontend.servlets.visualizers.tree.GraphicsItem;

public abstract class AbstractImageGraphicsItem implements GraphicsItem {
	private List<AbstractImageGraphicsItem> children = new ArrayList<AbstractImageGraphicsItem>();
	private int zValue;
	
	public void addChildItem(AbstractImageGraphicsItem childItem) {
		children.add(childItem);
	}
	
	public abstract void draw(Graphics2D canvas);
	
	@Override
	public void setParentItem(GraphicsItem parent) {
		((AbstractImageGraphicsItem)parent).addChildItem(this);
	}
	
	@Override
	public void setZValue(int newZValue) {
		zValue = newZValue;
	}
	
	public int getZValue() {
		return zValue;
	}

	public Collection<AbstractImageGraphicsItem> getChildren() {
		return children;
	}

	public void getAllChildren(List<AbstractImageGraphicsItem> outputList) {
		outputList.addAll(children);
		for (AbstractImageGraphicsItem child: children) {
			child.getAllChildren(outputList);
		}
	}
}
