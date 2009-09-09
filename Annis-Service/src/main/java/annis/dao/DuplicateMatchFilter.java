/**
 * 
 */
package annis.dao;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;


public class DuplicateMatchFilter implements MatchFilter {

	private Logger log = Logger.getLogger(this.getClass());
	
	private class Key {
		private long textRef;
		private long tokenLeft;
		private long tokenRight;
		
		public Key(Match match) {
			textRef = match.get(0).getTextRef();
			tokenLeft = Integer.MAX_VALUE;
			tokenRight = Integer.MIN_VALUE;
			for (Span node : match) {
				tokenLeft = Math.min(tokenLeft, node.getTokenLeft());
				tokenRight = Math.max(tokenRight, node.getTokenRight());
			}
		}
		
		@Override
		public boolean equals(Object obj) {
			if ( ! (obj instanceof Key) )
				return false;
			
			Key k = (Key) obj;
		
			return new EqualsBuilder()
				.append(this.textRef, k.textRef)
				.append(this.tokenRight, k.tokenRight)
				.append(this.tokenLeft, k.tokenLeft)
				.isEquals();
		}
		
		@Override
		public int hashCode() {
			return new HashCodeBuilder()
				.append(textRef)
				.append(tokenLeft)
				.append(tokenRight)
				.toHashCode();
		}
	}
	
	private Set<Key> seen = new HashSet<Key>();
	
	public void init() {
		seen.clear();
	}
	
	public boolean filterMatch(Match match) {
		Key key = new Key(match);
		
		if (seen.contains(key)) {
			log.debug("already seen match that references the tokens " + key.tokenRight + " to " + key.tokenLeft + " of text " + key.textRef);
			return true;
		}
		
		seen.add(key);
		return false;
	}
	
}