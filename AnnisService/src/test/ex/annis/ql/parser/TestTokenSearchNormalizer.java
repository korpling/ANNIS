package ex.annis.ql.parser;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import ex.annis.ql.helper.AstBuilder;
import ex.annis.ql.node.AAnnotationSearchExpr;
import ex.annis.ql.node.ATextSearchExpr;
import ex.annis.ql.node.AWildTextSpec;
import ex.annis.ql.node.Start;

public class TestTokenSearchNormalizer {

	/*
	 * keine Änderung bei z.B. pos
	 */
	@Test
	public void caseAnnotationSearchExprNormalSearch() {
		AstBuilder b = new AstBuilder();
		
		AWildTextSpec textSpec = b.newWildTextSpec("hello");
		AAnnotationSearchExpr expr = b.newAnnotationSearchExpr("pos", textSpec);
		Start start = b.newStart(expr);
		
		TokenSearchNormalizer normalizer = new TokenSearchNormalizer();
		
		start.apply(normalizer);
		
		assertThat(start.getPExpr(), is(instanceOf(AAnnotationSearchExpr.class)));
		assertThat((AAnnotationSearchExpr) start.getPExpr(), is(sameInstance(expr)));
		
	}
	
	/*
	 * Spezialfall tok: Textsuche
	 */
	@Test
	public void caseAnnotationSearchExprTokenSearch() {
		AstBuilder b = new AstBuilder();
		
		AWildTextSpec textSpec = b.newWildTextSpec("hello");
		AAnnotationSearchExpr expr = b.newAnnotationSearchExpr("tok", textSpec);
		Start start = b.newStart(expr);
		
		TokenSearchNormalizer normalizer = new TokenSearchNormalizer();
		
		start.apply(normalizer);
		
		assertThat(start.getPExpr(), is(instanceOf(ATextSearchExpr.class)));
		
		ATextSearchExpr textSearchExpr = (ATextSearchExpr) start.getPExpr();
		assertThat(textSearchExpr.getTextSpec(), is(instanceOf(AWildTextSpec.class)));
		assertThat((AWildTextSpec) textSearchExpr.getTextSpec(), is(sameInstance(textSpec)));
	}
	
}
