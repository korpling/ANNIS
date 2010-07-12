package annis.frontend.servlets.visualizers.tree;

public enum VerticalOrientation {
	TOP_ROOT(1),
	BOTTOM_ROOT(-1);

	final int value;
	
	private VerticalOrientation(int v) {
		value = v;
	}
	
}
