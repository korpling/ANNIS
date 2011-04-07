package annis.service.objects;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import annis.service.ifaces.AnnisAttribute;

public class AnnisAttributeImpl implements AnnisAttribute, Serializable
{

  private static final long serialVersionUID = 4786862953057862936L;
  private String name = "";
  private String edgeName = null;
  private Set<String> distinctValues = new HashSet<String>();
  private Type type;
  private SubType subtype;

  @Override
  public Set<String> getValueSet()
  {
    return this.distinctValues;
  }

  @Override
  public String getName()
  {
    return this.name;
  }

  @Override
  public void setName(String name)
  {
    this.name = name;
  }

  @Override
  public String getEdgeName()
  {
    return edgeName;
  }

  @Override
  public void setEdgeName(String edgeName)
  {
    this.edgeName = edgeName;
  }


  @Override
  public Type getType()
  {
    return type;
  }

  @Override
  public void setType(Type type)
  {
    this.type = type;
  }

  @Override
  public SubType getSubtype()
  {
    return subtype;
  }

  @Override
  public void setSubtype(SubType subtype)
  {
    this.subtype = subtype;
  }
  
  @Override
  public String getJSON()
  {
    StringBuffer sBuffer = new StringBuffer();
    sBuffer.append("\"name\": \"").append(this.getName()).append("\",\n");
    if(this.getEdgeName() != null)
    {
      sBuffer.append("\"edge_name\": \"").append(this.getEdgeName()).append("\",\n");
    }
    sBuffer.append("\"type\" : \"").append(getType().name()).append("\",\n")
      .append("\"subtype\" : \"").append(getSubtype().name()).append("\",\n")
      .append("\"values\": [");
    int vCount = 0;
    for (String value : this.getValueSet())
    {
      if (vCount++ > 0)
      {
        sBuffer.append(", ");
      }
      sBuffer.append("\"").append(value).append("\"");
    }
    sBuffer.append("]");
    return "{" + sBuffer + "}";
  }

  @Override
  public void addValue(String value)
  {
    this.distinctValues.add(value);
  }

  @Override
  public boolean hasValue(String value)
  {
    return this.distinctValues.contains(value);
  }

  @Override
  public String toString()
  {
    return name + " " + distinctValues;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null || !(obj instanceof AnnisAttributeImpl))
    {
      return false;
    }

    AnnisAttributeImpl other = (AnnisAttributeImpl) obj;

    return new EqualsBuilder().append(this.name, other.name).append(this.distinctValues, other.distinctValues).isEquals();
  }

  @Override
  public int hashCode()
  {
    return new HashCodeBuilder().append(name).append(distinctValues).toHashCode();
  }
}
