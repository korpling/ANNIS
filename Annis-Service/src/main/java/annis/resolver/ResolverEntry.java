/*
 *  Copyright 2010 Collaborative Research Centre SFB 632.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package annis.resolver;

import java.io.Serializable;
import java.util.Properties;

/**
 * This class corresponds to a single entry in the resolver table
 * @author thomas
 */
public class ResolverEntry implements Serializable
{
  public enum ElementType
  {
    Node, Edge
  }

  private long id;
  private String corpus;
  private String version;
  private String namespace;
  private ElementType element;
  private String visType;
  private String displayName;

  private Properties mappings;
  private int order;

  public ResolverEntry(long id, String corpus, String version, String namespace, ElementType element, String visType, String displayName, Properties mappings, int order)
  {
    this.id = id;
    this.corpus = corpus;
    this.version = version;
    this.namespace = namespace;
    this.element = element;
    this.visType = visType;
    this.displayName = displayName;
    this.mappings = mappings;
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



}
