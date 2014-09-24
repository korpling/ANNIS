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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple {@link FromClauseSqlGenerator} that statically returns
 * the name of the view which contains the selected facts table.
 * 
 * In order to work this needs the {@link SelectedCorporaWithClauseGenerator} to
 * be enabled and we have to use the facts table. This implementation is not
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
    
    boolean optimize = (queryData.getCorpusList().size() == 1);
    boolean empty = queryData.getCorpusList().isEmpty();
    
    for (QueryNode node : alternative)
    {
      TableAccessStrategy tas = tables(node);
      String aliasName = TableAccessStrategy.aliasedTable(node, tas.getTableAliases(), TableAccessStrategy.FACTS_TABLE, 1);
      if(empty)
      {
        clauses.add("(SELECT * FROM facts LIMIT 0)" + " AS " + aliasName);
      }
      else if(optimize)
      {
        clauses.add("facts_" + queryData.getCorpusList().get(0) + " AS " + aliasName);
      }
      else
      {
        clauses.add(innerQuery(queryData, alternative, indent + AbstractSqlGenerator.TABSTOP) 
          + " AS " + aliasName);
      }
    }

    return Joiner.on(",\n" + indent + AbstractSqlGenerator.TABSTOP).join(clauses);
  }
  
  private String innerQuery(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    List<String> tables = new LinkedList<>();
    for(Long corpusID : queryData.getCorpusList())
    {
      tables.add("SELECT * FROM facts_" + corpusID);
    }
    
    String indent2 = indent + AbstractSqlGenerator.TABSTOP;
    
    return "(\n" + indent2
      + Joiner.on("\n" + indent2 + "UNION ALL\n" + indent2).join(tables)
      + "\n" + indent + ")";
  }
  
}
