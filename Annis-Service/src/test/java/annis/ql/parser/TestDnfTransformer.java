package annis.ql.parser;

import static annis.ql.parser.AstBuilder.newAndExpr;
import static annis.ql.parser.AstBuilder.newAnnotationSearchExpr;
import static annis.ql.parser.AstBuilder.newAnyNodeSearchExpr;
import static annis.ql.parser.AstBuilder.newLinguisticConstraintExpr;
import static annis.ql.parser.AstBuilder.newMetaConstraintExpr;
import static annis.ql.parser.AstBuilder.newOrExpr;
import static annis.ql.parser.AstBuilder.newStart;
import static annis.ql.parser.AstBuilder.newTextSearchExpr;
import static annis.ql.parser.AstBuilder.newWildTextSpec;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;

import annis.ql.node.PExpr;
import annis.ql.node.Start;
import annis.ql.parser.AstComparator.DifferentTreeException;


public class TestDnfTransformer {
	
	// class under test
	private DnfTransformer dnfTransformer;
	private PExpr leaf1;
	private PExpr leaf2;
	private PExpr leaf3;
	
	@Before
	public void setup() {
		dnfTransformer = new DnfTransformer();

		leaf1 = newAnnotationSearchExpr("leaf1");
		leaf2 = newAnnotationSearchExpr("leaf2");
		leaf3 = newAnnotationSearchExpr("leaf3");
	}
	
	// a meta constraint is already normalized
	@Test
	public void metaConstraintIsNormalized() {
		assertLeafExpressionIsNormalized(newMetaConstraintExpr("namespace", "name", "value"), false);
	}
	
	// a text search is already normalized
	@Test
	public void textSearchIsNormalized() {
		assertLeafExpressionIsNormalized(newTextSearchExpr(newWildTextSpec("text")));
	}

	// a annotation search is already normalized
	@Test
	public void annotationSearchIsNormalized() {
		assertLeafExpressionIsNormalized(newAnnotationSearchExpr("annotation"));
	}
	
	// a node search is already normalized
	@Test
	public void nodeSearchIsNormalized() {
		assertLeafExpressionIsNormalized(newAnyNodeSearchExpr());
	}
	
	// a linguistic expression is already normalized
	@Test
	public void linguisticOperationIsNormalized() {
		PExpr expr = newLinguisticConstraintExpr();
		Start actual = newStart(expr);
		
		actual.apply(dnfTransformer);
		
		Start expected = newStart(expr);
		assertThat(actual, sameTree(expected));
		// linguistic operations have no position
	}
	
	// inline nested AND
	@Test
	public void nestedAnd() {
		Start actual = newStart(newAndExpr(leaf1, newAndExpr(leaf2, leaf3)));
		Start expected = newStart(newAndExpr(clone(leaf1, leaf2, leaf3)));
		assertTreeAndLeafPosition(actual, expected);
	}
	
	// inline nested OR
	@Test
	public void nestedOr() {
		Start actual = newStart(newOrExpr(leaf1, newOrExpr(leaf2, leaf3)));
		Start expected = newStart(newOrExpr(clone(leaf1, leaf2, leaf3)));
		assertTreeAndLeafPosition(actual, expected);
	}
	
	// nested OR within an AND is distributed
	@Test
	public void nestedOrInAnd() {
		Start actual = newStart(newAndExpr(leaf1, newOrExpr(leaf2, leaf3)));
		Start expected = newStart(newOrExpr(newAndExpr(clone(leaf1, leaf2)), newAndExpr(clone(leaf1, leaf3))));
		assertTreeAndLeafPosition(actual, expected);
	}
	
	// nested AND within an OR is returned as is
	@Test
	public void nestedAndInOr() {
		Start actual = newStart(newOrExpr(leaf1, newAndExpr(leaf2, leaf3)));
		Start expected = newStart(newOrExpr(clone(leaf1), newAndExpr(clone(leaf2, leaf3))));
		assertTreeAndLeafPosition(actual, expected);
	}
	
	///// custom Matcher
	
	private Matcher<Start> sameTree(final Start expected) {
		return new TypeSafeMatcher<Start>() {

			@Override
			public boolean matchesSafely(Start item) {
				try {
					item.apply(new AstComparator(expected));
				} catch (DifferentTreeException e) {
					return false;
				}
				return true;
			}

			public void describeTo(Description description) {
				description.appendText("A syntax tree matching the query: " + AnnisParser.dumpTree(expected));
			}
			
		};
	}

	///// custom asserts
	
	// test expressions that are already normalized
	// normalized tree is same is input tree
	// normalizer remembers position
	private void assertLeafExpressionIsNormalized(PExpr expr) {
		assertLeafExpressionIsNormalized(expr, true);
	}
	
	private void assertLeafExpressionIsNormalized(PExpr expr, boolean rememberPosition) {
		Start actual = newStart(expr);
		
		actual.apply(dnfTransformer);
		
		Start expected = newStart(expr);
		assertThat(actual, sameTree(expected));
		if (rememberPosition)
			assertThat(dnfTransformer.getPosition(expr), is(1));
		else 
			assertThat(dnfTransformer.getPosition(expr), is(-1));
	}
	
	// test nested boolean expressions of same kind
	// nested boolean expressions are inlined
	// leaf order is unchanged (assume order leaf1, leaf2, leaf3)
	private void assertTreeAndLeafPosition(Start actual, Start expected) {
		actual.apply(dnfTransformer);
		assertThat(actual, sameTree(expected));
		assertThat(dnfTransformer.getPosition(leaf1), is(1));
		assertThat(dnfTransformer.getPosition(leaf2), is(2));
		assertThat(dnfTransformer.getPosition(leaf3), is(3));
	}

	///// Helper
	
	private PExpr clone(PExpr expr) {
		return (PExpr) expr.clone();
	}
	
	private PExpr[] clone(PExpr... exprs) {
		PExpr[] result = new PExpr[exprs.length];
		for (int i = 0; i < exprs.length; ++i)
			result[i] = (PExpr) exprs[i].clone();
		return result;
	}


}
