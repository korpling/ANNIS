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
package annis.resolver;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;
import org.corpus_tools.annis.api.model.VisualizerRule.ElementEnum;

/**
 * This represents a request to a resolver entry. A list of this type is a complete query.
 * 
 * @author thomas
 */
@XmlRootElement
public class SingleResolverRequest implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 4092681220798179503L;
  private String corpusName;
  private String namespace;
  private ElementEnum type;

  public SingleResolverRequest() {

  }

  public SingleResolverRequest(String corpusName, String namespace, ElementEnum type) {
    this.corpusName = corpusName;
    this.namespace = namespace;
    this.type = type;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SingleResolverRequest other = (SingleResolverRequest) obj;
    if (!this.corpusName.equals(other.corpusName)) {
      return false;
    }
    if ((this.namespace == null) ? (other.namespace != null)
        : !this.namespace.equals(other.namespace)) {
      return false;
    }
    return this.type == other.type;
  }

  public String getCorpusName() {
    return corpusName;
  }

  public String getNamespace() {
    return namespace;
  }

  public ElementEnum getType() {
    return type;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 41 * hash + (this.corpusName != null ? this.corpusName.hashCode() : 0);
    hash = 41 * hash + (this.namespace != null ? this.namespace.hashCode() : 0);
    hash = 41 * hash + (this.type != null ? this.type.hashCode() : 0);
    return hash;
  }



}
