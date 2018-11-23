/*
 * Copyright 2013 SFB 632.
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
package annis.sqlgen;

import annis.dao.objects.AnnotatedMatch;
import annis.dao.objects.AnnotatedSpan;
import annis.model.Annotation;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;

/**
 * Implements an {@link Iterator} for a {@link AnnotatedMatch} from
 * a JDBC {@link ResultSet}.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class AnnotatedMatchIterator implements Iterator<AnnotatedMatch>
{
  private final ResultSetTypedIterator<AnnotatedSpan> itSpan;
  private AnnotatedSpan lastSpan;
  
  public AnnotatedMatchIterator(ResultSet rs, RowMapper<AnnotatedSpan> mapper)
  {
    this.itSpan = new ResultSetTypedIterator<>(rs, mapper);
    this.lastSpan = null;
  }
  
  /**
   * Returns to the beginning of the iteration.
   */
  public void reset()
  {
    lastSpan = null;
    itSpan.reset();
  }

  @Override
  public boolean hasNext()
  {
    return lastSpan != null || itSpan.hasNext();
  }

  @Override
  public AnnotatedMatch next()
  {
    List<Long> key = new ArrayList<>();
    AnnotatedSpan[] matchedSpans = new AnnotatedSpan[0];
    
    if(lastSpan != null)
    {
      key = lastSpan.getKey();
      if(key == null)
      {
        matchedSpans = new AnnotatedSpan[0];
      }
      else
      {
        matchedSpans = new AnnotatedSpan[key.size()];

        setSpanForAllMatchedPositions(key, matchedSpans, lastSpan);
      }
      
      lastSpan = null;
    }
    
    while(itSpan.hasNext())
    {
      AnnotatedSpan span = itSpan.next();
      List<Long> newKey = span.getKey();
      
      if(matchedSpans.length == 0)
      {
        matchedSpans = new AnnotatedSpan[newKey.size()];
      }
      
      if(key.isEmpty() || newKey.equals(key))
      {
        setSpanForAllMatchedPositions(newKey, matchedSpans, span);
        
        key = newKey;
        lastSpan = null;
      }
      else
      {
        // save reference to the already fetched span since we can't get back
        lastSpan = span;
        // we finished collecting all relevant spans
        break;
      }
    }
    
    // HACK: delete metadata spans for non-first nodes 
    for(int i=1; i < matchedSpans.length; i++)
    {
      if(matchedSpans[i] != null)
      {
        matchedSpans[i].setMetadata(new LinkedList<Annotation>());
      }
    }
    
    return new AnnotatedMatch(matchedSpans);    
  }

  @Override
  public void remove()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  private void setSpanForAllMatchedPositions(List<Long> key, 
    AnnotatedSpan[] matchedSpans, AnnotatedSpan span)
  {
    // set annotation spans for *all* positions of the id
    // (node could have matched several times)
    int i=0;
    for(Long lRaw : key)
    {
      if(lRaw != null)
      {
        long l = (long) lRaw;
        if(l == span.getId())
        {
          matchedSpans[i] = new AnnotatedSpan(span);
        }
      }
      i++;
    }
  }
  
}
