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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.deutschdiachrondigital.dddquery.analysis.DepthFirstAdapter;
import de.deutschdiachrondigital.dddquery.node.AAlignmentSpec;
import de.deutschdiachrondigital.dddquery.node.AEndMarkerSpec;
import de.deutschdiachrondigital.dddquery.node.AExactSearchNodeTest;
import de.deutschdiachrondigital.dddquery.node.AFunctionExpr;
import de.deutschdiachrondigital.dddquery.node.ALayerAxis;
import de.deutschdiachrondigital.dddquery.node.AMarkerSpec;
import de.deutschdiachrondigital.dddquery.node.ANumberLiteralExpr;
import de.deutschdiachrondigital.dddquery.node.AStartMarkerSpec;
import de.deutschdiachrondigital.dddquery.node.AStep;
import de.deutschdiachrondigital.dddquery.node.AStringLiteralExpr;
import de.deutschdiachrondigital.dddquery.node.AVarrefNodeTest;
import de.deutschdiachrondigital.dddquery.node.Node;
import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.node.Token;

// FIXME: move to test
public class AstComparator extends DepthFirstAdapter {
	
	@SuppressWarnings("serial")
	public static class DifferentTreeException extends RuntimeException {
		
		public DifferentTreeException(String msg) {
			super(msg);
		}
		
		public DifferentTreeException(String msg, String expected, String actual) {
			super(msg + "; expected: <" + expected + ">; was: <" + actual + ">");
		}
		
	};

	private final List<Node> nodes;

	public AstComparator(Node expectedTree) {
		this.nodes = pushNodesToStack(expectedTree);
	}

	/*
	 * called for nodes that have no terminals
	 */
	@Override
	public void defaultIn(Node node) {
		popNode(node.getClass());
	}

	/*
	 * Wenn noch Knoten im Template-Baum nicht verarbeitet wurden, sind die Bäume nicht gleich.
	 */
	@Override
	public void outStart(Start node) {
		if (nodes.isEmpty())
			return;
		
		throw new DifferentTreeException("trees are different: next node in template tree: " + nodes.get(0).getClass());
	}
	
	@Override
	public void inAMarkerSpec(AMarkerSpec node) {
		checkTerminal(node, "marker");
	}
	
	@Override
	public void inAStartMarkerSpec(AStartMarkerSpec node) {
		checkTerminal(node, "marker");
	}
	
	@Override
	public void inAEndMarkerSpec(AEndMarkerSpec node) {
		checkTerminal(node, "marker");
	}
	
	@Override
	public void inAStep(AStep node) {
		checkTerminal(node, "variable");
	}
	
	@Override
	public void inALayerAxis(ALayerAxis node) {
		checkTerminal(node, "name");
	}
	
	@Override
	public void inAAlignmentSpec(AAlignmentSpec node) {
		String terminals[] = { "role1", "role2", "greed1", "greed2" };
		checkTerminals(node, terminals);
	}
	
	@Override
	public void inANumberLiteralExpr(ANumberLiteralExpr node) {
		checkTerminal(node, "number");
	}
	
	@Override
	public void inAVarrefNodeTest(AVarrefNodeTest node) {
		checkTerminal(node, "variable");
	}
	
	@Override
	public void inAFunctionExpr(AFunctionExpr node) {
		checkTerminal(node, "name");
	}
	
	@Override
	public void inAStringLiteralExpr(AStringLiteralExpr node) {
		checkTerminal(node, "string");
	}
	
	@Override
	public void inAExactSearchNodeTest(AExactSearchNodeTest node) {
		checkTerminal(node, "pattern");
	}
	
	/*
	 * Pusht alle Knoten des Template-Baumes auf einen Stack.  Der Template-Baum wird depth-first
	 * durchwandert.
	 */
	private List<Node> pushNodesToStack(Node root) {
		final List<Node> nodes = new ArrayList<Node>();
		root.apply(new DepthFirstAdapter() {

			@Override
			public void defaultIn(Node node) {
				nodes.add(node);
			}

		});
		return nodes;
	}

