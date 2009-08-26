/**
 * 
 */
package de.deutschdiachrondigital.dddquery.sql.old2;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import de.deutschdiachrondigital.dddquery.node.APathExpr;
import de.deutschdiachrondigital.dddquery.node.AStep;
import de.deutschdiachrondigital.dddquery.node.AVarrefNodeTest;

public class PathMatcher extends TypeSafeMatcher<APathExpr> {
	private APathExpr expected;
	
	public PathMatcher(APathExpr expected) {
		this.expected = expected;
	}
	
	@Override
	public boolean matchesSafely(APathExpr item) {
		return pathRef(item) != null && pathRef(item).equals(pathRef(expected));
	}

	public void describeTo(Description description) {
		description.appendText("path" + pathRef(expected));
	}
	
	private String pathRef(APathExpr path) {
		try {
			return ((AVarrefNodeTest) ((AStep) path.getStep().get(0)).getNodeTest()).getVariable().getText();
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Matcher<APathExpr> path(APathExpr path) {
		return new PathMatcher(path);
	}
	
}