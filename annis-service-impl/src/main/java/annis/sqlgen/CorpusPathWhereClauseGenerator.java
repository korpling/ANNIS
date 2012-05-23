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

import static annis.sqlgen.AbstractSqlGenerator.TABSTOP;
import static annis.sqlgen.SqlConstraints.join;

import annis.model.QueryNode;
import annis.ql.parser.QueryData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import java.util.Set;
import org.apache.commons.lang.StringUtils;

/**
 *
 * 
 * 
 * @author benjamin
 */
public class CorpusPathWhereClauseGenerator extends AbstractFromClauseGenerator
  implements WhereClauseSqlGenerator<QueryData>
{

  @Override
  public String fromClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    List<String> tables = new ArrayList<String>();


    for (int i = 0; i < alternative.size(); i++)
    {
      QueryNode n = alternative.get(i);
      tables.add("Corpus" + " AS Corpus" + (i + 1));
    }
    return StringUtils.join(tables, ", " + indent + TABSTOP);
  }

  @Override
  public Set<String> whereConditions(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    Set<String> conditions = new HashSet<String>();
    int i = 0;

    for (QueryNode n : alternative)
    {
      i++;
      String factTable = tables(n).aliasedTable("facts", i) + ".corpus_ref";
      String corpusTable = tables(n).aliasedTable("Corpus", i) + ".id";

      conditions.add(join("=", factTable, corpusTable));
    }

    return conditions;
  }
}
