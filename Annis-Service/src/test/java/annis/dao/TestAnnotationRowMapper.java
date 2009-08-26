package annis.dao;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import org.junit.Test;

import annis.model.Annotation;


public class TestAnnotationRowMapper extends ModelRowMapperTestCase<Annotation> {

	// table name
	private static final String TABLE = "TABLE";
	
	// some row data
	private static final String NAMESPACE = "NAMESPACE";
	private static final String NAME = "NAME";
	private static final String VALUE = "VALUE";

	// object under test
	@Override
	protected AbstractModelRowMapper<Annotation> createModelRowMapper() {
		return new AnnotationRowMapper(TABLE);
	}

	@Test
	public void annotationRowMapper() throws SQLException {
		stubStringColumn("namespace", NAMESPACE);
		stubStringColumn("name", NAME);
		stubStringColumn("value", VALUE);
		
		// expected annotation
		Annotation expected = new Annotation(NAMESPACE, NAME, VALUE);
		
		// call and test		
		assertThat(rowMapper.mapRow(resultSet, 0), is(expected));
	}
	
	@Test
	public void nullAnnotation() throws SQLException {
		when(resultSet.wasNull()).thenReturn(true);
		assertThat(rowMapper.mapRow(resultSet, 0), is(nullValue()));
	}

	private void stubStringColumn(final String column, final String value) throws SQLException {
		stubStringColumn(TABLE, column, value);
	}

}
