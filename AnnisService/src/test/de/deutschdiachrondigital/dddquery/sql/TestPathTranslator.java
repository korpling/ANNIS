package de.deutschdiachrondigital.dddquery.sql;

import static de.deutschdiachrondigital.dddquery.helper.IsCollection.isCollection;
import static de.deutschdiachrondigital.dddquery.helper.IsCollectionContainingSubTypes.containsItem;
import static de.deutschdiachrondigital.dddquery.helper.IsCollectionEmpty.empty;
import static de.deutschdiachrondigital.dddquery.helper.IsCollectionSize.size;
import static de.deutschdiachrondigital.dddquery.helper.NodeLookup.lookup;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.deutschdiachrondigital.dddquery.helper.AstBuilder;
import de.deutschdiachrondigital.dddquery.node.AAndExpr;
import de.deutschdiachrondigital.dddquery.node.AAttributeNodeTest;
import de.deutschdiachrondigital.dddquery.node.AElementNodeTest;
import de.deutschdiachrondigital.dddquery.node.AEqComparison;
import de.deutschdiachrondigital.dddquery.node.AMarkerSpec;
import de.deutschdiachrondigital.dddquery.node.ANumberLiteralExpr;
import de.deutschdiachrondigital.dddquery.node.AOrExpr;
import de.deutschdiachrondigital.dddquery.node.APathExpr;
import de.deutschdiachrondigital.dddquery.node.ARegexpLiteralExpr;
import de.deutschdiachrondigital.dddquery.node.AStep;
import de.deutschdiachrondigital.dddquery.node.AStringLiteralExpr;
import de.deutschdiachrondigital.dddquery.node.Node;
import de.deutschdiachrondigital.dddquery.node.PExpr;
import de.deutschdiachrondigital.dddquery.node.PStep;
import de.deutschdiachrondigital.dddquery.sql.model.AliasSet;
import de.deutschdiachrondigital.dddquery.sql.model.Condition;
import de.deutschdiachrondigital.dddquery.sql.model.JoinField;
import de.deutschdiachrondigital.dddquery.sql.model.Literal;
import de.deutschdiachrondigital.dddquery.sql.model.Path;
import de.deutschdiachrondigital.dddquery.sql.model.RegexpLiteral;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.Alternative;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.Conjunction;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.IsNullCondition;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.Join;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.OrCondition;

public class TestPathTranslator {
	
	private AstBuilder b;
	private Path path;
	private AliasSet context;
	private AliasSet target;
	private PathTranslatorA translator;
	private AliasSetProvider aliasSetProvider;

	@Before
	public void setup() {
		b = new AstBuilder();
		
		translator = new PathTranslatorA();
		path = new Path();
		translator.setPath(path);
		
		aliasSetProvider = new AliasSetProvider();
		translator.setAliasSetProvider(aliasSetProvider);

		context = aliasSetProvider.getAliasSet();
		target = aliasSetProvider.getAliasSet();
		translator.setContext(context);
		translator.setTarget(target);
	}
	
	@Test
	public void inAPathExprNoContext() {
		translator.setContext(null);
		translator.setTarget(null);
		translator.inAPathExpr(b.newPathExpr(null, new PStep[] { b.newStep(null, null) } ));
		AliasSet target = translator.getTarget();
		assertThat(target, is(not(nullValue())));
		assertThat(path.getAliasSets(), isCollection(target));
	}
	
	@Test
	public void inAPathExprHasContext() {
		translator.inAPathExpr(b.newPathExpr(null, new PStep[] { b.newStep(null, null) } ));
		AliasSet target = translator.getTarget();
		assertThat(target, is(not(nullValue())));
		assertThat(path.getAliasSets(), empty());
	}
	
	@Test
	public void inAAbsolutePathType() {
		translator.inAAbsolutePathType(null);
		assertThat(path.getConditions(), isCollection(new IsNullCondition(target.getColumn("rank", "parent"))));
	}
	
