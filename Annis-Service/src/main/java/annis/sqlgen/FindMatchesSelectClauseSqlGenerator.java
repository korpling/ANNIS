package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import annis.model.AnnisNode;

public class FindMatchesSelectClauseSqlGenerator
	extends BaseNodeSqlGenerator
	implements SelectClauseSqlGenerator {

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
