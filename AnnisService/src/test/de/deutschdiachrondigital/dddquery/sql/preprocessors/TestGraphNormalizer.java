package de.deutschdiachrondigital.dddquery.sql.preprocessors;

import static de.deutschdiachrondigital.dddquery.helper.Helper.parse;
import static de.deutschdiachrondigital.dddquery.helper.IsCollectionSize.size;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;

import de.deutschdiachrondigital.dddquery.helper.Ast2String;
import de.deutschdiachrondigital.dddquery.helper.AstBuilder;
import de.deutschdiachrondigital.dddquery.helper.BeanFactory;
import de.deutschdiachrondigital.dddquery.node.AAndExpr;
import de.deutschdiachrondigital.dddquery.node.AOrExpr;
import de.deutschdiachrondigital.dddquery.node.APathExpr;
import de.deutschdiachrondigital.dddquery.node.AStep;
import de.deutschdiachrondigital.dddquery.node.AVarrefNodeTest;
import de.deutschdiachrondigital.dddquery.node.Node;
import de.deutschdiachrondigital.dddquery.node.PExpr;
import de.deutschdiachrondigital.dddquery.node.Start;

public class TestGraphNormalizer {

	private AstBuilder b;
	private DisjunctiveNormalformNormalizer normalizer;
	private APathExpr path1;
	private APathExpr path2;
	private APathExpr path3;
	private APathExpr path4;
	private APathExpr path5;
	private APathExpr path6;
	
	static class PathMatcher extends TypeSafeMatcher<APathExpr> {
		APathExpr expected;
		APathExpr actual;
		
		public PathMatcher(APathExpr expected) {
			this.expected = expected;
		}
		
		@Override
		public boolean matchesSafely(APathExpr item) {
			actual = item;
			return pathRef(item) != null && pathRef(item).equals(pathRef(expected));
		}
	
		public void describeTo(Description description) {
			description.appendText("a path with ref " + pathRef(expected) + "; got: " + pathRef(actual));
		}
		
		private String pathRef(APathExpr path) {
			try {
				return ((AVarrefNodeTest) ((AStep) path.getStep().get(0)).getNodeTest()).getVariable().getText();
			} catch (Exception e) {
				return null;
			}
		}
		
	}
	
	static class OrMatcher extends TypeSafeMatcher<AOrExpr> {
		APathExpr[] expected;
		
		public OrMatcher(APathExpr... expected) {
			this.expected = expected;
		}
		
		@Override
		public boolean matchesSafely(AOrExpr actual) {
			List<PExpr> children = actual.getExpr();
			if (expected.length != children.size())
				return false;
			for (int i = 0; i < expected.length; ++i) {
				if ( ! (new PathMatcher((APathExpr) expected[i]).matchesSafely((APathExpr) children.get(i))) )
					return false;
			}
			return true;
		}

		public void describeTo(Description description) {
			description.appendText("an or expression with children ");
			for (APathExpr path : expected)
				description.appendValue(path);
		}
	}
	
