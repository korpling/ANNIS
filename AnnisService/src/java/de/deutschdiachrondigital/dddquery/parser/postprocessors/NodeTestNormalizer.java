package de.deutschdiachrondigital.dddquery.parser.postprocessors;

import org.apache.log4j.Logger;

import de.deutschdiachrondigital.dddquery.analysis.DepthFirstAdapter;
import de.deutschdiachrondigital.dddquery.node.AAttributeAxis;
import de.deutschdiachrondigital.dddquery.node.AAttributeNodeTest;
import de.deutschdiachrondigital.dddquery.node.AElementNodeTest;
import de.deutschdiachrondigital.dddquery.node.AElementSpanAxis;
import de.deutschdiachrondigital.dddquery.node.ASpanNodeTest;
import de.deutschdiachrondigital.dddquery.node.AStep;
import de.deutschdiachrondigital.dddquery.node.AUnknownNodeTest;
import de.deutschdiachrondigital.dddquery.node.TId;

/**
 * A class that normalizes node tests where only the name of the node is given.
 * 
 * <p>
 * In DDDquery the two node tests <tt>element(name)</tt> and <tt>attribute(name)</tt> can
 * both be abbreviated to simply <tt>name</tt>.  If simply <tt>name</tt> is given as a node test,
 * the SableCC parser returns AUnknownNodeTest.  This is transformed to <tt>AAttributeNodeTest</tt>, 
 * if the axis of the step is <tt>attribute</tt> and <tt>AElementNodeTest</tt> if not.
 * 
 * <p>
 * Similarly, the node test of the input <tt>element-span::*</tt> is transformed to 
 * <tt>ASpanNodeTest</tt>.
 * 
 * @author Viktor Rosenfeld <rosenfel@informatik.hu-berlin.de>
 */
public class NodeTestNormalizer extends DepthFirstAdapter {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	@Override
	public void inAUnknownNodeTest(AUnknownNodeTest node) {
		TId nameToken = node.getName();
		String name = nameToken == null ? "" : nameToken.getText();
		AStep step = (AStep) node.parent();
		
		if (step.getAxis() instanceof AAttributeAxis) {
			log.info("setting node test to attribute(" + name + ")");
			
			AAttributeNodeTest n = new AAttributeNodeTest();
			n.setName(nameToken);
			n.setNamespace(node.getNamespace());
			node.replaceBy(n);
		} else if (step.getAxis() instanceof AElementSpanAxis) {
			log.info("setting node test to span()");
			
			ASpanNodeTest n = new ASpanNodeTest();
			node.replaceBy(n);
		} else {
			log.info("setting node test to element(" + name + ")");
			
			AElementNodeTest n = new AElementNodeTest();
			n.setName(nameToken);
			node.replaceBy(n);
		}
	}
	
}
