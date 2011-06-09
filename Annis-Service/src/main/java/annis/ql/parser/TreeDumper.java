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

import java.io.PrintWriter;

import annis.ql.analysis.DepthFirstAdapter;
import annis.ql.node.Node;
import annis.ql.node.Token;

/**
 * Dump abstract syntax trees on a PrintWriter.
 * 
 * Adapted from TreeDumper.java by Nat Pryce: 
 * http://nat.truemesh.com/archives/000531.html
 * 
 * @author Viktor Rosenfeld <rosenfel@informatik.hu-berlin.de>
 */
public class TreeDumper extends DepthFirstAdapter {
	private int depth = 0;
	private PrintWriter out;

	public TreeDumper(PrintWriter out) {
		this.out = out;
	}

	public void defaultCase(Node node) {
		indent();
		out.println(((Token)node).getText());
	}

	public void defaultIn(Node node) {
		indent();
		printNodeName(node);
		out.println();

		depth = depth+1;
	}

	public void defaultOut(Node node) {
		depth = depth-1;
		out.flush();
	}

	private void printNodeName(Node node) {
		String fullName = node.getClass().getName();
		String name = fullName.substring(fullName.lastIndexOf('.')+1);

		out.print(name);
	}

	private void indent() {
		for (int i = 0; i < depth; i++) out.write("   ");
	}

}
