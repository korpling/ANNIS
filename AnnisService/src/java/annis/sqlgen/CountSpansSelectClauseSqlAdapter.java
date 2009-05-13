package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import annis.dao.CorpusSelectionStrategy;
import annis.model.AnnisNode;

public class CountSpansSelectClauseSqlAdapter implements SelectClauseSqlAdapter {

	private TableAccessStrategyFactory tableAccessStrategyFactory;
	
	/* (non-Javadoc)
	 * @see annis.sqlgen.SelectClauseSqlAdapter#selectClause(java.util.List, annis.dao.CorpusSelectionStrategy)
	 */
	public String selectClause(List<AnnisNode> nodes, CorpusSelectionStrategy corpusSelectionStrategy) {
		StringBuffer sb = new StringBuffer();
		sb.append("count(DISTINCT ");
		
		List<String> selectedFields = new ArrayList<String>();

		// count all distinct node id combinations
		for (AnnisNode node : nodes) {
			TableAccessStrategy tableAccessStrategy = tableAccessStrategyFactory.createTableAccessStrategy(node, corpusSelectionStrategy);
			selectedFields.add(tableAccessStrategy.aliasedColumn(NODE_TABLE, "id"));
		}
		sb.append(StringUtils.join(selectedFields, " || '-' || "));

		sb.append(")");
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
