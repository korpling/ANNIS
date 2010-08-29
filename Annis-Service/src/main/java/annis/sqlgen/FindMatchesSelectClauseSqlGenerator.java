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
		for (int i = 0; i < nodes.size(); ++i) {
			nodeColumns.add(selectClauseForNode(nodes.get(i), i + 1));
		}
		
		// pad select clause with NULL values, so all queries in UNION have same cardinality
		for (int i = nodes.size(); i < maxWidth; ++i)
			nodeColumns.add(selectClauseForNode(null, i + 1));
		
		return "\n" + StringUtils.join(nodeColumns, ",\n");
	}
	
	private String selectClauseForNode(AnnisNode node, int index) {
		String[] columns = { "id", "text_ref", "left_token", "right_token" };
		for (int i = 0; i < columns.length; ++i) {
			columns[i] = (node == null ? "NULL" : tables(node).aliasedColumn(NODE_TABLE, columns[i])) + " AS " + columns[i] + index;
		}
		return "\t" + StringUtils.join(columns, ", ");
	}

}
