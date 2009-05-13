package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import annis.dao.CorpusSelectionStrategy;
import annis.model.AnnisNode;

public class TestCoveredTokensSelectClauseSqlAdapter {

	@SuppressWarnings("unchecked")
	@Test
	public void selectClause() {
		// list with 3 nodes
		AnnisNode node1 = new AnnisNode(1);
		AnnisNode node2 = new AnnisNode(2);
		List<AnnisNode> nodes = Arrays.asList(node1, node2);
		
		TableAccessStrategyFactory tableAccessStrategyFactory = new TableAccessStrategyFactory();
		tableAccessStrategyFactory.addColumnAlias(NODE_TABLE, "id", "ID");
		tableAccessStrategyFactory.addColumnAlias(NODE_TABLE, "text_ref", "TEXT_REF");
		tableAccessStrategyFactory.addColumnAlias(NODE_TABLE, "left_token", "LEFT_TOKEN");
		tableAccessStrategyFactory.addColumnAlias(NODE_TABLE, "right_token", "RIGHT_TOKEN");
		
		CoveredTokensSelectClauseSqlAdapter adapter = new CoveredTokensSelectClauseSqlAdapter();
		adapter.setTableAccessStrategyFactory(tableAccessStrategyFactory);
		
		String expected = "DISTINCT\n" +
			"\tnode1.ID, node1.TEXT_REF, node1.LEFT_TOKEN, node1.RIGHT_TOKEN,\n" +
			"\tnode2.ID, node2.TEXT_REF, node2.LEFT_TOKEN, node2.RIGHT_TOKEN" +
			"";
		
		final CorpusSelectionStrategy corpusSelectionStrategy = mock(CorpusSelectionStrategy.class);
		when(corpusSelectionStrategy.viewName(NODE_TABLE)).thenReturn(NODE_TABLE);

		assertEquals(expected, adapter.selectClause(nodes, corpusSelectionStrategy));
	}
	
}
