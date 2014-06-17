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
package annis.sqlgen;

import java.util.List;

import annis.model.QueryNode;
import annis.ql.parser.QueryData;

public abstract class AbstractSolutionMatchInFromClauseSqlGenerator extends
    AbstractSqlGenerator implements FromClauseSqlGenerator<QueryData>
{

  private SqlGenerator<QueryData> findSqlGenerator;

  @Override
  public String fromClause(QueryData queryData, List<QueryNode> alternative,
      String indent)
  {
    StringBuffer sb = new StringBuffer();
    
    sb.append(indent).append("(\n");
    
    sb.append(indent).append(TABSTOP);
    sb.append(findSqlGenerator.toSql(queryData, indent + TABSTOP));
    sb.append(indent).append(TABSTOP).append(") AS solutions");
    
    return sb.toString();
  }

  public SqlGenerator<QueryData> getFindSqlGenerator() {
    return findSqlGenerator;
  }

  public void setFindSqlGenerator(SqlGenerator<QueryData> findSqlGenerator) {
    this.findSqlGenerator = findSqlGenerator;
  }

}
