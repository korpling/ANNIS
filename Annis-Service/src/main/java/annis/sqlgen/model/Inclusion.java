package annis.sqlgen.model;

import annis.model.AnnisNode;

public class Inclusion extends Join {

	public Inclusion(AnnisNode target) {
		super(target);
	}
	
	@Override
	public String toString() {
		return "includes node " + target.getId();
	}

}
