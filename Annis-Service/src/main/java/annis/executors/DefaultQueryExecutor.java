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
import java.util.logging.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author thomas
 */
public class DefaultQueryExecutor implements QueryExecutor
{

  private SqlGenerator sqlGenerator;
  private String matchedNodesViewName;
  private String factsContextViewName;

  private void appendAtt(StringBuilder sb, int i, String att)
  {
      sb.append("context");
      sb.append(i);
      sb.append(".");
      sb.append(att);
      sb.append(" AS ");
      sb.append(att);
      sb.append(i);
  }

  /**
   *
   * @param jdbcTemplate
   * @param corpusList
   * @param queryData
   */
  @Override
  public void createMatchView(JdbcTemplate jdbcTemplate, List<Long> corpusList, QueryData queryData)
  {
    // sql for matches
    StringBuilder sb = new StringBuilder();
    sb.append("CREATE TEMPORARY VIEW \"");
    sb.append(matchedNodesViewName);
    sb.append("\" AS\n");
    
    // select clause
    sb.append("\t SELECT \n");
    for(int i=1; i <= queryData.getMaxWidth(); i++)
    {
      if(i > 1)
      {
        sb.append(", ");
      }
      // id
      appendAtt(sb, i, "id");
      sb.append(", ");

      // text_ref
      appendAtt(sb, i, "text_ref");
      sb.append(", ");

      // left_token
      appendAtt(sb, i, "left_token");
      sb.append(", ");

      // right_token
      appendAtt(sb, i, "right_token");

    }

    // from clause
    sb.append("\n\tFROM\n");
    for(int i=1; i <= queryData.getMaxWidth(); i++)
    {
      if(i > 1)
      {
        sb.append(", ");
      }
      sb.append(factsContextViewName);
      sb.append(" AS context");
      sb.append(i);
      sb.append("\n");
    }

    // where exists
    sb.append("WHERE EXISTS(\n");
    sb.append("SELECT * FROM (\n");
    // the real query
    sb.append(sqlGenerator.toSql(queryData, corpusList));
    
    sb.append("\n) AS query\n");
    // semi-join condition
    sb.append("WHERE ");
    for(int i=1; i <= queryData.getMaxWidth(); i++)
    {
      if(i > 1)
      {
        sb.append(" AND ");
      }
      sb.append("query.id");
      sb.append(i);
      sb.append(" = context");
      sb.append(i);
      sb.append(".id");
    }
    // end of EXISTS
    sb.append("\n)\n");

    jdbcTemplate.execute(sb.toString());

  }

  @Override
  public boolean checkIfApplicable(QueryData aql)
  {
    return true;
  }

  @Override
  public EnumSet<AQLConstraints> getNeededConstraints()
  {
    return EnumSet.noneOf(AQLConstraints.class);
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

  public String getFactsContextViewName()
  {
    return factsContextViewName;
  }

  public void setFactsContextViewName(String factsContextViewName)
  {
    this.factsContextViewName = factsContextViewName;
  }

  

}
