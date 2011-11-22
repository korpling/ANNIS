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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import annis.querymodel.AnnisNode;
import annis.querymodel.Annotation;

public class TestTableJoinsInFromClauseSqlGenerator {

	// class under test
	private TableJoinsInFromClauseSqlGenerator tableJoinsInFromClauseSqlGenerator;

	// dependencies
	private TableAccessStrategy tableAccessStrategy;
	
	// node that needs a from clause
	private AnnisNode node23;
	
	// dummy annotations
	@Mock Set<Annotation> annotations;
	
	@Before
	public void setup() {
		// setup class and dependencies
		initMocks(this);
		tableJoinsInFromClauseSqlGenerator = new TableJoinsInFromClauseSqlGenerator() {
			@Override
			protected TableAccessStrategy createTableAccessStrategy() {
				return tableAccessStrategy;
			}
		};
		
		// setup node
		node23 = new AnnisNode(23L);
		when(annotations.size()).thenReturn(3);
		
		// setup table aliases
		tableAccessStrategy = new TableAccessStrategy(node23);
		tableAccessStrategy.addTableAlias(NODE_TABLE, "_node");
		tableAccessStrategy.addTableAlias(RANK_TABLE, "_rank");
		tableAccessStrategy.addTableAlias(COMPONENT_TABLE, "_component");
		tableAccessStrategy.addTableAlias(NODE_ANNOTATION_TABLE, "_annotation");
		tableAccessStrategy.addTableAlias(EDGE_ANNOTATION_TABLE, "_rank_annotation");
	}
	
	// every node uses the node table
	@Test
	public void fromClauseDefault() {
		String expected = "_node AS _node23";
		assertFromClause(expected);
	}

	// if the node is root, use the rank table
	@Test
	public void fromClauseUsesRank() {
		node23.setRoot(true);

		String expected = 
			"_node AS _node23" + 
			" " +
			"JOIN _rank AS _rank23 ON (_rank23.node_ref = _node23.id)";		
		assertFromClause(expected);
	}
	
	// if the node is part of a dominance join, use the rank and component table
	@Test
	public void fromClauseUsesComponent() {
		node23.setPartOfEdge(true);

		String expected = 
			"_node AS _node23" + 
			" " +
			"JOIN _rank AS _rank23 ON (_rank23.node_ref = _node23.id) " +
			"JOIN _component AS _component23 ON (_component23.id = _rank23.component_ref)";		
		assertFromClause(expected);
	}
	
	// don't join rank table if the join is materialized
	@Test
	public void fromClauseUsesRankAndComponentAliasedToNode() {
		node23.setRoot(true);
		tableAccessStrategy.addTableAlias(COMPONENT_TABLE, "_node");
		tableAccessStrategy.addTableAlias(RANK_TABLE, "_node");

		String expected = "_node AS _node23";
		assertFromClause(expected);
	}
	
	// join the annotation table for each node annotation
	@Test
	public void fromClauseNodeAnnotations() {
		node23.setNodeAnnotations(annotations);
		
		String expected =
			"_node AS _node23 " + 
			"JOIN _annotation AS _annotation23_1 ON (_annotation23_1.node_ref = _node23.id) " +
			"JOIN _annotation AS _annotation23_2 ON (_annotation23_2.node_ref = _node23.id) " +
			"JOIN _annotation AS _annotation23_3 ON (_annotation23_3.node_ref = _node23.id)";
		assertFromClause(expected);
	}
	
	// join annotation table for each node annotation, use node table for first annotation if they are the same
	@Test
	public void fromClauseNodeAnnotationsAliasedToNode() {
		node23.setNodeAnnotations(annotations);
		tableAccessStrategy.addTableAlias(NODE_ANNOTATION_TABLE, "_node");
		tableAccessStrategy.addColumnAlias(NODE_ANNOTATION_TABLE, "node_ref", "id");
		
		String expected =
			"_node AS _node23_1 " +
			"JOIN _node AS _node23_2 ON (_node23_2.id = _node23_1.id) " +
			"JOIN _node AS _node23_3 ON (_node23_3.id = _node23_1.id)";
		assertFromClause(expected);
	}
	
	// join the rank table once and the edge annotation table for each edge annotation
	@Test
	public void fromClauseOfEdgeAnnotations() {
		node23.setEdgeAnnotations(annotations);
		
		String expected =
			"_node AS _node23 " + 
			"JOIN _rank AS _rank23 ON (_rank23.node_ref = _node23.id) " +
			"JOIN _component AS _component23 ON (_component23.id = _rank23.component_ref) " +
			"JOIN _rank_annotation AS _rank_annotation23_1 ON (_rank_annotation23_1.rank_ref = _rank23.pre) " +
			"JOIN _rank_annotation AS _rank_annotation23_2 ON (_rank_annotation23_2.rank_ref = _rank23.pre) " +
			"JOIN _rank_annotation AS _rank_annotation23_3 ON (_rank_annotation23_3.rank_ref = _rank23.pre)";
		assertFromClause(expected);
	}
	
	// join annotation table for each edge annotation, use rank table for first annotation if they are the same
	@Test
	public void fromClauseEdgeAnnotationsAliasedToNode() {
		node23.setEdgeAnnotations(annotations);
		tableAccessStrategy.addTableAlias(COMPONENT_TABLE, "_rank");
		tableAccessStrategy.addTableAlias(EDGE_ANNOTATION_TABLE, "_rank");
		tableAccessStrategy.addColumnAlias(EDGE_ANNOTATION_TABLE, "rank_ref", "pre");
		
		String expected =
			"_node AS _node23 " + 
			"JOIN _rank AS _rank23_1 ON (_rank23_1.node_ref = _node23.id) " +
			"JOIN _rank AS _rank23_2 ON (_rank23_2.pre = _rank23_1.pre) " +
			"JOIN _rank AS _rank23_3 ON (_rank23_3.pre = _rank23_1.pre)";
		assertFromClause(expected);
	}
	
	// use one node table for each annotation and nothing else if all tables are aliased to node
	@SuppressWarnings("unchecked")
	@Test
	public void fromClauseTablesAliasedToNode() {
		Set<Annotation> annotations1 = mock(Set.class);
		when(annotations1.size()).thenReturn(2);
		node23.setNodeAnnotations(annotations1);

		Set<Annotation> annotations2 = mock(Set.class);
		when(annotations2.size()).thenReturn(3);
		node23.setEdgeAnnotations(annotations2);

		tableAccessStrategy.addTableAlias(RANK_TABLE, "_node");
		tableAccessStrategy.addTableAlias(COMPONENT_TABLE, "_node");
		tableAccessStrategy.addTableAlias(NODE_ANNOTATION_TABLE, "_node");
		tableAccessStrategy.addColumnAlias(NODE_ANNOTATION_TABLE, "node_ref", "id");
		tableAccessStrategy.addTableAlias(EDGE_ANNOTATION_TABLE, "_node");
		tableAccessStrategy.addColumnAlias(EDGE_ANNOTATION_TABLE, "rank_ref", "pre");

		String expected =
			"_node AS _node23_1 " +
			"JOIN _node AS _node23_2 ON (_node23_2.id = _node23_1.id) " +
			"JOIN _node AS _node23_3 ON (_node23_3.pre = _node23_1.pre) " +
			"JOIN _node AS _node23_4 ON (_node23_4.pre = _node23_1.pre)";
		assertFromClause(expected);
	}	
	
	///// private helper
	
	private void assertFromClause(String expected) {
		assertEquals(expected, tableJoinsInFromClauseSqlGenerator.fromClauseForNode(node23, false));
	}

}
