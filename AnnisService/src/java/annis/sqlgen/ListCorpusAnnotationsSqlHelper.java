package annis.sqlgen;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import annis.model.Annotation;

public class ListCorpusAnnotationsSqlHelper implements ParameterizedRowMapper<Annotation> {

	public String createSqlQuery(long corpusId) {
		String template =
			"SELECT corpus_annotation.* " +
			"FROM corpus_annotation, corpus this, corpus parent " +
			"WHERE this.id = :id " +
			"AND this.pre >= parent.pre AND this.post <= parent.post " +
			"AND corpus_annotation.corpus_ref = parent.id";
		String sql = template.replaceAll(":id", String.valueOf(corpusId));
		return sql;
	}
	
	public Annotation mapRow(ResultSet rs, int rowNum) throws SQLException {
		String namespace = rs.getString("namespace");
		String name = rs.getString("name");
		String value = rs.getString("value");
		return new Annotation(namespace, name, value);
	}

}
