/*
 * Copyright 2012 SFB 632.
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
package annis.libgui;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A corpus set defines a named set of corpus names.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@XmlRootElement
public class CorpusSet implements Serializable
{
  
  private Set<String> corpora = new TreeSet<>();
  private String name = ""; 

  @XmlElement(name="corpus")
  public Set<String> getCorpora()
  {
    return corpora;
  }

  public void setCorpora(Set<String> corpora)
  {
    this.corpora = corpora;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
    return hash;
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
    final CorpusSet other = (CorpusSet) obj;
    if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name))
    {
      return false;
    }
    return true;
  }
  
}
