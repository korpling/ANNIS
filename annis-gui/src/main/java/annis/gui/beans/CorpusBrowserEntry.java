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

/**
 *
 * @author thomas
 */
public class CorpusBrowserEntry
{
  private String name;
  private String example;
  private long corpusId;

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

  public long getCorpusId()
  {
    return corpusId;
  }

  public void setCorpusId(long corpusId)
  {
    this.corpusId = corpusId;
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
    if((this.example == null) ? (other.example != null) : !this.example.equals(other.example))
    {
      return false;
    }
    if(this.corpusId != other.corpusId)
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 89 * hash + (this.name != null ? this.name.hashCode() : 0);
    hash = 89 * hash + (this.example != null ? this.example.hashCode() : 0);
    hash = 89 * hash + (int) (this.corpusId ^ (this.corpusId >>> 32));
    return hash;
  }
  
  
}
