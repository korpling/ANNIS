package de.deutschdiachrondigital.dddquery.sql.model;

import java.util.ArrayList;
import java.util.List;


// TODO: doc
public class Graph {
	
	private String input;
	private List<Path> alternatives;
	
	public Graph() {
		alternatives = new ArrayList<Path>();
	}
	
	public void addAlternative(Path alternative) {
		alternatives.add(alternative);
	}

	public List<Path> getAlternatives() {
		return alternatives;
	}
	
	// XXX: wirklich nötig?
	@Override
	public String toString() {
		if (alternatives.isEmpty())
			return "an empty query";
		
		StringBuffer sb = new StringBuffer();
		for (Path alternative : alternatives) {
			sb.append(alternative);
			sb.append(" | ");
		}
		sb.setLength(sb.length() - " | ".length());
		return sb.toString();
	}


	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

}
