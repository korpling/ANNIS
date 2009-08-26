package annis.ql.parser;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static test.CustomMatcher.hasInstance;
import static test.IsCollectionEmpty.empty;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import annis.ql.analysis.DepthFirstAdapter;
import annis.ql.lexer.LexerException;
import annis.ql.node.Start;
import annis.ql.parser.AnnisParser.InternalParser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"AnnisParser-context.xml"})
public class TestAnnisParser implements ApplicationContextAware {

	// for springManagedIsSingleton()
	private ApplicationContext ctx;

	// simple DddQueryParser instance
	private AnnisParser parser;

	// DddQueryParser that is managed by Spring (has post-processors injected)
	@Autowired private AnnisParser springManagedParser;
	
	@Before
	public void setup() {
		parser = new AnnisParser();
	}
	
	@Test
	public void postProcessors() {
		assertCorrectPostProcessorList(parser);
	}
	
	@Test
	public void springManagedPostProcessors() {
		assertCorrectPostProcessorList(springManagedParser);
	}

	private void assertCorrectPostProcessorList(AnnisParser parser) {
		List<DepthFirstAdapter> postProcessors = parser.getPostProcessors();
		assertThat(postProcessors, hasInstance(NodeSearchNormalizer.class));
		assertThat(postProcessors, hasInstance(TokenSearchNormalizer.class));
		assertThat(postProcessors, hasInstance(QueryValidator.class));
	}
	
	@Test
	public void parseAppliesPostProcessors() throws ParserException, LexerException, IOException {
		// some constants
		final String ANNIS_QUERY = "Annis QL query";
		final Start SYNTAX_TREE = new Start();

		// stub the internal SableCC parser
		InternalParser internalParser = mock(InternalParser.class);
		when(internalParser.parse(ANNIS_QUERY)).thenReturn(SYNTAX_TREE);
		parser.setInternalParser(internalParser);

		// setup a couple of post-processors
		DepthFirstAdapter postProcessor1 = mock(DepthFirstAdapter.class);
		DepthFirstAdapter postProcessor2 = mock(DepthFirstAdapter.class);
		parser.setPostProcessors(Arrays.asList(postProcessor1, postProcessor2));
		
		// parse the query
		parser.parse(ANNIS_QUERY);
		
		// assert that all post-processors are called in order
		InOrder inOrder = inOrder(postProcessor1, postProcessor2);
		inOrder.verify(postProcessor1).caseStart(SYNTAX_TREE);
		inOrder.verify(postProcessor2).caseStart(SYNTAX_TREE);
	}
	
	@Test
	public void springManagedIsThreadSafe() {
		assertThat("no post-processors supplied by Spring", 
				springManagedParser.getPostProcessors(), is(not(empty())));
		
		// multiple calls of getPostProcessors() return fresh instances
		DepthFirstAdapter processor1 = springManagedParser.getPostProcessors().get(0);
		DepthFirstAdapter processor2 = springManagedParser.getPostProcessors().get(0);
		assertThat(processor1, is(not(sameInstance(processor2))));

		// multiple calls to getInternalParser() return fresh instances
		// this is not strictly necessary, but ensures that there always be
		// a fresh SableCC parser instance
		InternalParser internalParser1 = springManagedParser.getInternalParser();
		InternalParser internalParser2 = springManagedParser.getInternalParser();
		assertThat(internalParser1, is(not(sameInstance(internalParser2))));
	}
	
	@Test
	public void springManagedIsSingleton() {
		// DddQueryParser is configured as a singleton
		AnnisParser parser1 = (AnnisParser) ctx.getBean("annisParser");
		AnnisParser parser2 = (AnnisParser) ctx.getBean("annisParser");
		assertThat(parser1, is(sameInstance(parser2)));
	}
	
	@Ignore
	public void dump() {
		System.out.println(springManagedParser.dumpTree("tok & tok & #1 . #2"));
	}

	///// Getter / Setter
	
	public void setApplicationContext(ApplicationContext applicationContext)
	throws BeansException {
		ctx = applicationContext;
	}

}
