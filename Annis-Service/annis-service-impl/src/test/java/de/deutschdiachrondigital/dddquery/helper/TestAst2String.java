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
package de.deutschdiachrondigital.dddquery.helper;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;

import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.parser.DddQueryParser;

@Ignore
public class TestAst2String {

	@Ignore
	public void test() {
		DddQueryParser parser = new BeanFactory().getDddQueryParser();
		String[] inputs = {
				"/a",
				"//b/@c",
				"a",
				"a = b",
				"a[@b = 'c']",
				"a[b = 'c'][d = 1]",
				"a#",
				"a#(m)",
				"a$v",
				"a#(m)[pred][pred2]$v",
				"a | b",
				"a & b"
		};
		for (String input : inputs) {
			System.out.println(input);
			assertThat(string(parser.parse(input)), is(input));
		}
	}
	
	private String string(Start start) {
		Ast2String ast2String = new Ast2String();
		start.apply(ast2String);
		return ast2String.getResult();
	}
	
}
