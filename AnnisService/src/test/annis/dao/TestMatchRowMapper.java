package annis.dao;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class TestMatchRowMapper {

	// object under test
	private MatchRowMapper matchRowMapper;

	// mocked result set and metadata
	@Mock private ResultSet resultSet;
	@Mock private ResultSetMetaData resultSetMetaData;

	@Before
	public void setup() throws SQLException {
		initMocks(this);
		matchRowMapper = new MatchRowMapper();
		when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
	}
	
	private void setColumns(int columns) throws SQLException {
		when(resultSetMetaData.getColumnCount()).thenReturn(columns);
	}
	
	@Test
	public void mapRowOneSpanPerMatch() throws SQLException {
		setColumns(4);
		
		// contents of the result set
		final int ID = 1;
		final int TEXT_REF = 2;
		final int LEFT_TOKEN = 3;
		final int RIGHT_TOKEN = 4;
		// FIXME: benante Spaltennamen wären besser
		when(resultSet.getInt(1)).thenReturn(ID);
		when(resultSet.getInt(2)).thenReturn(TEXT_REF);
		when(resultSet.getInt(3)).thenReturn(LEFT_TOKEN);
		when(resultSet.getInt(4)).thenReturn(RIGHT_TOKEN);
//		when(resultSet.getInt("id")).thenReturn(ID);
//		when(resultSet.getInt("text_ref")).thenReturn(TEXT_REF);
//		when(resultSet.getInt("left_token")).thenReturn(LEFT_TOKEN);
//		when(resultSet.getInt("right_token")).thenReturn(RIGHT_TOKEN);
		
		// test
		Match expected = new Match();
		expected.add(new Span(ID, TEXT_REF, LEFT_TOKEN, RIGHT_TOKEN));

		Match match = matchRowMapper.mapRow(resultSet, 0);
		assertThat(match, is(expected));
	}
	
	@Test
	public void mapRowManySpansPerMatch() throws SQLException {
		setColumns(8);
		
		// contents of the result set
		final int ID1 = 1;
		final int TEXT_REF1 = 2;
		final int LEFT_TOKEN1 = 3;
		final int RIGHT_TOKEN1 = 4;
		final int ID2 = 5;
		final int TEXT_REF2 = 6;
		final int LEFT_TOKEN2 = 7;
		final int RIGHT_TOKEN2 = 8;

		// FIXME: benante Spaltennamen wären besser
		when(resultSet.getInt(1)).thenReturn(ID1);
		when(resultSet.getInt(2)).thenReturn(TEXT_REF1);
		when(resultSet.getInt(3)).thenReturn(LEFT_TOKEN1);
		when(resultSet.getInt(4)).thenReturn(RIGHT_TOKEN1);
		when(resultSet.getInt(5)).thenReturn(ID2);
		when(resultSet.getInt(6)).thenReturn(TEXT_REF2);
		when(resultSet.getInt(7)).thenReturn(LEFT_TOKEN2);
		when(resultSet.getInt(8)).thenReturn(RIGHT_TOKEN2);
		
		// test
		Match expected = new Match();
		expected.add(new Span(ID1, TEXT_REF1, LEFT_TOKEN1, RIGHT_TOKEN1));
		expected.add(new Span(ID2, TEXT_REF2, LEFT_TOKEN2, RIGHT_TOKEN2));

		Match match = matchRowMapper.mapRow(resultSet, 0);
		assertThat(match, is(expected));
	}
}
