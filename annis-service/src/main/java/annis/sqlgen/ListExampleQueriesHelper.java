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
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import annis.examplequeries.ExampleQuery;

/**
 * Generates SQL query for example queries
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class ListExampleQueriesHelper implements
  ParameterizedRowMapper<ExampleQuery>
{


  public String createSQLQuery(List<Long> corpusIDs)
  {
    if (corpusIDs == null || corpusIDs.isEmpty())
    {
      return "SELECT example_query, example_queries.\"type\", used_ops, "
        + "description, c.name as corpus_name "
        + "\nFROM example_queries, corpus c"
        + "\nWHERE corpus_ref = c.id";
    }
    else
    {
      String sql = "SELECT example_query, example_queries.\"type\", used_ops, "
        + "description, c.name as corpus_name  "
        + "\nFROM example_queries, ("
        + "\nSELECT * FROM corpus "
        + "\nWHERE corpus.id in (" + StringUtils.join(corpusIDs, ",") + ")) as c"
        + "\nWHERE	corpus_ref = c.id"
        + "\nORDER BY (nodes, used_ops)";

      return sql;
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
    exampleQuery.setCorpusName(rs.getString("corpus_name"));

    return exampleQuery;
  }
}
