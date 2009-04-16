package annis.sqlgen.model;

import annis.model.AnnisNode;


public class LeftDominance extends Dominance {

	public LeftDominance(AnnisNode target) {
		super(target, 1);
	}
	
	@Override
	public String toString() {
		return "left-dominates node " + target.getId();
	}

}
