package annis.dao;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Before;
import org.mockito.Mock;

import annis.sqlgen.TableAccessStrategy;

public abstract class ModelRowMapperTestCase<T> {

	// object under test
	protected AbstractModelRowMapper<T> rowMapper;
	
	// dependencies
	@Mock protected ResultSet resultSet;
	@Mock private TableAccessStrategy tableAccessStrategy;
	private static final String PREFIX = "PREFIX_";

	@Before
	public void setup() {
		initMocks(this);
		rowMapper = createModelRowMapper();
		rowMapper.setTableAccessStrategy(tableAccessStrategy);
	}

	protected abstract AbstractModelRowMapper<T> createModelRowMapper();

	protected void stubStringColumn(String table, final String column, final String value)
			throws SQLException {
				when(tableAccessStrategy.columnName(table, column)).thenReturn(PREFIX + column);
				when(resultSet.getString(PREFIX + column)).thenReturn(value);
			}

	protected void stubLongColumn(String table, final String column, final Long value)
			throws SQLException {
				when(tableAccessStrategy.columnName(table, column)).thenReturn(PREFIX + column);
				when(resultSet.getLong(PREFIX + column)).thenReturn(value);
			}

}