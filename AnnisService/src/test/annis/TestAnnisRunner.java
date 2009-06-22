package annis;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import annis.ql.parser.AnnisParser;
import de.deutschdiachrondigital.dddquery.DddQueryMapper;
import de.deutschdiachrondigital.dddquery.DddQueryRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"AnnisRunner-context.xml"})
public class TestAnnisRunner extends AnnisHomeTest {

	// some constants for stubbing and testing
	final String ANNIS_QUERY = "ANNIS_QUERY";
	final String DDD_QUERY = "DDD_QUERY";
	
	// object under test with a mock DddQueryMapper and DddQueryRunner
	private AnnisRunner annisRunner;
	@Mock private DddQueryRunner dddQueryRunner;
	@Mock private DddQueryMapper dddQueryMapper;
	@Mock private PrintStream out;
	
	// AnnisRunner that is managed by Spring
	@Autowired private AnnisRunner springManagedAnnisRunner;
		
	@Before
	public void setup() {
		// setup test object and dependencies
		annisRunner = new AnnisRunner();
		initMocks(this);
		annisRunner.setDddQueryRunner(dddQueryRunner);
		annisRunner.setDddQueryMapper(dddQueryMapper);
		annisRunner.setOut(out);

		// stub the DddQueryMapper
		when(dddQueryMapper.translate(ANNIS_QUERY)).thenReturn(DDD_QUERY);
	}

	// Spring managed instance has its dependencies set
	@Test
	public void springManagedInstanceHasAllDependencies() {
		assertThat(springManagedAnnisRunner.getDddQueryRunner(), is(not(nullValue())));
		assertThat(springManagedAnnisRunner.getAnnisParser(), is(not(nullValue())));
		assertThat(springManagedAnnisRunner.getDddQueryMapper(), is(not(nullValue())));
	}
	
	// doDddQuery translates query and prints result
	@Test
	public void doDddQuery() {
		annisRunner.doDddquery(ANNIS_QUERY);
		verify(dddQueryMapper).translate(ANNIS_QUERY);
		verify(out).println(DDD_QUERY);
	}
	
	// doParseInternal translates and delegates to DddQueryRunner.doParse()
	@Test
	public void doParseInternal() {
		annisRunner.doParseInternal(ANNIS_QUERY);
		verify(dddQueryMapper).translate(ANNIS_QUERY);
		verify(dddQueryRunner).doParse(DDD_QUERY);
	}
	
	// doParse creates a tree dump and prints
	@Test
	public void doParse() {
		// setup mock AnnisParser and a mock syntax tree
		AnnisParser annisParser = mock(AnnisParser.class);
		annisRunner.setAnnisParser(annisParser);

		final String SYNTAX_TREE = "TREE";
		when(annisParser.dumpTree(ANNIS_QUERY)).thenReturn(SYNTAX_TREE);

		// test
		annisRunner.doParse(ANNIS_QUERY);
		verify(annisParser).dumpTree(ANNIS_QUERY);
		verify(out).println(SYNTAX_TREE);
	}
	
	// doSql translates and delegates to DddQueryRunner.doSql()
	@Test
	public void doSql() {
		annisRunner.doSql(ANNIS_QUERY);
		verify(dddQueryMapper).translate(ANNIS_QUERY);
		verify(dddQueryRunner).doSql(DDD_QUERY);
	}
	
	// doFind translates and delegates to DddQueryRunner.doFind()
	@Test
	public void doFind() {
		annisRunner.doFind(ANNIS_QUERY);
		verify(dddQueryMapper).translate(ANNIS_QUERY);
		verify(dddQueryRunner).doFind(DDD_QUERY);
	}
	
	// doCount translates and delegates to DddQueryRunner.doCount()
	@Test
	public void doCount() {
		annisRunner.doCount(ANNIS_QUERY);
		verify(dddQueryMapper).translate(ANNIS_QUERY);
		verify(dddQueryRunner).doCount(DDD_QUERY);
	}
	
	// doPlan translates and delegates to DddQueryRunner.doPlan()
	@Test
	public void doPlan() {
		annisRunner.doPlan(ANNIS_QUERY);
		verify(dddQueryMapper).translate(ANNIS_QUERY);
		verify(dddQueryRunner).doPlan(DDD_QUERY);
	}
	
	// doAnalyze translates and delegates to DddQueryRunner.doAnalyze()
	@Test
	public void doAnalyze() {
		annisRunner.doAnalyze(ANNIS_QUERY);
		verify(dddQueryMapper).translate(ANNIS_QUERY);
		verify(dddQueryRunner).doAnalyze(DDD_QUERY);
	}
	
	// doAnnotate translates and delegates to DddQueryRunner.doAnnotate()
	@Test
	public void doAnnotate() {
		annisRunner.doAnnotate(ANNIS_QUERY);
		verify(dddQueryMapper).translate(ANNIS_QUERY);
		verify(dddQueryRunner).doAnnotate(DDD_QUERY);
	}

	// doAnnotate2 translates and delegates to DddQueryRunner.doAnnotate2
	@Test
	public void doAnnotate2() {
		annisRunner.doAnnotate2(ANNIS_QUERY);
		verify(dddQueryMapper).translate(ANNIS_QUERY);
		verify(dddQueryRunner).doAnnotate2(DDD_QUERY);
	}
	
	// setCorpus translates the corpusList and delegates to DddQueryRunner.doSetCorpus()
	@Test
	public void doCorpus() {
		// stub dddQueryMapper.translateCorpusList
		final String CORPUS_LIST = "1 2 3";
		final List<Long> CORPORA = new ArrayList<Long>();
		CORPORA.add(1L);
		CORPORA.add(2L);
		CORPORA.add(3L);
		when(dddQueryMapper.translateCorpusList(CORPUS_LIST)).thenReturn(CORPORA);
		
		// test call
		annisRunner.doCorpus(CORPUS_LIST);
		
		// verify that corpus list is set in DddQueryRunner
		verify(dddQueryRunner).doCorpus(CORPORA);
	}
	
	// doWeka translates and delegates to DddQueryRunner.doWeka()
	@Test
	public void doWeka() {
		annisRunner.doWeka(ANNIS_QUERY);
		verify(dddQueryMapper).translate(ANNIS_QUERY);
		verify(dddQueryRunner).doWeka(DDD_QUERY);
	}
	
	// doList delegates to DddQueryRunner.doList() {
	@Test
	public void doList() {
		annisRunner.doList(null);
		verify(dddQueryRunner).doList(null);
	}
	
	// doNodeAnnotations delegates to DddQueryRunner.doNodeAnnotations()
	@Test
	public void doNodeAnnotations() {
		final String VALUES = "VALUES";
		annisRunner.doNodeAnnotations(VALUES);
		verify(dddQueryRunner).doNodeAnnotations(VALUES);
	}
	
	// doDot translates and delegates to DddQueryRunner.doDot()
	@Test
	public void doDot() {
		annisRunner.doDot(ANNIS_QUERY);
		verify(dddQueryMapper).translate(ANNIS_QUERY);
		verify(dddQueryRunner).doDot(DDD_QUERY);
	}

	// doMeta translates and delegates to DddQueryRunner.doMeta()
	@Test
	public void doMeta() {
		final String ID = "ID";
		annisRunner.doMeta(ID);
		verify(dddQueryRunner).doMeta(ID);
	}
	
}
