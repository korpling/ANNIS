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
import static org.mockito.MockitoAnnotations.initMocks;
import static test.TestUtils.uniqueInt;
import static test.TestUtils.uniqueLong;
import static test.TestUtils.uniqueString;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.Bag;
import org.apache.commons.collections.bag.HashBag;
import org.apache.commons.lang.NotImplementedException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Spy;

import annis.querymodel.AnnisNode;
import annis.ql.parser.QueryData;

public class TestAbstractFromClauseGenerator {

	private AbstractFromClauseGenerator generator;
	@Spy private TableAccessStrategy tableAccessStrategy = new TableAccessStrategy();
	
	@Before
	public void setup() {
		initMocks(this);
		generator = new AbstractFromClauseGenerator() {
			
			@Override
			public String fromClause(QueryData queryData, List<AnnisNode> alternative,
					String indent) {
				throw new NotImplementedException("This AbstractFromClauseGenerator is only used for testing purposes");
			}
			
			@Override
			protected TableAccessStrategy createTableAccessStrategy() {
				return tableAccessStrategy;
			}
		};
	}
	
	/**
	 * Create a table alias definition for a table that is only used once.
	 */
	@Test
	public void shouldCreateTableAliasWithoutCount() {
		// given
		long id = uniqueLong();
		AnnisNode node = new AnnisNode(id);
		String table = uniqueString();
		String tableAlias = uniqueString();
		int count = 1;
		setupTableAliases(table, tableAlias, count);
		// when
		String alias = generator.tableAliasDefinition(node, table, count);
		// then
		String expected = tableAlias + " AS " + tableAlias + id;
		assertThat(alias, is(expected));
	}

	/**
	 * Create a table alias definition for a table that is used multiple times.
	 */
	@Test
	public void shouldCreateTableAliasWithCount() {
		// given
		long id = uniqueLong();
		AnnisNode node = new AnnisNode(id);
		String table = uniqueString();
		String tableAlias = uniqueString();
		int count = uniqueInt();
		setupTableAliases(table, tableAlias, count);
		// when
		String alias = generator.tableAliasDefinition(node, table, count);
		// then
		String expected = tableAlias + " AS " + tableAlias + id + "_" + count;
		assertThat(alias, is(expected));
	}

	// set up table access strategy to return the requested table alias
	// simulate that count copies of the table (alias) are required
	private void setupTableAliases(String table, String tableAlias, int count) {
		HashMap<String, String> tableAliases = new HashMap<String, String>();
		tableAliases.put(table, tableAlias);
		tableAccessStrategy.setTableAliases(tableAliases);
		Bag tables = new HashBag();
		tables.add(tableAlias, count);
		given(tableAccessStrategy.computeSourceTables()).willReturn(tables);
	}
	
}
