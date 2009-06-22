package annis.sqlgen.model;

import annis.model.AnnisNode;

public class Sibling extends Dominance {

	public Sibling(AnnisNode target) {
		super(target);
	}
	
	@Override
	public String toString() {
		return "sibling of node " + target.getId();
	}

}
