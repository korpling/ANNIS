/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package annis.service.objects;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement
public class AnnisAttribute implements Serializable {

  public enum SubType {
    n, d, p, c, m, unknown
  }

  public enum Type {
    node, edge, segmentation, meta, unknown
  };

  /**
   * 
   */
  private static final long serialVersionUID = 6152665033698349568L;;


  private String name = "";
  private String edgeName = null;
  private LinkedHashSet<String> distinctValues = new LinkedHashSet<String>();
  private Type type;
  private SubType subtype;


  public void addValue(String value) {
    this.distinctValues.add(value);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof AnnisAttribute)) {
      return false;
    }

    AnnisAttribute other = (AnnisAttribute) obj;

    return new EqualsBuilder().append(this.name, other.name)
        .append(this.distinctValues, other.distinctValues).isEquals();
  }

  public String getEdgeName() {
    return edgeName;
  }

  public String getName() {
    return this.name;
  }

  public SubType getSubtype() {
    return subtype;
  }

  public Type getType() {
    return type;
  }


  @XmlElementWrapper(name = "value-set")
  @XmlElement(name = "value")
  public Collection<String> getValueSet() {
    return distinctValues;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(name).append(distinctValues).toHashCode();
  }

  public boolean hasValue(String value) {
    return this.distinctValues.contains(value);
  }

  public void setEdgeName(String edgeName) {
    this.edgeName = edgeName;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setSubtype(SubType subtype) {
    this.subtype = subtype;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public void setValueSet(Collection<String> values) {
    this.distinctValues = new LinkedHashSet<String>(values);
  }

  @Override
  public String toString() {
    return name + " " + distinctValues;
  }
}
