package annis.sqlgen;

import annis.model.AnnisNode;
import annis.model.AnnisNode.TextMatching;

public class BaseNodeSqlGenerator {

	// pluggable access to tables representing the node
	private TableAccessStrategyFactory tableAccessStrategyFactory;
	
	///// Helpers
	
	protected TableAccessStrategy tables(AnnisNode node) {
		return tableAccessStrategyFactory.createTableAccessStrategy(node);
	}
	
	protected String tableAliasDefinition(AnnisNode node, String table, int count) {
		StringBuffer sb = new StringBuffer();
	
		sb.append(tables(node).tableName(table));
		sb.append(" AS ");
		sb.append(tables(node).aliasedTable(table, count));
	
		return sb.toString();
	}

	protected String join(String op, String lhs, String rhs) {
		return lhs + " " + op + " " + rhs;
	}

	protected String numberJoin(String op, String lhs, String rhs,
			int offset) {
				String plus = offset >= 0 ? " + " : " - ";
				return join(op, lhs, rhs) + plus + String.valueOf(Math.abs(offset));
			}

	protected String sqlString(String string) {
		return "'" + string + "'";
	}

	protected String sqlString(String string, TextMatching textMatching) {
		if (textMatching == TextMatching.REGEXP)
			string = "^" + string + "$";
		return sqlString(string);
	}

	///// Getter / Setter

	public TableAccessStrategyFactory getTableAccessStrategyFactory() {
		return tableAccessStrategyFactory;
	}

	public void setTableAccessStrategyFactory(TableAccessStrategyFactory tableAccessStrategyFactory) {
		this.tableAccessStrategyFactory = tableAccessStrategyFactory;
	}

}