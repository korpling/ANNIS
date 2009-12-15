
package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.COMPONENT_TABLE;
import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static test.IsCollectionSize.size;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnisNode.TextMatching;
import annis.sqlgen.model.Dominance;
import annis.sqlgen.model.Inclusion;
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

/*
 * FIXME: refactor tests, so they use the same condition constants everywhere
 * also, get rid of stupid helper functions like join (dup code)
 */
public class TestDefaultWhereClauseSqlGenerator {

	// an example node
	private AnnisNode node23;
	private AnnisNode node42;

	// object under test: the adapter to that node
	private DefaultWhereClauseSqlGenerator generator;

	// dependencies
	@Mock private TableAccessStrategyFactory tableAccessStrategyFactory;
	
	// more constants for easier testing
	private final static String NAME = "name";
	
	// dummy annotation set
	@Mock Set<Annotation> annotations;
	
	@Before
	public void setup() {
		initMocks(this);
		node23 = new AnnisNode(23);
		node42 = new AnnisNode(42);
	
		generator = new DefaultWhereClauseSqlGenerator();
		generator.setTableAccessStrategyFactory(tableAccessStrategyFactory);

		// add table aliases to make sure they are used for both nodes
		for (AnnisNode node : Arrays.asList(node23, node42)) {
			TableAccessStrategy tableAccessStrategy = new TableAccessStrategy(node);
			tableAccessStrategy.addTableAlias(NODE_TABLE, "_node");
			tableAccessStrategy.addTableAlias(COMPONENT_TABLE, "_component");
			tableAccessStrategy.addTableAlias(RANK_TABLE, "_rank");
			tableAccessStrategy.addTableAlias(NODE_ANNOTATION_TABLE, "_annotation");
			tableAccessStrategy.addTableAlias(EDGE_ANNOTATION_TABLE, "_rank_annotation");
			when(tableAccessStrategyFactory.createTableAccessStrategy(node)).thenReturn(tableAccessStrategy);
		}

		// simulate three annotations
		when(annotations.size()).thenReturn(3);
	}
	
	// WHERE condition for root node
	@Test
	public void whereClauseForNodeRoot() {
		node23.setRoot(true);
		checkWhereCondition("_rank23.root IS TRUE");
	}

	// WHERE condition for namespace
	@Test
	public void whereClauseForNodeNamespace() {
		node23.setNamespace("namespace");
		checkWhereCondition(join("=", "_node23.namespace", "'namespace'"));
	}
	
	// WHERE condition for name
	@Test
	public void whereClauseForNodeName() {
		node23.setName("name");
		checkWhereCondition(join("=", "_node23.name", "'name'"));
	}
	
	// WHERE condition for spanned text (string)
	@Test
	public void whereClauseForNodeSpanString() {
		node23.setSpannedText("string", TextMatching.EXACT);
		checkWhereCondition(join("=", "_node23.span", "'string'"));
	}
	
	// WHERE condition for spanned text (regexp) 
	@Test
	public void whereClauseForNodeSpanRegexp() {
		node23.setSpannedText("regexp", TextMatching.REGEXP);
		checkWhereCondition(join("~", "_node23.span", "'^regexp$'"));
	}
	
	// WHERE condition for node annotation
	@Test
	public void whereClauseForNodeAnnotation() {
		node23.addNodeAnnotation(new Annotation("namespace1", "name1"));
		node23.addNodeAnnotation(new Annotation("namespace2", "name2", "value2", TextMatching.EXACT));
		node23.addNodeAnnotation(new Annotation("namespace3", "name3", "value3", TextMatching.REGEXP));
		checkWhereCondition(
				join("=", "_annotation23_1.namespace", "'namespace1'"),
				join("=", "_annotation23_1.name", "'name1'"),
				join("=", "_annotation23_2.namespace", "'namespace2'"),
				join("=", "_annotation23_2.name", "'name2'"),
				join("=", "_annotation23_2.value", "'value2'"),
				join("=", "_annotation23_3.namespace", "'namespace3'"),
				join("=", "_annotation23_3.name", "'name3'"),
				join("~", "_annotation23_3.value", "'^value3$'")
		);
	}
	
