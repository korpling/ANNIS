package annis.sqlgen;

import annis.dao.CorpusSelectionStrategy;
import annis.model.AnnisNode;

public abstract class NodeSqlAdapterFactory {
	
	@Deprecated
	public NodeSqlAdapter createNodeSqlAdapter(AnnisNode node, boolean inDominanceJoin, CorpusSelectionStrategy corpusSelectionStrategy) {
		return createNodeSqlAdapter(node, corpusSelectionStrategy);
	}
	
	public NodeSqlAdapter createNodeSqlAdapter(AnnisNode node, CorpusSelectionStrategy corpusSelectionStrategy) {
		AbstractNodeSqlAdapter adapter = createNodeSqlAdapter();
		adapter.setNode(node);
		adapter.setCorpusSelectionStrategy(corpusSelectionStrategy);
		corpusSelectionStrategy.registerNodeAdapter(adapter);
		return adapter;
	}
	
	public abstract AbstractNodeSqlAdapter createNodeSqlAdapter();

}