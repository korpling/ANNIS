package annis.service.ifaces;

import java.io.Serializable;
import java.util.Set;

/**
 * Set container for AnnisResult objects.
 * 
 * @author k.huetter
 *
 */
public interface AnnisResultSet extends Set<AnnisResult>, Serializable, JSONAble {
	public abstract Set<String> getAnnotationLevelSet();
	public abstract Set<String> getTokenAnnotationLevelSet();
}