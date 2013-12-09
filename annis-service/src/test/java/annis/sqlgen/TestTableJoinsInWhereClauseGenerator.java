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
package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.COMPONENT_TABLE;
import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;
import org.mockito.Mock;

import annis.model.QueryNode;
import annis.model.QueryAnnotation;
import org.junit.Before;


public class TestTableJoinsInWhereClauseGenerator {

	// an example node
	private QueryNode node23;

	// object under test
	private TableJoinsInWhereClauseGenerator generator;

	// dependencies
	@Mock private TableAccessStrategyFactory tableAccessStrategyFactory;
	private TableAccessStrategy tableAccessStrategy;
	
	// a few distinct annotations
	private static final QueryAnnotation annotation1 = new QueryAnnotation("namespace", "name1");
	private static final QueryAnnotation annotation2 = new QueryAnnotation("namespace", "name2");
	private static final QueryAnnotation annotation3 = new QueryAnnotation("namespace", "name3");
	private Set<QueryAnnotation> annotations;
  
	@Before
	public void setup() {
		initMocks(this);
		node23 = new QueryNode(23);
	
		generator = new TableJoinsInWhereClauseGenerator() {
			@Override
			protected TableAccessStrategy createTableAccessStrategy() {
				return tableAccessStrategy;
			}
		};

		// add column aliases to make sure they are used
		tableAccessStrategy = new TableAccessStrategy(node23);
		tableAccessStrategy.addTableAlias(NODE_TABLE, "_node");
		tableAccessStrategy.addTableAlias(RANK_TABLE, "_rank");
		tableAccessStrategy.addTableAlias(COMPONENT_TABLE, "_component");
		tableAccessStrategy.addTableAlias(NODE_ANNOTATION_TABLE, "_annotation");
		tableAccessStrategy.addTableAlias(EDGE_ANNOTATION_TABLE, "_rank_annotation");
		when(tableAccessStrategyFactory.tables(any(QueryNode.class))).thenReturn(tableAccessStrategy);
		
		annotations = annotationSet(annotation1, annotation2, annotation3); 

	}
	
	// every node uses the node table
	@Test
	public void fromClauseDefault() {
		String expected = "_node AS _node23";
		assertEquals(expected, generator.fromClauseForNode(node23));
	}

	// if the node is part of a dominance join, use the rank table
	@Test
	public void fromClauseUsesRank() {
		node23.setRoot(true);

		String expected = 
			"_node AS _node23, _rank AS _rank23, _component AS _component23";		
		assertEquals(expected, generator.fromClauseForNode(null, node23));
		checkWhereCondition("_rank23.node_ref = _node23.id", "_rank23.component_ref = _component23.id");
	}
	
	// don't join rank table if the join is materialized
	@Test
	public void fromClauseUsesRankAliasedToNode() {
		node23.setRoot(true);
		tableAccessStrategy.addTableAlias(RANK_TABLE, "_node");

		String expected = "_node AS _node23, _component AS _component23";
		assertEquals(expected, generator.fromClauseForNode(node23));
		checkWhereCondition("_node23.component_ref = _component23.id");
	}
	
	// don't join rank and component table if the join is materialized
	@Test
	public void fromClauseUsesRankComponentAliasedToNode() {
		node23.setRoot(true);
		tableAccessStrategy.addTableAlias(RANK_TABLE, "_node");
		tableAccessStrategy.addTableAlias(COMPONENT_TABLE, "_node");

		String expected = "_node AS _node23";
		assertEquals(expected, generator.fromClauseForNode(node23));
	}
	
	// join the annotation table for each node annotation
	@Test
	public void fromClauseNodeAnnotations() {
		node23.setNodeAnnotations(annotations);
		
		String expected = "_node AS _node23, _annotation AS _annotation23_1, _annotation AS _annotation23_2, _annotation AS _annotation23_3"; 
		assertEquals(expected, generator.fromClauseForNode(node23));
		checkWhereCondition(
				"_annotation23_1.node_ref = _node23.id",
				"_annotation23_2.node_ref = _node23.id",
				"_annotation23_3.node_ref = _node23.id"
		);
	}
	
