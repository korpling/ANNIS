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
package de.deutschdiachrondigital.dddquery.parser;


import static annis.ql.parser.AstBuilder.newCommonAncestorAxis;
import static annis.ql.parser.AstBuilder.newSiblingAxis;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newAbsolutePathType;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newAndExpr;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newAttributeAxis;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newAttributeNodeTest;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newChildAxis;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newComparisonExpr;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newDescendantAxis;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newElementNodeTest;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newEqComparison;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newExactEdgeAnnotation;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newExistanceEdgeAnnotation;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newFollowingAxis;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newFunctionExpr;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newMarkerSpec;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newMetaNodeTest;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newParentAxis;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newPathExpr;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newQuotedText;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newRangeSpec;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newRegexpEdgeAnnotation;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newRegexpLiteralExpr;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newRegexpQuotedText;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newRelativePathType;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newStart;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newStep;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newStringLiteralExpr;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static annis.test.IsCollectionSize.size;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import annis.model.QueryNode;
import annis.model.QueryAnnotation;
import annis.model.QueryNode.TextMatching;
import annis.sqlgen.model.CommonAncestor;
import annis.sqlgen.model.Dominance;
import annis.sqlgen.model.Inclusion;
import annis.sqlgen.model.Join;
import annis.sqlgen.model.LeftAlignment;
import annis.sqlgen.model.LeftDominance;
import annis.sqlgen.model.LeftOverlap;
import annis.sqlgen.model.Overlap;
import annis.sqlgen.model.PointingRelation;
import annis.sqlgen.model.Precedence;
import annis.sqlgen.model.RightAlignment;
import annis.sqlgen.model.RightDominance;
import annis.sqlgen.model.RightOverlap;
import annis.sqlgen.model.SameSpan;
import annis.sqlgen.model.Sibling;
import de.deutschdiachrondigital.dddquery.node.AEqComparison;
import de.deutschdiachrondigital.dddquery.node.APathExpr;
import de.deutschdiachrondigital.dddquery.node.AStep;
import de.deutschdiachrondigital.dddquery.node.PExpr;
import de.deutschdiachrondigital.dddquery.node.Start;

public class TestClauseAnalysis {

	private static final String VALUE = "value";
	private static final String NAME = "name";
	private static final String NAMESPACE = "namespace";
	
	private ClauseAnalysis clauseAnalysis;
	
	@Before
	public void setup() {
		clauseAnalysis = new ClauseAnalysis();
	}
	
	@Test
	public void freshInstanceSetup() {
		assertThat(clauseAnalysis.isTopLevel(), is(true));
		assertThat(clauseAnalysis.isFirstStep(), is(true));
		assertThat(context(), is(nullValue()));
//		assertThat(target(), is(not(nullValue())));
	}

	// root node "/a"
	@Test
	public void caseAAbsolutePathType() {
		APathExpr path = newPathExpr(newAbsolutePathType(), newStep());
		clauseAnalysis.caseStart(newStart(path));
		assertThat(nodes(), size(1));
		assertThat(target(), isRoot());
	}

	// .../parent::element()
	@Test
	public void caseAStepElementNodeTest() {
		setTarget();
		QueryNode oldTarget = target();
		clauseAnalysis.caseAStep(newStep(newParentAxis(), newElementNodeTest()));
		assertThat(context(), is(sameInstance(oldTarget)));
		assertThat(target(), is(not(nullValue())));
	}

	/// .../attribute::attribute()
	@Test
	public void caseAStepAttributeNodeTest() {
		QueryNode oldContext = setContext();
		QueryNode oldTarget = setTarget();
		clauseAnalysis.caseAStep(newStep(null, newAttributeNodeTest()));
		assertThat(context(), is(sameInstance(oldContext)));
		assertThat(target(), is(sameInstance(oldTarget)));
	}
	
