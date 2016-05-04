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
package annis.dao;

import static annis.sqlgen.SqlConstraints.sqlString;
import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.Validate;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;

public class ListCorpusByNameDaoHelper extends ParameterizedSingleColumnRowMapper<Long>
{

  public String createSql(List<String> corpusNames)
  {
    Validate.notEmpty(corpusNames, "Need at least one corpus name");

    // turn corpus names into sql strings (enclosed with ')
    List<String> corpusNamesSqlStrings = new ArrayList<>();
    for (String corpus : corpusNames)
    {
      corpusNamesSqlStrings.add(sqlString(corpus));
    }

    // build sql query
    StringBuilder sb = new StringBuilder();
    
    List<String> singeCorpusSelect = new LinkedList<>();
    int idx=0;
    for(String c : corpusNamesSqlStrings)
    {
      singeCorpusSelect.add("SELECT id, " 
        + idx + "::int AS sourceIdx FROM corpus WHERE name=" + c 
        + " AND top_level IS TRUE");
      idx++;
    }
    
    sb.append("SELECT tmp.id FROM\n");
    sb.append("(\n");
    Joiner.on("\nUNION\n").appendTo(sb, singeCorpusSelect);
    sb.append(") AS tmp\n");
    sb.append("ORDER BY tmp.sourceIdx");
    
    return sb.toString();
  }
}
