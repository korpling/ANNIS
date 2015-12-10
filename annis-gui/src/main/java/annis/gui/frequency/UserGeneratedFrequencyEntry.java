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
package annis.gui.frequency;

import annis.service.objects.FrequencyTableEntry;
import annis.service.objects.FrequencyTableEntryType;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class UserGeneratedFrequencyEntry implements Serializable
{
  private String nr;
  private String annotation;
  private String comment = "manually created";

  public String getNr()
  {
    return nr;
  }

  public void setNr(String nr)
  {
    this.nr = nr;
  }

  public String getAnnotation()
  {
    return annotation;
  }

  public void setAnnotation(String annotation)
  {
    this.annotation = annotation;
  }

  public String getComment()
  {
    return comment;
  }

  public void setComment(String comment)
  {
    this.comment = comment;
  }
  
  /**
   * Converts this object to a proper definition.
   * @return 
   */
  public FrequencyTableEntry toFrequencyTableEntry()
  {
    FrequencyTableEntry result = new FrequencyTableEntry();
    
    result.setReferencedNode(nr);
    
    if (annotation != null && "tok".equals(annotation))
    {
      result.setType(FrequencyTableEntryType.span);
    }
    else
    {
      result.setType(FrequencyTableEntryType.annotation);
      result.setKey(annotation);
    }
    
    return result;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 53 * hash + Objects.hashCode(this.nr);
    hash = 53 * hash + Objects.hashCode(this.annotation);
    return hash;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
    {
      return true;
    }
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final UserGeneratedFrequencyEntry other = (UserGeneratedFrequencyEntry) obj;
    if (!Objects.equals(this.nr, other.nr))
    {
      return false;
    }
    if (!Objects.equals(this.annotation, other.annotation))
    {
      return false;
    }
    return true;
  }

  
}
