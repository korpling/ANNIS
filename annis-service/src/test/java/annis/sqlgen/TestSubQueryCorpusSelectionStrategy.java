/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.sqlgen;

import annis.model.QueryAnnotation;
import annis.model.QueryNode.TextMatching;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class TestSubQueryCorpusSelectionStrategy {

	private List<Long> corpusList;
	private List<QueryAnnotation> metaData;
	private SubQueryCorpusSelectionStrategy strategy;
	
	@Before
	public void setup() {
		corpusList = new ArrayList<>();
		metaData = new ArrayList<>();
		strategy = new SubQueryCorpusSelectionStrategy();
	}

	@Test
	public void hasCorpusSelectionFalse() {
		assertThat(strategy.hasCorpusSelection(corpusList, metaData), is(false));
	}
	
	@Test
	public void hasCorpusSelectionCorpusList() {
		corpusList = Arrays.asList(23L);
		assertThat(strategy.hasCorpusSelection(corpusList, metaData), is(true));
	}
	
	@Test
	public void hasCorpusSelectionMetaData() {
		metaData = Arrays.asList(new QueryAnnotation("NAMESPACE", "NAME"));
		assertThat(strategy.hasCorpusSelection(corpusList, metaData), is(true));
	}
	
	@Test
	public void buildSubQueryOneCorpus() {
		String expected = "" +
				"SELECT DISTINCT c1.id " +
				"FROM corpus AS c1, corpus AS c2 " +
				"WHERE c1.pre >= c2.pre " +
				"AND c1.post <= c2.post " +
				"AND c2.id IN ( 23 )";
		corpusList = Arrays.asList(23L);
		assertEquals(expected, strategy.buildSubQuery(corpusList, metaData));
	}
	
	@Test
	public void buildSubQueryManyCorpus() {
		String expected = "" +
				"SELECT DISTINCT c1.id " +
				"FROM corpus AS c1, corpus AS c2 " +
				"WHERE c1.pre >= c2.pre " +
				"AND c1.post <= c2.post " +
				"AND c2.id IN ( 23, 42, 69 )";
		corpusList = Arrays.asList(23L, 42L, 69L);
		assertEquals(expected, strategy.buildSubQuery(corpusList, metaData));
	}
	
	@Test
	public void buildSubQueryEmptyCorpusList() {
		String expected = "SELECT DISTINCT c1.id FROM corpus AS c1";
		assertEquals(expected, strategy.buildSubQuery(corpusList, metaData));
	}
	
	@Test
	public void corpusConstraintEmptyCorpusListAnnotation() {
		String expected = "" +
			"SELECT DISTINCT c1.id " +
			"FROM corpus AS c1, corpus_annotation AS corpus_annotation1, corpus_annotation AS corpus_annotation2, corpus_annotation AS corpus_annotation3 " +
			"WHERE corpus_annotation1.namespace = 'namespace1' " +
			"AND corpus_annotation1.name = 'name1' " +
			"AND corpus_annotation1.corpus_ref = c1.id " +
			"AND corpus_annotation2.namespace = 'namespace2' " +
			"AND corpus_annotation2.name = 'name2' " +
			"AND corpus_annotation2.value = 'value2' " +
			"AND corpus_annotation2.corpus_ref = c1.id " +
			"AND corpus_annotation3.namespace = 'namespace3' " +
			"AND corpus_annotation3.name = 'name3' " +
			"AND corpus_annotation3.value ~ '^value3$' " +
			"AND corpus_annotation3.corpus_ref = c1.id";
		
		QueryAnnotation annotation1 = new QueryAnnotation("namespace1", "name1");
		QueryAnnotation annotation2 = new QueryAnnotation("namespace2", "name2", "value2", TextMatching.EXACT_EQUAL);
		QueryAnnotation annotation3 = new QueryAnnotation("namespace3", "name3", "value3", TextMatching.REGEXP_EQUAL);
		
		metaData = Arrays.asList(annotation1, annotation2, annotation3);
		assertEquals(expected, strategy.buildSubQuery(corpusList, metaData));
	}
	
	@Test
	public void corpusConstraintCorpusListAndAnnotation() {
		String expected = "" +
			"SELECT DISTINCT c1.id " +
			"FROM corpus AS c1, corpus AS c2, corpus_annotation AS corpus_annotation1, corpus_annotation AS corpus_annotation2, corpus_annotation AS corpus_annotation3 " +
			"WHERE c1.pre >= c2.pre " +
			"AND c1.post <= c2.post " +
			"AND c2.id IN ( 23, 42, 69 ) " +
			"AND corpus_annotation1.namespace = 'namespace1' " +
			"AND corpus_annotation1.name = 'name1' " +
			"AND corpus_annotation1.corpus_ref = c1.id " +
			"AND corpus_annotation2.namespace = 'namespace2' " +
			"AND corpus_annotation2.name = 'name2' " +
			"AND corpus_annotation2.value = 'value2' " +
			"AND corpus_annotation2.corpus_ref = c1.id " +
			"AND corpus_annotation3.namespace = 'namespace3' " +
			"AND corpus_annotation3.name = 'name3' " +
			"AND corpus_annotation3.value ~ '^value3$' " +
			"AND corpus_annotation3.corpus_ref = c1.id";
		
		QueryAnnotation annotation1 = new QueryAnnotation("namespace1", "name1");
		QueryAnnotation annotation2 = new QueryAnnotation("namespace2", "name2", "value2", TextMatching.EXACT_EQUAL);
		QueryAnnotation annotation3 = new QueryAnnotation("namespace3", "name3", "value3", TextMatching.REGEXP_EQUAL);
		
		corpusList = Arrays.asList(23L, 42L, 69L);
		metaData = Arrays.asList(annotation1, annotation2, annotation3);
		assertEquals(expected, strategy.buildSubQuery(corpusList, metaData));
	}
	
}