	// #i >@l #j, $i/left-child::$j
	@Ignore // We're getting rid of the DDDquery mapping anyway
	public void caseALeftChildAxis() {
		setContext();
		setTarget();
		clauseAnalysis.caseALeftChildAxis(null);
		assertThat(context(), hasJoin(new LeftDominance(target())));
	}
	
	// #i >@r #j, $i/right-child::$j
	@Ignore // We're getting rid of the DDDquery mapping anyway
	public void caseARightChildAxis() {
		setContext();
		setTarget();
		clauseAnalysis.caseARightChildAxis(null);
//		assertThat(context(), hasJoin(new RightDominance(target())));
	}
	
	@Test
	public void caseAExistanceEdgeAnnotation() {
		setTarget();
		clauseAnalysis.caseAExistanceEdgeAnnotation(newExistanceEdgeAnnotation(NAMESPACE, NAME));
		assertThat(target(), hasEdgeAnnotation(NAMESPACE, NAME));
	}
	
	@Test
	public void caseAExactEdgeAnnotation() {
		setTarget();
		clauseAnalysis.caseAExactEdgeAnnotation(newExactEdgeAnnotation(NAMESPACE, NAME, VALUE));
		assertThat(target(), hasEdgeAnnotation(NAMESPACE, NAME, VALUE, QueryNode.TextMatching.EXACT_EQUAL));
	}
	
	@Test
	public void caseARegexpEdgeAnnotation() {
		setTarget();
		clauseAnalysis.caseARegexpEdgeAnnotation(newRegexpEdgeAnnotation(NAMESPACE, NAME, VALUE));
		assertThat(target(), hasEdgeAnnotation(NAMESPACE, NAME, VALUE, QueryNode.TextMatching.REGEXP_EQUAL));
	}
	
	@Test
	public void caseAElementNodeTest() {
		setTarget();
		// FIXME: namespaces in element
		clauseAnalysis.caseAElementNodeTest(newElementNodeTest(NAME));
		assertThat(target(), isNamed(null, NAME));
	}
	
	@Test
	public void caseAMarkerSpec() {
		setTarget();
		clauseAnalysis.caseAMarkerSpec(newMarkerSpec("marker"));
		assertThat(target(), isMarked("marker"));
	}
	
	// #i > #j, $i/child[d]::$j
	@Test
	public void caseAChildAxisDominance() {
		setContext();
		setTarget();
		clauseAnalysis.setFirstStep(false);
		clauseAnalysis.caseAChildAxis(newChildAxis("d"));
		assertThat(context(), hasJoin(new Dominance(target(), 1)));
	}
	
	// #i > name #j, $i/child[d, name]::$j
	@Test
	public void caseAChildAxisDominanceNamed() {
		setContext();
		setTarget();
		clauseAnalysis.setFirstStep(false);
		clauseAnalysis.caseAChildAxis(newChildAxis("d", NAME));
		assertThat(context(), hasJoin(new Dominance(target(), NAME, 1)));
	}
	
	// #i -> name #j, $i/child[p, name]::$j
	@Test
	public void caseAChildAxisPointingRelation() {
		setContext();
		setTarget();
		clauseAnalysis.setFirstStep(false);
		clauseAnalysis.caseAChildAxis(newChildAxis("p", NAME));
		assertThat(context(), hasJoin(new PointingRelation(target(), NAME, 1)));
	}
	
	// unknown edge type
	@Test(expected = IllegalArgumentException.class)
	public void caseAChildAxisUnknownType() {
		setContext();
		setTarget();
		clauseAnalysis.setFirstStep(false);
		clauseAnalysis.caseAChildAxis(newChildAxis("x"));
	}
	
	// #i >* #j, $i/descendant[d]::$j
	@Test
	public void caseADescendantAxisIndirectDominance() {
		setContext();
		setTarget();
		clauseAnalysis.caseADescendantAxis(newDescendantAxis("d"));
		assertThat(context(), hasJoin(new Dominance(target())));
	}
	
