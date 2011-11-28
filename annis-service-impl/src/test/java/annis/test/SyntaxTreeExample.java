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
package annis.test;

import org.junit.experimental.theories.PotentialAssignment;

public class SyntaxTreeExample extends PotentialAssignment {
	private String query;
	private String syntaxTree;

	@Override
	public Object getValue() throws CouldNotGenerateValueException {
		return this;
	}
	
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public String getSyntaxTree() {
		return syntaxTree;
	}
	public void setSyntaxTree(String syntaxTree) {
		this.syntaxTree = syntaxTree;
	}

	@Override
	public String getDescription() throws CouldNotGenerateValueException {
		return "query = " + query;
	}
}