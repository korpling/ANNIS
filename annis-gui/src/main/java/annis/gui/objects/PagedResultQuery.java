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
package annis.gui.objects;

import java.util.Set;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

/**
 * Parameters for a query that shows the result to the user
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class PagedResultQuery extends ContextualizedQuery implements Cloneable
{
  private final static Logger log = LoggerFactory.getLogger(PagedResultQuery.class);
  
  private int offset;
  private int limit;
  

  public PagedResultQuery()
  {
    
  }

  public PagedResultQuery(int contextLeft, int contextRight, int offset,
    int limit, String segmentation, String query,
    Set<String> corpora)
  {
    super.setContextLeft(contextLeft);
    super.setContextRight(contextRight);
    super.setSegmentation(segmentation);
    this.offset = offset;
    this.limit = limit;
  }

  public int getOffset()
  {
    return offset;
  }

  public void setOffset(int offset)
  {
    this.offset = offset;
  }

  public int getLimit()
  {
    return limit;
  }

  public void setLimit(int limit)
  {
    this.limit = limit;
  }

  
  @Override
  public PagedResultQuery clone()
  {
    PagedResultQuery c = null;    
    try
    {
      c = (PagedResultQuery) super.clone();
    }
    catch (CloneNotSupportedException ex)
    {
      log.error("cloning of {} failed", PagedResultQuery.class.getName());
    }
    
    return c;
  }
}
