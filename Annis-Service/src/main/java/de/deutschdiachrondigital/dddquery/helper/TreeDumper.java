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

import java.io.PrintWriter;
import java.io.StringWriter;

import de.deutschdiachrondigital.dddquery.analysis.DepthFirstAdapter;
import de.deutschdiachrondigital.dddquery.node.EOF;
import de.deutschdiachrondigital.dddquery.node.Node;
import de.deutschdiachrondigital.dddquery.node.Token;


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
	
	public TreeDumper() { }
	
	public TreeDumper(PrintWriter out) {
		setOut(out);
	}

	public void setOut(PrintWriter out) {
		this.out = out;
	}
	
	public String dumpTree(Node node) {
		StringWriter result = new StringWriter();
		TreeDumper t = new TreeDumper(new PrintWriter(result));
		node.apply(t);
		return result.toString();
	}

	public void defaultCase(Node node) {
		if (node instanceof EOF)
			return;
		indent();
		out.println(((Token)node).getText());
	}

	public void defaultIn(Node node) {
		indent();
		out.println(nodeName(node));

		depth = depth+1;
	}

	public void defaultOut(Node node) {
		depth = depth-1;
		out.flush();
	}

	private String nodeName(Node node) {
		String fullName = node.getClass().getName();
		String name = fullName.substring(fullName.lastIndexOf('.')+1);
		return name;
	}

	private void indent() {
		for (int i = 0; i < depth; i++) out.write("   ");
	}

	
}
