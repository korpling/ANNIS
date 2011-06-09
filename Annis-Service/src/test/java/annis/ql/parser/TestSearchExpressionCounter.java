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
import static org.junit.Assert.assertThat;

import java.io.PrintWriter;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;

import annis.ql.lexer.Lexer;
import annis.ql.node.Node;
import annis.ql.node.Start;


public class TestSearchExpressionCounter {

	String input = "\"das\" & ( cat=/NP/ & #1 . #2 | node & #1 . #3 )";

	//	@Test
	public void foo() {
		Start start = parse(input);
		System.out.println("--- before normalization");
		dumpTree(start);
		start.apply(new SearchExpressionCounter());
		System.out.println("--- after normalization");
		dumpTree(start);
	}

	private Start parse(String input) {
		try {
			Parser parser = new Parser(new Lexer(new PushbackReader(new StringReader(input))));
			Start start = parser.parse();
			return start;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void dumpTree(Node node) {
		StringWriter writer = new StringWriter();
		TreeDumper dumper = new TreeDumper(new PrintWriter(writer)); 
		node.apply(dumper);
		System.out.println(writer.toString());
	}
	
	@Test
	public void countSearchExpressions() {
		Start start = parse(input);
		SearchExpressionCounter analysis = new SearchExpressionCounter();
		start.apply(analysis);
		
		int count = analysis.getCount();
		assertThat(count, is(3));

		for (int i = 0; i < count; ++i) {
			int pos = i + 1;
			Node node = analysis.getSearchExpression(pos);
			assertThat(analysis.getPosition(node), is(pos));
		}
	}
}
