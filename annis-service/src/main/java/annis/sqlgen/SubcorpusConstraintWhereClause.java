/*
 * Copyright 2009-2012 Collaborative Research Centre SFB 632 
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

import static annis.sqlgen.SqlConstraints.join;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import annis.model.QueryNode;
import annis.ql.parser.QueryData;

/**
 * Adds the constraint that all nodes are always inside one document as
 * WHERE clause elements.
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class SubcorpusConstraintWhereClause 
  extends TableAccessStrategyFactory
  implements WhereClauseSqlGenerator<QueryData>
{

  @Override
  public Set<String> whereConditions(QueryData queryData, List<QueryNode> alternative, String indent)
  {
	  Set<String> conditions = new HashSet<String>();
	  List<Long> corpusList = queryData.getCorpusList();
	  List<Long> documents = queryData.getDocuments();
	  
	  conditions.addAll(commonWhereConditions(alternative, corpusList, documents));
	  
	  return conditions;
  }

	
	// VR: inline
	@Deprecated
  public List<String> commonWhereConditions(List<QueryNode> nodes, List<Long> corpusList, List<Long> documents)
  {
    LinkedList<String> conditions = new LinkedList<String>();

    // annotations can always only be inside a subcorpus/document
    QueryNode[] copyNodes = nodes.toArray(new QueryNode[0]);

    for (int left = 0; left < copyNodes.length; left++)
    {
      for (int right = left + 1; right < copyNodes.length; right++)
      {
        conditions.add(join("=",
          tables(copyNodes[left]).aliasedColumn(NODE_TABLE, "corpus_ref"),
          tables(copyNodes[right]).aliasedColumn(NODE_TABLE, "corpus_ref")));
      }
    }

    return conditions;
  }

}
