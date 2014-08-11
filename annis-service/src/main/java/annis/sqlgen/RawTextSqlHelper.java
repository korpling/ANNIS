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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 * Extractor for raw text from the database.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class RawTextSqlHelper implements ResultSetExtractor<List<String>>
{

  public String createSQL(long topLevelCorpusId)
  {
    StringBuilder sb = new StringBuilder();

    sb.append("SELECT text.corpus_ref, text.text, ids.name, path_name\n");
    sb.append("FROM (\n");
    sb.append("\tSELECT children.*\n");
    sb.append("\tFROM corpus as parent, corpus children\n");
    sb.append("\tWHERE\n");
    sb.append("\t\tparent.id = ").append(topLevelCorpusId).append("\n");
    sb.append("\tAND\tparent.pre < children.pre\n");
    sb.append("\tAND\tparent.post > children.post\n");
    sb.append(") AS ids, text\n");
    sb.append("WHERE\n");
    sb.append("text.corpus_ref = ids.id");

    return sb.toString();
  }

  public String createSQL(long topLevelCorpusId, String docName)
  {
    StringBuilder sb = new StringBuilder();

    sb.append("SELECT * FROM\n");
    sb.append("(\n");
    sb.append("\tSELECT doc.id\n");
    sb.append("\tFROM corpus as top, corpus as doc\n");
    sb.append("\tWHERE\n");
    sb.append("\t\ttop.id = ").append(topLevelCorpusId).append("\n");
    sb.append("\t\tAND	top.type = 'CORPUS'\n");
    sb.append("\t\tAND	top.top_level = TRUE\n");
    sb.append("\t\tAND	top.pre < doc.pre\n");
    sb.append("\t\tAND	top.post > doc.post\n");
    sb.append("\t\tAND  doc.name = '").append(docName).append("'\n");
    sb.append(") as docs, text\n");
    sb.append("WHERE\n");
    sb.append("\t\tdocs.id = text.corpus_ref");

    return sb.toString();
  }

  @Override
  public List<String> extractData(ResultSet rs) throws SQLException, DataAccessException
  {
    List<String> resultBuffer = new ArrayList<>();
    while (rs.next())
    {
      resultBuffer.add(rs.getString("text"));
    }
    
    return resultBuffer;
  }
}
