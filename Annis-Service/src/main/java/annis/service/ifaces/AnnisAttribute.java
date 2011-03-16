package annis.service.ifaces;

import java.io.Serializable;
import java.util.Set;

/**
 * Represents an attribute available in a corpus.
 * May contain a list of distinct values on that particular attribute.
 * 
 * @author k.huetter
 *
 */
public interface AnnisAttribute extends JSONAble, Serializable
{

  public enum Type
  {
    node,
    edge,
    unknown
  };

  public enum SubType
  {
    n,
    d,
    p,
    c,
    unknown
  };

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
   * Returns the type (node, edge, ...) of this attribute
   * @return
   */
  public Type getType();

  /**
   * Sets the type (node, edge, ...) of this attribute
   * @param type
   */
  public void setType(Type type);

  /**
   * Returns the sub-type ((n)ode, (c)overage, (d)ominance, ...) of this attribute
   * @return
   */
  public SubType getSubtype();

  /**
   * Sets the sub-type ((n)ode, (c)overage, (d)ominance, ...) of this attribute
   * @param type
   */
  public void setSubtype(SubType subtype);
  
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