	// WHERE condition for node annotation
	@Test
	public void whereClauseForNodeEdgeAnnotation() {
		node23.addEdgeAnnotation(new Annotation("namespace1", "name1"));
		node23.addEdgeAnnotation(new Annotation("namespace2", "name2", "value2", TextMatching.EXACT));
		node23.addEdgeAnnotation(new Annotation("namespace3", "name3", "value3", TextMatching.REGEXP));
		checkWhereCondition(
				join("=", "_rank_annotation23_1.namespace", "'namespace1'"),
				join("=", "_rank_annotation23_1.name", "'name1'"),
				join("=", "_rank_annotation23_2.namespace", "'namespace2'"),
				join("=", "_rank_annotation23_2.name", "'name2'"),
				join("=", "_rank_annotation23_2.value", "'value2'"),
				join("=", "_rank_annotation23_3.namespace", "'namespace3'"),
				join("=", "_rank_annotation23_3.name", "'name3'"),
				join("~", "_rank_annotation23_3.value", "'^value3$'")
		);
	}
	
	// WHERE condition for isToken
	@Test
	public void whereClauseForNodeIsToken() {
		node23.setToken(true);
		checkWhereCondition("_node23.token_index IS NOT NULL");
	}
	
	// WHERE condition for _=_
	@Test
	public void whereClauseForNodeSameSpan() {
		node23.addJoin(new SameSpan(node42));
		checkWhereCondition(
				join("=", "_node23.text_ref", "_node42.text_ref"),
				join("=", "_node23.left", "_node42.left"),
				join("=", "_node23.right", "_node42.right")
		);
	}
	
	// WHERE condition for _l_
	@Test
	public void whereClauseForNodeLeftAlignment() {
		node23.addJoin(new LeftAlignment(node42));
		checkWhereCondition(
				join("=", "_node23.text_ref", "_node42.text_ref"),
				join("=", "_node23.left", "_node42.left")
		);
	}
	
	// WHERE condition for _r_
	@Test
	public void whereClauseForNodeRightAlignment() {
		node23.addJoin(new RightAlignment(node42));
		checkWhereCondition(
				join("=", "_node23.text_ref", "_node42.text_ref"),
				join("=", "_node23.right", "_node42.right")
		);
	}
	
	// WHERE condition for _i_
	@Test
	public void whereClauseForNodeInclusion() {
		node23.addJoin(new Inclusion(node42));
		checkWhereCondition(
				join("=", "_node23.text_ref", "_node42.text_ref"),
				join("<=", "_node23.left", "_node42.left"),
				join(">=", "_node23.right", "_node42.right")
		);
	}
	
	// WHERE condition for _ol_
	@Test
	public void whereClauseForNodeLeftOverlap() {
		node23.addJoin(new LeftOverlap(node42));
		checkWhereCondition(
				join("=", "_node23.text_ref", "_node42.text_ref"),
				join("<=", "_node23.left", "_node42.left"),
				join("<=", "_node42.left", "_node23.right"),
				join("<=", "_node23.right", "_node42.right")
		);
	}
	
	// WHERE condition for _or_
	// FIXME: unnecessary, is exchanged for #2 _ol_ #2
	@Test
	public void whereClauseForNodeRightOverlap() {
		node23.addJoin(new RightOverlap(node42));
		checkWhereCondition(
				join("=", "_node23.text_ref", "_node42.text_ref"),
				join(">=", "_node23.right", "_node42.right"),
				join(">=", "_node42.right", "_node23.left"),
				join(">=", "_node23.left", "_node42.left")
		);
	}
	
