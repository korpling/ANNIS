package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import annis.model.AnnisNode;
import annis.ql.parser.QueryData;

public class TestCorpusSelectionByViewByDocumentTableAccessStrategy {

	// class under test
	private CorpusSelectionByViewByDocumentTableAccessStrategy corpusSelectionByViewByDocumentTableAccessStrategy;

	// node table view name
	private final static String VIEW_NAME = "VIEW_NAME";

	// table and column alias
	private static final String TABLE_ALIAS = "TABLE_ALIAS";
	private static final String COLUMN_ALIAS = "COLUMN_ALIAS";

	// view definition if no corpus is selected
	private static final String VIEW_DEFINITION_NO_SELECTION = "" +
			"CREATE TEMPORARY VIEW " + VIEW_NAME + " " +
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
		corpusSelectionByViewByDocumentTableAccessStrategy = new CorpusSelectionByViewByDocumentTableAccessStrategy(mock(AnnisNode.class));
		corpusSelectionByViewByDocumentTableAccessStrategy.setSubQueryCorpusSelectionStrategy(subQueryCorpusSelectionStrategy);

		// set view name
		corpusSelectionByViewByDocumentTableAccessStrategy.setNodeTableViewName(VIEW_NAME);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void modifySqlSessionWithCorpusSelection() {
		// make sure the correct table and column alias is used
		corpusSelectionByViewByDocumentTableAccessStrategy.addTableAlias(NODE_TABLE, TABLE_ALIAS);
		corpusSelectionByViewByDocumentTableAccessStrategy.addColumnAlias(NODE_TABLE, "corpus_ref", COLUMN_ALIAS);
		
		// assume that some corpora are selected
		when(subQueryCorpusSelectionStrategy.hasCorpusSelection(anyList(), anyList())).thenReturn(true);
		final String CORPUS_SELECTION_SUBQUERY = "CORPUS_SELECTION_SUBQUERY";
		when(subQueryCorpusSelectionStrategy.buildSubQuery(anyList(), anyList())).thenReturn(CORPUS_SELECTION_SUBQUERY);

		// stub in row mapper for converting list of longs
		ParameterizedSingleColumnRowMapper<Long> corpusIdRowMapper = mock(ParameterizedSingleColumnRowMapper.class);
		corpusSelectionByViewByDocumentTableAccessStrategy.setCorpusIdRowMapper(corpusIdRowMapper);
		
		// expected SQL query: view definition constrained by corpus selection
		final String expectedSql = "" +
		VIEW_DEFINITION_NO_SELECTION + " " +
		"WHERE " + TABLE_ALIAS + "." + COLUMN_ALIAS + " IN ( 1, 2, 3 )";

		// return mocked document list for a corpus
		SimpleJdbcTemplate simpleJdbcTemplate = mock(SimpleJdbcTemplate.class);
		final List<Long> DOCUMENT_LIST = Arrays.asList(1L, 2L, 3L);
		// for some reason anyString(), any(ParameterizedSingleColumnRowMapper.class), doesn't work here
		// could overloading of query be the culprit
		when(simpleJdbcTemplate.query(CORPUS_SELECTION_SUBQUERY, corpusIdRowMapper)).thenReturn(DOCUMENT_LIST);
		
		// call and test
		corpusSelectionByViewByDocumentTableAccessStrategy.modifySqlSession(simpleJdbcTemplate, queryData);
		verify(simpleJdbcTemplate).query(CORPUS_SELECTION_SUBQUERY, corpusIdRowMapper);
		verify(simpleJdbcTemplate).update(expectedSql);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void modifySqlSessionNoCorpusSelection() {
		// make sure the node table is recognized even if it is aliased
		corpusSelectionByViewByDocumentTableAccessStrategy.addTableAlias(NODE_TABLE, TABLE_ALIAS);
		
		// assume that no copora are selected
		when(subQueryCorpusSelectionStrategy.hasCorpusSelection(anyList(), anyList())).thenReturn(false);

		// expected SQL query: view definition is unconstrained
		final String expectedSql = VIEW_DEFINITION_NO_SELECTION;

		// call and test
		SimpleJdbcTemplate simpleJdbcTemplate = mock(SimpleJdbcTemplate.class);
		corpusSelectionByViewByDocumentTableAccessStrategy.modifySqlSession(simpleJdbcTemplate, queryData);
		verify(simpleJdbcTemplate).update(expectedSql);
	}
	
}
