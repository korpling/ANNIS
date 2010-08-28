package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.ql.parser.QueryData;

public class TestCorpusSelectionByViewOnlyToplevelTableAccessStrategy {

	// class under test
	private CorpusSelectionByViewOnlyToplevelTableAccessStrategy corpusSelectionByViewOnlyToplevelTableAccessStrategy;

	// node table view name
	private final static String FACTS_VIEW_NAME = "FACTS_VIEW_NAME";
  private final static String FACTS_CONTEXT_VIEW_NAME = "FACTS_CONTEXT_VIEW_NAME";
  private final static String NODE_VIEW_NAME = "NODE_VIEW_NAME";

	// corpus list
	private static final List<Long> CORPUS_LIST = Arrays.asList(1L, 2L, 3L);
	private static final String CORPUS_LIST_AS_STRING = StringUtils.join(CORPUS_LIST, ", ");
	
	// table and column aliases
	private static final String TABLE_ALIAS = "TABLE_ALIAS";
	private static final String TOPLEVEL_ALIAS = "TOPLEVEL_ALIAS";
	private static final String CORPUS_ALIAS = "CORPUS_ALIAS";

	// view definition if no corpus is selected
	private static final String VIEW_DEFINITION_NO_SELECTION = "" +
			"CREATE TEMPORARY VIEW " + FACTS_VIEW_NAME + " " +
			"AS SELECT * " +
			"FROM " + TABLE_ALIAS;
	
	// query data contains metadata
	@Mock private QueryData queryData;
	
	// used to determine documents with appropriate metadata
	@Mock private SubQueryCorpusSelectionStrategy subQueryCorpusSelectionStrategy;
	
	@Before
	public void setup() {
		// setup class and depencies
		initMocks(this);
		corpusSelectionByViewOnlyToplevelTableAccessStrategy = new CorpusSelectionByViewOnlyToplevelTableAccessStrategy(mock(AnnisNode.class));
		corpusSelectionByViewOnlyToplevelTableAccessStrategy.setSubQueryCorpusSelectionStrategy(subQueryCorpusSelectionStrategy);
		
		// set view name and make sure table and column aliases are used
		corpusSelectionByViewOnlyToplevelTableAccessStrategy.setFactsViewName(FACTS_VIEW_NAME);
    corpusSelectionByViewOnlyToplevelTableAccessStrategy.setFactsContextViewName(FACTS_CONTEXT_VIEW_NAME);
    corpusSelectionByViewOnlyToplevelTableAccessStrategy.setNodeViewName(NODE_VIEW_NAME);
		corpusSelectionByViewOnlyToplevelTableAccessStrategy.addTableAlias(NODE_TABLE, TABLE_ALIAS);
		corpusSelectionByViewOnlyToplevelTableAccessStrategy.addColumnAlias(NODE_TABLE, "toplevel_corpus", TOPLEVEL_ALIAS);
		corpusSelectionByViewOnlyToplevelTableAccessStrategy.addColumnAlias(NODE_TABLE, "corpus_ref", CORPUS_ALIAS);
		
		// stub in corpus list
		when(queryData.getCorpusList()).thenReturn(CORPUS_LIST);
	}
	
	@Test
	public void tableNameReturnsViewNameForNodeTable() {
		// make sure the node table is recognized even if it is aliased
		corpusSelectionByViewOnlyToplevelTableAccessStrategy.addTableAlias(NODE_TABLE, TABLE_ALIAS);
		assertEquals(FACTS_VIEW_NAME, corpusSelectionByViewOnlyToplevelTableAccessStrategy.tableName(NODE_TABLE));
	}
	
	@Test
	public void tableNameDelegatesForAllOtherTables() {
		// make sure node table is not aliased
		corpusSelectionByViewOnlyToplevelTableAccessStrategy.addTableAlias(NODE_TABLE, NODE_TABLE);
		// alias another table
		corpusSelectionByViewOnlyToplevelTableAccessStrategy.addTableAlias(RANK_TABLE, TABLE_ALIAS);
		// test alias of other table
		assertEquals(TABLE_ALIAS, corpusSelectionByViewOnlyToplevelTableAccessStrategy.tableName(RANK_TABLE));
	}
	
	@Test
	public void modifySqlSessionWithCorpusSelection() {
		// expected SQL query: view definition constrained by corpus selection
		final String expectedSql = "" +
		VIEW_DEFINITION_NO_SELECTION + " " +
		"WHERE " + TABLE_ALIAS + "." + TOPLEVEL_ALIAS + " IN ( " + CORPUS_LIST_AS_STRING + " )";

		// call and test
		SimpleJdbcTemplate simpleJdbcTemplate = mock(SimpleJdbcTemplate.class);
		corpusSelectionByViewOnlyToplevelTableAccessStrategy.modifySqlSession(simpleJdbcTemplate, queryData);
		verify(simpleJdbcTemplate).update(expectedSql);
	}

	@Test
	public void modifySqlSessionNoCorpusSelection() {
		// assume that no copora are selected
		when(queryData.getCorpusList()).thenReturn(new ArrayList<Long>());

		// expected SQL query: view definition is unconstrained
		final String expectedSql = VIEW_DEFINITION_NO_SELECTION;

		// call and test
		SimpleJdbcTemplate simpleJdbcTemplate = mock(SimpleJdbcTemplate.class);
		corpusSelectionByViewOnlyToplevelTableAccessStrategy.modifySqlSession(simpleJdbcTemplate, queryData);
		verify(simpleJdbcTemplate).update(expectedSql);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void modifySqlSessionWithCorpusAndMetaData() {
		// stub in documents with metadata
		when(queryData.getMetaData()).thenReturn(Arrays.asList(new Annotation("NAMESPACE", "NAME")));
		final String METADATA_SQL = "METADATA_SQL";
		when(subQueryCorpusSelectionStrategy.buildSubQuery(anyList(), anyList())).thenReturn(METADATA_SQL);
		final List<Long> METADATA_CORPUS_LIST = Arrays.asList(10L, 20L, 30L);
		final String METADATA_CORPUS_LIST_AS_STRING = StringUtils.join(METADATA_CORPUS_LIST, ", ");
		SimpleJdbcTemplate simpleJdbcTemplate = mock(SimpleJdbcTemplate.class);
		when(simpleJdbcTemplate.query(anyString(), any(ParameterizedRowMapper.class))).thenReturn(METADATA_CORPUS_LIST);
		
		// expected SQL query: view definition constrained by corpus selection
		final String expectedSql = "" +
			VIEW_DEFINITION_NO_SELECTION + " " +
			"WHERE " + TABLE_ALIAS + "." + TOPLEVEL_ALIAS + " IN ( " + CORPUS_LIST_AS_STRING + " ) " +
			"AND " + TABLE_ALIAS + "." + CORPUS_ALIAS + " IN ( " + METADATA_CORPUS_LIST_AS_STRING + " )";

		// call and test
		corpusSelectionByViewOnlyToplevelTableAccessStrategy.modifySqlSession(simpleJdbcTemplate, queryData);
		verify(simpleJdbcTemplate).update(expectedSql);
	}
}
