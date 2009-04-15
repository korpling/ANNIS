package annisservice.ifaces;

import java.io.Serializable;
import java.util.Set;

/**
 * Represents an attribute available in a corpus.
 * May contain a list of distinct values on that particular attribute.
 * 
 * @author k.huetter
 *
 */
public interface AnnisAttribute extends JSONAble, Serializable {

	/**
	 * 
	 * @return A set of distinct values available on this attribute, if populated. Otherwise an empty set object.
	 */
	public Set<String> getValueSet();
	
	/**
	 * 
	 * @return The name of that this particular attribute.
	 */
	public String getName();
	
	/**
	 * Sets the name of this attribute.
	 * 
	 * @param name
	 */
	public void setName(String name);
	
	/**
	 * Adds a value the set of distinct values
	 * 
	 * @param value 
	 */
	public void addValue(String value);
	
	/**
	 * 
	 * @param value
	 * @return True if the value set contains value. Otherwise false.
	 */
	public boolean hasValue(String value);

}