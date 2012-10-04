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

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;


public class CountExtractor
{

  private String matchedNodesViewName;

  public String explain(JdbcTemplate jdbcTemplate, boolean analyze)
  {
   
    ParameterizedSingleColumnRowMapper<String> planRowMapper = 
      new ParameterizedSingleColumnRowMapper<String>();
    
    List<String> plan = jdbcTemplate.query((analyze ? "EXPLAIN ANALYZE " : "EXPLAIN ")
      + "\n" + getCountQuery(jdbcTemplate), planRowMapper);
    return StringUtils.join(plan, "\n"); 
  }

  public int queryCount(JdbcTemplate jdbcTemplate)
  {
    return jdbcTemplate.queryForInt(getCountQuery(jdbcTemplate));
  }

  private String getCountQuery(JdbcTemplate jdbcTemplate)
  {
    StringBuilder sql = new StringBuilder();

    sql.append("SELECT count(*) FROM ");
    sql.append(matchedNodesViewName);
    sql.append(" AS solutions");

    return sql.toString();
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
