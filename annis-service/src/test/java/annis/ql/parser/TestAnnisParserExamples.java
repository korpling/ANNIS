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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static annis.test.TestHelper.springFiles;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import annis.test.SpringQueryExamples;
import annis.test.SpringSyntaxTreeExamples;
import annis.test.SyntaxTreeExample;
import annis.exceptions.AnnisException;
import annis.ql.AqlLexer;
import annis.ql.AqlParser;
import annis.ql.node.Start;
import java.util.LinkedList;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Ignore;
import org.junit.Test;

// see http://junit.sourceforge.net/doc/ReleaseNotes4.4.html
// and http://popper.tigris.org/tutorial.html
@RunWith(Theories.class)
public class TestAnnisParserExamples {
	
	// where to find query and syntax tree examples
	private static final String EXAMPLES = "annis/ql/parser/TestAnnisParser-examples.xml";

	// access to AnnisParser
	static ApplicationContext ctx;

	// simple AnnisParser instance
	private AnnisParser parser;
  private AnnisParserAntlr parserAntlr;
  
	// load Spring application context once
	@BeforeClass
	public static void loadApplicationContext() {
		final String[] ctxFiles = 
			springFiles(TestAnnisParserExamples.class, "AnnisParser-context.xml");
		ctx = new ClassPathXmlApplicationContext(ctxFiles);
	}
	
	// setup a fresh parser
	@Before
	public void setup() {
		parser = (AnnisParser) ctx.getBean("annisParser");
    parserAntlr = (AnnisParserAntlr) ctx.getBean("annisParserAntlr");
	}
	
	///// Syntax-Tree tests
	
	@Theory
	public void testSyntaxTrees(
			@SpringSyntaxTreeExamples(exampleMap = "exampleSyntaxTrees", contextLocation=EXAMPLES) 
			SyntaxTreeExample example) {
		Start syntaxTree = parser.parse(example.getQuery());
		assertThat(syntaxTree, is(not(nullValue())));
		String actual = parser.dumpTree(syntaxTree).trim();
                // replace the unix line ending from the SVN with the systems line ending
                // (the dump tree will use the system line ending and the examples were created under linux)
                String provided = example.getSyntaxTree().replaceAll("\n", System.getProperty("line.separator"));
		assertEquals("wrong syntax tree for: " + example.getQuery(),provided.trim(), actual.trim());
	}

	@Theory
	public void testGoodQueries(
			@SpringQueryExamples(exampleList = "good", contextLocation=EXAMPLES) 
			String annisQuery) {
		assertThat(parser.parse(annisQuery), is(not(nullValue())));
	}
	
	@Theory
	public void testBadQueries(
			@SpringQueryExamples(exampleList = "bad", contextLocation=EXAMPLES) 
			String annisQuery) {
		try {
			parser.parse(annisQuery);
			fail("bad query passed as good: " + annisQuery);
		} catch (AnnisException e) {
			// ok
		}
	}
  
  @Theory
  @Test
	public void testGoodQueriesAntLR(
			@SpringQueryExamples(exampleList = "good", contextLocation=EXAMPLES) 
			String annisQuery) 
  {
    
    try
    {
      QueryData result = parserAntlr.parse(annisQuery, new LinkedList<Long>());
      assertThat(result, is(not(nullValue())));
    }
    catch (Exception ex)
    {
      ex.printStackTrace(System.err);
      fail("good query throw exception: " + annisQuery);
    }
	}
	
	@Theory
  @Test
  @Ignore
	public void testBadQueriesAntLR(
			@SpringQueryExamples(exampleList = "bad", contextLocation=EXAMPLES) 
			String annisQuery) {
		
    try
    {
      parserAntlr.parse(annisQuery, new LinkedList<Long>());
      
			fail("bad query passed as good: " + annisQuery);
    }
    catch (Exception ex)
    {
      // ok
    }

	}
	
}
