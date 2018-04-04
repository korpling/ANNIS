/*
 * Copyright 2014 SFB 632.
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
import com.google.common.base.Joiner;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple {@link FromClauseSqlGenerator} that statically returns
 * the name of the view which contains the selected facts table.
 * 
 * In order to work we have to use the facts table. This implementation is not
 * prepared for a dynamic scheme where node, rank etc. are seperate tables.
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class SelectedFactsFromClauseGenerator extends AbstractFromClauseGenerator
{

  @Override
  public String fromClause(QueryData queryData, List<QueryNode> alternative,
    String indent)
  {
    List<String> clauses = new LinkedList<>();
    
    for (QueryNode node : alternative)
    {
      TableAccessStrategy tas = tables(node);
      String aliasName = TableAccessStrategy.aliasedTable(node, tas.getTableAliases(), 
      TableAccessStrategy.FACTS_TABLE, 1);
      
      clauses.add(selectedFactsSQL(queryData.getCorpusList(), indent)  + " AS " + aliasName);
    }

    return Joiner.on(",\n" + indent + AbstractSqlGenerator.TABSTOP).join(clauses);
  }
  
  public static String selectedFactsSQL(List<Long> corpusList, String indent)
  {
    if (corpusList == null || corpusList.isEmpty())
    {
      return "(SELECT * FROM facts LIMIT 0)";
    }
    else if (corpusList.size() == 1)
    {
      return "facts_" + corpusList.get(0);
    }
    else
    {
      return unionAllSelectedFacts(corpusList, indent
        + AbstractSqlGenerator.TABSTOP);
    }
  }
  
  private static String unionAllSelectedFacts(List<Long> corpusList, 
    String indent)
  {
    List<String> tables = new LinkedList<>();
    int idx=0;
    for(Long corpusID : corpusList)
    {
      tables.add("SELECT *, " + idx + "::int AS sourceIdx FROM facts_" + corpusID);
      idx++;
    }
    
    String indent2 = indent + AbstractSqlGenerator.TABSTOP;
    
    return "(\n" + indent2
      + Joiner.on("\n" + indent2 + "UNION ALL\n" + indent2).join(tables)
      + "\n" + indent + ")";
  }
  
}
