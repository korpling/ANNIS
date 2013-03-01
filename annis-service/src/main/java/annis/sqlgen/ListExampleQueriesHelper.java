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
import annis.examplequeries.QueryType;
import java.sql.ResultSet;
import java.sql.SQLException;
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

  public String createSQLQuery(String corpusName)
  {
    if (corpusName == null)
    {
      return "SELECT * from example_queries";
    }
    else
    {
      return "SELECT * FROM (SELECT id FROM CORPUS WHERE name = \'" + corpusName + "\') AS corpus, example_queries WHERE corpus.id = corpus_ref";
    }
  }

  @Override
  public ExampleQuery mapRow(ResultSet rs, int i) throws SQLException
  {
    ExampleQuery exampleQuery = new ExampleQuery();

    exampleQuery.setType(QueryType.valueOf(rs.getString("type")));
    exampleQuery.setUsedOperators("used_operators");
    exampleQuery.setExampleQuery(rs.getString("example_query"));
    exampleQuery.setDescription(rs.getString("description"));

    return exampleQuery;
  }
}
