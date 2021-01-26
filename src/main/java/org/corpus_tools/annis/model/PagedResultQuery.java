/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corpus_tools.annis.model;

import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.corpus_tools.annis.api.model.FindQuery.OrderEnum;

/**
 * Parameters for a query that shows the result to the user
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class PagedResultQuery extends ContextualizedQuery {
  /**
   * 
   */
  private static final long serialVersionUID = -4589516338778409392L;
  private long offset;
  private int limit;
  private OrderEnum order = OrderEnum.NORMAL;

  public PagedResultQuery() {

  }

  public PagedResultQuery(int contextLeft, int contextRight, long offset, int limit,
      String segmentation, String query, Set<String> corpora) {
    super.setLeftContext(contextLeft);
    super.setRightContext(contextRight);
    super.setSegmentation(segmentation);
    super.setQuery(query);
    super.setCorpora(corpora);

    this.offset = offset;
    this.limit = limit;
  }

  public PagedResultQuery(PagedResultQuery orig) {
    super(orig);
    this.offset = orig.getOffset();
    this.limit = orig.getLimit();
    this.order = orig.getOrder();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PagedResultQuery other = (PagedResultQuery) obj;
    return Objects.equals(getQuery(), other.getQuery())
        && Objects.equals(getCorpora(), other.getCorpora())
        && Objects.equals(getLeftContext(), other.getLeftContext())
        && Objects.equals(getRightContext(), other.getRightContext())
        && Objects.equals(getSegmentation(), other.getSegmentation())
        && Objects.equals(getLimit(), other.getLimit())
        && Objects.equals(getOffset(), other.getOffset())
        && Objects.equals(getOrder(), other.getOrder());
  }

  @Override
  public Map<String, String> getCitationFragmentArguments() {
    Map<String, String> result = super.getCitationFragmentArguments();
    result.put("s", "" + getOffset());
    result.put("l", "" + getLimit());
    if (getSegmentation() != null) {
      result.put("_seg", getSegmentation());
    }

    return result;
  }

  public int getLimit() {
    return limit;
  }

  public long getOffset() {
    return offset;
  }

  public OrderEnum getOrder() {
    return order;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getCorpora(), getQuery(), getLeftContext(), getRightContext(),
        getSegmentation(), getLimit(), getOffset(), getOrder());
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }


  public void setOffset(long offset) {
    this.offset = offset;
  }

  public void setOrder(OrderEnum order) {
    Preconditions.checkNotNull(order, "The order of a paged result query must never be null.");
    this.order = order;
  }

}
