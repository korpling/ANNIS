package annis.service.ifaces;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * This is the actual container for a single search result.
 * 
 * @author k.huetter
 *
 */
public interface AnnisResult extends JSONAble, Serializable {
	
	// PAULA-Unart representation
	public abstract String getPaula();

	// id of last token
	public abstract long getEndNodeId();

	// id of first token
	public abstract long getStartNodeId();

	// ordered list of tokens
	public abstract List<AnnisToken> getTokenList();
	
	// non-token annotation names
	public abstract Set<String> getAnnotationLevelSet();
	
	// token annotation names
	public abstract Set<String> getTokenAnnotationLevelSet();

	// get marker for node
	String getMarkerId(Long nodeId);

	// is there a node with marker markerID
	boolean hasMarker(String markerId);
	
}