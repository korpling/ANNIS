/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
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

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import annis.model.AnnisNode;

// FindSqlGenerator hat seinen eigenen SelectClauseGenerator
@Deprecated
public class FindMatchesSelectClauseSqlGenerator
	extends BaseNodeSqlGenerator {

	public String selectClause(List<AnnisNode> nodes, int maxWidth) {
		Validate.isTrue(nodes.size() <= maxWidth, "BUG: nodes.size() > maxWidth");
		
		List<String> nodeColumns = new ArrayList<String>();
		
		// columns for nodes
    boolean isDistinct = false;
		for (int i = 0; i < nodes.size(); ++i) 
    {
      AnnisNode n = nodes.get(i);
      TableAccessStrategy t = tables(n);
			nodeColumns.add(selectClauseForNode(n, i + 1));
      if(t.usesComponentTable() || t.usesEdgeAnnotationTable() || t.usesRankTable())
      {
        isDistinct = true;
      }
		}
		
		// pad select clause with NULL values, so all queries in UNION have same cardinality
		for (int i = nodes.size(); i < maxWidth; ++i)
			nodeColumns.add(selectClauseForNode(null, i + 1));

    if(isDistinct)
    {
      return "DISTINCT\n" + StringUtils.join(nodeColumns, ",\n");
    }
    else
    {
      return StringUtils.join(nodeColumns, ",\n");
    }
	}
	
	private String selectClauseForNode(AnnisNode node, int index) {
		String[] columns = getColumns();
		for (int i = 0; i < columns.length; ++i) {
			columns[i] = (node == null ? "NULL" : tables(node).aliasedColumn(NODE_TABLE, columns[i])) + " AS " + columns[i] + index;
		}
		return "\t" + StringUtils.join(columns, ", ");
	}

  public String[] getColumns()
  {
    return new String[] { "id", "text_ref", "left_token", "right_token" };
  }

}
