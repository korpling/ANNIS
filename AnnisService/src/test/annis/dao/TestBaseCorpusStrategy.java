package annis.dao;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import annis.model.Annotation;
import annis.model.AnnisNode.TextMatching;

public class TestBaseCorpusStrategy {

	@Test
	public void corpusConstraintOneCorpus() {
		String expected = "" +
			"IN ( " +
				"SELECT DISTINCT c1.id " +
				"FROM corpus AS c1, corpus AS c2 " +
				"WHERE c1.pre >= c2.pre " +
				"AND c1.post <= c2.post " +
				"AND c2.id IN ( 23 ) " +
			")";
		assertCorpusConstraint(expected, 23L);
	}
	
	@Test
	public void corpusConstraintManyCorpus() {
		String expected = "" +
			"IN ( " +
				"SELECT DISTINCT c1.id " +
				"FROM corpus AS c1, corpus AS c2 " +
				"WHERE c1.pre >= c2.pre " +
				"AND c1.post <= c2.post " +
				"AND c2.id IN ( 23, 42, 69 ) " +
			")";
		assertCorpusConstraint(expected, 23L, 42L, 69L);
	}
	
	@Test
	public void corpusConstraintEmptyCorpusList() {
		assertCorpusConstraint(null);
	}
	
	@Test
	public void corpusConstraintEmptyCorpusListAnnotation() {
		String expected = "" +
		"IN ( " +
			"SELECT DISTINCT c1.id " +
			"FROM corpus AS c1, corpus_meta_attribute AS corpus_meta_attribute1, corpus_meta_attribute AS corpus_meta_attribute2, corpus_meta_attribute AS corpus_meta_attribute3 " +
			"WHERE corpus_meta_attribute1.namespace = 'namespace1' " +
			"AND corpus_meta_attribute1.name = 'name1' " +
			"AND corpus_meta_attribute1.corpus_ref = c1.id " +
			"AND corpus_meta_attribute2.namespace = 'namespace2' " +
			"AND corpus_meta_attribute2.name = 'name2' " +
			"AND corpus_meta_attribute2.value = 'value2' " +
			"AND corpus_meta_attribute2.corpus_ref = c1.id " +
			"AND corpus_meta_attribute3.namespace = 'namespace3' " +
			"AND corpus_meta_attribute3.name = 'name3' " +
			"AND corpus_meta_attribute3.value ~ 'value3' " +
			"AND corpus_meta_attribute3.corpus_ref = c1.id " +
		")";
		
		Annotation annotation1 = new Annotation("namespace1", "name1");
		Annotation annotation2 = new Annotation("namespace2", "name2", "value2", TextMatching.EXACT);
		Annotation annotation3 = new Annotation("namespace3", "name3", "value3", TextMatching.REGEXP);
		
		BaseCorpusSelectionStrategy corpusSelectionStrategy = new BaseCorpusSelectionStrategy();
		corpusSelectionStrategy.addMetaAnnotations(Arrays.asList(annotation1, annotation2, annotation3));
		assertEquals(expected, corpusSelectionStrategy.corpusConstraint());
	}
	
	@Test
	public void corpusConstraintCorpusListAndAnnotation() {
		String expected = "" +
		"IN ( " +
			"SELECT DISTINCT c1.id " +
			"FROM corpus AS c1, corpus AS c2, corpus_meta_attribute AS corpus_meta_attribute1, corpus_meta_attribute AS corpus_meta_attribute2, corpus_meta_attribute AS corpus_meta_attribute3 " +
			"WHERE c1.pre >= c2.pre " +
			"AND c1.post <= c2.post " +
			"AND c2.id IN ( 23, 42, 69 ) " +
			"AND corpus_meta_attribute1.namespace = 'namespace1' " +
			"AND corpus_meta_attribute1.name = 'name1' " +
			"AND corpus_meta_attribute1.corpus_ref = c1.id " +
			"AND corpus_meta_attribute2.namespace = 'namespace2' " +
			"AND corpus_meta_attribute2.name = 'name2' " +
			"AND corpus_meta_attribute2.value = 'value2' " +
			"AND corpus_meta_attribute2.corpus_ref = c1.id " +
			"AND corpus_meta_attribute3.namespace = 'namespace3' " +
			"AND corpus_meta_attribute3.name = 'name3' " +
			"AND corpus_meta_attribute3.value ~ 'value3' " +
			"AND corpus_meta_attribute3.corpus_ref = c1.id " +
		")";
		
		Annotation annotation1 = new Annotation("namespace1", "name1");
		Annotation annotation2 = new Annotation("namespace2", "name2", "value2", TextMatching.EXACT);
		Annotation annotation3 = new Annotation("namespace3", "name3", "value3", TextMatching.REGEXP);
		
		BaseCorpusSelectionStrategy corpusSelectionStrategy = new BaseCorpusSelectionStrategy();
		corpusSelectionStrategy.addMetaAnnotations(Arrays.asList(annotation1, annotation2, annotation3));
		corpusSelectionStrategy.setCorpusList(Arrays.asList(23L, 42L, 69L));
		assertEquals(expected, corpusSelectionStrategy.corpusConstraint());
	}
	
	///// private helpers
	
	private void assertCorpusConstraint(String expected, Long... docIds) {
		List<Long> corpusList = Arrays.asList(docIds);
		BaseCorpusSelectionStrategy corpusSelectionStrategy = new CorpusSelectionStrategy1();
		corpusSelectionStrategy.setCorpusList(corpusList);
		assertEquals(expected, corpusSelectionStrategy.corpusConstraint());
	}
	
	
}
