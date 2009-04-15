package annisservice.ifaces;

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

	/**
	 * 
	 * @return The paula data of this result item.
	 */
	public abstract String getPaula();

	/**
	 * 
	 * @param paula The paula data of this result item.
	 */
	public abstract void setPaula(String paula);

	/**
	 * 
	 * @return The id of the first token node in this result including left context.
	 */
	public abstract long getStartNodeId();

	/**
	 * 
	 * @param startNodeId
	 */
	public abstract void setStartNodeId(long startNodeId);

	/**
	 * 
	 * @return The id of the last token node in this result including right context.
	 */
	public abstract long getEndNodeId();

	/**
	 * 
	 * @param endNodeId
	 */
	public abstract void setEndNodeId(long endNodeId);

	/**
	 * 
	 * @return Id of the corpus this result item belongs to.
	 */
	public abstract long getCorpusId();

	/**
	 *
	 * @param corpusId
	 */
	public abstract void setCorpusId(long corpusId);

	/**
	 * 
	 * @return Id of the text this result item belongs to.
	 */
	public abstract long getTextId();

	public abstract void setTextId(long textId);

	/**
	 * 
	 * @return An ordered list of all token in this result item including all context token.
	 */
	public abstract List<AnnisToken> getTokenList();
	
	/**
	 * 
	 * @return A Set of annotation levels available in this result. Does not include token annotation levels.
	 */
	public abstract Set<String> getAnnotationLevelSet();
	
	/**
	 * 
	 * @return A Set of annotation levels available for tokens in this result. Does not include structural annotation levels.
	 */
	public abstract Set<String> getTokenAnnotationLevelSet();

	/**
	 * 
	 * @param nodeId
	 * @return True if node with id nodeId has a corresponding marker.
	 */
	public abstract boolean hasNodeMarker(Long nodeId);

	/**
	 * Establishes a new marker in this result item.
	 * 
	 * @param markerId
	 * @param nodeId
	 */
	public abstract void putMarker(String markerId, Long nodeId);

	/**
	 * 
	 * @param markerId
	 * @return The corresponding node id for a particular marker.
	 */
	public abstract Long getNodeId(String markerId);

	/**
	 * 
	 * 
	 * @param nodeId
	 * @return The corresponding marker id for a particular node. Null if this node has no marker.
	 */
	public abstract String getMarkerId(Long nodeId);

	/**
	 * 
	 * @param markerId
	 * @return True if there is a marker known by markerId. Otherwise false.
	 */
	public abstract boolean hasMarker(String markerId);
	
}