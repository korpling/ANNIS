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

import annis.service.objects.OrderType;
import java.util.Objects;
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
  private OrderType order = OrderType.normal;

  public PagedResultQuery()
  {
    
  }

  public PagedResultQuery(int contextLeft, int contextRight, int offset,
    int limit, String segmentation, String query,
    Set<String> corpora)
  {
    super.setLeftContext(contextLeft);
    super.setRightContext(contextRight);
    super.setSegmentation(segmentation);
    super.setQuery(query);
    super.setCorpora(corpora);
    
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

  public OrderType getOrder()
  {
    return order;
  }

  public void setOrder(OrderType order)
  {
    this.order = order;
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
  
  @Override
  public int hashCode()
  {
    return Objects.hash(getCorpora(), getQuery(), getLeftContext(), getRightContext(), getSegmentation(), 
      getLimit(), getOffset(), getOrder());
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
    final PagedResultQuery other = (PagedResultQuery) obj;
    return
      Objects.equals(getQuery(), other.getQuery())
      && Objects.equals(getCorpora(), other.getCorpora())
      && Objects.equals(getLeftContext(), other.getLeftContext())
      && Objects.equals(getRightContext(), other.getRightContext())
      && Objects.equals(getSegmentation(), other.getSegmentation())
      && Objects.equals(getLimit(), other.getLimit())
      && Objects.equals(getOffset(), other.getOffset())
      && Objects.equals(getOrder(), other.getOrder());
  }
}