	// WHERE condition for _o_
	@Test
	public void whereClauseForNodeOverlap() {
		node23.addJoin(new Overlap(node42));
		checkWhereCondition(
				join("=", "_node23.text_ref", "_node42.text_ref"),
				join("<=", "_node23.left", "_node42.right"),
				join("<=", "_node42.left", "_node23.right")
		);
	}
	
	// WHERE condition for .
	@Test
	public void whereClauseForNodeDirectPrecedence() {
		node23.addJoin(new Precedence(node42, 1));
		checkWhereCondition(
				join("=", "_node23.text_ref", "_node42.text_ref"),
				join("=", "_node23.right_token", "_node42.left_token", -1)
		);
	}
	
	// WHERE condition for .*
	@Test
	public void whereClauseForNodeIndirectPrecedence() {
		node23.addJoin(new Precedence(node42));
		checkWhereCondition(
				join("=", "_node23.text_ref", "_node42.text_ref"),
				join("<", "_node23.right_token", "_node42.left_token")
		);
	}
	
	// WHERE condition for .n
	@Test
	public void whereClauseForNodeExactPrecedence() {
		node23.addJoin(new Precedence(node42, 10));
		checkWhereCondition(
				join("=", "_node23.text_ref", "_node42.text_ref"),
				join("=", "_node23.right_token", "_node42.left_token", -10));
	}
	
	// WHERE condition for .n,m
	@Test
	public void whereClauseForNodeRangedPrecedence() {
		node23.addJoin(new Precedence(node42, 10, 20));
		checkWhereCondition(
				join("=", "_node23.text_ref", "_node42.text_ref"),
				"_node23.right_token BETWEEN SYMMETRIC _node42.left_token - 10 AND _node42.left_token - 20"
		);
	}
	
	// WHERE condition for >
	@Test
	public void whereClauseForNodeDirectDominance() {
		node23.addJoin(new Dominance(node42, 1));
		checkWhereCondition(
//				join("=", "_rank23.component_ref", "_rank42.component_ref"),
				join("=", "_component23.type", "'d'"),
				"_component23.name IS NULL",
				join("=", "_rank23.pre", "_rank42.parent")
		);
	}
	
	// WHERE condition for > name
	@Test
	public void whereClauseDirectDominanceNamed() {
		node23.addJoin(new Dominance(node42, NAME, 1));
		checkWhereCondition(
//				join("=", "_rank23.component_ref", "_rank42.component_ref"),
				join("=", "_component23.type", "'d'"),
				join("=", "_component23.name", "'" + NAME + "'"),
				join("=", "_rank23.pre", "_rank42.parent")
		);
	}
	
	// WHERE condition for > name [annotation]
	@Test
	public void whereClauseDirectDominanceNamedAndAnnotated() {
		node23.addJoin(new Dominance(node42, NAME, 1));
		node42.addNodeAnnotation(new Annotation("namespace3", "name3", "value3", TextMatching.REGEXP));
		checkWhereCondition(
//				join("=", "_rank23.component_ref", "_rank42.component_ref"),
				join("=", "_component23.type", "'d'"),
				join("=", "_component23.name", "'" + NAME + "'"),
				join("=", "_rank23.pre", "_rank42.parent")
		);
		checkWhereCondition(node42,
				join("=", "_annotation42.namespace", "'namespace3'"),
				join("=", "_annotation42.name", "'name3'"),
				join("~", "_annotation42.value", "'^value3$'")
		);
	}
	
	// WHERE condition for >*
	@Test
	public void whereClauseForNodeIndirectDominance() {
		node23.addJoin(new Dominance(node42));
		checkWhereCondition(
//				join("=", "_rank23.component_ref", "_rank42.component_ref"),
				join("=", "_component23.type", "'d'"),
				"_component23.name IS NULL",
				join("<", "_rank23.pre", "_rank42.pre"),
				join("<", "_rank42.pre", "_rank23.post")
		);
	}
	
