/**
 * 
 */
package de.deutschdiachrondigital.dddquery.sql.old2;

import static de.deutschdiachrondigital.dddquery.sql.old2.PathMatcher.path;

import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import de.deutschdiachrondigital.dddquery.node.AAndExpr;
import de.deutschdiachrondigital.dddquery.node.APathExpr;
import de.deutschdiachrondigital.dddquery.node.PExpr;

public class AndMatcher extends TypeSafeMatcher<AAndExpr> {
	APathExpr[] expected;
	
	public AndMatcher(APathExpr... expected) {
		this.expected = expected;
	}
	
	@Override
	public boolean matchesSafely(AAndExpr actual) {
		List<PExpr> children = actual.getExpr();

		if (expected.length != children.size())
			return false;
		for (int i = 0; i < expected.length; ++i) {
			if ( ! path(expected[i]).matches((APathExpr) children.get(i)) )
				return false;
		}
		return true;
	}

	public void describeTo(Description description) {
		description.appendText("an or expression with children ");
		for (APathExpr path : expected)
			description.appendValue(path);
	}
	
	public static Matcher<AAndExpr> and(APathExpr... paths) {
		return new AndMatcher(paths);
	}

}