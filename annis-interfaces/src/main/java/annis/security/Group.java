/*
 * Copyright 2014 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package annis.security;

import annis.CaseSensitiveOrder;
import com.google.common.base.Splitter;
import java.io.Serializable;
import java.util.List;
import java.util.TreeSet;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a group. If a {@link AnnisUserConfig} is assigned to a group
 * it will inherit the access rights of this group.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@XmlRootElement
public class Group implements Serializable
{
  
  public static final String WILDCARD = "*";
  public static final String ANONYMOUS = "anonymous";
  public static final String DEFAULT_USER_ROLE = "user";
  
  private String name;
  private TreeSet<String> corpora = new TreeSet<>(CaseSensitiveOrder.INSTANCE);

  public Group()
  {
    
  }
  
  public Group(String name)
  {
    this.name = name;
  }
  
  public Group(String name, TreeSet<String> corpora)
  {
    this.name = name;
    this.corpora = corpora;
  }
  
  public Group(String name, String corpusNames)
  {
    this(name);
    List<String> splitted = Splitter.on(',').omitEmptyStrings().trimResults().splitToList(
      corpusNames);
    corpora.addAll(splitted);
  }
  
  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  @XmlElement(name="corpora")
  public TreeSet<String> getCorpora()
  {
    return corpora;
  }

  public void setCorpora(TreeSet<String> corpora)
  {
    this.corpora = corpora;
  }
  
}
