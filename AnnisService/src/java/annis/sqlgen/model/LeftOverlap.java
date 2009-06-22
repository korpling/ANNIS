package annis.sqlgen.model;

import annis.model.AnnisNode;

public class LeftOverlap extends Join {

	public LeftOverlap(AnnisNode target) {
		super(target);
	}
	
	@Override
	public String toString() {
		return "left overlaps node " + target.getId();
	}

}
