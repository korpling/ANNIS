package annis.sqlgen;

import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newPathExpr;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import annis.dao.BaseCorpusSelectionStrategy;
import annis.model.Annotation;
import de.deutschdiachrondigital.dddquery.node.APathExpr;
import de.deutschdiachrondigital.dddquery.node.PExpr;
import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.parser.DddQueryParser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"SqlGenerator-context.xml", 
		"classpath:de/deutschdiachrondigital/dddquery/parser/DddQueryParser-context.xml"})
public class TestSqlGenerator {

	// simple SqlGenerator instance with mocked dependencies
	private SqlGenerator sqlGenerator;
	private @Mock DnfTransformer dnfTransformer;
	private @Mock ClauseAnalysis clauseAnalysis;
	private @Mock ClauseSqlAdapter clauseSqlAdapter;
	private @Mock BaseCorpusSelectionStrategy corpusSelectionStrategy;
	
	// SqlGenerator that is managed by Spring (has ClauseAnalyzer injected)
	@Autowired private SqlGenerator springManagedSqlGenerator;
	@Autowired private CoveredTokensSelectClauseSqlAdapter coveredTokensSelectClauseSqlAdapter;
	@Autowired private DddQueryParser parser;

	@Before
	public void setup() {
		initMocks(this);
		sqlGenerator = new SqlGenerator();
		sqlGenerator.setDisjunctiveNormalForm(dnfTransformer);
		sqlGenerator.setClauseAnalysis(clauseAnalysis);
		sqlGenerator.setClauseSqlAdapter(clauseSqlAdapter);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void toSql() {
		// statement that should be transformed into SQL
		final Start statement = new Start();
		
		// contains three clauses
		final ArrayList<PExpr> clauses = new ArrayList<PExpr>();
		final APathExpr clause1 = newPathExpr();
		final APathExpr clause2 = newPathExpr();
		final APathExpr clause3 = newPathExpr();
		clauses.add(clause1);
		clauses.add(clause2);
		clauses.add(clause3);
		when(dnfTransformer.listClauses(statement)).thenReturn(clauses);
		
		// maximal column width of clauses
		final Integer COLUMN_WIDTH = new Integer(5);
		when(clauseAnalysis.nodesCount()).thenReturn(COLUMN_WIDTH);
		
		// meta annotations
		List<Annotation> annotations1 = mock(List.class);
		List<Annotation> annotations2 = mock(List.class);
		List<Annotation> annotations3 = mock(List.class);
		when(clauseAnalysis.getMetaAnnotations()).thenReturn(annotations1, annotations2, annotations3);
		
		// stub SQL code of individual clauses
		final String sql1 = "SELECT 1";
		final String sql2 = "SELECT 2";
		final String sql3 = "SELECT 3";
		sqlGenerator.setClauseSqlAdapter(clauseSqlAdapter);
		when(clauseSqlAdapter.toSql(clauseAnalysis.getNodes(), COLUMN_WIDTH, corpusSelectionStrategy, null)).thenReturn(sql1, sql2, sql3);
		
		// convert statement to SQL
		String sql = sqlGenerator.toSql(statement, corpusSelectionStrategy, null);
		
		// verify flow control
		InOrder inOrder = inOrder(dnfTransformer, clauseAnalysis, clauseSqlAdapter);
		
		// first statement is normalized
		inOrder.verify(dnfTransformer).caseStart(statement);
		inOrder.verify(dnfTransformer).listClauses(statement);
		
		// then each clause is analyzed independently
		inOrder.verify(clauseAnalysis).caseAPathExpr(clause1);
		inOrder.verify(clauseAnalysis).nodesCount();
		inOrder.verify(clauseAnalysis).caseAPathExpr(clause2);
		inOrder.verify(clauseAnalysis).nodesCount();
		inOrder.verify(clauseAnalysis).caseAPathExpr(clause3);
		inOrder.verify(clauseAnalysis).nodesCount();
		
		// then each clause is transformed to SQL with correct width
		// XXX: 2nd parameter COLUMN_WIDTH + 5 should cause a failure, why does it pass???
		inOrder.verify(clauseSqlAdapter, times(3)).toSql(clauseAnalysis.getNodes(), COLUMN_WIDTH + 5, corpusSelectionStrategy, null);

		// check for correct SQL
		assertEquals(sql1 + "\n\nUNION " + sql2 + "\n\nUNION " + sql3, sql);
		
		// corpus selection knows about meta data
		verify(corpusSelectionStrategy).addMetaAnnotations(annotations1);
		verify(corpusSelectionStrategy).addMetaAnnotations(annotations2);
		verify(corpusSelectionStrategy).addMetaAnnotations(annotations3);
	}
	
	// TODO thread safety (clause analysis + node adapter)

	// Spring managed instance has its dependencies set
	@Test
	public void springManagedInstanceHasAllDependencies() {
		assertThat(springManagedSqlGenerator.getClauseSqlAdapter(), is(not(nullValue())));
		ClauseSqlAdapter clauseSqlGenerator = springManagedSqlGenerator.getClauseSqlAdapter();
		assertThat(clauseSqlGenerator.getNodeSqlAdapterFactory(), is(not(nullValue())));
	}
	
	// XXX: move me?
	@Test
	public void dump() {
//		dumpSql("/a#(n1)/b#(n2)");
//		dumpSql("element()#(n1)[. = \"N.*\"]");
		dumpSql("element()#(n2)[@attribute(tiger:pos) = r\"S\"]");
//		dumpSql("element()#(n2)[@attribute(tiger:pos)][@attribute(urml:lemma) = r\"boink\"]");
//		dumpSql("element()#(n1)[@attribute(tiger:pos)]/right-child::element()#(n2)[@attribute(urml:lemma)]");
	}

	private void dumpSql(String input) {
		System.out.println("-- " + input);
		System.out.println(springManagedSqlGenerator.toSql(parser.parse((input)), corpusSelectionStrategy, coveredTokensSelectClauseSqlAdapter));
		System.out.println();
	}

}
