package annis.frontend.servlets.visualizers.tree;

import java.util.Comparator;

import annis.model.AnnisNode;

public enum HorizontalOrientation {
	LEFT_TO_RIGHT(1),
	RIGHT_TO_LEFT(-1);
	
	private final int directionModifier;
	
	HorizontalOrientation(int directionModifier_) {
		directionModifier = directionModifier_;
	}
	
	Comparator<AnnisNode> getComparator() {
		return new Comparator<AnnisNode>() {
			@Override
			public int compare(AnnisNode o1, AnnisNode o2) {
				return directionModifier * (o1.getTokenIndex().intValue() - o2.getTokenIndex().intValue());
			}
		};	
	}
}
