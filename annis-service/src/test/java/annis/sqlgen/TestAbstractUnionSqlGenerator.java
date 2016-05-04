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

import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import static annis.test.TestUtils.newSet;
import static annis.test.TestUtils.uniqueString;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.MockitoAnnotations.initMocks;
import org.mockito.Spy;

public class TestAbstractUnionSqlGenerator {

	@InjectMocks private AbstractUnionSqlGenerator generator;
	@Mock private SelectClauseSqlGenerator<QueryData> selectClauseSqlGenerator;
	@Mock private FromClauseSqlGenerator<QueryData> fromClauseSqlGenerator;
	@Spy private List<FromClauseSqlGenerator<QueryData>> fromClauseSqlGenerators = new ArrayList<>();
	@Mock private WhereClauseSqlGenerator<QueryData> whereClauseSqlGenerator;
	@Spy private List<WhereClauseSqlGenerator<QueryData>> whereClauseSqlGenerators = new ArrayList<>();
	@Mock private GroupByClauseSqlGenerator<QueryData> groupByClauseSqlGenerator;
	@Mock private OrderByClauseSqlGenerator<QueryData> orderByClauseSqlGenerator;
	@Mock private LimitOffsetClauseSqlGenerator<QueryData> limitOffsetClauseSqlGenerator;
	
	// test data
	private QueryData queryData = new QueryData();
	private QueryNode annisNode = new QueryNode();
	private List<QueryNode> alternative = new ArrayList<>();
	private List<List<QueryNode>> alternatives = new ArrayList<>();
	
	@Before
	public void setup() {
		// wire up dependencies
		generator = new AbstractUnionSqlGenerator() {
		};
		initMocks(this);
		fromClauseSqlGenerators.add(fromClauseSqlGenerator);
		whereClauseSqlGenerators.add(whereClauseSqlGenerator);
		generator.setSelectClauseSqlGenerator(selectClauseSqlGenerator);
		generator.setFromClauseSqlGenerators(fromClauseSqlGenerators);
		generator.setWhereClauseSqlGenerators(whereClauseSqlGenerators);
		generator.setGroupByClauseSqlGenerator(groupByClauseSqlGenerator);
		generator.setOrderByClauseSqlGenerator(orderByClauseSqlGenerator);
		generator.setLimitOffsetClauseSqlGenerator(limitOffsetClauseSqlGenerator);

		// wire up query data: 1 alternative with 1 node
		alternative.add(annisNode);
		alternatives.add(alternative);
		queryData.setAlternatives(alternatives);
	}
	
	/**
	 * Create SQL statement for one alternative.
	 */
	@Test
	public void shouldCreateStatementForOneAlternative() {
		// given
		String selectClause = uniqueString();
		String fromClause = uniqueString();
		String whereCondition = uniqueString();
		String orderBy = uniqueString();
		String groupBy = uniqueString();
		String limitOffset = uniqueString();
		setupClauseSqlGenerators(selectClause, fromClause, whereCondition,
				orderBy, groupBy, limitOffset);
		// when
		String sql = generator.toSql(queryData);
		// then
		String expected = 
				"SELECT " + selectClause + "\n" +
			    "FROM" + "\n" + 
			    "  " + fromClause + "\n" +
			    "WHERE" + "\n" + 
			    "  " + whereCondition + "\n" +
			    "GROUP BY " + groupBy + "\n" + 
			    "ORDER BY " + orderBy + "\n" +
			    limitOffset + "\n";
		assertThat(sql, is(expected));
	}

	/**
	 * Create SQL statement for multiple alternatives: 
	 * ORDER BY and LIMIT/OFFSET clauses only appear once.
	 */
	@Test
	public void shouldCreateStatementForMultipleAlternatives() {
		// given
		alternatives.add(alternative);
		String selectClause = uniqueString();
		String fromClause = uniqueString();
		String whereCondition = uniqueString();
		String orderBy = uniqueString();
		String groupBy = uniqueString();
		String limitOffset = uniqueString();
		setupClauseSqlGenerators(selectClause, fromClause, whereCondition,
				orderBy, groupBy, limitOffset);
		// when
		String sql = generator.toSql(queryData);
		// then
		String expected = 
				"SELECT " + selectClause + "\n" +
			    "FROM" + "\n" + 
			    "  " + fromClause + "\n" +
			    "WHERE" + "\n" + 
			    "  " + whereCondition + "\n" +
			    "GROUP BY " + groupBy + "\n" +
			    "\n" + 
				"UNION SELECT " + selectClause + "\n" +
			    "FROM" + "\n" + 
			    "  " + fromClause + "\n" +
			    "WHERE" + "\n" + 
			    "  " + whereCondition + "\n" +
			    "GROUP BY " + groupBy + "\n" +
			    "ORDER BY " + orderBy + "\n" +
			    limitOffset + "\n";
		assertThat(sql, is(expected));
	}
	
	// setup return values of individual SQL clause generators
	private void setupClauseSqlGenerators(String selectClause,
			String fromClause, String whereCondition, String orderBy,
			String groupBy, String limitOffset) {
		given(selectClauseSqlGenerator.selectClause(eq(queryData), eq(alternative), anyString()))
			.willReturn(selectClause);
		given(fromClauseSqlGenerator.fromClause(eq(queryData), eq(alternative), anyString()))
			.willReturn(fromClause);
		given(whereClauseSqlGenerator.whereConditions(eq(queryData), eq(alternative), anyString()))
			.willReturn(newSet(whereCondition));
		given(groupByClauseSqlGenerator.groupByAttributes(eq(queryData), eq(alternative)))
		.willReturn(groupBy);
		given(orderByClauseSqlGenerator.orderByClause(eq(queryData), anyListOf(QueryNode.class), anyString()))
			.willReturn(orderBy);
		given(limitOffsetClauseSqlGenerator.limitOffsetClause(eq(queryData), anyListOf(QueryNode.class), anyString()))
			.willReturn(limitOffset);
	}
	
}
