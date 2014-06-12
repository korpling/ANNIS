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
import static annis.sqlgen.AbstractSqlGenerator.TABSTOP;
import static annis.sqlgen.TableAccessStrategy.COMPONENT_TABLE;
import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class AnnotationCategoryJoinFromClause extends AbstractFromClauseGenerator
{

  @Override
  public String fromClause(QueryData queryData, List<QueryNode> alternative,
    String indent)
  {
    List<String> tables = new ArrayList<>();
    for (QueryNode node : alternative)
    {
      tables.addAll(fromClauseForNode(queryData.getCorpusList(), node));
    }
    return StringUtils.join(tables, ",\n" + indent + TABSTOP);
  }
  
  public List<String> fromClauseForNode(List<Long> corpusList,  QueryNode node) 
  {
		List<String> tables = new ArrayList<>();

    TableAccessStrategy tas = tables(node);
    Preconditions.checkArgument(tas.isMaterialized(RANK_TABLE, NODE_TABLE), 
      "rank table must be materialized in a facts table");
    Preconditions.checkArgument(tas.isMaterialized(COMPONENT_TABLE, RANK_TABLE), 
      "component table must be materialized in a facts table");
    Preconditions.checkArgument(tas.isMaterialized(NODE_ANNOTATION_TABLE, NODE_TABLE), 
      "node_annotation table must be materialized in a facts table");
    Preconditions.checkArgument(tas.isMaterialized(EDGE_ANNOTATION_TABLE, RANK_TABLE), 
      "edge_annotation table must be materialized in a facts table");

    String factsAliasDef = tableAliasDefinition(tas, node, NODE_TABLE, 1, corpusList);
    String catAliasDef = tableAliasDefinition(tas, node, "annotation_category", 1, corpusList);
    String factsAlias = TableAccessStrategy.aliasedTable(node, tas.getTableAliases(), NODE_TABLE,
      1);
    String catAlias = TableAccessStrategy.aliasedTable(node, tas.getTableAliases(), "annotation_category",
      1);
    
    // TODO: use alias for column
    // join the facts table with the annotation_category
		tables.add(
      factsAliasDef
    + " LEFT JOIN " 
    + catAliasDef
    + " ON (" + factsAlias + ".toplevel_corpus" +  " =  " + catAlias +".toplevel_corpus AND "
      + factsAlias + ".node_anno_category = " + catAlias + ".id)");


    return tables;
	}

}
