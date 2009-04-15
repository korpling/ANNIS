/**
 * 
 */
package de.deutschdiachrondigital.dddquery.helper;

import java.sql.ResultSet;

public interface ResultSetConverter<T> {
	
	public T convertResultSet(ResultSet resultSet);
	
}