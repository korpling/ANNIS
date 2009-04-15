package de.deutschdiachrondigital.dddquery.helper;

import java.io.PrintWriter;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.StringWriter;

import de.deutschdiachrondigital.dddquery.lexer.Lexer;
import de.deutschdiachrondigital.dddquery.node.Node;
import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.parser.Parser;
import de.deutschdiachrondigital.dddquery.sql.preprocessors.SemanticAnalysis;

public class Helper {

	public static Start parse(String input) {
		try {
			Start start = new Parser(new Lexer(new PushbackReader(new StringReader(input)))).parse();
			start.apply(new SemanticAnalysis());
			return start;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String dumpTree(Node node) {
		return dumpTree(node, false);
	}

	public static String dumpTree(Node node, boolean filter) {
		StringWriter writer = new StringWriter();
		TreeDumper dumper = new TreeDumper(new PrintWriter(writer)); 
		node.apply(dumper);
		String result = writer.toString();
		System.out.println(result);
		return result;
	}
	
}
