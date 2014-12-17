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

import com.google.common.base.Splitter;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class FrequencyTableEntry implements Serializable
{
  private FrequencyTableEntryType type;

  private String key;

  private String referencedNode;

  public FrequencyTableEntry()
  {
    
  }
  
  /**
   * A constructor that takes the raw definition as argument.
   * 
   * A definition consists of two parts: the referenced node and the annotation 
   * name or "tok" separated by ":" 
   * @param definition 
   * @return 
   */
  public static FrequencyTableEntry parse(String definition)
  {
    List<String> splitted
      = Splitter.on(':').trimResults().omitEmptyStrings().limit(2).
      splitToList(definition);
    if (splitted.size() == 2)
    {
      FrequencyTableEntry entry = new FrequencyTableEntry();

      if ("meta".equals(splitted.get(0)))
      {
        entry.setReferencedNode(null);
        entry.setType(FrequencyTableEntryType.meta);
        entry.setKey(splitted.get(1));
      }
      else
      {
        entry.setReferencedNode(splitted.get(0));
        if ("tok".equals(splitted.get(1)))
        {
          entry.setType(FrequencyTableEntryType.span);
        }
        else
        {
          entry.setType(FrequencyTableEntryType.annotation);
          entry.setKey(splitted.get(1));
        }
      }
      return entry;
    }
    return null;
  }
  
  
  public FrequencyTableEntryType getType()
  {
    return type;
  }

  public void setType(FrequencyTableEntryType type)
  {
    this.type = type;
  }

  public String getKey()
  {
    return key;
  }

  public void setKey(String key)
  {
    this.key = key;
  }

  public String getReferencedNode()
  {
    return referencedNode;
  }

  public void setReferencedNode(String referencedNode)
  {
    this.referencedNode = referencedNode;
  }

  @Override
  public String toString()
  {
    switch(type)
    {
      case span:
        return referencedNode + ":tok";
      case annotation:
        return referencedNode + ":" + key;
      case meta:
        return "meta:" + key;
    }
    return super.toString();
  }
  
  
  
}
