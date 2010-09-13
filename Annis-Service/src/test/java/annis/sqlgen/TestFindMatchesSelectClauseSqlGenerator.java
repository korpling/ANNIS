package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import annis.model.AnnisNode;

public class TestFindMatchesSelectClauseSqlGenerator {

	// object under test
	private FindMatchesSelectClauseSqlGenerator generator;
	
	// dependencies
	@Mock private TableAccessStrategyFactory tableAccessStrategyFactory;
	
	// some nodes
	private AnnisNode node23;
	private AnnisNode node42;
	
	@Before
	public void setup() {
		initMocks(this);
		generator = new FindMatchesSelectClauseSqlGenerator();
		
		node23 = new AnnisNode(23);
		node42 = new AnnisNode(42);
		
		// add table aliases to make sure the table access strategy is used
		for (AnnisNode node : Arrays.asList(node23, node42)) {
			TableAccessStrategy tableAccessStrategy = new TableAccessStrategy(node);
			tableAccessStrategy.addTableAlias(NODE_TABLE, "_node");
			when(tableAccessStrategyFactory.createTableAccessStrategy(node)).thenReturn(tableAccessStrategy);
		}
		generator.setTableAccessStrategyFactory(tableAccessStrategyFactory);
	}
	
	// return id, text_ref, left_token, right_token for every node
	@Test
	public void selectClauseOneMatch() {
		String expected = "" +
				"\t_node23.id AS id1, _node23.text_ref AS text_ref1, _node23.left_token AS left_token1, _node23.right_token AS right_token1";
		String actual = generator.selectClause(Arrays.asList(node23), 1);
		assertEquals(expected, actual);
	}
	
	// put columns on new line for every node
	@Test
	public void selectClauseManyMatches() {
		String expected = "" +
			"\t_node23.id AS id1, _node23.text_ref AS text_ref1, _node23.left_token AS left_token1, _node23.right_token AS right_token1,\n" +
			"\t_node42.id AS id2, _node42.text_ref AS text_ref2, _node42.left_token AS left_token2, _node42.right_token AS right_token2";
		String actual = generator.selectClause(Arrays.asList(node23, node42), 2);
		assertEquals(expected, actual);
	}
	
	// return NULL fields if less nodes than maxWidth are supplied
	@Test
	public void selectClausePadForMissingNodes() {
		String expected = "" +
			"\t_node23.id AS id1, _node23.text_ref AS text_ref1, _node23.left_token AS left_token1, _node23.right_token AS right_token1,\n" +
			"\tNULL AS id2, NULL AS text_ref2, NULL AS left_token2, NULL AS right_token2";
		assertEquals(expected, generator.selectClause(Arrays.asList(node23), 2));
	}

	// throw error if nodes.size() > maxWidth
	@Test(expected=IllegalArgumentException.class)
	public void selectClauseBadMaxWidth() {
		generator.selectClause(Arrays.asList(node23, node42), 1);
	}
	
}
