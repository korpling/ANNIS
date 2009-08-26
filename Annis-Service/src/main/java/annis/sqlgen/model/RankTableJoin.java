package annis.sqlgen.model;

import annis.model.AnnisNode;

public abstract class RankTableJoin extends RangedJoin {

	protected String name;

	public RankTableJoin(AnnisNode target, String name, int minDistance, int maxDistance) {
		super(target, minDistance, maxDistance);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}