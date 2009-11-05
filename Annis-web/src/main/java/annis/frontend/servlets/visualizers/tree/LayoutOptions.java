package annis.frontend.servlets.visualizers.tree;

public class LayoutOptions {
	private final VerticalOrientation orientation;
	private final HorizontalOrientation h_orientation;
	
	public LayoutOptions(VerticalOrientation vor, HorizontalOrientation hor) {
		orientation = vor;
		h_orientation = hor;
	}
	
	public VerticalOrientation getOrientation() {
		return orientation;
	}
	
	public HorizontalOrientation getHorizontalOrientation() {
		return h_orientation;
	}
}
