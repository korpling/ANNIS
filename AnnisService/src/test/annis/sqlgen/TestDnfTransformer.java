package annis.sqlgen;

import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newAndExpr;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newOrExpr;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newPathExpr;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newStart;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newStep;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newVarrefNodeTest;
import static de.deutschdiachrondigital.dddquery.sql.old2.AndMatcher.and;
import static de.deutschdiachrondigital.dddquery.sql.old2.OrMatcher.or;
import static de.deutschdiachrondigital.dddquery.sql.old2.PathMatcher.path;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;
import static org.junit.matchers.JUnitMatchers.each;
import static org.mockito.Mockito.mock;
import static test.IsCollection.isCollection;
import static test.IsCollectionEmpty.empty;
import static test.IsCollectionSize.size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import de.deutschdiachrondigital.dddquery.node.AAndExpr;
import de.deutschdiachrondigital.dddquery.node.AOrExpr;
import de.deutschdiachrondigital.dddquery.node.APathExpr;
import de.deutschdiachrondigital.dddquery.node.Node;
import de.deutschdiachrondigital.dddquery.node.PExpr;
import de.deutschdiachrondigital.dddquery.node.Start;

@RunWith(Theories.class)
public class TestDnfTransformer {

	// class under test
	private DnfTransformer dnfTransformer;

	// a couple of path
	private APathExpr path1 = makePath("1");;
	private APathExpr path2 = makePath("2");;
	private APathExpr path3 = makePath("3");;
	private APathExpr path4 = makePath("4");;
	private APathExpr path5 = makePath("5");;
	private APathExpr path6 = makePath("6");;
	
	@Before
	public void setup() {
		dnfTransformer = new DnfTransformer();
	}

