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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author thomas
 */
public class HistoryEntry implements CitationProvider, Serializable
{

  private Set<String> corpora;
  private String query;

  public HistoryEntry()
  {
    corpora = new HashSet<String>();
  }

  @Override
  public Set<String> getCorpora()
  {
    return corpora;
  }

  public void setCorpora(Set<String> corpora)
  {
    this.corpora = corpora;
  }

  @Override
  public String getQuery()
  {
    return query;
  }

  public void setQuery(String query)
  {
    this.query = query;
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

  @Override
  public String toString()
  {
    return StringUtils.replaceChars(query, "\r\n", "  ");
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
    final HistoryEntry other = (HistoryEntry) obj;
    if(this.corpora != other.corpora && (this.corpora == null || !this.corpora.equals(other.corpora)))
    {
      return false;
    }
    if((this.query == null) ? (other.query != null) : !this.query.equals(other.query))
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 11 * hash + (this.corpora != null ? this.corpora.hashCode() : 0);
    hash = 11 * hash + (this.query != null ? this.query.hashCode() : 0);
    return hash;
  }
}
