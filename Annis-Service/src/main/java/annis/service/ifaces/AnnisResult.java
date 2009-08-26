package annis.service.ifaces;

import annis.model.AnnotationGraph;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * This is the actual container for a single search result.
 * 
 * @author k.huetter
 *
 */
public interface AnnisResult extends Serializable {
	
	/** PAULA-Unart representation */
	public String getPaula();

	/** id of last token */
	public long getEndNodeId();

	/** id of first token */
	public long getStartNodeId();

	/** ordered list of tokens */
	public List<AnnisToken> getTokenList();
	
	/** non-token annotation names */
	public Set<String> getAnnotationLevelSet();
	
	/** token annotation names */
	public Set<String> getTokenAnnotationLevelSet();

	/** get marker for node */
	public String getMarkerId(Long nodeId);

	/** is there a node with marker markerID **/
	public boolean hasMarker(String markerId);

  /** Get the underlying annotation graph */
  public AnnotationGraph getGraph();
	
}