	// #i > name * #j, $i/descendant[d, name]::$j
	@Test
	public void caseADescendantAxisIndirectDominanceNamed() {
		setContext();
		setTarget();
		clauseAnalysis.caseADescendantAxis(newDescendantAxis("d", NAME));
		assertThat(context(), hasJoin(new Dominance(target(), NAME)));
	}
	
	// #i >n #j, $i/descendant[d](n)::$j
	@Test
	public void caseADescendantAxisDistanceDominance() {
		int distance = 10;

		setContext();
		setTarget();
		clauseAnalysis.caseADescendantAxis(newDescendantAxis("d", newRangeSpec(distance)));
		assertThat(context(), hasJoin(new Dominance(target(), distance)));
	}
	
	// #i > name n #j, $i/descendant[d, name](n)::$j
	@Test
	public void caseADescendantAxisDistanceDominanceNamed() {
		int distance = 10;

		setContext();
		setTarget();
		clauseAnalysis.caseADescendantAxis(newDescendantAxis("d", NAME, newRangeSpec(distance)));
		assertThat(context(), hasJoin(new Dominance(target(), NAME, distance)));
	}
	
	// #i >n,m $j, $i/descendant[d](n, m)::$j
	@Test
	public void caseADescendantAxisRangeDominance() {
		int min = 10;
		int max = 20;

		setContext();
		setTarget();
		clauseAnalysis.caseADescendantAxis(newDescendantAxis("d", newRangeSpec(min, max)));
		assertThat(context(), hasJoin(new Dominance(target(), min, max)));
	}
	
	// #i > name n,m $j, $i/descendant[d, name](n, m)::$j
	@Test
	public void caseADescendantAxisRangeDominanceNamed() {
		int min = 10;
		int max = 20;

		setContext();
		setTarget();
		clauseAnalysis.caseADescendantAxis(newDescendantAxis("d", NAME, newRangeSpec(min, max)));
		assertThat(context(), hasJoin(new Dominance(target(), NAME, min, max)));
	}
	
	// #i >* #j, $i/descendant[p]::$j
	@Test
	public void caseADescendantAxisIndirectPointingRelation() {
		setContext();
		setTarget();
		clauseAnalysis.caseADescendantAxis(newDescendantAxis("p", NAME));
		assertThat(context(), hasJoin(new PointingRelation(target(), NAME)));
	}
	
	// #i >n #j, $i/descendant[d](n)::$j
	@Test
	public void caseADescendantAxisDistancePointingRelation() {
		int distance = 10;

		setContext();
		setTarget();
		clauseAnalysis.caseADescendantAxis(newDescendantAxis("p", NAME, newRangeSpec(distance)));
		assertThat(context(), hasJoin(new PointingRelation(target(), NAME, distance)));
	}
	
	// #i >n,m $j, $i/descendant[d](n, m)::$j
	@Test
	public void caseADescendantAxisRangePointingRelation() {
		int min = 10;
		int max = 20;

		setContext();
		setTarget();
		clauseAnalysis.caseADescendantAxis(newDescendantAxis("p", NAME, newRangeSpec(min, max)));
		assertThat(context(), hasJoin(new PointingRelation(target(), NAME, min, max)));
	}
	
	// unknown edge type
	@Test(expected = IllegalArgumentException.class)
	public void caseADescendantAxisUnknownEdgeType() {
		setContext();
		setTarget();
		clauseAnalysis.caseADescendantAxis(newDescendantAxis("x"));
	}
	
	AEqComparison EQ = newEqComparison();
	
	// Text search (regexp)
	@SuppressWarnings("unchecked")
	@Test
	public void caseAComparisonExpr1() {
		QueryNode oldTarget = setTarget();
		testCompare(EQ, newRegexpLiteralExpr("regexp"), newPathExpr(newRelativePathType()));
		assertThat(target(), allOf(spans("regexp", TextMatching.REGEXP_EQUAL), is(oldTarget)));
	}
	
