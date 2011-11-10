/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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

import annis.model.AnnisNode;
import annis.ql.parser.QueryData;
import java.util.List;
import org.springframework.util.Assert;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public abstract class QueryDataSqlGenerator<T> extends BaseSqlGenerator<QueryData, T>
{
  @Override
  public String toSql(QueryData queryData)
  {
    return toSql(queryData, 0);
  }

  @Override
  public String toSql(QueryData queryData, int indentBy)
  {
    Assert.notEmpty(queryData.getAlternatives(), "BUG: no alternatives");

    // push alternative down
    List<AnnisNode> alternative = queryData.getAlternatives().get(0);

    String indent = computeIndent(indentBy);
    StringBuffer sb = new StringBuffer();
    indent(sb, indent);
    sb.append(createSqlForAlternative(queryData, alternative, indent));
    appendOrderByClause(sb, queryData, alternative, indent);
    appendLimitOffsetClause(sb, queryData, alternative, indent);
    return sb.toString();
  }
}
