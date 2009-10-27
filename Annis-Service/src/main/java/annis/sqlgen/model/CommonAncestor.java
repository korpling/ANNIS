package annis.sqlgen.model;

import annis.model.AnnisNode;

public class CommonAncestor extends Dominance {

	public CommonAncestor(AnnisNode target) {
		super(target);
	}

	public CommonAncestor(AnnisNode target, String name) {
		super(target, name);
	}
	
	@Override
	public String toString() {
		return "shares ancestor with node " + target.getId() + " (" + name + ")";
	}


}
