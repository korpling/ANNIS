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
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 * Lists all annotations of all sub documents of a specific corpus. Optionally
 * includes the annotations of the toplevel corpus.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class ListDocumentsAnnotationsSqlHelper implements
  ParameterizedRowMapper<Annotation>
{

  public String createSqlQuery(String toplevelCorpusName, boolean listRootCorpus)
  {
    String template = "SELECT DISTINCT docs.name as corpus_name, docs.path_name as path_name, docs.pre, meta.namespace, meta.name, meta.value, docs.type\n"
      + "from corpus this, corpus docs \n"
      + "FULL JOIN corpus_annotation meta \n"
      + "ON docs.id=meta.corpus_ref \n"
      + "WHERE this.name = :toplevelname \n"
      + "AND docs.pre :> this.pre \n"
      + "AND docs.post :< this.post \n"
      + "AND meta.value is not null";
    String sql = template.replaceAll(":toplevelname", sqlString(
      toplevelCorpusName));

    if (listRootCorpus)
    {
      return sql.replaceAll(":>", ">=").replaceAll(":<", "<=");
    }
    else
    {
      return sql.replaceAll(":>", ">").replaceAll(":<", "<");
    }
  }

  @Override
  public Annotation mapRow(ResultSet rs, int rowNum) throws SQLException
  {

    Integer pre = rs.getInt("pre");
    String corpusName = rs.getString("corpus_name");
    String type = rs.getString("type");
    String namespace = rs.getString("namespace");
    String name = rs.getString("name");
    String value = rs.getString("value");
    Array annotationPathArray = rs.getArray("path_name");
    List<String> annotationPath = new LinkedList<>();
    if(annotationPathArray.getBaseType() == Types.VARCHAR)
    {
      annotationPath = Arrays.asList((String[]) annotationPathArray.getArray());
    }
    return new Annotation(namespace, name, value, type, corpusName, pre,
      annotationPath);
  }
}
