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
package annis.ql.parser;

import de.deutschdiachrondigital.dddquery.parser.QueryAnalysis;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newPathExpr;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import annis.model.QueryNode;
import annis.model.QueryAnnotation;
import de.deutschdiachrondigital.dddquery.parser.ClauseAnalysis;
import de.deutschdiachrondigital.dddquery.parser.DnfTransformer;
import de.deutschdiachrondigital.dddquery.node.APathExpr;
import de.deutschdiachrondigital.dddquery.node.PExpr;
import de.deutschdiachrondigital.dddquery.node.Start;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:annis/sqlgen/SqlGenerator-context.xml", 
		"classpath:de/deutschdiachrondigital/dddquery/parser/DddQueryParser-context.xml"})
public class TestQueryAnalysis {

	// simple QueryAnalysis instance with mocked dependencies
	private QueryAnalysis queryAnalysis;
	private @Mock DnfTransformer dnfTransformer;
	private @Mock ClauseAnalysis clauseAnalysis;
	
	// QueryAnalysis instance that is managed by Spring (has ClauseAnalyzer injected)
	@Autowired private QueryAnalysis springManagedQueryAnalysis;
	
	@Before
	public void setup() {
		initMocks(this);
		queryAnalysis = new QueryAnalysis();
		queryAnalysis.setDnfTransformer(dnfTransformer);
		queryAnalysis.setClauseAnalysis(clauseAnalysis);
	}
	
	// extract the annotations from one clause
	@SuppressWarnings("unchecked")
	@Test
	public void analyzeQuery() {
		// statement that should be analyzed
		final Start statement = new Start();
		
		// contains two clauses
		final ArrayList<PExpr> clauses = new ArrayList<PExpr>();
		final APathExpr clause1 = newPathExpr();
		final APathExpr clause2 = newPathExpr();
		clauses.add(clause1);
		clauses.add(clause2);
		when(dnfTransformer.listClauses(statement)).thenReturn(clauses);
		
		// maximal column width of clauses
		final Integer columnWidth = new Integer(5);
		when(clauseAnalysis.nodesCount()).thenReturn(columnWidth);
		
		// nodes for each clause
		QueryNode node1 = mock(QueryNode.class);
		QueryNode node2 = mock(QueryNode.class);
		List<QueryNode> nodes1 = Arrays.asList(node1);
		List<QueryNode> nodes2 = Arrays.asList(node2);
		when(clauseAnalysis.getNodes()).thenReturn(nodes1, nodes2);
		
		// meta annotations
		QueryAnnotation annotation1 = mock(QueryAnnotation.class);
		QueryAnnotation annotation2 = mock(QueryAnnotation.class);
		List<QueryAnnotation> annotations1 = Arrays.asList(annotation1);
		List<QueryAnnotation> annotations2 = Arrays.asList(annotation2);
		when(clauseAnalysis.getMetaAnnotations()).thenReturn(annotations1, annotations2);
		
		// finally the corpus list on which this query should be evaluated
		List<Long> corpusList = mock(List.class);
		
		// analyze the entire query
		QueryData queryData = queryAnalysis.analyzeQuery(statement, corpusList);
		
		// query data has copy of corpus list
		assertThat(queryData.getCorpusList(), is(corpusList));
		assertThat(queryData.getAlternatives(), is(Arrays.asList(nodes1, nodes2)));
		assertThat(queryData.getMaxWidth(), is(columnWidth));
		assertThat(queryData.getMetaData(), is(Arrays.asList(annotation1, annotation2)));
		
		// verify flow control
		InOrder inOrder = inOrder(dnfTransformer, clauseAnalysis);
		
		// first statement is normalized
		inOrder.verify(dnfTransformer).caseStart(statement);
		inOrder.verify(dnfTransformer).listClauses(statement);
		
		// then each clause is analyzed independently
		inOrder.verify(clauseAnalysis).caseAPathExpr(clause1);
		inOrder.verify(clauseAnalysis).nodesCount();
		inOrder.verify(clauseAnalysis).caseAPathExpr(clause2);
		inOrder.verify(clauseAnalysis).nodesCount();
	}
	
	// Spring managed instance has its dependencies set
	@Test
	public void springManagedInstanceHasAllDependencies() {
		assertThat(springManagedQueryAnalysis.getClauseAnalysis(), is(not(nullValue())));
		assertThat(springManagedQueryAnalysis.getDnfTransformer(), is(not(nullValue())));
	}
	
	// getClauseAnalysis() returns new instance on each call
	@Test
	public void springManagedInstanceIsThreadSafe() {
		ClauseAnalysis clauseAnalysis1 = springManagedQueryAnalysis.getClauseAnalysis();
		ClauseAnalysis clauseAnalysis2 = springManagedQueryAnalysis.getClauseAnalysis();
		assertThat(clauseAnalysis1, is(not(sameInstance(clauseAnalysis2))));
	}
	
}