	// Text search (regexp), kommutativ
	@SuppressWarnings("unchecked")
	@Test
	public void caseAComparsionExpr2() {
		QueryNode oldTarget = setTarget();
		testCompare(EQ, newPathExpr(newRelativePathType()), newRegexpLiteralExpr("regexp"));
		assertThat(target(), allOf(spans("regexp", TextMatching.REGEXP_EQUAL), is(oldTarget)));
	}
	
	// Text search (string)
	@SuppressWarnings("unchecked")
	@Test
	public void caseAComparisonExpr3() {
		QueryNode oldTarget = setTarget();
		testCompare(EQ, newPathExpr(newRelativePathType()), newStringLiteralExpr("string"));
		assertThat(target(), allOf(spans("string", TextMatching.EXACT_EQUAL), is(oldTarget)));
	}
	
	// Annotation search (string) 
	@SuppressWarnings("unchecked")
	@Test
	public void caseAComparisonExpr4() {
		QueryNode oldTarget = setTarget();
		testCompare(EQ, 
				newPathExpr(newRelativePathType(), newStep(newAttributeAxis(), newAttributeNodeTest(NAMESPACE, NAME))), 
				newStringLiteralExpr("string"));
		assertThat(target().getNodeAnnotations(), size(1));
		assertThat(target(), allOf(hasAnnotation(NAMESPACE, NAME, "string", TextMatching.EXACT_EQUAL), is(oldTarget)));
	}
	
	// Annotation search (regexp)
	@SuppressWarnings("unchecked")
	@Test
	public void caseAComparisonExpr5() {
		QueryNode oldTarget = setTarget();
		testCompare(EQ, 
				newRegexpLiteralExpr("regexp"),
				newPathExpr(newRelativePathType(), newStep(newAttributeAxis(), newAttributeNodeTest(NAMESPACE, NAME))));
		assertThat(target().getNodeAnnotations(), size(1));
		assertThat(target(), allOf(hasAnnotation(NAMESPACE, NAME, "regexp", TextMatching.REGEXP_EQUAL), is(oldTarget)));
	}
	
	// Annotation search (existence)
	@Test
	public void pathEndsWithAttribute() {
		APathExpr path = newPathExpr(newRelativePathType(), 
				newStep(newChildAxis(), newElementNodeTest()), 
				newStep(newAttributeAxis(), newAttributeNodeTest(NAMESPACE, NAME)));
		clauseAnalysis.caseStart(newStart(path));
		assertThat(target().getNodeAnnotations(), size(1));
		assertThat(target(), hasAnnotation(NAMESPACE, NAME));
	}
	
	// Annotation search (existence)
	@Test
	public void stepWithAttributePathInPredicate() {
		APathExpr nested = newPathExpr(newRelativePathType(), newStep(newAttributeAxis(), newAttributeNodeTest(NAMESPACE, NAME)));
		APathExpr path = newPathExpr(newRelativePathType(), 
				newStep(newChildAxis(), newElementNodeTest(), nested));
		clauseAnalysis.caseStart(newStart(path));
		assertThat(target().getNodeAnnotations(), size(1));
		assertThat(target(), hasAnnotation(NAMESPACE, NAME));
	}
	
	// a/b
	@SuppressWarnings("unchecked")
	@Test
	public void removeLeadingChildAxis() {
		APathExpr path = newPathExpr(newAbsolutePathType(),
				newStep(newChildAxis(), newElementNodeTest("a")),
				newStep(newChildAxis(), newElementNodeTest("b")));
		clauseAnalysis.caseStart(newStart(path));
		assertThat(nodes(), size(2));
		assertThat(context(), allOf(
				hasJoin(new Dominance(target(), 1)),
//				hasJoin(context().preRank(), target().parent()),
				isNamed(null, "a")));
		assertThat(target(), isNamed(null, "b"));
	}
	
