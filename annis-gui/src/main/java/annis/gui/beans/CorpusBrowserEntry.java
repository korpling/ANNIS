/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.beans;

import annis.service.objects.AnnisCorpus;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author thomas
 */
public class CorpusBrowserEntry implements CitationProvider, Serializable
{
  private String name;
  private String example;
  private AnnisCorpus corpus;

  public String getExample()
  {
    return example;
  }

  public void setExample(String example)
  {
    this.example = example;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public AnnisCorpus getCorpus()
  {
    return corpus;
  }

  public void setCorpus(AnnisCorpus corpus)
  {
    this.corpus = corpus;
  }

  @Override
  public boolean equals(Object obj)
  {
    if(obj == null)
    {
      return false;
    }
    if(getClass() != obj.getClass())
    {
      return false;
    }
    final CorpusBrowserEntry other = (CorpusBrowserEntry) obj;
    if((this.name == null) ? (other.name != null) : !this.name.equals(other.name))
    {
      return false;
    }
    if(this.corpus != other.corpus && (this.corpus == null || !this.corpus.equals(other.corpus)))
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 13 * hash + (this.name != null ? this.name.hashCode() : 0);
    hash = 13 * hash + (this.corpus != null ? this.corpus.hashCode() : 0);
    return hash;
  }

  
  @Override
  public String getQuery()
  {
    return example;
  }

  @Override
  public Set<String> getCorpora()
  {
    Set<String> result = new HashSet<>();
    result.add(corpus.getName());
    return result;
  }

  @Override
  public int getLeftContext()
  {
    return 5;
  }

  @Override
  public int getRightContext()
  {
    return 5;
  }
  
}
