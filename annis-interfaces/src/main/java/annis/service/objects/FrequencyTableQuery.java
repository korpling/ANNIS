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
package annis.service.objects;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This is an a list of several {@link FrequencyTableEntry} entries.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class FrequencyTableQuery extends ArrayList<FrequencyTableEntry>
{

  public static FrequencyTableQuery parse(String completeDefinition)
  {
    FrequencyTableQuery result = new FrequencyTableQuery();
    Iterator<String> it = Splitter.on(',').trimResults().omitEmptyStrings().
      split(completeDefinition).iterator();
    while (it.hasNext())
    {
      String f = it.next();
      FrequencyTableEntry entry = FrequencyTableEntry.parse(f);
      if (entry != null)
      {
        result.add(entry);
      }
    }
    return result;
  }

  @Override
  public String toString()
  {
    return Joiner.on(',').join(this);
  }
  
  
}
