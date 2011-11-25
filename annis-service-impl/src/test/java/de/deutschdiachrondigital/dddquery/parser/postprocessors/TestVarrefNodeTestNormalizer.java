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

import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newPathExpr;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newStep;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newVarrefNodeTest;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.deutschdiachrondigital.dddquery.node.AElementNodeTest;
import de.deutschdiachrondigital.dddquery.node.APathExpr;
import de.deutschdiachrondigital.dddquery.node.AStep;

public class TestVarrefNodeTestNormalizer {

	// child::$v results in an element node test and a variable binding of the step
	@Test
	public void normalizeVarrefNodeTests() {
		AStep step1 = newStep(null, newVarrefNodeTest("p"));
		AStep step2 = newStep(null, newVarrefNodeTest("q"));
		APathExpr path = newPathExpr(step1, step2);
		path.apply(new VarrefNodeTestNormalizer());
		assertThat(step1.getNodeTest(), instanceOf(AElementNodeTest.class));
		assertThat(step1.getVariable().getText(), is("p"));
		assertThat(step2.getNodeTest(), instanceOf(AElementNodeTest.class));
		assertThat(step2.getVariable().getText(), is("q"));
	}
	
}
