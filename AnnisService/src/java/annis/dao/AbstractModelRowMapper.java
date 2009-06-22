package annis.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import annis.sqlgen.TableAccessStrategy;

public abstract class AbstractModelRowMapper<T> implements ParameterizedRowMapper<T> {

	private TableAccessStrategy tableAccessStrategy;

	protected long longValue(ResultSet resultSet, String table, String column) throws SQLException {
		return resultSet.getLong(tableAccessStrategy.columnName(table, column));
	}

	protected String stringValue(ResultSet resultSet, String table, String column) throws SQLException {
		return resultSet.getString(tableAccessStrategy.columnName(table, column));
	}

	///// Getter / Setter
	
	public TableAccessStrategy getTableAccessStrategy() {
		return tableAccessStrategy;
	}

	public void setTableAccessStrategy(TableAccessStrategy tableAccessStrategy) {
		this.tableAccessStrategy = tableAccessStrategy;
	}

}