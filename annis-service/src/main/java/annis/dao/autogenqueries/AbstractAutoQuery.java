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
package annis.dao.autogenqueries;

import annis.examplequeries.ExampleQuery;
import annis.sqlgen.extensions.AnnotateQueryData;
import annis.sqlgen.extensions.LimitOffsetQueryData;

/**
 * Implements the initializing of the {@link ExampleQuery} class, which might be
 * convenient for several auto generated query objects, and set some defaults.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
abstract public class AbstractAutoQuery implements QueriesGenerator.QueryBuilder
{

  /**
   * Retrieve the amount of nodes, which are contained by the {@link #getAQL()}
   * query.
   *
   * @return An integer, which represents the amount of nodes.
   */
  abstract public int getNodes();


  abstract public String getFinalAQLQuery();

  abstract public String getDescription();

  /**
   * TODO define types
   */
  public String getType()
  {
    return null;
  }

  @Override
  public LimitOffsetQueryData getLimitOffsetQueryData()
  {
    return new LimitOffsetQueryData(5, 5);
  }

  @Override
  public AnnotateQueryData getAnnotateQueryData()
  {
    return new AnnotateQueryData(5, 5, null);
  }

  /**
   * TODO restricted operators for type safeness
   */
  public String getOperators()
  {
    return null;
  }

  @Override
  public ExampleQuery getExampleQuery()
  {
    ExampleQuery exampleQuery = new ExampleQuery();
    exampleQuery.setNodes(getNodes());
    exampleQuery.setType(getType());
    exampleQuery.setDescription(getDescription());
    exampleQuery.setUsedOperators(getOperators());
    exampleQuery.setExampleQuery(getFinalAQLQuery());

    return exampleQuery;
  }
}
