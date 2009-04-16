package annis.dao;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class TestCorpusSelectionStrategy1 {

	private static final String DOC_REF_COLUMN = "DOC_REF_COLUMN";
	private static final String CORPUS_CONSTRAINT = "CORPUS CONSTRAINT";
	
	// select documents for each node in WHERE clause
	@Test
	public void whereCondition() {
		final String expected = DOC_REF_COLUMN + " " + CORPUS_CONSTRAINT;
		assertWhereCondition(expected, 23L);
	}
	
	// don't emit a condition if corpus list is empty
	@Test
	public void emptyCorpusList() {
		assertWhereCondition(null);
	}

	// private helper
	
	private void assertWhereCondition(final String expected, Long... docIds) {
		List<Long> corpusList = Arrays.asList(docIds);
		CorpusSelectionStrategy1 corpusSelectionStrategy = new CorpusSelectionStrategy1() {
			@Override
			protected String corpusConstraint() {
				return CORPUS_CONSTRAINT;
			}
		};
		corpusSelectionStrategy.setCorpusList(corpusList);
		assertThat(corpusSelectionStrategy.whereClauseForNode(DOC_REF_COLUMN), is(expected));
	}
	
}
