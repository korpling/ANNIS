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
package annis.sqlgen;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import annis.model.Annotation;

public class ListCorpusAnnotationsSqlHelper implements
		ParameterizedRowMapper<Annotation>
{

	public String createSqlQuery(long corpusId)
	{
		String template = "SELECT  parent.type, ca.name as key, ca.value, ca.namespace "
				+ "FROM corpus_annotation ca, corpus this, corpus parent "
				+ "WHERE this.id = :id "
				+ "AND this.pre >= parent.pre "
				+ "AND this.post <= parent.post "
				+ "AND ca.corpus_ref = parent.id " + "ORDER BY parent.pre DESC";
		String sql = template.replaceAll(":id", String.valueOf(corpusId));
		return sql;
	}

	public Annotation mapRow(ResultSet rs, int rowNum) throws SQLException
	{

		String namespace = rs.getString("type");
		String name = (rs.getString("namespace") == null) ? rs.getString("key")
				: rs.getString("namespace") + ":" + rs.getString("key");
		String value = rs.getString("value");
		return new Annotation(namespace, name, value);
	}
}
