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
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Edge implements Serializable
{

  // this class is sent to the front end
  public enum EdgeType
  {

    COVERAGE("c", "Coverage"),
    DOMINANCE("d", "Dominance"),
    POINTING_RELATION("p", "Pointing Relation"),
    UNKNOWN(null, "UnknownEdgeType");
    private String type;
    private String name;

    private EdgeType(String type, String name)
    {
      this.type = type;
      this.name = name;
    }

    @Override
    public String toString()
    {
      return name + (type != null ? "(" + type + ")" : "");
    }

    public String getTypeChar()
    {
      return type;
    }

    // FIXME: test parseEdgeType()
    public static EdgeType parseEdgeType(String type)
    {
      if ("c".equals(type))
      {
        return COVERAGE;
      }
      else if ("d".equals(type))
      {
        return DOMINANCE;
      }
      else if ("p".equals(type))
      {
        return POINTING_RELATION;
      }
      else
      {
        return UNKNOWN;
      }
    }
  };
  private AnnisNode source;
  private AnnisNode destination;
  private long pre;
  private EdgeType edgeType;
  private String namespace;
  private String name;
  private Set<Annotation> annotations;

  public Edge()
  {
    annotations = new HashSet<Annotation>();
    edgeType = EdgeType.UNKNOWN;
  }

  public boolean addAnnotation(Annotation o)
  {
    return annotations.add(o);
  }

  @Override
  public String toString()
  {
    Long src = source != null ? source.getId() : null;
    long dst = destination.getId();
    String type = edgeType != null ? edgeType.toString() : null;
    String name = getQualifiedName();
    return src + "->" + dst + " " + name + " " + type;
  }

  public String getQualifiedName()
  {
    return AnnisNode.qName(namespace, name);
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null || !(obj instanceof Edge))
    {
      return false;
    }

    Edge other = (Edge) obj;

    return new EqualsBuilder().append(this.source, other.source).append(
      this.destination, other.destination).append(this.pre, other.pre).append(
      this.edgeType, other.edgeType).append(this.name, other.name).append(
      this.namespace, other.namespace).isEquals();
  }

  @Override
  public int hashCode()
  {
    return new HashCodeBuilder().append(source).append(destination).append(pre).
      append(edgeType).append(getQualifiedName()).
      append(annotations).toHashCode();
  }

  ///// Getters / Setters
  public long getPre()
  {
    return pre;
  }

  public void setPre(long pre)
  {
    this.pre = pre;
  }

  public EdgeType getEdgeType()
  {
    return edgeType;
  }

  public void setEdgeType(EdgeType edgeType)
  {
    this.edgeType = edgeType;
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

  public Set<Annotation> getAnnotations()
  {
    return annotations;
  }

  public AnnisNode getSource()
  {
    return source;
  }

  public void setSource(AnnisNode source)
  {
    this.source = source;
  }

  public AnnisNode getDestination()
  {
    return destination;
  }

  public void setDestination(AnnisNode destination)
  {
    this.destination = destination;
  }
}
