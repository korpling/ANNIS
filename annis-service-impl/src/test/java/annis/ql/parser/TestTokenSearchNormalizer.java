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
