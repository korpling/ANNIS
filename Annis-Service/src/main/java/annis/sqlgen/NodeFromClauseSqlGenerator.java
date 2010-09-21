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

import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;

import annis.model.AnnisNode;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 * Generates a from clause only containing the node and node_annotation tables.
 * @author thomas
 */
public class NodeFromClauseSqlGenerator extends BaseNodeSqlGenerator
	implements FromClauseSqlGenerator
{

  @Override
  public String fromClause(AnnisNode node)
  {
    List<String> tables = new ArrayList<String>();

		// every node uses the node table
		tables.add(tableAliasDefinition(node, NODE_TABLE, 1));

		// node annotations
		if (tables(node).usesNodeAnnotationTable()) {
			int start = tables(node).isMaterialized(NODE_ANNOTATION_TABLE, NODE_TABLE) ? 2 : 1;
			for (int i = start; i <= node.getNodeAnnotations().size(); ++i) {
				tables.add(tableAliasDefinition(node, NODE_ANNOTATION_TABLE, i));
			}
		}

		return StringUtils.join(tables, ", ");
  }

}
