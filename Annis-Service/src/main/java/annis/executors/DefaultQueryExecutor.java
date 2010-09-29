/*
 *  Copyright 2010 thomas.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package annis.executors;

import annis.ql.parser.QueryData;
import annis.sqlgen.SqlGenerator;
import java.util.EnumSet;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author thomas
 */
public class DefaultQueryExecutor implements QueryExecutor
{

  private SqlGenerator sqlGenerator;
  private String matchedNodesViewName;	
  
  /**
   *
   * @param jdbcTemplate
   * @param corpusList
   * @param queryData
   */
  @Override
  public void createMatchView(JdbcTemplate jdbcTemplate, List<Long> corpusList, 
    List<Long> documents, QueryData queryData)
  {
    // sql for matches
    StringBuilder matchSb = new StringBuilder();
    matchSb.append("CREATE TEMPORARY VIEW \"");
    matchSb.append(matchedNodesViewName);
    matchSb.append("\" AS\n");
    matchSb.append("\t SELECT DISTINCT *\n");
    matchSb.append("\n\tFROM\n(\n");
    matchSb.append(sqlGenerator.toSql(queryData, corpusList, documents));
    matchSb.append("\n) as matched_ids");

    jdbcTemplate.execute(matchSb.toString());

  }

  @Override
  public boolean checkIfApplicable(QueryData aql)
  {
    return true;
  }

  // getter/setter

  public SqlGenerator getSqlGenerator()
  {
    return sqlGenerator;
  }

  public void setSqlGenerator(SqlGenerator sqlGenerator)
  {
    this.sqlGenerator = sqlGenerator;
  }

  public String getMatchedNodesViewName()
  {
    return matchedNodesViewName;
  }

  public void setMatchedNodesViewName(String matchedNodesViewName)
  {
    this.matchedNodesViewName = matchedNodesViewName;
  }

}
