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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.MockitoAnnotations.initMocks;
import static test.TestUtils.newSet;
import static test.TestUtils.uniqueString;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.dao.DataAccessException;

import annis.model.AnnisNode;
import annis.ql.parser.QueryData;

public class TestAbstractUnionSqlGenerator {

	@InjectMocks private AbstractUnionSqlGenerator<?> generator;
	@Mock private SelectClauseSqlGenerator<QueryData> selectClauseSqlGenerator;
	@Mock private FromClauseSqlGenerator<QueryData> fromClauseSqlGenerator;
	@Spy private List<FromClauseSqlGenerator<QueryData>> fromClauseSqlGenerators = new ArrayList<FromClauseSqlGenerator<QueryData>>();
	@Mock private WhereClauseSqlGenerator<QueryData> whereClauseSqlGenerator;
	@Spy private List<WhereClauseSqlGenerator<QueryData>> whereClauseSqlGenerators = new ArrayList<WhereClauseSqlGenerator<QueryData>>();
	@Mock private GroupByClauseSqlGenerator<QueryData> groupByClauseSqlGenerator;
	@Mock private OrderByClauseSqlGenerator<QueryData> orderByClauseSqlGenerator;
	@Mock private LimitOffsetClauseSqlGenerator<QueryData> limitOffsetClauseSqlGenerator;
	
	// test data
	private QueryData queryData = new QueryData();
	private AnnisNode annisNode = new AnnisNode();
	private List<AnnisNode> alternative = new ArrayList<AnnisNode>();
	private List<List<AnnisNode>> alternatives = new ArrayList<List<AnnisNode>>();
	
	@Before
	public void setup() {
		// wire up dependencies
		generator = new AbstractUnionSqlGenerator<Object>() {

			@Override
			public Object extractData(ResultSet rs) throws SQLException,
					DataAccessException {
				throw new NotImplementedException("This SqlGenerator is only used for test purposes.");
			}
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
		given(orderByClauseSqlGenerator.orderByClause(eq(queryData), anyListOf(AnnisNode.class), anyString()))
			.willReturn(orderBy);
		given(limitOffsetClauseSqlGenerator.limitOffsetClause(eq(queryData), anyListOf(AnnisNode.class), anyString()))
			.willReturn(limitOffset);
	}
	
}