	// WHERE condition for >n
	@Test
	public void whereClauseForNodeExactDominance() {
		node23.addJoin(new Dominance(node42, 10));
		checkWhereCondition(
//				join("=", "_rank23.component_ref", "_rank42.component_ref"),
				join("=", "_component23.type", "'d'"),
				"_component23.name IS NULL",
				join("<", "_rank23.pre", "_rank42.pre"),
				join("<", "_rank42.pre", "_rank23.post"),
				join("=", "_rank23.level", "_rank42.level", -10)
		);
	}
	
	// WHERE condition for >n,m
	@Test
	public void whereClauseForNodeRangedDominance() {
		node23.addJoin(new Dominance(node42, 10, 20));
		checkWhereCondition(
//				join("=", "_rank23.component_ref", "_rank42.component_ref"),
				join("=", "_component23.type", "'d'"),
				"_component23.name IS NULL",
				join("<", "_rank23.pre", "_rank42.pre"),
				join("<", "_rank42.pre", "_rank23.post"),
				"_rank23.level BETWEEN SYMMETRIC _rank42.level - 10 AND _rank42.level - 20"

		);
	}
	
	// WHERE condition for >@l
	@Test
	public void whereClauseForNodeLeftDominance() {
		node23.addJoin(new LeftDominance(node42));
		checkWhereCondition(
				join("=", "_rank23.pre", "_rank42.pre", -1),
				join("=", "_component23.type", "'d'"),
				"_component23.name IS NULL"
		);
	}
	
	// WHERE condition for >@r
	@Test
	public void whereClauseForNodeRightDominance() {
		node23.addJoin(new RightDominance(node42));
		checkWhereCondition(
				join("=", "_rank23.post", "_rank42.post", 1),
				join("=", "_component23.type", "'d'"),
				"_component23.name IS NULL"
		);
	}
	
	// WHERE condition for ->
	@Test
	public void whereClauseDirectPointingRelation() {
		node23.addJoin(new PointingRelation(node42, NAME, 1));
		checkWhereCondition(
//				join("=", "_rank23.component_ref", "_rank42.component_ref"),
				join("=", "_component23.type", "'p'"),
				join("=", "_component23.name", "'" + NAME + "'"),
				join("=", "_rank23.pre", "_rank42.parent")

		);
	}
	
	// WHERE condition for ->*
	@Test
	public void whereClauseIndirectPointingRelation() {
		node23.addJoin(new PointingRelation(node42, NAME));
		checkWhereCondition(
//				join("=", "_rank23.component_ref", "_rank42.component_ref"),
				join("=", "_component23.type", "'p'"),
				join("=", "_component23.name", "'" + NAME + "'"),
				join("<", "_rank23.pre", "_rank42.pre"),
				join("<", "_rank42.pre", "_rank23.post")
		);
	}
	
	// WHERE condition for $
	@Test
	public void whereClauseForNodeSibling() {
		node23.addJoin(new Sibling(node42));
		checkWhereCondition(
				join("=", "_rank23.parent", "_rank42.parent"),
				join("=", "_component23.type", "'d'"),
				"_component23.name IS NULL"
		);
	}
	
	///// Helper
	

	
	private void checkWhereCondition(String... expected) {
		checkWhereCondition(node23, expected);
	}

	private void checkWhereCondition(AnnisNode node, String... expected) {
		List<String> actual = generator.whereConditions(node, null, null);
		for (String item : expected)
			assertThat(actual, hasItem(item));
		assertThat(actual, is(size(expected.length)));
	}

	private String join(String op, String lhs, String rhs) {
		return lhs + " " + op + " " + rhs;
	}
	
	private String join(String op, String lhs, String rhs, int offset) {
		String plus = offset >= 0 ? " + " : " - ";
		return join(op, lhs, rhs) + plus + String.valueOf(Math.abs(offset));
	}
	
}
