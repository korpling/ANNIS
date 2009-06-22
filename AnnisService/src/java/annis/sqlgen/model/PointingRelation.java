package annis.sqlgen.model;

import annis.model.AnnisNode;

// FIXME: same as Dominance, abstract range information and refactor generation code in ClauseAnalysis
public class PointingRelation extends RankTableJoin {

	public PointingRelation(AnnisNode target, String name) {
		this(target, name, 0, 0);
	}
	
	public PointingRelation(AnnisNode target, String name, int distance) {
		this(target, name, distance, distance);
	}
	
	public PointingRelation(AnnisNode target, String name, int minDistance, int maxDistance) {
		super(target, name, minDistance, maxDistance);
	}
	
	@Override
	public String toString() {
		return "points to node " + target.getId() + " (" + name + ", " + minDistance + ", " + maxDistance + ")";
	}

}
