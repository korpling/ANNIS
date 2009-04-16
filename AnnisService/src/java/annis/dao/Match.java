package annis.dao;

import java.util.ArrayList;
import java.util.List;

// FIXME: change to interface
// FIXME: change to Map<String, Node>
// key should be the marker of the step in the DDDquery
@SuppressWarnings("serial")
public class Match extends ArrayList<Span> {

	public Match() {
		
	}
	
	public Match(List<Span> nodes) {
		addAll(nodes);
	}
	
}
