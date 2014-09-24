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

import annis.dao.SelectedCorporaSessionModifier;
import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import com.google.common.base.Joiner;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple {@link FromClauseSqlGenerator} that statically returns
 * the name of the view which contains the selected facts table.
 * 
 * In order to work this needs the {@link SelectedCorporaSessionModifier} to
 * be enabled and we have to use the facts table. This implementation is not
 * prepared for a dynamic scheme where node, rank etc. are seperate tables.
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class SelectedFactsFromClauseGenerator implements FromClauseSqlGenerator<QueryData>
{

  @Override
  public String fromClause(QueryData queryData, List<QueryNode> alternative,
    String indent)
  {
    List<String> clauses = new LinkedList<>();
    
    for (QueryNode node : alternative)
    {
      clauses.add("selected_facts AS facts" + String.valueOf(node.getId()));
    }

    return Joiner.on(",\n" + indent + AbstractSqlGenerator.TABSTOP).join(clauses);
  }
  
}
