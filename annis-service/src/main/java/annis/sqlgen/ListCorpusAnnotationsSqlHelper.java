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

import annis.model.Annotation;
import static annis.sqlgen.SqlConstraints.sqlString;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class ListCorpusAnnotationsSqlHelper implements
  ParameterizedRowMapper<Annotation>
{

  public String createSqlQuery(String toplevelCorpusName, String corpusName,
    boolean exclude)
  {
    String template;
    String sql;

    if (exclude && !toplevelCorpusName.equals(corpusName))
    {
      template = "SELECT DISTINCT "
        + "doc.type, doc.name as corpus_name, doc.pre AS corpus_pre, ca.name, "
        + "ca.value, ca.namespace"
        + "\nFROM corpus_annotation ca, corpus doc, corpus toplevel "
        + "\nWHERE doc.name = :docname \n"
        + "\nAND toplevel.name = :toplevelname \n"
        + "\nAND toplevel.top_level = true \n"
        + "\nAND doc.pre > toplevel.pre \n"
        + "\nAND doc.post < toplevel.post \n"
        + "\nAND ca.corpus_ref = doc.id \n"
        + "ORDER BY corpus_pre ASC";
      sql = template.replaceAll(":docname", sqlString(corpusName)).
        replaceAll(":toplevelname", sqlString(toplevelCorpusName));
    }
    else
    {
      template = "SELECT DISTINCT parent.type, parent.name AS corpus_name, "
        + "parent.pre AS corpus_pre, ca.name, ca.value, ca.namespace "
        + "\nFROM corpus_annotation ca, corpus parent, corpus this, corpus toplevel "
        + "\nWHERE this.name = :docname \n"
        + "\nAND toplevel.name = :toplevelname \n"
        + "\nAND toplevel.top_level = true \n"
        + "\nAND parent.pre >= toplevel.pre \n"
        + "\nAND parent.post <= toplevel.post \n"
        + "\nAND this.pre >= parent.pre \n"
        + "\nAND this.post <= parent.post \n"
        + "\nAND ca.corpus_ref = parent.id \n"
        + "\nORDER BY corpus_pre ASC";
      sql = template.replaceAll(":docname", sqlString(corpusName)).
        replaceAll(":toplevelname", sqlString(toplevelCorpusName));
    }
    return sql;
  }

  @Override
  public Annotation mapRow(ResultSet rs, int rowNum) throws SQLException
  {

    String namespace = rs.getString("namespace");
    String name = rs.getString("name");
    String value = rs.getString("value");
    String type = rs.getString("type");
    String corpusName = rs.getString("corpus_name");
    int pre = rs.getInt("corpus_pre");
    return new Annotation(namespace, name, value, type, corpusName, pre);
  }
}
