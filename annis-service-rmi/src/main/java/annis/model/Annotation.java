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
package annis.model;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@XmlRootElement
public class Annotation implements Comparable<Annotation>, Serializable
{

  // this class is sent to the front end
  private String namespace;
  private String name;
  private String value;
  private String type;
  private String corpusName;
  private int pre; // determine the order

  public Annotation()
  {
  }
  
  public Annotation(String namespace, String name)
  {
    this(namespace, name, null);
  }

  public Annotation(String namespace, String name, String value)
  {
    this.namespace = namespace;
    this.name = name;
    this.value = value;
  }

  public Annotation(String namespace, String name, String value, String type,
    String corpusName)
  {
    this(namespace, name, value);
    this.type = type;
    this.corpusName = corpusName;
  }

  /**
   * With the constructor we could determine the order of the pre parameter
   */
  public Annotation(String namespace, String name, String value, String type,
    String corpusName, int pre)
  {
    this(namespace, name, value, type, corpusName);
    this.pre = pre;
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(AnnisNode.qName(namespace, name));
    if (value != null)
    {
      sb.append("=");
      sb.append(value);
    }
    return sb.toString();
  }

  @Override
  public int compareTo(Annotation o)
  {
    String name1 = getQualifiedName();
    String name2 = o.getQualifiedName();
    return name1.compareTo(name2);
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null || !(obj instanceof Annotation))
    {
      return false;
    }

    Annotation other = (Annotation) obj;

    return new EqualsBuilder().append(this.namespace, other.namespace).append(
      this.name, other.name).append(this.value, other.value).isEquals();
  }

  @Override
  public int hashCode()
  {
    return new HashCodeBuilder().append(namespace).append(name).append(value).
      toHashCode();
  }

  // /// Getter / Setter
  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public String getNamespace()
  {
    return namespace;
  }

  public void setNamespace(String namespace)
  {
    this.namespace = namespace;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getQualifiedName()
  {
    return AnnisNode.qName(namespace, name);
  }

  public String getType()
  {
    return type;
  }

  public String getCorpusName()
  {
    return corpusName;
  }

  public int getPre()
  {
    return pre;
  }
}