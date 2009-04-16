package annis.ql.parser;

import static annis.ql.parser.AstBuilder.newAnnotationSearchExpr;
import static annis.ql.parser.AstBuilder.newStart;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import annis.ql.node.AAnnotationSearchExpr;
import annis.ql.node.AAnyNodeSearchExpr;
import annis.ql.node.Start;

public class TestNodeSearchNormalizer {

	private NodeSearchNormalizer nodeSearchNormalizer;
	
	@Before
	public void setup() {
		nodeSearchNormalizer = new NodeSearchNormalizer();
	}
	
	// don't replace annotation search, if type is not "node"
	@SuppressWarnings("unchecked")
	@Test
	public void caseAAnnotationSearchNormal() {
		AAnnotationSearchExpr anno = newAnnotationSearchExpr("foo");
		Start start = newStart(anno);
		start.apply(nodeSearchNormalizer);
		assertThat(start.getPExpr(), allOf(
				instanceOf(AAnnotationSearchExpr.class),
				sameInstance(anno)));
	}
	
	// replace annotation search with any node search if type is "node"
	@Test
	public void caseAAnnotationSearchNodeSearch() {
		Start start = newStart(newAnnotationSearchExpr("node"));
		start.apply(nodeSearchNormalizer);
		assertThat(start.getPExpr(), is(instanceOf(AAnyNodeSearchExpr.class)));
	}
	
}
