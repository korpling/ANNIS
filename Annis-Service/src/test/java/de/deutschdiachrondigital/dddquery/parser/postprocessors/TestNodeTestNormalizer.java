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

import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newAttributeAxis;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newChildAxis;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newElementSpanAxis;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newStep;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newUnknownNodeTest;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.deutschdiachrondigital.dddquery.node.AAttributeNodeTest;
import de.deutschdiachrondigital.dddquery.node.AElementNodeTest;
import de.deutschdiachrondigital.dddquery.node.ASpanNodeTest;
import de.deutschdiachrondigital.dddquery.node.AStep;

public class TestNodeTestNormalizer {

	// attribute::name => attribute::attribute(name)
	@Test
	public void normalizeAttributeAxis() {
		AStep in = newStep(newAttributeAxis(), newUnknownNodeTest(null, "foo"));
		in.apply(new NodeTestNormalizer());
		
		assertThat(in.getNodeTest(), is(instanceOf(AAttributeNodeTest.class)));
		AAttributeNodeTest out = (AAttributeNodeTest) in.getNodeTest();
		assertThat(out.getName().getText(), is("foo"));
	}

	// element-span::* => element-span::span()
	@Test
	public void normalizeSpanAxis() {
		AStep in = newStep(newElementSpanAxis(), newUnknownNodeTest(null, null));
		in.apply(new NodeTestNormalizer());
		
		assertThat(in.getNodeTest(), is(instanceOf(ASpanNodeTest.class)));
	}
	
	// child::name => child::element(name)
	@Test
	public void normalizeChildAxis() {
		AStep in = newStep(newChildAxis(), newUnknownNodeTest(null, "foo"));
		in.apply(new NodeTestNormalizer());
		
		assertThat(in.getNodeTest(), is(instanceOf(AElementNodeTest.class)));
		AElementNodeTest out = (AElementNodeTest) in.getNodeTest();
		assertThat(out.getName().getText(), is("foo"));
	}
	
}
