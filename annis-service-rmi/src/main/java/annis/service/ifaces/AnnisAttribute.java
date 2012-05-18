/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.service.ifaces;

import java.io.Serializable;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents an attribute available in a corpus.
 * May contain a list of distinct values on that particular attribute.
 * 
 * @author k.huetter
 *
 */
@XmlRootElement
public interface AnnisAttribute extends Serializable
{

  public enum Type
  {
    node,
    edge,
    segmentation,
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
   * Get the fully qualified edge name including namespace (if any)
   * or null if this is not an edge.
   */
  public String getEdgeName();

  /**
   * Set the fully qualified edge name including namespace (if any)
   * or null if this is not an edge.
   */
  public void setEdgeName(String edgeName);

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
