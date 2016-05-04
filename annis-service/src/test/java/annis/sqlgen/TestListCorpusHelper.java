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

import annis.service.objects.AnnisCorpus;
import java.sql.ResultSet;
import java.sql.SQLException;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestListCorpusHelper {

	// object under test
	private ListCorpusSqlHelper listCorpusHelper;
	
	// dummy corpus data
	private static final long ID1 = 1L;
	private static final String NAME1 = "NAME1";
	private static final int TEXT_COUNT1 = 2;
	private static final int TOKEN_COUNT1 = 3;
	
	@Before
	public void setup() {
		listCorpusHelper = new ListCorpusSqlHelper();
	}
	
	@Test
	public void createSqlQuery() {
		String expected = "" +
			"SELECT * FROM corpus_stats";
		assertEquals(expected, listCorpusHelper.createSqlQuery());
	}
	
	@Test
	public void mapRow() throws SQLException {
		// stub a result set to return a single row
		ResultSet resultSet = mock(ResultSet.class);
		when(resultSet.getLong("id")).thenReturn(ID1);
		when(resultSet.getString("name")).thenReturn(NAME1);
		when(resultSet.getInt("text")).thenReturn(TEXT_COUNT1);
		when(resultSet.getInt("tokens")).thenReturn(TOKEN_COUNT1);
		
		// call and test
		AnnisCorpus annisCorpus = listCorpusHelper.mapRow(resultSet, 0);
		assertThat(annisCorpus.getId(), is(ID1));
		assertThat(annisCorpus.getName(), is(NAME1));
		assertThat(annisCorpus.getTextCount(), is(TEXT_COUNT1));
		assertThat(annisCorpus.getTokenCount(), is(TOKEN_COUNT1));
	}
	
}
