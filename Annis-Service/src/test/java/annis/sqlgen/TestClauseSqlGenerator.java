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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import annis.model.AnnisNode;
import annis.model.Annotation;


public class TestClauseSqlGenerator {

	// class under test
	private ClauseSqlGenerator generator;

	// two example nodes in a list
	private List<AnnisNode> nodes;
	private AnnisNode node23;
	private AnnisNode node42;
	final private int MAX_WIDTH = 2;
	
	@Before
	public void setup() {
		initMocks(this);

		// some nodes
		nodes = new ArrayList<AnnisNode>();
		node23 = createNode(23);
		node42 = createNode(42);
		
		// create object under test and wire dependencies
		generator = new ClauseSqlGenerator();
	}

	// create a node and add it to default nodes list
	private AnnisNode createNode(int aliasCount) {
		AnnisNode node = new AnnisNode(aliasCount);
		nodes.add(node);
		return node;
	}
	
	///// sanity checks
	
	// nodes must not be empty
	@Test(expected=IllegalArgumentException.class)
	public void toSqlNodesSizeZero() {
		generator.toSql(new ArrayList<AnnisNode>(), 1, null, null);
	}
	
	// maxWidth must be at least the number of nodes
	@Test(expected=IllegalArgumentException.class)
	public void toSqlMaxWidthLessThanNodesSize() {
		generator.toSql(nodes, nodes.size() - 1, null, null);
	}
	
	///// flow control
	
	// SQL query consists of SELECT, FROM and (possibly empty) WHERE clause 
	@Test
	public void toSqlFlowControl() {
		// stub the generation of the SELECT, FROM and WHERE clauses 
		// stub the generation of SqlTableNodeAdapter for each node
		// alias global nodes to verify that it was passed to called functions
		final String selectClause = "SELECT ... ";
		final String fromClause = "FROM ... ";
		final String whereClause = "WHERE ... ";
		final List<AnnisNode> aliasedNodes = nodes;
		ClauseSqlGenerator stubbedClauseSqlGenerator = new ClauseSqlGenerator() {

			@Override
			void appendSelectClause(StringBuffer sb, List<AnnisNode> nodes, int maxWidth) {
				assertThat(nodes, is(sameInstance(aliasedNodes)));
				sb.append(selectClause);
			}
			
			@Override
			void appendFromClause(StringBuffer sb, List<AnnisNode> nodes) {
				assertThat(nodes, is(sameInstance(aliasedNodes)));
				sb.append(fromClause);
			}
			
			@Override
			void appendWhereClause(StringBuffer sb, List<AnnisNode> nodes, List<Long> corpusList, List<Long> documents) {
				assertThat(nodes, is(sameInstance(aliasedNodes)));
				sb.append(whereClause);
			}
		};

		String sql = stubbedClauseSqlGenerator.toSql(aliasedNodes, MAX_WIDTH, null, null);
		assertThat(sql, is(selectClause + fromClause + whereClause));
	}
	
	///// SELECT clause
	
	@SuppressWarnings("unchecked")
	@Test
	public void appendSelectClause() {
		// setup a dummy SelectClauseSqlGenerator
		SelectClauseSqlGenerator selectClauseSqlGenerator = mock(SelectClauseSqlGenerator.class);
		generator.setSelectClauseSqlGenerators(Arrays.asList(selectClauseSqlGenerator));
		
		final String SELECT_CLAUSE = "SELECT CLAUSE";
		when(selectClauseSqlGenerator.selectClause(anyList(), anyInt())).thenReturn(SELECT_CLAUSE);
		
		String expected = "SELECT " + SELECT_CLAUSE + "\n";
		
		StringBuffer sb = new StringBuffer();
		generator.appendSelectClause(sb, nodes, MAX_WIDTH);
		assertEquals(expected, sb.toString());
	}
	
	///// FROM clause
	
