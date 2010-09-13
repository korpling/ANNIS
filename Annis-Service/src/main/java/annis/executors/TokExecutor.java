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
import java.util.EnumSet;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * QueryExecutor that is only able to handle the "tok" query.
 * @author thomas
 */
public class TokExecutor implements QueryExecutor
{

  private String matchedNodesViewName;
  private String filteredNodeViewName;

  @Override
  public boolean checkIfApplicable(QueryData aql)
  {
    if(aql.getAlternatives().size() == 1
      && aql.getAlternatives().get(0).size() == 1
      && aql.getAlternatives().get(0).get(0).isToken())
    {
      return true;
    }

    return false;
  }

  @Override
  public EnumSet<AQLConstraints> getNeededConstraints()
  {
    return EnumSet.noneOf(AQLConstraints.class);
  }

  @Override
  public void createMatchView(JdbcTemplate jdbcTemplate, List<Long> corpusList,
    List<Long> documents, QueryData queryData)
  {

    StringBuilder sql = new StringBuilder();

    sql.append("CREATE TEMPORARY VIEW ");
    sql.append(matchedNodesViewName);
    sql.append(" AS \n");
    sql.append("SELECT id AS id1, text_ref AS text_ref1, left_token AS left_token1, right_token AS right_token1");
    sql.append(" FROM ");
    sql.append(filteredNodeViewName);
    sql.append(" WHERE token_index IS NOT NULL AND\n");
    sql.append(" toplevel_corpus IN (");
    sql.append(StringUtils.join(corpusList, ","));
    sql.append(") AND corpus_ref IN (");
    sql.append(StringUtils.join(documents, ","));
    sql.append(")");


    jdbcTemplate.update(sql.toString());
  }

  public String getFilteredNodeViewName()
  {
    return filteredNodeViewName;
  }

  public void setFilteredNodeViewName(String filteredNodeViewName)
  {
    this.filteredNodeViewName = filteredNodeViewName;
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
