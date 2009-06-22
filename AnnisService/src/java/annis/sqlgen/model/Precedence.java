package annis.sqlgen.model;

import annis.model.AnnisNode;

public class Precedence extends RangedJoin {

	public Precedence(AnnisNode target) {
		this(target, 0, 0);
	}

	public Precedence(AnnisNode target, int distance) {
		this(target, distance, distance);
	}
	
	public Precedence(AnnisNode target, int minDistance, int maxDistance) {
		super(target, minDistance, maxDistance);
	}

	@Override
	public String toString() {
		return "precedes node " + target.getId() + " (" + minDistance + ", " + maxDistance + ")";
	}

}