	static class AndMatcher extends TypeSafeMatcher<AAndExpr> {
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
				if ( ! (new PathMatcher((APathExpr) expected[i]).matchesSafely((APathExpr) children.get(i))) )
					return false;
			}
			return true;
		}

		public void describeTo(Description description) {
			description.appendText("an or expression with children ");
			for (APathExpr path : expected)
				description.appendValue(path);
		}
	}
	
	static Matcher<APathExpr> path(APathExpr path) {
		return new PathMatcher(path);
	}
	
	static Matcher<AOrExpr> or(APathExpr... paths) {
		return new OrMatcher(paths);
	}
	
	static Matcher<AAndExpr> and(APathExpr... paths) {
		return new AndMatcher(paths);
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
		AOrExpr or = b.newOrExpr(new PExpr[] { path1, path2 } );
		assertThat(or, is(or(path1, path2)));
	}
	
	@Test(expected=AssertionError.class)
	public void sanityOrDontMatchDifferentPath() {
		AOrExpr or = b.newOrExpr(new PExpr[] { path1, path2 } );
		assertThat(or, is(or(path1, path3)));
	}
	
	@Test(expected=AssertionError.class)
	public void sanityOrDontMatchDifferentChildrenCount() {
		AOrExpr or = b.newOrExpr(new PExpr[] { path1, path2 } );
		assertThat(or, is(or(path1, path2, path3)));
	}
	
	@Test
	public void sanityAndMatches() {
		AAndExpr and = b.newAndExpr(new PExpr[] { path1, path2 } );
		assertThat(and, is(and(path1, path2)));
	}
	
	@Test(expected=AssertionError.class)
	public void sanityAndDontMatchDifferentPath() {
		AAndExpr and = b.newAndExpr(new PExpr[] { path1, path2 } );
		assertThat(and, is(and(path1, path3)));
	}
	
	@Test(expected=AssertionError.class)
	public void sanityAndDontMatchDifferentChildrenCount() {
		AAndExpr and = b.newAndExpr(new PExpr[] { path1, path2 } );
		assertThat(and, is(and(path1, path2, path3)));
	}
	
	@Before
	public void setup() {
		b = new AstBuilder();
		normalizer = new DisjunctiveNormalformNormalizer();

		path1 = b.newPathExpr(null, new AStep[] { b.newStep(null, b.newVarrefNodeTest("1")) });
		path2 = b.newPathExpr(null, new AStep[] { b.newStep(null, b.newVarrefNodeTest("2")) });
		path3 = b.newPathExpr(null, new AStep[] { b.newStep(null, b.newVarrefNodeTest("3")) });
		path4 = b.newPathExpr(null, new AStep[] { b.newStep(null, b.newVarrefNodeTest("4")) });
		path5 = b.newPathExpr(null, new AStep[] { b.newStep(null, b.newVarrefNodeTest("5")) });
		path6 = b.newPathExpr(null, new AStep[] { b.newStep(null, b.newVarrefNodeTest("6")) });
	}
	
	@Test
	public void caseStart() {
		Start start = b.newStart(b.newAndExpr(new PExpr[] {
			b.newOrExpr(new PExpr[] { path1, path2 }),
			path3
		}));
		start.apply(normalizer);
		
		AOrExpr or = (AOrExpr) start.getPExpr();
		assertThat(or.getExpr(), size(2));
		
		AAndExpr and1 = (AAndExpr) or.getExpr().get(0);
		assertThat(and1, is(and(path1, path3)));

		AAndExpr and2 = (AAndExpr) or.getExpr().get(1);
		assertThat(and2, is(and(path2, path3)));
	}
	
	class MockGraphNormalizer extends DisjunctiveNormalformNormalizer {
		@Override
		public void inAPathExpr(APathExpr node) {
			throw new AssertionError("traversal should stop at paths");
		}
	}
	
	@Test
	public void pathsAreLeafs() {
		Start start = parse("a");
		// exception here if we descent below a path
		start.apply(new MockGraphNormalizer());
	}
	
	@Test
	public void normalizePath() {
		Node out = normalizer.normalize(path1);
		assertThat((APathExpr) out, is(path(path1)));
	}
	
	@Test
	public void normalizeOrChildrenAreLeafs() {
		AOrExpr in = b.newOrExpr(new PExpr[] { path1, path2 });
		Node out = normalizer.normalize(in);
		
		assertThat((AOrExpr) out, is(or(path1, path2)));
	}
		
	@Test 
	public void normalizeOrChildIsOr() {
		AOrExpr in = b.newOrExpr(new PExpr[] { path1, b.newOrExpr(new PExpr[] { path2, path3 }) });

		Node out = normalizer.normalize(in);
		
		assertThat((AOrExpr) out, is(or(path1, path2, path3)));
	}
	
	@Test
	public void normalizeOrChildIsAnd() {
		AOrExpr in = b.newOrExpr(new PExpr[] { path1, b.newAndExpr(new PExpr[] { path2, path3 }) });
		
		Node out = normalizer.normalize(in);
		
		List<PExpr> children = ((AOrExpr) out).getExpr();
		assertThat(children, size(2));
		assertThat((APathExpr) children.get(0), is(path(path1)));
		assertThat(children.get(1), instanceOf(AAndExpr.class));
		
		AAndExpr and = ((AAndExpr) children.get(1));
		assertThat(and, is(and(path2, path3)));
	}
	
	@Test
	public void normalizeAndChildAreLeafs() {
		AAndExpr in = b.newAndExpr(new PExpr[] { path1, path2 });
		Node out = normalizer.normalize(in);
		
		assertThat((AAndExpr) out, is(and(path1, path2)));
	}
	
	@Test
	public void normalizeAndChildIsAnd() {
		AAndExpr in = b.newAndExpr(new PExpr[] { path1, b.newAndExpr(new PExpr[] { path2, path3 }) });

		Node out = normalizer.normalize(in);
		
		assertThat((AAndExpr) out, is(and(path1, path2, path3)));
	}
	
	@Test
	public void normalizeAndChildIsOr() {
		AAndExpr in = b.newAndExpr(new PExpr[] { b.newOrExpr(new PExpr[] { path1, path2 } ), b.newOrExpr(new PExpr[] { path3, path4 }) });

		Node out = normalizer.normalize(in);
		
		assertThat(out, is(instanceOf(AOrExpr.class)));
		
		List<PExpr> children = ((AOrExpr) out).getExpr();
		assertThat(children, size(4));
		
		AAndExpr and1 = (AAndExpr) children.get(0);
		assertThat(and1, is(and(path1, path3)));

		AAndExpr and2 = (AAndExpr) children.get(1);
		assertThat(and2, is(and(path1, path4)));

		AAndExpr and3 = (AAndExpr) children.get(2);
		assertThat(and3, is(and(path2, path3)));

		AAndExpr and4 = (AAndExpr) children.get(3);
		assertThat(and4, is(and(path2, path4)));
	}
	
	@Test
	public void distrubute1OrNoRecursion() {
		List<AOrExpr> ors = new ArrayList<AOrExpr>();
		ors.add(b.newOrExpr(new PExpr[] { path1, path2 } ));
		
		List<AAndExpr> ands = new ArrayList<AAndExpr>();
		ands.add(b.newAndExpr(new PExpr[] { path3 } ));

		List<AAndExpr> out = normalizer.distribute(ors, ands);
		
		assertThat(out, size(2));
		
		AAndExpr and1 = out.get(0);
		assertThat(and1, is(and(path1, path3)));

		AAndExpr and2 = out.get(1);
		assertThat(and2, is(and(path2, path3)));
	}
	
	@Test
	public void distrubuteManyOrsRecursion() {
		List<AOrExpr> ors = new ArrayList<AOrExpr>();
		ors.add(b.newOrExpr(new PExpr[] { path1, path2 } ));
		ors.add(b.newOrExpr(new PExpr[] { path3, path4 } ));
		
		List<AAndExpr> ands = new ArrayList<AAndExpr>();
		ands.add(b.newAndExpr(new PExpr[] { }));

		List<AAndExpr> out = normalizer.distribute(ors, ands);
		
		assertThat(out, size(4));
		
		AAndExpr and1 = (AAndExpr) out.get(0);
		assertThat(and1, is(and(path1, path3)));

		AAndExpr and2 = (AAndExpr) out.get(1);
		assertThat(and2, is(and(path1, path4)));

		AAndExpr and3 = (AAndExpr) out.get(2);
		assertThat(and3, is(and(path2, path3)));

		AAndExpr and4 = (AAndExpr) out.get(3);
		assertThat(and4, is(and(path2, path4)));
	}
	
	@Test
	public void normalizeComplexExample() {
		AAndExpr in = b.newAndExpr(new PExpr[] {
				b.newOrExpr(new PExpr[] {
						b.newAndExpr(new PExpr[] {
							path1,
							path2,
						}),
						b.newOrExpr(new PExpr[] {
							path3,
							path4
						})
				}),
				b.newAndExpr(new PExpr[] {
					path5,
					path6
				})
		});
		
		Node out = normalizer.normalize(in);
		
		assertThat((AOrExpr) out, is(instanceOf(AOrExpr.class)));
		AOrExpr or = (AOrExpr) out;
		
		AAndExpr and1 = (AAndExpr) or.getExpr().get(0);
		assertThat(and1, is(and(path1, path2, path5, path6)));

		AAndExpr and2 = (AAndExpr) or.getExpr().get(1);
		assertThat(and2, is(and(path3, path5, path6)));

		AAndExpr and3 = (AAndExpr) or.getExpr().get(2);
		assertThat(and3, is(and(path4, path5, path6)));
		
//		assertThat((AOrExpr) out, is(or(
//				and(path(1), path(2), path(5), path(6)),
//				and(path(3), path(5), path(6)),
//				and(path(4), path(5), path(6)))));
	}

	@Test
	public void normalizeComplexExampleStart() {
		String[] in = {
				"( a | b ) & ( c | ( d & f ) | ( g & ( h | i ) ) )",
				"( a & ( b | ( c & ( d | ( e & ( f | g ) ) ) ) ) )",
				"( a | ( b | ( c & ( d & ( e | ( f | ( g & ( h & i ) ) ) ) ) ) ) )",
				"( a & ( b & ( c | ( d | ( e & ( f & ( g | ( h | i ) ) ) ) ) ) ) )",
//				"( ( *#(a1)[@verb = '*']$a1//TOKEN#(t1)$t1 | *#(a1)[@* = 'verb']$a1//TOKEN#(t1)$t1 ) & ( *#(a2)[@uArt = '*']$a2//TOKEN#(t2)$t2 | *#(a2)[@* = 'uArt']$a2//TOKEN#(t2)$t2 ) ) & ( $t1/following::element()$t2 )",
//				"( ( ( *#(a1)[@verb = '*']$a1//TOKEN#(t1)$t1 ) | ( *#(a1)[@* = 'verb']$a2//TOKEN#(t1)$t1 ) ) & ( ( *#(a2)[@uArt = '*']$a2//TOKEN#(t2)$t2 ) | ( *#(a2)[@* = 'uArt']$a2//TOKEN#(t2)$t2 ) ) ) & ( $t1-->$t2 )"
//				"( ( ( *#(a1)[@verb = '*']$a1//TOKEN#(t1)$t1 ) | ( *#(a2)[@* = 'verb']$a2//TOKEN#(t2)$t2 ) ) & ( ( *#(a3)[@uArt = '*']$a3//TOKEN#(t3)$t3 ) | ( *#(a4)[@* = 'uArt']$a4//TOKEN#(t4)$t4 ) ) ) & ( ( ( $t1/following::element()$t3 ) | ( $t1/following::element()$t4 ) ) | ( ( $t2/following::element()$t3 ) | ( $t2/following::element()$t4 ) ) )"
//				"( *#[@pos='kA']$v | *#[@pos='verb']$v ) & *#[@pos='uArt']$w & $v/following::$w"
		};
		String[] out = {
				"( a & c ) | ( a & d & f ) | ( a & h & g ) | ( a & i & g ) | ( b & c ) | ( b & d & f ) | ( b & h & g ) | ( b & i & g )",
				"( b & a ) | ( d & c & a ) | ( f & e & c & a ) | ( g & e & c & a )",
				"a | b | ( e & d & c ) | ( f & d & c ) | ( g & h & i & d & c )",
				"( c & b & a ) | ( d & b & a ) | ( g & f & e & b & a ) | ( h & f & e & b & a ) | ( i & f & e & b & a )"
		};
		for (int i = 0; i < in.length; ++i) {
			Ast2String ast2String = new Ast2String();
			Start start = new BeanFactory().getDddQueryParser().parseDddQuery(in[i]);
			start.apply(normalizer);
			start.apply(ast2String);
			System.out.println(ast2String.getResult());
			assertThat(ast2String.getResult(), is(out[i]));
		}
	}
	
	/*
	 * BUG in SableCC?
	 * 
	 * Wenn ein Knoten mit einer Liste geklont wird, wird die Liste im ursprünglichen
	 * Knoten gelöscht.
	 * 
	 * Fix: Node.cloneList() muss cloneNode aufrufen, um ein Eintrag zu kopieren
	 */
	@Test
	public void nodeCloneListFix() {
		assertThat(path1.getStep(), size(1));
		APathExpr path = (APathExpr) path1.clone();
		assertThat(path.getStep(), size(1));
		assertThat(path1.getStep(), size(1));
	}
	
}
