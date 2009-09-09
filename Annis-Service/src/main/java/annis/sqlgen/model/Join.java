package annis.sqlgen.model;

import annis.model.AnnisNode;
import annis.model.DataObject;

@SuppressWarnings("serial")
public abstract class Join extends DataObject {

	protected AnnisNode target;

	public Join(AnnisNode target) {
		this.target = target;
	}

	public AnnisNode getTarget() {
		return target;
	}

	public void setTarget(AnnisNode target) {
		this.target = target;
	}
	
}