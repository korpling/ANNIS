package annis.sqlgen;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static test.IsCollectionEmpty.empty;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.ql.parser.QueryAnalysis;
import annis.ql.parser.QueryData;
import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.parser.DddQueryParser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"SqlGenerator-context.xml", 
		"classpath:de/deutschdiachrondigital/dddquery/parser/DddQueryParser-context.xml"})
public class TestSqlGenerator {

	// simple SqlGenerator instance with mocked dependencies
	private SqlGenerator sqlGenerator;
	private @Mock ClauseSqlGenerator clauseSqlGenerator;
	private @Mock QueryAnalysis queryAnalysis;
	
	// SqlGenerator that is managed by Spring
	@Autowired private SqlGenerator springManagedSqlGenerator;
	@Autowired private DddQueryParser parser;

	@Before
	public void setup() {
		initMocks(this);
		sqlGenerator = new SqlGenerator();
		sqlGenerator.setClauseSqlGenerator(clauseSqlGenerator);
		sqlGenerator.setQueryAnalysis(queryAnalysis);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void toSql() {
		// statement that should be transformed into SQL
		final Start statement = new Start();
		
		// is represented by the following data
		QueryData queryData = mock(QueryData.class);
		List<Long> corpusList = mock(List.class);
		when(queryData.getCorpusList()).thenReturn(corpusList);
		List<AnnisNode> nodes1 = mock(List.class);
		List<AnnisNode> nodes2 = mock(List.class);
		when(queryData.getAlternatives()).thenReturn(Arrays.asList(nodes1, nodes2));
		final Integer maxWidth = new Integer(5);
		when(queryData.getMaxWidth()).thenReturn(maxWidth);
		List<Annotation> metaData = mock(List.class);
		when(queryData.getMetaData()).thenReturn(metaData);
		
		// stub in the sample query data
		when(queryAnalysis.analyzeQuery(any(Start.class), anyList())).thenReturn(queryData);
		
		// stub SQL code of individual clauses
		final String sql1 = "SELECT 1";
		final String sql2 = "SELECT 2";
		sqlGenerator.setClauseSqlGenerator(clauseSqlGenerator);
		when(clauseSqlGenerator.toSql(anyList(), anyInt(), anyList(), anyList())).thenReturn(sql1, sql2);
		
		// convert statement to SQL
		String sql = sqlGenerator.toSql(statement, corpusList);
		
		// verify flow control
	
		// first the query is analyzed
		verify(queryAnalysis).analyzeQuery(statement, corpusList);
		
		// each clause (list of nodes) is transformed to SQL with correct width
		verify(clauseSqlGenerator).toSql(nodes1, maxWidth, corpusList, metaData);
		verify(clauseSqlGenerator).toSql(nodes2, maxWidth, corpusList, metaData);

		// check for correct SQL
		assertEquals(sql1 + "\n\nUNION " + sql2, sql);
		
	}
	
	// Spring managed instance has its dependencies set
	@Test
	public void springManagedInstanceHasAllDependencies() {
		assertThat(springManagedSqlGenerator.getClauseSqlGenerator(), is(not(nullValue())));
		assertThat(springManagedSqlGenerator.getQueryAnalysis(), is(not(nullValue())));
		ClauseSqlGenerator clauseSqlGenerator = springManagedSqlGenerator.getClauseSqlGenerator();
		assertThat(clauseSqlGenerator, is(not(nullValue())));
		assertThat(clauseSqlGenerator.getSelectClauseSqlGenerators(), is(not(empty())));
		assertThat(clauseSqlGenerator.getFromClauseSqlGenerators(), is(not(empty())));
		assertThat(clauseSqlGenerator.getWhereClauseSqlGenerators(), is(not(empty())));
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
		System.out.println(springManagedSqlGenerator.toSql(parser.parse((input)), null));
		System.out.println();
	}

}
