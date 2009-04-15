package de.deutschdiachrondigital.dddquery.sql;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class Match extends ArrayList<Node> {

	public Match() {
		
	}
	
	public Match(List<Node> nodes) {
		addAll(nodes);
	}
	
}