	@Test
	public void inAStep() {
		translator.inAStep(b.newStep(null, null));
		AliasSet newContext = translator.getContext();
		AliasSet newTarget = translator.getTarget();
		assertThat(newContext, is(sameInstance(target)));
		assertThat(newTarget, is(not(sameInstance(target))));
		assertThat(path.getAliasSets(), hasItem(newTarget));
	}
	
	@Test
	public void inAStepNewVariable() {
		AStep node = b.newStep(null, null, null, null, "foo");
		translator.inAStep(node);

		AliasSet newContext = translator.getContext();
		AliasSet newTarget = translator.getTarget();
		assertThat(newContext, is(sameInstance(target)));
		assertThat(newTarget, is(not(sameInstance(target))));
		assertThat(path.getAliasSets(), hasItem(newTarget));
	}
	
	@Test
	public void inAStepKnownVariable() {
		AliasSet bound = aliasSetProvider.getAliasSet("foo");

		AStep node = b.newStep(null, null, null, null, "foo");
		translator.inAStep(node);
		
		AliasSet newContext = translator.getContext();
		AliasSet newTarget = translator.getTarget();
		assertThat(newContext, is(sameInstance(target)));
		assertThat(newTarget, is(not(sameInstance(target))));
		assertThat(path.getAliasSets(), not(hasItem(newTarget)));
		assertThat(newTarget, is(sameInstance(bound)));
	}
	
	@Test
	public void inAMarkerSpec() {
		AMarkerSpec node = b.newMarkerSpec("foo");
		translator.inAMarkerSpec(node);
		assertThat(path.getMarkings(), hasEntry(target, "foo"));
	}
	
	@Test
	public void inAAttributeAxis() {
		translator.inAAttributeAxis(null);
		assertThat(path.getConditions(), isCollection(Join.eq(context.getColumn("struct", "id"), target.getColumn("struct", "id"))));
	}
	
	@Test
	public void inAChildAxis() {
		translator.inAChildAxis(null);
		assertThat(path.getConditions(), isCollection(Join.eq(context.getColumn("rank", "pre"), target.getColumn("rank", "parent"))));
	}
	
	@Test
	public void inAParentAxis() {
		translator.inAParentAxis(null);
		assertThat(path.getAliasSets(), size(1));
		AliasSet helper = path.getAliasSets().get(0);
		assertThat(path.getConditions(), isCollection(
				Join.eq(context.getColumn("rank", "pre"), helper.getColumn("rank", "pre")),
				Join.eq(helper.getColumn("rank", "parent"), target.getColumn("rank", "pre"))
				));
	}
	
	// FIXME: lt
	@Test
	public void inADescendantAxis() {
		translator.inADescendantAxis(null);
		assertThat(path.getConditions(), isCollection(
				Join.le(context.getColumn("rank", "pre"), target.getColumn("rank", "pre")),
				Join.le(target.getColumn("rank", "pre"), context.getColumn("rank", "post"))));
	}
	
	// FIXME: gt
	@Test
	public void inAAncenstorAxis() {
		translator.inAAncestorAxis(null);
		assertThat(path.getAliasSets(), size(1));
		AliasSet helper = path.getAliasSets().get(0);
		assertThat(path.getConditions(), isCollection(
				Join.eq(context.getColumn("rank", "pre"), helper.getColumn("rank", "pre")),
				Join.ge(helper.getColumn("rank", "pre"), target.getColumn("rank", "pre")),
				Join.ge(target.getColumn("rank", "pre"), helper.getColumn("rank", "post"))
				));
	}
	
	@Test
	public void inASiblingAxis() {
		translator.inASiblingAxis(null);
		assertThat(path.getConditions(), isCollection(
				Join.eq(context.getColumn("rank", "parent"), target.getColumn("rank", "parent"))));
	}
	
