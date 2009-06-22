package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import annis.model.AnnisNode;
import annis.ql.parser.QueryData;

public class TestCorpusSelectionByViewTableAccessStrategy {

	// class under test
	private CorpusSelectionByViewTableAccessStrategy corpusSelectionByViewTableAccessStrategy;

	// node table view name
	private final static String VIEW_NAME = "VIEW_NAME";

	// table and column alias
	private static final String TABLE_ALIAS = "TABLE_ALIAS";
	private static final String COLUMN_ALIAS = "COLUMN_ALIAS";

	// view definition if no corpus is selected
	private static final String VIEW_DEFINITION_NO_SELECTION = "" +
			"CREATE VIEW " + VIEW_NAME + " " +
			"AS SELECT * " +
			"FROM " + TABLE_ALIAS;
	
	// depencencies
	@Mock SubQueryCorpusSelectionStrategy subQueryCorpusSelectionStrategy;
	
	// arguments needed for SqlSessionModifier.modifySqlSession()
	// not really used in this test
	@Mock private QueryData queryData;
	
	@Before
	public void setup() {
		// setup class and depencies
		initMocks(this);
		corpusSelectionByViewTableAccessStrategy = new CorpusSelectionByViewTableAccessStrategy(mock(AnnisNode.class));
		corpusSelectionByViewTableAccessStrategy.setSubQueryCorpusSelectionStrategy(subQueryCorpusSelectionStrategy);

		// set view name
		corpusSelectionByViewTableAccessStrategy.setNodeTableViewName(VIEW_NAME);
	}
	
	@Test
	public void tableNameReturnsViewNameForNodeTable() {
		// make sure the node table is recognized even if it is aliased
		corpusSelectionByViewTableAccessStrategy.addTableAlias(NODE_TABLE, TABLE_ALIAS);
		assertEquals(VIEW_NAME, corpusSelectionByViewTableAccessStrategy.tableName(NODE_TABLE));
	}
	
	@Test
	public void tableNameDelegatesForAllOtherTables() {
		corpusSelectionByViewTableAccessStrategy.addTableAlias(RANK_TABLE, TABLE_ALIAS);
		assertEquals(TABLE_ALIAS, corpusSelectionByViewTableAccessStrategy.tableName(RANK_TABLE));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void modifySqlSessionWithCorpusSelection() {
		// make sure the correct table and column alias is used
		corpusSelectionByViewTableAccessStrategy.addTableAlias(NODE_TABLE, TABLE_ALIAS);
		corpusSelectionByViewTableAccessStrategy.addColumnAlias(NODE_TABLE, "corpus_ref", COLUMN_ALIAS);
		
		// assume that some corpora are selected
		when(subQueryCorpusSelectionStrategy.hasCorpusSelection(anyList(), anyList())).thenReturn(true);
		final String CORPUS_SELECTION_SUBQUERY = "CORPUS_SELECTION_SUBQUERY";
		when(subQueryCorpusSelectionStrategy.buildSubQuery(anyList(), anyList())).thenReturn(CORPUS_SELECTION_SUBQUERY);

		// expected SQL query: view definition constrained by corpus selection
		final String expectedSql = "" +
		VIEW_DEFINITION_NO_SELECTION + " " +
		"WHERE " + TABLE_ALIAS + "." + COLUMN_ALIAS + " IN ( " + CORPUS_SELECTION_SUBQUERY + " )";

		// call and test
		SimpleJdbcTemplate simpleJdbcTemplate = mock(SimpleJdbcTemplate.class);
		corpusSelectionByViewTableAccessStrategy.modifySqlSession(simpleJdbcTemplate, queryData);
		verify(simpleJdbcTemplate).update(expectedSql);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void modifySqlSessionNoCorpusSelection() {
		// make sure the node table is recognized even if it is aliased
		corpusSelectionByViewTableAccessStrategy.addTableAlias(NODE_TABLE, TABLE_ALIAS);
		
		// assume that no copora are selected
		when(subQueryCorpusSelectionStrategy.hasCorpusSelection(anyList(), anyList())).thenReturn(false);

		// expected SQL query: view definition is unconstrained
		final String expectedSql = VIEW_DEFINITION_NO_SELECTION;

		// call and test
		SimpleJdbcTemplate simpleJdbcTemplate = mock(SimpleJdbcTemplate.class);
		corpusSelectionByViewTableAccessStrategy.modifySqlSession(simpleJdbcTemplate, queryData);
		verify(simpleJdbcTemplate).update(expectedSql);
	}
	
}
