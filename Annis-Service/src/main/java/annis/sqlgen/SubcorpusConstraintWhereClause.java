/*
 *  Copyright 2010 thomas.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package annis.sqlgen;

import annis.model.AnnisNode;
import annis.model.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.springframework.util.Assert;

/**
 *
 * @author thomas
 */
public class SubcorpusConstraintWhereClause extends BaseNodeSqlGenerator
  implements WhereClauseSqlGenerator
{

  @Override
  public List<String> whereConditions(AnnisNode node, List<Long> corpusList, List<Annotation> metaData)
  {
    return null;
  }

  @Override
  public List<String> commonWhereConditions(List<AnnisNode> nodes, List<Long> corpusList, List<Annotation> metaData)
  {
    LinkedList<String> conditions = new LinkedList<String>();
    
    conditions.add("-- annotations can always only be inside a subcorpus/document");

    AnnisNode[] copyNodes = nodes.toArray(new AnnisNode[0]);

    for(int left=0; left < copyNodes.length; left++)
    {
      for(int right=left+1; right < copyNodes.length; right++)
      {
        conditions.add(join("=",
          tables(copyNodes[left]).aliasedColumn("node", "corpus_ref"),
          tables(copyNodes[right]).aliasedColumn("node", "corpus_ref"))
        );
      }
    }

    return conditions;
  }
}