	@Test
	public void inAFollowingAxis() {
		translator.inAFollowingAxis(null);
		assertThat(path.getConditions(), isCollection(
				Join.eq(context.getColumn("struct", "text_ref"), target.getColumn("struct", "text_ref")),
				Join.lt(context.getColumn("struct", "left"), target.getColumn("struct", "right"))));
	}
	
	@Test
	public void inAPrecedingAxis() {
		translator.inAPrecedingAxis(null);
		assertThat(path.getConditions(), isCollection(
				Join.eq(context.getColumn("struct", "text_ref"), target.getColumn("struct", "text_ref")),
				Join.gt(context.getColumn("struct", "right"), target.getColumn("struct", "left"))));
	}
	
	@Test
	public void inAFollowingSiblingAxis() {
		translator.inAFollowingSiblingAxis(null);
		assertThat(path.getConditions(), isCollection(
				Join.eq(context.getColumn("rank", "parent"), target.getColumn("rank", "parent")),
				Join.eq(context.getColumn("struct", "text_ref"), target.getColumn("struct", "text_ref")),
				Join.lt(context.getColumn("struct", "left"), target.getColumn("struct", "right"))));
	}
	
	@Test
	public void inAPrecedingSiblingAxis() {
		translator.inAPrecedingSiblingAxis(null);
		assertThat(path.getConditions(), isCollection(
				Join.eq(context.getColumn("rank", "parent"), target.getColumn("rank", "parent")),
				Join.eq(context.getColumn("struct", "text_ref"), target.getColumn("struct", "text_ref")),
				Join.gt(context.getColumn("struct", "right"), target.getColumn("struct", "left"))));
	}
	
	private void missingAxis() {
//		fail("missing axis");
	}
	
	@Test
	public void inAImmediatelyFollowingAxis() {
		missingAxis();
	}
	
	@Test
	public void inAImmediatelyPrecedingAxis() {
		missingAxis();
	}
	
	@Test
	public void inAImmediatelyFollowingSiblingAxis() {
		missingAxis();
	}
	
	@Test
	public void inAImmediatelyPrecedingSiblingAxis() {
		missingAxis();
	}
	
	@Test
	public void inAContainedAxis() {
		translator.inAContainedAxis(null);
		assertThat(path.getConditions(), isCollection(
				Join.ge(context.getColumn("struct", "right"), target.getColumn("struct", "right")),
				Join.le(context.getColumn("struct", "left"), target.getColumn("struct", "left"))));
	}
	
	@Test
	public void inAContainingAxis() {
		translator.inAContainingAxis(null);
		assertThat(path.getConditions(), isCollection(
				Join.le(context.getColumn("struct", "right"), target.getColumn("struct", "right")),
				Join.ge(context.getColumn("struct", "left"), target.getColumn("struct", "left"))));
	}
	
	@Test
	public void inAPrefix() {
		translator.inAPrefixAxis(null);
		assertThat(path.getConditions(), isCollection(
				Join.eq(context.getColumn("struct", "text_ref"), target.getColumn("struct", "text_ref")),
				Join.eq(context.getColumn("struct", "right"), target.getColumn("struct", "right")),
				Join.le(context.getColumn("struct", "left"), target.getColumn("struct", "left"))));
	}

	@Test
	public void inASuffix() {
		translator.inASuffixAxis(null);
		assertThat(path.getConditions(), isCollection(
				Join.eq(context.getColumn("struct", "text_ref"), target.getColumn("struct", "text_ref")),
				Join.ge(context.getColumn("struct", "right"), target.getColumn("struct", "right")),
				Join.eq(context.getColumn("struct", "left"), target.getColumn("struct", "left"))));
	}
	
