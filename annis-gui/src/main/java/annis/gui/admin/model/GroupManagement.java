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

package annis.gui.admin.model;

import annis.CaseSensitiveOrder;
import annis.security.Group;
import com.google.common.collect.ImmutableSet;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A model for groups.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class GroupManagement implements Serializable
{
  
  private final Logger log = LoggerFactory.getLogger(GroupManagement.class);
  
  private final Map<String, Group> groups = new TreeMap<>(CaseSensitiveOrder.INSTANCE);
  
  private WebResourceProvider webResourceProvider;
  
  public void clear()
  {
    groups.clear();
  }
  
  public boolean fetchFromService()
  {
    if(webResourceProvider != null)
    {
      WebResource res = webResourceProvider
        .getWebResource().path("admin/groups");
      groups.clear();
      try
      {
        List<Group> list = res.get(new GenericType<List<Group>>() {});
        for(Group g : list)
        {
          groups.put(g.getName(), g);
        }
        return true;
      }
      catch(UniformInterfaceException ex)
      {
        log.error("Could not get the list of groups", ex);
      }
    }
    return false;
  }
  
  public void createOrUpdateGroup(Group newGroup)
  {
    if(webResourceProvider != null)
    {
      WebResource res = webResourceProvider.getWebResource()
        .path("admin/groups").path(newGroup.getName());
      try
      {
        res.put(newGroup);
        groups.put(newGroup.getName(), newGroup);
      }
      catch(UniformInterfaceException ex)
      {
        log.warn("Could not update group", ex);
      }
      
    }
  }
  
  public void deleteGroup(String groupName)
  {
    if(webResourceProvider != null)
    {
      WebResource res = webResourceProvider
        .getWebResource().path("admin/groups").path(groupName);
      res.delete();
      groups.remove(groupName);
    }
  }
  
  public Group getGroup(String groupName)
  {
    return groups.get(groupName);
  }
  
  public Collection<Group> getGroups()
  {
    return groups.values();
  }
  
  public ImmutableSet<String> getGroupNames()
  {
    return ImmutableSet.copyOf(groups.keySet());
  }

  public WebResourceProvider getWebResourceProvider()
  {
    return webResourceProvider;
  }

  public void setWebResourceProvider(WebResourceProvider webResourceProvider)
  {
    this.webResourceProvider = webResourceProvider;
  }
}
