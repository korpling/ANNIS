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

import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.FACTS_TABLE;

import annis.model.AnnisNode;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thomas
 */
public class ExistsWhereClauseSqlGenerator extends DefaultWhereClauseSqlGenerator
{

  @Override
  public List<String> whereConditions(AnnisNode node, List<Long> corpusList, List<Long> documents)
  {
    List<String> conditions = new ArrayList<String>();

    addNodeConditions(node, conditions, node.getNodeAnnotations());
    addAnnotationConditions(node, conditions, node.getNodeAnnotations(),
      NODE_ANNOTATION_TABLE, "node_annotation_");

    addNodeJoinConditions(node, conditions);

    // TODO: edge joins and annotations
    
    return conditions;
  }
}
