package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import annis.dao.CorpusSelectionStrategy;
import annis.model.AnnisNode;

public class CoveredTokensSelectClauseSqlAdapter implements SelectClauseSqlAdapter {

	private TableAccessStrategyFactory tableAccessStrategyFactory;
	
	/* (non-Javadoc)
	 * @see annis.sqlgen.SelectClauseSqlAdapter#selectClause(java.util.List, annis.dao.CorpusSelectionStrategy)
	 */
	public String selectClause(List<AnnisNode> nodes, CorpusSelectionStrategy corpusSelectionStrategy) {
		StringBuffer sb = new StringBuffer();
		sb.append("DISTINCT\n");
		
		List<String> selectedFields = new ArrayList<String>();

		// add fields to SELECT clause
		for (AnnisNode node : nodes) {
			TableAccessStrategy tableAccessStrategy = tableAccessStrategyFactory.createTableAccessStrategy(node, corpusSelectionStrategy);
			String[] columns = { "id", "text_ref", "left_token", "right_token" };
			for (int i = 0; i < columns.length; ++i) {
				columns[i] = tableAccessStrategy.aliasedColumn(NODE_TABLE, columns[i]);
				if (i == 0)
					columns[i] = "\t" + columns[i];
			}
			selectedFields.add(StringUtils.join(columns, ", "));
		}
		sb.append(StringUtils.join(selectedFields, ",\n"));

		return sb.toString();
	}

	public TableAccessStrategyFactory getTableAccessStrategyFactory() {
		return tableAccessStrategyFactory;
	}

	public void setTableAccessStrategyFactory(
			TableAccessStrategyFactory tableAccessStrategyFactory) {
		this.tableAccessStrategyFactory = tableAccessStrategyFactory;
	}

}
