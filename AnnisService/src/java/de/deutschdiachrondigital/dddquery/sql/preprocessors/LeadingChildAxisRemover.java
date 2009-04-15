package de.deutschdiachrondigital.dddquery.sql.preprocessors;

import org.apache.log4j.Logger;

import de.deutschdiachrondigital.dddquery.analysis.DepthFirstAdapter;
import de.deutschdiachrondigital.dddquery.node.AChildAxis;
import de.deutschdiachrondigital.dddquery.node.APathExpr;
import de.deutschdiachrondigital.dddquery.node.ARelativePathType;
import de.deutschdiachrondigital.dddquery.node.AStep;
import de.deutschdiachrondigital.dddquery.node.Start;

// TODO test
// TODO leere AliasSets entfernen
public class LeadingChildAxisRemover extends DepthFirstAdapter {

	private Logger log = Logger.getLogger(this.getClass());
	
	private int level;
	private int step;
	private boolean relative;
	
	@Override
	public void inStart(Start node) {
		level = 0;
	}
	
	@Override
	public void inAPathExpr(APathExpr node) {
		++level;
		step = 0;
		relative = node.getPathType() instanceof ARelativePathType;
	}
	
	@Override
	public void outAPathExpr(APathExpr node) {
		--level;
	}
	
	@Override
	public void inAStep(AStep node) {
		if (level == 1 && relative && step == 0 && node.getAxis() instanceof AChildAxis) {
			log.debug("removing leading child axis");
			
			node.setAxis(null);
		}
	}
	
	@Override
	public void outAStep(AStep node) {
		++step;
	}
	
}
