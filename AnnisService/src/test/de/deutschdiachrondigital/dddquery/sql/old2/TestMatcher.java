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
