/*
 *  Copyright 2010 thomas.
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

import annis.resolver.ResolverEntry.ElementType;
import java.io.Serializable;

/**
 * This represents a request to a resolver entry. A list of this type is a complete
 * query.
 * 
 * @author thomas
 */
public class SingleResolverRequest implements Serializable
{

  private long corpusId;
  private String namespace;
  private ResolverEntry.ElementType type;

  public SingleResolverRequest(long corpusId, String namespace, ElementType type)
  {
    this.corpusId = corpusId;
    this.namespace = namespace;
    this.type = type;
  }

  public long getCorpusId()
  {
    return corpusId;
  }

  public String getNamespace()
  {
    return namespace;
  }

  public ElementType getType()
  {
    return type;
  }

  
}
