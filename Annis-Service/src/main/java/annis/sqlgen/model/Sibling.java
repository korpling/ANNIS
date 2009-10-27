package annis.sqlgen.model;

import annis.model.AnnisNode;

@SuppressWarnings("serial")
public class Sibling extends Dominance {

	public Sibling(AnnisNode target) {
		super(target);
	}
	
	public Sibling(AnnisNode target, String name) {
		super(target, name);
	}
	
	@Override
	public String toString() {
		return "sibling of node " + target.getId() + " (" + name + ")";
	}

}
