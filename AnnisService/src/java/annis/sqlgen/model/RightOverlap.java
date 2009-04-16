package annis.sqlgen.model;

import annis.model.AnnisNode;

public class RightOverlap extends Join {

	public RightOverlap(AnnisNode target) {
		super(target);
	}

	@Override
	public String toString() {
		return "right overlaps node " + target.getId();
	}
	
}
