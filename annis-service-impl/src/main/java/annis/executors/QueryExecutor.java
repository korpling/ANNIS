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
package annis.executors;

import annis.ql.parser.QueryData;
import java.util.EnumSet;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Interface for a SQLGenerator that transforms an AQL to a SQL query on the
 * relANNIS scheme.
 * @author Thomas Krause
 */
public interface QueryExecutor {

  /**
   * Check if this generator is able to handle a specific AQL query.
   * @param aql The parsed AQL query.
   * @return
   */
  public boolean checkIfApplicable(QueryData aql);

  /**
   * Create a SQL query on the relANNIS scheme from an AQL query.
   * 
   * @param jdbcTemplate
   * @param corpusList
   * @param dddQuery
   * @param offset
   * @param limit
   * @param left
   * @param right
   *
   */
  public void createMatchView(JdbcTemplate jdbcTemplate, List<Long> corpusList, 
    List<Long> documents, QueryData queryData);

}