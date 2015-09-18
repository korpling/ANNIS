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
package annis.service.objects;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AnnisCorpus implements Serializable, Comparable<AnnisCorpus>
{

  private long id;
  private String name;
  private int textCount, tokenCount;
  private String sourcePath;

  public AnnisCorpus(long id, String name, int textCount, int tokenCount)
  {
    this.id = id;
    this.textCount = textCount;
    this.tokenCount = tokenCount;
    this.name = name;
  }

  public AnnisCorpus()
  {
    this(0, null, 0, 0);
  }


  public long getId()
  {
    return id;
  }

  public void setId(long id)
  {
    this.id = id;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public int getTextCount()
  {
    return textCount;
  }

  public void setTextCount(int textCount)
  {
    this.textCount = textCount;
  }

  public int getTokenCount()
  {
    return tokenCount;
  }
  
  public void setTokenCount(int tokenCount)
  {
    this.tokenCount = tokenCount;
  }

  public String getSourcePath()
  {
    return sourcePath;
  }

  public void setSourcePath(String sourcePath)
  {
    this.sourcePath = sourcePath;
  }
  

  @Override
  public String toString()
  {
    return String.valueOf("corpus #" + id + ": " + name);
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
    final AnnisCorpus other = (AnnisCorpus) obj;
    if(this.id != other.id)
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 53 * hash + (int) (this.id ^ (this.id >>> 32));
    return hash;
  }

  @Override
  public int compareTo(AnnisCorpus o)
  {
    if(o == null)
    {
      return -1;
    }
    else
    {
      return id < o.getId() ? -1 : (id > o.getId() ? +1 : 0);
    }
  }
}
