package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import annis.model.AnnisNode;

public class CountMatchesSelectClauseSqlGenerator 
	extends BaseNodeSqlGenerator
	implements SelectClauseSqlGenerator {

	public String selectClause(List<AnnisNode> nodes, int maxWidth) {
		StringBuffer sb = new StringBuffer();
		sb.append("count(DISTINCT ");
		
		List<String> selectedFields = new ArrayList<String>();

		// count all distinct node id combinations
		for (AnnisNode node : nodes) {
			selectedFields.add(tables(node).aliasedColumn(NODE_TABLE, "id"));
		}
		sb.append(StringUtils.join(selectedFields, " || '-' || "));

		sb.append(")");
		return sb.toString();
	}

}
