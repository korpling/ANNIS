package annis.ql.parser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import annis.ql.analysis.DepthFirstAdapter;
import annis.ql.node.AAnnotationSearchExpr;
import annis.ql.node.AEdgeDominanceSpec;
import annis.ql.node.ALinguisticConstraintExpr;
import annis.ql.node.ARangeSpec;
import annis.ql.node.ARegexpTextSpec;
import annis.ql.node.AWildTextSpec;
import annis.ql.node.Node;
import annis.ql.node.Start;
import annis.ql.node.Token;


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
	 * Wenn noch Knoten im Template-Baum nicht verarbeitet wurden, sind die B�ume nicht gleich.
	 */
	@Override
	public void outStart(Start node) {
		if (nodes.isEmpty())
			return;
		
		throw new DifferentTreeException("trees are different: next node in template tree: " + nodes.get(0).getClass());
	}
	
	@Override
	public void inAAnnotationSearchExpr(AAnnotationSearchExpr node) {
		String[] terminals = { "anno_type" };
		checkTerminals(node, terminals);

	}
	
	@Override
	public void inAWildTextSpec(AWildTextSpec node) {
		checkTerminal(node, "text");
	}
	
	@Override
	public void inARegexpTextSpec(ARegexpTextSpec node) {
		checkTerminal(node, "regexp");
	}
	
	@Override
	public void inALinguisticConstraintExpr(ALinguisticConstraintExpr node) {
		String[] terminals = { "lhs", "rhs" };
		checkTerminals(node, terminals);
	}
	
	@Override
	public void inARangeSpec(ARangeSpec node) {
		String[] terminals = { "min", "max" };
		checkTerminals(node, terminals);
	}
	
	@Override
	public void inAEdgeDominanceSpec(AEdgeDominanceSpec node) {
		checkTerminal(node, "edge");
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

	// Pop a template node from the stack and check if it's of a certain type.
	private Node popNode(Class<?> nodeClass) {
		if (nodes.isEmpty())
			throw new DifferentTreeException("trees are different: not enough nodes in template");
		
		Node node = nodes.remove(0);

		if (nodeClass.isInstance(node))
			return node;
			
		throw new DifferentTreeException("wrong node type", node.getClass().getName(), nodeClass.getName());
	}
	
	/**
	 * �berpr�ft die Terminals zweier Knoten auf Gleichheit.
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
	 * Convenience-Methode, wenn nur ein Terminal �berpr�ft wird.
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