	@Test
	public void inAOverlapping() {
		translator.inAOverlappingAxis(null);

		Alternative alternative = new Alternative();
		Conjunction conjunction1 = new Conjunction();
		conjunction1.addCondition(Join.ge(context.getColumn("struct", "left"), target.getColumn("struct", "left")));
		conjunction1.addCondition(Join.le(context.getColumn("struct", "left"), target.getColumn("struct", "left")));
		Conjunction conjunction2 = new Conjunction();
		conjunction2.addCondition(Join.ge(context.getColumn("struct", "right"), target.getColumn("struct", "right")));
		conjunction2.addCondition(Join.le(context.getColumn("struct", "right"), target.getColumn("struct", "left")));
		alternative.addCondition(conjunction1);
		alternative.addCondition(conjunction2);

		assertThat(path.getConditions(), isCollection(
				Join.eq(context.getColumn("struct", "text_ref"), target.getColumn("struct", "text_ref")),
				alternative));
	}

	@Test
	public void inAOverlappingFollowing() {
		translator.inAOverlappingFollowingAxis(null);

		assertThat(path.getConditions(), isCollection(
				Join.eq(context.getColumn("struct", "text_ref"), target.getColumn("struct", "text_ref")),
				Join.ge(context.getColumn("struct", "left"), target.getColumn("struct", "left")),
				Join.le(context.getColumn("struct", "left"), target.getColumn("struct", "left"))));
	}

	@Test
	public void inAOverlappingPreceding() {
		translator.inAOverlappingPrecedingAxis(null);

		assertThat(path.getConditions(), isCollection(
				Join.eq(context.getColumn("struct", "text_ref"), target.getColumn("struct", "text_ref")),
				Join.ge(context.getColumn("struct", "right"), target.getColumn("struct", "right")),
				Join.le(context.getColumn("struct", "right"), target.getColumn("struct", "left"))));
	}
	
	@Test
	public void inAMatchingElementAxis() {
		translator.inAMatchingElementAxis(null);
		assertThat(path.getConditions(), isCollection(
				Join.eq(context.getColumn("struct", "text_ref"), target.getColumn("struct", "text_ref")),
				Join.eq(context.getColumn("struct", "left"), target.getColumn("struct", "left")),
				Join.eq(context.getColumn("struct", "right"), target.getColumn("struct", "right"))));
	}

	@Test
	public void inALeftAlignAxis() {
		translator.inALeftAlignAxis(null);
		assertThat(path.getConditions(), isCollection(
				Join.eq(context.getColumn("struct", "text_ref"), target.getColumn("struct", "text_ref")),
				Join.eq(context.getColumn("struct", "left"), target.getColumn("struct", "left"))));
	}
	
	@Test
	public void inARightAlignAxis() {
		translator.inARightAlignAxis(null);
		assertThat(path.getConditions(), isCollection(
				Join.eq(context.getColumn("struct", "text_ref"), target.getColumn("struct", "text_ref")),
				Join.eq(context.getColumn("struct", "right"), target.getColumn("struct", "right"))));
	}
	
	@Test
	public void inAElementNodeTest() {
		AElementNodeTest node = b.newElementNodeTest("a");
		translator.inAElementNodeTest(node);
		assertThat(path.getConditions(), isCollection(Join.eq(target.getColumn("struct", "name"), "'a'")));
	}
	
	@Test
	public void inAElementNodeTestNoName() {
		AElementNodeTest node = b.newElementNodeTest(null);
		translator.inAElementNodeTest(node);
		
		assertThat(path.getConditions(), empty());
		assertThat(target.usesTable("struct"), is(false));
	}

	@Test
	public void inAAttributeNodeTestNoName() {
		AAttributeNodeTest node = b.newAttributeNodeTest(null);
		translator.inAAttributeNodeTest(node);

		assertThat(path.getConditions(), empty());
		assertThat(target.usesTable("anno"), is(true));
		assertThat(target.usesTable("struct"), is(true));
	}
	
	@Test
	public void inAAttributeNodeTest() {
		AAttributeNodeTest node = b.newAttributeNodeTest("a");
		translator.inAAttributeNodeTest(node);

		assertThat(path.getConditions(), isCollection(Join.eq(target.getColumn("anno_attribute", "name"), "'a'")));
		assertThat(target.usesTable("anno"), is(true));
		assertThat(target.usesTable("struct"), is(true));
	}

