/**
 * 
 */
package annis.dao;

public interface MatchFilter {
	public void init();
	public boolean filterMatch(Match match);
}