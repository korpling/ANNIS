package annis.sqlgen.model;

import annis.model.AnnisNode;

@Deprecated
public class DirectDominance extends Join {

	public DirectDominance(AnnisNode target) {
		super(target);
	}
	
	@Override
	public String toString() {
		return "directly dominates node " + target.getId();
	}

}
