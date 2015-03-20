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
  private long id;
  private AnnisNode source;
  private AnnisNode destination;
  private long pre;
  private long componentID;
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
    String src = source != null ? "" + source.getId() : "(no source)";
    String dst = destination != null ? "" + destination.getId() : "(no destination)";
    String type = edgeType != null ? edgeType.toString() : "(no type)";
    String qname = getQualifiedName() != null ? getQualifiedName() : "(no name)";
    
    String strcomponent = "component-id " + componentID;
    String strpre = "pre-order " + pre;
    
    
    return src + "->" + dst + " " + qname + " " + type + "(" 
      + strcomponent + ", " + strpre + ")";
  }

  public String getQualifiedName()
  {
    return AnnisNode.qName(namespace, name);
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final Edge other = (Edge) obj;
    if (this.source != other.source &&
      (this.source == null || !this.source.equals(other.source)))
    {
      return false;
    }
    if (this.destination != other.destination &&
      (this.destination == null || !this.destination.equals(other.destination)))
    {
      return false;
    }
    if (this.pre != other.pre)
    {
      return false;
    }
    if (this.componentID != other.componentID)
    {
      return false;
    }
    if (this.edgeType != other.edgeType)
    {
      return false;
    }
    if ((this.namespace == null) ? (other.namespace != null)
      : !this.namespace.equals(other.namespace))
    {
      return false;
    }
    if ((this.name == null) ? (other.name != null)
      : !this.name.equals(other.name))
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 17 * hash + (this.source != null ? this.source.hashCode() : 0);
    hash =
      17 * hash + (this.destination != null ? this.destination.hashCode() : 0);
    hash = 17 * hash + (int) (this.pre ^ (this.pre >>> 32));
    hash = 17 * hash + (int) (this.componentID ^ (this.componentID >>> 32));
    hash = 17 * hash + (this.edgeType != null ? this.edgeType.hashCode() : 0);
    hash = 17 * hash + (this.namespace != null ? this.namespace.hashCode() : 0);
    hash = 17 * hash + (this.name != null ? this.name.hashCode() : 0);
    return hash;
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

  public long getComponentID()
  {
    return componentID;
  }

  public void setComponentID(long componentID)
  {
    this.componentID = componentID;
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

  public long getId()
  {
    return id;
  }

  public void setId(long id)
  {
    this.id = id;
  }
  
  
}
