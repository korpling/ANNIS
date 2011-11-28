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
import de.deutschdiachrondigital.dddquery.node.AExactSearchNodeTest;
import de.deutschdiachrondigital.dddquery.node.APathExpr;
import de.deutschdiachrondigital.dddquery.node.ARegexpLiteralExpr;
import de.deutschdiachrondigital.dddquery.node.ARegexpSearchNodeTest;
import de.deutschdiachrondigital.dddquery.node.AStep;
import de.deutschdiachrondigital.dddquery.node.AStringLiteralExpr;
import de.deutschdiachrondigital.dddquery.node.Node;
import de.deutschdiachrondigital.dddquery.node.TPattern;

/**
 * A class that identifies string literals.
 * 
 * <p>
 * With the grammar defined by Thorsten Vitt, there are two possible derivations
 * from the Start symbol to the input <tt>"String"</tt>.
 * 
 * <ul>
 * <li><tt>"String" -> TextPattern -> NodeTest -> AbbrAxisStep -> TradAxisStep -> ... -> Start</tt>
 * <li><tt>"String" -> TextPattern -> Literal -> PrimaryExpr -> TradAxisStep -> ... -> Start</tt>
 * </ul>
 * 
 * <p>
 * The SableCC parser will always assume the first alternative, i.e. it will reduce the input
 * <tt>"String"</tt> to a path with a node test of type AExactSearchNodeTest.
 * 
 * <p>
 * This class will transform a step with a node test of type AExactSearchNodeTest to an AStringLiteralExpr
 * (the second alternative) if the following conditions are met:
 * 
 * <ul>
 * <li>the axis of the step is not specified</li>
 * <li>the step has no marker, no variable, and no predicates</li>
 * <li>the step is the only step of the parent path expression</li>
 * </ul>
 * 
 * <p>
 * <b>Note</b>: This post processor needs to be applied to the parse tree before the
 * post processor {@link ChildAxisNormalizer} is applied.
 *  
 * @author Viktor Rosenfeld <rosenfel@informatik.hu-berlin.de>
 */
public class StringLiteralNormalizer extends DepthFirstAdapter {

	private Logger log = Logger.getLogger(this.getClass());
	
	@Override
	public void inAStep(AStep node) {
		APathExpr parent = (APathExpr) node.parent();
		if (parent.getStep().size() != 1)
			return;
		
		Node n = null;
		if (node.getAxis() == null && node.getMarkerSpec() == null && node.getPredicates().size() == 0 && node.getVariable() == null) {
			if (node.getNodeTest() instanceof AExactSearchNodeTest)
				n = makeStringLiteral((AExactSearchNodeTest) node.getNodeTest());
			
			else if (node.getNodeTest() instanceof ARegexpSearchNodeTest)
				n = makeRegexpLiteral((ARegexpSearchNodeTest) node.getNodeTest());
			
		}
		if (n != null)
			parent.replaceBy(n);
	}
	
	private AStringLiteralExpr makeStringLiteral(AExactSearchNodeTest nodeTest) {
		TPattern pattern = nodeTest.getPattern();
		log.info("found a string literal: " + pattern);

		AStringLiteralExpr n = new AStringLiteralExpr();
		n.setString(pattern);
		return n;
	}
	
	private ARegexpLiteralExpr makeRegexpLiteral(ARegexpSearchNodeTest nodeTest) {
		TPattern pattern = nodeTest.getPattern();
		log.info("found a string literal: " + pattern);

		ARegexpLiteralExpr n = new ARegexpLiteralExpr();
		n.setRegexp(pattern);
		return n;
	}
}
