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

import com.google.common.base.Preconditions;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.UI;
import java.lang.reflect.Field;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to generate unique IDs for components.
 * 
 * Each component will have the ID of it's parent component as prefix in the ID.
 * Thus assigning an ID to an component will always assign an ID to all it's
 * ancestors as well. Additionally it is checked that the siblings of a
 * component don't have the same ID when it is assigned.
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class IDGenerator
{
  private static Logger log = LoggerFactory.getLogger(IDGenerator.class);
  
  public static String assignID(Component c)
  {
    String fieldName = "c";
    if(c != null)
    {
      fieldName = c.getClass().getSimpleName();
    }
    return assignID(c, fieldName);
  }
  
  protected static String assignIDForField(HasComponents parent, Component c)
  {
    String fieldName = "c";
    if(parent != null && c != null)
    {
      // iterate over each field of the parent
      for(Field f : parent.getClass().getDeclaredFields())
      {
        if(Component.class.isAssignableFrom(f.getType()))
        {
          try
          {
            f.setAccessible(true);
            Component fieldComponent = (Component) f.get(parent);
            if(fieldComponent == c)
            {
              fieldName = f.getName();
            }
          }
          catch (IllegalArgumentException | IllegalAccessException | SecurityException ex)
          {
            log.warn("Could not automatically get field name for assigning ID", ex);
          }
        }
      }
    }
    return assignID(c, fieldName);
  }
  
  public static void assignIDForFields(HasComponents parent, Component... components)
  {
    for(Component c : components)
    {
      assignIDForField(parent, c);
    }
  }
  
  public static void assignIDForEachField(HasComponents parent)
  {
    if(parent != null)
    {
      Iterator<Component> itComponents = parent.iterator();
      while(itComponents.hasNext())
      {
        Component c = itComponents.next();
        assignIDForField(parent, c);
      }
    }
  }
  
  public static String assignID(Component c, String fieldName)
  {
    String id = null;
    if(c != null && fieldName != null && !fieldName.isEmpty())
    {
      Preconditions.checkArgument(c.isAttached(), "Component " + c.getConnectorId() + " must be attached before it can get an automatic ID.");
      id = c.getId();
      if(id == null)
      {
        // try to get the parent ID
        HasComponents parent = c.getParent();
        if(parent == null || parent instanceof UI)
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
