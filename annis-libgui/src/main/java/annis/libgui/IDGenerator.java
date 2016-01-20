/*
 * Copyright 2016 SFB 632.
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
package annis.libgui;

import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import java.util.Iterator;

/**
 * Helper class to generate unique IDs for components.
 * 
 * Each component will have the ID of it's parent component as prefix in the ID.
 * Thus assigning an ID to an component will always assign an ID to all it's
 * ancestors as well.
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class IDGenerator
{
  public static String assignID(Component c)
  {
    String fieldName = "child";
    if(c != null)
    {
      fieldName = c.getClass().getSimpleName();
    }
    return assignID(c, fieldName);
  }
  
  public static String assignID(Component c, String fieldName)
  {
    String id = null;
    if(c != null && fieldName != null && !fieldName.isEmpty())
    {
      id = c.getId();
      if(id == null)
      {
        // try to get the parent ID
        HasComponents parent = c.getParent();
        if(parent == null)
        {
          // use class name as ID
          id = fieldName;
        }
        else
        {
          String parentID = parent.getId();
          if(parentID == null)
          {
            parentID = assignID(parent);
          }
          String idBase = parentID + ":" + fieldName;
          // check that no other child has the same ID
          int counter = 1;
          id = idBase;
          while(childHasID(parent, id))
          {
            id = idBase + "." + counter++; 
          }
        }
        c.setId(id);
      }
    }
    return id;
  }
  
  private static boolean childHasID(HasComponents parent, String id)
  {
    if(parent != null && id != null)
    {
      Iterator<Component> itChildren = parent.iterator();
      while(itChildren.hasNext())
      {
        Component child = itChildren.next();
        if(id.equals(child.getId()))
        {
          return true;
        }
      }
    }
    return false;
  }
}
