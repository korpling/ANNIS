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

import annis.dao.AnnotatedMatch;
import annis.dao.AnnotatedSpan;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;

/**
 * Implements an {@link Iterator} for a {@link AnnotatedMatch} from
 * a JDBC {@link ResultSet}.
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class AnnotatedMatchIterator implements Iterator<AnnotatedMatch>
{
  private ResultSetTypedIterator<AnnotatedSpan> itSpan;
  private AnnotatedSpan lastSpan;
  
  public AnnotatedMatchIterator(ResultSet rs, RowMapper<AnnotatedSpan> mapper)
  {
    this.itSpan = new ResultSetTypedIterator<AnnotatedSpan>(rs, mapper);
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
    List<AnnotatedSpan> matchedSpans = new LinkedList<AnnotatedSpan>();
    List<Long> key = new ArrayList<Long>();
    
    if(lastSpan != null)
    {
      key = lastSpan.getKey();
      matchedSpans.add(lastSpan);
      lastSpan = null;
    }
    
    while(itSpan.hasNext())
    {
      AnnotatedSpan span = itSpan.next();
      List<Long> newKey = span.getKey();
      if(key.isEmpty() || newKey.equals(key))
      {
        matchedSpans.add(span);
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

    return new AnnotatedMatch(matchedSpans);    
  }

  @Override
  public void remove()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
}
