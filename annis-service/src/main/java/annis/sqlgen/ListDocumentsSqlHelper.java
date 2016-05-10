/*
 * Copyright 2013 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.sqlgen;

import annis.model.Annotation;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class ListDocumentsSqlHelper implements ParameterizedRowMapper<Annotation>
{

  public String createSql(String topLevelCorpusName)
  {
    StringBuilder sb = new StringBuilder();
    sb.append(
      "SELECT docs.name as name, docs.pre as pre, docs.path_name as path_name FROM corpus this, corpus docs\n");
    sb.append("WHERE\n")
      .append("this.name = :toplevel\n")
      .append("AND this.top_level IS TRUE\n")
      .append("AND this.pre < docs.pre\n")
      .append("AND this.post > docs.post\n")
      .append("AND docs.type = 'DOCUMENT'\n");

    return sb.toString().replace(":toplevel", SqlConstraints.sqlString(topLevelCorpusName));
  }

  @Override
  public Annotation mapRow(ResultSet rs, int rowNum) throws SQLException
  {
    Annotation annotation = new Annotation();
    annotation.setName(rs.getString("name"));
    annotation.setPre(rs.getInt("pre"));
    Array annotationPathArray = rs.getArray("path_name");
    List<String> annotationPath = new LinkedList<>();
    if(annotationPathArray.getBaseType() == Types.VARCHAR)
    {
      annotationPath = Arrays.asList((String[]) annotationPathArray.getArray());
    }
    annotation.setAnnotationPath(annotationPath);
    return annotation;
  }
}
