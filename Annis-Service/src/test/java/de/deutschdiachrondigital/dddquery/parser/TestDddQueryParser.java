package de.deutschdiachrondigital.dddquery.parser;

import static de.deutschdiachrondigital.dddquery.helper.Helper.dumpTree;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static test.IsCollectionEmpty.empty;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.deutschdiachrondigital.dddquery.analysis.DepthFirstAdapter;
import de.deutschdiachrondigital.dddquery.lexer.Lexer;
import de.deutschdiachrondigital.dddquery.lexer.LexerException;
import de.deutschdiachrondigital.dddquery.node.EOF;
import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.node.Token;
import de.deutschdiachrondigital.dddquery.parser.DddQueryParser.InternalParser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"DddQueryParser-context.xml"})
public class TestDddQueryParser implements ApplicationContextAware {

	// for springManagedIsSingleton()
	private ApplicationContext ctx;

	// simple DddQueryParser instance
	private DddQueryParser parser;

	// DddQueryParser that is managed by Spring (has post-processors injected)
	@Autowired private DddQueryParser springManagedParser;
	
	@Before
	public void setup() {
		parser = new DddQueryParser();
	}
	
	@Test
	public void parseAppliesPostProcessors() throws ParserException, LexerException, IOException {
		// some constants
		final String DDD_QUERY = "a DDDquery";
		final Start SYNTAX_TREE = new Start();

		// stub the internal SableCC parser
		InternalParser internalParser = mock(InternalParser.class);
		when(internalParser.parse(DDD_QUERY)).thenReturn(SYNTAX_TREE);
		parser.setInternalParser(internalParser);

		// setup a couple of post-processors
		DepthFirstAdapter postProcessor1 = mock(DepthFirstAdapter.class);
		DepthFirstAdapter postProcessor2 = mock(DepthFirstAdapter.class);
		parser.setPostProcessors(Arrays.asList(postProcessor1, postProcessor2));
		
		// parse the query
		parser.parse(DDD_QUERY);
		
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
		InternalParser internalParser1 = springManagedParser.getInternalParser();
		InternalParser internalParser2 = springManagedParser.getInternalParser();
		assertThat(internalParser1, is(not(sameInstance(internalParser2))));
	}
	
	@Test
	public void springManagedIsSingleton() {
		// DddQueryParser is configured as a singleton
		DddQueryParser parser1 = (DddQueryParser) ctx.getBean("dddQueryParser");
		DddQueryParser parser2 = (DddQueryParser) ctx.getBean("dddQueryParser");
		assertThat(parser1, is(sameInstance(parser2)));
	}
	
	@Test
	public void dump() {
		dumpTree(springManagedParser.parse("a/b/c"));
	}
	
	@Test 
	public void lex() throws LexerException, IOException {
		Lexer l = new Lexer(new PushbackReader(new StringReader("a/b/c")));
		for (Token t = l.next(); true; t = l.next()) {
			System.out.println(t + "\t" + t.getClass());
			if (t instanceof EOF)
				break;
		}
	}

	///// Getter / Setter
	
	public void setApplicationContext(ApplicationContext applicationContext)
	throws BeansException {
		ctx = applicationContext;
	}
	
}
