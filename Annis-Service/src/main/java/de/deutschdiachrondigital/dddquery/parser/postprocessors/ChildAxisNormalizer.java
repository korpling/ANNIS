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
