package annis.sqlgen.model;

import annis.model.AnnisNode;


@SuppressWarnings("serial")
public class LeftDominance extends Dominance {

	public LeftDominance(AnnisNode target) {
		super(target);
	}
	
	public LeftDominance(AnnisNode target, String name) {
		super(target, name, 1);
	}
	
	@Override
	public String toString() {
		return "left-dominates node " + target.getId() + " (" + name + ")";
	}

}