	private APathExpr makePath(String id) {
		return newPathExpr(newStep(null, newVarrefNodeTest(id)));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void caseStartCallsNormalize() {
		Start statement = newStart(newAndExpr(newOrExpr(path1, path2), path3));
		statement.apply(dnfTransformer);
		assertThat((AOrExpr) statement.getPExpr(), is(or(and(path1, path3), and(path2, path3))));
	}

	@Test
	public void listClausesPath() {
		// a statement with only single path
		APathExpr path = newPathExpr();
		Start statement = newStart(path);
		
		// get clauses
		List<PExpr> clauses = dnfTransformer.listClauses(statement);
		
		// verify one clause: the original path
		assertThat(clauses, isCollection(path));
	}
	
	@Test
	public void listClausesAnd() {
		// a statement with a couple of ANDed paths
		AAndExpr and = newAndExpr(newPathExpr(), newPathExpr());
		Start statement = newStart(and);
		
		// get clauses
		List<PExpr> clauses = dnfTransformer.listClauses(statement);
		
		// verify one clause: the original and
		assertThat(clauses, isCollection(and));
	}
	
	@Test
	public void listClausesOr() {
		// a statement with a couple of ORed paths and ANDs
		APathExpr path1 = newPathExpr();
		APathExpr path2 = newPathExpr();
		AAndExpr and = newAndExpr(newPathExpr(), newPathExpr());
		AOrExpr or = newOrExpr(path1, path2, and);
		Start statement = newStart(or);
		
		// get clauses
		List<PExpr> clauses = dnfTransformer.listClauses(statement);
		
		// verify two clauses: the paths that were ORed
		assertThat(clauses, isCollection(path1, path2, and));
	}
	
	@Test(expected=UnknownExpressionException.class)
	public void listClausesUnknownPExpr() {
		// a statement with an unknown PExpr
		PExpr unknownType = mock(PExpr.class);
		Start statement = newStart(unknownType);
		
		// should throw an exception
		dnfTransformer.listClauses(statement);
	}
	
	@Test(expected=UnknownExpressionException.class)
	public void listClausesUnknownPExprInOr() {
		// a statement with an or that contains an unknown PExpr as child
		PExpr unknownType = mock(PExpr.class);
		Start statement = newStart(newOrExpr(newPathExpr(), unknownType));
		
		// should throw an exception
		dnfTransformer.listClauses(statement);
	}
	
	@Test
	public void normalizePath() {
		Node out = dnfTransformer.normalize(path1);
		assertThat((APathExpr) out, is(path(path1)));
	}
	
	@Test
	public void normalizeOrChildrenAreLeafs() {
		AOrExpr in = newOrExpr(path1, path2);
		Node out = dnfTransformer.normalize(in);
		assertThat((AOrExpr) out, is(or(path1, path2)));
	}
		
	@Test 
	public void normalizeOrChildIsOr() {
		AOrExpr in = newOrExpr(path1, newOrExpr(path2, path3));
		Node out = dnfTransformer.normalize(in);
		assertThat((AOrExpr) out, is(or(path1, path2, path3)));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void normalizeOrChildIsAnd() {
		AOrExpr in = newOrExpr(path1, newAndExpr(path2, path3));
		Node out = dnfTransformer.normalize(in);
		assertThat((AOrExpr) out, is(or(path(path1), and(path2, path3))));
	}
	
	@Test
	public void normalizeAndChildrenAreLeafs() {
		AAndExpr in = newAndExpr(path1, path2);
		Node out = dnfTransformer.normalize(in);
		assertThat((AAndExpr) out, is(and(path1, path2)));
	}
	
	@Test
	public void normalizeAndChildIsAnd() {
		AAndExpr in = newAndExpr(path1, newAndExpr(path2, path3));
		Node out = dnfTransformer.normalize(in);
		assertThat((AAndExpr) out, is(and(path1, path2, path3)));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void normalizeAndChildIsOr() {
		AAndExpr in = newAndExpr(newOrExpr(path1, path2), newOrExpr(path3, path4));
		Node out = dnfTransformer.normalize(in);
		assertThat((AOrExpr) out, is(or(
				and(path1, path3), 
				and(path1, path4), 
				and(path2, path3), 
				and(path2, path4))));
	}
	
	@Test(expected=UnknownExpressionException.class)
	public void normalizeUnknownPExpr() {
		PExpr expr = mock(PExpr.class);
		dnfTransformer.normalize(expr);
	}
	
	@Test
	public void distrubute1OrNoRecursion() {
		List<AOrExpr> ors = new ArrayList<AOrExpr>();
		ors.add(newOrExpr(path1, path2));
		
		List<AAndExpr> ands = new ArrayList<AAndExpr>();
		ands.add(newAndExpr(path3));

		List<AAndExpr> out = dnfTransformer.distribute(ors, ands);
		
		assertThat(out, size(2));
		assertThat(out.get(0), is(and(path1, path3)));
		assertThat(out.get(1), is(and(path2, path3)));
	}
	
	@Test
	public void distrubuteManyOrsRecursion() {
		List<AOrExpr> ors = new ArrayList<AOrExpr>();
		ors.add(newOrExpr(path1, path2));
		ors.add(newOrExpr(path3, path4));
		
		List<AAndExpr> ands = new ArrayList<AAndExpr>();
		ands.add(newAndExpr(new PExpr[] { }));

		List<AAndExpr> out = dnfTransformer.distribute(ors, ands);
		
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
	
	@SuppressWarnings("unchecked")
	@Test
	public void normalizeComplexExample() {
		AAndExpr in = newAndExpr(
				newOrExpr(
						newAndExpr(path1, path2), 
						newOrExpr(path3, path4)),
				newAndExpr(path5, path6));
		
		Node out = dnfTransformer.normalize(in);
		
		assertThat((AOrExpr) out, is(or(
				and(path1, path2, path5, path6),
				and(path3, path5, path6),
				and(path4, path5, path6))));
	}

	@Test(expected = UnknownExpressionException.class)
	public void normalizeThrowsExceptionOnUnknownPExpr() {
		PExpr unknownPExpr = mock(PExpr.class);
		dnfTransformer.normalize(unknownPExpr);
	}
	
	@DataPoint public final Start SIMPLE_PATH = newStart(newPathExpr());
	@DataPoint public final Start SIMPLE_AND = 
		newStart(newAndExpr(newPathExpr(), newPathExpr()));
	@DataPoint public final Start SIMPLE_OR =
		newStart(newOrExpr(newPathExpr(), newPathExpr()));
	@DataPoint public final Start NESTED_AND = 
		newStart(newAndExpr(newAndExpr(newPathExpr(), newPathExpr()), newPathExpr()));
	@DataPoint public final Start NESTED_OR =
		newStart(newOrExpr(newOrExpr(), newPathExpr()));
	@DataPoint public final Start AND_IN_OR =
		newStart(newOrExpr(newAndExpr(newPathExpr(), newPathExpr()), newPathExpr()));
	@DataPoint public final Start OR_IN_AND = 
		newStart(newAndExpr(newOrExpr(newPathExpr(), newPathExpr()), newPathExpr()));
	@DataPoint public final Start COMPLEX_EXAMPLE = 
		newStart(newAndExpr(newOrExpr(newAndExpr(newPathExpr(), newPathExpr()), 
				newOrExpr(newPathExpr(), newPathExpr())), newPathExpr()));
	
	@Test
	public void inDnfSanityCheck() {
		// examples in DNF
		assertThat(Arrays.asList(SIMPLE_PATH, SIMPLE_AND, SIMPLE_OR, AND_IN_OR), each(is(inDnf())));

		// examples that are not in DNF
		assertThat(Arrays.asList(OR_IN_AND, NESTED_OR, NESTED_AND, COMPLEX_EXAMPLE), each(is(not(inDnf()))));
	}
	
	@Theory
	public void returnedStatementsAreInDnf(Start statement) {
		statement.apply(dnfTransformer);
		assertThat(statement, is(inDnf()));
	}
	
	@Theory
	public void listClausesIsNeverEmpty(Start statement) {
		assumeThat(statement, is(inDnf()));

		List<PExpr> clauses = dnfTransformer.listClauses(statement);
		
		assertThat(clauses, is(not(nullValue())));
		assertThat(clauses, is(not(empty())));
	}
	
	public Matcher<Start> inDnf() {
		return new TypeSafeMatcher<Start>() {

			@Override
			public boolean matchesSafely(Start item) {
				PExpr result = item.getPExpr();
				
				// single path
				if (result instanceof APathExpr)
					return true;
				
				// single clause that only contains paths
				else if (result instanceof AAndExpr) {
					AAndExpr and = (AAndExpr) result;
					return each(is(instanceOf(APathExpr.class))).matches(and.getExpr());
				}
				
				// single or: each clause is either a path or a clause that only contains paths
				else if (result instanceof AOrExpr) {
					AOrExpr or = (AOrExpr) result;
					for (PExpr clause : or.getExpr()) {
						
						if (clause instanceof APathExpr)
							continue;
						
						else if (clause instanceof AAndExpr) {
							AAndExpr and = (AAndExpr) clause;
							if ( ! each(is(instanceOf(APathExpr.class))).matches(and.getExpr()) )
								return false;
						} else
							return false;
					}
					return true;
				}
				
				else
					return false;
			}

			public void describeTo(Description description) {
				description.appendText("a statement in DNF");
			}
			
		};
	}
		  
	/*
	 * BUG in SableCC?
	 * 
	 * Wenn ein Knoten mit einer Liste geklont wird, wird die Liste im ursprünglichen
	 * Knoten gelöscht.
	 * 
	 * Fix: Node.cloneList() muss cloneNode aufrufen, um ein Eintrag zu kopieren
	 * XXX: SableCC patchen?
	 */
	@Test
	public void nodeCloneListFix() {
		// path with one step
		assertThat(path1.getStep(), size(1));
		
		// clone path: new path has one step
		APathExpr path = (APathExpr) path1.clone();
		assertThat(path.getStep(), size(1));
		
		// old path still has one step (bugfix in SableCC)
		assertThat(path1.getStep(), size(1));
	}
	
}
