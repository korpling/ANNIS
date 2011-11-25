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

import static de.deutschdiachrondigital.dddquery.sql.old2.PathMatcher.path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import de.deutschdiachrondigital.dddquery.node.AOrExpr;
import de.deutschdiachrondigital.dddquery.node.APathExpr;
import de.deutschdiachrondigital.dddquery.node.PExpr;

public class OrMatcher extends TypeSafeMatcher<AOrExpr> {
	List<Matcher<? extends PExpr>> childMatchers;
	
	public OrMatcher(Matcher<? extends PExpr>... matchers) {
		this.childMatchers = Arrays.asList(matchers);
	}
	
	public OrMatcher(List<Matcher<? extends PExpr>> childMatchers) {
		this.childMatchers = childMatchers;
	}
	
	@Override
	public boolean matchesSafely(AOrExpr actual) {
		List<PExpr> children = actual.getExpr();
		if (childMatchers.size() != children.size())
			return false;

		for (int i = 0; i < childMatchers.size(); ++i) {
			if ( ! childMatchers.get(i).matches(children.get(i)) )
				return false;
		}
		return true;
	}

	public void describeTo(Description description) {
		description.appendList("", " or ", "", childMatchers);
	}
	
	public static Matcher<AOrExpr> or(APathExpr... paths) {
		List<Matcher<? extends PExpr>> childMatcher = new ArrayList<Matcher<? extends PExpr>>();
		for (APathExpr path : paths)
			childMatcher.add(path(path));
		return new OrMatcher(childMatcher);
	}
	
	public static Matcher<AOrExpr> or(Matcher<? extends PExpr>... matchers) {
		return new OrMatcher(matchers);
	}
	
}