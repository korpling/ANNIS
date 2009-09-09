package annis.sqlgen;

import annis.model.AnnisNode;

public class TableAccessStrategyFactory {

	public TableAccessStrategy createTableAccessStrategy(AnnisNode node) {
		TableAccessStrategy tableAccessStrategy = createTableAccessStrategy();
		tableAccessStrategy.setNode(node);
		return tableAccessStrategy;
	}
	
	protected TableAccessStrategy createTableAccessStrategy() {
		return new TableAccessStrategy();
	}

}
