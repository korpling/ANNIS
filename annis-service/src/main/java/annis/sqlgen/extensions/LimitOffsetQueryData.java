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
package annis.sqlgen.extensions;

import java.util.Optional;

import annis.service.objects.OrderType;

/**
 *
 * @author benjamin
 */
public class LimitOffsetQueryData {

  private final long offset;
  private final long limit;
  private final OrderType order;

  public LimitOffsetQueryData(long offset, long limit) {
    this.offset = offset;
    this.limit = limit;
    this.order = OrderType.ascending;
  }

  public LimitOffsetQueryData(long offset, long limit, OrderType order) {
    this.offset = offset;
    this.limit = limit;
    this.order = order;
  }

  public Optional<Long> getLimit() {
    if (limit == 0 || limit == Long.MAX_VALUE) {
      return Optional.empty();
    } else {
      return Optional.of(limit);
    }
  }

  public long getOffset() {
    return offset;
  }

  public boolean isPaged() {
    return offset != 0 || limit != 0;
  }

  public OrderType getOrder() {
    return order;
  }

}
