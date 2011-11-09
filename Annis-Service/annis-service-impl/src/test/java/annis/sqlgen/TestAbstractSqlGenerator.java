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
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;
import static test.TestUtils.uniqueString;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.dao.DataAccessException;

import annis.model.AnnisNode;
import annis.ql.parser.QueryData;


public class TestAbstractSqlGenerator {

	// class under test and dependencies
	@InjectMocks private AbstractSqlGenerator<?> generator;
	@Mock private SelectClauseSqlGenerator selectClauseSqlGenerator;
	@Mock private FromClauseSqlGenerator fromClauseSqlGenerator;
	@Spy private List<FromClauseSqlGenerator> fromClauseSqlGenerators = new ArrayList<FromClauseSqlGenerator>();
	@Mock private WhereClauseSqlGenerator whereClauseSqlGenerator;
	@Spy private List<WhereClauseSqlGenerator> whereClauseSqlGenerators = new ArrayList<WhereClauseSqlGenerator>();
	
	// test data
	private QueryData queryData = new QueryData();
	private AnnisNode annisNode = new AnnisNode();
	private List<AnnisNode> alternative = new ArrayList<AnnisNode>();
	private List<List<AnnisNode>> alternatives = new ArrayList<List<AnnisNode>>();
	
	@Before
	public void setup() {
		// wire up dependencies
		generator = new AbstractSqlGenerator<Object>() {

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

		// wire up query data: 1 alternative with 1 node
		alternative.add(annisNode);
		alternatives.add(alternative);
		queryData.setAlternatives(alternatives);
	}
	
	/**
	 * An AQL query must contain at least one alternative to be transformed
	 * to a SQL statement. 
	 */
	@Test(expected=IllegalArgumentException.class)
	public void errorIfZeroAlternativesInQueryData() {
		// given
		alternatives.clear();
		// when
		generator.toSql(queryData);
	}

	/**
	 * A minimal SQL statement consists of a SELECT clause and a FROM clause.
	 */
	@Test
	public void shouldAppendSelectAndFromClauseForMinimalQuery() {
		// given
		String selectClause = uniqueString();
		String fromClause = uniqueString();
		setupSelectAndFromClause(selectClause, fromClause);
		// when
		String sql = generator.toSql(queryData);
		// then
		String expected = createMinimalSqlStatement(selectClause, fromClause);
		assertThat(sql, is(expected));
	}
	
	/**
	 * FROM clauses from multiple FromClauseSqlGenrators are separated by ",".
	 */
	@Test
	public void shouldJoinMultipleFromClausesWithComma() {
		// given
		String selectClause = uniqueString();
		String fromClause = uniqueString();
		setupSelectAndFromClause(selectClause, fromClause);
		String fromClause2 = uniqueString();
		FromClauseSqlGenerator fromClauseSqlGenerator2 = mock(FromClauseSqlGenerator.class);
		given(fromClauseSqlGenerator2.fromClause(eq(queryData), eq(alternative), anyString()))
			.willReturn(fromClause2);
		fromClauseSqlGenerators.add(fromClauseSqlGenerator2);
		// when
		String sql = generator.toSql(queryData);
		// then
		String expected = createMinimalSqlStatement(selectClause, fromClause);
		expected = expected.substring(0, expected.length() - 1); // strip last newline
		expected += 
				",\n" + 
			    "  " + fromClause2 + "\n";
		assertThat(sql, is(expected));
	}
	
	/**
	 * WHERE conditions from one WhereClauseSqlGenerator are ANDed.
	 */
	@Test
	public void shouldAndWhereConditionsFromOneWhereClauseSqlGenerator() {
		// given
		String selectClause = uniqueString();
		String fromClause = uniqueString();
		setupSelectAndFromClause(selectClause, fromClause);
		String whereCondition1 = "a" + uniqueString();
		String whereCondition2 = "b" + uniqueString();
		given(whereClauseSqlGenerator.whereConditions(eq(queryData), eq(alternative), anyString()))
			.willReturn(newSet(whereCondition1, whereCondition2));
		// when
		String sql = generator.toSql(queryData);
		// then
		String expected = createMinimalSqlStatement(selectClause, fromClause);
		expected += 
				"WHERE\n" + 
			    "  " + whereCondition1 + " AND\n" +
			    "  " + whereCondition2 + "\n";
		assertThat(sql, is(expected));
	}
	
	/**
	 * WHERE conditions from many WhereClauseSqlGenerator are ANDed.
	 */
	@Test
	public void shouldAndWhereConditionsFromMultipeWhereClauseSqlGenerators() {
		// given
		String selectClause = uniqueString();
		String fromClause = uniqueString();
		setupSelectAndFromClause(selectClause, fromClause);
		String whereCondition1 = "a" + uniqueString();
		String whereCondition2 = "b" + uniqueString();
		given(whereClauseSqlGenerator.whereConditions(eq(queryData), eq(alternative), anyString()))
			.willReturn(newSet(whereCondition1));
		WhereClauseSqlGenerator whereClauseSqlGenerator2 = mock(WhereClauseSqlGenerator.class);
		given(whereClauseSqlGenerator2.whereConditions(eq(queryData), eq(alternative), anyString()))
			.willReturn(newSet(whereCondition2));
		whereClauseSqlGenerators.add(whereClauseSqlGenerator2);
		// when
		String sql = generator.toSql(queryData);
		// then
		String expected = createMinimalSqlStatement(selectClause, fromClause);
		expected += 
				"WHERE\n" + 
			    "  " + whereCondition1 + " AND\n" +
			    "  " + whereCondition2 + "\n";
		assertThat(sql, is(expected));
	}
	
	/**
	 * Do not add a WHERE clause if there are no WHERE conditions.
	 */
	@Test
	public void shouldNotAppendWhereConditionIfEmpty() {
		// given
		String selectClause = uniqueString();
		String fromClause = uniqueString();
		setupSelectAndFromClause(selectClause, fromClause);
		given(whereClauseSqlGenerator.whereConditions(eq(queryData), eq(alternative), anyString()))
			.willReturn(emptySetOf(String.class));
		// when
		String sql = generator.toSql(queryData);
		// then
		String expected = createMinimalSqlStatement(selectClause, fromClause);
		assertThat(sql, is(expected));
	}
	
	/**
	 * Append GROUP BY clause.
	 */
	@Test
	public void shouldAppendGroupByClause() {
		// given
		String selectClause = uniqueString();
		String fromClause = uniqueString();
		setupSelectAndFromClause(selectClause, fromClause);
		String groupBy = uniqueString();
		GroupByClauseSqlGenerator groupByClauseSqlGenerator = mock(GroupByClauseSqlGenerator.class);
		generator.setGroupByClauseSqlGenerator(groupByClauseSqlGenerator);
		given(groupByClauseSqlGenerator.groupByAttributes(eq(queryData), eq(alternative)))
			.willReturn(groupBy);
		// when
		String sql = generator.toSql(queryData);
		// then
		String expected = createMinimalSqlStatement(selectClause, fromClause);
		expected += 
				"GROUP BY " + groupBy + "\n";
		assertThat(sql, is(expected));
	}

	/**
	 * Append ORDER BY clause.
	 */
	@Test
	public void shouldAppendOrderByClause() {
		// given
		String selectClause = uniqueString();
		String fromClause = uniqueString();
		setupSelectAndFromClause(selectClause, fromClause);
		String orderBy = uniqueString();
		OrderByClauseSqlGenerator orderByClauseSqlGenerator = mock(OrderByClauseSqlGenerator.class);
		generator.setOrderByClauseSqlGenerator(orderByClauseSqlGenerator);
		given(orderByClauseSqlGenerator.orderByClause(eq(queryData), eq(alternative), anyString()))
			.willReturn(orderBy);
		// when
		String sql = generator.toSql(queryData);
		// then
		String expected = createMinimalSqlStatement(selectClause, fromClause);
		expected += 
				"ORDER BY " + orderBy + "\n";
		assertThat(sql, is(expected));
	}

	/**
	 * Append LIMIT/OFFSET clause.
	 */
	@Test
	public void shouldAppendLimitAndOffsetClause() {
		// given
		String selectClause = uniqueString();
		String fromClause = uniqueString();
		setupSelectAndFromClause(selectClause, fromClause);
		String limitOffset = uniqueString();
		LimitOffsetClauseSqlGenerator limitOffsetClauseSqlGenerator = mock(LimitOffsetClauseSqlGenerator.class);
		generator.setLimitOffsetClauseSqlGenerator(limitOffsetClauseSqlGenerator);
		given(limitOffsetClauseSqlGenerator.limitOffsetClause(eq(queryData), eq(alternative), anyString()))
			.willReturn(limitOffset);
		// when
		String sql = generator.toSql(queryData);
		// then
		String expected = createMinimalSqlStatement(selectClause, fromClause);
		expected += 
				limitOffset + "\n";
		assertThat(sql, is(expected));
	}

	// create a set from a list of items
	private <T> Set<T> newSet(T... items) {
		Set<T> set = new HashSet<T>();
		for (T item : items) {
			set.add(item);
		}
		return set;
	}
	
	
	// create an empty set
	private <T> Set<T> emptySetOf(Class<T> clazz) {
		Set<T> set = new HashSet<T>();
		return set;
	}

	// stub selectClauseSqlGenerator and fromClauseSqlGenerator for a minimal SQL statement
	private void setupSelectAndFromClause(String selectClause, String fromClause) {
		given(selectClauseSqlGenerator.selectClause(eq(queryData), anyListOf(AnnisNode.class), anyString()))
			.willReturn(selectClause);
		given(fromClauseSqlGenerator.fromClause(eq(queryData), eq(alternative), anyString()))
			.willReturn(fromClause);
	}
	
	// create minimal SQL statement from a SELECT clause content and FROM clause content
	private String createMinimalSqlStatement(String selectClause,
			String fromClause) {
		String expected = 
				"SELECT " + selectClause + "\n" +
				"FROM" + "\n" + 
				"  " + fromClause + "\n";
		return expected;
	}

}
