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
package de.deutschdiachrondigital.dddquery.sql.old2;

import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newAndExpr;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newOrExpr;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newPathExpr;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newStep;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newVarrefNodeTest;
import static de.deutschdiachrondigital.dddquery.sql.old2.AndMatcher.and;
import static de.deutschdiachrondigital.dddquery.sql.old2.OrMatcher.or;
import static de.deutschdiachrondigital.dddquery.sql.old2.PathMatcher.path;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import de.deutschdiachrondigital.dddquery.node.AAndExpr;
import de.deutschdiachrondigital.dddquery.node.AOrExpr;
import de.deutschdiachrondigital.dddquery.node.APathExpr;

// FIXME: move to class that uses these tests
public class TestMatcher {


	private APathExpr path1;
	private APathExpr path2;
	private APathExpr path3;
	
	@Before
	public void setup() {
		path1 = newPathExpr(newStep(null, newVarrefNodeTest("1")));
		path2 = newPathExpr(newStep(null, newVarrefNodeTest("2")));
		path3 = newPathExpr(newStep(null, newVarrefNodeTest("3")));
	}

	@Test
	public void sanityPathMatches() {
		assertThat(path1, is(path(path1)));
	}
	
	@Test(expected=AssertionError.class)
	public void sanityPathDontMacht() {
		assertThat(path1, is(path(path2)));
	}
	
	@Test
	public void sanityOrMatches() {
		AOrExpr or = newOrExpr(path1, path2);
		assertThat(or, is(or(path1, path2)));
	}
	
	@Test(expected=AssertionError.class)
	public void sanityOrDontMatchDifferentPath() {
		AOrExpr or = newOrExpr(path1, path2);
		assertThat(or, is(or(path1, path3)));
	}
	
	@Test(expected=AssertionError.class)
	public void sanityOrDontMatchDifferentChildrenCount() {
		AOrExpr or = newOrExpr(path1, path2);
		assertThat(or, is(or(path1, path2, path3)));
	}
	
	@Test
	public void sanityAndMatches() {
		AAndExpr and = newAndExpr(path1, path2);
		assertThat(and, is(and(path1, path2)));
	}
	
	@Test(expected=AssertionError.class)
	public void sanityAndDontMatchDifferentPath() {
		AAndExpr and = newAndExpr(path1, path2);
		assertThat(and, is(and(path1, path3)));
	}
	
	@Test(expected=AssertionError.class)
	public void sanityAndDontMatchDifferentChildrenCount() {
		AAndExpr and = newAndExpr(path1, path2);
		assertThat(and, is(and(path1, path2, path3)));
	}
	
}
