package annis.sqlgen.model;

import annis.model.AnnisNode;

public class RightAlignment extends Join {

	public RightAlignment(AnnisNode target) {
		super(target);
	}
	
	@Override
	public String toString() {
		return "right aligned with node " + target.getId();
	}

}
