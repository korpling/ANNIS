package annis.sqlgen;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import annis.service.ifaces.AnnisCorpus;

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
			"SELECT id, name, text, tokens " +
			"FROM corpus_stats";
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
