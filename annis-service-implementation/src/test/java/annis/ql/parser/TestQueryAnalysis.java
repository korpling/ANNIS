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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import annis.model.QueryAnnotation;
import annis.model.QueryNode;
import annis.ql.node.AAndExpr;
import annis.ql.node.PExpr;
import annis.ql.node.Start;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:annis/ql/parser/AnnisParser-context.xml"})
public class TestQueryAnalysis {

	// simple QueryAnalysis instance with mocked dependencies
	@InjectMocks private QueryAnalysis queryAnalysis = new QueryAnalysis();
	@Mock private DnfTransformer dnfTransformer;
	@Mock private ClauseAnalysis clauseAnalysis;
	@Mock private NodeRelationNormalizer nodeRelationNormalizer;
	
	// QueryAnalysis instance that is managed by Spring (has Adapters injected)
	@Autowired private QueryAnalysis springManagedQueryAnalysis;
	
	// test data
	private Start statement = new Start();
	private ArrayList<Long> corpusList = new ArrayList<Long>();
	
	@Before
	public void setup() {
		initMocks(this);
	}

  @Test
  public void shouldDuplicateNodesInEdgeRelations()
  {
    // given
    queryAnalysis.setNormalizeNodesInEdgeRelations(true);
    AAndExpr clause1 = new AAndExpr();
    AAndExpr clause2 = new AAndExpr();
    setupStatement(statement, clause1, clause2);
    // when
    queryAnalysis.analyzeQuery(statement, corpusList);
    // then
    verify(nodeRelationNormalizer).caseAAndExpr(clause1);
    verify(nodeRelationNormalizer).caseAAndExpr(clause2);
  }

  @Test
  public void shouldNotDuplicateNodesInEdgeRelations()
  {
    // given
    queryAnalysis.setNormalizeNodesInEdgeRelations(false);
    AAndExpr clause1 = new AAndExpr();
    AAndExpr clause2 = new AAndExpr();
    setupStatement(statement, clause1, clause2);
    // when
    queryAnalysis.analyzeQuery(statement, corpusList);
    // then
    verifyZeroInteractions(nodeRelationNormalizer);
  }

  private void setupStatement(Start statement, PExpr... exprs)
  {
    ArrayList<PExpr> clauses = new ArrayList<PExpr>();
    for (PExpr expr: exprs)
    {
      clauses.add(expr);
    }
    given(dnfTransformer.listClauses(statement)).willReturn(clauses);
  }
	
	// extract the annotations from one clause
	@SuppressWarnings("unchecked")
	@Test
	public void analyzeQuery() {
		// statement that should be analyzed
		final Start statement = new Start();
		
		// contains two clauses
		final ArrayList<PExpr> clauses = new ArrayList<PExpr>();
		AAndExpr clause1 = new AAndExpr();
		clauses.add(clause1);
		AAndExpr clause2 = new AAndExpr();
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
		List<Long> corpusList = new ArrayList<Long>();
		
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
		inOrder.verify(clauseAnalysis).caseAAndExpr(clause1);
		inOrder.verify(clauseAnalysis).nodesCount();
		inOrder.verify(clauseAnalysis).caseAAndExpr(clause2);
		inOrder.verify(clauseAnalysis).nodesCount();
	}
	
	// Spring managed instance has its dependencies set
	@Test
	public void springManagedInstanceHasAllDependencies() {
		assertThat(springManagedQueryAnalysis.getClauseAnalysis(), is(not(nullValue())));
		assertThat(springManagedQueryAnalysis.getDnfTransformer(), is(not(nullValue())));
		assertThat(springManagedQueryAnalysis.getNodeRelationNormalizer(), is(not(nullValue())));
	}
	
	// getClauseAnalysis() returns new instance on each call
	@Test
	public void springManagedInstanceIsThreadSafe() {
    ClauseAnalysis clauseAnalysis1 = springManagedQueryAnalysis.getClauseAnalysis();
    ClauseAnalysis clauseAnalysis2 = springManagedQueryAnalysis.getClauseAnalysis();
    assertThat(clauseAnalysis1, is(not(sameInstance(clauseAnalysis2))));
    DnfTransformer dnfTransformer1 = springManagedQueryAnalysis.getDnfTransformer();
    DnfTransformer dnfTransformer2 = springManagedQueryAnalysis.getDnfTransformer();
    assertThat(dnfTransformer1, is(not(sameInstance(dnfTransformer2))));
    NodeRelationNormalizer nodeRelationNormalizer1 = springManagedQueryAnalysis.getNodeRelationNormalizer();
    NodeRelationNormalizer nodeRelationNormalizer2 = springManagedQueryAnalysis.getNodeRelationNormalizer();
    assertThat(nodeRelationNormalizer1, is(not(sameInstance(nodeRelationNormalizer2))));
	}
	
}
