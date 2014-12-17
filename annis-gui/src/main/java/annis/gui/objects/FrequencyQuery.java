
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
package annis.gui.objects;

import annis.service.objects.FrequencyTableQuery;
import java.util.Objects;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class FrequencyQuery extends Query
{
  
  private FrequencyTableQuery frequencyDefinition;

  public FrequencyTableQuery getFrequencyDefinition()
  {
    return frequencyDefinition;
  }

  public void setFrequencyDefinition(FrequencyTableQuery frequencyDefinition)
  {
    this.frequencyDefinition = frequencyDefinition;
  }
  
  
  
  @Override
  public int hashCode()
  {
    return Objects.hash(getCorpora(), getQuery(), getFrequencyDefinition());
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
    final FrequencyQuery other = (FrequencyQuery) obj;
    
    return Objects.equals(this.getQuery(), other.getQuery())
      && Objects.equals(this.getCorpora(), other.getCorpora())
      && Objects.equals(this.getFrequencyDefinition(), other.getFrequencyDefinition());
  }
}
