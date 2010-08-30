package de.deutschdiachrondigital.dddquery;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static test.IsCollectionEmpty.empty;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import annis.AnnisHomeTest;
import annis.AnnotationGraphDotExporter;
import annis.TableFormatter;
import annis.WekaDaoHelper;
import annis.dao.AnnisDao;
import annis.dao.AnnotationGraphDaoHelper;
import annis.dao.Match;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.service.ifaces.AnnisAttribute;
import annis.service.ifaces.AnnisCorpus;
import annis.sqlgen.ListCorpusSqlHelper;
import annis.sqlgen.ListNodeAnnotationsSqlHelper;

// FIXME: calls to TableFormatter.formatAsTable(List, String...) should only be stubbed once
// see http://code.google.com/p/mockito/issues/detail?id=62
// as of 8/4/2009 fixed in Mockito trunk
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"DddQueryRunner-context.xml"})
public class TestDddQueryRunner extends AnnisHomeTest {

	// object under test
	private DddQueryRunner dddQueryRunner;
	
	// mockup dependencies
	@Mock private PrintStream out;
	@Mock private AnnisDao annisDao;
	@Mock private AnnotationGraphDaoHelper annotationGraphHelper;
	@Mock private WekaDaoHelper wekaDaoHelper;
	@Mock private ListCorpusSqlHelper listCorpusHelper;
	@Mock private ListNodeAnnotationsSqlHelper listNodeAnnotationsSqlHelper;
	@Mock private TableFormatter tableFormatter;
	@Mock private AnnotationGraphDotExporter annotationGraphDotExporter;

	// some constants and mocks for stubbing and testing
	private static final String DDDQUERY = "DDDQUERY";
	private static final int MATCH_LIMIT = 3;
	private static final List<Long> CORPUS_LIST = new ArrayList<Long>();
	@Mock private List<Match> MATCHES;
	private String TABLE = "TABLE";
	
	// DddQueryRunner that is managed by Spring
	@Autowired private DddQueryRunner springManagedDddQueryRunner;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		initMocks(this);
		
		// dependencies
		dddQueryRunner = new DddQueryRunner();
		dddQueryRunner.setOut(out);
		dddQueryRunner.setAnnisDao(annisDao);
		dddQueryRunner.setAnnotationGraphDaoHelper(annotationGraphHelper);
		dddQueryRunner.setWekaDaoHelper(wekaDaoHelper);
		dddQueryRunner.setListCorpusHelper(listCorpusHelper);
		dddQueryRunner.setListNodeAnnotationsSqlHelper(listNodeAnnotationsSqlHelper);
		dddQueryRunner.setTableFormatter(tableFormatter);
		dddQueryRunner.setAnnotationGraphDotExporter(annotationGraphDotExporter);
		
		// settings
		dddQueryRunner.setMatchLimit(MATCH_LIMIT);

		// stub DAO to retrieve matches
		when(annisDao.findMatches(anyList(), eq(DDDQUERY))).thenReturn(MATCHES);
		
