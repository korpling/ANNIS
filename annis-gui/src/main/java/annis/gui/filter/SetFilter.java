/*
 * Copyright 2015 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.filter;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import java.util.Set;

/**
 * A filter for containers to include only items available in a specific
 * set.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 * @param <T>
 */
public class SetFilter<T> implements Container.Filter
{
  
  private final Set<T> allowedItems;
  
  private final Object propertyId;

  /**
   * 
   * @param allowedItems
   * @param propertyId 
   */
  public SetFilter(Set<T> allowedItems, Object propertyId)
  {
    this.allowedItems = allowedItems;
    this.propertyId = propertyId;
  }

  
  
  @Override
  public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException
  {
    Property p = item.getItemProperty(propertyId);
    if(p != null)
    {
      return allowedItems.contains((T) p.getValue());
    }
    return false;
  }

  @Override
  public boolean appliesToProperty(Object propertyId)
  {
    return this.propertyId.equals(propertyId);
  }
  
}
