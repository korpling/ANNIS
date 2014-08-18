/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
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

package annis.gui.controlpanel;

import com.vaadin.data.Item;
import com.vaadin.data.util.DefaultItemSorter;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class CorpusSorter extends DefaultItemSorter
{

  @Override
  protected int compareProperty(Object propertyId, boolean sortDirection,
    Item item1, Item item2)
  {
    if ("name".equals(propertyId))
    {
      String val1 = (String) item1.getItemProperty(propertyId).getValue();
      String val2 = (String) item2.getItemProperty(propertyId).getValue();
      if (sortDirection)
      {
        return val1.compareToIgnoreCase(val2);
      }
      else
      {
        return val2.compareToIgnoreCase(val1);
      }
    }
    else
    {
      return super.compareProperty(propertyId, sortDirection, item1, item2);
    }
  }
  
}
