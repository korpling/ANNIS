/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
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

package annis.gui.requesthandler;

import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ContentRange
{
  private final long start;

  private final long end;

  private final long totalSize;
  
  private static final Pattern fullPattern 
    = Pattern.compile("^bytes=[0-9]*-[0-9]*(,[0-9]d*-[0-9]d*)*$");
  
  private static final Pattern partPattern 
    = Pattern.compile("^([0-9]*)-([0-9]*)$");

  public ContentRange(long start, long end, long totalSize)
  {
    this.start = start;
    this.end = end;
    this.totalSize = totalSize;
  }

  /**
   * Parses the header value of a HTTP Range request
   * @param rawRange raw range value as given by the header
   * @param totalSize total size of the content
   * @param maxNum maximal number of allowed ranges. Will throw exception if client requests more ranges.
   * @return
   * @throws annis.gui.requesthandler.ContentRange.InvalidRangeException 
   */
  public static List<ContentRange> parseFromHeader(String rawRange, 
    long totalSize,
    int maxNum) throws InvalidRangeException
  {
    List<ContentRange> result = new ArrayList<>();
    if (rawRange != null)
    {
      if(!fullPattern.matcher(rawRange).matches())
      {
        throw new InvalidRangeException("invalid syntax");
      }
      
      rawRange = rawRange.substring("bytes=".length(), rawRange.length());
      
      
      for(String partRange : Splitter.on(",")
        .omitEmptyStrings().trimResults().split(rawRange))
      {
        if(result.size() >= totalSize)
        {
          throw new InvalidRangeException("more ranges than acceptable");
        }
        
        long from = 0;
        long to = totalSize-1;
        
        Matcher m = partPattern.matcher(partRange);
        if(!m.find())
        {
          throw new InvalidRangeException("invalid syntax for partial range");
        }
        
        String fromString = m.group(1);
        String toString = m.group(2);
        
        if(fromString != null && !fromString.isEmpty())
        {
          from = Long.parseLong(fromString);
        }
        if(toString != null && !toString.isEmpty())
        {
          to = Long.parseLong(toString);
        }
        
        if(from > to)
        {
          throw new InvalidRangeException("start is larger then end");
        }
        
        result.add(new ContentRange(from, to, totalSize));
        
      }
    }
    return result;
  }

  public long getStart()
  {
    return start;
  }

  public long getEnd()
  {
    return end;
  }

  public long getTotalSize()
  {
    return totalSize;
  }

  public long getLength()
  {
    return end - start + 1;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 59 * hash + (int) (this.start ^ (this.start >>> 32));
    hash = 59 * hash + (int) (this.end ^ (this.end >>> 32));
    hash = 59 * hash + (int) (this.totalSize ^ (this.totalSize >>> 32));
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
    final ContentRange other = (ContentRange) obj;
    if (this.start != other.start)
    {
      return false;
    }
    if (this.end != other.end)
    {
      return false;
    }
    if (this.totalSize != other.totalSize)
    {
      return false;
    }
    return true;
  }

  @Override
  public String toString()
  {
    return "bytes " + start + "-" + end + "/" + totalSize;
  }
  
  public static class InvalidRangeException extends Exception
  {
    public InvalidRangeException()
    {
      super("");
    }
    
    public InvalidRangeException(String message)
    {
      super(message);
    }
  }
  
}