	// node & tiger:pos=/VVFIN/ & #1 > #2
	// dddquery: element()#(n1)$n1 & element()#(n2)[@tiger:pos = r"VVFIN"]$n2 & $n1/$n2
	@Test
	public void variables() {
		APathExpr path1 = newPathExpr(newRelativePathType(), newStep(newChildAxis(), newElementNodeTest(), "n1"));
		APathExpr path2 = newPathExpr(newRelativePathType(), newStep(newChildAxis(), newElementNodeTest(), "n2",
						newComparisonExpr(newEqComparison(), 
								newPathExpr(newRelativePathType(), newStep(newAttributeAxis(), newAttributeNodeTest("tiger", "pos"))), 
								newRegexpLiteralExpr("VVFIN"))));
		APathExpr path3 = newPathExpr(newRelativePathType(),
				newStep(newChildAxis(), newElementNodeTest(), "n1"),
				newStep(newChildAxis(), newElementNodeTest(), "n2"));

		clauseAnalysis.caseStart(newStart(newAndExpr(path1, path2, path3)));
		
		assertThat(nodes(), size(2));
		assertThat(context(), hasJoin(new Dominance(target(), 1)));
//		assertThat(context(), hasJoin(context().preRank(), target().parent()));
		assertThat(target(), hasAnnotation("tiger", "pos", "VVFIN", TextMatching.REGEXP_EQUAL));
	}
	
	// #i .* #j, a/following::b
	@Test
	public void caseAFollowingAxis() {
		setContext();
		setTarget();
		clauseAnalysis.caseAFollowingAxis(newFollowingAxis());
		assertThat(context(), hasJoin(new Precedence(target())));
	}
	
	// #i .* #j, a/following::b (precedence limited)
	@Test
	public void caseAFollowingAxisPrecedenceLimited() {
		setContext();
		setTarget();
		final int PRECEDENCE_BOUND = 20;
		clauseAnalysis.setPrecedenceBound(PRECEDENCE_BOUND);
		clauseAnalysis.caseAFollowingAxis(newFollowingAxis());
		assertThat(context(), hasJoin(new Precedence(target(), 1, PRECEDENCE_BOUND)));
	}
	
	// #i . #j, a/immediately-following::b
	@Test
	public void caseAImmediatelyFollowingAxis() {
		setContext();
		setTarget();
		clauseAnalysis.caseAImmediatelyFollowingAxis(null);
		assertThat(context(), hasJoin(new Precedence(target(), 1)));
	}
	
	// #i .n #j, $i/following(n)::$j
	@Test
	public void caseAFollowingAxisDistance() {
		setContext();
		setTarget();
		clauseAnalysis.caseAFollowingAxis(newFollowingAxis(newRangeSpec(10)));
		assertThat(context(), hasJoin(new Precedence(target(), 10)));
	}
	
	// #i .n,m #j, $i//following(n, m)::$j
	@Test
	public void caseAFollowingAxisRange() {
		setContext();
		setTarget();
		clauseAnalysis.caseAFollowingAxis(newFollowingAxis(newRangeSpec(10, 20)));
		assertThat(context(), hasJoin(new Precedence(target(), 10, 20)));
	}
	
	// #i _=_ #j, $i/matching-element::$j
	@Test
	public void caseAMatchingElementAxis() {
		setContext();
		setTarget();
		clauseAnalysis.caseAMatchingElementAxis(null);
		assertThat(context(), hasJoin(new SameSpan(target())));
	}
	
	// #i _l_ #j, $i/left-align::$j
	@Test
	public void caseALeftAlignAxis() {
		setContext();
		setTarget();
		clauseAnalysis.caseALeftAlignAxis(null);
		assertThat(context(), hasJoin(new LeftAlignment(target())));
	}
	
	// #i _r_ #j, $i/right-align::$j
	@Test
	public void caseARightAlignAxis() {
		setContext();
		setTarget();
		clauseAnalysis.caseARightAlignAxis(null);
		assertThat(context(), hasJoin(new RightAlignment(target())));
	}
	
	// #i _i_ #j, $i/containing::#j
	@Test
	public void caseAContainingAxis() {
		setContext();
		setTarget();
		clauseAnalysis.caseAContainingAxis(null);
		assertThat(context(), hasJoin(new Inclusion(target())));
	}
	
