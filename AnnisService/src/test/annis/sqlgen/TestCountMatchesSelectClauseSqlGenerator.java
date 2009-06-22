package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import annis.model.AnnisNode;

public class TestCountMatchesSelectClauseSqlGenerator {

	@Test
	public void selectClause() {
		// list with 2 nodes
		AnnisNode node1 = new AnnisNode(1);
		AnnisNode node2 = new AnnisNode(2);
		List<AnnisNode> nodes = Arrays.asList(node1, node2);
		
		TableAccessStrategyFactory tableAccessStrategyFactory = mock(TableAccessStrategyFactory.class);
		for (AnnisNode node : nodes) {
			TableAccessStrategy tableAccessStrategy = new TableAccessStrategy(node);
			tableAccessStrategy.addColumnAlias(NODE_TABLE, "id", "ID");
			tableAccessStrategy.addColumnAlias(NODE_TABLE, "text_ref", "TEXT_REF");
			tableAccessStrategy.addColumnAlias(NODE_TABLE, "left_token", "LEFT_TOKEN");
			tableAccessStrategy.addColumnAlias(NODE_TABLE, "right_token", "RIGHT_TOKEN");
			when(tableAccessStrategyFactory.createTableAccessStrategy(node)).thenReturn(tableAccessStrategy);
		}
		
		CountMatchesSelectClauseSqlGenerator generator = new CountMatchesSelectClauseSqlGenerator();
		generator.setTableAccessStrategyFactory(tableAccessStrategyFactory);
		
		String expected = "count(DISTINCT node1.ID || '-' || node2.ID)";
		
		assertEquals(expected, generator.selectClause(nodes, 0));
	}
	
}
