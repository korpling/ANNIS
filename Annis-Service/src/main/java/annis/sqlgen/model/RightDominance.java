package annis.sqlgen.model;

import annis.model.AnnisNode;


@SuppressWarnings("serial")
public class RightDominance extends Dominance {

	public RightDominance(AnnisNode target) {
		super(target);
	}
	
	public RightDominance(AnnisNode target, String name) {
		super(target, name, 1);
	}
	
	@Override
	public String toString() {
		return "right-dominates node " + target.getId() + " (" + name + ")";
	}
	
}
