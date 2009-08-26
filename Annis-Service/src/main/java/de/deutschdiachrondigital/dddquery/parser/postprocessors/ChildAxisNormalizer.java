package de.deutschdiachrondigital.dddquery.parser.postprocessors;

import org.apache.log4j.Logger;

import de.deutschdiachrondigital.dddquery.analysis.DepthFirstAdapter;
import de.deutschdiachrondigital.dddquery.node.AChildAxis;
import de.deutschdiachrondigital.dddquery.node.AStep;

/**
 * Parser post processor that normalizes child axis.
 * 
 * <p>
 * The input string <tt>a</tt> has no axis specified.  The DDDquery language definition states
 * that the axis of this input is <tt>child</tt>.  While this behavior could be achieved
 * with a modification of the SableCC definition file, it would conflict with the parsing
 * of string literals (see {@link StringLiteralNormalizer} for more information).  Instead
 * the SableCC parser leaves the axis unspecified.
 * 
 * <p>
 * If a step with an unspecified axis is found, the axis is set to <tt>child</tt>.
 * 
 * @author Viktor Rosenfeld <rosenfel@informatik.hu-berlin.de>
 */
public class ChildAxisNormalizer extends DepthFirstAdapter {

	private Logger log = Logger.getLogger(this.getClass());
	
	@Override
	public void inAStep(AStep node) {
		if (node.getAxis() == null) {
			
			log.info("adding leading child axis");
			
			node.setAxis(new AChildAxis());
		}
	}
	
}
