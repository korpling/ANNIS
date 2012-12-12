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
import static annis.sqlgen.SqlConstraints.sqlString;

public class ListDocumentsAnnotationsSqlHelper implements
  ParameterizedRowMapper<Annotation>
{
  public String createSqlQuery(String toplevelCorpusName)
  {
    String template = "SELECT DISTINCT meta.name, meta.namespace \n" +
        "from corpus this, corpus docs \n" +
        "FULL JOIN corpus_annotation meta \n" +
        "ON docs.pre=meta.corpus_ref \n" +
        "WHERE this.name = :toplevelname \n" +
        "AND docs.pre > this.pre \n" +
        "AND docs.post < this.post \n" +
        "AND meta.namespace is not null";
    String sql = template.replaceAll(":toplevelname", sqlString(toplevelCorpusName));
    return sql;
  }

  @Override
  public Annotation mapRow(ResultSet rs, int rowNum) throws SQLException
  {

    String namespace = rs.getString("namespace");
    String name = new String();
    name = rs.getString("name");
    return new Annotation(namespace, name);
  }
}
