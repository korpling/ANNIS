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

import annis.security.User;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.ui.Notification;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A model that manages users.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class UserManagement
{
  private WebResource rootResource;

  private final Map<String, User> users = new TreeMap<>();
  
  private final Logger log = LoggerFactory.getLogger(UserManagement.class);
  
  public void createOrUpdateUser(User newUser)
  {
    if(rootResource != null)
    {
      WebResource res = rootResource.path("admin/users").path(newUser.getName());
      res.put(newUser);
      users.put(newUser.getName(), newUser);
    }
  }
  
  public void deleteUser(String userName)
  {
    if(rootResource != null)
    {
      WebResource res = rootResource.path("admin/users").path(userName);
      res.delete();
      users.remove(userName);
    }
  }
  
  public User setPassword(String userName, String newPassword)
  {

    WebResource res = rootResource.path("admin/users").path(userName).path("password");
    User newUser = res.post(User.class, newPassword);
    if(newUser != null)
    {
      users.put(newUser.getName(), newUser);
    }
    return newUser;

  }
  
  public boolean fetchUsers()
  {
    if(rootResource != null)
    {
      WebResource res = rootResource.path("admin/users");
      users.clear();
      try
      {
        List<User> list = res.get(new GenericType<List<User>>() {});
        for(User u : list)
        {
          users.put(u.getName(), u);
        }
        return true;
      }
      catch(UniformInterfaceException ex)
      {
        log.error("Could not get the list of users", ex);
      }
    }
    return false;
  }
  
  public User getUser(String userName)
  {
    return users.get(userName);
  }
  
  public Collection<User> getUsers()
  {
    return users.values();
  }
  
  public WebResource getRootResource()
  {
    return rootResource;
  }

  public void setRootResource(WebResource rootResource)
  {
    this.rootResource = rootResource;
  }
  
  
}
