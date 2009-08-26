package annis.sqlgen.model;

import annis.model.AnnisNode;

public class Overlap extends Join {

	public Overlap(AnnisNode target) {
		super(target);
	}
	
	@Override
	public String toString() {
		return "overlaps node " + target.getId();
	}

}
