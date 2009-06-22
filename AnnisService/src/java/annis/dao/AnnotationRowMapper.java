/**
 * 
 */
package annis.dao;


import java.sql.ResultSet;
import java.sql.SQLException;

import annis.model.Annotation;

public class AnnotationRowMapper extends AbstractModelRowMapper<Annotation> {
	
	// table name (what kind of annotation)
	private String table;
	
	public AnnotationRowMapper(String table) {
		this.table = table;
	}

	public Annotation mapRow(ResultSet resultSet, int rowNum) throws SQLException {
		// NOT NULL constraint on NAME => NULL indicates no annotation (of this type)
		String name = stringValue(resultSet, "name");
		if (resultSet.wasNull())
			return null;
		
		String namespace = stringValue(resultSet, "namespace");
		String value = stringValue(resultSet, "value");
		
		return new Annotation(namespace, name, value);
	}
	
	private String stringValue(ResultSet resultSet, String column) throws SQLException {
		return stringValue(resultSet, table, column);
	}
	
}