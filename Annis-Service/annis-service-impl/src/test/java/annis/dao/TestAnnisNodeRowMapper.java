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
package annis.dao;

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import org.junit.Test;

import annis.model.AnnisNode;


public class TestAnnisNodeRowMapper extends ModelRowMapperTestCase<AnnisNode>{

	// some row data
	private static final long ID = 1;
	private static final long TEXT_REF = 2;
	private static final long CORPUS_REF = 3;
	private static final String NAMESPACE = "NAMESPACE";
	private static final String NAME = "NAME";
	private static final long LEFT = 4;
	private static final long RIGHT = 5;
	private static final long TOKEN_INDEX = 6;
	private static final long LEFT_TOKEN = 7;
	private static final long RIGHT_TOKEN = 8;
	private static final String SPAN = "SPAN";

	// object under test
	@Override
	protected AbstractModelRowMapper<AnnisNode> createModelRowMapper() {
		return new AnnisNodeRowMapper();
	}

	@Test
	public void annisNodeRowMapper() throws SQLException {
		stubDefaultResultSet();
		
		// expected node
		AnnisNode expected = new AnnisNode(ID, CORPUS_REF, TEXT_REF, LEFT, RIGHT, NAMESPACE, NAME, TOKEN_INDEX, SPAN, LEFT_TOKEN, RIGHT_TOKEN);
		
		// call and test		
		AnnisNode actual = rowMapper.mapRow(resultSet, 0);
		assertThat(actual, is(expected));
	}

	@Test
	public void mapRowToken() throws SQLException {
		// stub result set to return NULL column for token_index
		stubDefaultResultSet();
		when(resultSet.wasNull()).thenReturn(true);
		AnnisNode node = rowMapper.mapRow(resultSet, 0);
		assertThat(node.isToken(), is(false));
	}
	
	@Test
	public void mapRowNoToken() throws SQLException {
		// stub result set to return no NULL value for token_index
		stubDefaultResultSet();
		when(resultSet.wasNull()).thenReturn(false);
		AnnisNode node = rowMapper.mapRow(resultSet, 0);
		assertThat(node.isToken(), is(true));
	}
	
	private void stubDefaultResultSet() throws SQLException {
		stubLongColumn("id", ID);
		stubLongColumn("corpus_ref", CORPUS_REF);
		stubLongColumn("text_ref", TEXT_REF);
		stubLongColumn("left", LEFT);
		stubLongColumn("right", RIGHT);
		stubStringColumn("namespace", NAMESPACE);
		stubStringColumn("name", NAME);
		stubLongColumn("token_index", TOKEN_INDEX);
		stubStringColumn("span", SPAN);
		stubLongColumn("left_token", LEFT_TOKEN);
		stubLongColumn("right_token", RIGHT_TOKEN);
	}

	private void stubStringColumn(final String column, final String value) throws SQLException {
		stubStringColumn(NODE_TABLE, column, value);
	}

	private void stubLongColumn(String column, long value) throws SQLException {
		stubLongColumn(NODE_TABLE, column, value);
	}

}
