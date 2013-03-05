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

import annis.examplequeries.ExampleQuery;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 * Generates SQL query for example queries
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class ListExampleQueriesHelper implements
  ParameterizedRowMapper<ExampleQuery>
{

  private static final Logger log = LoggerFactory.
    getLogger(ListExampleQueriesHelper.class);

  public String createSQLQuery(String[] corpusNames)
  {
    if (corpusNames == null || corpusNames.length == 0)
    {
      return "SELECT * from example_queries";
    }
    else
    {
      String replaceName = ":corpusName";
      String select = "\nSELECT * FROM (SELECT id FROM CORPUS WHERE name = \'"
        + replaceName + "\') AS corpus, example_queries WHERE corpus.id = corpus_ref";

      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < corpusNames.length; i++)
      {
        sb.append(select.replace(replaceName, corpusNames[i]));
        if (i < corpusNames.length - 1)
        {
          sb.append("\nUNION");
        }
      }
      sb.append("\nORDER BY corpus_ref");
      return sb.toString();
    }
  }

  @Override
  public ExampleQuery mapRow(ResultSet rs, int i) throws SQLException
  {
    ExampleQuery exampleQuery = new ExampleQuery();

    exampleQuery.setType(rs.getString("type"));
    exampleQuery.setUsedOperators(rs.getString("used_ops"));
    exampleQuery.setExampleQuery(rs.getString("example_query"));
    exampleQuery.setDescription(rs.getString("description"));

    return exampleQuery;
  }
}