	/*
	 * Popt einen Template-Knoten vom Stack und überprüft gleichzeitig, ob er von einem bestimmten
	 * Typ ist.
	 */
	private Node popNode(@SuppressWarnings("unchecked") Class nodeClass) {
		if (nodes.isEmpty())
			throw new DifferentTreeException("trees are different: not enough nodes in template");
		
		Node node = nodes.remove(0);

		if (nodeClass.isInstance(node))
			return node;
			
		throw new DifferentTreeException("wrong node type", node.getClass().getName(), nodeClass.getName());
	}
	
	/**
	 * Überprüft die Terminals zweier Knoten auf Gleichheit.
	 * 
	 * Die Namen der Terminals werden wie in der SableCC-Steuerdatei angegeben.
	 */
	private void checkTerminals(Node node, String[] terminals) {
		
		Node other = popNode(node.getClass());
		
		for (String terminal : terminals) {
			try {
				String methodName = terminalAccessorMethodName(terminal);
				
				Method actualTerminalAccessor = node.getClass().getMethod(methodName, new Class[] { });
				Method expectedTerminalAccessor = other.getClass().getMethod(methodName, new Class[] { });

				Token actualToken = (Token) actualTerminalAccessor.invoke(node, new Object[] { });
				Token expectedToken = (Token) expectedTerminalAccessor.invoke(other, new Object[] { });

				if (actualToken == null && expectedToken == null)
					continue;
				
				if (actualToken == null)
					throw new DifferentTreeException("wrong terminal for " + terminal, expectedToken.getText(), null);
				
				if (expectedToken == null)
					throw new DifferentTreeException("wrong terminal for " + terminal, null, actualToken.getText());
				
				String actual = actualToken.getText();
				String expected = expectedToken.getText();

				if ( ! expected.equals(actual) )
					throw new DifferentTreeException("wrong terminal for " + terminal, expected, actual);

			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				throw new DifferentTreeException("BUG: unknown terminal: " + terminal);
			} catch (SecurityException e) {
				e.printStackTrace();
				throw new RuntimeException("BUG!", e);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				throw new RuntimeException("BUG!", e);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new RuntimeException("BUG!", e);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				throw new RuntimeException("BUG!", e);
			}
		}
	}
	
	/**
	 * Convenience-Methode, wenn nur ein Terminal überprüft wird.
	 */
	private void checkTerminal(Node node, String terminal) {
		checkTerminals(node, new String[] { terminal } );
	}
	
	/**
	 * Erzeugt den Namen der Getter-Methode aus dem Namen eines Terminal.
	 * 
	 * <p>
	 * Bsp: In der SableCC-Steuerdatei steht folgende Produktion:
	 * 
	 * <pre>
	 * Productions
	 * 		...
	 * 		expr = [left_hand_side]:id and [right_hand_side]:id ;
	 * </pre>
	 * 
	 * Dann hat die Klasse AExpr zwei Methoden:
	 * <ul>
	 * <li>getLeftHandSide() = terminalAccessorMethodName("left_hand_side");
	 * <li>getRightHandSide() = terminalAccessorMethodName("right_hand_side");
	 * </ul>
	 */
	public String terminalAccessorMethodName(String terminal) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("get");
		
		int i = 0;
		int last = 0;
		
		while (true) {
			i = terminal.indexOf("_", last);
			
			if (i == -1)
				break;
			
			sb.append(terminal.substring(last, last + 1).toUpperCase());
			sb.append(terminal.substring(last + 1, i));
			
			last = i + 1;
			
		}
		
		if (last < terminal.length()) {
			sb.append(terminal.substring(last, last + 1).toUpperCase());
			sb.append(terminal.substring(last + 1));
		}
			
		return sb.toString();
	}
	
}
