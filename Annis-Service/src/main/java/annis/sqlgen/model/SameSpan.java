package annis.sqlgen.model;

import annis.model.AnnisNode;

public class SameSpan extends Join {

	public SameSpan(AnnisNode target) {
		super(target);
	}
	
	@Override
	public String toString() {
		return "same span as node " + target.getId();
	}

	
}
