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
package annis.resolver;

import java.io.Serializable;
import java.util.Properties;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class corresponds to a single entry in the resolver table
 *
 * @author thomas
 */
@XmlRootElement
public class ResolverEntry implements Serializable
{

  public enum ElementType
  {

    node, edge
  }
  private long id;
  private String corpus;
  private String version;
  private String namespace;
  private ElementType element;
  private String visType;
  private String displayName;
  private String visibility;
  private Properties mappings;
  private int order;

  public ResolverEntry()
  {
  }

  public ResolverEntry(long id, String corpus, String version, String namespace, ElementType element, String visType, String displayName, String visibility, Properties mappings, int order)
  {
    this.id = id;
    this.corpus = corpus;
    this.version = version;
    this.namespace = namespace;
    this.element = element;
    this.visType = visType;
    this.displayName = displayName;
    this.mappings = mappings;
    this.visibility = visibility;
    this.order = order;
  }

  public String getCorpus()
  {
    return corpus;
  }

  public void setCorpus(String corpus)
  {
    this.corpus = corpus;
  }

  public String getDisplayName()
  {
    return displayName;
  }

  public void setDisplayName(String displayName)
  {
    this.displayName = displayName;
  }

  public ElementType getElement()
  {
    return element;
  }

  public void setElement(ElementType element)
  {
    this.element = element;
  }

  public long getId()
  {
    return id;
  }

  public void setId(long id)
  {
    this.id = id;
  }

  public Properties getMappings()
  {
    return mappings;
  }

  public void setMappings(Properties mappings)
  {
    this.mappings = mappings;
  }

  public String getNamespace()
  {
    return namespace;
  }

  public void setNamespace(String namespace)
  {
    this.namespace = namespace;
  }

  public int getOrder()
  {
    return order;
  }

  public void setOrder(int order)
  {
    this.order = order;
  }

  public String getVersion()
  {
    return version;
  }

  public void setVersion(String version)
  {
    this.version = version;
  }

  public String getVisType()
  {
    return visType;
  }

  public void setVisType(String visType)
  {
    this.visType = visType;
  }

  public String getVisibility()
  {
    return this.visibility;
  }

  public void setVisibility(String visibility)
  {
    this.visibility = visibility;
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
    final ResolverEntry other = (ResolverEntry) obj;
    if (this.id != other.id)
    {
      return false;
    }
    if ((this.corpus == null) ? (other.corpus != null) : !this.corpus.equals(other.corpus))
    {
      return false;
    }
    if ((this.version == null) ? (other.version != null) : !this.version.equals(other.version))
    {
      return false;
    }
    if ((this.namespace == null) ? (other.namespace != null) : !this.namespace.equals(other.namespace))
    {
      return false;
    }
    if (this.element != other.element && (this.element == null || !this.element.equals(other.element)))
    {
      return false;
    }
    if ((this.visType == null) ? (other.visType != null) : !this.visType.equals(other.visType))
    {
      return false;
    }
    if ((this.displayName == null) ? (other.displayName != null) : !this.displayName.equals(other.displayName))
    {
      return false;
    }

    if ((this.visibility == null) ? (other.visibility != null) : !this.visibility.equals(other.visibility))
    {
      return false;
    }


    if (this.mappings != other.mappings && (this.mappings == null || !this.mappings.equals(other.mappings)))
    {
      return false;
    }
    if (this.order != other.order)
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 97 * hash + (int) (this.id ^ (this.id >>> 32));
    hash = 97 * hash + (this.corpus != null ? this.corpus.hashCode() : 0);
    hash = 97 * hash + (this.version != null ? this.version.hashCode() : 0);
    hash = 97 * hash + (this.namespace != null ? this.namespace.hashCode() : 0);
    hash = 97 * hash + (this.element != null ? this.element.hashCode() : 0);
    hash = 97 * hash + (this.visType != null ? this.visType.hashCode() : 0);
    hash = 97 * hash + (this.displayName != null ? this.displayName.hashCode() : 0);
    hash = 97 * hash + (this.visibility != null ? this.visibility.hashCode() : 0);
    hash = 97 * hash + (this.mappings != null ? this.mappings.hashCode() : 0);
    hash = 97 * hash + this.order;
    return hash;
  }
}