	// #i _ol_ #j, $i/overlapping-following::$j
	@Test
	public void caseAOverlappingFollowingAxis() {
		setContext();
		setTarget();
		clauseAnalysis.caseAOverlappingFollowingAxis(null);
		assertThat(context(), hasJoin(new LeftOverlap(target())));
	}
	
	// #i _or_ #j, $i/overlapping-preceding::$j
	@Test
	public void caseAOverlappingPrecedingAxis() {
		setContext();
		setTarget();
		clauseAnalysis.caseAOverlappingPrecedingAxis(null);
		assertThat(context(), hasJoin(new RightOverlap(target())));
	}
	
	// #i _o_ #j, $i/overlapping::$j
	@Test
	public void caseAOverlappingAxis() {
		setContext();
		setTarget();
		clauseAnalysis.caseAOverlappingAxis(null);
		assertThat(context(), hasJoin(new Overlap(target())));
	}
	
	// #i $ #j, $i/sibling::$j
	@Test
	public void caseASiblingAxis() {
		setContext();
		setTarget();
		clauseAnalysis.caseASiblingAxis(newSiblingAxis());
		assertThat(context(), hasJoin(new Sibling(target())));
	}
	
	// #i $* #j, $i/common-ancestor::$j
	@SuppressWarnings("unchecked")
	@Test
	public void caseAFollowingSiblingAxis() {
		setContext();
		setTarget();
		clauseAnalysis.caseACommonAncestorAxis(newCommonAncestorAxis());
		assertThat(context(), hasJoin(new CommonAncestor(target())));
	}

	@Test
	public void nodesCount() {
		final int COUNT = 3;
		
		// simulated COUNT generated nodes in analysis 
		List<QueryNode> nodes = new ArrayList<QueryNode>();
		for (int i = 1; i <= COUNT; ++i) 
			nodes.add(new QueryNode(i));
		clauseAnalysis.setNodes(nodes);
		
		assertThat(clauseAnalysis.nodesCount(), is(COUNT));
	}
	
	// tok, element()[isToken()]
	@Test
	public void caseAFunctionExprIsToken() {
		// check entire expression
		AStep step = newStep(newChildAxis(), newElementNodeTest(), newFunctionExpr("isToken"));
		Start start = newStart(newPathExpr(step));
		clauseAnalysis.caseStart(start);
		
		assertThat(target().isToken(), is(true));
	}
	
	// meta::namespace:name="value"
	@Test
	public void metaAnnotation() {
		QueryAnnotation expected = new QueryAnnotation(NAMESPACE, NAME, VALUE, TextMatching.EXACT_EQUAL);

		Start start = newStart(newPathExpr(newStep(newChildAxis(), newMetaNodeTest(NAMESPACE, NAME, newQuotedText(VALUE)))));
		clauseAnalysis.caseStart(start);

		assertThat(clauseAnalysis.getMetaAnnotations(), hasItem(expected));
		assertThat(clauseAnalysis.nodesCount(), is(0));
	}
	
	// meta::namespace:name=/value/
	@Test
	public void metaAnnotationRegexp() {
		QueryAnnotation expected = new QueryAnnotation(NAMESPACE, NAME, VALUE, TextMatching.REGEXP_EQUAL);

		Start start = newStart(newPathExpr(newStep(newChildAxis(), newMetaNodeTest(NAMESPACE, NAME, newRegexpQuotedText(VALUE)))));
		clauseAnalysis.caseStart(start);

		assertThat(clauseAnalysis.getMetaAnnotations(), hasItem(expected));
		assertThat(clauseAnalysis.nodesCount(), is(0));
	}
	
	///// Helper
	
	private QueryNode setTarget() {
		clauseAnalysis.setTarget(new QueryNode(1));
		return target();
	}
	
	private QueryNode setContext() {
		clauseAnalysis.setContext(new QueryNode(0));
		return context();
	}

