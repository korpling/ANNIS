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

import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newChildAxis;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newElementNodeTest;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newPathExpr;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newRelativePathType;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newStart;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newStep;

import org.junit.Test;

import de.deutschdiachrondigital.dddquery.helper.AstComparator;
import de.deutschdiachrondigital.dddquery.node.AStep;
import de.deutschdiachrondigital.dddquery.node.Start;

public class TestChildAxisNormalizer {

	private Start exampleExpr(AStep step) {
		Start actual = newStart(newPathExpr(newRelativePathType()));
		return actual;
	}

	// if axis is null, set axis to AChildAxis
	@Test
	public void childAxis() {
		Start actual = exampleExpr(newStep(null, newElementNodeTest("a")));
		Start expected = exampleExpr(newStep(newChildAxis(), newElementNodeTest("a")));
		actual.apply(new ChildAxisNormalizer());
		actual.apply(new AstComparator(expected));
	}
	
}
