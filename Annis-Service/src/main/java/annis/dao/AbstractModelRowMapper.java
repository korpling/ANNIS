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