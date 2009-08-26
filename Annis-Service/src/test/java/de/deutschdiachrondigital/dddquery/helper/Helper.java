package de.deutschdiachrondigital.dddquery.helper;

import java.io.PrintWriter;
import java.io.StringWriter;

import de.deutschdiachrondigital.dddquery.node.Node;

public class Helper {

	public static String dumpTree(Node node) {
		StringWriter writer = new StringWriter();
		node.apply(new TreeDumper(new PrintWriter(writer)));
		
		String result = writer.toString();
		System.out.println(result);
		
		return result;
	}

}
