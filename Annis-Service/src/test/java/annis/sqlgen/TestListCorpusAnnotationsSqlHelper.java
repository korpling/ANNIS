package annis.sqlgen;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import annis.model.Annotation;

public class TestListCorpusAnnotationsSqlHelper {

	private ListCorpusAnnotationsSqlHelper listCorpusAnnotationsHelper;
	
	@Before
	public void setup() {
		listCorpusAnnotationsHelper = new ListCorpusAnnotationsSqlHelper();
	}
	
	@Test
	public void createSqlQuery() {
		final long ID = 42L;
		final String expected =
			"SELECT corpus_annotation.* " +
			"FROM corpus_annotation, corpus this, corpus parent " +
			"WHERE this.id = " + String.valueOf(ID) + " " +
			"AND this.pre >= parent.pre AND this.post <= parent.post " +
			"AND corpus_annotation.corpus_ref = parent.id";
		assertThat(listCorpusAnnotationsHelper.createSqlQuery(ID), is(expected));
	}
	
	@Test
	public void mapRow() throws SQLException {
		// stub a row in the ResultSet
		ResultSet resultSet = mock(ResultSet.class);
		final String NAMESPACE = "NAMESPACE";
		final String NAME = "NAME";
		final String VALUE = "VALUE";
		when(resultSet.getString("namespace")).thenReturn(NAMESPACE);
		when(resultSet.getString("name")).thenReturn(NAME);
		when(resultSet.getString("value")).thenReturn(VALUE);
		
		// expected annotation
		Annotation expected = new Annotation(NAMESPACE, NAME, VALUE);
		
		// call and test
		assertThat(listCorpusAnnotationsHelper.mapRow(resultSet, 1), is(expected));
	}
	
}
