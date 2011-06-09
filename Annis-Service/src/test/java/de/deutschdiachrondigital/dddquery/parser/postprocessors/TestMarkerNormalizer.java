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
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newEndMarkerSpec;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newMarkerSpec;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newPathExpr;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newRelativePathType;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newStart;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newStartMarkerSpec;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newStep;

import org.junit.Test;

import de.deutschdiachrondigital.dddquery.helper.AstComparator;
import de.deutschdiachrondigital.dddquery.node.AStep;
import de.deutschdiachrondigital.dddquery.node.PMarkerSpec;
import de.deutschdiachrondigital.dddquery.node.PStep;
import de.deutschdiachrondigital.dddquery.node.Start;

public class TestMarkerNormalizer {

	private AStep step(String name, PMarkerSpec marker) {
		return newStep(newChildAxis(), newElementNodeTest(name), marker, null, null);
	}
	
	private Start exampleExpr(PStep[] steps) {
		Start actual = newStart(newPathExpr(newRelativePathType(), steps));
		return actual;
	}
	
	// a#+(v)/b/c#-/d => a#(v)/b#(v)/c#(v)
	@Test
	public void marker() {
		Start actual = exampleExpr(new PStep[] {
			step("a", newStartMarkerSpec("v")),
			step("b", null),
			step("c", newEndMarkerSpec(null)),
			step("d", null),
		});
		Start expected = exampleExpr(new PStep[] {
			step("a", newMarkerSpec("v")),
			step("b", newMarkerSpec("v")),
			step("c", newMarkerSpec("v")),
			step("d", null),
		});
		actual.apply(new MarkerNormalizer());
		actual.apply(new AstComparator(expected));
	}

	// start and end marker definitions can nest 
	@Test
	public void markerNested() {
		Start actual = exampleExpr(new PStep[] {
			step("a", newStartMarkerSpec("v")),
			step("b", null),
			step("c", newStartMarkerSpec(null)),
			step("d", null),
			step("e", newEndMarkerSpec(null)),
			step("f", null),
			step("g", newEndMarkerSpec(null)),
			step("h", null),
		});
		Start expected = exampleExpr(new PStep[] {
			step("a", newMarkerSpec("v")),
			step("b", newMarkerSpec("v")),
			step("c", newMarkerSpec(null)),
			step("d", newMarkerSpec(null)),
			step("e", newMarkerSpec(null)),
			step("f", newMarkerSpec("v")),
			step("g", newMarkerSpec("v")),
			step("h", null),
		});
		actual.apply(new MarkerNormalizer());
		actual.apply(new AstComparator(expected));
	}
	
}