		// stub table formatter
		when(tableFormatter.formatAsTable(anyList())).thenReturn(TABLE);
	}
	
	// Spring managed instance has its dependencies set
	@Test
	public void springManagedInstanceHasAllDependencies() {
		assertThat(springManagedDddQueryRunner.getAnnisDao(), is(not(nullValue())));
		assertThat(springManagedDddQueryRunner.getDddQueryParser(), is(not(nullValue())));
		assertThat(springManagedDddQueryRunner.getFindSqlGenerator(), is(not(nullValue())));
		assertThat(springManagedDddQueryRunner.getAnnotationGraphDaoHelper(), is(not(nullValue())));
		assertThat(springManagedDddQueryRunner.getWekaDaoHelper(), is(not(nullValue())));
		assertThat(springManagedDddQueryRunner.getListCorpusHelper(), is(not(nullValue())));
		assertThat(springManagedDddQueryRunner.getListNodeAnnotationsSqlHelper(), is(not(nullValue())));
		assertThat(springManagedDddQueryRunner.getTableFormatter(), is(not(nullValue())));
		assertThat(springManagedDddQueryRunner.getAnnotationGraphDotExporter(), is(not(nullValue())));

		assertThat(springManagedDddQueryRunner.getMatchLimit(), is(100));	// default value
	}
	
	// fresh instances have an empty corpusList
	@Test
	public void freshCorpusList() {
		assertThat(dddQueryRunner.getCorpusList(), is(not(nullValue())));
		assertThat(dddQueryRunner.getCorpusList(), is(empty()));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void doCount() {
		final int COUNT = 23;
		when(annisDao.countMatches(anyList(), anyString())).thenReturn(COUNT);
		
		dddQueryRunner.doCount(DDDQUERY);
		
		verify(annisDao).countMatches(CORPUS_LIST, DDDQUERY);
		verify(out).println(COUNT);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void doPlanCount() {
		final String PLAN = "PLAN";
		when(annisDao.planCount(eq(DDDQUERY), anyList(), anyBoolean())).thenReturn(PLAN);
		
		dddQueryRunner.doPlanCount(DDDQUERY);
		
		verify(annisDao).planCount(DDDQUERY, CORPUS_LIST, false);
		verify(out).println(PLAN);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void doAnalyzeCount() {
		final String PLAN = "PLAN";
		when(annisDao.planCount(eq(DDDQUERY), anyList(), anyBoolean())).thenReturn(PLAN);
		
		dddQueryRunner.doAnalyzeCount(DDDQUERY);
		
		verify(annisDao).planCount(DDDQUERY, CORPUS_LIST, true);
		verify(out).println(PLAN);
	}
	
	@Test
	public void doAnnotate() {
		// stub context for DddQueryRunner
		final int CONTEXT = 2;
		dddQueryRunner.setContext(CONTEXT);
		
		// call and test
		dddQueryRunner.doAnnotate(DDDQUERY);
		verify(annisDao).retrieveAnnotationGraph(CORPUS_LIST, DDDQUERY, 0, MATCH_LIMIT, CONTEXT, CONTEXT);
	}
	
	@Test
	public void doCorpus() {
		final List<Long> CORPORA = Arrays.asList(1L, 2L, 3L);
		dddQueryRunner.doCorpus(CORPORA);
		assertThat(dddQueryRunner.getCorpusList(), is(CORPORA));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void doList() {
		// stub DAO to return a corpus list
		final List<AnnisCorpus> CORPORA = mock(List.class);
		when(annisDao.listCorpora()).thenReturn(CORPORA);

		// stub TableFormatter for this test (http://code.google.com/p/mockito/issues/detail?id=62)
		when(tableFormatter.formatAsTable(anyList(), anyString(), anyString(), anyString(), anyString())).thenReturn(TABLE);
		
		// call and test
		dddQueryRunner.doList(null);
		verify(annisDao).listCorpora();
		verify(tableFormatter).formatAsTable(CORPORA, "id", "name", "textCount", "tokenCount");
		verify(out).println(TABLE);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void doNodeAnnotations() {
		// stub DAO to return a list of node annotations
		final List<AnnisAttribute> ANNOTATIONS = mock(List.class);
		when(annisDao.listNodeAnnotations(anyList(), anyBoolean())).thenReturn(ANNOTATIONS);
		
		// stub TableFormatter for this test (http://code.google.com/p/mockito/issues/detail?id=62)
		when(tableFormatter.formatAsTable(anyList(), anyString(), anyString())).thenReturn(TABLE);
		
		// call and test
		dddQueryRunner.doNodeAnnotations(null);
		verify(annisDao).listNodeAnnotations(CORPUS_LIST, false);
		verify(tableFormatter).formatAsTable(ANNOTATIONS, "name", "distinctValues");
		verify(out).println(TABLE);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void doNodeAnnotationsWithValues() {
		// stub DAO to return a list of node annotations
		final List<AnnisAttribute> ANNOTATIONS = mock(List.class);
		when(annisDao.listNodeAnnotations(anyList(), anyBoolean())).thenReturn(ANNOTATIONS);
		
		// stub TableFormatter for this test (http://code.google.com/p/mockito/issues/detail?id=62)
		when(tableFormatter.formatAsTable(anyList(), anyString(), anyString())).thenReturn(TABLE);
		
		// call and test
		dddQueryRunner.doNodeAnnotations("values");
		verify(annisDao).listNodeAnnotations(CORPUS_LIST, true);
		verify(tableFormatter).formatAsTable(ANNOTATIONS, "name", "distinctValues");
		verify(out).println(TABLE);
	}
	
	@Test
	public void doMeta() {
		// stub a dummy list of corpus annotations
		List<Annotation> ANNOTATIONS = mock(List.class);
		when(annisDao.listCorpusAnnotations(anyLong())).thenReturn(ANNOTATIONS);
		
		// stub TableFormatter for this test (http://code.google.com/p/mockito/issues/detail?id=62)
		when(tableFormatter.formatAsTable(anyList(), anyString(), anyString(), anyString())).thenReturn(TABLE);
		
		// call and verify
		final long ID = 42L;
		dddQueryRunner.doMeta(String.valueOf(ID));
		verify(annisDao).listCorpusAnnotations(ID);
		verify(tableFormatter).formatAsTable(ANNOTATIONS, "namespace", "name", "value");
		verify(out).println(TABLE);
	}
}
