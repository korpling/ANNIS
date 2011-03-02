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

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.COMPONENT_TABLE;
import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.FACTS_TABLE;

import annis.model.AnnisNode;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author thomas
 */
public class SampleWhereClause extends BaseNodeSqlGenerator
  implements WhereClauseSqlGenerator
{

  @Override
  public List<String> whereConditions(AnnisNode node, List<Long> corpusList, List<Long> documents)
  {
    LinkedList<String> conditions = new LinkedList<String>();

    TableAccessStrategy t = tables(node);

    // only apply if we have a full facts table
    if(t.isMaterialized(NODE_TABLE, FACTS_TABLE)
      && t.isMaterialized(NODE_ANNOTATION_TABLE, FACTS_TABLE)
      && t.isMaterialized(EDGE_ANNOTATION_TABLE, FACTS_TABLE)
      && t.isMaterialized(RANK_TABLE, FACTS_TABLE)
      && t.isMaterialized(COMPONENT_TABLE, FACTS_TABLE))
    {

      if(!t.usesRankTable() && !t.usesComponentTable() && !t.usesNodeAnnotationTable() && !t.usesEdgeAnnotationTable())
      {
        conditions.add(join("=", t.aliasedColumn(FACTS_TABLE, "sample_n"), "true"));
      }
      else if(!t.usesNodeAnnotationTable() && !t.usesEdgeAnnotationTable())
      {
        conditions.add(join("=", t.aliasedColumn(FACTS_TABLE, "sample_n_r_c"), "true"));
      }
      else if(!t.usesRankTable() && !t.usesComponentTable() && !t.usesEdgeAnnotationTable())
      {
        conditions.add(join("=", t.aliasedColumn(FACTS_TABLE, "sample_n_na"), "true"));
      }
      else if(!t.usesNodeAnnotationTable())
      {
        conditions.add(join("=", t.aliasedColumn(FACTS_TABLE, "sample_n_r_c_ea"), "true"));
      }
      else if(!t.usesEdgeAnnotationTable())
      {
        conditions.add(join("=", t.aliasedColumn(FACTS_TABLE, "sample_n_r_c_na"), "true"));
      }
    }
    return conditions;
  }

  @Override
  public List<String> commonWhereConditions(List<AnnisNode> nodes, List<Long> corpusList, List<Long> documents)
  {
    return null;
  }

}
