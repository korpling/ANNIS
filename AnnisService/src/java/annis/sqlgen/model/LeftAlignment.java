package annis.sqlgen.model;

import annis.model.AnnisNode;

public class LeftAlignment extends Join {

	public LeftAlignment(AnnisNode target) {
		super(target);
	}
	
	@Override
	public String toString() {
		return "left aligned with node " + target.getId();
	}

}
