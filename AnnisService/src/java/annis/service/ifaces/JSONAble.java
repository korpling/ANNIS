package annis.service.ifaces;

/**
 * Interface used by frontend application to use JSON as data interchange format.
 * For further information on JSON format please refer to {@link http://json.org}.
 * 
 * @author k.huetter
 *
 */
public interface JSONAble {
	
	/**
	 * 
	 * @return JSON representation of this object.
	 */
	public String getJSON();
}
