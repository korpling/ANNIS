/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
