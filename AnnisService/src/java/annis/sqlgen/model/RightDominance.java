package annis.sqlgen.model;

import annis.model.AnnisNode;


public class RightDominance extends Dominance {

	public RightDominance(AnnisNode target) {
		super(target, 1);
	}
	
	@Override
	public String toString() {
		return "right-dominates node " + target.getId();
	}
	
}