	@Test
	public void textValueStringLiteral() {
		AStringLiteralExpr node = b.newStringLiteralExpr("foo");
		assertThat(translator.textValue(node).toString(), is("'foo'"));
	}
	
	@Test
	public void textValueStringLiteralEmpty() {
		AStringLiteralExpr node = b.newStringLiteralExpr(null);
		assertThat(translator.textValue(node).toString(), is("''"));
	}
	
	@Test
	public void textValueRegexpLiteral() {
		ARegexpLiteralExpr node = b.newRegexpLiteralExpr("regexp");
		JoinField textValue = translator.textValue(node);
		assertThat(textValue.toString(), is("'regexp'"));
		assertThat(textValue, instanceOf(RegexpLiteral.class));
	}
	
	@Test
	public void textValueNumberLiteral() {
		ANumberLiteralExpr node = b.newNumberLiteralExpr(1);
		assertThat(translator.textValue(node).toString(), is("1"));
	}
	
	@Test
	public void textValuePathElement() {
		APathExpr node = (APathExpr) lookup("child::element()", APathExpr.class);
		
		JoinField textValue = translator.textValue(node);
		assertThat(textValue.toString(), is("struct3.span"));
	
		assertThat(path.getConditions(), containsItem(Join.eq("rank2.pre", "rank3.parent")));
	}
	
	@Test
	public void textValuePathAttribute() {
		APathExpr node = (APathExpr) lookup("attribute::attribute(foo)", APathExpr.class);
		
		assertThat(translator.textValue(node).toString(), is("anno_attribute3.value"));
		
		assertThat(path.getConditions(), containsItem(Join.eq("anno_attribute3.name", "'foo'")));
	}
	
	@Test
	public void caseAComparisonExpr() {
		PathTranslatorA translator = new PathTranslatorA() {

			int i = 0;
			String[] values = { "lhs", "rhs" };

			@Override
			public JoinField textValue(Node node) {
				return new Literal(values[i++ % 2]);
			}
			
			@Override
			public void inAEqComparison(AEqComparison node) {
				setComparison("joinOp");
			}
		};
		translator.setPath(path);
		translator.caseAComparisonExpr(b.newComparisonExpr(b.newEqComparison(), null, null));
		assertThat(path.getConditions(), containsItem(new Join("joinOp", "lhs", "rhs")));
	}
	
	@Test
	public void caseAComparisonExprRegexp() {
		PathTranslatorA translator = new PathTranslatorA() {

			int i = 0;
			String[] values = { "lhs", "rhs" };

			@Override
			public JoinField textValue(Node node) {
				return new RegexpLiteral(values[i++ % 2]);
			}
			
			@Override
			public void inAEqComparison(AEqComparison node) {
				setComparison("=");
			}
		};
		translator.setPath(path);
		translator.caseAComparisonExpr(b.newComparisonExpr(b.newEqComparison(), null, null));
		assertThat(path.getConditions(), containsItem(new Join("~", "lhs", "rhs")));
	}
	
	@Test
	public void inAEqComparison() {
		translator.inAEqComparison(null);
		assertThat(translator.getComparison(), is("="));
	}

	@Test
	public void inANeComparison() {
		translator.inANeComparison(null);
		assertThat(translator.getComparison(), is("!="));
	}

	@Test
	public void inALtComparison() {
		translator.inALtComparison(null);
		assertThat(translator.getComparison(), is("<"));
	}

	@Test
	public void inALeComparison() {
		translator.inALeComparison(null);
		assertThat(translator.getComparison(), is("<="));
	}

	@Test
	public void inAGtComparison() {
		translator.inAGtComparison(null);
		assertThat(translator.getComparison(), is(">"));
	}

	@Test
	public void inAGeComparison() {
		translator.inAGeComparison(null);
		assertThat(translator.getComparison(), is(">="));
	}
	
