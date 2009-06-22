package annis.sqlgen;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static test.IsCollectionEmpty.empty;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import annis.model.AnnisNode;
import annis.model.Annotation;


public class TestCorpusSelectionInWhereClauseSqlGenerator {

	// class under test
	private CorpusSelectionInWhereClauseSqlGenerator generator;
	
	// dependencies
	@Mock TableAccessStrategyFactory tableAccessStrategyFactory;
	@Mock SubQueryCorpusSelectionStrategy subQueryCorpusSelectionStrategy;
	
	// arguments needed for WhereClauseSqlGenerator.whereConditions()
	// not really used in this test
	@Mock private List<Long> corpusList;
	@Mock private List<Annotation> metaData;
	@Mock private AnnisNode node;
	
	@Before
	public void setup() {
		initMocks(this);
		generator = new CorpusSelectionInWhereClauseSqlGenerator();
		generator.setSubQueryCorpusSelectionStrategy(subQueryCorpusSelectionStrategy);
		generator.setTableAccessStrategyFactory(tableAccessStrategyFactory);
	}
	
	// emit a condition for the where clause if some corpora have been selected
	@SuppressWarnings("unchecked")
	@Test
	public void whereConditionsWithCorpusSelection() {
		// make the SQL generator think that some corpora have been selected
		when(subQueryCorpusSelectionStrategy.hasCorpusSelection(anyList(), anyList())).thenReturn(true);

		// dummy corpus selection sub query
		final String CORPUS_SELECTION_SUBQUERY = "CORPUS_SELECTION_SUBQUERY";
		when(subQueryCorpusSelectionStrategy.buildSubQuery(anyList(), anyList())).thenReturn(CORPUS_SELECTION_SUBQUERY);
		
		// make sure the corpus_ref column is not hard-coded 
		TableAccessStrategy tableAccessStrategy = mock(TableAccessStrategy.class);
		when(tableAccessStrategyFactory.createTableAccessStrategy(any(AnnisNode.class))).thenReturn(tableAccessStrategy);
		final String TABLE_COLUMN = "table.column";
		when(tableAccessStrategy.aliasedColumn(TableAccessStrategy.NODE_TABLE, "corpus_ref")).thenReturn(TABLE_COLUMN);

		// test: corpus_ref column is constrained to the corpus selection sub query
		List<String> expected = Arrays.asList(TABLE_COLUMN + " IN (" + CORPUS_SELECTION_SUBQUERY + ")");
		assertThat(generator.whereConditions(node, corpusList, metaData), is(expected));
	}
	
	// don't emit a condition if no corpus is selected
	@SuppressWarnings("unchecked")
	@Test
	public void whereConditionsNoCorpusSelection() {
		// make the SQL generator think that no corpus has been selected
		when(subQueryCorpusSelectionStrategy.hasCorpusSelection(anyList(), anyList())).thenReturn(false);
		
		// test: no where conditions
		assertThat(generator.whereConditions(node, corpusList, metaData), is(empty()));
	}


}
