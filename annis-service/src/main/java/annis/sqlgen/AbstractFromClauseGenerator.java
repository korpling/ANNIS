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
import java.util.Map;

public abstract class AbstractFromClauseGenerator 
	extends TableAccessStrategyFactory 
	implements FromClauseSqlGenerator<QueryData>
{

    protected String tableAliasDefinition(QueryNode node, String table, int count)
	  {
      return tableAliasDefinition(tables(node).getTableAliases(), node, table, count);
    }
  
	  public static String tableAliasDefinition(
      Map<String, String> tableAliases, QueryNode node, String table, int count)
	  {
	    StringBuilder sb = new StringBuilder();

	    sb.append(TableAccessStrategy.tableName(tableAliases, table));
	    sb.append(" AS ");
	    sb.append(TableAccessStrategy.aliasedTable(node, tableAliases, table, count));

	    return sb.toString();
	  }
	
}
