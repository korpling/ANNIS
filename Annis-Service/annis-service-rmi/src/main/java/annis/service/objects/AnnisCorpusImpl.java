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

import annis.service.ifaces.AnnisCorpus;
import annis.service.ifaces.JSONAble;

public class AnnisCorpusImpl implements Serializable, JSONAble, AnnisCorpus
{

  private static final long serialVersionUID = 7622346856447696397L;
  private long id;
  private String name;
  private int textCount, tokenCount;

  public AnnisCorpusImpl(long id, String name, int textCount, int tokenCount)
  {
    this.id = id;
    this.textCount = textCount;
    this.tokenCount = tokenCount;
    this.name = name;
  }

  public AnnisCorpusImpl()
  {
    this(0, null, 0, 0);
  }

  /* (non-Javadoc)
   * @see annisservice.objects.AnnisCorpus#getId()
   */
  @Override
  public long getId()
  {
    return id;
  }
  /* (non-Javadoc)
   * @see annisservice.objects.AnnisCorpus#setId(long)
   */

  @Override
  public void setId(long id)
  {
    this.id = id;
  }
  /* (non-Javadoc)
   * @see annisservice.objects.AnnisCorpus#getName()
   */

  @Override
  public String getName()
  {
    return name;
  }
  /* (non-Javadoc)
   * @see annisservice.objects.AnnisCorpus#setName(java.lang.String)
   */

  @Override
  public void setName(String name)
  {
    this.name = name;
  }
  /* (non-Javadoc)
   * @see annisservice.objects.AnnisCorpus#getTextCount()
   */

  @Override
  public int getTextCount()
  {
    return textCount;
  }
  /* (non-Javadoc)
   * @see annisservice.objects.AnnisCorpus#setTextCount(int)
   */

  @Override
  public void setTextCount(int textCount)
  {
    this.textCount = textCount;
  }
  /* (non-Javadoc)
   * @see annisservice.objects.AnnisCorpus#getTokenCount()
   */

  @Override
  public int getTokenCount()
  {
    return tokenCount;
  }
  /* (non-Javadoc)
   * @see annisservice.objects.AnnisCorpus#setTokenCount(int)
   */

  @Override
  public void setTokenCount(int tokenCount)
  {
    this.tokenCount = tokenCount;
  }

  @Override
  public String toString()
  {
    return String.valueOf("corpus #" + id + ": " + name);
  }

  /* (non-Javadoc)
   * @see annisservice.objects.AnnisCorpus#getJSON()
   */
  @Override
  public String getJSON()
  {
    return "{\"id\":" + id + ",\"name\":\"" + name + "\",\"textCount\":" + textCount + ",\"tokenCount\":" + tokenCount + "}";
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
    final AnnisCorpusImpl other = (AnnisCorpusImpl) obj;
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
