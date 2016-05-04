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
package annis.service.objects;

import com.google.common.base.Joiner;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * A frequency table holds the result of a frequency analysis on a specific result.
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@XmlRootElement
public class FrequencyTable implements Serializable
{
  
  private long sum;
  @XmlTransient
  private Set<Entry> entries;

  public FrequencyTable()
  {
    entries = new TreeSet<Entry>(new HighestCountComparator());
  }
  
  /**
   * Gets the sum of the counts of all entries.
   * @return 
   */
  public long getSum()
  {
    return sum;
  }
  
  public void setSum(long sum)
  {
    this.sum = sum;
  }
  
  /**
   * Adds an entry.
   * @param e 
   */
  public void addEntry(Entry e)
  {
    if(entries.add(e))
    {
      sum += e.getCount();
    }
  }

  public void removeEntry(Entry e)
  {
    if(entries.remove(e))
    {
      sum -= e.getCount(); 
    }
  }

  @XmlElement(name = "entry")
  public Collection<Entry> getEntries()
  {
    return entries;
  }
  
  public void setEntries(Collection<Entry> entries)
  {
    this.entries = new TreeSet<Entry>(new HighestCountComparator()); 
    this.entries.addAll(entries);
  }
  
  public static class HighestCountComparator implements Comparator<Entry>, Serializable
  {

    @Override
    public int compare(Entry lhs, Entry rhs)
    {
      if (lhs == rhs)
      {
        return 0;
      }
      if (lhs == null)
      {
        return +1; // greatest elements first (would be -1 otherwise
      }
      if (rhs == null)
      {
        return -1; // see comment above
      }

      // we want to have greatest elements first
      int countCmp = ((Long) lhs.getCount()).compareTo(rhs.getCount());
      if (countCmp == 0)
      {
        // their are actually equal in count, but we can still use their hash code
        return ((Integer) lhs.hashCode()).compareTo(rhs.hashCode());
      }
      else if(countCmp < 0)
      {
        return +1;
      }
      else
      {
        return -1;
      }

    }
  }
  
  public static class Entry implements Serializable
  {
    private String[] tupel;
    private long count;
    
    public Entry()
    {
      tupel = new String[0];
      count = 0;
    }
    
    public Entry(int tupelSize)
    {
      tupel = new String[tupelSize];
    }
    
    public Entry(String[] tupel, long count)
    {
      this.tupel = Arrays.copyOf(tupel, tupel.length);
      this.count = count;
    }

    public String[] getTupel()
    {
      return Arrays.copyOf(tupel, tupel.length);
    }

    public void setTupel(String[] tupel)
    {
      this.tupel = Arrays.copyOf(tupel, tupel.length);
    }

    public long getCount()
    {
      return count;
    }

    public void setCount(long count)
    {
      this.count = count;
    }

    @Override
    public int hashCode()
    {
      int hash = 7;
      hash = 41 * hash + Arrays.deepHashCode(this.tupel);
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
      final Entry other = (Entry) obj;
      if (!Arrays.deepEquals(this.tupel, other.tupel))
      {
        return false;
      }
      return true;
    }

    @Override
    public String toString()
    {
      return Joiner.on(" | ").join(tupel) + " -> " + count;
    }
    
    
  }
  
  
}
