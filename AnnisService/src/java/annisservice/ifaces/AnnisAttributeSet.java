package annisservice.ifaces;

import java.io.Serializable;
import java.util.Set;

/**
 * A set container for AnnisAttribute.
 * 
 * @author k.huetter
 *
 */
public interface AnnisAttributeSet extends Set<AnnisAttribute>, Serializable, JSONAble {

}