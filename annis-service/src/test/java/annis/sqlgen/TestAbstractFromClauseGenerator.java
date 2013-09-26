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

import annis.model.QueryAnnotation;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.initMocks;
import static annis.test.TestUtils.uniqueInt;
import static annis.test.TestUtils.uniqueLong;
import static annis.test.TestUtils.uniqueString;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.Bag;
import org.apache.commons.collections.bag.HashBag;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Spy;

import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import java.util.Map;

public class TestAbstractFromClauseGenerator {

	private AbstractFromClauseGenerator generator;
	@Spy private TableAccessStrategy tableAccessStrategy = new TableAccessStrategy();
	
	@Before
	public void setup() {
		initMocks(this);
		generator = new AbstractFromClauseGenerator() {
			
			@Override
			public String fromClause(QueryData queryData, List<QueryNode> alternative,
					String indent) {
				throw new UnsupportedOperationException("This AbstractFromClauseGenerator is only used for testing purposes");
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
		QueryNode node = new QueryNode(id);
		String table = uniqueString();
		String tableAlias = uniqueString();
		int count = 1;
		Map<String, String> tableAliases = setupTableAliases(node, table, tableAlias, count);
		// when
		String alias = AbstractFromClauseGenerator.tableAliasDefinition(tableAliases, node, table, count);
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
		QueryNode node = new QueryNode(id);
		String table = uniqueString();
		String tableAlias = uniqueString();
		int count = uniqueInt(2, 100);
		Map<String, String> tableAliases = setupTableAliases(node, table, tableAlias, count);
		// when
		String alias = AbstractFromClauseGenerator.tableAliasDefinition(tableAliases, node, table, count);
		// then
		String expected = tableAlias + " AS " + tableAlias + id + "_" + count;
		assertThat(alias, is(expected));
	}

	// set up table access strategy to return the requested table alias
	// simulate that count copies of the table (alias) are required
	private Map<String, String> setupTableAliases(QueryNode node, String table, String tableAlias, int count) 
  {
		HashMap<String, String> tableAliases = new HashMap<String, String>();
		tableAliases.put(table, tableAlias);
    if(count > 0)
    {
      tableAliases.put(TableAccessStrategy.NODE_ANNOTATION_TABLE, tableAlias);
    } 
    for(int i=0; i < count; i++)
    {
      // this will add new entries for the source tables
      node.addNodeAnnotation(new QueryAnnotation("dummy", "dummy" + i));
    }
    
		tableAccessStrategy.setTableAliases(tableAliases);
    tableAccessStrategy.setNode(node);
		Bag tables = new HashBag();
		tables.add(tableAlias, count);
		given(tableAccessStrategy.computeSourceTables())
      .willReturn(tables);
    
    return tableAliases;
    
	}
	
}