	private QueryNode context() {
		return clauseAnalysis.getContext();
	}
	
	private QueryNode target() {
		return clauseAnalysis.getTarget();
	}

	private List<QueryNode> nodes() {
		return clauseAnalysis.getNodes();
	}
	
	private void testCompare(AEqComparison comp, PExpr lhs, PExpr rhs) {
		clauseAnalysis.caseAComparisonExpr(newComparisonExpr(comp, lhs, rhs));
	}

	///// Matcher
	
	private Matcher<QueryNode> spans(final String pattern, final TextMatching textMatching) {
		return new TypeSafeMatcher<QueryNode>() {

			@Override
			public boolean matchesSafely(QueryNode node) {
				return 
					pattern.equals(node.getSpannedText()) &&
					node.getSpanTextMatching() == textMatching;
			}

			public void describeTo(Description description) {
				String quote = textMatching.quote();
				description.appendText("node that spans ");
				description.appendText(quote);
				description.appendText(pattern);
				description.appendText(quote);
			}
			
		};
	}

	private Matcher<QueryNode> isNamed(final String namespace, final String name) {
		return new TypeSafeMatcher<QueryNode>() {

			@Override
			public boolean matchesSafely(QueryNode node) {
				if (namespace != null)
					return namespace.equals(node.getNamespace()) && name.equals(node.getNamespace()); 
				else
					return name.equals(node.getName());
			}

			public void describeTo(Description description) {
				description.appendText("node named: ");
				description.appendText(QueryNode.qName(namespace, name));
			}
			
		};
	}

	private Matcher<QueryNode> isMarked(final String marker) {
		return new TypeSafeMatcher<QueryNode>() {

			@Override
			public boolean matchesSafely(QueryNode node) {
				return marker.equals(node.getMarker());
			}

			public void describeTo(Description description) {
				description.appendText("node marked: ");
				description.appendText(marker);
			}
			
		};
	}
	
	private Matcher<QueryNode> hasAnnotation(String namespace, String name) {
		return hasAnnotation(namespace, name, null, null);
	}

	private Matcher<QueryNode> hasAnnotation(final String namespace, final String name, 
			final String value, final QueryNode.TextMatching textMatching) {
		return new TypeSafeMatcher<QueryNode>() {
			
			QueryAnnotation annotation = new QueryAnnotation(namespace, name, value, textMatching);

			@Override
			public boolean matchesSafely(QueryNode node) {
				return node.getNodeAnnotations().contains(annotation);
			}

			public void describeTo(Description description) {
				description.appendText("node with label: ");
				description.appendValue(annotation);
			}
			
		};
	}

	private Matcher<QueryNode> hasEdgeAnnotation(String namespace, String name) {
		return hasEdgeAnnotation(namespace, name, null, null);
	}

	private Matcher<QueryNode> hasEdgeAnnotation(final String namespace, final String name, 
			final String value, final QueryNode.TextMatching textMatching) {
		return new TypeSafeMatcher<QueryNode>() {
			
			QueryAnnotation annotation = new QueryAnnotation(namespace, name, value, textMatching);

			@Override
			public boolean matchesSafely(QueryNode node) {
				return node.getEdgeAnnotations().contains(annotation);
			}

			public void describeTo(Description description) {
				description.appendText("node with edge label: ");
				description.appendValue(annotation);
			}
			
		};
	}

	private Matcher<QueryNode> isRoot() {
		return new TypeSafeMatcher<QueryNode>() {
			
			@Override
			public boolean matchesSafely(QueryNode node) {
				return node.isRoot();
			}

			public void describeTo(Description description) {
				description.appendText("root node");
			}
			
		};
	}
	
	private Matcher<QueryNode> hasJoin(final Join expected) {
		
		return new TypeSafeMatcher<QueryNode>() {

			@Override
			public boolean matchesSafely(QueryNode node) {
				return node.getJoins().contains(expected);
			}

			public void describeTo(Description description) {
				description.appendValue(expected);
			}
			
		};
	}
	
}