	class MockTranslator extends PathTranslatorA {
		boolean called = false;
	}
	
	@Test
	public void caseAStepCallsInAStep() {
		MockTranslator translator = new MockTranslator() {
			@Override
			public void inAStep(AStep node) {
				called = true;
			}
		};
		translator.caseAStep(b.newStep(null, null));
		assertThat(translator.called, is(true));
	}
	
	@Test
	public void caseAStepWithPredicatesSavesTarget() {
		PExpr[] predicates = {
				b.newPathExpr(null, new PStep[] {
						b.newStep(b.newChildAxis(), b.newElementNodeTest("a")),	
						b.newStep(b.newChildAxis(), b.newElementNodeTest("b")),	
				}),
				b.newPathExpr(null, new PStep[] {
						b.newStep(b.newChildAxis(), b.newElementNodeTest("c")),	
						b.newStep(b.newChildAxis(), b.newElementNodeTest("d")),	
				})

		};
		AStep node = b.newStep(null, null, null, predicates, null);
		translator.caseAStep(node);
		assertThat(translator.getTarget().getId(), is(3));
	}
	
	@Test
	public void inAAndExprSavesTarget() {
		PExpr[] predicates = {
				b.newPathExpr(null, new PStep[] {
						b.newStep(b.newChildAxis(), b.newElementNodeTest("a")),	
						b.newStep(b.newChildAxis(), b.newElementNodeTest("b")),	
				}),
				b.newPathExpr(null, new PStep[] {
						b.newStep(b.newChildAxis(), b.newElementNodeTest("c")),	
						b.newStep(b.newChildAxis(), b.newElementNodeTest("d")),	
				})

		};
		AAndExpr node = b.newAndExpr(predicates);
		translator.caseAAndExpr(node);
		assertThat(translator.getTarget(), is(sameInstance(target)));
	}
	
	@Test
	public void inAOrExpr() {
		AOrExpr node = b.newOrExpr(new PExpr[] {
				b.newComparisonExpr(b.newEqComparison(), 
						b.newPathExpr(null, new PStep[] {
								b.newStep(b.newAttributeAxis(), b.newAttributeNodeTest("sentence"))
						}),
						b.newStringLiteralExpr("S")),
				b.newComparisonExpr(b.newEqComparison(), 
						b.newPathExpr(null, new PStep[] {
								b.newStep(b.newAttributeAxis(), b.newAttributeNodeTest("foo"))
						}),
						b.newStringLiteralExpr("bar")) });
		translator.caseAOrExpr(node);
	
		assertThat(translator.getTarget(), is(sameInstance(target)));
		assertThat(path.getConditions(), size(1));
		
		OrCondition condition = (OrCondition) path.getConditions().get(0);
		assertThat(condition.getLhs(), is(target.getColumn("rank", "pre")));
		
		List<Path> alternatives = condition.getAlternatives();
		assertThat(alternatives, size(2));
		
		Path path1 = alternatives.get(0);
		List<Condition> conditions1 = path1.getConditions();
		assertThat(conditions1, isCollection(
				Join.eq("struct3.id", "struct4.id"),
				Join.eq("anno_attribute4.name", "'sentence'"),
				Join.eq("anno_attribute4.value", "'S'")));
		List<AliasSet> aliasSet1 = path1.getAliasSets();
		assertThat(aliasSet1, isCollection(new AliasSet(3), new AliasSet(4)));

		Path path2 = alternatives.get(1);
		List<Condition> conditions2 = path2.getConditions();
		assertThat(conditions2, isCollection(
				Join.eq("struct5.id", "struct6.id"),
				Join.eq("anno_attribute6.name", "'foo'"),
				Join.eq("anno_attribute6.value", "'bar'")));
		List<AliasSet> aliasSet2 = path2.getAliasSets();
		assertThat(aliasSet2, isCollection(new AliasSet(5), new AliasSet(6)));
	}
	
}
