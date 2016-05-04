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

import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * Abstract base class for a SQL statement which coalesces multiple alternatives
 * using UNION.
 *
 * Only the SELECT, FROM, WHERE and GROUP BY clauses are used for each
 * alternative. The ORDER BY and LIMIT/OFFSET clauses are applied to the entire
 * query.
 *
 * It is the responsibility of the calling code to correctly set
 * {@link QueryData.maxWidth} and the responsibility of the
 * {@link SelectClauseSqlGenrator} to pad the SELECT clause if necessary.
 *
 * @author Viktor Rosenfeld <rosenfel@informatik.hu-berlin.de>
 *
 * @param <T> Type into which the JDBC result set is transformed.
 */
public abstract class AbstractUnionSqlGenerator extends AbstractSqlGenerator
{

  // corpusList, documents
  @Override
  public String toSql(QueryData queryData, String indent)
  {
    Assert.notEmpty(queryData.getAlternatives(), "BUG: no alternatives");

    StringBuffer sb = new StringBuffer();

    List<String> alternatives = new ArrayList<>();
    for (List<QueryNode> alternative : queryData.getAlternatives())
    {
      alternatives.add(createSqlForAlternative(queryData, alternative, indent));
    }
    sb.append(StringUtils.join(alternatives, "\n" + indent + "UNION "));

    // ORDER BY and LIMIT/OFFSET clauses cannot depend on alternative?
    appendOrderByClause(sb, queryData, null, indent);
    appendLimitOffsetClause(sb, queryData, null, indent);

    return sb.toString();
  }
}
