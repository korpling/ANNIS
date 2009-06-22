/**
 * 
 */
package annis.dao;

import org.apache.log4j.Logger;


public class MultipleTextsFilter implements MatchFilter {

	private Logger log = Logger.getLogger(this.getClass());
	
	public void init() {
		// no init necessary, since each match is individually filtered
	}
	
	public boolean filterMatch(Match match) {
		// every match has at least one node
		int textRef = match.get(0).getTextRef();
		
		// check all nodes and return true if a node is found with a different text ref
		for (Span node : match)
			if (textRef != node.getTextRef()) {
				log.debug("multiple texts referenced in match, expected <" + textRef + ">, was <" + node.getTextRef() + ">");
				return true;
			}
	
		// all text refs are the same
		return false;
	}
	
}