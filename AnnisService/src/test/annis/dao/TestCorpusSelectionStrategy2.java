package annis.dao;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import annis.model.Annotation;

public class TestCorpusSelectionStrategy2 {

	// class under test
	private CorpusSelectionStrategy2 corpusSelectionStrategy;

	// table and view name constants
	private final static String KNOWN_TABLE = "STRUCT_TABLE";
	private final static String VIEW_SUFFIX = "VIEW SUFFIX";
	private final static String GENERATED_VIEW_NAME = KNOWN_TABLE + VIEW_SUFFIX;

	private final static String CORPUS_CONSTRAINT = "CORPUS CONSTRAINT";

	private static final String VIEW_DEFINITION = "" +
			"CREATE VIEW " + GENERATED_VIEW_NAME + " " +
			"AS SELECT * " +
			"FROM " + KNOWN_TABLE + " " +
			"WHERE corpus_ref " + CORPUS_CONSTRAINT;
	
	@Before
	public void setup() {
		corpusSelectionStrategy = new CorpusSelectionStrategy2() {
			@Override
			protected String corpusConstraint() {
				return CORPUS_CONSTRAINT;
			}
		};
		corpusSelectionStrategy.setViewSuffix(VIEW_SUFFIX);
		corpusSelectionStrategy.setStructTable(KNOWN_TABLE);
	}
	
	// SQL for a view on the struct table
	@Test
	public void createViewSqlCorpora() {
		String expected = "CREATE VIEW " + GENERATED_VIEW_NAME + " " +
		"AS SELECT * " +
		"FROM " + KNOWN_TABLE + " " +
		"WHERE corpus_ref " + CORPUS_CONSTRAINT;
		assertCreateViewSqlWithCorpus(expected, Arrays.asList(23L));
	}

	// SQL for a view on the struct table
	@Test
	public void createViewSqlAnnotations() {
		assertCreateViewSqlWithAnnotations(VIEW_DEFINITION, Arrays.asList(new Annotation("namespace", "name")));
	}

	// don't create a view when corpus list is empty, don't change struct table in adapter
	@Test
	public void createViewSqlEmpty() {
		assertCreateViewSql(null, new ArrayList<Long>(), new ArrayList<Annotation>());
	}
	
	@Test
	public void viewNameUnknownTable() {
		assertThat(corpusSelectionStrategy.viewName("unknown_table"), is("unknown_table"));
	}
	
	@Test
	public void viewNameKnownTable() {
		corpusSelectionStrategy.setCorpusList(Arrays.asList(23L));
		assertThat(corpusSelectionStrategy.viewName(KNOWN_TABLE), is(GENERATED_VIEW_NAME));
	}
	
	@Test
	public void viewNameKnownTableAllCorpus() {
		corpusSelectionStrategy.setCorpusList(new ArrayList<Long>());
		assertThat(corpusSelectionStrategy.viewName(KNOWN_TABLE), is(KNOWN_TABLE));
	}
	
	///// private helper

	private void assertCreateViewSqlWithCorpus(String expected, List<Long> corpusList) {
		assertCreateViewSql(expected, corpusList, new ArrayList<Annotation>());
	}
	
	private void assertCreateViewSqlWithAnnotations(String expected, List<Annotation> annotations) {
		assertCreateViewSql(expected, new ArrayList<Long>(), annotations);
	}

	private void assertCreateViewSql(String expected, List<Long> corpusList, List<Annotation> annotations) {
		corpusSelectionStrategy.setCorpusList(corpusList);
		corpusSelectionStrategy.addMetaAnnotations(annotations);
		assertEquals(expected, corpusSelectionStrategy.createViewSql());
	}

}
