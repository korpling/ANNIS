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

import java.util.Objects;
import java.util.Set;

import com.google.common.base.Preconditions;

import annis.service.objects.OrderType;

/**
 * Parameters for a query that shows the result to the user
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class PagedResultQuery extends ContextualizedQuery
{  
  private long offset;
  private int limit;
  private OrderType order = OrderType.ascending;
  

  public PagedResultQuery()
  {
    
  }

  public PagedResultQuery(int contextLeft, int contextRight, long offset,
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

  public long getOffset()
  {
    return offset;
  }

  public void setOffset(long offset)
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
    Preconditions.checkNotNull(order, "The order of a paged result query must never be null.");
    this.order = order;
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
