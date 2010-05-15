package annis.ql.parser;

import static annis.ql.parser.AstBuilder.newAnnotationSearchExpr;
import static annis.ql.parser.AstBuilder.newStart;
import static annis.ql.parser.AstBuilder.newWildTextSpec;
import static annis.ql.parser.AstBuilder.newEqualAnnoValue;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import annis.ql.node.AAnnotationSearchExpr;
import annis.ql.node.AEqualAnnoValue;
import annis.ql.node.ATextSearchExpr;
import annis.ql.node.AWildTextSpec;
import annis.ql.node.Start;

public class TestTokenSearchNormalizer {

	// no change for normal annotations, eg. pos="hello"
	@Test
	public void caseAnnotationSearchExprNormalSearch() {
		AWildTextSpec textSpec = newWildTextSpec("hello");
    AEqualAnnoValue annoValue = newEqualAnnoValue(textSpec);
		AAnnotationSearchExpr expr = newAnnotationSearchExpr("pos", annoValue);
		Start start = newStart(expr);
		
		TokenSearchNormalizer normalizer = new TokenSearchNormalizer();
		
		start.apply(normalizer);
		
		assertThat(start.getPExpr(), is(instanceOf(AAnnotationSearchExpr.class)));
		assertThat((AAnnotationSearchExpr) start.getPExpr(), is(sameInstance(expr)));
		
	}
	
	// tok="hello" is a text search
	@Test
	public void caseAnnotationSearchExprTokenSearch() {
		AWildTextSpec textSpec = newWildTextSpec("hello");
    AEqualAnnoValue annoValue = newEqualAnnoValue(textSpec);
		AAnnotationSearchExpr expr = newAnnotationSearchExpr("tok", annoValue);
		Start start = newStart(expr);
		
		TokenSearchNormalizer normalizer = new TokenSearchNormalizer();
		
		start.apply(normalizer);

		assertThat(start.getPExpr(), is(instanceOf(ATextSearchExpr.class)));
		
		ATextSearchExpr textSearchExpr = (ATextSearchExpr) start.getPExpr();
    assertThat(textSearchExpr.getTextSpec(), is(instanceOf(AWildTextSpec.class)));
		assertThat((AWildTextSpec) textSearchExpr.getTextSpec(), is(sameInstance(textSpec)));
	}
	
}
