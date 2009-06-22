package annis.ql.parser;

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

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.sqlgen.ClauseAnalysis;
import annis.sqlgen.DnfTransformer;
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
		AnnisNode node1 = mock(AnnisNode.class);
		AnnisNode node2 = mock(AnnisNode.class);
		List<AnnisNode> nodes1 = Arrays.asList(node1);
		List<AnnisNode> nodes2 = Arrays.asList(node2);
		when(clauseAnalysis.getNodes()).thenReturn(nodes1, nodes2);
		
		// meta annotations
		Annotation annotation1 = mock(Annotation.class);
		Annotation annotation2 = mock(Annotation.class);
		List<Annotation> annotations1 = Arrays.asList(annotation1);
		List<Annotation> annotations2 = Arrays.asList(annotation2);
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
