package annis.sqlgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import annis.dao.CorpusSelectionStrategy;
import annis.model.AnnisNode;
import annis.sqlgen.model.Join;
import annis.sqlgen.model.RankTableJoin;

public class ClauseSqlAdapter {

	// pluggable factory that creates objects for SQL table representation of a node
	private NodeSqlAdapterFactory nodeSqlAdapterFactory;
	
	public String toSql(List<AnnisNode> nodes, int maxWidth, CorpusSelectionStrategy corpusSelectionStrategy) {
		Assert.notEmpty(nodes, "empty node list");
		Assert.isTrue(maxWidth >= nodes.size(), "maxWidth < nodes.size()");
		
		Map<AnnisNode, NodeSqlAdapter> sqlAdapters = sqlAdaptersForNodes(nodes, corpusSelectionStrategy);
		
		StringBuffer sb = new StringBuffer();
		appendSelectClause(sb, nodes, sqlAdapters, maxWidth);
		appendFromClause(sb, nodes, sqlAdapters);
		appendWhereClause(sb, nodes, sqlAdapters);
		
		return sb.toString();
	}
	
	///// SELECT clause generation

	void appendSelectClause(StringBuffer sb, List<AnnisNode> nodes, Map<AnnisNode, NodeSqlAdapter> adapters, int maxWidth) {
		List<String> selectedFields = new ArrayList<String>();

		// add fields to SELECT clause
		for (AnnisNode node : nodes) {
			NodeSqlAdapter adapter = adapters.get(node);
			selectedFields.add(adapter.selectClause());
		}
		
		// pad SELECT clause with NULLs if nodes.size() < maxWidth
		if (nodes.size() < maxWidth) {
			NodeSqlAdapter nullAdapter = nodeSqlAdapterFactory.createNodeSqlAdapter();
			for (int j = nodes.size(); j < maxWidth; ++j) {
				selectedFields.add(nullAdapter.selectClauseNullValues());
			}
		}
		
		// create SELECT clause
		sb.append("SELECT DISTINCT");
		sb.append("\n\t");
		sb.append(StringUtils.join(selectedFields, ",\n\t"));
		sb.append("\n");
	}
	
	///// FROM clause generation
		
	void appendFromClause(StringBuffer sb, List<AnnisNode> nodes, Map<AnnisNode, NodeSqlAdapter> adapters) {
		sb.append("FROM");
		List<String> fromTables = new ArrayList<String>();
		for (AnnisNode node : nodes) {
			NodeSqlAdapter adapter = adapters.get(node);
			fromTables.add(adapter.fromClause());
		}
		sb.append("\n\t");
		sb.append(StringUtils.join(fromTables, ",\n\t"));
		sb.append("\n");
	}
	
	///// WHERE clause generation

	void appendWhereClause(StringBuffer sb, List<AnnisNode> nodes, Map<AnnisNode, NodeSqlAdapter> adapters) {
		
		// treat each condition as mutable string to remove last AND
		List<StringBuffer> whereClause = new ArrayList<StringBuffer>();
		for (AnnisNode node : nodes) {
			// append node comment
			whereClause.add(new StringBuffer("-- " + node));
			// append node conditions
			NodeSqlAdapter adapter = adapters.get(node);
			for (String constraint : adapter.whereClause())
				whereClause.add(new StringBuffer(constraint));
		}
		
		// append AND to each condition in WHERE clause, skip comments, remember last condition
		StringBuffer lastConstraint = null;
		for (StringBuffer constraint : whereClause) {
			if ( ! constraint.toString().startsWith("--") ) {
				constraint.append(" AND");
				lastConstraint = constraint;
			}
		}
		
		// no node with condition, just print out the node comments
		if (lastConstraint == null) {
			for (StringBuffer constraint : whereClause) {
				sb.append("\t");
				sb.append(constraint);
				sb.append("\n");
			}
			
		// at least one condition, prepend WHERE and remove AND from last condition
		} else {
			sb.append("WHERE");
			lastConstraint.setLength(lastConstraint.length() - " AND".length());
			for (StringBuffer constraint : whereClause) {
				sb.append("\n\t");
				sb.append(constraint);
			}
			sb.append("\n");
		}
	}

	///// helper
	
	Map<AnnisNode, NodeSqlAdapter> sqlAdaptersForNodes(List<AnnisNode> nodes, CorpusSelectionStrategy corpusSelectionStrategy) {
		Map<AnnisNode, Boolean> nodesInDominanceJoin = computeNodesInDominanceJoin(nodes);
		
		Map<AnnisNode, NodeSqlAdapter> result = new HashMap<AnnisNode, NodeSqlAdapter>();
		for (AnnisNode node : nodes) {
			boolean inDominanceJoin = nodesInDominanceJoin.get(node);
			NodeSqlAdapter adapter = nodeSqlAdapterFactory.createNodeSqlAdapter(node, corpusSelectionStrategy);
			result.put(node, adapter);
		}
		return result;
	}
		
	@Deprecated
	Map<AnnisNode, Boolean> computeNodesInDominanceJoin(List<AnnisNode> nodes) {
		Map<AnnisNode, Boolean> usedRankTables = new HashMap<AnnisNode, Boolean>();
		
		// initialization: no node uses the rank table
		for (AnnisNode node : nodes) {
			usedRankTables.put(node, false);
		}
		
		// look for nodes that take part in dominance joins
		for (AnnisNode node : nodes) {
			for (Join join : node.getJoins()) {
				if (join instanceof RankTableJoin) {
					usedRankTables.put(node, true);
					usedRankTables.put(join.getTarget(), true);
				}
			}
		}
		
		return usedRankTables;
	}

	///// Getter / Setter

	public NodeSqlAdapterFactory getNodeSqlAdapterFactory() {
		return nodeSqlAdapterFactory;
	}

	public void setNodeSqlAdapterFactory(NodeSqlAdapterFactory nodeSqlAdapterFactory) {
		this.nodeSqlAdapterFactory = nodeSqlAdapterFactory;
	}


}