	// FROM clause consists of string "FROM" followed by table definitions for a node, separated by ","
	@Test
	public void appendFromClause() {
		// setup a dummy FromClauseSqlGenerator
		FromClauseSqlGenerator fromClauseSqlGenerator = mock(FromClauseSqlGenerator.class);
		generator.setFromClauseSqlGenerators(Arrays.asList(fromClauseSqlGenerator));
		
		// stub FROM clause of adapter for node 23
		final String FROM_CLAUSE_FOR_NODE_23 = "FROM CLAUSE FOR NODE 23";
		when(fromClauseSqlGenerator.fromClause(node23)).thenReturn(FROM_CLAUSE_FOR_NODE_23);

		// stub FROM clause of adapter for node 42
		final String FROM_CLAUSE_FOR_NODE_42 = "FROM CLAUSE FOR NODE 42";
		when(fromClauseSqlGenerator.fromClause(node42)).thenReturn(FROM_CLAUSE_FOR_NODE_42);
		
		// expected FROM clause
		String expected = 
			"FROM\n" + 
			"\t" + FROM_CLAUSE_FOR_NODE_23 + ",\n" +
			"\t" + FROM_CLAUSE_FOR_NODE_42 + "\n";
		
		// test
		StringBuffer sb = new StringBuffer();
		generator.appendFromClause(sb, nodes);
		assertEquals(expected, sb.toString());
	}
	
	///// WHERE clause
	
	@SuppressWarnings("unchecked")
	@Test
	public void appendWhereClauseEmptyNodes() {
		// setup a dummy WhereClauseSqlGenerator
		WhereClauseSqlGenerator whereClauseSqlGenerator = mock(WhereClauseSqlGenerator.class);
		generator.setWhereClauseSqlGenerators(Arrays.asList(whereClauseSqlGenerator));
		
		// stub empty WHERE clause for both adapters
		final List<String> EMPTY_WHERE_CLAUSE = new ArrayList<String>();
		when(whereClauseSqlGenerator.whereConditions(any(AnnisNode.class), anyList(), anyList())).thenReturn(EMPTY_WHERE_CLAUSE);
		
		// expected WHERE clause
		String expected = "" +
				"\t-- node 23\n" +
				"\t-- node 42\n";
		
		// test
		StringBuffer sb = new StringBuffer();
		generator.appendWhereClause(sb, nodes, null, null);
		assertEquals(expected, sb.toString());
	}
	
	// if at least one node has a WHERE condition, prepend WHERE clause and AND conditions
	@SuppressWarnings("unchecked")
	@Test
	public void appendWhereClauseNotEmptyNodes() {
		// setup a dummy WhereClauseSqlGenerator
		WhereClauseSqlGenerator whereClauseSqlGenerator = mock(WhereClauseSqlGenerator.class);
		generator.setWhereClauseSqlGenerators(Arrays.asList(whereClauseSqlGenerator));
		
		AnnisNode node99 = createNode(99);
		
		// stub WHERE conditions for nodes
		when(whereClauseSqlGenerator.whereConditions(eq(node23), anyList(), anyList())).thenReturn(Arrays.asList("node 23 condition 1", "node 23 condition 2"));
		when(whereClauseSqlGenerator.whereConditions(eq(node42), anyList(), anyList())).thenReturn(new ArrayList<String>());
		when(whereClauseSqlGenerator.whereConditions(eq(node99), anyList(), anyList())).thenReturn(Arrays.asList("node 99 condition 1", "node 99 condition 2"));

		// test call
		StringBuffer sb = new StringBuffer();
		generator.appendWhereClause(sb, nodes, null, null);
		
		String expected = "" +
				"WHERE\n" +						// prepend WHERE
				"\t-- node 23\n" +
				"\tnode 23 condition 1 AND\n" +	// append AND to condition
				"\tnode 23 condition 2 AND\n" +	// more conditions in WHERE clause, append AND
				"\t-- node 42\n" +				// node without conditions
				"\t-- node 99\n" +
				"\tnode 99 condition 1 AND\n" +
				"\tnode 99 condition 2\n";		// last condition, don't append AND
		String actual = sb.toString();
		assertEquals(expected, actual);
	}
	
}
