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

import annis.model.Join;
import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import static annis.sqlgen.SqlConstraints.join;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import annis.sqlgen.model.Identical;
import annis.sqlgen.model.RankTableJoin;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Adds the constraint that all nodes are always inside one document as WHERE
 * clause elements.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class SubcorpusConstraintWhereClause
  extends TableAccessStrategyFactory
  implements WhereClauseSqlGenerator<QueryData>
{

  @Override
  public Set<String> whereConditions(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    Set<String> conditions = new HashSet<>();
    List<Long> corpusList = queryData.getCorpusList();
    List<Long> documents = queryData.getDocuments();

    conditions.addAll(commonWhereConditions(alternative, corpusList, documents));

    return conditions;
  }

  // VR: inline
  @Deprecated
  public List<String> commonWhereConditions(List<QueryNode> nodes,
    List<Long> corpusList, List<Long> documents)
  {
    LinkedList<String> conditions = new LinkedList<>();

    // annotations can always only be inside a subcorpus/document
    QueryNode[] copyNodes = nodes.toArray(new QueryNode[nodes.size()]);

    for (int left = 0; left < copyNodes.length; left++)
    {
      for (int right = 0; right < copyNodes.length; right++)
      {
        if(left != right)
        {
          // only add constraint if the two nodes are not already connected by their component or node id
          boolean needsCorpusRef = false;
          for(Join j : copyNodes[left].getOutgoingJoins())
          {
            if(j.getTarget() != null 
              && j.getTarget().getId() == copyNodes[right].getId()
              )
            {
              if((j instanceof RankTableJoin || j instanceof Identical))
              {
                // we definitly don't have to apply this join
                needsCorpusRef = false;
                break;
              }
              else
              {
                // there is at least one actual join between this nodes, assume we
                // need a corpus_ref join for now
                needsCorpusRef = true;
              }
            }
          }

          if (needsCorpusRef)
          {
            conditions.add(join("=",
              tables(copyNodes[left]).aliasedColumn(NODE_TABLE, "corpus_ref"),
              tables(copyNodes[right]).aliasedColumn(NODE_TABLE, "corpus_ref")));
          }
        } // end if left != right
      } // end right loop
    } // end left loop

    return conditions;
  }

}
