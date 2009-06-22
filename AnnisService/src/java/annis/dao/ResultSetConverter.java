/**
 * 
 */
package annis.dao;

import java.sql.ResultSet;

@Deprecated
public interface ResultSetConverter<T> {
	
	public T convertResultSet(ResultSet resultSet);
	
}