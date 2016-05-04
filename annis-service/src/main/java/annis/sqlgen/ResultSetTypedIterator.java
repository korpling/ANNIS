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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

/**
 * A wrapper for a {@link ResultSet} and a {@link RowMapper} and combines them
 * in order to provide the {@link Iterator} interface.
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ResultSetTypedIterator<T> implements Iterator<T>
{
  
  private static final Logger log = LoggerFactory.getLogger(ResultSetTypedIterator.class);
  
  private ResultSet rs;
  private RowMapper<T> mapper;
  private boolean hasNext;
  private int rowNum;

  /**
   * Constructor
   * @param rs {@link ResultSet} to wrap. Must not be null.
   * @param mapper Is used to map each row in each iteration step.
   */
  public ResultSetTypedIterator(ResultSet rs,
    RowMapper<T> mapper)
  {
    this.rs = rs;
    this.mapper = mapper;
    this.rowNum = 0;
    
    if(rs == null)
    {
      throw new IllegalArgumentException("ResultSet must not be null");
    }
    if(mapper == null)
    {
      throw new IllegalArgumentException("RowMapper must not be null");
    }
    try
    {
      if(rs.getType() == ResultSet.TYPE_FORWARD_ONLY)
      {
        hasNext = rs.next();
      }
      else
      {
        hasNext = rs.first();
      }
    }
    catch (SQLException ex)
    {
      log.error(null, ex);
    }
    
  }
  
  /**
   * Returns to the beginning of the iteration.
   */
  public void reset()
  {
    try
    {
      if(rs.getType() == ResultSet.TYPE_FORWARD_ONLY)
      {
        throw new UnsupportedOperationException("Can not reset iterator for a ResultSet that is of type \"forward only\"");
      }
      hasNext = rs.first();
    }
    catch (SQLException ex)
    {
      log.error(null, ex);
    }
  }
  
  @Override
  public boolean hasNext()
  {
    return hasNext;
  }

  @Override
  public T next()
  {
    if(hasNext)
    {
      try
      {
        T result = mapper.mapRow(rs, rowNum++);

        // call next after the getter function to provide the item for the next call
        hasNext = rs.next();
        
        return result;
      }
      catch (SQLException ex)
      {
        log.warn("Cannot read next result set item", ex);
      }
    }
    
    hasNext = false;
    throw new NoSuchElementException();
  }

  @Override
  public void remove()
  {
    throw new UnsupportedOperationException("Removal of items in the result set is not supported.");
  }
  
}
