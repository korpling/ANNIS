/*
 * Copyright 2012 SFB 632.
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

import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import annis.sqlgen.extensions.LimitOffsetQueryData;
import java.util.List;

/**
 *
 * @author benjamin
 */
public class CommonLimitOffsetGenerator implements
  LimitOffsetClauseSqlGenerator<QueryData>
{

  @Override
  public String limitOffsetClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    LimitOffsetQueryData LimitOffsetQueryData = getLimitOffsetQueryData(
      queryData);
    StringBuilder sb = new StringBuilder();
    Integer limit = null;
    Integer offset = null;

    if (LimitOffsetQueryData != null)
    {
      limit = LimitOffsetQueryData.getLimit();
      offset = LimitOffsetQueryData.getOffset();
    }

    if (limit != null && limit >= 0)
    {
      sb.append(indent).append("LIMIT ").append(limit).append("\n");
    }

    if (offset != null && offset >= 0)
    {
      sb.append(indent).append("OFFSET ").append(offset);
    }

    return sb.toString();
  }

  private LimitOffsetQueryData getLimitOffsetQueryData(QueryData queryData)
  {
    for (Object o : queryData.getExtensions())
    {
      if (o instanceof LimitOffsetQueryData)
      {
        return (LimitOffsetQueryData) o;
      }
    }
    return null;
  }
}
