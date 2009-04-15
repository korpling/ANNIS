package de.deutschdiachrondigital.dddquery.helper;

import java.io.PushbackReader;
import java.io.StringReader;

import de.deutschdiachrondigital.dddquery.analysis.DepthFirstAdapter;
import de.deutschdiachrondigital.dddquery.lexer.Lexer;
import de.deutschdiachrondigital.dddquery.node.Node;
import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.parser.Parser;
import de.deutschdiachrondigital.dddquery.sql.preprocessors.SemanticAnalysis;

public class NodeLookup extends DepthFirstAdapter {
	
	private Node result;
	private Class<? extends Node> clazz;
	
	public NodeLookup(String input, Class<? extends Node> clazz) {
		this.clazz = clazz;
		try {
			Start start = new Parser(new Lexer(new PushbackReader(new StringReader(input)))).parse();
			start.apply(new SemanticAnalysis());
			start.apply(this);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void defaultIn(Node node) {
		if (result == null && clazz.isInstance(node))
			result = node;
	}

	public Node getResult() {
		return result;
	}
	
	public static Node lookup(String string, Class<? extends Node> clazz) {
		return new NodeLookup(string, clazz).getResult();
	}	

}