	// join annotation table for each node annotation, use node table for first annotation if they are the same
	@Test
	public void fromClauseNodeAnnotationsAliasedToNode() {
		node23.setNodeAnnotations(annotations);
		tableAccessStrategy.addTableAlias(NODE_ANNOTATION_TABLE, "_node");
		tableAccessStrategy.addColumnAlias(NODE_ANNOTATION_TABLE, "node_ref", "id");
		
		String expected = "_node AS _node23_1, _node AS _node23_2, _node AS _node23_3";
		assertEquals(expected, generator.fromClauseForNode(node23));
		checkWhereCondition(
				"_node23_2.id = _node23_1.id",
				"_node23_3.id = _node23_1.id"
		);
	}

	// join the rank table once and the edge annotation table for each edge annotation
	@Test
	public void fromClauseOfEdgeAnnotations() {
		node23.setEdgeAnnotations(annotations);
		
		String expected = "_node AS _node23, _rank AS _rank23, _component AS _component23, _rank_annotation AS _rank_annotation23_1, _rank_annotation AS _rank_annotation23_2, _rank_annotation AS _rank_annotation23_3"; 
		assertEquals(expected, generator.fromClauseForNode(node23));
		checkWhereCondition(
				"_rank23.node_ref = _node23.id",
				"_rank23.component_ref = _component23.id",
				"_rank_annotation23_2.rank_ref = _rank23.pre",
				"_rank_annotation23_2.rank_ref = _rank23.pre",
				"_rank_annotation23_2.rank_ref = _rank23.pre"
		);
	}
	
	// join annotation table for each edge annotation, use rank table for first annotation if they are the same
	@Test
	public void fromClauseEdgeAnnotationsAliasedToRank() {
		node23.setEdgeAnnotations(annotations);
		tableAccessStrategy.addTableAlias(COMPONENT_TABLE, "_rank");
		tableAccessStrategy.addTableAlias(EDGE_ANNOTATION_TABLE, "_rank");
		tableAccessStrategy.addColumnAlias(EDGE_ANNOTATION_TABLE, "rank_ref", "pre");
		
		String expected = "_node AS _node23, _rank AS _rank23_1, _rank AS _rank23_2, _rank AS _rank23_3";
		assertEquals(expected, generator.fromClauseForNode(node23));
		checkWhereCondition(
				"_rank23_1.node_ref = _node23.id",
				"_rank23_2.pre = _rank23_1.pre",
				"_rank23_3.pre = _rank23_1.pre"
		);
	}
	
	// use one node table for each annotation and nothing else if all tables are aliased to node
	@Test
	public void fromClauseAllTablesAliasedToNode() {
		Set<QueryAnnotation> annotations1 = annotationSet(annotation1, annotation2);
		node23.setNodeAnnotations(annotations1);

		Set<QueryAnnotation> annotations2 = annotationSet(annotation1, annotation2, annotation3);
		node23.setEdgeAnnotations(annotations2);

		tableAccessStrategy.addTableAlias(RANK_TABLE, "_node");
		tableAccessStrategy.addTableAlias(COMPONENT_TABLE, "_node");
		tableAccessStrategy.addTableAlias(NODE_ANNOTATION_TABLE, "_node");
		tableAccessStrategy.addColumnAlias(NODE_ANNOTATION_TABLE, "node_ref", "id");
		tableAccessStrategy.addTableAlias(EDGE_ANNOTATION_TABLE, "_node");
		tableAccessStrategy.addColumnAlias(EDGE_ANNOTATION_TABLE, "rank_ref", "pre");

		String expected = "_node AS _node23_1, _node AS _node23_2, _node AS _node23_3, _node AS _node23_4";
		assertEquals(expected, generator.fromClauseForNode(node23));
		checkWhereCondition(
				"_node23_2.id = _node23_1.id",
				"_node23_3.pre = _node23_1.pre",
				"_node23_4.pre = _node23_1.pre"
		);
	}
	
	private TreeSet<QueryAnnotation> annotationSet(QueryAnnotation... annotations) {
		return new TreeSet<QueryAnnotation>(Arrays.asList(annotations));
	}

	///// Helper
	
	private void checkWhereCondition(String... expected) {
		Set<String> actual = generator.whereConditionsForNode(node23);
		for (String item : expected)
			assertThat(actual, hasItem(item));
	}

}
