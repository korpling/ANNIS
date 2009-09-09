package de.deutschdiachrondigital.dddquery.parser.postprocessors;

import org.apache.log4j.Logger;

import de.deutschdiachrondigital.dddquery.analysis.DepthFirstAdapter;
import de.deutschdiachrondigital.dddquery.node.AElementNodeTest;
import de.deutschdiachrondigital.dddquery.node.AStep;
import de.deutschdiachrondigital.dddquery.node.AVarrefNodeTest;
import de.deutschdiachrondigital.dddquery.node.TId;

/**
 * A class that normalizes steps where a variable binding is used
 * instead of a node test.
 * 
 * <p>
 * The input <tt>$v</tt> (or <tt>a/$v</tt>) is parsed as a step that
 * contains a node test for a variable (AVarrefNodeTest) by SableCC
 * to simplify the SableCC definition file.
 * 
 * <p>
 * This class transforms such a step into a step with an <tt>element()</tt> node test
 * and sets the variable binding of this step.
 * 
 * @author Viktor Rosenfeld <rosenfel@informatik.hu-berlin.de>
 */
public class VarrefNodeTestNormalizer extends DepthFirstAdapter {

	private Logger log = Logger.getLogger(this.getClass());
	
	@Override
	public void inAStep(AStep node) {
		if ( node.getNodeTest() instanceof AVarrefNodeTest ) {
			TId varref = ((AVarrefNodeTest) node.getNodeTest()).getVariable();

			log.info("found node test for a variable: " + varref);
			
			node.setNodeTest(new AElementNodeTest());
			node.setVariable(varref);
		}
	}
	
}
