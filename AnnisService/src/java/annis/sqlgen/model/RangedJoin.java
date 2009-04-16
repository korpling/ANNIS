package annis.sqlgen.model;

import annis.model.AnnisNode;

public abstract class RangedJoin extends Join {

	protected int minDistance;
	protected int maxDistance;

	public RangedJoin(AnnisNode target, int minDistance, int maxDistance) {
		super(target);
		this.minDistance = minDistance;
		this.maxDistance = maxDistance;		
	}

	public int getMinDistance() {
		return minDistance;
	}

	public int getMaxDistance() {
		return maxDistance;
	}

}