package annis.sqlgen.model;

import annis.model.AnnisNode;

public class Dominance extends RankTableJoin {

	public Dominance(AnnisNode target) {
		this(target, null);
	}
	
	public Dominance(AnnisNode target, String name) {
		this(target, name, 0, 0);
	}
	
	public Dominance(AnnisNode target, int distance) {
		this(target, null, distance);
	}
	
	public Dominance(AnnisNode target, String name, int distance) {
		this(target, name, distance, distance);
	}
	
	public Dominance(AnnisNode target, int minDistance, int maxDistance) {
		this(target, null, minDistance, maxDistance);
	}
	
	public Dominance(AnnisNode target, String name, int minDistance, int maxDistance) {
		super(target, name, minDistance, maxDistance);
	}
	
	@Override
	public String toString() {
		return "dominates node " + target.getId() + " (" + name + ", " + minDistance + ", " + maxDistance + ")";
	}

}
